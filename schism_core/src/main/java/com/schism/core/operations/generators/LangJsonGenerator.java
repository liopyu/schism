package com.schism.core.operations.generators;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.schism.core.Log;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class LangJsonGenerator
{
    private static final LangJsonGenerator INSTANCE = new LangJsonGenerator();
    public static LangJsonGenerator get()
    {
        return INSTANCE;
    }

    private final Gson gson;
    public LangJsonGenerator()
    {
        this.gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    }

    public void generate(String namespace)
    {
        Log.infoTitle("generator", "Lang JSON Files Generator");

        try {
            URI rootUri = Objects.requireNonNull(this.getClass().getResource("/generate/lang")).toURI();
            Log.info("generator", "Generating lang jsons from lang folders at: " + rootUri);
            final Path rootPath = Paths.get(rootUri);

            // Walk Files:
            final Iterator<Path> iterator = Files.walk(rootPath).iterator();
            while (iterator.hasNext()) {
                Path filePath = iterator.next();

                // Only handle root directories:
                Path path = rootPath.relativize(filePath);
                if (!filePath.toFile().isDirectory() || path.toString().equals("") || path.toString().split(Pattern.quote("/")).length > 1) {
                    continue;
                }

                this.generateLang(namespace, path.toString(), rootPath);
            }
        } catch (Exception e) {
            Log.error("generator", "An exception occurred when generating lang jsons.");
            e.printStackTrace();
        }
    }

    public void generateLang(String modId, String lang, Path rootPath)
    {
        Log.info("generator", "Generating lang: " + lang + " at " + rootPath);
        List<JsonObject> jsonObjects = new ArrayList<>();

        // Walk Files and Read Json:
        try {
            final Iterator<Path> iterator = Files.walk(rootPath).iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();

                // Skip non-ogg files:
                if (!"json".equals(FilenameUtils.getExtension(path.toString()))) {
                    continue;
                }

                // Read Json:
                Log.info("generator", "Reading lang json: " + path);
                BufferedReader reader = null;
                jsonObjects.add(this.readJson(Files.newBufferedReader(path)));
            }
        } catch (Exception e) {
            Log.error("generator", "An exception occurred when reading lang part json files for: " + lang);
            e.printStackTrace();
        }

        // Merge Json:
        Log.info("generator", "Loaded " + jsonObjects.size() + " json lang part files.");
        Map<String, String> langMap = new HashMap<>();
        jsonObjects.forEach(jsonObject -> {
            Map<Integer, List<String>> keyPartsMap = new HashMap<>();
            jsonObject.keySet().forEach(key -> {
                String[] keyParts = key.split(Pattern.quote("."));
                for (int i = 0; i < keyParts.length; i++) {
                    if (keyParts[i].equals("*")) { // Dont add wildcard key parts.
                        continue;
                    }
                    if (!keyPartsMap.containsKey(i)) {
                        keyPartsMap.put(i, new ArrayList<>());
                    }
                    if (!keyPartsMap.get(i).contains(keyParts[i])) {
                        keyPartsMap.get(i).add(keyParts[i]);
                    }
                }
            });
            jsonObject.keySet().forEach(key -> {
                if (key.contains("*")) { // Expand wildcards. Not recursive for now.
                    String[] keyParts = key.split(Pattern.quote("."));
                    for (int i = 0; i < keyParts.length; i++) {
                        if (keyParts[i].equals("*") && keyPartsMap.containsKey(i)) {
                            keyPartsMap.get(i).forEach(keyPart -> langMap.put(key.replace("*", keyPart), jsonObject.get(key).getAsString()));
                            break;
                        }
                    }
                } else {
                    langMap.put(key, jsonObject.get(key).getAsString());
                }
            });
        });
        JsonObject langJson = new JsonObject();
        langMap.keySet().stream().sorted().forEach(key -> langJson.addProperty(key, langMap.get(key)));

        // Write:
        try {
            String assetsPath = (new File(".")) + "/../src/main/resources/assets/" + modId;
            File file = new File(assetsPath + "/lang/" + lang + ".json");
            file.getParentFile().mkdirs();
            try (final PrintWriter printWriter = new PrintWriter(file)) {
                final JsonWriter jsonWriter = this.gson.newJsonWriter(printWriter);
                jsonWriter.setIndent("    ");
                this.gson.toJson(langJson, jsonWriter);
                printWriter.println();
                Log.info("generator", "Lang saved to " + file.toString());
            } catch (IOException e) {
                Log.error("generator", "An exception occurred when writing " + lang + ".json.");
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.error("generator", "An exception occurred when writing " + lang + ".json.");
            e.printStackTrace();
        }
    }

    /**
     * Reads json data from the provided buffered reader into a json object.
     * @param reader The file reader to read data from.
     * @return Returns a newly created Json Object based on the read file data.
     */
    public JsonObject readJson(BufferedReader reader) throws IOException
    {
        final JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setLenient(false);
        return this.gson.getAdapter(JsonObject.class).read(jsonReader);
    }
}

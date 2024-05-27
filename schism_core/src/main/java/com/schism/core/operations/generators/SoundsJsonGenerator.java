package com.schism.core.operations.generators;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.schism.core.Log;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoundsJsonGenerator
{
    private static final SoundsJsonGenerator INSTANCE = new SoundsJsonGenerator();

    public static SoundsJsonGenerator get()
    {
        return INSTANCE;
    }

    public void generate(String namespace)
    {
        Log.infoTitle("generator", "Sounds JSON File Generator");
        Map<String, SoundsJsonEntry> entries = this.getEntries(namespace);
        Log.info("generator", "Loaded " + entries.size() + " entries for inserting into sounds.json.");

        JsonObject json = new JsonObject();
        entries.values().stream().sorted().forEach(entry -> json.add(entry.key(), entry.toJson()));

        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        try {
            String assetsPath = (new File(".")) + "/../src/main/resources/assets/" + namespace;
            File file = new File(assetsPath + "/sounds.json");
            file.getParentFile().mkdirs();
            try (final PrintWriter printWriter = new PrintWriter(file)) {
                final JsonWriter jsonWriter = gson.newJsonWriter(printWriter);
                jsonWriter.setIndent("    ");
                gson.toJson(json, jsonWriter);
                printWriter.println();
                Log.info("generator", "Saved to " + file.toString());
            } catch (IOException e) {
                Log.error("generator", "An exception occurred when writing sounds.json.");
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.error("generator", "An exception occurred when writing sounds.json.");
            e.printStackTrace();
        }
    }

    public Map<String, SoundsJsonEntry> getEntries(String modId)
    {
        Map<String, SoundsJsonEntry> entries = new HashMap<>();
        final Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");

        // Generate from sound files:
        try {
            URI rootUri = Objects.requireNonNull(this.getClass().getResource("/assets/" + modId + "/sounds")).toURI();
            Log.info("generator", "Generating sounds.json from sound files at: " + rootUri);
            final Path rootPath = Paths.get(rootUri);

            // Walk Files:
            final Iterator<Path> iterator = Files.walk(rootPath).iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();

                // Skip non-ogg files:
                if (!"ogg".equals(FilenameUtils.getExtension(path.toString()))) {
                    continue;
                }

                // Extract path:
                Path relativePath = rootPath.relativize(path);
                String[] pathParts = relativePath.toString().split(Pattern.quote("/"));
                if (pathParts.length < 2) {
                    continue;
                }
                pathParts[pathParts.length - 1] = pathParts[pathParts.length - 1].replace(".ogg", "");

                // Check for variations:
                String strippedPath = relativePath.toString();
                int variationIndex = 1;
                Matcher matcher = lastIntPattern.matcher(strippedPath);
                if (matcher.find()) {
                    String variationString = matcher.group(1);
                    strippedPath = strippedPath.substring(0, strippedPath.length() - variationString.length());
                    variationIndex = Integer.parseInt(variationString);
                }

                // Existing sound variant:
                if (entries.containsKey(strippedPath)) {
                    Log.info("generator", "Adding variant file: " + relativePath);
                    entries.get(strippedPath).addVariation(variationIndex);
                    continue;
                }

                // Add sound file:
                Log.info("generator", "Adding sound file: " + relativePath);
                String category = resolveCategory(pathParts);
                entries.put(strippedPath, new SoundsJsonEntry(modId, pathParts, category));
            }
        } catch (Exception e) {
            Log.error("generator", "An exception occurred when generating sounds.json.");
            e.printStackTrace();
        }

        return entries;
    }

    /**
     * Determines a sound's category from the provided path parts.
     * @param pathParts An array of folder and file names that make up a sound's relative path.
     * @return The sound category to use.
     */
    public String resolveCategory(String[] pathParts)
    {
        return switch(pathParts[0]) {
            case "block" -> "block";
            case "entity" -> "hostile"; // For Creatures: Check EntityDefinition to determine if hostile or neutral.
            case "encounter" -> "record";
            default -> "ambient";
        };
    }
}

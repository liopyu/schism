package com.schism.core.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.schism.core.Log;
import com.schism.core.Schism;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DataSource
{
    /**
     * Data source location types, these determine how data is located.
     */
    public enum Location
    {
        INTERNAL, // Data is located inside the mod jar file (or project resources directory when in a dev environment).
        CONFIG, // Data is located in the config directory and can be freely edited by users.
        REMOTE // Data is stored at a remote location such as from a url, not yet implemented.
    }

    /**
     * The name of this data source.
     */
    private final String name;

    /**
     * The location type that this data source should use when loading data.
     */
    private final Location location;

    /**
     * The gson json formatter.
     */
    private final Gson gson;

    /**
     * Constructor
     * @param name The name of this data source.
     * @param location The type of location that this source should read data from.
     */
    public DataSource(String name, Location location)
    {
        this.name = name;
        this.location = location;
        this.gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    }

    @Override
    public String toString()
    {
        return "Datasource(" + this.name + "@" + this.location + ")";
    }

    /**
     * Gets this Data Source's Location type.
     * @return The location type.
     */
    public Location location()
    {
        return this.location;
    }

    /**
     * Loads json data from this source.
     * @return A list of Json Objects.
     */
    public List<DatabaseJson> loadJson(Map<DataSource, List<DatabaseJson>> jsonDatabase)
    {
        switch (this.location) {
            case INTERNAL -> {
                return this.loadJsonInternal();
            }
            case CONFIG -> {
                return this.loadJsonConfig();
            }
            case REMOTE -> {
                return new ArrayList<>(); // Not yet implemented.
            }
            default -> {
                return new ArrayList<>();
            }
        }
    }

    /**
     * Loads json data from this source via an internal location type (inside the mod jarfile or project directory).
     * @return A list of Json Objects.
     */
    public List<DatabaseJson> loadJsonInternal()
    {
        try {
            // Determine a root directory using the name of this data source and by searching for a .root file.
            URI rootUri = Objects.requireNonNull(this.getClass().getResource("/" + this.name + "/" + ".root")).toURI();
            switch (rootUri.getScheme()) {

                // File/Union: This is called when in a development environment.
                case "file", "union" -> {
                    final Path rootFilePath = Paths.get(rootUri);
                    final String rootPathString = rootFilePath.toString().replace(rootFilePath.getFileName().toString(), ""); // Set the root path to the path that is leading to the .root file with the filename removed.
                    final Path rootPath = rootFilePath.getFileSystem().getPath(rootPathString);
                    return this.loadJsonObjects(rootPath);
                }

                // Mod Jar: This is called when the mod is running from a jarfile, this can be when live or when running the mod jar in a development environment.
                case "modjar" -> {
                    final JarFile jar = new JarFile(this.getJarFile());
                    final Enumeration<JarEntry> entries = jar.entries();
                    return this.loadJsonObjects(entries);
                }

                // This should never be reached but the weird and wonderful world of the internet always finds a way!
                default -> throw new RuntimeException("Unsupported file scheme: " + rootUri.getScheme());
            }
        } catch (Exception e) {
            Log.error("datasource", "An exception occurred when trying to load from: " + this);
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads json data from this source via an internal location type (inside the mod jarfile or project directory).
     * @return A list of Json Objects.
     */
    public List<DatabaseJson> loadJsonConfig()
    {
        String configRootPath = (new File(".")) + "/config/" + Schism.NAMESPACE + "/";
        File rootPathFile = new File(configRootPath + this.name);
        rootPathFile.mkdirs(); // Create the data source folder if it doesn't exist.
        Path rootPath = rootPathFile.toPath();
        try {
            return this.loadJsonObjects(rootPath);
        } catch (IOException e) {
            Log.error("datasource", "An exception occurred when trying to load from: " + this);
            throw new RuntimeException(e);
        }
    }

    /**
     * Walks through all json files and directories recursively in the provided path and returns a list of json objects.
     * @param path The path to walk.
     * @return A list of Json Objects.
     */
    public List<DatabaseJson> loadJsonObjects(Path path) throws IOException
    {
        final List<DatabaseJson> databaseJsons = new ArrayList<>();
        final Iterator<Path> iterator = Files.walk(path).iterator();
        while (iterator.hasNext()) {
            Path filePath = iterator.next();

            // Skip non-json files:
            Path relativePath = path.relativize(filePath);
            if (!"json".equals(FilenameUtils.getExtension(filePath.toString()))) {
                continue;
            }

            BufferedReader reader = null;
            try {
                reader = Files.newBufferedReader(filePath);
                databaseJsons.add(DatabaseJson.fromPath(this.readJson(reader), relativePath.toString()));
            } catch (JsonParseException e) {
                Log.error("datasource", "Error parsing json from the file: " + relativePath + " using: " + this);
                throw new RuntimeException(e);
            } catch (EOFException e) {
                Log.error("datasource", "Error reading json from an empty file at: " + relativePath + " using: " + this);
                throw new RuntimeException(e);
            } catch (Exception e) {
                Log.error("datasource", "An exception occurred when trying to read data from: " + relativePath + " using: " + this);
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
        return databaseJsons;
    }

    /**
     * Walks through all json files in the provided jar file entry enumeration and returns a list of json objects.
     * @param entries The jar file entry enumeration to walk.
     * @return A list of Json Objects.
     */
    public List<DatabaseJson> loadJsonObjects(Enumeration<JarEntry> entries) throws IOException
    {
        final List<DatabaseJson> databaseJsons = new ArrayList<>();
        while (entries.hasMoreElements()) {
            final String entryName = entries.nextElement().getName();
            if (entryName.startsWith(this.name + "/") && !entryName.endsWith("/")) {
                InputStream inputStream = this.getClass().getResourceAsStream("/" + entryName);
                if (inputStream == null) {
                    throw new RuntimeException("Error streaming file entry from mod jar: /" + entryName);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                databaseJsons.add(DatabaseJson.fromPath(this.readJson(reader), entryName));
            }
        }
        return databaseJsons;
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

    /**
     * Gets this mod's jarfile for reading data from.
     * @return A File instance pointing to this mod's jarfile.
     */
    public File getJarFile()
    {
        try {
            final Path path = new File("mods").toPath();
            final Iterator<Path> iterator = Files.walk(path).iterator();
            while (iterator.hasNext()) {
                Path filePath = iterator.next();
                if (filePath.getFileName().toString().toLowerCase().contains(Schism.NAMESPACE)) {
                    return filePath.toFile();
                }
            }
            throw new RuntimeException("Unable to locate mod jar file.");
        } catch (Exception e) {
            Log.error("datasource", "Error locating mod jar file.");
            throw new RuntimeException(e);
        }
    }
}

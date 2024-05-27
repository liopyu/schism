package com.schism.core.operations.generators;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SoundsJsonEntry implements Comparable<SoundsJsonEntry>
{
    protected final String namespace;
    protected final String[] pathParts;
    protected final String category;
    protected final boolean stream;
    protected int variations = 0;

    /**
     * Constructor
     * @param namespace The namespace of the mod (should match the assets subfolder name).
     * @param pathParts The sound path broken into an array relative to the sounds folder and with the sound extension removed.
     * @param category The sound category to use.
     */
    public SoundsJsonEntry(String namespace, String[] pathParts, String category)
    {
        this.namespace = namespace;
        this.pathParts = pathParts;
        this.category = category;
        this.stream = category.equals("record");
    }

    /**
     * Adds a new sound variation index, if it is high the variations count will increase to it.
     * Note: If no variations are added, the count will be at 0 indicating that no number should be appended to entries.
     * @param index The sound variation number.
     */
    public void addVariation(int index)
    {
        this.variations = Math.max(this.variations, index);
    }

    /**
     * Creates the key for this sound entry.
     * @return The sound entry key.
     */
    public String key()
    {
        return String.join(".", this.pathParts);
    }

    /**
     * Creates a sound entry json.
     * @return A sound entry json object.
     */
    public JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        json.addProperty("category", this.category);

        String path = this.namespace + ":" + String.join("/", this.pathParts);
        JsonArray sounds = new JsonArray();
        if (this.variations > 0) {
            for (int i = 1; i < this.variations; i++) {
                sounds.add(this.createSoundPathJson(path + String.format(Locale.ENGLISH, "%02d", i)));
            }
        } else {
            sounds.add(this.createSoundPathJson(path));
        }
        json.add("sounds", sounds);

        return json;
    }

    /**
     * Creates a sound path json object.
     * @param path The path to use.
     * @return A sound path json object for adding to the "sounds" property of a sound entry json object.
     */
    protected JsonObject createSoundPathJson(String path)
    {
        JsonObject soundJson = new JsonObject();
        soundJson.addProperty("name", path);
        soundJson.addProperty("stream", this.stream);
        return soundJson;
    }

    @Override
    public String toString()
    {
        return this.key();
    }

    @Override
    public int compareTo(@NotNull SoundsJsonEntry entry)
    {
        return this.key().compareTo(entry.key());
    }
}

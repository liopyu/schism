package com.schism.core.database;

import com.google.gson.JsonObject;

import java.util.Objects;
import java.util.regex.Pattern;

public record DatabaseJson(JsonObject jsonObject, String subject, String type, String relativePath)
{
    /**
     * A database json entry, this contains the original json object as well as information about it. Json filenames are interpreted as 'subject.type.json'.
     * @param jsonObject The original json object.
     * @param subject The subject that the json data is for, this is determined from the filename ignoring the file extensions (separated by periods '.', ex: aspid.creature.json would have aspid as the subject).
     * @param type The type of data that the json object contains for its subject, this is determined from the filenames second extension (ex: aspid.creature.json would be creature type).
     * @param relativePath The relative path for the json file, this is mainly used when generated default config files so that they can copy the internal file structure.
     */
    public DatabaseJson
    {
        Objects.requireNonNull(jsonObject);
        Objects.requireNonNull(subject);
        Objects.requireNonNull(type);
        Objects.requireNonNull(relativePath);
    }

    /**
     * A database json entry, this contains the original json object as well as information about it. Json filenames are interpreted as 'subject.type.json'.
     * @param jsonObject The original json object.
     * @param relativePath The relative path for the json file, this is mainly used when generated default config files so that they can copy the internal file structure.
     */
    public static DatabaseJson fromPath(JsonObject jsonObject, String relativePath)
    {
        relativePath = relativePath.replace("\\", "/"); // Get rid of those smelly windows backslashes!
        String[] pathParts = relativePath.split(Pattern.quote("/"));
        String[] filenameParts = pathParts[pathParts.length - 1].split(Pattern.quote("."));
        if (filenameParts.length != 3) {
            throw new RuntimeException("Invalid json filename at: " + relativePath + " must follow the naming pattern of: subject.type.json");
        }
        return new DatabaseJson(jsonObject, filenameParts[0], filenameParts[1], relativePath);
    }
}

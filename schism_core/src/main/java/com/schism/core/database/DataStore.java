package com.schism.core.database;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;
import com.schism.core.util.Vec4;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.regex.Pattern;

public class DataStore
{
    enum Type
    {
        PRIMITIVE,
        ARRAY,
        OBJECT,
        INVALID
    }

    /**
     * The determined type of this data store.
     */
    private final Type type;

    /**
     * A collection of nested data stores for array types.
     */
    private List<DataStore> arrayList;

    /**
     * A map of nested data stores for object types.
     */
    private LinkedHashMap<String, DataStore> objectMap;

    /**
     * The primitive string value of this definition, an empty string when unset.
     */
    private String stringValue = "";

    /**
     * The primitive boolean value of this definition, defaults to false.
     */
    private boolean booleanValue = false;

    /**
     * The primitive integer value of this definition, defaults to 0.
     */
    private int intValue = 0;

    /**
     * The primitive float value of this definition, defaults to 0.
     */
    private float floatValue = 0;

    /**
     * Constructor
     * @param map The Map to create a data store from, this does a shallow copy.
     */
    public DataStore(Map<String, DataStore> map)
    {
        this.type = Type.OBJECT;
        this.objectMap = new LinkedHashMap<>(map);
    }

    /**
     * Constructor
     * @param list The List to create a data store from, this does a shallow copy.
     */
    public DataStore(List<DataStore> list)
    {
        this.type = Type.ARRAY;
        this.arrayList = new ArrayList<>(list);
    }

    /**
     * Constructor
     * @param value The primitive value to create a data store for.
     */
    public DataStore(String value)
    {
        this.type = Type.PRIMITIVE;
        this.stringValue = value;
        this.booleanValue = Boolean.parseBoolean(value);
        this.intValue = 0;
        this.floatValue = 0;
    }

    /**
     * Constructor
     * @param value The primitive value to create a data store for.
     */
    public DataStore(boolean value)
    {
        this.type = Type.PRIMITIVE;
        this.stringValue = String.valueOf(value);
        this.booleanValue = value;
        this.intValue = value ? 1 : 0;
        this.floatValue = value ? 1 : 0;
    }

    /**
     * Constructor
     * @param value The primitive value to create a data store for.
     */
    public DataStore(int value)
    {
        this.type = Type.PRIMITIVE;
        this.stringValue = String.valueOf(value);
        this.booleanValue = value > 0;
        this.intValue = value;
        this.floatValue = value;
    }

    /**
     * Constructor
     * @param value The primitive value to create a data store for.
     */
    public DataStore(float value)
    {
        this.type = Type.PRIMITIVE;
        this.stringValue = String.valueOf(value);
        this.booleanValue = value > 0;
        this.intValue = Math.round(value);
        this.floatValue = value;
    }

    /**
     * Constructor
     * @param jsonElement A json element to set the initial data and type of this data store.
     */
    public DataStore(JsonElement jsonElement)
    {
        if (jsonElement.isJsonObject()) {
            this.type = Type.OBJECT;
            this.objectMap = new LinkedHashMap<>();
        } else if (jsonElement.isJsonArray()) {
            this.type = Type.ARRAY;
            this.arrayList = new ArrayList<>();
        } else if (jsonElement.isJsonPrimitive() || jsonElement.isJsonNull()) {
            this.type = Type.PRIMITIVE;
        } else {
            this.type = Type.INVALID;
        }
        this.mergeJsonElement(jsonElement);
    }

    /**
     * Merges the provided json element into this data store.
     * @param jsonElement The json element to merge data from. Primitive/null values will replace the current value, collection values will be added and object map entries merged replacing duplicate keys.
     */
    public void mergeJsonElement(JsonElement jsonElement)
    {
        // Handle Primitive/Null:
        if (jsonElement.isJsonPrimitive() || jsonElement.isJsonNull()) {
            if (this.type != Type.PRIMITIVE) {
                throw new RuntimeException("An overriding json element (primitive/null) is of a different type to the element it is trying to override. Override: " + jsonElement + " Original Type: " + this.type);
            }
            this.setPrimitiveValue(jsonElement);
            return;
        }

        // Handle Array:
        if (jsonElement.isJsonArray()) {
            if (this.type != Type.ARRAY) {
                throw new RuntimeException("An overriding json element (array) is of a different type to the element it is trying to override. Override: " + jsonElement + " Original Type: " + this.type);
            }
            jsonElement.getAsJsonArray().forEach(arrayElement -> this.arrayList.add(new DataStore(arrayElement)));
            return;
        }

        // Handle Object:
        if (jsonElement.isJsonObject()) {
            if (this.type != Type.OBJECT) {
                throw new RuntimeException("An overriding json element (object) is of a different type to the element it is trying to override. Override: " + jsonElement + " Original Type: " + this.type);
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            jsonObject.keySet().forEach(key -> {
                if (jsonObject.get(key).isJsonNull()) { // Remove existing object map entries if a null entry is deliberately passed.
                    this.objectMap.remove(key);
                } else {
                    this.objectMap.put(key, new DataStore(jsonObject.get(key)));
                }
            });
            return;
        }

        throw new RuntimeException("Unable to merge a json element into a data store: " + jsonElement);
    }

    /**
     * Returns the type of this data store.
     * @return An entry type. Primitives store single values, arrays an iterable collection and objects a mapped collection.
     */
    public Type getType()
    {
        return this.type;
    }

    /**
     * Gets a nested data store by key.
     * @param key The key to get a string for, can get nested props using periods, ex: 'liquid.reactions'.
     * @return The nested data store or null if not present.
     */
    public DataStore prop(String key)
    {
        if (this.objectMap == null) {
            return null;
        }
        List<String> keys = new ArrayList<>(List.of(key.split(Pattern.quote("."))));
        String valueKey = keys.get(keys.size() - 1);

        // Bore into nested object data stores:
        if (keys.size() > 1) {
            String nextKey = keys.remove(0);
            if (!this.objectMap.containsKey(nextKey)) {
                return null;
            }
            return this.objectMap.get(nextKey).prop(String.join(".", keys));
        }

        // Return from this object data store:
        return this.objectMap.getOrDefault(valueKey, null);
    }

    /**
     * Returns if the provided key has been set. Note that values from unset keys will all return as 0, false, an empty string/list, etc.
     * @param key The key to check for.
     * @return True if the key has been set.
     */
    public boolean has(String key)
    {
        return this.prop(key) != null;
    }

    /**
     * Gets all object map data stores for the provided key.
     * @param key The key to get a collection for, can get nested props using periods, ex: 'liquid.reactions'.
     * @return A collection of the nested data stores.
     */
    public Map<String, DataStore> mapProp(String key)
    {
        DataStore dataStore = this.prop(key);
        if (dataStore == null) {
            return new LinkedHashMap<>();
        }
        return dataStore.objectMap();
    }

    /**
     * Gets all array list data stores for the provided key.
     * @param key The key to get a collection for, can get nested props using periods, ex: 'liquid.reactions'.
     * @return A list of the nested data stores.
     */
    public List<DataStore> listProp(String key)
    {
        DataStore dataStore = this.prop(key);
        if (dataStore == null) {
            return new ArrayList<>();
        }
        return dataStore.arrayList();
    }

    /**
     * Gets all array list data stores for the provided key.
     * @param key The key to get a collection for, can get nested props using periods, ex: 'liquid.reactions'.
     * @return A list of the nested data stores.
     */
    public List<Direction> directionListProp(String key)
    {
        DataStore dataStore = this.prop(key);
        if (dataStore == null) {
            return new ArrayList<>();
        }
        return dataStore.directionListValue();
    }

    /**
     * Gets a string property for the provided key.
     * @param key The key to get a string for, can get nested props using periods, ex: 'liquid.reactions'.
     * @return A string property, empty if invalid.
     */
    public String stringProp(String key)
    {
        DataStore dataStore = this.prop(key);
        if (dataStore == null) {
            return "";
        }
        return dataStore.stringValue();
    }

    /**
     * Gets a boolean property for the provided key.
     * @param key The key to get a boolean for.
     * @return A boolean property, false if invalid.
     */
    public boolean booleanProp(String key)
    {
        DataStore dataStore = this.prop(key);
        if (dataStore == null) {
            return false;
        }
        return dataStore.booleanValue();
    }

    /**
     * Gets an integer property for the provided key.
     * @param key The key to get an integer for.
     * @return An integer property, 0 if invalid.
     */
    public int intProp(String key)
    {
        DataStore dataStore = this.prop(key);
        if (dataStore == null) {
            return 0;
        }
        return dataStore.intValue();
    }

    /**
     * Gets a float property for the provided key.
     * @param key The key to get a float for.
     * @return A float property, 0 if invalid.
     */
    public float floatProp(String key)
    {
        DataStore dataStore = this.prop(key);
        if (dataStore == null) {
            return 0;
        }
        return dataStore.floatValue();
    }

    /**
     * Gets the vector 2 value for the provided key (requires an array of at least 2 floats).
     * @param key The key to get a vector 2 value for.
     * @return The vector 2 value of this data store, a zero vector 2 if invalid.
     */
    public Vec2 vec2Prop(String key)
    {
        DataStore dataStore = this.prop(key);
        if (dataStore == null) {
            return Vec2.ZERO;
        }
        return dataStore.vec2Value();
    }

    /**
     * Gets the vector 3 value for the provided key (requires an array of at least 3 floats).
     * @param key The key to get a vector 3 value for.
     * @return The vector 3 value of this data store, a zero vector 3 if invalid.
     */
    public Vec3 vec3Prop(String key)
    {
        DataStore dataStore = this.prop(key);
        if (dataStore == null) {
            return Vec3.ZERO;
        }
        return dataStore.vec3Value();
    }

    /**
     * Gets the vector 4 value for the provided key (requires an array of at least 4 floats).
     * @param key The key to get a vector 4 value for.
     * @return The vector 4 value of this data store, a zero vector 4 if invalid.
     */
    public Vec4 vec4Prop(String key)
    {
        DataStore dataStore = this.prop(key);
        if (dataStore == null) {
            return Vec4.ZERO;
        }
        return dataStore.vec4Value();
    }

    /**
     * Gets a collection of nested data stores.
     * @return A collection of nested data stores.
     */
    public LinkedHashMap<String, DataStore> objectMap()
    {
        if (this.objectMap == null) {
            return new LinkedHashMap<>();
        }
        return this.objectMap;
    }

    /**
     * Gets a collection of nested data stores.
     * @return A collection of nested data stores.
     */
    public List<DataStore> arrayList()
    {
        if (this.arrayList == null) {
            return new ArrayList<>();
        }
        return this.arrayList;
    }

    /**
     * Gets the string value of this data store.
     * @return The string value of this data store.
     */
    public String stringValue()
    {
        return this.stringValue;
    }

    /**
     * Gets the boolean value of this data store.
     * @return The boolean value of this data store.
     */
    public boolean booleanValue()
    {
        return this.booleanValue;
    }

    /**
     * Gets the integer value of this data store.
     * @return The integer value of this data store.
     */
    public int intValue()
    {
        return this.intValue;
    }

    /**
     * Gets the float value of this data store.
     * @return The float value of this data store.
     */
    public float floatValue()
    {
        return this.floatValue;
    }

    /**
     * Gets the vector 2 value of this data store (requires an array of at least 2 floats).
     * @return The vector 2 value of this data store.
     */
    public Vec2 vec2Value()
    {
        if (this.arrayList == null || this.arrayList.size() < 2) {
            return Vec2.ZERO;
        }
        return new Vec2(this.arrayList.get(0).floatValue(), this.arrayList.get(1).floatValue());
    }

    /**
     * Gets the vector 3 value of this data store (requires an array of at least 3 floats).
     * @return The vector 3 value of this data store.
     */
    public Vec3 vec3Value()
    {
        if (this.arrayList == null || this.arrayList.size() < 3) {
            return Vec3.ZERO;
        }
        return new Vec3(this.arrayList.get(0).floatValue(), this.arrayList.get(1).floatValue(), this.arrayList.get(2).floatValue());
    }

    /**
     * Gets the vector 4 value of this data store (requires an array of at least 4 floats).
     * @return The vector 4 value of this data store.
     */
    public Vec4 vec4Value()
    {
        if (this.arrayList == null || this.arrayList.size() < 4) {
            return Vec4.ZERO;
        }
        return new Vec4(this.arrayList.get(0).floatValue(), this.arrayList.get(1).floatValue(), this.arrayList.get(2).floatValue(), this.arrayList.get(3).floatValue());
    }

    /**
     * Gets this data store's list as a list of directions.
     * @return A list of directions.
     */
    public List<Direction> directionListValue()
    {
        List<Direction> directions = new ArrayList<>();
        directions.addAll(this.arrayList().stream()
                .map(dataStoreItem -> {
                    if (dataStoreItem.stringValue().equals("cardinal")) {
                        directions.add(Direction.NORTH);
                        directions.add(Direction.EAST);
                        directions.add(Direction.SOUTH);
                        directions.add(Direction.WEST);
                    }
                    return dataStoreItem.stringValue();
                })
                .map(Direction::byName).filter(Objects::nonNull).toList());
        return directions;
    }

    /**
     * Sets the primitive value of this data store from the provided json element (assuming json primitive or null).
     * @param jsonElement The json element to read a value from.
     */
    private void setPrimitiveValue(JsonElement jsonElement)
    {
        if (jsonElement.isJsonNull()) {
            this.stringValue = "";
            this.booleanValue = false;
            this.intValue = 0;
            this.floatValue = 0;
            return;
        }

        if (!jsonElement.isJsonPrimitive()) {
            throw new RuntimeException("An invalid json element has been passed for extracting a primitive or null value from.");
        }

        JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
        this.stringValue = jsonPrimitive.getAsString();
        this.booleanValue = jsonPrimitive.getAsBoolean();
        this.intValue = jsonPrimitive.isNumber() ? jsonPrimitive.getAsInt() : 0;
        this.floatValue = jsonPrimitive.isNumber() ? jsonPrimitive.getAsFloat() : 0;
    }

    /**
     * Helper function that determines if the provided resource location id is in the provided list of ids.
     * @param resourceLocation The resource location id to search for.
     * @param list A list of id strings to search.
     * @return True if the resource location id is in the provided list of id strings.
     */
    public static boolean idInList(ResourceLocation resourceLocation, List<String> list)
    {
        if (resourceLocation == null || list.isEmpty()) {
            return false;
        }
        return list.stream().anyMatch(Objects.requireNonNull(resourceLocation.toString()::equals));
    }
}

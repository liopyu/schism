package com.schism.core.client.models;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.schism.core.Log;
import com.schism.core.database.DataStore;
import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

public class ObjModel extends AbstractModel
{
    protected static final String COMMENT = "#";
    protected static final String OBJECT = "o"; // Object > Mesh
    protected static final String GROUP = "g"; // Group > Mesh
    protected static final String VERT = "v"; // Vertex > Vertex
    protected static final String FACE = "f"; // Face > Indices per vertex
    protected static final String TEX = "vt"; // Vertex Texture > UV
    protected static final String NORMAL = "vn"; // Vertex Normal > Normal
    protected static final String USE_MATERIAL = "usemtl"; // Materials are not used.
    protected static final String NEW_MATERIAL = "mtllib"; // Materials are not used.

    public ObjModel(String path)
    {
        super(path, "obj", "armature.json");
    }

    @Override
    public boolean load()
    {
        if (!super.load()) {
            return false;
        }
        try {
            this.loadMeshes();
            this.loadArmature();
        } catch (IOException e) {
            e.printStackTrace();
            Log.error("model", "Error reading obj model - Obj: " + this.modelResource + " Armature: " + this.armatureResource);
            return false;
        }

        return true;
    }

    /**
     * Loads meshes from a wavefront obj model file.
     * @throws IOException Throws an io exception on file reading errors.
     */
    public void loadMeshes() throws IOException
    {
        this.meshes = new ArrayList<>();

        InputStream inputStream = Minecraft.getInstance().getResourceManager().getResource(this.modelResource).getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> lines = bufferedReader.lines().toList();

        String meshName = null;
        List<Index> indices = new ArrayList<>();
        List<Vertex> vertices = new ArrayList<>();
        List<Normal> normals = new ArrayList<>();
        List<UV> uvs = new ArrayList<>();
        int offsetVertex = 0;
        int offsetUv = 0;
        int offsetNormal = 0;

        // Parse Lines:
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }

            // Parse line into parts:
            List<String> parts = Arrays.stream(line.split(Pattern.quote(" "))).filter(part -> !part.trim().isEmpty()).toList();
            if (parts.isEmpty()) {
                continue;
            }

            switch (parts.get(0)) {
                case OBJECT, GROUP -> {
                    if (meshName != null) { // Write Last Mesh:
                        this.meshes.add(new Mesh(meshName, indices, vertices, uvs, normals));

                        // Clear lists and update offsets:
                        offsetVertex += vertices.size();
                        offsetUv += uvs.size();
                        offsetNormal += normals.size();
                        indices.clear();
                        vertices.clear();
                        uvs.clear();
                        normals.clear();
                    }
                    meshName = parts.get(1); // Start New Mesh:
                }
                case FACE -> {
                    for(int i = 0; i < parts.size() - 3; i++) { // Handles tris, quads and up.
                        indices.add(this.parseIndex(parts.get(1), offsetVertex, offsetUv, offsetNormal));
                        indices.add(this.parseIndex(parts.get(2 + i), offsetVertex, offsetUv, offsetNormal));
                        indices.add(this.parseIndex(parts.get(3 + i), offsetVertex, offsetUv, offsetNormal));
                    }
                }
                case VERT -> {
                    vertices.add(new Vertex(Float.parseFloat(parts.get(1)), Float.parseFloat(parts.get(2)), Float.parseFloat(parts.get(3))));
                }
                case TEX -> {
                    uvs.add(new UV(Float.parseFloat(parts.get(1)), Float.parseFloat(parts.get(2))));
                }
                case NORMAL -> {
                    normals.add(new Normal(Float.parseFloat(parts.get(1)), Float.parseFloat(parts.get(2)), Float.parseFloat(parts.get(3))));
                }
            }
        }
        if (meshName != null) { // Write Last Mesh:
            this.meshes.add(new Mesh(meshName, indices, vertices, uvs, normals));
        }

        bufferedReader.close();
    }

    /**
     * Loads an armature from a json armature file.
     * @throws IOException Throws an io exception on file reading errors.
     */
    public void loadArmature() throws IOException
    {
        InputStream inputStream = Minecraft.getInstance().getResourceManager().getResource(this.armatureResource).getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        final JsonReader jsonReader = new JsonReader(bufferedReader);
        jsonReader.setLenient(false);
        JsonObject json = this.gson.getAdapter(JsonObject.class).read(jsonReader);

        // Read Bones:
        Map<String, Bone> bones = new HashMap<>();
        if (json.has("bones")) {
            json.getAsJsonArray("bones").forEach(jsonElement -> {
                DataStore dataStore = new DataStore(jsonElement);
                String name = dataStore.stringProp("name");
                String parent = dataStore.stringProp("parent");
                float x = dataStore.floatProp("x");
                float y = dataStore.floatProp("y");
                float z = dataStore.floatProp("z");
                Bone bone = new Bone(name, () -> bones.getOrDefault(parent, null), x, y, z);
                bones.put(bone.name(), bone);
            });
        }

        // Create Weights:
        List<Weight> weights = new ArrayList<>();
        bones.values().forEach(bone -> {
            Optional<Mesh> optionalMesh = this.meshes.stream().filter(mesh -> mesh.name().equals(bone.name())).findFirst();
            optionalMesh.ifPresent(mesh -> weights.add(new Weight(bone, mesh, 1)));
        });

        // Create Armature:
        this.armature = new Armature(bones, weights);

        bufferedReader.close();
    }

    /**
     * Parses an obj face into an index.
     * @param face The face to parse.
     * @param offsetVertex The vertex index offset to subtract, used when moving to the next object/group.
     * @param offsetUv The uv index offset to subtract, used when moving to the next object/group.
     * @param offsetNormal The normal index offset to subtract, used when moving to the next object/group.
     * @return A newly created Index.
     */
    protected Index parseIndex(String face, int offsetVertex, int offsetUv, int offsetNormal)
    {
        String[] values = face.split(Pattern.quote("/"));
        int vertex = Integer.parseInt(values[0]) - 1 - offsetVertex;
        int uv = 0;
        int normal = 0;
        if (values.length > 1) {
            if (!values[1].isEmpty()) {
                uv = Integer.parseInt(values[1]) - 1 - offsetUv;
            }
            if (values.length > 2 && !values[2].isEmpty()) {
                normal = Integer.parseInt(values[2]) - 1 - offsetNormal;
            }
        }
        return new Index(vertex, uv, normal);
    }
}

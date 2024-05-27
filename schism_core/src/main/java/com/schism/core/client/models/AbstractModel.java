package com.schism.core.client.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import com.schism.core.Log;
import com.schism.core.Schism;
import com.schism.core.client.renderers.RenderTypes;
import com.schism.core.database.DataStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractModel
{
    protected static final List<Runnable> RELOAD_LISTENERS = new ArrayList<>();

    protected final ResourceLocation textureBase;
    protected final ResourceLocation layersResource;
    protected final ResourceLocation modelResource;
    protected final ResourceLocation armatureResource;

    protected List<RenderType> renderTypes;
    protected final List<TextureLayer> layers;

    protected final Gson gson;

    protected boolean initialised;
    protected List<Mesh> meshes;
    protected Armature armature;

    /**
     * Creates a new model using the provided path and format.
     * @param path The model path to get resource relative from.
     * @param format The model format.
     */
    public static AbstractModel create(String path, String format)
    {
        return switch (format) {
            case "obj" -> new ObjModel(path);
            default -> {
                Log.info("model", "Invalid model format requested: " + format + " for path: " + path);
                yield null;
            }
        };
    }

    /**
     * Adds a runnable to be called whenever all models should be reloaded.
     * @param listener The runnable to call.
     * @return A runnable that removes the event listener when called.
     */
    public static Runnable addReloadListener(Runnable listener)
    {
        RELOAD_LISTENERS.add(listener);
        return () -> RELOAD_LISTENERS.remove(listener);
    }

    /**
     * Attempts to reload all models by calling all reload listeners.
     */
    public static void reloadAll()
    {
        RELOAD_LISTENERS.forEach(Runnable::run);
    }

    public AbstractModel(String path, String modelFormat, String armatureFormat)
    {
        this.textureBase = new ResourceLocation(Schism.NAMESPACE, "textures/" + path);
        this.layersResource = new ResourceLocation(Schism.NAMESPACE, "textures/" + path + ".layers.json");
        this.modelResource = new ResourceLocation(Schism.NAMESPACE, "models/" + path + "." + modelFormat);
        this.armatureResource = new ResourceLocation(Schism.NAMESPACE, "models/" + path + "." + armatureFormat);

        this.layers = new ArrayList<>();
        this.renderTypes = new ArrayList<>();

        this.gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    }

    /**
     * Loads and parses the model into cache.
     * @return True on a successful load, false if there was an issue.
     */
    public boolean load()
    {
        try {
            this.loadLayers();
            this.renderTypes = this.layers.stream().map(TextureLayer::blending).distinct().map(blending ->
                    RenderTypes.modelRenderType(this.layers, blending)).toList();
        } catch (IOException e) {
            e.printStackTrace();
            Log.error("model", "Error reading model layers: " + this.layersResource);
            return false;
        }
        return true;
    }

    /**
     * Loads a texture layers from a json layers file.
     * @throws IOException Throws an io exception on file reading errors.
     */
    public void loadLayers() throws IOException
    {
        InputStream inputStream = Minecraft.getInstance().getResourceManager().getResource(this.layersResource).getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        final JsonReader jsonReader = new JsonReader(bufferedReader);
        jsonReader.setLenient(false);
        JsonObject json = this.gson.getAdapter(JsonObject.class).read(jsonReader);

        // Read Layers:
        if (json.has("layers")) {
            json.getAsJsonArray("layers").forEach(jsonElement -> {
                DataStore dataStore = new DataStore(jsonElement);
                TextureLayer layer = new TextureLayer(dataStore, this.textureBase);
                this.layers.add(layer);
            });
        }

        bufferedReader.close();
    }

    /**
     * Renders this model.
     */
    public void render(PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource)
    {
        if (!this.initialised) {
            this.initialised = true;
            if (!this.load()) {
                return;
            }
        }

        Matrix4f poseMatrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();
        this.renderTypes.forEach(renderType -> {
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
            // TODO Animate using Weights, AnimationStacks and an AnimationState.
            this.meshes.forEach(mesh -> mesh.render(vertexConsumer, poseMatrix, normalMatrix));
        });
    }
}

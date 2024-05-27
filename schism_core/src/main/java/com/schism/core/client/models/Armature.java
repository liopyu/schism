package com.schism.core.client.models;

import java.util.List;
import java.util.Map;

public class Armature
{
    protected final Map<String, Bone> bones;
    protected final List<Weight> weights;

    /**
     * A model armature used for animation.
     * @param bones A collection of bones that armature has for animating with.
     * @param meshes A collection of meshes that armature should set up weights for.
     */
    public Armature(Map<String, Bone> bones, List<Weight> weights)
    {
        this.bones = bones;
        this.weights = weights;
        bones.values().forEach(Bone::init);
    }

    /**
     * Gets the list of weights that influence animations.
     * @return The weights for meshes using this armature.
     */
    public List<Weight> weights()
    {
        return this.weights;
    }
}

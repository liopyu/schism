package com.schism.core.client.models;

import com.schism.core.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class Bone
{
    protected final String name;
    protected final Supplier<Bone> parentSupplier;
    protected final float x;
    protected final float y;
    protected final float z;
    protected final List<Bone> children;
    protected Bone parent;

    /**
     * An armature bone used for animation.
     * @param name The name of the bone.
     * @param parentSupplier A supplier to get a parent bone, can return null for no parent.
     * @param x The x position of the bone.
     * @param y The y position of the bone.
     * @param z The z position of the bone.
     */
    public Bone(String name, Supplier<Bone> parentSupplier, float x, float y, float z)
    {
        this.name = name;
        this.parentSupplier = parentSupplier;
        this.x = x;
        this.y = y;
        this.z = z;
        this.children = new ArrayList<>();
    }

    /**
     * Initialises this bone for use, should be called after loading, called by Armatures.
     */
    public void init()
    {
        this.parent = this.parentSupplier.get();
        if (this.parent != null) {
            this.parent.addChild(this);
        }
    }

    /**
     * Gets the name of this bone.
     * @return The bone name.
     */
    public String name()
    {
        return this.name;
    }

    /**
     * Gets an optional parent of this bone.
     * @return The optional parent bone.
     */
    public Optional<Bone> parent()
    {
        if (this.parent == null) {
            return Optional.empty();
        }
        return Optional.of(this.parent);
    }

    /**
     * Gets a list of child bones that have this bone as their parent.
     * @return The list of child bones.
     */
    public List<Bone> children()
    {
        return this.children;
    }

    /**
     * Adds the provided bone as a child to this bone.
     * @param bone The bone to add as a child.
     */
    public void addChild(Bone bone)
    {
        if (bone == this) {
            Log.error("model", "A bone tried to add itself as it's own child.");
            return;
        }
        this.children.add(bone);
    }
}

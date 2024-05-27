package com.schism.core.client.gui;

import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;

public class Placement
{
    protected Vec3 position;
    protected Vec2 size;

    /**
     * A mutable placement for use with gui widgets.
     */
    public Placement()
    {
        this.position = Vec3.ZERO;
        this.size = Vec2.ZERO;
    }

    /**
     * A mutable placement for use with gui widgets.
     * @param position The initial position of this placement.
     */
    public Placement(Vec3 position)
    {
        this.position = position;
        this.size = Vec2.ZERO;
    }

    /**
     * A mutable placement for use with gui widgets.
     * @param size The initial size of this placement.
     */
    public Placement(Vec2 size)
    {
        this.position = Vec3.ZERO;
        this.size = size;
    }

    /**
     * A mutable placement for use with gui widgets.
     * @param position The initial position of this placement.
     * @param size The initial size of this placement.
     */
    public Placement(Vec3 position, Vec2 size)
    {
        this.position = position;
        this.size = size;
    }

    /**
     * The position of this placement.
     * @return The position to use.
     */
    public Vec3 position()
    {
        return this.position;
    }

    /**
     * Sets the xyz position of this placement.
     * @return The current instance.
     */
    public Placement setPosition(Vec3 position)
    {
        this.position = position;
        return this;
    }

    /**
     * Sets the xy position of this placement, doesn't affect the z position.
     * @return The current instance.
     */
    public Placement setPosition(Vec2 position)
    {
        this.position = new Vec3(position.x(), position().y(), this.position().z());
        return this;
    }

    /**
     * The size of this placement.
     * @return The size to use.
     */
    public Vec2 size()
    {
        return this.size;
    }

    /**
     * Sets the size of this placement.
     * @return The current instance.
     */
    public Placement setSize(Vec2 size)
    {
        this.size = size;
        return this;
    }
}

package com.schism.core.util;

public class Vec2
{
    public static final Vec2 ZERO = new Vec2(0, 0);
    public static final Vec2 ONE = new Vec2(1, 1);
    public static final Vec2 NEG_ONE = new Vec2(-1, -1);

    protected final float x;
    protected final float y;

    /**
     * Constructor
     * @param x The x value of this vector.
     * @param y The y value of this vector.
     */
    public Vec2(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructor
     * @param x The x value of this vector.
     * @param y The y value of this vector.
     */
    public Vec2(double x, double y)
    {
        this.x = (float)x;
        this.y = (float)y;
    }

    /**
     * Constructor
     * @param physVec2 The world physics vec2 to create this vector from.
     */
    public Vec2(net.minecraft.world.phys.Vec2 physVec2)
    {
        this.x = physVec2.x;
        this.y = physVec2.y;
    }

    /**
     * Gets the x value of this vector.
     * @return The x value of this vector.
     */
    public float x()
    {
        return this.x;
    }

    /**
     * Gets the y value of this vector.
     * @return The y value of this vector.
     */
    public float y()
    {
        return this.y;
    }

    /**
     * Creates a new physics vector 2 from this vector.
     * @return A new physics vector 2 with the values from this vector.
     */
    public net.minecraft.world.phys.Vec2 physVec2()
    {
        return new net.minecraft.world.phys.Vec2(this.x(), this.y());
    }

    /**
     * Returns a new vector with values equal to this vector plus the provided values.
     * @param x The x value to add to a new vector.
     * @param y The y value to add to a new vector.
     * @return A new vector that is the addition of this vector and the provided values.
     */
    public Vec2 add(float x, float y)
    {
        return new Vec2(this.x() + x, this.y() + y);
    }

    /**
     * Returns a new vector with all values equal to this vector plus the provided value.
     * @param all The value to add to all values of a new vector by.
     * @return A new vector that is the addition of this vector and the provided value.
     */
    public Vec2 add(float all)
    {
        return new Vec2(this.x() + all, this.y() + all);
    }

    /**
     * Returns a new vector with values equal to this vector plus the provided vector.
     * @param vector The vector to add to a new vector.
     * @return A new vector that is the addition of this vector and the provided vector.
     */
    public Vec2 add(Vec2 vector)
    {
        return this.add(vector.x(), vector.y());
    }

    /**
     * Returns a new vector with values equal to this vector minus the provided vector.
     * @param vector The vector to subtract from a new vector.
     * @return A new vector that is the subtraction of this vector and the provided vector.
     */
    public Vec2 subtract(Vec2 vector)
    {
        return this.add(-vector.x(), -vector.y());
    }

    /**
     * Returns a new vector with values equal to this vector multiplied by the provided values.
     * @param x The x value to multiply a new vector by.
     * @param y The y value to multiply a new vector by.
     * @return A new vector that is this vector multiplied by the provided values.
     */
    public Vec2 multiply(float x, float y)
    {
        return new Vec2(this.x() * x, this.y() * y);
    }

    /**
     * Returns a new vector with values equal to this vector all multiplied by the provided value.
     * @param all The value to multiply all values of a new vector by.
     * @return A new vector that is all values of this vector multiplied by the provided value.
     */
    public Vec2 multiply(float all)
    {
        return this.multiply(all, all);
    }

    /**
     * Returns a new vector with values equal to this vector multiplied by the provided vector.
     * @param vector The vector to multiply by.
     * @return A new vector that is the multiplication of this vector and the provided vector.
     */
    public Vec2 multiply(Vec2 vector)
    {
        return this.multiply(vector.x(), vector.y());
    }

    /**
     * Returns a new vector that has the highest values from this vector and the provided values.
     * @param x The x value to compare with.
     * @param y The y value to compare with.
     * @return A new vector that has the higher values of this vector and the provided values.
     */
    public Vec2 max(float x, float y)
    {
        return new Vec2(Math.max(this.x(), x), Math.max(this.y(), y));
    }

    /**
     * Returns a new vector that has the highest values from this vector and the provided value.
     * @param all The value to compare with.
     * @return A new vector that has the higher values of this vector and the provided value.
     */
    public Vec2 max(float all)
    {
        return this.max(all, all);
    }

    /**
     * Returns a new vector that has the highest values from this vector and the provided vector.
     * @param vector The vector to compare values with.
     * @return A new vector that has the higher values of this vector and the provided vector.
     */
    public Vec2 max(Vec2 vector)
    {
        return this.max(vector.x(), vector.y());
    }

    /**
     * Returns a new vector that has the lowest values from this vector and the provided values.
     * @param x The x value to compare with.
     * @param y The y value to compare with.
     * @return A new vector that has the lower values of this vector and the provided values.
     */
    public Vec2 min(float x, float y)
    {
        return new Vec2(Math.min(this.x(), x), Math.min(this.y(), y));
    }

    /**
     * Returns a new vector that has the lowest values from this vector and the provided value.
     * @param all The value to compare with.
     * @return A new vector that has the lower values of this vector and the provided value.
     */
    public Vec2 min(float all)
    {
        return this.min(all, all);
    }

    /**
     * Returns a new vector that has the lowest values from this vector and the provided vector.
     * @param vector The vector to compare values with.
     * @return A new vector that has the lower values of this vector and the provided vector.
     */
    public Vec2 min(Vec2 vector)
    {
        return this.min(vector.x(), vector.y());
    }

    /**
     * Returns the distance from this vector to the provided target vector squared.
     * This avoids square root making it more performant for comparing distances but doesn't provide an actual distance.
     * @param vector The target vector to get the distance to.
     * @return The distance from this vector to the provided target vector.
     */
    public float distanceSq(Vec2 vector)
    {
        float distX = vector.x() - this.x();
        float distY = vector.y() - this.y();
        return (distX * distX ) + (distY * distY);
    }

    /**
     * Returns the distance from this vector to the provided target vector.
     * @param vector The target vector to get the distance to.
     * @return The distance from this vector to the provided target vector.
     */
    public float distance(Vec2 vector)
    {
        return (float)Math.sqrt(this.distanceSq(vector));
    }

    /**
     * Returns the normalization of this vector where are values are reduced down to their relative ranges of -1.0 to 1.0.
     * @return The normalization of this vector.
     */
    public Vec2 normalize()
    {
        double length = Math.sqrt((this.x() * this.x()) + (this.y() * this.y()));
        return length < 1.0E-4D ? ZERO : new Vec2(this.x() / length, this.y() / length);
    }

    @Override
    public String toString()
    {
        return "Vector2(" + this.x() + ", " + this.y() + ")";
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Vec2 vec2) {
            if (Float.compare(this.x(), vec2.x()) != 0) {
                return false;
            }
            return Float.compare(this.y(), vec2.y()) == 0;
        }
        return super.equals(object);
    }

    @Override
    public int hashCode()
    {
        int hash = Float.floatToIntBits(this.x());
        return 31 * hash + Float.floatToIntBits(this.y());
    }
}

package com.schism.core.util;

import org.joml.Vector4f;

import java.awt.Color;

public class Vec4 extends Vec3
{
    public static final Vec4 ZERO = new Vec4(0, 0, 0, 0);
    public static final Vec4 ONE = new Vec4(1, 1, 1, 1);
    public static final Vec4 NEG_ONE = new Vec4(-1, -1, -1, -1);
    public static final Vec4 BLACK = new Vec4(1, 1, 1, 0);

    protected final float w;

    /**
     * Constructor
     * @param x The x value of this vector.
     * @param y The y value of this vector.
     * @param z The z value of this vector.
     * @param w The w value of this vector.
     */
    public Vec4(float x, float y, float z, float w)
    {
        super(x, y, z);
        this.w = w;
    }

    /**
     * Constructor
     * @param x The x value of this vector.
     * @param y The y value of this vector.
     * @param z The z value of this vector.
     * @param w The w value of this vector.
     */
    public Vec4(double x, double y, double z, double w)
    {
        super(x, y, z);
        this.w = (float)w;
    }

    /**
     * Constructor
     * @param vector4f The float vector 4 to create this vector from.
     */
    public Vec4(Vector4f vector4f)
    {
        super(vector4f.x(), vector4f.y(), vector4f.z());
        this.w = vector4f.w();
    }

    /**
     * Gets the w value of this vector.
     * @return The w value of this vector.
     */
    public float w()
    {
        return this.w;
    }

    /**
     * Creates a new float vector 4 from this vector.
     * @return A new float vector 4 with the values from this vector.
     */
    public Vector4f vector4f()
    {
        return new Vector4f(this.x(), this.y(), this.z(), this.w());
    }

    /**
     * Creates a new color from this vector as rgba values ranging from 0.0 to 1.0.
     * @return A new block position with the values from this vector.
     */
    public Color color()
    {
        return new Color(this.x(), this.y(), this.z(), this.w());
    }

    /**
     * Creates a vector 3 from the xyz values of this vector.
     * @return A new vector 3 with this vector's xyz values.
     */
    public Vec3 xyz()
    {
        return new Vec3(this.x(), this.y(), this.z());
    }

    /**
     * Returns a new vector with values equal to this vector plus the provided values.
     * @param x The x value to add to a new vector.
     * @param y The y value to add to a new vector.
     * @param z The z value to add to a new vector.
     * @param w The w value to add to a new vector.
     * @return A new vector that is the addition of this vector and the provided values.
     */
    public Vec4 add(float x, float y, float z, float w)
    {
        return new Vec4(this.x() + x, this.y() + y, this.z() + z, this.w() + w);
    }

    /**
     * Returns a new vector with all values equal to this vector plus the provided value.
     * @param all The value to add to all values of a new vector by.
     * @return A new vector that is the addition of this vector and the provided value.
     */
    @Override
    public Vec4 add(float all)
    {
        return new Vec4(this.x() + all, this.y() + all, this.z() + all, this.w() + all);
    }

    /**
     * Returns a new vector with values equal to this vector plus the provided vector.
     * @param vector The vector to add to a new vector.
     * @return A new vector that is the addition of this vector and the provided vector.
     */
    public Vec4 add(Vec4 vector)
    {
        return this.add(vector.x(), vector.y(), vector.z(), vector.w());
    }

    /**
     * Returns a new vector with values equal to this vector minus the provided vector.
     * @param vector The vector to subtract from a new vector.
     * @return A new vector that is the subtraction of this vector and the provided vector.
     */
    public Vec4 subtract(Vec4 vector)
    {
        return this.add(-vector.x(), -vector.y(), -vector.z(), -vector.w());
    }

    /**
     * Returns a new vector with values equal to this vector multiplied by the provided values.
     * @param x The x value to multiply a new vector by.
     * @param y The y value to multiply a new vector by.
     * @param z The z value to multiply a new vector by.
     * @param w The w value to multiply a new vector by.
     * @return A new vector that is this vector multiplied by the provided values.
     */
    public Vec4 multiply(float x, float y, float z, float w)
    {
        return new Vec4(this.x() * x, this.y() * y, this.z() * z, this.w() * w);
    }

    /**
     * Returns a new vector with values equal to this vector all multiplied by the provided value.
     * @param all The value to multiply all values of a new vector by.
     * @return A new vector that is all values of this vector multiplied by the provided value.
     */
    @Override
    public Vec4 multiply(float all)
    {
        return this.multiply(all, all, all, all);
    }

    /**
     * Returns a new vector with values equal to this vector multiplied by the provided vector.
     * @param vector The vector to multiply by.
     * @return A new vector that is the multiplication of this vector and the provided vector.
     */
    public Vec4 multiply(Vec4 vector)
    {
        return this.multiply(vector.x(), vector.y(), vector.z(), vector.w());
    }

    /**
     * Returns a new vector that has the highest values from this vector and the provided values.
     * @param x The x value to compare with.
     * @param y The y value to compare with.
     * @param z The z value to compare with.
     * @param w The w value to compare with.
     * @return A new vector that has the higher values of this vector and the provided values.
     */
    public Vec4 max(float x, float y, float z, float w)
    {
        return new Vec4(Math.max(this.x(), x), Math.max(this.y(), y), Math.max(this.z(), z), Math.max(this.w(), w));
    }

    /**
     * Returns a new vector that has the highest values from this vector and the provided value.
     * @param all The value to compare with.
     * @return A new vector that has the higher values of this vector and the provided value.
     */
    @Override
    public Vec4 max(float all)
    {
        return this.max(all, all, all, all);
    }

    /**
     * Returns a new vector that has the highest values from this vector and the provided vector.
     * @param vector The vector to compare values with.
     * @return A new vector that has the higher values of this vector and the provided vector.
     */
    public Vec4 max(Vec4 vector)
    {
        return this.max(vector.x(), vector.y(), vector.z(), vector.w());
    }

    /**
     * Returns a new vector that has the lowest values from this vector and the provided values.
     * @param x The x value to compare with.
     * @param y The y value to compare with.
     * @param z The z value to compare with.
     * @param w The w value to compare with.
     * @return A new vector that has the lower values of this vector and the provided values.
     */
    public Vec4 min(float x, float y, float z, float w)
    {
        return new Vec4(Math.min(this.x(), x), Math.min(this.y(), y), Math.min(this.z(), z), Math.min(this.w(), w));
    }

    /**
     * Returns a new vector that has the lowest values from this vector and the provided value.
     * @param all The value to compare with.
     * @return A new vector that has the lower values of this vector and the provided value.
     */
    @Override
    public Vec4 min(float all)
    {
        return this.min(all, all, all, all);
    }

    /**
     * Returns a new vector that has the lowest values from this vector and the provided vector.
     * @param vector The vector to compare values with.
     * @return A new vector that has the lower values of this vector and the provided vector.
     */
    public Vec4 min(Vec4 vector)
    {
        return this.min(vector.x(), vector.y(), vector.z(), vector.w());
    }

    /**
     * Returns the distance from this vector to the provided target vector squared.
     * This avoids square root making it more performant for comparing distances but doesn't provide an actual distance.
     * @param vector The target vector to get the distance to.
     * @return The distance from this vector to the provided target vector.
     */
    public float distanceSq(Vec4 vector)
    {
        float distX = vector.x() - this.x();
        float distY = vector.y() - this.y();
        float distZ = vector.z() - this.z();
        float distW = vector.w() - this.w();
        return (distX * distX ) + (distY * distY) + (distZ * distZ) + (distW * distW);
    }

    /**
     * Returns the distance from this vector to the provided target vector.
     * @param vector The target vector to get the distance to.
     * @return The distance from this vector to the provided target vector.
     */
    public float distance(Vec4 vector)
    {
        return (float)Math.sqrt(this.distanceSq(vector));
    }

    /**
     * Returns the normalization of this vector where are values are reduced down to their relative ranges of -1.0 to 1.0.
     * @return The normalization of this vector.
     */
    @Override
    public Vec4 normalize()
    {
        double length = Math.sqrt((this.x() * this.x()) + (this.y() * this.y()) + (this.z() * this.z()) + (this.w() * this.w()));
        return length < 1.0E-4D ? ZERO : new Vec4(this.x() / length, this.y() / length, this.z() / length, this.w() / length);
    }

    @Override
    public String toString()
    {
        return "Vector4(" + this.x() + ", " + this.y() + ", " + this.z() + ", " + this.w() + ")";
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Vec4 vec4) {
            if (Float.compare(this.x(), vec4.x()) != 0) {
                return false;
            }
            if (Float.compare(this.y(), vec4.y()) != 0) {
                return false;
            }
            if (Float.compare(this.z(), vec4.z()) != 0) {
                return false;
            }
            return Float.compare(this.w(), vec4.w()) == 0;
        }
        return super.equals(object);
    }

    @Override
    public int hashCode()
    {
        int hash = Float.floatToIntBits(this.x());
        hash = 31 * hash + Float.floatToIntBits(this.y());
        hash = 31 * hash + Float.floatToIntBits(this.z());
        return 31 * hash + Float.floatToIntBits(this.w());
    }
}

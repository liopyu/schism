package com.schism.core.util;

import org.joml.Quaternionf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import java.awt.*;

public class Vec3 extends Vec2
{
    public static final Vec3 ZERO = new Vec3(0, 0, 0);
    public static final Vec3 ONE = new Vec3(1, 1, 1);
    public static final Vec3 NEG_ONE = new Vec3(-1, -1, -1);

    protected final float z;

    /**
     * Constructor
     * @param x The x value of this vector.
     * @param y The y value of this vector.
     * @param z The z value of this vector.
     */
    public Vec3(float x, float y, float z)
    {
        super(x, y);
        this.z = z;
    }

    /**
     * Constructor
     * @param x The x value of this vector.
     * @param y The y value of this vector.
     * @param z The z value of this vector.
     */
    public Vec3(double x, double y, double z)
    {
        super((float)x, (float)y);
        this.z = (float)z;
    }

    /**
     * Constructor
     * @param physVec3 The world physics vector 2 to create this vector from.
     */
    public Vec3(net.minecraft.world.phys.Vec3 physVec3)
    {
        super(physVec3.x(), physVec3.y());
        this.z = (float)physVec3.z();
    }

    /**
     * Constructor
     * @param vec3i The integer vector 3 (or block position) to create this vector from.
     */
    public Vec3(Vec3i vec3i)
    {
        super(vec3i.getX(), vec3i.getY());
        this.z = vec3i.getZ();
    }

    /**
     * Constructor
     * @param direction The direction to create a vector for.
     */
    public Vec3(Direction direction)
    {
        this(direction.getNormal());
    }

    /**
     * Gets the z value of this vector.
     * @return The z value of this vector.
     */
    public float z()
    {
        return this.z;
    }

    /**
     * Creates a new physics vector 3 from this vector.
     * @return A new physics vector 3 with the values from this vector.
     */
    public net.minecraft.world.phys.Vec3 physVec3()
    {
        return new net.minecraft.world.phys.Vec3(this.x(), this.y(), this.z());
    }

    /**
     * Creates a new block position from this vector. Values are always floored.
     * @return A new block position with the values from this vector.
     */
    public BlockPos blockPos()
    {
        return new BlockPos((int)this.x(), (int)this.y(), (int)this.z());
    }

    /**
     * Creates a new color from this vector as rgb values ranging from 0.0 to 1.0.
     * @return A new block position with the values from this vector.
     */
    public Color color()
    {
        return new Color(this.x(), this.y(), this.z());
    }

    /**
     * Creates a vector 2 from the xy values of this vector.
     * @return A new vector 2 with this vector's xy values.
     */
    public Vec2 xy()
    {
        return new Vec2(this.x(), this.y());
    }

    /**
     * Creates a vector 2 from the xz values of this vector.
     * @return A new vector 2 with this vector's xz values.
     */
    public Vec2 xz()
    {
        return new Vec2(this.x(), this.z());
    }

    /**
     * Creates a vector 2 from the yz values of this vector.
     * @return A new vector 2 with this vector's yz values.
     */
    public Vec2 yz()
    {
        return new Vec2(this.y(), this.z());
    }

    /**
     * Returns a new vector with values equal to this vector plus the provided values.
     * @param x The x value to add to a new vector.
     * @param y The y value to add to a new vector.
     * @param z The z value to add to a new vector.
     * @return A new vector that is the addition of this vector and the provided values.
     */
    public Vec3 add(float x, float y, float z)
    {
        return new Vec3(this.x() + x, this.y() + y, this.z() + z);
    }

    /**
     * Returns a new vector with all values equal to this vector plus the provided value.
     * @param all The value to add to all values of a new vector by.
     * @return A new vector that is the addition of this vector and the provided value.
     */
    @Override
    public Vec3 add(float all)
    {
        return new Vec3(this.x() + all, this.y() + all, this.z() + all);
    }

    /**
     * Returns a new vector with values equal to this vector plus the provided vector.
     * @param vector The vector to add to a new vector.
     * @return A new vector that is the addition of this vector and the provided vector.
     */
    public Vec3 add(Vec3 vector)
    {
        return this.add(vector.x(), vector.y(), vector.z());
    }

    /**
     * Returns a new vector with values equal to this vector minus the provided vector.
     * @param vector The vector to subtract from a new vector.
     * @return A new vector that is the subtraction of this vector and the provided vector.
     */
    public Vec3 subtract(Vec3 vector)
    {
        return this.add(-vector.x(), -vector.y(), -vector.z());
    }

    /**
     * Returns a new vector with values equal to this vector multiplied by the provided values.
     * @param x The x value to multiply a new vector by.
     * @param y The y value to multiply a new vector by.
     * @param z The z value to multiply a new vector by.
     * @return A new vector that is this vector multiplied by the provided values.
     */
    public Vec3 multiply(float x, float y, float z)
    {
        return new Vec3(this.x() * x, this.y() * y, this.z() * z);
    }

    /**
     * Returns a new vector with values equal to this vector all multiplied by the provided value.
     * @param all The value to multiply all values of a new vector by.
     * @return A new vector that is all values of this vector multiplied by the provided value.
     */
    @Override
    public Vec3 multiply(float all)
    {
        return this.multiply(all, all, all);
    }

    /**
     * Returns a new vector with values equal to this vector multiplied by the provided vector.
     * @param vector The vector to multiply by.
     * @return A new vector that is the multiplication of this vector and the provided vector.
     */
    public Vec3 multiply(Vec3 vector)
    {
        return this.multiply(vector.x(), vector.y(), vector.z());
    }

    /**
     * Returns a new vector that has the highest values from this vector and the provided values.
     * @param x The x value to compare with.
     * @param y The y value to compare with.
     * @param z The z value to compare with.
     * @return A new vector that has the higher values of this vector and the provided values.
     */
    public Vec3 max(float x, float y, float z)
    {
        return new Vec3(Math.max(this.x(), x), Math.max(this.y(), y), Math.max(this.z(), z));
    }

    /**
     * Returns a new vector that has the highest values from this vector and the provided value.
     * @param all The value to compare with.
     * @return A new vector that has the higher values of this vector and the provided value.
     */
    @Override
    public Vec3 max(float all)
    {
        return this.max(all, all, all);
    }

    /**
     * Returns a new vector that has the highest values from this vector and the provided vector.
     * @param vector The vector to compare values with.
     * @return A new vector that has the higher values of this vector and the provided vector.
     */
    public Vec3 max(Vec3 vector)
    {
        return this.max(vector.x(), vector.y(), vector.z());
    }

    /**
     * Returns a new vector that has the lowest values from this vector and the provided values.
     * @param x The x value to compare with.
     * @param y The y value to compare with.
     * @param z The z value to compare with.
     * @return A new vector that has the lower values of this vector and the provided values.
     */
    public Vec3 min(float x, float y, float z)
    {
        return new Vec3(Math.min(this.x(), x), Math.min(this.y(), y), Math.min(this.z(), z));
    }

    /**
     * Returns a new vector that has the lowest values from this vector and the provided value.
     * @param all The value to compare with.
     * @return A new vector that has the lower values of this vector and the provided value.
     */
    @Override
    public Vec3 min(float all)
    {
        return this.min(all, all, all);
    }

    /**
     * Returns a new vector that has the lowest values from this vector and the provided vector.
     * @param vector The vector to compare values with.
     * @return A new vector that has the lower values of this vector and the provided vector.
     */
    public Vec3 min(Vec3 vector)
    {
        return this.min(vector.x(), vector.y(), vector.z());
    }

    /**
     * Returns the distance from this vector to the provided target vector squared.
     * This avoids square root making it more performant for comparing distances but doesn't provide an actual distance.
     * @param vector The target vector to get the distance to.
     * @return The distance from this vector to the provided target vector.
     */
    public float distanceSq(Vec3 vector)
    {
        float distX = vector.x() - this.x();
        float distY = vector.y() - this.y();
        float distZ = vector.z() - this.z();
        return (distX * distX ) + (distY * distY) + (distZ * distZ);
    }

    /**
     * Returns the distance from this vector to the provided target vector.
     * @param vector The target vector to get the distance to.
     * @return The distance from this vector to the provided target vector.
     */
    public float distance(Vec3 vector)
    {
        return (float)Math.sqrt(this.distanceSq(vector));
    }

    /**
     * Returns the normalization of this vector where are values are reduced down to their relative ranges of -1.0 to 1.0.
     * @return The normalization of this vector.
     */
    @Override
    public Vec3 normalize()
    {
        double length = Math.sqrt((this.x() * this.x()) + (this.y() * this.y()) + (this.z() * this.z()));
        return length < 1.0E-4D ? ZERO : new Vec3(this.x() / length, this.y() / length, this.z() / length);
    }

    /**
     * Returns a new quaternion of the rotation of this vector around the provided amount in radians.
     * @param radians The rotation amount in radians.
     * @return A new quaternion of the rotation of this vector around the provided amount.
     */
    public Quaternionf rotation(float radians)
    {
        float sin = (float)Math.sin(radians / 2.0F);
        float cos = (float)Math.cos(radians / 2.0F);
        return new Quaternionf(this.x() * sin, this.y() * sin, this.z() * sin, cos);
    }

    /**
     * Returns a new quaternion of the rotation of this vector around the provided amount in degrees.
     * @param degrees The rotation amount in degrees.
     * @return A new quaternion of the rotation of this vector around the provided amount.
     */
    public Quaternionf rotationDegrees(float degrees)
    {
        return this.rotation(degrees * ((float)Maths.TAU / 360F));
    }

    @Override
    public String toString()
    {
        return "Vector3(" + this.x() + ", " + this.y() + ", " + this.z() + ")";
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Vec3 vec3) {
            if (Float.compare(this.x(), vec3.x()) != 0) {
                return false;
            }
            if (Float.compare(this.y(), vec3.y()) != 0) {
                return false;
            }
            return Float.compare(this.z(), vec3.z()) == 0;
        }
        return super.equals(object);
    }

    @Override
    public int hashCode()
    {
        int hash = Float.floatToIntBits(this.x());
        hash = 31 * hash + Float.floatToIntBits(this.y());
        return 31 * hash + Float.floatToIntBits(this.z());
    }
}

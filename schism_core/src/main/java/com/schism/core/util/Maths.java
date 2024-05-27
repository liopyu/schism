package com.schism.core.util;

public class Maths
{
    public static final double TAU = 6.283185307179585D;

    /**
     * Used for iterating a range of numbers from a center number alternating outwards by minus and plus. Use with oddCenterRange.
     * Note: This will return 0 twice when starting from 0 so start from 1 with a range value doubled minus 1.
     * @param input The number to 'center split'.
     * @return The input halved and negative if even, positive and offset by -1 if odd.
     */
    public static int oddCenter(int input)
    {
        return (input % 2) == 0 ? (-input / 2) : ((input - 1) / 2);
    }

    /**
     * Returns a range for iterating over using oddCenter, use with oddCenter to loop from the middle of a number range outwards starting from 1.
     * @param input The range to modify.
     * @return The modified range (doubled minus 1).
     */
    public static int oddCenterRange(int input)
    {
        return (input * 2) - 1;
    }
}

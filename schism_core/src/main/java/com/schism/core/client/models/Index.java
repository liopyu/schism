package com.schism.core.client.models;

public record Index(int vertex, int uv, int normal)
{
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Index index) {
            return index.vertex == vertex && index.uv == uv && index.normal == normal;
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        final int base = 17;
        final int multiplier = 31;

        int result = base;
        result = multiplier * result + vertex;
        result = multiplier * result + uv;
        result = multiplier * result + normal;
        return result;
    }
}

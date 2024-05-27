package com.schism.core.gods;

import com.schism.core.database.IHasDefinition;

public class God implements IHasDefinition<GodDefinition>
{
    protected final GodDefinition definition;

    public God(GodDefinition definition)
    {
        this.definition = definition;
    }

    @Override
    public GodDefinition definition()
    {
        return this.definition;
    }
}

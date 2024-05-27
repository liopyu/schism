package com.schism.core.elements;

import net.minecraft.world.damagesource.DamageSource;

import java.util.List;

public class ElementsDamageSource extends DamageSource implements IHasElements
{
    protected final List<ElementDefinition> elements;

    public ElementsDamageSource(List<ElementDefinition> elements)
    {
        super("elements");
        this.elements = elements;
    }

    @Override
    public List<ElementDefinition> elements()
    {
        return this.elements;
    }
}

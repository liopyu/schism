package com.schism.core.elements;

import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class ElementsEntityDamageSource extends IndirectEntityDamageSource implements IHasElements
{
    protected final List<ElementDefinition> elements;

    public ElementsEntityDamageSource(List<ElementDefinition> elements, Entity directEntity, Entity ownerEntity)
    {
        super("entity_elements", directEntity, ownerEntity);
        this.elements = elements;
    }

    @Override
    public List<ElementDefinition> elements()
    {
        return this.elements;
    }
}

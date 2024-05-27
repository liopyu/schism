package com.schism.core.gods.perks;

import com.schism.core.database.CachedDefinition;
import com.schism.core.database.DataStore;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.elements.ElementRepository;
import net.minecraft.world.damagesource.DamageSource;

import java.util.List;
import java.util.Optional;

public class ElementWorshipPerk extends AbstractWorshipPerk
{
    protected final CachedDefinition<ElementDefinition> cachedElement;
    protected final boolean damage;
    protected final boolean resistance;
    protected final boolean harmony;

    public ElementWorshipPerk(DataStore dataStore)
    {
        super(dataStore);

        this.cachedElement = new CachedDefinition<>(dataStore.stringProp("element"), ElementRepository.get());
        this.damage = dataStore.booleanProp("damage");
        this.resistance = dataStore.booleanProp("resistance");
        this.harmony = dataStore.booleanProp("harmony");
    }

    @Override
    public Optional<ElementDefinition> resistElement()
    {
        if (this.resistance) {
            return Optional.ofNullable(this.cachedElement.get());
        }
        return Optional.empty();
    }

    @Override
    public Optional<ElementDefinition> attackElement()
    {
        if (this.damage) {
            return Optional.ofNullable(this.cachedElement.get());
        }
        return Optional.empty();
    }

    @Override
    public Optional<ElementDefinition> harmonyElement()
    {
        if (this.harmony) {
            return Optional.ofNullable(this.cachedElement.get());
        }
        return Optional.empty();
    }
}

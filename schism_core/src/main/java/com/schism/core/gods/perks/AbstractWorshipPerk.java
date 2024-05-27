package com.schism.core.gods.perks;

import com.schism.core.database.DataStore;
import com.schism.core.elements.ElementDefinition;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractWorshipPerk
{
    protected final String type;

    public AbstractWorshipPerk(DataStore dataStore)
    {
        this.type = dataStore.stringProp("type");
    }

    public static List<AbstractWorshipPerk> listFromMap(Map<String, DataStore> dataStoreMap)
    {
        return dataStoreMap.entrySet().stream().map(AbstractWorshipPerk::create).filter(Objects::nonNull).toList();
    }

    public static AbstractWorshipPerk create(Map.Entry<String, DataStore> dataStoreEntry)
    {
        DataStore dataStore = dataStoreEntry.getValue();
        return switch (dataStore.stringProp("type")) {
            case "element" -> new ElementWorshipPerk(dataStore);
            case "effect_protection" -> new EffectProtectionWorshipPerk(dataStore);
            default -> throw new RuntimeException("Invalid Worship Perk Type: " + dataStore.stringProp("type"));
        };
    }

    /**
     * Called when an entity with this perk has an update tick.
     * @param livingEntity The entity with the perk.
     */
    public void tick(LivingEntity livingEntity)
    {

    }

    /**
     * Called when an entity with this perk takes damage.
     * @param amount The amount of damage taken.
     * @param damageSource The damage source.
     * @param damageElements A list of elements applicable to the damage.
     * @return A potentially modified damage amount.
     */
    public float hurt(float amount, DamageSource damageSource, List<ElementDefinition> damageElements)
    {
        return amount;
    }

    /**
     * Called when an entity with this perk is being test for effect vulnerability.
     * @param effectInstance The effect that is trying to be applied.
     * @return True to allow the effect, false to prevent it.
     */
    public boolean effectApplicable(MobEffectInstance effectInstance)
    {
        return true;
    }

    /**
     * Gets an element that this perk should add a resistance to.
     * @return An optional resist element.
     */
    public Optional<ElementDefinition> resistElement()
    {
        return Optional.empty();
    }

    /**
     * Gets a damage element that this perk should add to the entity damage.
     * @return An optional element to add to entity damage.
     */
    public Optional<ElementDefinition> attackElement()
    {
        return Optional.empty();
    }

    /**
     * Gets an element that this perk should add harmony to.
     * @return An optional harmony element.
     */
    public Optional<ElementDefinition> harmonyElement()
    {
        return Optional.empty();
    }
}

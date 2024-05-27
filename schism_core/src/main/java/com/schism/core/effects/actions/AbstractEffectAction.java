package com.schism.core.effects.actions;

import com.schism.core.database.Action;
import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

abstract public class AbstractEffectAction extends Action<EffectDefinition>
{
    public AbstractEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);
    }

    /**
     * Creates a list of effect actions from the provided map.
     * @param definition The definition the list is for.
     * @param dataStoreMap A map of keys and nested data stores for populating actions with.
     * @return The newly created effect action list.
     */
    public static List<AbstractEffectAction> listFromMap(EffectDefinition definition, Map<String, DataStore> dataStoreMap)
    {
        return dataStoreMap.entrySet().stream()
                .map(dataStoreEntry -> AbstractEffectAction.create(definition, dataStoreEntry))
                .filter(Objects::nonNull).toList();
    }

    /**
     * Creates a new effect action instance for the provided data store entry.
     * @param definition The definition the action is for.
     * @param dataStoreEntry The data store entry to get action properties from.
     * @return The newly created effect action.
     */
    public static AbstractEffectAction create(EffectDefinition definition, Map.Entry<String, DataStore> dataStoreEntry)
    {
        DataStore dataStore = dataStoreEntry.getValue();
        return switch (dataStore.stringProp("type")) {
            case "bed" -> new BedEffectAction(definition, dataStore);
            case "damage" -> new DamageEffectAction(definition, dataStore);
            case "damage_dealt" -> new DamageDealtEffectAction(definition, dataStore);
            case "damage_recoil" -> new DamageRecoilEffectAction(definition, dataStore);
            case "damage_taken" -> new DamageTakenEffectAction(definition, dataStore);
            case "effect_protection" -> new EffectProtectionEffectAction(definition, dataStore);
            case "environment" -> new EnvironmentEffectAction(definition, dataStore);
            case "fear" -> new FearEffectAction(definition, dataStore);
            case "healing_taken" -> new HealingTakenEffectAction(definition, dataStore);
            case "items" -> new ItemsEffectAction(definition, dataStore);
            case "jump" -> new JumpEffectAction(definition, dataStore);
            case "kill" -> new KillEffectAction(definition, dataStore);
            case "knockback" -> new KnockbackEffectAction(definition, dataStore);
            case "speed" -> new SpeedEffectAction(definition, dataStore);
            case "spread" -> new SpreadEffectAction(definition, dataStore);
            case "velocity" -> new VelocityEffectAction(definition, dataStore);
            default -> throw new RuntimeException("Invalid Effect Action Type: " + dataStore.stringProp("type"));
        };
    }

    /**
     * Called on the update event of affected entities.
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     */
    public void update(MobEffectInstance effectInstance, LivingEntity livingEntity)
    {

    }

    /**
     * Called on the jump event of affected entities.
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     * @return Returns true to allow the jump or false to try and cancel the jump.
     */
    public boolean jump(MobEffectInstance effectInstance, LivingEntity livingEntity)
    {
        return true;
    }

    /**
     * Called on the affected attacking entities of hurt event damage sources (when entities deal damage to other entities via the target's hurt event).
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     * @param damageSource The damage source instance.
     * @param amount The amount of damage to be dealt.
     * @return Returns a potentially modified amount of damage.
     */
    public float damageDealt(MobEffectInstance effectInstance, LivingEntity livingEntity, LivingEntity targetEntity, DamageSource damageSource, float amount)
    {
        return amount;
    }

    /**
     * Called on the hurt event of affected entities.
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     * @param damageSource The damage source instance. This can be used to get an attacking entity if applicable.
     * @param amount The amount of damage received.
     * @return Returns a potentially modified amount of damage.
     */
    public float damageTaken(MobEffectInstance effectInstance, LivingEntity livingEntity, DamageSource damageSource, float amount)
    {
        return amount;
    }

    /**
     * Called on the heal event of affected entities.
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     * @param amount The amount of healing received.
     * @return Returns a potentially modified amount of healing.
     */
    public float healingTaken(MobEffectInstance effectInstance, LivingEntity livingEntity, float amount)
    {
        return amount;
    }

    /**
     * Called on the sleep event of affected entities.
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     * @param optionalPos The sleeping position, this is optional.
     * @return Returns true to allow the sleep event or false to try and cancel the sleep event.
     */
    public boolean sleep(MobEffectInstance effectInstance, LivingEntity livingEntity, Optional<BlockPos> optionalPos)
    {
        return true;
    }

    /**
     * Called on the item use event of affected entities.
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     * @param itemStack The item stack being used.
     * @param duration The duration the item has been used for.
     * @return Returns true to allow the item use or false to try and cancel the item use.
     */
    public boolean item(MobEffectInstance effectInstance, LivingEntity livingEntity, ItemStack itemStack, int duration)
    {
        return true;
    }
}

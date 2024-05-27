package com.schism.core.effects.actions;

import com.schism.core.database.CachedDefinition;
import com.schism.core.database.CachedList;
import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import com.schism.core.elements.ElementsDamageSource;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.elements.ElementRepository;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class DamageEffectAction extends AbstractEffectAction
{
    protected final CachedList<ElementDefinition> cachedElements;
    protected final float damage;
    protected final int tickRate;
    protected final boolean requireMovement;

    public DamageEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedElements = new CachedList<> (dataStore.listProp("elements").stream()
                .map(entry -> new CachedDefinition<>(entry.stringValue(), ElementRepository.get())).toList());
        this.damage = dataStore.floatProp("damage");
        this.tickRate = dataStore.intProp("tick_rate");
        this.requireMovement = dataStore.booleanProp("require_movement");
    }

    @Override
    public void update(MobEffectInstance effectInstance, LivingEntity livingEntity)
    {
        // Tick Rate:
        if (livingEntity.getLevel().getGameTime() % this.tickRate != 0) {
            return;
        }

        // Require Movement:
        if (this.requireMovement) {
            if (livingEntity.getVehicle() != null) {
                return;
            }
            if (livingEntity.walkDist == livingEntity.walkDistO) {
                return;
            }
        }

        int power = effectInstance.getAmplifier() + 1;
        DamageSource damageSource = new DamageSource(this.definition().subject());
        if (!this.cachedElements.get().isEmpty()) {
            damageSource = new ElementsDamageSource(this.cachedElements.get()).bypassArmor();
        }
        livingEntity.hurt(damageSource, this.damage * power);
    }
}

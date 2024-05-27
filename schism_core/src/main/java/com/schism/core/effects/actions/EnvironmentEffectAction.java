package com.schism.core.effects.actions;

import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class EnvironmentEffectAction extends AbstractEffectAction
{
    protected final String effect;
    protected final int ticks;

    public EnvironmentEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.effect = dataStore.stringProp("effect");
        this.ticks = dataStore.intProp("ticks");
    }

    @Override
    public void update(MobEffectInstance effectInstance, LivingEntity livingEntity)
    {
        if (livingEntity.getLevel().isClientSide()) {
            return;
        }
        switch (this.effect) {
            case "burn" -> {
                if (!livingEntity.fireImmune()) {
                    livingEntity.setSecondsOnFire(this.ticks / 20);
                }
            }
            case "extinguish" -> {
                if (!livingEntity.fireImmune()) {
                    livingEntity.setSecondsOnFire(0);
                }
            }
            case "freeze" -> {
                if (livingEntity.canFreeze()) { // Increases frozen ticks by 3 due to a timing problem with the poor freezing implementation on entities which reduce ticks by 2.
                    livingEntity.setIsInPowderSnow(true);
                    livingEntity.setTicksFrozen(Math.min(Math.max(this.ticks, livingEntity.getTicksFrozen() + 3), livingEntity.getTicksRequiredToFreeze() + 2));
                }
            }
            case "thaw" -> {
                if (livingEntity.canFreeze()) {
                    livingEntity.setIsInPowderSnow(false);
                    if (livingEntity.getTicksFrozen() > this.ticks) {
                        livingEntity.setTicksFrozen(this.ticks);
                    }
                }
            }
            case "drown" -> {
                if (!livingEntity.canBreatheUnderwater()) {
                    livingEntity.setAirSupply(Math.min(Math.max(livingEntity.getAirSupply() - this.ticks, 0), livingEntity.getMaxAirSupply()));
                }
            }
            case "breath" -> {
                if (!livingEntity.canBreatheUnderwater()) {
                    livingEntity.setAirSupply(Math.min(Math.max(livingEntity.getAirSupply() + this.ticks, 0), livingEntity.getMaxAirSupply()));
                }
            }
        }
    }
}

package com.schism.core.effects.actions;

import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class FearEffectAction extends AbstractEffectAction
{
    protected final float speed;

    public FearEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.speed = dataStore.floatProp("speed");
    }

    @Override
    public void update(MobEffectInstance effectInstance, LivingEntity livingEntity)
    {
        // TODO Create the Fear Entity, implement Extended Entities and implement Pickup Logic!
    }
}

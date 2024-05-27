package com.schism.core.effects.actions;

import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class BedEffectAction extends AbstractEffectAction
{
    protected final boolean cancel;

    public BedEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cancel = dataStore.booleanProp("cancel");
    }

    @Override
    public boolean sleep(MobEffectInstance effectInstance, LivingEntity livingEntity, Optional<BlockPos> optionalPos)
    {
        return this.cancel;
    }
}

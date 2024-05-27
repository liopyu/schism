package com.schism.core.blocks.actions;

import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.DataStore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EnvironmentBlockAction extends AbstractBlockAction
{
    protected final String effect;
    protected final int ticks;

    public EnvironmentBlockAction(BlockDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.effect = dataStore.stringProp("effect");
        this.ticks = dataStore.intProp("ticks");
    }

    @Override
    public BlockState entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity)
    {
        if (this.ticks <= 0) {
            return blockState;
        }
        if (entity instanceof LivingEntity livingEntity) {
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
                    if (livingEntity.canFreeze()) {
                        livingEntity.setIsInPowderSnow(true);
                        if (livingEntity.getTicksFrozen() < this.ticks) {
                            livingEntity.setTicksFrozen(this.ticks);
                        }
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
        return blockState;
    }
}

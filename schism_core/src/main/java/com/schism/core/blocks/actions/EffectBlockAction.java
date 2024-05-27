package com.schism.core.blocks.actions;

import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.CachedRegistryObject;
import com.schism.core.database.DataStore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectBlockAction extends AbstractBlockAction
{
    protected final CachedRegistryObject<MobEffect> cachedEffect;
    protected final int ticks;
    protected final int level;

    public EffectBlockAction(BlockDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedEffect = new CachedRegistryObject<>(dataStore.stringProp("effect_id"), () -> ForgeRegistries.MOB_EFFECTS);
        this.ticks = dataStore.intProp("ticks");
        this.level = dataStore.intProp("level");
    }

    @Override
    public BlockState entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity)
    {
        if (this.ticks <= 0 || this.level <= 0 || !this.cachedEffect.isPresent()) {
            return blockState;
        }

        MobEffectInstance effectInstance = new MobEffectInstance(this.cachedEffect.get(), this.ticks, this.level - 1);
        if (entity instanceof LivingEntity livingEntity && livingEntity.canBeAffected(effectInstance)) {
            ((LivingEntity) entity).addEffect(effectInstance);
        }
        return blockState;
    }
}

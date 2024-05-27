package com.schism.core.blocks.actions;

import com.schism.core.blocks.BlockDefinition;
import com.schism.core.creatures.CreatureDefinition;
import com.schism.core.creatures.CreatureRepository;
import com.schism.core.database.CachedDefinition;
import com.schism.core.database.CachedList;
import com.schism.core.database.DataStore;
import com.schism.core.elements.ElementsDamageSource;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.elements.ElementRepository;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class DamageBlockAction extends AbstractBlockAction
{
    protected final CachedList<ElementDefinition> cachedElements;
    protected final float damage;
    protected final int tickRate;

    public DamageBlockAction(BlockDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedElements = new CachedList<> (dataStore.listProp("elements").stream()
                .map(entry -> new CachedDefinition<>(entry.stringValue(), ElementRepository.get())).toList());
        this.damage = dataStore.floatProp("damage");
        this.tickRate = dataStore.intProp("tick_rate");
    }

    @Override
    public BlockState entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity)
    {
        if (!(entity instanceof LivingEntity livingEntity) || level.getGameTime() % this.tickRate != 0) {
            return blockState;
        }

        // Non-Player Base Element Immunity:
        if (this.cachedElements.get().size() == 1 && !(livingEntity instanceof Player)) {
            Optional<CreatureDefinition> creatureDefinition = CreatureRepository.get().getDefinition(livingEntity);
            if (creatureDefinition.isPresent() && creatureDefinition.get().baseElement() == this.cachedElements.get().get(0)) {
                return blockState;
            }
        }

        DamageSource damageSource = new DamageSource(this.definition().subject());
        if (!this.cachedElements.get().isEmpty()) {
            damageSource = level.damageSources().generic();
        }
        entity.hurt(damageSource, this.damage);

        return blockState;
    }
}

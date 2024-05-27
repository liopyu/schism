package com.schism.core.blocks.actions;

import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.DataStore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class KillBlockAction extends AbstractBlockAction
{
    protected final boolean items;
    protected final boolean experienceOrbs;
    protected final boolean mobs;

    public KillBlockAction(BlockDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.items = dataStore.booleanProp("items");
        this.experienceOrbs = dataStore.booleanProp("experience_orbs");
        this.mobs = dataStore.booleanProp("mobs");
    }

    @Override
    public BlockState entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity)
    {
        if (entity instanceof ItemEntity || entity instanceof ExperienceOrb) {
            entity.kill();
        }
        return blockState;
    }
}

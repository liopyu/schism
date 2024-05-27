package com.schism.core.altars.actions;

import com.schism.core.altars.AltarDefinition;
import com.schism.core.creatures.Creatures;
import com.schism.core.creatures.ICreature;
import com.schism.core.database.DataStore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class WorshipAltarAction extends AbstractAltarAction
{
    protected final String god;
    protected final int devotion;

    public WorshipAltarAction(AltarDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.god = dataStore.stringProp("god");
        this.devotion = dataStore.intProp("devotion");
    }

    @Override
    public void onActivate(LivingEntity livingEntity, BlockPos corePosition, String ritual, Entity tributeEntity)
    {
        if (livingEntity.getLevel().isClientSide() || !ritual.equals(this.ritual)) {
            return;
        }

        ICreature creature = Creatures.get().getCreature(livingEntity).orElse(null);
        if (creature == null) {
            return;
        }
        creature.piety().addDevotion(this.god, this.devotion);
    }
}

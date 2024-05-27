package com.schism.core.altars;

import com.schism.core.altars.actions.AbstractAltarAction;
import com.schism.core.altars.schematics.AbstractAltarStructure;
import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.DataStore;
import com.schism.core.util.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class AltarDefinition extends AbstractDefinition
{
    protected AbstractAltarStructure structure;
    protected Vec3 corePosition;
    protected List<AbstractAltarAction> actions;

    /**
     * Altar definitions hold information about altars which are structures that can trigger other things like item actions.
     * @param subject The name of the altar.
     * @param dataStore The data store that holds config data about this definition.
     */
    public AltarDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public String type()
    {
        return AltarRepository.get().type();
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);

        this.structure = AbstractAltarStructure.create(dataStore.prop("structure"));
        this.corePosition = dataStore.vec3Prop("core_position");
        this.actions = AbstractAltarAction.listFromMap(this, dataStore.mapProp("altar_actions"));
    }


    /**
     * Gets the structure used to identify and create this altar.
     * @return The altar structure.
     */
    public AbstractAltarStructure structure()
    {
        return this.structure;
    }

    /**
     * Gets a list of actions this altar performs when activated.
     * @return A list of actions this altar performs on activation.
     */
    public List<AbstractAltarAction> actions()
    {
        return this.actions;
    }

    /**
     * Gets the position within the structure of the altar's core.
     * @return The altar core position.
     */
    public Vec3 corePosition()
    {
        return this.corePosition;
    }

    /**
     * Gets the blocks that act as the altar core.
     * @return The altar core blocks.
     */
    public List<Block> coreBlocks()
    {
        return this.structure().blocksAt(this.corePosition());
    }

    /**
     * Activates this altar calling all actions.
     * @param livingEntity The entity that activated the altar.
     * @param corePosition The position of the activated altar's core.
     * @param ritual The name of the ritual that was activated.
     * @param tributeEntity An entity that was involved in the activation, could be a sacrifice or the same as the activator entity.
     */
    public void activate(LivingEntity livingEntity, BlockPos corePosition, String ritual, Entity tributeEntity)
    {
        this.actions().forEach(action -> action.onActivate(livingEntity, corePosition, ritual, tributeEntity));
    }
}

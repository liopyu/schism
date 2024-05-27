package com.schism.core.blocks;

import com.schism.core.database.IHasDefinition;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;

abstract public class Fluid extends ForgeFlowingFluid implements IHasDefinition<BlockDefinition>
{
    protected final BlockDefinition definition;

    /**
     * Constructor
     * @param blockDefinition The Block Definition.
     * @param properties The Fluid Properties.
     */
    public Fluid(BlockDefinition blockDefinition, Properties properties)
    {
        super(properties);
        this.definition = blockDefinition;
    }

    @Override
    public BlockDefinition definition()
    {
        return this.definition;
    }

    @Override
    public int getSlopeFindDistance(LevelReader levelReader) {
        return this.definition().fluidSloping();
    }

    @Override
    protected int getDropOff(LevelReader levelReader)
    {
        return Math.max(this.definition().fluidDropOff(), 1);
    }

    public static class Flowing extends Fluid
    {
        public Flowing(BlockDefinition blockDefinition, Properties properties)
        {
            super(blockDefinition, properties);
            this.setRegistryName(blockDefinition.subject() + "_flowing");
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<net.minecraft.world.level.material.Fluid, FluidState> fluidStateBuilder)
        {
            super.createFluidStateDefinition(fluidStateBuilder);
            fluidStateBuilder.add(LEVEL);
        }

        @Override
        public int getAmount(@NotNull FluidState fluidState)
        {
            return fluidState.getValue(LEVEL);
        }

        @Override
        public boolean isSource(@NotNull FluidState fluidState)
        {
            return false;
        }
    }

    public static class Still extends Fluid
    {
        public Still(BlockDefinition blockDefinition, Properties properties)
        {
            super(blockDefinition, properties);
            this.setRegistryName(blockDefinition.subject());
        }

        @Override
        public int getAmount(@NotNull FluidState fluidState)
        {
            return 8;
        }

        @Override
        public boolean isSource(@NotNull FluidState fluidState)
        {
            return true;
        }
    }
}

package com.schism.core.blocks.types;

import com.schism.core.blocks.BlockActor;
import com.schism.core.blocks.BlockDefinition;
import com.schism.core.database.IHasDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class FenceBlock extends net.minecraft.world.level.block.FenceBlock implements IHasDefinition<BlockDefinition>
{
    protected final BlockDefinition definition;
    protected final BlockActor actor;

    /**
     * Constructor
     * @param definition The Block Definition.
     * @param properties The Block Properties.
     */
    public FenceBlock(BlockDefinition definition, BlockActor actor, Block.Properties properties)
    {
        super(properties);
        this.setRegistryName(definition.resourceLocation());
        this.definition = definition;
        this.actor = actor;
        this.registerDefaultState(this.actor.defaultState(this.getStateDefinition().any()));
    }

    @Override
    public BlockDefinition definition()
    {
        return this.definition;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(itemStack, blockGetter, list, tooltipFlag);
        list.add(Component.translatable(this.getDescriptionId() + ".description").withStyle(ChatFormatting.GREEN));
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        BlockActor.createBlockStateDefinition(builder);
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState replaceBlockState, boolean moving)
    {
        super.onPlace(blockState, level, blockPos, replaceBlockState, moving);
        this.actor.onPlace(blockState, level, blockPos, replaceBlockState, moving);
    }

    @Override
    public void neighborChanged(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Block neighborBlock, @NotNull BlockPos neighborBlockPos, boolean someBoolean)
    {
        this.actor.neighborChangedBlock(blockState, level, blockPos, neighborBlock, neighborBlockPos, someBoolean);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState neighborBlockState, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos neighborBlockPos)
    {
        blockState = super.updateShape(blockState, direction, neighborBlockState, levelAccessor, blockPos, neighborBlockPos);
        return this.actor.neighborChangedBlockState(blockState, levelAccessor, blockPos, direction, neighborBlockState, neighborBlockPos);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos)
    {
        return this.actor.canSurvive(blockState, levelReader, blockPos);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel level, BlockPos blockPos, Random random)
    {
        super.tick(blockState, level, blockPos, random);
        this.actor.tick(blockState, level, blockPos, random);
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity)
    {
        super.entityInside(blockState, level, blockPos, entity);
        this.actor.entityInside(blockState, level, blockPos, entity);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult)
    {
        super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
        return this.actor.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random)
    {
        super.animateTick(blockState, level, blockPos, random);
        this.actor.animateTick(blockState, level, blockPos, random);
    }
}

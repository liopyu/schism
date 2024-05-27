package com.schism.core.items;

import com.schism.core.util.Helpers.Material;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class FluidBucketItem extends BucketItem
{
    public FluidBucketItem(Supplier<? extends ForgeFlowingFluid> supplier, Properties builder)
    {
        super(supplier, builder);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(itemStack, level, list, tooltipFlag);
        list.add(Component.translatable(this.getDescriptionId() + ".description").withStyle(ChatFormatting.GREEN));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BlockHitResult blockHitResult = getPlayerPOVHitResult(level, player, this.getFluid() == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        InteractionResultHolder<ItemStack> result = net.minecraftforge.event.ForgeEventFactory.onBucketUse(player, level, itemStack, blockHitResult);

        if (result != null) {
            return result;
        }
        if (blockHitResult.getType() == HitResult.Type.MISS || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        }

        BlockPos blockPos = blockHitResult.getBlockPos();
        Direction direction = blockHitResult.getDirection();
        BlockPos targetBlockPos = blockPos.relative(direction);
        if (level.mayInteract(player, blockPos) && player.mayUseItemAt(targetBlockPos, direction, itemStack)) {
            BlockState blockState = level.getBlockState(blockPos);

            if (this.getFluid() == Fluids.EMPTY) {
                if (blockState.getBlock() instanceof BucketPickup bucketPickup) {
                    ItemStack pickupItemStack = bucketPickup.pickupBlock(level, blockPos, blockState);
                    if (!pickupItemStack.isEmpty()) {
                        player.awardStat(Stats.ITEM_USED.get(this));
                        bucketPickup.getPickupSound(blockState).ifPresent((p_150709_) -> player.playSound(p_150709_, 1.0F, 1.0F));
                        level.gameEvent(player, GameEvent.FLUID_PICKUP, blockPos);
                        ItemStack filledItemStack = ItemUtils.createFilledResult(itemStack, player, pickupItemStack);

                        if (!level.isClientSide) {
                            CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, pickupItemStack);
                        }

                        return InteractionResultHolder.sidedSuccess(filledItemStack, level.isClientSide());
                    }
                }
                return InteractionResultHolder.fail(itemStack);
            }

            BlockPos emptyBlockPos = canBlockContainFluid(level, blockPos, blockState) ? blockPos : targetBlockPos;
            if (this.emptyContents(player, level, emptyBlockPos, blockHitResult)) {
                this.checkExtraContent(player, level, itemStack, emptyBlockPos);
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, emptyBlockPos, itemStack);
                }
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.sidedSuccess(getEmptySuccessItem(itemStack, player), level.isClientSide());
            }
        }

        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos blockPos, @Nullable BlockHitResult blockHitResult) {
        if (!(this.getFluid() instanceof FlowingFluid)) {
            return false;
        }

        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
                BlockBehaviour.BlockStateBase material = blockState;
        boolean canBeReplaced = blockState.canBeReplaced(this.getFluid());
        boolean replace = blockState.isAir() || canBeReplaced || block instanceof LiquidBlockContainer && ((LiquidBlockContainer)block).canPlaceLiquid(level, blockPos, blockState, this.getFluid());
        if (!replace) {
            return blockHitResult != null && this.emptyContents(player, level, blockHitResult.getBlockPos().relative(blockHitResult.getDirection()), null);
        }

        if (block instanceof LiquidBlockContainer && ((LiquidBlockContainer)block).canPlaceLiquid(level, blockPos, blockState, this.getFluid())) {
            ((LiquidBlockContainer)block).placeLiquid(level, blockPos, blockState, ((FlowingFluid)this.getFluid()).getSource(false));
            this.playEmptySound(player, level, blockPos);
            return true;
        }

        if (!level.isClientSide && canBeReplaced && !material.liquid()) {
            level.destroyBlock(blockPos, true);
        }

        if (!level.setBlock(blockPos, this.getFluid().defaultFluidState().createLegacyBlock(), 11) && !blockState.getFluidState().isSource()) {
            return false;
        }

        this.playEmptySound(player, level, blockPos);
        return true;
    }

    @Override
    protected void playEmptySound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos) {
        SoundEvent soundEvent = this.getFluid().getFluidType().getSound(SoundActions.BUCKET_EMPTY);
        if(soundEvent == null) {
            soundEvent = this.getFluid().is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        }
        levelAccessor.playSound(player, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        levelAccessor.gameEvent(player, GameEvent.FLUID_PLACE, blockPos);
    }

    /**
     * Determines if the provided position can contain a fluid.
     * @param level The level.
     * @param blockPos The position to check.
     * @param blockState The block state to check.
     * @return True if a fluid can be contained.
     */
    public boolean canBlockContainFluid(Level level, BlockPos blockPos, BlockState blockState)
    {
        return blockState.getBlock() instanceof LiquidBlockContainer && ((LiquidBlockContainer)blockState.getBlock()).canPlaceLiquid(level, blockPos, blockState, this.getFluid());
    }
}

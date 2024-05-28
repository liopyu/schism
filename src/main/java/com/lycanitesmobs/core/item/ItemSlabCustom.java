package com.lycanitesmobs.core.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.Property;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

// This is a copy of ItemSlab and only altered to allow for any block to be the double slab.
public class ItemSlabCustom extends BlockItem
{
    private final SlabBlock singleSlab;
    private final Block doubleSlab;

    public ItemSlabCustom(Item.Properties properties, Block block, SlabBlock singleSlab, Block doubleSlab) {
        super(block, properties);
        this.singleSlab = singleSlab;
        this.doubleSlab = doubleSlab;
        properties.durability(0);
        this.setRegistryName(block.getRegistryName());
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getDescriptionId(ItemStack stack) {
        return this.singleSlab.getDescriptionId();
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        return this.singleSlab.getName();
    }

    /**
     * Called when a Block is right-clicked with this Item
     */
    public ActionResultType onItemUse(PlayerEntity player, World worldIn, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        // TODO Do we still need this custom BlockItem?
        /*ItemStack itemStack = player.getHeldItem(hand);
        if (itemStack.getCount() != 0 && player.canPlayerEdit(pos.offset(facing), facing, itemStack)) {
            Comparable<?> comparable = this.singleSlab.getTypeForItem(itemStack);
            BlockState iblockstate = worldIn.getBlockState(pos);

            if (iblockstate.getBlock() == this.singleSlab) {
                IProperty<?> iproperty = this.singleSlab.getVariantProperty();
                Comparable<?> comparable1 = iblockstate.getValue(iproperty);
                SlabBlock.EnumBlockHalf blockslab$enumblockhalf = (SlabBlock.EnumBlockHalf)iblockstate.getValue(SlabBlock.HALF);

                if ((facing == EnumFacing.UP && blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.BOTTOM || facing == EnumFacing.DOWN && blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.TOP) && comparable1 == comparable) {
                    BlockState iblockstate1 = this.makeState(iproperty, comparable1);
                    AxisAlignedBB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, pos);

                    if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos)) && worldIn.setBlockState(pos, iblockstate1, 11)) {
                        SoundType soundtype = this.doubleSlab.getSoundType();
                        worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                        itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
                    }

                    return ActionResultType.SUCCESS;
                }
            }

            return this.tryPlace(player, itemStack, worldIn, pos.offset(facing), comparable) ? EnumActionResult.SUCCESS : super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        }
        else {
            return ActionResultType.FAIL;
        }*/
        return ActionResultType.FAIL;
    }

    /*@OnlyIn(Dist.CLIENT)
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, PlayerEntity player, ItemStack stack) {
        BlockPos blockpos = pos;
        IProperty<?> iproperty = this.singleSlab.getVariantProperty();
        Comparable<?> comparable = this.singleSlab.getTypeForItem(stack);
        BlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() == this.singleSlab)
        {
            boolean top = iblockstate.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;

            if ((side == EnumFacing.UP && !top || side == EnumFacing.DOWN && top) && comparable == iblockstate.getValue(iproperty))
            {
                return true;
            }
        }

        pos = pos.offset(side);
        BlockState iblockstate1 = worldIn.getBlockState(pos);
        return iblockstate1.getBlock() == this.singleSlab && comparable == iblockstate1.getValue(iproperty) ? true : super.canPlaceBlockOnSide(worldIn, blockpos, side, player, stack);
    }

    private boolean tryPlace(PlayerEntity player, ItemStack itemStack, World worldIn, BlockPos pos, Object itemSlabType)
    {
        BlockState iblockstate = worldIn.getBlockState(pos);

        if (iblockstate.getBlock() == this.singleSlab)
        {
            Comparable<?> comparable = iblockstate.getValue(this.singleSlab.getVariantProperty());

            if (comparable == itemSlabType)
            {
                BlockState iblockstate1 = this.makeState(this.singleSlab.getVariantProperty(), comparable);
                AxisAlignedBB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, pos);

                if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos)) && worldIn.setBlockState(pos, iblockstate1, 11))
                {
                    SoundType soundtype = this.doubleSlab.getSoundType();
                    worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
                }

                return true;
            }
        }

        return false;
    }*/

    protected <T extends Comparable<T>> BlockState makeState(Property<T> p_185055_1_, Comparable<?> p_185055_2_) {
        return this.doubleSlab.defaultBlockState();//.withProperty(p_185055_1_, (T)p_185055_2_);
    }
}
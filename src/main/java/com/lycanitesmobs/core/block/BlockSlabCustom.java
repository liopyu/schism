package com.lycanitesmobs.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockSlabCustom extends SlabBlock {
    public String blockName = "BlockBase";

	// ==================================================
	//                   Constructor
	// ==================================================
	public BlockSlabCustom(Block.Properties properties, BlockBase block) {
		super(properties);
        String slabName = "_slab";
        this.setRegistryName(new ResourceLocation(block.group.modid, block.blockName + slabName));
	}

    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    public IFormattableTextComponent getName() {
        return new TranslationTextComponent(this.getDescriptionId());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(this.getDescription(stack, world));
    }

    public ITextComponent getDescription(ItemStack itemStack, @Nullable IBlockReader world) {
        return new TranslationTextComponent(this.getDescriptionId() + ".description").withStyle(TextFormatting.GREEN);
    }


    // ==================================================
    //                    Harvesting
    // ==================================================
    @Nullable
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return Item.byBlock(this);
    }

    public ItemStack getItem(World worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }
}

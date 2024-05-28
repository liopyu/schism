package com.lycanitesmobs.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.WallBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class BlockWallCustom extends WallBlock {
	public String blockName = "BlockBase";

	// ==================================================
	//                   Constructor
	// ==================================================
	public BlockWallCustom(Block.Properties properties, BlockBase block) {
		super(properties);
        this.setRegistryName(new ResourceLocation(block.group.modid, block.blockName + "_wall"));
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
}

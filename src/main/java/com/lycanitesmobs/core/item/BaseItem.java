package com.lycanitesmobs.core.item;

import com.google.common.collect.Multimap;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.info.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.Item.Properties;

public class BaseItem extends Item {
	public static int DESCRIPTION_WIDTH = 200;
	
	public String itemName = "unamed_item";
	public ModInfo modInfo = LycanitesMobs.modInfo;

    public BaseItem(Properties properties) {
    	super(properties);
    }

    public void setup() {
        this.setRegistryName(this.modInfo.modid, this.itemName);
    }

    @Override
	@Nonnull
	public String getDescriptionId() {
    	return "item." + this.modInfo.modid + "." + this.itemName;
	}

	@Override
	public ITextComponent getName(ItemStack stack) {
		return new TranslationTextComponent(this.getDescriptionId(stack));
	}

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flag) {
		ITextComponent description = this.getDescription(stack, worldIn, tooltip, flag);
    	if(!"".equalsIgnoreCase(description.getString())) {
			tooltip.add(description);
    	}
    	super.appendHoverText(stack, worldIn, tooltip, flag);
    }

    public ITextComponent getDescription(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flag) {
    	return new TranslationTextComponent(this.getDescriptionId() + ".description").withStyle(TextFormatting.GREEN);
    }

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		return super.onEntityItemUpdate(stack, entity);
	}

    @Override
    public ActionResultType useOn(ItemUseContext context) {
    	return super.useOn(context);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
    	return super.use(world, player, hand);
    }

	public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
		return super.onLeftClickEntity(stack, player, entity);
	}

	@Override
	public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
    	return super.interactLivingEntity(stack, player, entity, hand);
	}

    @Override
    public void onUsingTick(ItemStack itemStack, LivingEntity entity, int useRemaining) {
    	super.onUsingTick(itemStack, entity, useRemaining);
    }

    @Override
    public void releaseUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
    	super.releaseUsing(stack, worldIn, entityLiving, timeLeft);
    }

    @Override
    public UseAction getUseAnimation(ItemStack itemStack) {
        return super.getUseAnimation(itemStack);
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairStack) {
        return super.isValidRepairItem(itemStack, repairStack);
    }

	/** Gets or creates an NBT Compound for the provided itemstack. **/
	public CompoundNBT getTagCompound(ItemStack itemStack) {
		if(itemStack.hasTag()) {
			return itemStack.getTag();
		}
		return new CompoundNBT();
	}

    public void playSound(World world, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        world.playSound(null, x, y, z, sound, category, volume, pitch);
    }

    public void playSound(World world, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        world.playSound(null, pos, sound, category, volume, pitch);
    }
}

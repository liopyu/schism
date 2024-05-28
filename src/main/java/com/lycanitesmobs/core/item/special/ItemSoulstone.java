package com.lycanitesmobs.core.item.special;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureType;
import com.lycanitesmobs.core.item.BaseItem;
import com.lycanitesmobs.core.item.CreatureTypeItem;
import com.lycanitesmobs.core.pets.PetEntry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemSoulstone extends CreatureTypeItem {
    public ItemSoulstone(Item.Properties properties, @Nullable CreatureType creatureType) {
		super(properties, "soulstone", creatureType);
        if(creatureType != null) {
        	this.itemName += creatureType.getName();
		}
        this.setup();
    }

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
    	return super.use(world, player, hand);
	}

	@Override
	public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
    	if(this.applySoulstoneToEntity(player, entity)) {
			// Consume Soulstone:
			if (!player.abilities.instabuild)
				stack.setCount(Math.max(0, stack.getCount() - 1));
			if (stack.getCount() <= 0)
				player.inventory.setItem(player.inventory.selected, ItemStack.EMPTY);

			return ActionResultType.SUCCESS;
		}

    	return super.interactLivingEntity(stack, player, entity, hand);
	}

	/**
	 * Applies this Soulstone to the provided entity.
	 * @param player The player using the Soulstone.
	 * @param entity The entity targeted by the Soulstone.
	 * @return True on success.
	 */
	public boolean applySoulstoneToEntity(PlayerEntity player, LivingEntity entity) {
		ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
		if(playerExt == null)
			return false;
		if(!(entity instanceof TameableCreatureEntity)) {
			if(!player.getCommandSenderWorld().isClientSide)
				player.sendMessage(new TranslationTextComponent("message.soulstone.invalid"), Util.NIL_UUID);
			return false;
		}

		TameableCreatureEntity entityTameable = (TameableCreatureEntity)entity;
		CreatureInfo creatureInfo = entityTameable.creatureInfo;
		if(!creatureInfo.isTameable() || entityTameable.getOwner() != player) {
			if(!player.getCommandSenderWorld().isClientSide)
				player.sendMessage(new TranslationTextComponent("message.soulstone.untamed"), Util.NIL_UUID);
			return false;
		}
		if(entityTameable.getPetEntry() != null) {
			if(!player.getCommandSenderWorld().isClientSide)
				player.sendMessage(new TranslationTextComponent("message.soulstone.exists"), Util.NIL_UUID);
			return false;
		}

		// Particle Effect:
		if(player.getCommandSenderWorld().isClientSide) {
			for(int i = 0; i < 32; ++i) {
				entity.getCommandSenderWorld().addParticle(ParticleTypes.HAPPY_VILLAGER,
						entity.position().x() + (4.0F * player.getRandom().nextFloat()) - 2.0F,
						entity.position().y() + (4.0F * player.getRandom().nextFloat()) - 2.0F,
						entity.position().z() + (4.0F * player.getRandom().nextFloat()) - 2.0F,
						0.0D, 0.0D, 0.0D);
			}
		}

		// Store Pet:
		if(!player.getCommandSenderWorld().isClientSide) {
			String petType = "pet";
			if(entityTameable.creatureInfo.isMountable()) {
				petType = "mount";
			}

			ITextComponent message = new TranslationTextComponent("message.soulstone." + petType + ".added.prefix")
					.append(" ")
					.append(creatureInfo.getTitle())
					.append(" ")
					.append(new TranslationTextComponent("message.soulstone." + petType + ".added.suffix"));
			player.sendMessage(message, Util.NIL_UUID);
			//player.addStat(ObjectManager.getStat("soulstone"), 1);

			// Add Pet Entry:
			PetEntry petEntry = PetEntry.createFromEntity(player, entityTameable, petType);
			playerExt.petManager.addEntry(petEntry);
			playerExt.sendPetEntriesToPlayer(petType);
			petEntry.assignEntity(entity);
			entityTameable.setPetEntry(petEntry);
		}

		return true;
	}
}

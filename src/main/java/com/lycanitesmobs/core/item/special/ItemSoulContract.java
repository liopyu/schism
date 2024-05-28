package com.lycanitesmobs.core.item.special;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.item.BaseItem;
import com.lycanitesmobs.core.pets.PetEntry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemSoulContract extends BaseItem {

    public ItemSoulContract(Properties properties) {
		super(properties);
		this.modInfo = LycanitesMobs.modInfo;
        this.itemName = "soul_contract";
        this.setup();
    }

	/** Returns the owner uuid. **/
	public UUID getOwnerUUID(ItemStack itemStack) {
		CompoundNBT nbt = this.getTagCompound(itemStack);
		UUID uuid = null;
		if(nbt.contains("ownerUUID")) {
			uuid = nbt.getUUID("ownerUUID");
		}
		return uuid;
	}

	/** Returns the pet entry uuid. **/
	public UUID getPetEntryUUID(ItemStack itemStack) {
		CompoundNBT nbt = this.getTagCompound(itemStack);
		UUID uuid = null;
		if(nbt.contains("petEntryUUID")) {
			uuid = nbt.getUUID("petEntryUUID");
		}
		return uuid;
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
    	return super.use(world, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, world, tooltip, tooltipFlag);

		UUID ownerUUID = this.getOwnerUUID(itemStack);
		UUID petEntryUUID = this.getPetEntryUUID(itemStack);
		if (ownerUUID != null && petEntryUUID != null) {
			PlayerEntity owner = world.getPlayerByUUID(ownerUUID);
			ExtendedPlayer extendedOwner = ExtendedPlayer.getForPlayer(owner);
			if (owner == null || extendedOwner == null) {
				tooltip.add(new TranslationTextComponent("item.lycanitesmobs.soul_contract.offline"));
				return;
			}
			PetEntry petEntry = extendedOwner.petManager.getEntry(petEntryUUID);
			if (petEntry == null) {
				tooltip.add(new TranslationTextComponent("item.lycanitesmobs.soul_contract.released").withStyle(TextFormatting.DARK_RED));
				tooltip.add(new TranslationTextComponent("item.lycanitesmobs.soul_contract.owner").append(" ").append(owner.getDisplayName()).withStyle(TextFormatting.DARK_PURPLE));
				return;
			}
			tooltip.add(new TranslationTextComponent("item.lycanitesmobs.soul_contract.bound").append(" ").append(petEntry.getDisplayName()).withStyle(TextFormatting.GOLD));
			tooltip.add(new TranslationTextComponent("item.lycanitesmobs.soul_contract.owner").append(" ").append(owner.getDisplayName()).withStyle(TextFormatting.DARK_PURPLE));
		}
	}

	@Override
	public ActionResultType interactLivingEntity(ItemStack itemStack, PlayerEntity player, LivingEntity entity, Hand hand) {
		// Invalid Entity:
		if(!(entity instanceof BaseCreatureEntity) || !((BaseCreatureEntity)entity).isBoundPet()) {
			if (!player.getCommandSenderWorld().isClientSide) {
				player.sendMessage(new TranslationTextComponent("message.soul_contract.invalid").withStyle(TextFormatting.RED), Util.NIL_UUID);
			}
			return ActionResultType.PASS;
		}

		BaseCreatureEntity creatureEntity = (BaseCreatureEntity)entity;
    	UUID ownerUUID = this.getOwnerUUID(itemStack);
    	UUID petEntryUUID = this.getPetEntryUUID(itemStack);

		if (ownerUUID == null || petEntryUUID == null) {
			return this.bindSoul(itemStack, player, creatureEntity);
		}

		return this.transferSoul(itemStack, player, creatureEntity, ownerUUID, petEntryUUID);
	}

	/**
	 * Binds the provided pet uuid to this soul contract.
	 * @param itemStack The Soul Contract itemstack.
	 * @param player The player using the Soul Contract to transfer from.
	 * @param creatureEntity The entity targeted by the Soul Contract.
	 * @return Pass on failure otherwise success.
	 */
	public ActionResultType bindSoul(ItemStack itemStack, PlayerEntity player, BaseCreatureEntity creatureEntity) {

		// Check Ownership:
		if(creatureEntity.getOwner() != player || "familiar".equalsIgnoreCase(creatureEntity.getPetEntry().getType())) {
			if (!player.getCommandSenderWorld().isClientSide) {
				player.sendMessage(new TranslationTextComponent("message.soul_contract.not_owner").withStyle(TextFormatting.RED), Util.NIL_UUID);
			}
			return ActionResultType.PASS;
		}

		// Bind Soul:
		if (!player.getCommandSenderWorld().isClientSide) {
			CompoundNBT nbt = this.getTagCompound(itemStack);
			nbt.putUUID("ownerUUID", player.getUUID());
			nbt.putUUID("petEntryUUID", creatureEntity.getPetEntry().petEntryID);
			itemStack.setTag(nbt);
			player.inventory.setItem(player.inventory.selected, itemStack);
			player.sendMessage(new TranslationTextComponent("message.soul_contract.bound").withStyle(TextFormatting.GREEN), Util.NIL_UUID);
		}

		return ActionResultType.SUCCESS;
	}

	/**
	 * Transfers the bound pet to a new owner.
	 * @param itemStack The Soul Contract itemstack.
	 * @param player The player using the Soul Contract to transfer to.
	 * @param creatureEntity The entity targeted by the Soul Contract to check for a match.
	 * @param ownerUUID The unique id of the pet owner.
	 * @param petEntryUUID The unique id of the pet entry to transfer.
	 * @return Pass on failure otherwise success.
	 */
	public ActionResultType transferSoul(ItemStack itemStack, PlayerEntity player, BaseCreatureEntity creatureEntity, UUID ownerUUID, UUID petEntryUUID) {
		// Transferring to Self:
		if (player.getUUID().equals(ownerUUID)) {
			// Unbind from Contract:
			if (petEntryUUID.equals(creatureEntity.getPetEntry().petEntryID)) {
				if (!player.getCommandSenderWorld().isClientSide) {
					CompoundNBT nbt = this.getTagCompound(itemStack);
					nbt.remove("ownerUUID");
					nbt.remove("petEntryUUID");
					itemStack.setTag(nbt);
					player.inventory.setItem(player.inventory.selected, itemStack);
					player.sendMessage(new TranslationTextComponent("message.soul_contract.unbound").withStyle(TextFormatting.LIGHT_PURPLE), Util.NIL_UUID);
				}
				return ActionResultType.SUCCESS;
			}

			// Bind a different Creature to the Contract instead:
			return this.bindSoul(itemStack, player, creatureEntity);
		}

		// Transfer on Wrong Pet:
		if (!petEntryUUID.equals(creatureEntity.getPetEntry().petEntryID)) {
			if (!player.getCommandSenderWorld().isClientSide) {
				player.sendMessage(new TranslationTextComponent("message.soul_contract.wrong_target").withStyle(TextFormatting.RED), Util.NIL_UUID);
			}
			return ActionResultType.PASS;
		}

		// Get Owner:
		PlayerEntity owner = player.getCommandSenderWorld().getPlayerByUUID(ownerUUID);
		ExtendedPlayer extendedOwner = ExtendedPlayer.getForPlayer(owner);
		if (owner == null || extendedOwner == null) {
			if (!player.getCommandSenderWorld().isClientSide) {
				player.sendMessage(new TranslationTextComponent("message.soul_contract.owner_missing").withStyle(TextFormatting.RED), Util.NIL_UUID);
			}
			return ActionResultType.PASS;
		}

		// Get Receiver:
		ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
		if (extendedPlayer == null) {
			return ActionResultType.PASS;
		}

		// Transfer to New Owner:
		if (!player.getCommandSenderWorld().isClientSide) {
			extendedOwner.petManager.removeEntry(creatureEntity.getPetEntry());
			extendedPlayer.petManager.addEntry(creatureEntity.getPetEntry());
			extendedOwner.sendPetEntriesToPlayer(creatureEntity.getPetEntry().getType());
			extendedOwner.sendPetEntryRemoveToPlayer(creatureEntity.getPetEntry());
			extendedPlayer.sendPetEntriesToPlayer(creatureEntity.getPetEntry().getType());
			owner.sendMessage(new TranslationTextComponent("message.soul_contract.transferred").withStyle(TextFormatting.GREEN), Util.NIL_UUID);
			player.sendMessage(new TranslationTextComponent("message.soul_contract.transferred").withStyle(TextFormatting.GREEN), Util.NIL_UUID);
		}
		if (creatureEntity instanceof TameableCreatureEntity) {
			((TameableCreatureEntity) creatureEntity).setPlayerOwner(player);
		}
		player.inventory.setItem(player.inventory.selected, ItemStack.EMPTY);

		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return this.getOwnerUUID(itemStack) != null && this.getPetEntryUUID(itemStack) != null;
	}
}

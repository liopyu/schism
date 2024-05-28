package com.lycanitesmobs.core.item.equipment.features;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class SummonEquipmentFeature extends EquipmentFeature {
	/** The id of the mob to summon. **/
	public String summonMobId;

	/** The chance on summoning mobs. **/
	public double summonChance = 0.05;

	/** How long in ticks the summoned creature lasts for. **/
	public int summonDuration = 60;

	/** The minimum amount of mobs to summon. **/
	public int summonCountMin = 1;

	/** The maximum amount of mobs to summon. **/
	public int summonCountMax = 1;

	/** The size scale of summoned mobs. **/
	public double sizeScale = 1;


	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);

		this.summonMobId = json.get("summonMobId").getAsString();

		if(json.has("summonChance"))
			this.summonChance = json.get("summonChance").getAsDouble();

		if(json.has("summonDuration"))
			this.summonDuration = json.get("summonDuration").getAsInt();

		if(json.has("summonCountMin"))
			this.summonCountMin = json.get("summonCountMin").getAsInt();

		if(json.has("summonCountMax"))
			this.summonCountMax = json.get("summonCountMax").getAsInt();

		if(json.has("sizeScale"))
			this.sizeScale = json.get("sizeScale").getAsDouble();
	}

	@Override
	public ITextComponent getDescription(ItemStack itemStack, int level) {
		if(!this.isActive(itemStack, level)) {
			return null;
		}
		TextComponent description = (TextComponent) new TranslationTextComponent("equipment.feature." + this.featureType)
			.append(" ").append(new TranslationTextComponent("entity." + this.summonMobId));

		if(this.summonCountMin != this.summonCountMax) {
			description.append(" x" + (this.summonCountMin + " - " + this.summonCountMax));
		}
		else {
			description.append(" x" + this.summonCountMax);
		}

		description.append(" " + Math.round(this.summonChance * 100) + "%");

		if(this.summonDuration > 0) {
			description.append(" " + ((float)this.summonDuration / 20) + "s");
		}

		return description;
	}

	@Override
	public ITextComponent getSummary(ItemStack itemStack, int level) {
		if(!this.isActive(itemStack, level)) {
			return null;
		}
		return new TranslationTextComponent("entity." + this.summonMobId);
	}

	/**
	 * Called when an entity is hit by equipment with this feature.
	 * @param itemStack The ItemStack being hit with.
	 * @param target The target entity being hit.
	 * @param attacker The entity using this item to hit.
	 */
	public boolean onHitEntity(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
		if(target == null || attacker == null || attacker.getCommandSenderWorld().isClientSide) {
			return false;
		}

		// Summon:
		EntityType entityType = null;
		CreatureInfo creatureInfo = CreatureManager.getInstance().getCreatureFromId(this.summonMobId);
		if(creatureInfo != null) {
			entityType = creatureInfo.getEntityType();
		}
		else {
			Object entityTypeObj = GameRegistry.findRegistry(EntityType.class).getValue(new ResourceLocation(this.summonMobId));
			if(entityTypeObj instanceof EntityType) {
				entityType = (EntityType)entityTypeObj;
			}
		}
		if(entityType == null) {
			return false;
		}

		int summonCount = this.summonCountMin;
		if(this.summonCountMax > this.summonCountMin) {
			summonCount = this.summonCountMin + attacker.getRandom().nextInt(this.summonCountMax - this.summonCountMin);
		}
		int summonedCreatures = 0;
		for(int i = 0; i < summonCount; i++) {
			if (attacker.getRandom().nextDouble() <= this.summonChance) {
				try {
					Entity entity = entityType.create(attacker.getCommandSenderWorld());
					entity.moveTo(attacker.blockPosition().getX(), attacker.blockPosition().getY(), attacker.blockPosition().getZ(), attacker.yRot, 0.0F);
					if (entity instanceof BaseCreatureEntity) {
						BaseCreatureEntity entityCreature = (BaseCreatureEntity) entity;
						entityCreature.setMinion(true);
						entityCreature.setTemporary(this.summonDuration * 20);
						entityCreature.setSizeScale(this.sizeScale);

						if (attacker instanceof PlayerEntity && entityCreature instanceof TameableCreatureEntity) {
							TameableCreatureEntity entityTameable = (TameableCreatureEntity) entityCreature;
							entityTameable.setPlayerOwner((PlayerEntity) attacker);
							entityTameable.setSitting(false);
							entityTameable.setFollowing(true);
							entityTameable.setPassive(false);
							entityTameable.setAssist(true);
							entityTameable.setAggressive(true);
							entityTameable.setPVP(target instanceof PlayerEntity);
						}

						float randomAngle = 45F + (45F * attacker.getRandom().nextFloat());
						if (attacker.getRandom().nextBoolean()) {
							randomAngle = -randomAngle;
						}
						BlockPos spawnPos = entityCreature.getFacingPosition(attacker, -1, randomAngle);
						entity.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), attacker.yRot, 0.0F);
						entityCreature.setTarget(target);
						entity.getCommandSenderWorld().addFreshEntity(entity);
						summonedCreatures++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return summonedCreatures > 0;
	}
}

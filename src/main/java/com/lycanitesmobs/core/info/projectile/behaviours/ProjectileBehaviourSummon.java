package com.lycanitesmobs.core.info.projectile.behaviours;

import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.network.MessageScreenRequest;
import com.lycanitesmobs.core.pets.SummonSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProjectileBehaviourSummon extends ProjectileBehaviour {
	/** The id of the mob to summon. **/
	public String summonMobId;

	/** If true, the player selected minion is summoned instead of a direct entity from mobId. **/
	public boolean summonMinion = false;

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
		if(json.has("summonMobId"))
			this.summonMobId = json.get("summonMobId").getAsString();

		if(json.has("summonMinion"))
			this.summonMinion = json.get("summonMinion").getAsBoolean();

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
	public void onProjectileImpact(BaseProjectileEntity projectile, World world, BlockPos pos) {
		if(projectile == null || projectile.getCommandSenderWorld().isClientSide) {
			return;
		}
		if (!CreatureManager.getInstance().config.isSummoningAllowed(world)) {
			return;
		}
		EntityType entityType = null;

		// Summon Minion:
		SummonSet summonSet = null;
		if(this.summonMinion) {
			if(!(projectile.getOwner() instanceof PlayerEntity)) {
				return;
			}
			PlayerEntity player = (PlayerEntity)projectile.getOwner();
			ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
			if(extendedPlayer == null) {
				return;
			}
			summonSet = extendedPlayer.getSelectedSummonSet();
			if(summonSet == null || summonSet.getCreatureType() == null) {
				if (player instanceof ServerPlayerEntity) {
					MessageScreenRequest messageScreenRequest = new MessageScreenRequest(MessageScreenRequest.GuiRequest.SUMMONING);
					LycanitesMobs.packetHandler.sendToPlayer(messageScreenRequest, (ServerPlayerEntity) player);
				}
				return;
			}
			entityType = summonSet.getCreatureType();
		}

		// Summon From ID:
		if(entityType == null && this.summonMobId != null) {
			CreatureInfo creatureInfo = CreatureManager.getInstance().getCreatureFromId(this.summonMobId);
			if (creatureInfo != null) {
				entityType = creatureInfo.getEntityType();
			}
			else {
				Object entityTypeObj = GameRegistry.findRegistry(EntityType.class).getValue(new ResourceLocation(this.summonMobId));
				if (entityTypeObj instanceof EntityType) {
					entityType = (EntityType) entityTypeObj;
				}
			}
		}
		if (entityType == null) {
			return;
		}

		int summonCount = this.summonCountMin;
		if(this.summonCountMax > this.summonCountMin) {
			summonCount = this.summonCountMin + projectile.getCommandSenderWorld().random.nextInt(this.summonCountMax - this.summonCountMin);
		}

		for(int i = 0; i < summonCount; i++) {
			if (projectile.getCommandSenderWorld().random.nextDouble() <= this.summonChance) {
				try {
					Entity entity = entityType.create(projectile.getCommandSenderWorld());
					entity.moveTo(projectile.blockPosition().getX(), projectile.blockPosition().getY(), projectile.blockPosition().getZ(), projectile.yRot, 0.0F);
					if (entity instanceof BaseCreatureEntity) {
						BaseCreatureEntity entityCreature = (BaseCreatureEntity) entity;
						entityCreature.setMinion(true);
						entityCreature.setTemporary(this.summonDuration);
						entityCreature.setSizeScale(this.sizeScale);

						if (projectile.getOwner() instanceof PlayerEntity && entityCreature instanceof TameableCreatureEntity) {
							TameableCreatureEntity entityTameable = (TameableCreatureEntity) entityCreature;
							entityTameable.setPlayerOwner((PlayerEntity) projectile.getOwner());
							entityTameable.setSitting(false);
							entityTameable.setFollowing(true);
							entityTameable.setPassive(false);
							entityTameable.setAssist(true);
							entityTameable.setAggressive(true);
							if(summonSet != null) {
								summonSet.applyBehaviour(entityTameable);
								entityTameable.setSubspecies(summonSet.subspecies);
								entityTameable.applyVariant(summonSet.variant);
							}
						}

						float randomAngle = 45F + (45F * projectile.getCommandSenderWorld().random.nextFloat());
						if (projectile.getCommandSenderWorld().random.nextBoolean()) {
							randomAngle = -randomAngle;
						}
						BlockPos spawnPos = entityCreature.getFacingPosition(projectile, -1, randomAngle);
						entity.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), projectile.yRot, 0.0F);
						entity.getCommandSenderWorld().addFreshEntity(entity);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}

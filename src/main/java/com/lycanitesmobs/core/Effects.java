package com.lycanitesmobs.core;

import com.google.common.base.Predicate;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.api.IGroupBoss;
import com.lycanitesmobs.core.config.ConfigExtra;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.FearEntity;
import com.lycanitesmobs.core.info.CreatureGroup;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.network.MessageEntityVelocity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.UUID;

public class Effects {
    private static final UUID swiftswimmingMoveBoostUUID = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5e");
    private static final AttributeModifier swiftswimmingMoveBoost = (new AttributeModifier(swiftswimmingMoveBoostUUID, "Swiftswimming Speed Boost", 1D, AttributeModifier.Operation.ADDITION));
	private static final UUID swiftswimmingMoveBoostUUID2 = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5d");
	private static final AttributeModifier swiftswimmingMoveBoost2 = (new AttributeModifier(swiftswimmingMoveBoostUUID2, "Swiftswimming Speed Boost 2", 2D, AttributeModifier.Operation.ADDITION));
	private static final UUID swiftswimmingMoveBoostUUID3 = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5c");
	private static final AttributeModifier swiftswimmingMoveBoost3 = (new AttributeModifier(swiftswimmingMoveBoostUUID3, "Swiftswimming Speed Boost 3", 3D, AttributeModifier.Operation.ADDITION));
	private static final UUID swiftswimmingMoveBoostUUID4 = UUID.fromString("6d4fe17f-06eb-4ebc-a573-364b79faed5b");
	private static final AttributeModifier swiftswimmingMoveBoost4 = (new AttributeModifier(swiftswimmingMoveBoostUUID4, "Swiftswimming Speed Boost 4", 4D, AttributeModifier.Operation.ADDITION));

	// Global Settings:
	public boolean disableNausea = false;

	// ==================================================
	//                    Initialize
	// ==================================================
	public Effects() {
		ObjectManager.addPotionEffect("paralysis", true, 0xFFFF00, false);
		ObjectManager.addPotionEffect("penetration", true, 0x222222, false);
		ObjectManager.addPotionEffect("recklessness", true, 0xFF0044, false); // TODO Implement
		ObjectManager.addPotionEffect("rage", true, 0xFF4400, false); // TODO Implement
		ObjectManager.addPotionEffect("weight", true, 0x000022, false);
		ObjectManager.addPotionEffect("fear", true, 0x220022, false);
		ObjectManager.addPotionEffect("decay", true, 0x110033, false);
		ObjectManager.addPotionEffect("insomnia", true, 0x002222, false);
		ObjectManager.addPotionEffect("instability", true, 0x004422, false);
		ObjectManager.addPotionEffect("lifeleak", true, 0x0055FF, false);
		ObjectManager.addPotionEffect("bleed", true, 0xFF2222, false);
		ObjectManager.addPotionEffect("plague", true, 0x220066, false);
		ObjectManager.addPotionEffect("aphagia", true, 0xFFDDDD, false);
		ObjectManager.addPotionEffect("smited", true, 0xDDDDFF, false);
		ObjectManager.addPotionEffect("smouldering", true, 0xDD0000, false);

		ObjectManager.addPotionEffect("leech", false, 0x00FF99, true);
		ObjectManager.addPotionEffect("swiftswimming", false, 0x0000FF, true);
		ObjectManager.addPotionEffect("fallresist", false, 0xDDFFFF, true);
		ObjectManager.addPotionEffect("rejuvenation", false, 0x99FFBB, true);
		ObjectManager.addPotionEffect("immunization", false, 0x66FFBB, true);
		ObjectManager.addPotionEffect("cleansed", false, 0x66BBFF, true);
		ObjectManager.addPotionEffect("repulsion", false, 0xBC532E, true);
		ObjectManager.addPotionEffect("heataura", false, 0x996600, true); // TODO Implement
		ObjectManager.addPotionEffect("staticaura", false, 0xFFBB551, true); // TODO Implement
		ObjectManager.addPotionEffect("freezeaura", false, 0x55BBFF, true); // TODO Implement
		ObjectManager.addPotionEffect("envenom", false, 0x44DD66, true); // TODO Implement

		// Event Listener:
		MinecraftForge.EVENT_BUS.register(this);

		// Effect Sounds:
		ObjectManager.addSound("effect_fear", LycanitesMobs.modInfo, "effect.fear");
	}


    // ==================================================
	//                   Entity Update
	// ==================================================
	@SubscribeEvent
	public void onEntityUpdate(LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if(entity == null) {
			return;
		}

		// Null Effect Fix:
		for(Object potionEffectObj : entity.getActiveEffects()) {
			if(potionEffectObj == null) {
				entity.removeAllEffects();
				LycanitesMobs.logWarning("EffectsSetup", "Found a null potion effect on entity: " + entity + " all effects have been removed from this entity.");
			}
		}
		
		// Night Vision Stops Blindness:
		if(entity.hasEffect(net.minecraft.potion.Effects.BLINDNESS) && entity.hasEffect(net.minecraft.potion.Effects.NIGHT_VISION)) {
			entity.removeEffect(net.minecraft.potion.Effects.BLINDNESS);
		}


		// Disable Nausea:
		this.disableNausea = ConfigExtra.INSTANCE.disableNausea.get();
		if(this.disableNausea && event.getEntityLiving() instanceof PlayerEntity) {
			if(entity.hasEffect(net.minecraft.potion.Effects.CONFUSION)) {
				entity.removeEffect(net.minecraft.potion.Effects.CONFUSION);
			}
		}

		// Immunity:
		boolean invulnerable = false;
		if(entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity)entity;
			invulnerable = player.isCreative() || player.isSpectator();
		}


		// ========== Debuffs ==========
		// Paralysis
		EffectBase paralysis = ObjectManager.getEffect("paralysis");
		if(paralysis != null) {
			if(!invulnerable && entity.hasEffect(paralysis)) {
				entity.setDeltaMovement(0, entity.getDeltaMovement().y() > 0 ? 0 : entity.getDeltaMovement().y(), 0);
				entity.setOnGround(false);
			}
		}
		
		// Weight
		EffectBase weight = ObjectManager.getEffect("weight");
		if(weight != null) {
			if(!invulnerable && entity.hasEffect(weight) && !entity.hasEffect(net.minecraft.potion.Effects.DAMAGE_BOOST)) {
				if(entity.getDeltaMovement().y() > -0.2D)
					entity.setDeltaMovement(entity.getDeltaMovement().add(0, -0.2D, 0));
			}
		}
		
		// Fear
		EffectBase fear = ObjectManager.getEffect("fear");
		if(fear != null && !entity.getCommandSenderWorld().isClientSide) {
			if(!invulnerable && entity.hasEffect(fear)) {
				ExtendedEntity extendedEntity = ExtendedEntity.getForEntity(entity);
				if(extendedEntity != null) {
					if(extendedEntity.fearEntity == null) {
						FearEntity fearEntity = new FearEntity((EntityType<? extends FearEntity>) CreatureManager.getInstance().getEntityType("fear"), entity.getCommandSenderWorld(), entity);
						entity.getCommandSenderWorld().addFreshEntity(fearEntity);
						extendedEntity.fearEntity = fearEntity;
					}
				}
			}
		}

		// Instability
		EffectBase instability = ObjectManager.getEffect("instability");
		if(instability != null && !entity.getCommandSenderWorld().isClientSide && !(entity instanceof IGroupBoss)) {
			if(!invulnerable && entity.hasEffect(instability)) {
				if(entity.getCommandSenderWorld().random.nextDouble() <= 0.1) {
					double strength = 1 + entity.getEffect(instability).getAmplifier();
					double motionX = strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D);
					double motionY = strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D);
					double motionZ = strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D);
					entity.setDeltaMovement(entity.getDeltaMovement().add(motionX, motionY, motionZ));
					try {
						if (entity instanceof ServerPlayerEntity) {
							ServerPlayerEntity player = (ServerPlayerEntity) entity;
							player.connection.send(new SEntityVelocityPacket(entity));
							MessageEntityVelocity messageEntityVelocity = new MessageEntityVelocity(
									player,
									strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D),
									strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D),
									strength * (entity.getCommandSenderWorld().random.nextDouble() - 0.5D)
							);
							LycanitesMobs.packetHandler.sendToPlayer(messageEntityVelocity, player);
						}
					}
					catch(Exception e) {
						LycanitesMobs.logWarning("", "Failed to create and send a network packet for instability velocity!");
						e.printStackTrace();
					}
				}
			}
		}

		// Plague
		EffectBase plague = ObjectManager.getEffect("plague");
		if(plague != null && !entity.getCommandSenderWorld().isClientSide) {
			if(!invulnerable && entity.hasEffect(plague)) {

				// Poison:
				int poisonAmplifier = entity.getEffect(plague).getAmplifier();
				int poisonDuration = entity.getEffect(plague).getDuration();
				if(entity.hasEffect(net.minecraft.potion.Effects.POISON)) {
					poisonAmplifier = Math.max(poisonAmplifier, entity.getEffect(net.minecraft.potion.Effects.POISON).getAmplifier());
					poisonDuration = Math.max(poisonDuration, entity.getEffect(net.minecraft.potion.Effects.POISON).getDuration());
				}
				entity.addEffect(new EffectInstance(net.minecraft.potion.Effects.POISON, poisonDuration, poisonAmplifier));

				// Spread:
				if(entity.getCommandSenderWorld().getGameTime() % 20 == 0) {
					List aoeTargets = this.getNearbyEntities(entity, LivingEntity.class, null, 2);
					for(Object entityObj : aoeTargets) {
						LivingEntity target = (LivingEntity)entityObj;
						if(target != entity && !entity.isAlliedTo(target)) {
							if(target instanceof PlayerEntity && !entity.canSee(target)) {
								continue;
							}
							int amplifier = entity.getEffect(plague).getAmplifier();
							int duration = entity.getEffect(plague).getDuration();
							if(amplifier > 0) {
								target.addEffect(new EffectInstance(plague, duration, amplifier - 1));
							}
							else {
								target.addEffect(new EffectInstance(net.minecraft.potion.Effects.POISON, duration, amplifier));
							}
						}
					}
				}
			}
		}

		// Smited
		EffectBase smited = ObjectManager.getEffect("smited");
		if(smited != null && !entity.getCommandSenderWorld().isClientSide) {
			if(!invulnerable && entity.hasEffect(smited) && entity.getCommandSenderWorld().getGameTime() % 20 == 0) {
				float brightness = entity.getBrightness();
				if(brightness > 0.5F && entity.getCommandSenderWorld().canSeeSkyFromBelowWater(entity.blockPosition())) {
					entity.setSecondsOnFire(4);
				}
			}
		}

		// Bleed
		EffectBase bleed = ObjectManager.getEffect("bleed");
		if(bleed != null && !entity.getCommandSenderWorld().isClientSide) {
			if(!invulnerable && entity.hasEffect(bleed) && entity.getCommandSenderWorld().getGameTime() % 20 == 0 && entity.getVehicle() == null) {
				if(entity.walkDistO != entity.walkDist) {
					entity.hurt(DamageSource.MAGIC, entity.getEffect(bleed).getAmplifier() + 1);
				}
			}
		}

		// Smouldering
		EffectBase smouldering = ObjectManager.getEffect("smouldering");
		if(smouldering != null && !entity.getCommandSenderWorld().isClientSide) {
			if(!invulnerable && entity.hasEffect(smouldering) && entity.getCommandSenderWorld().getGameTime() % 20 == 0) {
				entity.setSecondsOnFire(4 + (4 * entity.getEffect(smouldering).getAmplifier()));
			}
		}


		// ========== Buffs ==========
		// Swiftswimming
		EffectBase swiftswimming = ObjectManager.getEffect("swiftswimming");
		if(swiftswimming != null && entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity)entity;
			ModifiableAttributeInstance movement = entity.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get());
			int amplifier = -1;
			if(entity.hasEffect(swiftswimming)) {
				amplifier = entity.getEffect(swiftswimming).getAmplifier();
			}
			if(amplifier == 0 && movement.getModifier(swiftswimmingMoveBoostUUID) == null) {
				movement.addPermanentModifier(swiftswimmingMoveBoost);
			}
			else if(amplifier != 0 && movement.getModifier(swiftswimmingMoveBoostUUID) != null) {
				movement.removeModifier(swiftswimmingMoveBoost);
			}
			if(amplifier == 1 && movement.getModifier(swiftswimmingMoveBoostUUID2) == null) {
				movement.addPermanentModifier(swiftswimmingMoveBoost2);
			}
			else if(amplifier != 1 && movement.getModifier(swiftswimmingMoveBoostUUID2) != null) {
				movement.removeModifier(swiftswimmingMoveBoost2);
			}
			if(amplifier == 2 && movement.getModifier(swiftswimmingMoveBoostUUID3) == null) {
				movement.addPermanentModifier(swiftswimmingMoveBoost3);
			}
			else if(amplifier != 2 && movement.getModifier(swiftswimmingMoveBoostUUID3) != null) {
				movement.removeModifier(swiftswimmingMoveBoost3);
			}
			if(amplifier >= 3 && movement.getModifier(swiftswimmingMoveBoostUUID4) == null) {
				movement.addPermanentModifier(swiftswimmingMoveBoost4);
			}
			else if(amplifier < 3 && movement.getModifier(swiftswimmingMoveBoostUUID4) != null) {
				movement.removeModifier(swiftswimmingMoveBoost4);
			}
		}

		// Immunisation
		EffectBase immunization = ObjectManager.getEffect("immunization");
		if(immunization != null && !entity.getCommandSenderWorld().isClientSide) {
			if(entity.hasEffect(ObjectManager.getEffect("immunization"))) {
				if(entity.hasEffect(net.minecraft.potion.Effects.POISON)) {
					entity.removeEffect(net.minecraft.potion.Effects.POISON);
				}
				if(entity.hasEffect(net.minecraft.potion.Effects.HUNGER)) {
					entity.removeEffect(net.minecraft.potion.Effects.HUNGER);
				}
				if(entity.hasEffect(net.minecraft.potion.Effects.WEAKNESS)) {
					entity.removeEffect(net.minecraft.potion.Effects.WEAKNESS);
				}
				if(entity.hasEffect(net.minecraft.potion.Effects.CONFUSION)) {
					entity.removeEffect(net.minecraft.potion.Effects.CONFUSION);
				}
				if(ObjectManager.getEffect("paralysis") != null) {
					if(entity.hasEffect(ObjectManager.getEffect("paralysis"))) {
						entity.removeEffect(ObjectManager.getEffect("paralysis"));
					}
				}
			}
		}

		// Cleansed
		EffectBase cleansed = ObjectManager.getEffect("cleansed");
		if(ObjectManager.getEffect("cleansed") != null && !entity.getCommandSenderWorld().isClientSide) {
			if(entity.hasEffect(ObjectManager.getEffect("cleansed"))) {
				if(entity.hasEffect(net.minecraft.potion.Effects.WITHER)) {
					entity.removeEffect(net.minecraft.potion.Effects.WITHER);
				}
				if(entity.hasEffect(net.minecraft.potion.Effects.UNLUCK)) {
					entity.removeEffect(net.minecraft.potion.Effects.UNLUCK);
				}
				if(ObjectManager.getEffect("fear") != null) {
					if(entity.hasEffect(ObjectManager.getEffect("fear"))) {
						entity.removeEffect(ObjectManager.getEffect("fear"));
					}
				}
				if(ObjectManager.getEffect("insomnia") != null) {
					if(entity.hasEffect(ObjectManager.getEffect("insomnia"))) {
						entity.removeEffect(ObjectManager.getEffect("insomnia"));
					}
				}
			}
		}
	}
	
	
	// ==================================================
	//                    Entity Jump
	// ==================================================
	@SubscribeEvent
	public void onEntityJump(LivingJumpEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if(entity == null)
			return;

		boolean invulnerable = false;
		if(entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity)entity;
			invulnerable = player.abilities.invulnerable;
		}
		if(invulnerable) {
			return;
		}
			
		// Anti-Jumping:
		EffectBase paralysis = ObjectManager.getEffect("paralysis");
		if(paralysis != null) {
			if(entity.hasEffect(paralysis)) {
				if(event.isCancelable()) event.setCanceled(true);
			}
		}

		EffectBase weight = ObjectManager.getEffect("weight");
		if(weight != null) {
			if(entity.hasEffect(weight)) {
				if(event.isCancelable()) event.setCanceled(true);
			}
		}
	}


	// ==================================================
	//               Living Attack Event
	// ==================================================
	@SubscribeEvent
	public void onLivingAttack(LivingAttackEvent event) {
		if(event.isCancelable() && event.isCanceled())
			return;

		if(event.getEntityLiving() == null)
			return;

		LivingEntity target = event.getEntityLiving();
		LivingEntity attacker = null;
		if(event.getSource().getEntity() != null && event.getSource().getEntity() instanceof LivingEntity) {
			attacker = (LivingEntity) event.getSource().getEntity();
		}
		if(attacker == null) {
			return;
		}

		// ========== Debuffs ==========
		// Lifeleak
		EffectBase lifeleak = ObjectManager.getEffect("lifeleak");
		if(lifeleak != null && !event.getEntityLiving().getCommandSenderWorld().isClientSide) {
			if(attacker.hasEffect(lifeleak)) {
				if (event.isCancelable()) {
					event.setCanceled(true);
				}
				target.heal(event.getAmount());
			}
		}
	}


    // ==================================================
    //                 Living Hurt Event
    // ==================================================
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if(event.isCancelable() && event.isCanceled())
            return;

        if(event.getEntityLiving() == null)
            return;
		LivingEntity target = event.getEntityLiving();
		Entity attacker = event.getSource().getEntity();


		// ========== Debuffs ==========
        // Fall Resistance
		EffectBase fallresist = ObjectManager.getEffect("fallresist");
		if(fallresist != null) {
            if(event.getEntityLiving().hasEffect(fallresist)) {
                if("fall".equals(event.getSource().msgId)) {
                    event.setAmount(0);
                    event.setCanceled(true);
                }
            }
        }

		// Penetration
		EffectBase penetration = ObjectManager.getEffect("penetration");
		if(penetration != null) {
			if(event.getEntityLiving().hasEffect(penetration)) {
				float damage = event.getAmount();
				float multiplier = 0.25F * (event.getEntityLiving().getEffect(penetration).getAmplifier() + 1);
				event.setAmount(damage + (damage * multiplier));
			}
		}

		// Fear
		EffectBase fear = ObjectManager.getEffect("fear");
		if(fear != null) {
			if(event.getEntityLiving().hasEffect(fear)) {
				if("inWall".equals(event.getSource().msgId)) {
					event.setAmount(0);
					event.setCanceled(true);
				}
			}
		}


        // ========== Buffs ==========
        // Leeching
		EffectBase leech = ObjectManager.getEffect("leech");
		if(leech != null && event.getSource().getEntity() != null) {
			LivingEntity leechingEntity = null;
			if(event.getSource().getDirectEntity() instanceof LivingEntity) {
				leechingEntity = (LivingEntity)event.getSource().getDirectEntity();
			}
			else if(event.getSource().getEntity() instanceof LivingEntity) {
				leechingEntity = (LivingEntity)event.getSource().getEntity();
			}
			if(leechingEntity != null) {
				if(leechingEntity.hasEffect(leech)) {
					int leeching = leechingEntity.getEffect(leech).getAmplifier() + 1;
					leechingEntity.heal(Math.max(leeching, 1));
				}
			}
        }

		// Repulsion
		EffectBase repulsion = ObjectManager.getEffect("repulsion");
		if(repulsion != null) {
			boolean attackerIsBoss = attacker instanceof IGroupBoss;
			if(!attackerIsBoss && CreatureManager.getInstance().getCreatureGroup("boss") != null) {
				attackerIsBoss = CreatureManager.getInstance().getCreatureGroup("boss").hasEntity(attacker);
			}
			if(attacker != null && !attackerIsBoss && target.hasEffect(repulsion)) {
				double knockback = target.getEffect(repulsion).getAmplifier() + 2;
				double xDist = attacker.position().x() - target.position().x();
				double zDist = attacker.position().z() - target.position().z();
				double xzDist = Math.max(MathHelper.sqrt(xDist * xDist + zDist * zDist), 0.01D);
				double motionCap = 10;
				double xVel = xDist / xzDist * knockback;
				double zVel = zDist / xzDist * knockback;
				if (attacker.getDeltaMovement().x() < motionCap && attacker.getDeltaMovement().x() > -motionCap && attacker.getDeltaMovement().z() < motionCap && attacker.getDeltaMovement().z() > -motionCap) {
					attacker.push(xVel, 0, zVel);
				}
			}
		}
    }


	// ==================================================
	//                    Entity Heal
	// ==================================================
	@SubscribeEvent
	public void onEntityHeal(LivingHealEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if(entity == null)
			return;

		// Rejuvenation:
		EffectBase rejuvenation = ObjectManager.getEffect("rejuvenation");
		if(rejuvenation != null) {
			if(entity.hasEffect(rejuvenation)) {
				event.setAmount((float)Math.ceil(event.getAmount() * (2 * (1 + entity.getEffect(rejuvenation).getAmplifier()))));
			}
		}

		// Decay:
		EffectBase decay = ObjectManager.getEffect("decay");
		if(decay != null) {
			if(entity.hasEffect(decay)) {
				event.setAmount((float)Math.floor(event.getAmount() / (2 * (1 + entity.getEffect(decay).getAmplifier()))));
			}
		}
	}


	// ==================================================
	//                Player Use Bed Event
	// ==================================================
	/** This uses the player sleep in bed event to spawn mobs. **/
	@SubscribeEvent
	public void onSleep(PlayerSleepInBedEvent event) {
		PlayerEntity player = event.getPlayer();
		if(player == null || player.getCommandSenderWorld().isClientSide || event.isCanceled())
			return;

		// Insomnia:
		EffectBase insomnia = ObjectManager.getEffect("insomnia");
		if(insomnia != null && player.hasEffect(insomnia)) {
			event.setResult(PlayerEntity.SleepResult.NOT_SAFE);
		}
	}


	// ==================================================
	//               Item Use Event
	// ==================================================
	@SubscribeEvent
	public void onLivingUseItem(LivingEntityUseItemEvent event) {
		if(event.isCancelable() && event.isCanceled())
			return;

		if(event.getEntityLiving() == null)
			return;

		// ========== Debuffs ==========
		// Aphagia
		EffectBase aphagia = ObjectManager.getEffect("aphagia");
		if(aphagia != null && !event.getEntityLiving().getCommandSenderWorld().isClientSide) {
			if(event.getEntityLiving().hasEffect(aphagia)) {
				if(event.isCancelable()) {
					event.setCanceled(true);
				}
			}
		}
	}


	// ==================================================
	//                     Utility
	// ==================================================
	/** Get entities that are near the provided entity. **/
	public <T extends Entity> List<T> getNearbyEntities(Entity searchEntity, Class <? extends T > clazz, final Class filterClass, double range) {
		return searchEntity.getCommandSenderWorld().getEntitiesOfClass(clazz, searchEntity.getBoundingBox().inflate(range, range, range), (Predicate<Entity>) entity -> {
			if(filterClass == null)
				return true;
			return filterClass.isAssignableFrom(entity.getClass());
		});
	}

	/**
	 * Determines if the provided entity is considered a boss.
	 * @param entity The entity to check.
	 * @return True if the entity is a boss.
	 */
	public boolean isBoss(Entity entity) {
		if (entity instanceof EnderDragonEntity || entity instanceof WitherEntity) {
			return true;
		}
		if (entity instanceof BaseCreatureEntity) {
			BaseCreatureEntity creature = (BaseCreatureEntity)entity;
			return creature.isBoss();
		}
		CreatureGroup bossGroup = CreatureManager.getInstance().getCreatureGroup("boss");
		if (bossGroup != null) {
			return bossGroup.hasEntity(entity);
		}
		return false;
	}
}

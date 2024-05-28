package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.List;

public class EntityNymph extends TameableCreatureEntity {

	public int healingRate = 20;

    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityNymph(EntityType<? extends EntityNymph> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = false;

		this.fleeHealthPercent = 1.0F;
		this.isAggressiveByDefault = false;
        this.setupMob();

        this.maxUpStep = 1.0F;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

	@Override
	public void loadCreatureFlags() {
		this.healingRate = this.creatureInfo.getFlag("healingRate", this.healingRate);
	}


    // ==================================================
    //                      Updates
    // ==================================================
	private int farmingTick = 0;
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

		if(!this.getCommandSenderWorld().isClientSide) {
			// Healing Aura:
			if(this.healingRate > 0 && !this.isPetType("familiar")) {
				if (this.updateTick % this.healingRate == 0) {
					List aoeTargets = this.getNearbyEntities(LivingEntity.class, null, 4);
					for (Object entityObj : aoeTargets) {
						LivingEntity target = (LivingEntity) entityObj;
						if (target != this && !(target instanceof EntityNymph) && target != this.getTarget() && target != this.getAvoidTarget()) {
							target.addEffect(new EffectInstance(Effects.REGENERATION, 3 * 20, 0));
						}
					}
				}
			}

			// Ranged Attack:
			if(this.hasAvoidTarget()) {
				if(this.updateTick % this.getRangedCooldown() == 0) {
					this.attackRanged(this.getAvoidTarget(), this.distanceTo(this.getAvoidTarget()));
				}
			}
			else if(this.hasAttackTarget()) {
				if(this.updateTick % this.getRangedCooldown() == 0) {
					this.attackRanged(this.getTarget(), this.distanceTo(this.getTarget()));
				}
			}
		}

        /*/ Particles:
        if(this.getEntityWorld().isRemote)
            for(int i = 0; i < 1; ++i) {
                this.getEntityWorld().addParticle(ParticleTypes.BLOCK_CRACK,
                        this.getPositionVec().getX() + (this.rand.nextDouble() - 0.5D) * (double) this.getSize(Pose.STANDING).width,
                        this.getPositionVec().getY() + this.rand.nextDouble() * (double) this.getSize(Pose.STANDING).height,
                        this.getPositionVec().getZ() + (this.rand.nextDouble() - 0.5D) * (double) this.getSize(Pose.STANDING).width,
                        0.0D, 0.0D, 0.0D,
                        Blocks.RED_FLOWER.getStateId(Blocks.RED_FLOWER.getStateFromMeta(2)));
				this.getEntityWorld().addParticle(ParticleTypes.BLOCK_CRACK,
						this.getPositionVec().getX() + (this.rand.nextDouble() - 0.5D) * (double) this.getSize(Pose.STANDING).width,
						this.getPositionVec().getY() + this.rand.nextDouble() * (double) this.getSize(Pose.STANDING).height,
						this.getPositionVec().getZ() + (this.rand.nextDouble() - 0.5D) * (double) this.getSize(Pose.STANDING).width,
						0.0D, 0.0D, 0.0D,
						Blocks.RED_FLOWER.getStateId(Blocks.RED_FLOWER.getStateFromMeta(8)));
            }*/
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
		this.fireProjectile("faebolt", target, range, 0, new Vector3d(0, 0, 0), 0.75f, 1f, 1F);
		super.attackRanged(target, range);
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
    public boolean isFlying() { return true; }


    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() { return true; }


	// ==================================================
	//                     Equipment
	// ==================================================
	@Override
	public int getNoBagSize() { return 0; }
	@Override
	public int getBagSize() { return this.creatureInfo.bagSize; }
}

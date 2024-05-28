package com.lycanitesmobs.core.entity;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.inventory.CreatureInventory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FearEntity extends BaseCreatureEntity {
    public LivingEntity fearedEntity;
	
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public FearEntity(EntityType<? extends FearEntity> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.hasStepSound = false;
        this.hasAttackSound = false;
        this.spreadFire = false;
        //this.setSize(0.8f, 1.8f);

        this.setupMob();
    }

    public FearEntity(EntityType<? extends FearEntity> entityType, World world, LivingEntity feared) {
        this(entityType, world);
        this.setFearedEntity(feared);
    }

    // ========== Setup ==========
    /** This should be called by the specific mob entity and set the default starting values. **/
    @Override
    public void setupMob() {
        // Size:
        //Set by feared entity instead.

        // Stats:
        this.xpReward = 0;
        this.inventory = new CreatureInventory(this.getName().toString(), this);
    }
	
	// ========== Default Drops ==========
	@Override
	public void loadItemDrops() {}
	
	
	// ==================================================
  	//                     Spawning
  	// ==================================================
    // ========== Despawning ==========
    @Override
    protected boolean canDespawnNaturally() {
    	return false;
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();
        
        // Server Side Only:
        if(this.getCommandSenderWorld().isClientSide) {
			return;
		}
        
        // Clean Up:
        if(this.fearedEntity == null || !this.fearedEntity.isAlive() || !(this.fearedEntity instanceof LivingEntity)) {
        	this.remove();
        	return;
        }

        LivingEntity fearedLivingEntity = (LivingEntity)this.fearedEntity;
        
        // Pickup Entity For Fear Movement Override:
        if(this.canPickupEntity(this.fearedEntity)) {
        	this.pickupEntity(this.fearedEntity);
        }
        
        // Set Rotation:
        if(this.hasPickupEntity() && !(this.getPickupEntity() instanceof PlayerEntity)) {
        	this.getPickupEntity().yRot = this.yRot;
        	this.getPickupEntity().xRot = this.xRot;
        }
        
        // Follow Fear Target If Not Picked Up:
        if(this.getPickupEntity() == null) {
        	this.setPos(this.fearedEntity.position().x(), this.fearedEntity.position().y(), this.fearedEntity.position().z());
			this.setDeltaMovement(this.fearedEntity.getDeltaMovement());
			this.fallDistance = 0;
        }

        // Remove When Fear is Over:
        if(ObjectManager.getEffect("fear") == null || !fearedEntity.hasEffect(ObjectManager.getEffect("fear"))) {
            this.remove();
            return;
        }

        // Copy Movement Debuffs:
		if(this.fearedEntity != null) {
			if (fearedEntity.hasEffect(Effects.LEVITATION)) {
				EffectInstance activeDebuff = fearedEntity.getEffect(Effects.LEVITATION);
				this.addEffect(new EffectInstance(Effects.LEVITATION, activeDebuff.getDuration(), activeDebuff.getAmplifier()));
			}

			Effect instability = ObjectManager.getEffect("instability");
			if (instability != null && fearedEntity.hasEffect(instability)) {
				EffectInstance activeDebuff = fearedEntity.getEffect(instability);
				this.addEffect(new EffectInstance(instability, activeDebuff.getDuration(), activeDebuff.getAmplifier()));
			}
		}
    }

    @Override
	public boolean rollWanderChance() {
		return true;
	}
    
    
    // ==================================================
  	//                        Fear
  	// ==================================================
    public void setFearedEntity(LivingEntity feared) {
    	this.fearedEntity = feared;
        //this.setSize(feared.width, feared.height); TODO Entity Type Size
        this.noPhysics = feared.noPhysics;
        this.maxUpStep = feared.maxUpStep;
		this.moveTo(feared.position().x(), feared.position().y(), feared.position().z(), feared.yRot, feared.xRot);
		
        if(!(feared instanceof PlayerEntity)) {
	        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(feared.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue());
        }
    }
	
    
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public boolean isVulnerableTo(String type, DamageSource source, float damage) {
    	return false;
    }
    
    @Override
    public boolean canBeAffected(EffectInstance effectInstance) {
        return false;
    }
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    @Override
    public boolean isFlying() {
    	if(this.pickupEntity != null) {
    		if(this.pickupEntity instanceof BaseCreatureEntity)
    			return ((BaseCreatureEntity)this.pickupEntity).isFlying();
    		if(this.pickupEntity instanceof FlyingEntity)
    			return true;
    		if(this.pickupEntity instanceof PlayerEntity)
    			return ((PlayerEntity)this.pickupEntity).abilities.instabuild;
    	}
    	return false;
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInvisibleTo(PlayerEntity player) {
        return true;
    }

    public boolean isPickable()
    {
        return false;
    }
    
    
    // ==================================================
   	//                       Sounds
   	// ==================================================
    // ========== Idle ==========
    /** Returns the sound to play when this creature is making a random ambient roar, grunt, etc. **/
    @Override
    protected SoundEvent getAmbientSound() { return ObjectManager.getSound("effect_fear"); }

    // ========== Hurt ==========
    /** Returns the sound to play when this creature is damaged. **/
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) { return ObjectManager.getSound("effect_fear"); }

    // ========== Death ==========
    /** Returns the sound to play when this creature dies. **/
    @Override
    protected SoundEvent getDeathSound() { return ObjectManager.getSound("effect_fear"); }
     
    // ========== Fly ==========
    /** Plays a flying sound, usually a wing flap, called randomly when flying. **/
    public void playFlySound() {
    	return;
    }
}

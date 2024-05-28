package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.StealthGoal;
import com.lycanitesmobs.core.info.ObjectLists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityGrue extends TameableCreatureEntity implements IMob {
    
	private int teleportTime = 60;
	
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityGrue(EntityType<? extends EntityGrue> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = true;
        this.spawnsInWater = true;
        this.setupMob();

        this.maxUpStep = 1.0F;
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextPriorityGoalIndex++, new StealthGoal(this).setStealthTime(20).setStealthAttack(true).setStealthMove(true));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(true));
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();
        
        // Random Target Teleporting:
        if(!this.getCommandSenderWorld().isClientSide && this.hasAttackTarget()) {
	        if(this.teleportTime-- <= 0) {
	        	this.teleportTime = 60 + this.getRandom().nextInt(40);
        		BlockPos teleportPosition = this.getFacingPosition(this.getTarget(), -this.getTarget().getDimensions(Pose.STANDING).width - 1D, 0);
        		if(this.canTeleportTo(teleportPosition)) {
					this.playJumpSound();
					this.setPos(teleportPosition.getX(), teleportPosition.getY(), teleportPosition.getZ());
				}
	        }
        }
        
        // Particles:
        if(this.getCommandSenderWorld().isClientSide)
	        for(int i = 0; i < 2; ++i) {
	            this.getCommandSenderWorld().addParticle(ParticleTypes.WITCH, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
	        }
    }

	/**
	 * Checks if this entity can teleport to the provided block position.
	 * @param pos The position to teleport to.
	 * @return True if it's safe to teleport.
	 */
	public boolean canTeleportTo(BlockPos pos) {
		for (int y = 0; y <= 1; y++) {
			BlockState blockState = this.getCommandSenderWorld().getBlockState(pos.offset(0, y, 0));
			if (blockState.canOcclude())
				return false;
		}
        return true;
    }
    
    
    // ==================================================
   	//                     Stealth
   	// ==================================================
    @Override
    public boolean canStealth() {
    	if(this.getCommandSenderWorld().isClientSide) return false;
    	return this.testLightLevel() <= 0;
    }
    
    @Override
    public void startStealth() {
    	if(this.getCommandSenderWorld().isClientSide) {
            IParticleData particle = ParticleTypes.WITCH;
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            for(int i = 0; i < 100; i++)
            	this.getCommandSenderWorld().addParticle(particle, this.position().x() + (double)(this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double)this.getDimensions(Pose.STANDING).width, this.position().y() + 0.5D + (double)(this.random.nextFloat() * this.getDimensions(Pose.STANDING).height), this.position().z() + (double)(this.random.nextFloat() * this.getDimensions(Pose.STANDING).width * 2.0F) - (double)this.getDimensions(Pose.STANDING).width, d0, d1, d2);
        }
    	super.startStealth();
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Melee Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
    	if(!super.attackMelee(target, damageScale))
    		return false;
    	
    	// Leech:
    	if(this.isRareVariant() && target instanceof LivingEntity) {
    		LivingEntity targetLiving = (LivingEntity)target;
    		List<Effect> goodEffects = new ArrayList<>();
    		for(EffectInstance effectInstance : targetLiving.getActiveEffects()) {
				if(ObjectLists.inEffectList("buffs", effectInstance.getEffect()))
					goodEffects.add(effectInstance.getEffect());
    		}
    		if(goodEffects.size() > 0) {
    			if(goodEffects.size() > 1)
    				targetLiving.removeEffect(goodEffects.get(this.getRandom().nextInt(goodEffects.size())));
    			else
    				targetLiving.removeEffect(goodEffects.get(0));
				float leeching = Math.max(1, this.getAttackDamage(damageScale) / 2);
		    	this.heal(leeching);
    		}
    	}
        
        return true;
    }
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    @Override
    public boolean isFlying() { return true; }

    @Override
    public boolean isStrongSwimmer() { return true; }
    
    
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
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public boolean isVulnerableTo(String type, DamageSource source, float damage) {
        if(type.equals("inWall")) return false;
        return super.isVulnerableTo(type, source, damage);
    }
    
    /** Returns true if this mob should be damaged by the sun. **/
    public boolean daylightBurns() {
    	return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }


    // ==================================================
    //                       Visuals
    // ==================================================
    @Override
    public ResourceLocation getTexture(String suffix) {
        if(!this.hasCustomName() || !"Shadow Clown".equals(this.getCustomName().getString()))
            return super.getTexture(suffix);

        String textureName = this.getTextureName() + "_shadowclown";
		if(!"".equals(suffix)) {
			textureName += "_" + suffix;
		}
        if(TextureManager.getTexture(textureName) == null)
            TextureManager.addTexture(textureName, this.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
        return TextureManager.getTexture(textureName);
    }
}

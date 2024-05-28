package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityEpion extends RideableCreatureEntity implements IMob {
    
	public boolean griefing = true;
	
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityEpion(EntityType<? extends EntityEpion> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEAD;
        this.hasAttackSound = false;
        this.flySoundSpeed = 20;
        
        this.setupMob();

        this.maxUpStep = 1.0F;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75D).setRange(14.0F).setMinChaseDistance(6.0F));
    }

	@Override
	public void loadCreatureFlags() {
		this.griefing = this.creatureInfo.getFlag("griefing", this.griefing);
	}

	@Override
	public float getStrafeSpeed() {
		return 1F;
	}
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();
        
        // Sunlight Explosions:
        if(!this.getCommandSenderWorld().isClientSide && !this.isTamed() && !this.isMinion() && !this.isRareVariant()) {
        	if(!this.isFlying() && (this.onGround || this.isInWater()) && this.isAlive()) {
        		int explosionRadius = 2;
				if(this.subspecies != null)
					explosionRadius = 3;
				explosionRadius = Math.max(2, Math.round((float)explosionRadius * (float)this.sizeScale));
                if(this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.griefing)
	        	    this.getCommandSenderWorld().explode(this, this.position().x(), this.position().y(), this.position().z(), explosionRadius, Explosion.Mode.NONE);
				else
					this.getCommandSenderWorld().explode(this, this.position().x(), this.position().y(), this.position().z(), explosionRadius, Explosion.Mode.BREAK);
	        	this.remove();
        	}
        }
        
        // Particles:
        if(this.getCommandSenderWorld().isClientSide)
	        for(int i = 0; i < 2; ++i) {
	            this.getCommandSenderWorld().addParticle(ParticleTypes.WITCH, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
	        }
    }

	@Override
	public boolean canSee(Entity target) {
		if(this.isRareVariant()) {
			return true;
		}
		return super.canSee(target);
	}
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
	@Override
	public void attackRanged(Entity target, float range) {
		this.fireProjectile("bloodleech", target, range, 0, new Vector3d(0, 0, 0), 1.2f, 2f, 1F);
		super.attackRanged(target, range);
	}
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    @Override
    public boolean isFlying() {
    	if(this.getCommandSenderWorld().isClientSide) return true;
    	if(this.daylightBurns() && this.getCommandSenderWorld().isDay() && this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.griefing) {
    		float brightness = this.getBrightness();
        	if(brightness > 0.5F && this.getCommandSenderWorld().canSeeSkyFromBelowWater(this.blockPosition()))
        		return false;
    	}
        return true;
    }
    
    
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
    /** Returns true if this mob should be damaged by the sun. **/
    @Override
	public boolean daylightBurns() {
		return !this.isMinion() && !this.hasMaster() && !this.isTamed() && !this.isRareVariant();
	}
    
    @Override
    public float getFallResistance() { return 100; }


	// ==================================================
	//                   Mount Ability
	// ==================================================
	public void mountAbility(Entity rider) {
		if(this.getCommandSenderWorld().isClientSide)
			return;

		if(this.getStamina() < this.getStaminaCost())
			return;

		if(rider instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity)rider;
			ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("bloodleech");
			if(projectileInfo != null) {
				BaseProjectileEntity projectile = projectileInfo.createProjectile(this.getCommandSenderWorld(), player);
				this.getCommandSenderWorld().addFreshEntity(projectile);
				this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
				this.triggerAttackCooldown();
			}
		}

		this.applyStaminaCost();
	}

	public float getStaminaCost() {
		return 2;
	}

	public int getStaminaRecoveryWarmup() {
		return 0;
	}

	public float getStaminaRecoveryMax() {
		return 1.0F;
	}


	// ==================================================
	//                       Visuals
	// ==================================================
	/** Returns this creature's main texture. Also checks for for subspecies. **/
	public ResourceLocation getTexture() {
		if(!this.hasCustomName() || !"Vampire Bat".equals(this.getCustomName().getString()))
			return super.getTexture();

		String textureName = this.getTextureName() + "_vampirebat";
		if(TextureManager.getTexture(textureName) == null)
			TextureManager.addTexture(textureName, this.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
		return TextureManager.getTexture(textureName);
	}

	// ========== Rendering Distance ==========
	/** Returns a larger bounding box for rendering this large entity. **/
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getBoundingBoxForCulling() {
		return this.getBoundingBox().inflate(10, 10, 10).move(0, -5, 0);
	}
}

package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.info.ElementInfo;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class EntityReaper extends TameableCreatureEntity implements IMob {
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityReaper(EntityType<? extends EntityReaper> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEAD;
        this.hasAttackSound = false;
        this.setupMob();
        
        // No Block Collision:
        this.noPhysics = true;
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(0.75D).setRange(14.0F).setMinChaseDistance(0.75F).setCheckSight(false));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(true));

        this.targetSelector.addGoal(this.nextFindTargetIndex++, new FindAttackTargetGoal(this).addTargets(EntityType.PHANTOM));
    }

	@Override
    public void aiStep() {
        super.aiStep();
        
        // Particles:
        if(this.getCommandSenderWorld().isClientSide)
	        for(int i = 0; i < 2; ++i) {
	            this.getCommandSenderWorld().addParticle(ParticleTypes.WITCH, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
	        }
    }

    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("spectralbolt", target, range, 0, new Vector3d(0, 0, 0), 1.2f, 2f, 1F);
        super.attackRanged(target, range);
    }

    @Override
    public boolean isFlying() { return true; }

    @Override
    public boolean useDirectNavigator() {
        return true;
    }

    @Override
    public boolean canSee(Entity target) {
        return true;
    }

    @Override
    public boolean isVulnerableTo(String type, DamageSource source, float damage) {
    	if(type.equals("inWall")) return false;
    	return super.isVulnerableTo(type, source, damage);
    }

    /** Returns true if this mob should be damaged by the sun. **/
    public boolean daylightBurns() { return super.daylightBurns(); }

    @Override
    public boolean canBurn() {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
    	if(this.hasAttackTarget()) {
    		if(this.getTarget() instanceof PlayerEntity)
    			if("jbams".equalsIgnoreCase((this.getTarget()).getName().toString())) // JonBams special sound!
    				return ObjectManager.getSound(this.creatureInfo.getName() + "_say_jon");
    	}
        if(this.isTamed() && this.getOwner() != null) {
            if("jbams".equalsIgnoreCase(this.getOwnerName().getString()))
                return ObjectManager.getSound(this.creatureInfo.getName() + "_say_jon");
        }
    	return super.getAmbientSound();
    }

    @Override
    public ResourceLocation getTexture(String suffix) {
        if(!this.hasCustomName() || !"Satan Claws".equals(this.getCustomName().getString()))
            return super.getTexture(suffix);

        String textureName = this.getTextureName() + "_satanclaws";
        if(!"".equals(suffix)) {
            textureName += "_" + suffix;
        }
        if(TextureManager.getTexture(textureName) == null)
            TextureManager.addTexture(textureName, this.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
        return TextureManager.getTexture(textureName);
    }
}

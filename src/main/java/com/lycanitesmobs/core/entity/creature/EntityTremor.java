package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class EntityTremor extends TameableCreatureEntity implements IMob {

    public EntityTremor(EntityType<? extends EntityTremor> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = true;

		this.setupMob();

        this.maxUpStep = 1.0F;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(true));
    }

	@Override
    public void aiStep() {
        super.aiStep();
        
        if(!this.getCommandSenderWorld().isClientSide && this.isRareVariant() && !this.isPetType("familiar")) {
	    	// Random Charging:
	    	if(this.hasAttackTarget() && this.distanceTo(this.getTarget()) > 1 && this.getRandom().nextInt(20) == 0) {
	    		if(this.position().y() - 1 > this.getTarget().position().y())
	    			this.leap(6.0F, -1.0D, this.getTarget());
	    		else if(this.position().y() + 1 < this.getTarget().position().y())
	    			this.leap(6.0F, 1.0D, this.getTarget());
	    		else
	    			this.leap(6.0F, 0D, this.getTarget());
	    	}
        }

        // Particles:
        if(this.getCommandSenderWorld().isClientSide)
			for(int i = 0; i < 2; ++i) {
				this.getCommandSenderWorld().addParticle(ParticleTypes.SMOKE, this.position().x() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, this.position().y() + this.random.nextDouble() * (double)this.getDimensions(Pose.STANDING).height, this.position().z() + (this.random.nextDouble() - 0.5D) * (double)this.getDimensions(Pose.STANDING).width, 0.0D, 0.0D, 0.0D);
			}
    }

    @Override
    public boolean attackMelee(Entity target, double damageScale) {
    	if(!super.attackMelee(target, damageScale))
    		return false;
    	
    	// Explosion:
		int explosionStrength = Math.max(0, this.creatureInfo.getFlag("explosionStrength", 1));
		Explosion.Mode explosionMode = explosionStrength > 0 && this.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? Explosion.Mode.BREAK : Explosion.Mode.NONE;
		if(this.isPetType("familiar")) {
			explosionStrength = 1;
			explosionMode = Explosion.Mode.NONE;
		}
		this.getCommandSenderWorld().explode(this, this.position().x(), this.position().y(), this.position().z(), explosionStrength, explosionMode);

        return true;
    }

    @Override
    public boolean isFlying() {
    	return true;
    }

    @Override
    public boolean isVulnerableTo(String type, DamageSource source, float damage) {
		if(source.isExplosion()) {
			this.heal(damage);
			return false;
		}
    	if(type.equals("cactus") || type.equals("inWall")) return false;
    	    return super.isVulnerableTo(type, source, damage);
    }

	@Override
	public boolean isVulnerableTo(Entity entity) {
		if(entity instanceof WitherEntity) {
			return false;
		}
		return super.isVulnerableTo(entity);
	}

	@Override
	public boolean canBeAffected(EffectInstance effectInstance) {
    	if(effectInstance.getEffect() == Effects.WITHER) {
    		return false;
		}
		return super.canBeAffected(effectInstance);
	}
    
    @Override
    public boolean canBurn() { return false; }

    @Override
	public boolean canBeTargetedBy(LivingEntity entity) {
    	if(entity instanceof WitherEntity) {
    		return false;
		}
		return super.canBeTargetedBy(entity);
	}
}

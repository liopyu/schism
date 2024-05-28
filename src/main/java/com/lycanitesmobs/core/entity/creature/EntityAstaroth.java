package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.projectile.EntityHellShield;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class EntityAstaroth extends TameableCreatureEntity implements IMob {
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityAstaroth(EntityType<? extends EntityAstaroth> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = false;
        this.solidCollision = false;
        this.setupMob();
        this.hitAreaWidthScale = 1.5F;

        this.maxUpStep = 1.0F;
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false).setRange(1D).setMaxChaseDistance(8.0F));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(1.0D).setRange(40.0F).setMinChaseDistance(16.0F).setChaseTime(-1));
    }


    // ==================================================
    //                      Update
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Asmodeus Master:
        if(this.updateTick % 20 == 0) {
            if (this.getMasterTarget() != null && this.getMasterTarget() instanceof EntityAsmodeus && ((BaseCreatureEntity)this.getMasterTarget()).getBattlePhase() > 0) {
                EntityHellShield projectile = new EntityHellShield(ProjectileManager.getInstance().oldProjectileTypes.get(EntityHellShield.class), this.getCommandSenderWorld(), this);
                projectile.setProjectileScale(3f);
                projectile.setPos(
                        projectile.position().x(),
                        projectile.position().y() - this.getDimensions(Pose.STANDING).height * 0.35D,
                        projectile.position().z()
                );
                double dX = this.getMasterTarget().position().x() - this.position().x();
                double dY = this.getMasterTarget().position().y() + (this.getMasterTarget().getDimensions(Pose.STANDING).height * 0.75D) - projectile.position().y();
                double dZ = this.getMasterTarget().position().z() - this.position().z();
                double distance = MathHelper.sqrt(dX * dX + dZ * dZ) * 0.1F;
                float velocity = 0.8F;
                projectile.shoot(dX, dY + distance, dZ, velocity, 0.0F);
                this.getCommandSenderWorld().addFreshEntity(projectile);
            }
        }
    }
    
    
	// ==================================================
    //                      Attacks
    // ==================================================
    @Override
    public boolean canAttack(LivingEntity target) {
        if(target instanceof EntityTrite || target instanceof EntityMalwrath ||  target instanceof EntityAsmodeus)
            return false;
        return super.canAttack(target);
    }
    
    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("devilstar", target, range, 0, new Vector3d(0, 0, 0), 1.2f, 1f, 1F);
        super.attackRanged(target, range);
    }
	
	
	// ==================================================
   	//                      Death
   	// ==================================================
	@Override
	public void die(DamageSource damageSource) {
        if(!this.getCommandSenderWorld().isClientSide && CreatureManager.getInstance().getCreature("trite").enabled) {
            int j = 2 + this.random.nextInt(5) + getCommandSenderWorld().getDifficulty().getId() - 1;
            if(this.isTamed()) {
                j = 3;
            }
            for(int k = 0; k < j; ++k) {
                EntityTrite trite = (EntityTrite)CreatureManager.getInstance().getCreature("trite").createEntity(this.getCommandSenderWorld());
                this.summonMinion(trite, this.random.nextFloat() * 360.0F, 0.5F);
                if(this.isTamed()) {
                    trite.setTemporary(5 * 20);
                }
            }
        }
        super.die(damageSource);
    }


    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }
}

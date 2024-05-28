package com.lycanitesmobs.core.entity.projectile;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.api.IGroupBoss;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.creature.EntityRahovart;
import com.lycanitesmobs.core.info.ObjectLists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class EntityHellfireWall extends BaseProjectileEntity {

    // ==================================================
 	//                   Constructors
 	// ==================================================

    public EntityHellfireWall(EntityType<? extends BaseProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public EntityHellfireWall(EntityType<? extends BaseProjectileEntity> entityType, World world, LivingEntity par2LivingEntity) {
        super(entityType, world, par2LivingEntity);
    }

    public EntityHellfireWall(EntityType<? extends BaseProjectileEntity> entityType, World world, double par2, double par4, double par6) {
        super(entityType, world, par2, par4, par6);
    }
    
    // ========== Setup Projectile ==========
    public void setup() { // Size 10F
    	this.entityName = "hellfirewall";
    	this.modInfo = LycanitesMobs.modInfo;
    	this.setDamage(10);
    	this.setProjectileScale(20F);
        this.movement = false;
        this.ripper = true;
        this.pierceBlocks = true;
        this.projectileLife = 2 * 20;
        this.animationFrameMax = 59;
        this.textureTiling = 2;
        this.noPhysics = true;
        this.waterProof = true;
        this.lavaProof = true;
    }

    @Override
    public boolean isOnFire() { return false; }


    // ==================================================
    //                      Update
    // ==================================================
    @Override
    public void tick() {
    	super.tick();

    	if (!this.getCommandSenderWorld().isClientSide) {
            List list = this.getCommandSenderWorld().getEntities(this, this.getBoundingBox().inflate(this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z()).expandTowards(1.0D, 1.0D, 1.0D));

            for (int j = 0; j < list.size(); ++j) {
                Entity entity = (Entity)list.get(j);
                this.onHit(new EntityRayTraceResult(entity));
            }
        }
    }

    //========== Entity Living Collision ==========
    @Override
    public boolean onEntityLivingDamage(LivingEntity entityLiving) {
    	if(!entityLiving.isInvulnerableTo(DamageSource.ON_FIRE))
    		entityLiving.setSecondsOnFire(this.getEffectDuration(10) / 20);
    	return true;
    }

    //========== Do Damage Check ==========
    @Override
    public boolean canDamage(LivingEntity targetEntity) {
        Entity owner = this.getShooter();
        if(owner == null) {
            if(targetEntity instanceof EntityRahovart)
                return false;
        }
        return super.canDamage(targetEntity);
    }

    //========== On Damage ==========
    @Override
    public void onDamage(LivingEntity target, float damage, boolean attackSuccess) {

        // Remove Good Potion Effects:
        for(EffectInstance potionEffect : target.getActiveEffects().toArray(new EffectInstance[target.getActiveEffects().size()])) {
            if(ObjectLists.inEffectList("buffs", potionEffect.getEffect()))
                target.removeEffect(potionEffect.getEffect());
        }

        boolean obliterate = true;
        if(target instanceof PlayerEntity)
            obliterate = false;
        else if(target instanceof TameableEntity) {
            obliterate = !(((TameableEntity)target).getOwner() instanceof PlayerEntity);
        }
        else if(target instanceof TameableCreatureEntity) {
            obliterate = !(((TameableCreatureEntity)target).getOwner() instanceof PlayerEntity);
        }
        if(target instanceof IGroupBoss)
            obliterate = false;
        if(obliterate)
            target.setHealth(0);
        super.onDamage(target, damage, attackSuccess);
    }
    
    
    // ==================================================
 	//                      Sounds
 	// ==================================================
    @Override
    public SoundEvent getLaunchSound() {
    	return ObjectManager.getSound("hellfirewall");
    }
    
    
    // ==================================================
    //                   Brightness
    // ==================================================
    public float getBrightness() {
        return 1.0F;
    }
    
    @OnlyIn(Dist.CLIENT)
    public int getBrightnessForRender() {
        return 15728880;
    }
}

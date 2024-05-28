package com.lycanitesmobs.core.entity.projectile;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.creature.EntityRahovart;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityHellfireOrb extends BaseProjectileEntity {

    // ==================================================
 	//                   Constructors
 	// ==================================================
    public EntityHellfireOrb(EntityType<? extends BaseProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public EntityHellfireOrb(EntityType<? extends BaseProjectileEntity> entityType, World world, LivingEntity par2LivingEntity) {
        super(entityType, world, par2LivingEntity);
    }

    public EntityHellfireOrb(EntityType<? extends BaseProjectileEntity> entityType, World world, double par2, double par4, double par6) {
        super(entityType, world, par2, par4, par6);
    }
    
    // ========== Setup Projectile ==========
    public void setup() {
    	this.entityName = "hellfireorb";
    	this.modInfo = LycanitesMobs.modInfo;
    	this.setDamage(0);
    	this.setProjectileScale(2F);
        this.movement = false;
        this.ripper = true;
        this.pierceBlocks = true;
        this.projectileLife = 5;
        this.animationFrameMax = 4;
        this.noPhysics = true;
    }

    @Override
    public boolean isOnFire() { return false; }


    // ==================================================
    //                      Update
    // ==================================================
    @Override
    public void tick() {
        super.tick();
    }
    
    
    // ==================================================
 	//                     Impact
 	// ==================================================
    //========== Entity Living Collision ==========
    @Override
    public boolean onEntityLivingDamage(LivingEntity entityLiving) {
    	if(!entityLiving.isInvulnerableTo(DamageSource.ON_FIRE))
    		entityLiving.setSecondsOnFire(this.getEffectDuration(10) / 20);
    	return true;
    }

    //========== Do Damage Check ==========
    public boolean canDamage(LivingEntity targetEntity) {
        LivingEntity owner =  (LivingEntity) this.getShooter();
        if(owner == null) {
            if(targetEntity instanceof EntityRahovart)
                return false;
        }
        return super.canDamage(targetEntity);
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

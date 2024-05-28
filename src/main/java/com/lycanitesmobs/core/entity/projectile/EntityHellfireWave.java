package com.lycanitesmobs.core.entity.projectile;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.creature.EntityRahovart;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityHellfireWave extends BaseProjectileEntity {

	// Properties:
    public EntityHellfireWall[][] hellfireWalls;
    protected int hellfireWidth = 5;
    protected int hellfireHeight = 3;
    protected int hellfireSize = 10;
    public int time = 0;
    public int timeMax = 10 * 20;
    public float angle = 90;
    public double rotation = 0;

    // ==================================================
 	//                   Constructors
 	// ==================================================
    public EntityHellfireWave(EntityType<? extends BaseProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public EntityHellfireWave(EntityType<? extends BaseProjectileEntity> entityType, World world, LivingEntity shooterEntity) {
        super(entityType, world, shooterEntity);
    }

    public EntityHellfireWave(EntityType<? extends BaseProjectileEntity> entityType, World world, double x, double y, double z) {
        super(entityType, world, x, y, z);
    }
    
    // ========== Setup Projectile ==========
    public void setup() { // Size 2F
    	this.entityName = "hellfirewave";
    	this.modInfo = LycanitesMobs.modInfo;
    	this.setDamage(0);
    	this.setProjectileScale(0F);
        this.movement = false;
        this.ripper = true;
        this.pierceBlocks = true;
        this.projectileLife = 5 * 20;
        this.animationFrameMax = 59;
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
        if(this.getCommandSenderWorld().isClientSide)
            return;

        // Time Update:
        if(this.time++ >= this.timeMax)
            this.remove();

        // Populate:
        if(this.hellfireWalls == null) {
            this.hellfireWalls = new EntityHellfireWall[this.hellfireHeight][this.hellfireWidth];
            for(int row = 0; row < this.hellfireHeight; row++) {
                for(int col = 0; col < this.hellfireWidth; col++) {
                    if(this.getOwner() != null)
                        this.hellfireWalls[row][col] = new EntityHellfireWavePart(ProjectileManager.getInstance().oldProjectileTypes.get(EntityHellfireWavePart.class), this.getCommandSenderWorld(), (LivingEntity) this.getShooter());
                    else
                        this.hellfireWalls[row][col] = new EntityHellfireWavePart(ProjectileManager.getInstance().oldProjectileTypes.get(EntityHellfireWavePart.class), this.getCommandSenderWorld(), this.position().x(), this.position().y() + 5 + (this.hellfireSize * row), this.position().z());
                    this.hellfireWalls[row][col].setPos(
                            this.hellfireWalls[row][col].position().x(),
                            this.position().y() + (this.hellfireSize * row),
                            this.hellfireWalls[row][col].position().z()
                    );
                    this.getCommandSenderWorld().addFreshEntity(hellfireWalls[row][col]);
                    this.hellfireWalls[row][col].setProjectileScale(this.hellfireSize * 2);
                }
            }
        }

        // Move:
        for(int row = 0; row < this.hellfireHeight; row++) {
            for(int col = 0; col < this.hellfireWidth; col++) {
                double rotationRadians = Math.toRadians(((((float)col / this.hellfireWidth) * this.angle) - (this.angle / 2) + this.rotation) % 360);
                double x = (((float)this.time / this.timeMax) * 200) * Math.cos(rotationRadians) - Math.sin(rotationRadians);
                double z = (((float)this.time / this.timeMax) * 200) * Math.sin(rotationRadians) + Math.cos(rotationRadians);
                this.hellfireWalls[row][col].setPos(
                        this.position().x() + x,
                        this.position().y() + (this.hellfireSize * row),
                        this.position().z() + z
                );
                this.hellfireWalls[row][col].projectileLife = 2 * 20;
                if(!this.isAlive())
                    this.hellfireWalls[row][col].remove();
            }
        }
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
        LivingEntity owner = (LivingEntity) this.getShooter();
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
    	return ObjectManager.getSound("hellfirewave");
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


    // ==================================================
    //                      Visuals
    // ==================================================
    public ResourceLocation getTexture() {
        if(TextureManager.getTexture("hellfirewall") == null)
            TextureManager.addTexture("hellfirewall", this.modInfo, "textures/items/hellfirewall" + ".png");
        return TextureManager.getTexture("hellfirewall");
    }
}

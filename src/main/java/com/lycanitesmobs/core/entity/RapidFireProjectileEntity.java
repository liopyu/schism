package com.lycanitesmobs.core.entity;

import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class RapidFireProjectileEntity extends BaseProjectileEntity {
	// Properties:
	public LivingEntity shootingEntity;
	private float projectileWidth = 0.2f;
	private float projectileHeight = 0.2f;
	
	// Rapid Fire:
	private Class projectileClass;
	private ProjectileInfo projectileInfo;
	private int rapidTime = 100;
	private int rapidDelay = 5;
	
	// Offsets:
	public double offsetX = 0;
	public double offsetY = 0;
	public double offsetZ = 0;
	
    // ==================================================
 	//                   Constructors
 	// ==================================================
	public RapidFireProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, World world) {
		super(entityType, world);
		this.noPhysics = true;
	}

	public RapidFireProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Class entityClass, World world, int setTime, int setDelay) {
		super(entityType, world);
		this.projectileClass = entityClass;
		this.rapidTime = setTime;
		this.rapidDelay = setDelay;
		this.noPhysics = true;
	}

	public RapidFireProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Class entityClass, World world, double x, double y, double z, int setTime, int setDelay) {
        super(entityType, world, x, y, z);
		this.projectileClass = entityClass;
        this.rapidTime = setTime;
        this.rapidDelay = setDelay;
        this.noPhysics = true;
    }

    public RapidFireProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Class entityClass, World world, LivingEntity entityLivingBase, int setTime, int setDelay) {
        super(entityType, world, entityLivingBase);
        //this.setSize(projectileWidth, projectileHeight);
        this.projectileClass = entityClass;
        this.shootingEntity = entityLivingBase;
        this.offsetX = this.position().x() - entityLivingBase.position().x();
        this.offsetY = this.position().y() - entityLivingBase.position().y();
        this.offsetZ = this.position().z() - entityLivingBase.position().z();
        this.rapidTime = setTime;
        this.rapidDelay = setDelay;
        this.noPhysics = true;
    }

	public RapidFireProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, ProjectileInfo projectileInfo, World world, double par2, double par4, double par6, int setTime, int setDelay) {
		super(entityType, world, par2, par4, par6);
		//this.setSize(projectileWidth, projectileHeight);
		this.projectileInfo = projectileInfo;
		this.rapidTime = setTime;
		this.rapidDelay = setDelay;
		this.noPhysics = true;
	}

	public RapidFireProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, ProjectileInfo projectileInfo, World world, LivingEntity entityLivingBase, int setTime, int setDelay) {
		super(entityType, world, entityLivingBase);
		//this.setSize(projectileWidth, projectileHeight);
		this.projectileInfo = projectileInfo;
		this.shootingEntity = entityLivingBase;
		this.offsetX = this.position().x() - entityLivingBase.position().x();
		this.offsetY = this.position().y() - entityLivingBase.position().y();
		this.offsetZ = this.position().z() - entityLivingBase.position().z();
		this.rapidTime = setTime;
		this.rapidDelay = setDelay;
		this.noPhysics = true;
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return new SSpawnObjectPacket(this);
	}
	
    
    // ==================================================
 	//                   Update
 	// ==================================================
    @Override
    public void tick() {
    	if(this.shootingEntity != null) {
    		this.setPos(
					shootingEntity.position().x() + this.offsetX,
					shootingEntity.position().y() + this.offsetY,
					shootingEntity.position().z() + this.offsetZ
			);
    	}
    	if(rapidTime > 0) {
	    	if(projectileClass == null && this.projectileInfo == null) {
	    		rapidTime = 0;
	    		return;
	    	}
	    	
	    	if(rapidTime % rapidDelay == 0)
	    		fireProjectile();
	    	
	    	rapidTime--;
    	}
    	else if(this.isAlive()) {
    		this.remove();
    	}
    }
	
    
    // ==================================================
 	//                    Add Time
 	// ==================================================
	public void addTime(int addTime) {
		this.rapidTime += addTime;
	}
	
    
    // ==================================================
 	//                 Fire Projectile
 	// ==================================================
    public void fireProjectile() {
    	World world = this.getCommandSenderWorld();
    	if(world.isClientSide)
    		return;
    	
		try {
	        ProjectileEntity projectile;

	        if(this.shootingEntity == null) {
				if(this.projectileInfo != null) {
					projectile = this.projectileInfo.createProjectile(this.getCommandSenderWorld(), this.position().x(), this.position().y(), this.position().z());
					projectile.shoot(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z, (float)this.projectileInfo.velocity, 0);
				}
				else {
					projectile = ProjectileManager.getInstance().createOldProjectile(this.projectileClass, world, this.position().x(), this.position().y(), this.position().z());
					projectile.shoot(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z, 1, 1);
				}
	        }
	        else {
	        	if(this.projectileInfo != null) {
					projectile = this.projectileInfo.createProjectile(this.getCommandSenderWorld(), this.shootingEntity);
					projectile.shoot(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z, (float)this.projectileInfo.velocity, 0);
				}
	        	else {
					projectile = ProjectileManager.getInstance().createOldProjectile(this.projectileClass, world, this.shootingEntity);
					projectile.shoot(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z, 1, 1);
				}
                if(projectile instanceof ThrowableEntity) {
                    ThrowableEntity entityThrowable = (ThrowableEntity)projectile;
                    entityThrowable.setPos(this.shootingEntity.position().x() + this.offsetX, this.shootingEntity.position().y() + this.offsetY, this.shootingEntity.position().z() + this.offsetZ);
                }
	        }
	        
	        if(projectile instanceof BaseProjectileEntity) {
                ((BaseProjectileEntity) projectile).setProjectileScale(this.projectileScale);
            }
	        
	        world.addFreshEntity(projectile);
		}
		catch (Exception e) {
			System.out.println("[WARNING] [LycanitesMobs] EntityRapidFire was unable to instantiate the given projectile class.");
			e.printStackTrace();
		}
    }
	
    
    // ==================================================
 	//                   Movement
 	// ==================================================
    // ========== Gravity ==========
    @Override
    protected float getGravity() {
        return 0.0F;
    }

    // ========== Set Position ==========
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        if(this.shootingEntity != null) {
            this.offsetX = x - this.shootingEntity.position().x();
            this.offsetY = y - this.shootingEntity.position().y();
            this.offsetZ = z - this.shootingEntity.position().z();
        }
    }
    
    
    // ==================================================
 	//                     Impact
 	// ==================================================
    @Override
    protected void onHit(RayTraceResult rayTraceResult) {
    	return;
    }
    
    
    // ==================================================
 	//                      Visuals
 	// ==================================================
    @Override
    public ResourceLocation getTexture() {
    	return null;
    }
}

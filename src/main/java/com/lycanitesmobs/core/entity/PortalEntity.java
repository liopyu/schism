package com.lycanitesmobs.core.entity;

import com.lycanitesmobs.client.ClientManager;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.Utilities;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.item.summoningstaff.ItemStaffSummoning;
import com.lycanitesmobs.core.pets.SummonSet;
import com.lycanitesmobs.core.tileentity.TileEntitySummoningPedestal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

public class PortalEntity extends BaseProjectileEntity {
	// Summoning Portal:
	private double targetX;
	private double targetY;
	private double targetZ;
	public int summonAmount = 0;
	public int summonTick = 0;
    public int summonTime = 5 * 20;
	public double portalRange = 32.0D;
    public int summonDuration = 60 * 20;
	
	// Properties:
	public PlayerEntity shootingEntity;
	public EntityType summonType;
	public CreatureInfo creatureInfo;
	public ItemStaffSummoning portalItem;
    public UUID ownerUUID;
    public TileEntitySummoningPedestal summoningPedestal;

    // Datawatcher:
    protected static final DataParameter<Optional<UUID>> OWNER_UUID = EntityDataManager.defineId(PortalEntity.class, DataSerializers.OPTIONAL_UUID);
	
    // ==================================================
 	//                   Constructors
 	// ==================================================
    public PortalEntity(EntityType<? extends PortalEntity> entityType, World world) {
        super(entityType, world);
        this.setStats();
    }

    public PortalEntity(EntityType<? extends PortalEntity> entityType, World world, PlayerEntity shooter, SummonSet summonSet, ItemStaffSummoning portalItem) {
        super(entityType, world, shooter);
        this.shootingEntity = shooter;
        this.summonType = summonSet.getCreatureType();
		this.creatureInfo = summonSet.getCreatureInfo();
        this.portalItem = portalItem;
        this.setStats();
    }

	public PortalEntity(EntityType<? extends PortalEntity> entityType, World world, PlayerEntity shooter, CreatureInfo creatureInfo, ItemStaffSummoning portalItem) {
		super(entityType, world, shooter);
		this.shootingEntity = shooter;
		this.creatureInfo = creatureInfo;
		this.summonType = creatureInfo.getEntityType();
		this.portalItem = portalItem;
		this.setStats();
	}

    public PortalEntity(EntityType<? extends PortalEntity> entityType, World world, TileEntitySummoningPedestal summoningPedestal) {
        super(entityType, world);
        this.summoningPedestal = summoningPedestal;
        this.setStats();
        this.setPos(
				summoningPedestal.getBlockPos().getX() + 0.5D,
				summoningPedestal.getBlockPos().getY() + 1.5D,
				summoningPedestal.getBlockPos().getZ() + 0.5D
		);
    }
    
    public void setStats() {
    	this.entityName = "summoningportal";
        this.setProjectileScale(6);
        this.moveToTarget();

        this.textureOffsetY = -0.5f;
        this.animationFrameMax = 7;
        this.movement = false;

        this.waterProof = true;
        this.lavaProof = true;

        this.entityData.define(OWNER_UUID, Optional.empty());
    }
    
    
    // ==================================================
 	//                     Updates
 	// ==================================================
    // ========== Main Update ==========
    @Override
    public void tick() {
        if(this.shootingEntity != null || this.summoningPedestal != null) {
			this.projectileLife = 5;
		}
        super.tick();

        // ==========Summoning Pedestal ==========
        if(!this.getCommandSenderWorld().isClientSide) {
            if(this.summoningPedestal != null) {
                this.shootingEntity = this.summoningPedestal.getPlayer();
                this.summonType = this.summoningPedestal.getSummonType();
                this.creatureInfo = this.summoningPedestal.getCreatureInfo();
            }
        }

        // ==========Sync Shooter Name ==========
        if(!this.getCommandSenderWorld().isClientSide) {
            // Summoning Staff or Summoning Pedestal (with active player):
            if(this.shootingEntity != null) {
				this.entityData.set(OWNER_UUID, Optional.of(this.shootingEntity.getUUID()));
			}
            // Summoning Pedestal:
            else if(this.summoningPedestal != null && this.summoningPedestal.getOwnerUUID() != null) {
				this.entityData.set(OWNER_UUID, Optional.of(this.summoningPedestal.getOwnerUUID()));
			}
            // Wild:
            else {
				this.entityData.set(OWNER_UUID, Optional.empty());
			}
        }
        else {
            this.ownerUUID = this.entityData.get(OWNER_UUID).orElse(null);
        }

    	// ========== Check for Despawn ==========
    	if(!this.getCommandSenderWorld().isClientSide && this.isAlive()) {
            // Summoning Pedestal:
            if(this.summoningPedestal != null) {
                if(this.summonType == null) {
                    this.remove();
                    return;
                }
            }

            // Summoning Staff:
            else {
                if(this.shootingEntity == null || !this.shootingEntity.isAlive() || this.portalItem == null) {
                    this.remove();
                    return;
                }
                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(this.shootingEntity);
				if(playerExt != null && playerExt.staffPortal != this) {
					this.remove();
					return;
				}
            }
    	}
    	
    	// ========== Move ==========
    	this.moveToTarget();

        // ========== Stat Sync ==========
        // Summoning Staff:
        if(this.shootingEntity != null && this.summoningPedestal == null) {
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(this.shootingEntity);
            if(playerExt != null && this.portalItem != null) {
                if(++this.summonTick >= this.portalItem.getRapidTime(null)) {
                    this.summonDuration = this.portalItem.getSummonDuration();
                    if(this.shootingEntity.abilities.instabuild) {
						this.summonAmount += this.portalItem.getSummonAmount();
					}
                    else {
                    	int creatureSummonCost = 4;
                    	if(this.creatureInfo != null) {
							creatureSummonCost = this.creatureInfo.summonCost;
						}
                        float summonMultiplier = (float) (creatureSummonCost + this.portalItem.getSummonCostBoost()) * this.portalItem.getSummonCostMod();
                        int summonCost = Math.round((float) playerExt.summonFocusCharge * summonMultiplier);
                        if(playerExt.summonFocus >= summonCost) {
                            if(this.portalItem.getAdditionalCosts(this.shootingEntity)) {
                                playerExt.summonFocus -= summonCost;
                                this.summonAmount += this.portalItem.getSummonAmount();
                            }
                        }
                    }
                    this.summonTick = 0;
                }
            }
        }

        // Summoning Pedestal:
        else if(this.summonType != null) {
            if (++this.summonTick >= this.summonTime) {
                this.summonAmount = this.summoningPedestal.summonAmount;
                this.summonTick = 0;
            }
        }

        // ========== Client ==========
        if(this.getCommandSenderWorld().isClientSide) {
            for(int i = 0; i < 32; ++i) {
                double angle = Math.toRadians(this.random.nextFloat() * 360);
                float distance = this.random.nextFloat() * 2;
                double x = distance * Math.cos(angle) + Math.sin(angle);
                double z = distance * Math.sin(angle) - Math.cos(angle);
                this.getCommandSenderWorld().addParticle(ParticleTypes.PORTAL,
                        this.position().x() + x,
                        this.position().y() + (4.0F * this.random.nextFloat()) - 2.0F,
                        this.position().z() + z,
                        0.0D, 0.0D, 0.0D);
            }
            return;
        }
    }
    
    
    // ==================================================
  	//                 Summon Creatures
  	// ==================================================
    public int summonCreatures() {
    	if(this.getCommandSenderWorld().isClientSide) {
			return 1;
		}
        if(this.summonType == null) {
			return 0;
		}

    	for(int i = 0; i < this.summonAmount; i++) {
	    	Entity entity = this.summonType.create(this.getCommandSenderWorld());
	    	if(entity == null) {
				return 0;
			}
	    	entity.moveTo(this.position().x(), this.position().y(), this.position().z(), this.random.nextFloat() * 360.0F, 0.0F);

	    	if(entity instanceof BaseCreatureEntity) {
                BaseCreatureEntity entityCreature = (BaseCreatureEntity) entity;

                // Summoning Staff:
                if (this.shootingEntity != null && this.summoningPedestal == null) {
                    entityCreature.setMinion(true);
                    if (entityCreature instanceof TameableCreatureEntity) {
                        ((TameableCreatureEntity) entityCreature).setPlayerOwner(this.shootingEntity);
                        if (this.portalItem != null) {
                            this.portalItem.applyMinionBehaviour((TameableCreatureEntity) entityCreature, this.shootingEntity);
                            this.portalItem.applyMinionEffects(entityCreature);
                        }
                    }
                }

                // Summoning Pedestal:
                else if (this.summoningPedestal != null && this.summoningPedestal.getOwnerUUID() != null) {
                    entityCreature.setMinion(true);
					entityCreature.summoningPedestal = this.summoningPedestal;
                    if (entityCreature instanceof TameableCreatureEntity) {
                        ((TameableCreatureEntity) entityCreature).setOwnerId(this.summoningPedestal.getOwnerUUID());
                        this.summoningPedestal.applyMinionBehaviour((TameableCreatureEntity) entityCreature);
                    }
                }

                if (this.summonDuration > 0)
                    entityCreature.setTemporary(this.summonDuration);

                if (this.shootingEntity != null) {
					//this.shootingEntity.addStat(ObjectManager.getStat(entityCreature.creatureInfo.getName() + ".summon"), 1); TODO Player Stats
				}
            }
	    	this.getCommandSenderWorld().addFreshEntity(entity);
    	}
        int amount = this.summonAmount;
    	this.summonAmount = 0;
    	return amount;
    }
	
    
    // ==================================================
 	//                   Movement
 	// ==================================================
    // ========== Gravity ==========
    @Override
    protected float getGravity() {
        return 0.0F;
    }
    
    // ========== Move to Target ==========
    public void moveToTarget() {
    	if(this.shootingEntity != null && this.summoningPedestal == null) {
    		// Get Look Target
	        Vector3d lookDirection = this.shootingEntity.getLookAngle();
			this.targetX = this.shootingEntity.position().x() + (lookDirection.x * this.portalRange);
			this.targetY = this.shootingEntity.position().y() + (lookDirection.y * this.portalRange);
			this.targetZ = this.shootingEntity.position().z() + (lookDirection.z * this.portalRange);
	        
			// Apply Raytrace to Look Target:
			RayTraceResult target = Utilities.raytrace(this.getCommandSenderWorld(), this.shootingEntity.position().x(), this.shootingEntity.position().y(), this.shootingEntity.position().z(), this.targetX, this.targetY, this.targetZ, 1.0F, this, null);
	        if(target != null) {
				this.targetX = target.getLocation().x;
				this.targetY = target.getLocation().y;
				this.targetZ = target.getLocation().z;
	        }

	        this.targetY += 1.0D;

			// Update Position to Target:
			this.setPos(this.targetX, this.targetY, this.targetZ);
        }
    }
    
    // ========== Get Coord Behind ==========
    /** Returns the XYZ coordinate in front or behind this entity (using rotation angle) this entity with the given distance, use a negative distance for behind. **/
    public double[] getFacingPosition(Entity entity, double distance) {
    	double angle = Math.toRadians(this.yRot);
    	double xAmount = -Math.sin(angle);
    	double zAmount = Math.cos(angle);
    	double[] coords = new double[3];
        coords[0] = entity.position().x() + (distance * xAmount);
        coords[1] = entity.position().y();
        coords[2] = entity.position().z() + (distance * zAmount);
        return coords;
    }
    
    
    // ==================================================
 	//                     Impact
 	// ==================================================
    @Override
    protected void onHit(RayTraceResult movingObjectPos) {}
    
    
    // ==================================================
 	//                      Visuals
 	// ==================================================
    @Override
    public ResourceLocation getTexture() {
    	if(TextureManager.getTexture(this.entityName) == null) {
			TextureManager.addTexture(this.entityName, LycanitesMobs.modInfo, "textures/particles/" + this.entityName.toLowerCase() + ".png");
		}
        if(TextureManager.getTexture(this.entityName + "_client") == null) {
			TextureManager.addTexture(this.entityName + "_client", LycanitesMobs.modInfo, "textures/particles/" + this.entityName.toLowerCase() + "_client.png");
		}
        if(TextureManager.getTexture(this.entityName + "_player") == null) {
			TextureManager.addTexture(this.entityName + "_player", LycanitesMobs.modInfo, "textures/particles/" + this.entityName.toLowerCase() + "_player.png");
		}

        if(this.ownerUUID != null) {
            if(this.ownerUUID.equals(ClientManager.getInstance().getClientPlayer().getUUID())) {
				return TextureManager.getTexture(this.entityName + "_client");
			}
            else {
				return TextureManager.getTexture(this.entityName + "_player");
			}
        }
     	return TextureManager.getTexture(this.entityName);
    }

    @Override
    public float getTextureOffsetY() {
        return 0.2F;
    }
}

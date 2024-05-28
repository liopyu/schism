package com.lycanitesmobs.core.network;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;
import com.lycanitesmobs.core.entity.EntityFactory;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;

import java.io.IOException;
import java.util.UUID;

public class EntitySpawnPacket implements IPacket<ClientPlayNetHandler> {
	public String entityTypeName = "";
	public int entityId = 0;
	public UUID uuid;
	public float pitch;
	public float yaw;
	public Vector3d pos;
	public double y;
	public double z;

	public EntitySpawnPacket(Entity serverEntity) {
		if(serverEntity != null) {
			if(serverEntity instanceof BaseProjectileEntity) {
				this.entityTypeName = ((BaseProjectileEntity)serverEntity).entityName;
			}
			this.entityId = serverEntity.getId();
			this.uuid = serverEntity.getUUID();
			this.pitch = serverEntity.xRot;
			this.yaw = serverEntity.yRot;
			this.pos = serverEntity.position();
		}
	}

	@Override
	public void read(PacketBuffer packet) throws IOException {
		this.entityTypeName = packet.readUtf();
		this.entityId = packet.readInt();
		this.uuid = packet.readUUID();
		this.pitch = packet.readFloat();
		this.yaw = packet.readFloat();
		this.pos = new Vector3d(packet.readDouble(), packet.readDouble(), packet.readDouble());
	}

	@Override
	public void write(PacketBuffer packet) throws IOException {
		packet.writeUtf(this.entityTypeName);
		packet.writeInt(this.entityId);
		packet.writeUUID(this.uuid);
		packet.writeFloat(this.pitch);
		packet.writeFloat(this.yaw);
		packet.writeDouble(this.pos.x());
		packet.writeDouble(this.pos.y());
		packet.writeDouble(this.pos.z());
	}

	@Override
	public void handle(ClientPlayNetHandler handler) {
		if(!EntityFactory.getInstance().entityTypeNetworkMap.containsKey(this.entityTypeName)) {
			LycanitesMobs.logWarning("", "Unable to find entity type from packet: " + this.entityTypeName);
			return;
		}
		EntityType entityType = EntityFactory.getInstance().entityTypeNetworkMap.get(this.entityTypeName);
		Entity entity = EntityFactory.getInstance().create(entityType, LycanitesMobs.PROXY.getWorld());
		if(entity == null) {
			LycanitesMobs.logWarning("", "Unable to create client entity from packet: " + this.entityTypeName);
			return;
		}
		entity.setPos(this.pos.x(), this.pos.y(), this.pos.z());
		entity.xRot = this.pitch;
		entity.yRot = this.yaw;
		entity.setId(this.entityId);
		entity.setUUID(this.uuid);

		// Projectiles:
		if(entity instanceof BaseProjectileEntity) {
			((BaseProjectileEntity)entity).entityName = this.entityTypeName;
			if(entity instanceof CustomProjectileEntity) {
				ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.entityTypeName);
				if(projectileInfo != null) {
					((CustomProjectileEntity) entity).setProjectileInfo(projectileInfo);
				}
			}
		}

		handler.getLevel().putNonPlayerEntity(this.entityId, entity);
	}
}

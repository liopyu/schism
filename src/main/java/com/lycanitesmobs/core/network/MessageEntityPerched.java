package com.lycanitesmobs.core.network;

import com.lycanitesmobs.client.ClientManager;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageEntityPerched {
	public int perchedOnEntityID;
	public int perchedByEntityID;
	
	public MessageEntityPerched() {}
	public MessageEntityPerched(Entity perchedOnEntityID, Entity perchedByEntity) {
		this.perchedOnEntityID = perchedOnEntityID.getId();
		this.perchedByEntityID = perchedByEntity != null ? perchedByEntity.getId() : 0;
	}
	
	/**
	 * Called when this message is received.
	 */
	public static void handle(MessageEntityPerched message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		if(ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
			return;

		PlayerEntity player = ClientManager.getInstance().getClientPlayer();
		World world = player.getCommandSenderWorld();
		Entity perchedOnEntity = world.getEntity(message.perchedOnEntityID);
		Entity perchedByEntity = message.perchedByEntityID != 0 ? world.getEntity(message.perchedByEntityID) : null;

		if(!(perchedOnEntity instanceof LivingEntity))
			return;
		ExtendedEntity perchedOnEntityExt = ExtendedEntity.getForEntity((LivingEntity)perchedOnEntity);
		if(perchedOnEntityExt != null)
			perchedOnEntityExt.setPerchedByEntity(perchedByEntity);
		return;
	}
	
	/**
	 * Reads the message from bytes.
	 */
	public static MessageEntityPerched decode(PacketBuffer packet) {
		MessageEntityPerched message = new MessageEntityPerched();
		message.perchedOnEntityID = packet.readInt();
		message.perchedByEntityID = packet.readInt();
		return message;
	}
	
	/**
	 * Writes the message into bytes.
	 */
	public static void encode(MessageEntityPerched message, PacketBuffer packet) {
		packet.writeInt(message.perchedOnEntityID);
		packet.writeInt(message.perchedByEntityID);
	}
	
}

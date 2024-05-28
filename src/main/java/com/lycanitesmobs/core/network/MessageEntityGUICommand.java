package com.lycanitesmobs.core.network;

import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageEntityGUICommand {
	int entityID;
	public int guiCommandID;
	
	public MessageEntityGUICommand() {}
	public MessageEntityGUICommand(int guiCommandID, Entity entity) {
		this.entityID = entity.getId();
		this.guiCommandID = guiCommandID;
	}

	/**
	 * Called when this message is received.
	 */
	public static void handle(MessageEntityGUICommand message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		if(ctx.get().getDirection() != NetworkDirection.PLAY_TO_SERVER)
			return;

        ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			World world = player.getCommandSenderWorld();
			Entity entity = world.getEntity(message.entityID);
			if (entity instanceof TameableCreatureEntity) {
				TameableCreatureEntity pet = (TameableCreatureEntity) entity;
				pet.performGUICommand(player, message.guiCommandID);
			}
		});
	}
	
	/**
	 * Reads the message from bytes.
	 */
	public static MessageEntityGUICommand decode(PacketBuffer packet) {
		MessageEntityGUICommand message = new MessageEntityGUICommand();
		message.entityID = packet.readInt();
		message.guiCommandID = packet.readInt();
		return message;
	}
	
	/**
	 * Writes the message into bytes.
	 */
	public static void encode(MessageEntityGUICommand message, PacketBuffer packet) {
		packet.writeInt(message.entityID);
		packet.writeInt(message.guiCommandID);
	}
	
}

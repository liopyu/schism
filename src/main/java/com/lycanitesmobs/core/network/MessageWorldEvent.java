package com.lycanitesmobs.core.network;

import com.lycanitesmobs.client.ClientManager;
import com.lycanitesmobs.ExtendedWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageWorldEvent {
	public String mobEventName;
	public BlockPos pos;
	public int level = 1;
	public int subspecies = 1;

	public MessageWorldEvent() {}
	public MessageWorldEvent(String mobEventName, BlockPos pos, int level, int subspecies) {
		this.mobEventName = mobEventName;
		this.pos = pos;
		this.level = level;
		this.subspecies = subspecies;
	}
	
	/**
	 * Called when this message is received.
	 */
	public static void handle(MessageWorldEvent message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		if(ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
			return;

		PlayerEntity player = ClientManager.getInstance().getClientPlayer();
		World world = player.getCommandSenderWorld();
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
		
		if("".equals(message.mobEventName))
            worldExt.stopWorldEvent();
		else {
            worldExt.startMobEvent(message.mobEventName, null, message.pos, message.level, message.subspecies);
		}
	}
	
	/**
	 * Reads the message from bytes.
	 */
	public static MessageWorldEvent decode(PacketBuffer packet) {
		MessageWorldEvent message = new MessageWorldEvent();
		message.mobEventName = packet.readUtf(256);
		message.pos = packet.readBlockPos();
		message.level = packet.readInt();
		message.subspecies = packet.readInt();
		return message;
	}
	
	/**
	 * Writes the message into bytes.
	 */
	public static void encode(MessageWorldEvent message, PacketBuffer packet) {
        packet.writeUtf(message.mobEventName);
		packet.writeBlockPos(message.pos);
		packet.writeInt(message.level);
		packet.writeInt(message.subspecies);
	}
	
}

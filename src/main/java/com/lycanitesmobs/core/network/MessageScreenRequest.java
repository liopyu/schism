package com.lycanitesmobs.core.network;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageScreenRequest {
	public enum GuiRequest {
		BEASTIARY((byte)0), SUMMONING((byte)1);
		public byte id;
		GuiRequest(byte i) { id = i; }
	}

	public byte screenId;
	
	public MessageScreenRequest() {}
	public MessageScreenRequest(GuiRequest guiRequest) {
		this.screenId = guiRequest.id;
	}
	
	/**
	 * Called when this message is received.
	 */
	public static void handle(MessageScreenRequest message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		if(ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
			return;

		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			LycanitesMobs.PROXY.openScreen(message.screenId, player);
		});
	}
	
	/**
	 * Reads the message from bytes.
	 */
	public static MessageScreenRequest decode(PacketBuffer packet) {
		MessageScreenRequest message = new MessageScreenRequest();
		message.screenId = packet.readByte();
		return message;
	}
	
	/**
	 * Writes the message into bytes.
	 */
	public static void encode(MessageScreenRequest message, PacketBuffer packet) {
		packet.writeByte(message.screenId);
	}
	
}

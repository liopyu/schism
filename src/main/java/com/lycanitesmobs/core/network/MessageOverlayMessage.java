package com.lycanitesmobs.core.network;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageOverlayMessage {
	public ITextComponent message;

	public MessageOverlayMessage() {}
	public MessageOverlayMessage(ITextComponent message) {
		this.message = message;
	}
	
	/**
	 * Called when this message is received.
	 */
	public static void handle(MessageOverlayMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		LycanitesMobs.logDebug("", "Overlay Packet: " + ctx.get().getDirection());
		if(ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
			return;

		net.minecraft.client.Minecraft.getInstance().gui.setOverlayMessage(message.message, false);
	}
	
	/**
	 * Reads the message from bytes.
	 */
	public static MessageOverlayMessage decode(PacketBuffer packet) {
		MessageOverlayMessage message = new MessageOverlayMessage();
		message.message = packet.readComponent();
		return message;
	}
	
	/**
	 * Writes the message into bytes.
	 */
	public static void encode(MessageOverlayMessage message, PacketBuffer packet) {
		packet.writeComponent(message.message);
	}
	
}

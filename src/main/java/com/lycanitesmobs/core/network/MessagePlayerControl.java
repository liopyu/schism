package com.lycanitesmobs.core.network;

import com.lycanitesmobs.core.entity.ExtendedPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessagePlayerControl {
	public byte controlStates;

	public MessagePlayerControl() {}
	public MessagePlayerControl(byte controlStates) {
		this.controlStates = controlStates;
	}
	
	/**
	 * Called when this message is received.
	 */
	public static void handle(MessagePlayerControl message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		if(ctx.get().getDirection() != NetworkDirection.PLAY_TO_SERVER)
			return;

		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
			playerExt.updateControlStates(message.controlStates);
        });
	}
	
	/**
	 * Reads the message from bytes.
	 */
	public static MessagePlayerControl decode(PacketBuffer packet) {
		MessagePlayerControl message = new MessagePlayerControl();
		message.controlStates = packet.readByte();
		return message;
	}
	
	/**
	 * Writes the message into bytes.
	 */
	public static void encode(MessagePlayerControl message, PacketBuffer packet) {
		packet.writeByte(message.controlStates);
	}
	
}

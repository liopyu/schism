package com.lycanitesmobs.core.network;

import com.lycanitesmobs.client.ClientManager;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.pets.SummonSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageSummonSet {
	public byte summonSetID;
	public int subpsecies;
	public int variant;
	public String summonType;
	public byte behaviour;
	
	public MessageSummonSet() {}
	public MessageSummonSet(ExtendedPlayer playerExt, byte summonSetID) {
		this.summonSetID = summonSetID;
		this.summonType = playerExt.getSummonSet(summonSetID).summonType;
		this.subpsecies = playerExt.getSummonSet(summonSetID).subspecies;
		this.variant = playerExt.getSummonSet(summonSetID).variant;
		this.behaviour = playerExt.getSummonSet(summonSetID).getBehaviourByte();
	}
	
	/**
	 * Called when this message is received.
	 */
	public static void handle(MessageSummonSet message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		// Server Side:
		if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
			ctx.get().enqueueWork(() -> {
				PlayerEntity player = ctx.get().getSender();
				ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);

				SummonSet summonSet = playerExt.getSummonSet(message.summonSetID);
				summonSet.readFromPacket(message.summonType, message.subpsecies, message.variant, message.behaviour);
			});
            return;
        }

        // Client Side:
        PlayerEntity player = ClientManager.getInstance().getClientPlayer();
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
        if(playerExt == null)
        	return;

		SummonSet summonSet = playerExt.getSummonSet(message.summonSetID);
		summonSet.readFromPacket(message.summonType, message.subpsecies, message.variant, message.behaviour);
	}
	
	/**
	 * Reads the message from bytes.
	 */
	public static MessageSummonSet decode(PacketBuffer packet) {
		MessageSummonSet message = new MessageSummonSet();
        message.summonSetID = packet.readByte();
        message.summonType = packet.readUtf(256);
		message.subpsecies = packet.readInt();
		message.variant = packet.readInt();
        message.behaviour = packet.readByte();
		return message;
	}
	
	/**
	 * Writes the message into bytes.
	 */
	public static void encode(MessageSummonSet message, PacketBuffer packet) {
        packet.writeByte(message.summonSetID);
        packet.writeUtf(message.summonType);
		packet.writeInt(message.subpsecies);
		packet.writeInt(message.variant);
        packet.writeByte(message.behaviour);
	}
	
}

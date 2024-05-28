package com.lycanitesmobs.core.network;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.ClientManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.CreatureRelationshipEntry;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureKnowledge;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageCreature {
	int entityID;
	int playerReputation = 0;

	public MessageCreature() {}
	public MessageCreature(BaseCreatureEntity creatureEntity, int playerReputation) {
		this.entityID = creatureEntity.getId();
		this.playerReputation = playerReputation;
	}

	/**
	 * Called when this message is received.
	 */
	public static void handle(MessageCreature message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		if(ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
			return;

		PlayerEntity player = ClientManager.getInstance().getClientPlayer();
		World world = player.getCommandSenderWorld();
		Entity entity = world.getEntity(message.entityID);
		if (!(entity instanceof BaseCreatureEntity)) {
			return;
		}
		BaseCreatureEntity creatureEntity = (BaseCreatureEntity)entity;

		CreatureRelationshipEntry relationshipEntry = creatureEntity.relationships.getOrCreateEntry(player);
		relationshipEntry.setReputation(message.playerReputation);
	}
	
	/**
	 * Reads the message from bytes.
	 */
	public static MessageCreature decode(PacketBuffer packet) {
		MessageCreature message = new MessageCreature();
		try {
			message.entityID = packet.readInt();
			message.playerReputation = packet.readInt();
		}
		catch(Exception e) {
			LycanitesMobs.logWarning("", "There was a problem decoding the packet: " + packet + ".");
			e.printStackTrace();
		}
		return message;
	}
	
	/**
	 * Writes the message into bytes.
	 */
	public static void encode(MessageCreature message, PacketBuffer packet) {
        packet.writeInt(message.entityID);
        packet.writeInt(message.playerReputation);
	}
	
}

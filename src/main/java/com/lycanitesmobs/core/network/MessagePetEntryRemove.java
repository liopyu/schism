package com.lycanitesmobs.core.network;

import com.lycanitesmobs.client.ClientManager;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.pets.PetEntry;
import com.lycanitesmobs.core.pets.PetManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MessagePetEntryRemove {
    public UUID petEntryID;


	// ==================================================
	//                    Constructors
	// ==================================================
	public MessagePetEntryRemove() {}
	public MessagePetEntryRemove(ExtendedPlayer playerExt, PetEntry petEntry) {
        this.petEntryID = petEntry.petEntryID;
	}
	
	/**
	 * Called when this message is received.
	 */
	public static void handle(MessagePetEntryRemove message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		// Server Side:
		if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
			ctx.get().enqueueWork(() -> {
				PlayerEntity player = ctx.get().getSender();
				ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);

				PetManager petManager = playerExt.petManager;
				PetEntry petEntry = petManager.getEntry(message.petEntryID);
				if(petEntry == null) {
					LycanitesMobs.logWarning("", "Tried to remove a null PetEntry from server!");
					return; // Nothing to remove!
				}
				petEntry.remove();
            });
            return;
        }

        // Client Side:
        PlayerEntity player = ClientManager.getInstance().getClientPlayer();
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
		if(playerExt == null)
			return;

        PetManager petManager = playerExt.petManager;
        PetEntry petEntry = petManager.getEntry(message.petEntryID);
        if(petEntry == null) {
			LycanitesMobs.logWarning("", "Tried to remove a null PetEntry from client!");
            return; // Nothing to remove!
        }
        petEntry.remove();
	}
	
	/**
	 * Reads the message from bytes.
	 */
	public static MessagePetEntryRemove decode(PacketBuffer packet) {
		MessagePetEntryRemove message = new MessagePetEntryRemove();
		try {
            message.petEntryID = packet.readUUID();
		} catch (Exception e) {
			LycanitesMobs.logWarning("", "There was a problem decoding the packet: " + packet + ".");
			e.printStackTrace();
		}
		return message;
	}
	
	/**
	 * Writes the message into bytes.
	 */
	public static void encode(MessagePetEntryRemove message, PacketBuffer packet) {
		try {
			packet.writeUUID(message.petEntryID);
		} catch (Exception e) {
			LycanitesMobs.logWarning("", "There was a problem encoding the packet: " + packet + ".");
			e.printStackTrace();
		}
	}
	
}

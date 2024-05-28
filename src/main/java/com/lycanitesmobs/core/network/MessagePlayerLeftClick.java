package com.lycanitesmobs.core.network;

import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessagePlayerLeftClick {
	public MessagePlayerLeftClick() {}
	
	/**
	 * Called when this message is received.
	 */
	public static void handle(MessagePlayerLeftClick message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		if(ctx.get().getDirection() != NetworkDirection.PLAY_TO_SERVER)
			return;

		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();

			// Equipment:
			Hand activeHand = player.getUsedItemHand();
			ItemStack activeStack = player.getItemInHand(activeHand);
			if(activeStack.getItem() instanceof ItemEquipment) {
				((ItemEquipment)activeStack.getItem()).onItemLeftClick(player.getCommandSenderWorld(), player, activeHand);
			}
        });
	}
	
	/**
	 * Reads the message from bytes.
	 */
	public static MessagePlayerLeftClick decode(PacketBuffer packet) {
		MessagePlayerLeftClick message = new MessagePlayerLeftClick();
		return message;
	}
	
	/**
	 * Writes the message into bytes.
	 */
	public static void encode(MessagePlayerLeftClick message, PacketBuffer packet) {

	}
	
}

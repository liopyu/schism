package com.lycanitesmobs.core.network;

import com.lycanitesmobs.client.ClientManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageEntityVelocity {
    public int entityID;
    public int motionX;
    public int motionY;
    public int motionZ;

	public MessageEntityVelocity() {}
	public MessageEntityVelocity(Entity entity, double motionX, double motionY, double motionZ) {
		this.entityID = entity.getId();

		if (motionX < -3.9D) {
			motionX = -3.9D;
		}

		if (motionY < -3.9D) {
			motionY = -3.9D;
		}

		if (motionZ < -3.9D) {
			motionZ = -3.9D;
		}

		if (motionX > 3.9D) {
			motionX = 3.9D;
		}

		if (motionY > 3.9D) {
			motionY = 3.9D;
		}

		if (motionZ > 3.9D) {
			motionZ = 3.9D;
		}

		this.motionX = (int)(motionX * 8000.0D);
		this.motionY = (int)(motionY * 8000.0D);
		this.motionZ = (int)(motionZ * 8000.0D);
	}

	/**
	 * Called when this message is received.
	 */
	public static void handle(MessageEntityVelocity message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		if(ctx.get().getDirection() != NetworkDirection.PLAY_TO_CLIENT)
			return;

		PlayerEntity player = ClientManager.getInstance().getClientPlayer();
		World world = player.getCommandSenderWorld();
		Entity entity = world.getEntity(message.entityID);
		entity.setDeltaMovement(entity.getDeltaMovement().add((double)message.motionX / 8000.0D, (double)message.motionY / 8000.0D, (double)message.motionZ / 8000.0D));
	}

	/**
	 * Reads the message from bytes.
	 */
	public static MessageEntityVelocity decode(PacketBuffer packet) {
		MessageEntityVelocity message = new MessageEntityVelocity();
		message.entityID = packet.readVarInt();
		message.motionX = packet.readShort();
		message.motionY = packet.readShort();
		message.motionZ = packet.readShort();
		return message;
	}

	/**
	 * Writes the message into bytes.
	 */
	public static void encode(MessageEntityVelocity message, PacketBuffer packet) {
		packet.writeVarInt(message.entityID);
		packet.writeShort(message.motionX);
		packet.writeShort(message.motionY);
		packet.writeShort(message.motionZ);
	}
}
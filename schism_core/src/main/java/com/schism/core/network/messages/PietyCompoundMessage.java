package com.schism.core.network.messages;

import com.schism.core.creatures.Creatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PietyCompoundMessage
{
    protected int networkId;
    protected CompoundTag compoundTag;

    public PietyCompoundMessage(int networkId, CompoundTag compoundTag)
    {
        this.networkId = networkId;
        this.compoundTag = compoundTag;
    }

    public static void encode(PietyCompoundMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.networkId);
        buffer.writeNbt(message.compoundTag);
    }

    public static PietyCompoundMessage decode(FriendlyByteBuf buffer)
    {
        int networkId = buffer.readInt();
        CompoundTag compoundTag = buffer.readNbt();

        return new PietyCompoundMessage(networkId, compoundTag);
    }

    public static void handle(PietyCompoundMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);
        if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            return;
        }

        context.enqueueWork(() -> {
            Level level = net.minecraft.client.Minecraft.getInstance().player.getLevel();
            Entity entity = level.getEntity(message.networkId);
            if (!(entity instanceof LivingEntity livingEntity)) {
                return;
            }
            Creatures.get().getCreature(livingEntity).ifPresent(creature -> creature.piety().deserializeNBT(message.compoundTag));
        });
    }
}

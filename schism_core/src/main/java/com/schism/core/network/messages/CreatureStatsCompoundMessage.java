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

public class CreatureStatsCompoundMessage
{
    protected int networkId;
    protected CompoundTag compoundTag;

    public CreatureStatsCompoundMessage(int networkId, CompoundTag compoundTag)
    {
        this.networkId = networkId;
        this.compoundTag = compoundTag;
    }

    public static void encode(CreatureStatsCompoundMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.networkId);
        buffer.writeNbt(message.compoundTag);
    }

    public static CreatureStatsCompoundMessage decode(FriendlyByteBuf buffer)
    {
        int networkId = buffer.readInt();
        CompoundTag compoundTag = buffer.readNbt();

        return new CreatureStatsCompoundMessage(networkId, compoundTag);
    }

    public static void handle(CreatureStatsCompoundMessage message, Supplier<NetworkEvent.Context> contextSupplier)
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
            Creatures.get().getCreature(livingEntity).ifPresent(creature -> creature.stats().deserializeNBT(message.compoundTag));
        });
    }
}

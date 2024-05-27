package com.schism.core.network.messages;

import com.schism.core.creatures.Creatures;
import com.schism.core.creatures.ICreature;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class RequestCreatureMessage
{
    protected int id;

    public RequestCreatureMessage(int id)
    {
        this.id = id;
    }

    public static void encode(RequestCreatureMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.id);
    }

    public static RequestCreatureMessage decode(FriendlyByteBuf buffer)
    {
        int id = buffer.readInt();
        return new RequestCreatureMessage(id);
    }

    public static void handle(RequestCreatureMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);
        if (context.getDirection() != NetworkDirection.PLAY_TO_SERVER) {
            return;
        }

        context.enqueueWork(() -> {
            if (context.getSender() == null) {
                return;
            }
            Entity entity = context.getSender().getLevel().getEntity(message.id);
            if (!(entity instanceof LivingEntity livingEntity)) {
                return;
            }
            Optional<ICreature> creature = Creatures.get().getCreature(livingEntity);
            if (creature.isEmpty()) {
                return;
            }
            creature.get().syncClient(context.getSender());
        });
    }
}

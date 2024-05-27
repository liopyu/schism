package com.schism.core.network.messages;

import com.schism.core.client.gui.CreatureDebugOverlay;
import com.schism.core.client.models.AbstractModel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CommandMessage
{
    public enum CommandId {
        CREATURE_TRACK(1),
        CREATURE_TRACK_SELF(2),
        CREATURE_TRACK_CLEAR(3),
        RELOAD_MODELS(4);
        public final int id;
        CommandId(int id) { this.id = id; }
    }
    protected int id;

    public CommandMessage(int id)
    {
        this.id = id;
    }

    public static void encode(CommandMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.id);
    }

    public static CommandMessage decode(FriendlyByteBuf buffer)
    {
        int id = buffer.readInt();
        return new CommandMessage(id);
    }

    public static void handle(CommandMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);
        if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            return;
        }

        context.enqueueWork(() -> {
            switch (message.id) {
                case 1 -> CreatureDebugOverlay.get().target();
                case 2 -> CreatureDebugOverlay.get().self();
                case 3 -> CreatureDebugOverlay.get().clear();
                case 4 -> AbstractModel.reloadAll();
            }
        });
    }
}

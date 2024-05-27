package com.schism.core.network.messages;

import com.schism.core.AbstractRepository;
import com.schism.core.client.gui.Notifications;
import com.schism.core.database.AbstractDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NotificationMessage
{
    protected AbstractDefinition definition;
    protected String event;
    protected String value;

    public NotificationMessage(AbstractDefinition definition, String event, String value)
    {
        this.definition = definition;
        this.event = event;
        this.value = value;
    }

    public static void encode(NotificationMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeUtf(message.definition.type());
        buffer.writeUtf(message.definition.subject());
        buffer.writeUtf(message.event);
        buffer.writeUtf(message.value);
    }

    public static NotificationMessage decode(FriendlyByteBuf buffer)
    {
        String type = buffer.readUtf();
        String subject = buffer.readUtf();
        String event = buffer.readUtf();
        String value = buffer.readUtf();

        AbstractDefinition definition = AbstractRepository.fromType(type).flatMap(repository -> repository.getDefinition(subject)).orElse(null);
        return new NotificationMessage(definition, event, value);
    }

    public static void handle(NotificationMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);
        if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            return;
        }

        if (message.definition == null) {
            return;
        }

        context.enqueueWork(() -> {
            Notifications.get().notify(message.definition, message.event, message.value);
        });
    }
}

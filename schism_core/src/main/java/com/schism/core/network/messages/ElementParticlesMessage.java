package com.schism.core.network.messages;

import com.schism.core.client.ElementsClient;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.elements.ElementRepository;
import com.schism.core.util.Vec3;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ElementParticlesMessage
{
    protected ElementDefinition element;
    protected Vec3 position;
    protected int power;

    public ElementParticlesMessage(ElementDefinition element, Vec3 position, int power)
    {
        this.element = element;
        this.position = position;
        this.power = power;
    }

    public static void encode(ElementParticlesMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeUtf(message.element.subject());
        buffer.writeFloat(message.position.x());
        buffer.writeFloat(message.position.y());
        buffer.writeFloat(message.position.z());
        buffer.writeInt(message.power);
    }

    public static ElementParticlesMessage decode(FriendlyByteBuf buffer)
    {
        ElementDefinition element = ElementRepository.get().getDefinition(buffer.readUtf()).orElse(null);
        Vec3 position = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        return new ElementParticlesMessage(element, position, buffer.readInt());
    }

    public static void handle(ElementParticlesMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.setPacketHandled(true);
        if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            return;
        }

        context.enqueueWork(() -> ElementsClient.get().createSplash(message.element, message.position, message.power));
    }
}

package com.schism.core.network;

import com.schism.core.Schism;
import com.schism.core.network.messages.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class MessageRepository
{
    private static final MessageRepository INSTANCE = new MessageRepository();
    protected static final String PROTOCOL_VERSION = "1";

    protected final SimpleChannel handler = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Schism.NAMESPACE, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    /**
     * Gets the singleton MessageRepository instance.
     * @return The Message Repository which registers network messages.
     */
    public static MessageRepository get()
    {
        return INSTANCE;
    }

    /**
     * Registers all messages.
     */
    public void register()
    {
        int messageId = 0;

        // System:
        this.handler.registerMessage(messageId++, CommandMessage.class, CommandMessage::encode, CommandMessage::decode, CommandMessage::handle);
        this.handler.registerMessage(messageId++, NotificationMessage.class, NotificationMessage::encode, NotificationMessage::decode, NotificationMessage::handle);

        // Effects:
        this.handler.registerMessage(messageId++, ElementParticlesMessage.class, ElementParticlesMessage::encode, ElementParticlesMessage::decode, ElementParticlesMessage::handle);

        // Creatures:
        this.handler.registerMessage(messageId++, CreatureStatsCompoundMessage.class, CreatureStatsCompoundMessage::encode, CreatureStatsCompoundMessage::decode, CreatureStatsCompoundMessage::handle);
        this.handler.registerMessage(messageId++, PietyCompoundMessage.class, PietyCompoundMessage::encode, PietyCompoundMessage::decode, PietyCompoundMessage::handle);
        this.handler.registerMessage(messageId++, RequestCreatureMessage.class, RequestCreatureMessage::encode, RequestCreatureMessage::decode, RequestCreatureMessage::handle);
    }

    /**
     * Sends a message from the server to the provided player. Ignored when called client side.
     * @param message The message to send.
     * @param serverPlayer The player to send to.
     * @param <MessageT> The message type.
     */
    public <MessageT> void toPlayer(MessageT message, ServerPlayer serverPlayer)
    {
        if (serverPlayer.getLevel().isClientSide()) {
            return;
        }
        this.handler.sendTo(message, serverPlayer.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    /**
     * Sends a message from the server to all players in the provided level. Ignored when called client side.
     * @param message The message to send.
     * @param level The level to get players to send to.
     * @param <MessageT> The message type.
     */
    public <MessageT> void toLevel(MessageT message, Level level)
    {
        if (level.isClientSide()) {
            return;
        }
        level.players().stream()
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .forEach(serverPlayer -> this.toPlayer(message, serverPlayer));
    }

    /**
     * Sends a message from the local player to the server.
     * @param message The message to send.
     * @param <MessageT> The message type.
     */
    public <MessageT> void toServer(MessageT message)
    {
        this.handler.sendToServer(message);
    }
}

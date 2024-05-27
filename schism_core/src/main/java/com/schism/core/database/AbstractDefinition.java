package com.schism.core.database;

import com.schism.core.Schism;
import com.schism.core.network.MessageRepository;
import com.schism.core.network.messages.NotificationMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDefinition implements Comparable<AbstractDefinition>
{
    /**
     * The subject of this definition, this should be unique for a specific object like a block name, theme name, config category etc.
     */
    protected final String subject;

    /**
     * The data store that holds config data about this definition.
     */
    protected DataStore dataStore;

    /**
     * A generated Resource Location that objects using this definition can go by.
     */
    protected ResourceLocation resourceLocation;

    /**
     * Constructor
     * @param subject The subject of this definition, this should be unique for a specific object like a block name, theme name, config category etc.
     * @param dataStore The data store that holds config data about this definition.
     */
    public AbstractDefinition(String subject, DataStore dataStore)
    {
        this.subject = subject;
        this.dataStore = dataStore;
        this.update(dataStore);
    }

    /**
     * Gets the type of this definition, this should match the repository.
     * @return The type of this definition.
     */
    public abstract String type();

    /**
     * Gets the subject of this definition.
     * @return The subject of this definition for a specific object like a block name, theme name, config category etc.
     */
    public String subject()
    {
        return this.subject;
    }

    /**
     * Gets a name text component for this definition.
     * @return The name text component of this definition.
     */
    public Component name()
    {
        return Component.translatable(Schism.NAMESPACE + "." + this.type() + "." + this.subject());
    }

    /**
     * Gets a description text component for this definition.
     * @return The description text component of this definition.
     */
    public Component description()
    {
        return Component.translatable("description");
    }

    /**
     * Gets a description text component for this definition.
     * @param type The type of description to get.
     * @return The description text component of this definition.
     */
    public Component description(String type)
    {
        return Component.translatable(Schism.NAMESPACE + "." + this.type() + "." + this.subject() + "." + type);
    }

    /**
     * Returns a collection of text components that make up a descriptive summary of this definition.
     * @return A collection of text components that make up this definition's subject summary.
     */
    public List<Component> summary()
    {
        List<Component> summary = new ArrayList<>();
        summary.add(this.name());
        summary.add(this.description().copy().withStyle(ChatFormatting.GREEN));
        return summary;
    }

    /**
     * Gets an optional icon resource location that this definition can display in guis.
     * @return An optional icon texture resource.
     */
    public Optional<ResourceLocation> icon()
    {
        return Optional.of(new ResourceLocation(Schism.NAMESPACE, "textures/gui/" + this.type() + "/" + this.subject() + ".png"));
    }

    /**
     * Gets a resource location for this definition.
     * @return A generated Resource Location that objects using this definition can go by.
     */
    public ResourceLocation resourceLocation()
    {
        if (this.resourceLocation == null) {
            this.resourceLocation = new ResourceLocation(Schism.NAMESPACE, this.subject());
        }
        return this.resourceLocation;
    }

    /**
     * Updates this definition's root entry, called on creation and by a repository when the database is reloaded.
     * @param dataStore The new root data store to get data from.
     */
    public void update(DataStore dataStore)
    {
        this.dataStore = dataStore;
    }

    /**
     * Sends a notification about this definition.
     * @param event The event to notify about.
     * @param value Additional text to include, this does not get translated.
     * @param serverPlayer The player to notify.
     */
    public void sendNotification(String event, String value, ServerPlayer serverPlayer)
    {
        MessageRepository.get().toPlayer(new NotificationMessage(this, event, value), serverPlayer);
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + "(" + this.subject + ")";
    }

    @Override
    public int compareTo(@NotNull AbstractDefinition definition)
    {
        return this.name().getString().compareTo(definition.name().getString());
    }
}

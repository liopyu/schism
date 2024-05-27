package com.schism.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandRepository
{
    private static final CommandRepository INSTANCE = new CommandRepository();

    protected final List<AbstractCommand> commands = new ArrayList<>();

    /**
     * Gets the singleton CommandRepository instance.
     * @return The Command Repository which registers commands.
     */
    public static CommandRepository get()
    {
        return INSTANCE;
    }

    /**
     * The Command Repository registers and manages all commands.
     */
    public CommandRepository()
    {
        this.add(new AltarCommand());
        this.add(new ReloadCommand());
        this.add(new TrackCommand());
        this.add(new LevelCommand());
        this.add(new ExperienceCommand());
        this.add(new WorshipCommand());
        this.add(new NotifyCommand());
    }

    /**
     * Adds a Command to this repository.
     * @param command The command to add.
     */
    public void add(AbstractCommand command)
    {
        this.commands.add(command);
    }

    /**
     * Registers commands.
     * @param event The command registry event.
     */
    @SubscribeEvent
    public void register(final RegisterCommandsEvent event)
    {
        LiteralArgumentBuilder<CommandSourceStack> longLiteral = Commands.literal("schism");
        LiteralArgumentBuilder<CommandSourceStack> shortLiteral = Commands.literal("sc");
        this.commands.forEach(command -> {
            longLiteral.then(command.register());
            shortLiteral.then(command.register());
        });
        event.getDispatcher().register(longLiteral);
        event.getDispatcher().register(shortLiteral);
    }
}

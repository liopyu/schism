package com.schism.core.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.schism.core.network.MessageRepository;
import com.schism.core.network.messages.CommandMessage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.Supplier;

public class TrackCommand extends AbstractCommand
{
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> register()
    {
        LiteralArgumentBuilder<CommandSourceStack> track = Commands.literal("track").requires((stack) -> stack.hasPermission(3));
        track.executes(this::track);
        track.then(Commands.literal("self").executes(this::self));
        track.then(Commands.literal("clear").executes(this::clear));
        return track;
    }

    /**
     * Attempts to target a creature with a tracking debug overlay.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int track(CommandContext<CommandSourceStack> context)
    {
        Entity entity = context.getSource().getEntity();
        if (!(entity instanceof ServerPlayer serverPlayer)) {
            return 1;
        }

        MessageRepository.get().toPlayer(new CommandMessage(CommandMessage.CommandId.CREATURE_TRACK.id), serverPlayer);
        context.getSource().sendSuccess((Supplier<Component>) Component.translatable("schism.command.track.success"), true);
        return 0;
    }

    /**
     * Attempts to target the player with a tracking debug overlay.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int self(CommandContext<CommandSourceStack> context)
    {
        Entity entity = context.getSource().getEntity();
        if (!(entity instanceof ServerPlayer serverPlayer)) {
            return 1;
        }

        MessageRepository.get().toPlayer(new CommandMessage(CommandMessage.CommandId.CREATURE_TRACK_SELF.id), serverPlayer);
        context.getSource().sendSuccess((Supplier<Component>) Component.translatable("schism.command.track.self.success"), true);
        return 0;
    }

    /**
     * Attempts to target the player with a tracking debug overlay.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int clear(CommandContext<CommandSourceStack> context)
    {
        Entity entity = context.getSource().getEntity();
        if (!(entity instanceof ServerPlayer serverPlayer)) {
            return 1;
        }

        MessageRepository.get().toPlayer(new CommandMessage(CommandMessage.CommandId.CREATURE_TRACK_CLEAR.id), serverPlayer);
        context.getSource().sendSuccess((Supplier<Component>) Component.translatable("schism.command.track.clear.success"), true);
        return 0;
    }
}

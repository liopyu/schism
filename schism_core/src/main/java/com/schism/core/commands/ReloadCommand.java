package com.schism.core.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.schism.core.database.Database;
import com.schism.core.network.MessageRepository;
import com.schism.core.network.messages.CommandMessage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.Supplier;

public class ReloadCommand extends AbstractCommand
{
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> register()
    {
        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload").requires((stack) -> stack.hasPermission(3));
        reload.executes(this::reload);
        reload.then(Commands.literal("models").executes(this::reloadModels));
        return reload;
    }

    /**
     * Reloads the database updating all definitions and loading in new ones if able.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int reload(CommandContext<CommandSourceStack> context)
    {
        Database.get().loadJson();
        context.getSource().sendSuccess((Supplier<Component>) Component.translatable("schism.command.reload.success"), true);
        return 0;
    }

    /**
     * Reloads the models including armatures, also clears model textures, etc.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int reloadModels(CommandContext<CommandSourceStack> context)
    {
        Entity entity = context.getSource().getEntity();
        if (!(entity instanceof ServerPlayer serverPlayer)) {
            return 1;
        }

        MessageRepository.get().toPlayer(new CommandMessage(CommandMessage.CommandId.RELOAD_MODELS.id), serverPlayer);

        return 0;
    }
}

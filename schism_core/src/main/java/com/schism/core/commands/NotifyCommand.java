package com.schism.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.schism.core.AbstractRepository;
import com.schism.core.database.AbstractDefinition;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.Supplier;

public class NotifyCommand extends AbstractCommand
{
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> register()
    {
        LiteralArgumentBuilder<CommandSourceStack> notify = Commands.literal("notify").requires((stack) -> stack.hasPermission(3));
        notify.then(Commands.argument("type", StringArgumentType.string())
                .then(Commands.argument("subject", StringArgumentType.string())
                        .then(Commands.argument("event", StringArgumentType.string())
                                .then(Commands.argument("value", StringArgumentType.string()).executes(this::notify))
                        )
                )
        );
        return notify;
    }

    /**
     * Attempts to target a creature with a tracking debug overlay.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int notify(CommandContext<CommandSourceStack> context)
    {
        Entity entity = context.getSource().getEntity();
        if (!(entity instanceof ServerPlayer serverPlayer)) {
            return 1;
        }

        String type = StringArgumentType.getString(context, "type");
        String subject = StringArgumentType.getString(context, "subject");
        String event = StringArgumentType.getString(context, "event");
        String value = StringArgumentType.getString(context, "value");

        AbstractDefinition definition = AbstractRepository.fromType(type).flatMap(repository -> repository.getDefinition(subject)).orElse(null);
        if (definition == null) {
            context.getSource().sendFailure(Component.translatable("schism.command.notify.fail"));
            return 1;
        }

        definition.sendNotification(event, value, serverPlayer);
        context.getSource().sendSuccess((Supplier<Component>) Component.translatable("schism.command.notify.success"), true);
        return 0;
    }
}

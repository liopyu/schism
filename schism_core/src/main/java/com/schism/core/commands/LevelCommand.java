package com.schism.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.schism.core.creatures.Creatures;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class LevelCommand extends AbstractCommand
{
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> register()
    {
        LiteralArgumentBuilder<CommandSourceStack> level = Commands.literal("level").requires((stack) -> stack.hasPermission(3));
        level.then(Commands.literal("add")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(this::levelAdd)
                        .then(Commands.argument("targets", EntityArgument.entities()).executes(this::levelAddTargets))
                )
        );
        level.then(Commands.literal("set")
                .then(Commands.argument("level", IntegerArgumentType.integer(1)).executes(this::levelSet)
                        .then(Commands.argument("targets", EntityArgument.entities()).executes(this::levelSetTargets))
                )
        );
        return level;
    }

    /**
     * Adds levels to a creature.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int levelAdd(CommandContext<CommandSourceStack> context)
    {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        List<Entity> entities = new ArrayList<>();
        entities.add(context.getSource().getEntity());
        return this.levelAdd(context, amount, entities);
    }

    /**
     * Adds levels to a creature.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int levelAddTargets(CommandContext<CommandSourceStack> context)
    {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        Collection<? extends Entity> entities;
        try {
            entities = EntityArgument.getEntities(context, "targets");
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.translatable("schism.command.level.add.fail.targets"));
            return 1;
        }
        return this.levelAdd(context, amount, entities);
    }

    /**
     * Adds levels to a creature.
     * @param context The command context.
     * @param levels The amount of levels to add.
     * @param entities The entities to add levels to.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int levelAdd(CommandContext<CommandSourceStack> context, int levels, Collection<? extends Entity> entities)
    {
        if (!(context.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            context.getSource().sendFailure(Component.translatable("schism.command.level.add.fail.player"));
            return 1;
        }

        entities.stream().filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast)
                .forEach(livingEntity -> Creatures.get().getCreature(livingEntity).ifPresent(creature -> creature.stats().addLevels(levels)));
        context.getSource().sendSuccess((Supplier<Component>) Component.translatable("schism.command.level.add.success"), true);
        return 0;
    }

    /**
     * Adds levels to a creature.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int levelSet(CommandContext<CommandSourceStack> context)
    {
        int level = IntegerArgumentType.getInteger(context, "level");
        List<Entity> entities = new ArrayList<>();
        entities.add(context.getSource().getEntity());
        return this.levelSet(context, level, entities);
    }

    /**
     * Adds levels to a creature.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int levelSetTargets(CommandContext<CommandSourceStack> context)
    {
        int level = IntegerArgumentType.getInteger(context, "level");
        Collection<? extends Entity> entities;
        try {
            entities = EntityArgument.getEntities(context, "targets");
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.translatable("schism.command.level.set.fail.targets"));
            return 1;
        }
        return this.levelSet(context, level, entities);
    }

    /**
     * Sets a creature level.
     * @param context The command context.
     * @param level The level to set to.
     * @param entities The entities to add levels to.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int levelSet(CommandContext<CommandSourceStack> context, int level, Collection<? extends Entity> entities)
    {
        if (!(context.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            context.getSource().sendFailure(Component.translatable("schism.command.level.set.fail.player"));
            return 1;
        }

        entities.stream().filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast)
                .forEach(livingEntity -> Creatures.get().getCreature(livingEntity).ifPresent(creature -> creature.stats().setLevel(level)));
        context.getSource().sendSuccess((Supplier<Component>) Component.translatable("schism.command.level.set.success"), true);
        return 0;
    }
}

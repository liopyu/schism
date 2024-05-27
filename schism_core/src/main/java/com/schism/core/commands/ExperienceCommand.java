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

public class ExperienceCommand extends AbstractCommand
{
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> register()
    {
        LiteralArgumentBuilder<CommandSourceStack> experience = Commands.literal("experience").requires((stack) -> stack.hasPermission(3));
        experience.then(Commands.literal("add")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(this::experienceAdd)
                        .then(Commands.argument("targets", EntityArgument.entities()).executes(this::experienceAddTargets))
                )
        );
        return experience;
    }

    /**
     * Adds experience to a creature.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int experienceAdd(CommandContext<CommandSourceStack> context)
    {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        List<Entity> entities = new ArrayList<>();
        entities.add(context.getSource().getEntity());
        return this.experienceAdd(context, amount, entities);
    }

    /**
     * Adds experience to a creature.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int experienceAddTargets(CommandContext<CommandSourceStack> context)
    {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        Collection<? extends Entity> entities;
        try {
            entities = EntityArgument.getEntities(context, "targets");
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.translatable("schism.command.experience.add.fail.targets"));
            return 1;
        }
        return this.experienceAdd(context, amount, entities);
    }

    /**
     * Adds experience to a creature.
     * @param context The command context.
     * @param amount The amount of experience to add.
     * @param entities The entities to add experience to.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int experienceAdd(CommandContext<CommandSourceStack> context, int amount, Collection<? extends Entity> entities)
    {
        if (!(context.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            context.getSource().sendFailure(Component.translatable("schism.command.experience.add.fail.player"));
            return 1;
        }

        entities.stream().filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast)
                .forEach(livingEntity -> Creatures.get().getCreature(livingEntity).ifPresent(creature -> creature.stats().addExperience(amount)));
        context.getSource().sendSuccess((Supplier<Component>) Component.translatable("schism.command.experience.add.success"), true);
        return 0;
    }
}

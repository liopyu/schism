package com.schism.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.schism.core.creatures.Creatures;
import com.schism.core.gods.God;
import com.schism.core.gods.GodDefinition;
import com.schism.core.gods.GodRepository;
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

public class WorshipCommand extends AbstractCommand
{
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> register()
    {
        LiteralArgumentBuilder<CommandSourceStack> worship = Commands.literal("worship").requires((stack) -> stack.hasPermission(3));
        worship.then(Commands.argument("add", StringArgumentType.string())
                .then(Commands.argument("god", StringArgumentType.string()).executes(this::worshipAdd)
                        .then(Commands.argument("devotion", IntegerArgumentType.integer(1)).executes(this::worshipAddDevotion)
                                .then(Commands.argument("targets", EntityArgument.entities()).executes(this::worshipAddDevotionTargets))
                        )
                )
        );
        worship.then(Commands.argument("remove", StringArgumentType.string())
                .then(Commands.argument("god", StringArgumentType.string()).executes(this::worshipRemove)
                        .then(Commands.argument("targets", EntityArgument.entities()).executes(this::worshipRemoveDevotionTargets))
                )
        );
        return worship;
    }

    /**
     * Adds worship to a creature.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int worshipAdd(CommandContext<CommandSourceStack> context)
    {
        List<Entity> entities = new ArrayList<>();
        entities.add(context.getSource().getEntity());
        return this.worshipAdd(context, 50, entities);
    }

    /**
     * Adds worship to a creature.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int worshipAddDevotion(CommandContext<CommandSourceStack> context)
    {
        int devotion = IntegerArgumentType.getInteger(context, "devotion");
        List<Entity> entities = new ArrayList<>();
        entities.add(context.getSource().getEntity());
        return this.worshipAdd(context, devotion, entities);
    }

    /**
     * Adds worship to a creature.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int worshipAddDevotionTargets(CommandContext<CommandSourceStack> context)
    {
        int devotion = IntegerArgumentType.getInteger(context, "devotion");
        Collection<? extends Entity> entities;
        try {
            entities = EntityArgument.getEntities(context, "targets");
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.translatable("schism.command.worship.add.fail.targets"));
            return 1;
        }
        return this.worshipAdd(context, devotion, entities);
    }

    /**
     * Adds worship to a creature.
     * @param context The command context.
     * @param devotion The amount of devotion to set.
     * @param entities The entities to apply the worship to.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int worshipAdd(CommandContext<CommandSourceStack> context, int devotion, Collection<? extends Entity> entities)
    {
        if (!(context.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            context.getSource().sendFailure(Component.translatable("schism.command.worship.add.fail.player"));
            return 1;
        }

        String godSubject = StringArgumentType.getString(context, "god");
        God god = GodRepository.get().getDefinition(godSubject).map(GodDefinition::god).orElse(null);
        if (god == null) {
            context.getSource().sendFailure(Component.translatable("schism.command.worship.add.fail.god"));
            return 1;
        }

        entities.stream().filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast).forEach(livingEntity -> {
            Creatures.get().getCreature(livingEntity).ifPresent(creature -> creature.piety().addDevotion(godSubject, devotion));
        });
        context.getSource().sendSuccess((Supplier<Component>) Component.translatable("schism.command.worship.add.success"), true);
        return 0;
    }

    /**
     * Removes worship from a creature.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int worshipRemove(CommandContext<CommandSourceStack> context)
    {
        List<Entity> entities = new ArrayList<>();
        entities.add(context.getSource().getEntity());
        return this.worshipRemove(context, entities);
    }

    /**
     * Removes worship from a creature.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int worshipRemoveDevotionTargets(CommandContext<CommandSourceStack> context)
    {
        Collection<? extends Entity> entities;
        try {
            entities = EntityArgument.getEntities(context, "targets");
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.translatable("schism.command.worship.remove.fail.targets"));
            return 1;
        }
        return this.worshipRemove(context, entities);
    }

    /**
     * Removes worship from a creature.
     * @param context The command context.
     * @param entities The entities to apply the worship to.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int worshipRemove(CommandContext<CommandSourceStack> context, Collection<? extends Entity> entities)
    {
        if (!(context.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            context.getSource().sendFailure(Component.translatable("schism.command.worship.remove.fail.player"));
            return 1;
        }

        String godSubject = StringArgumentType.getString(context, "god");
        God god = GodRepository.get().getDefinition(godSubject).map(GodDefinition::god).orElse(null);
        if (god == null) {
            context.getSource().sendFailure(Component.translatable("schism.command.worship.remove.fail.god"));
            return 1;
        }

        entities.stream().filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast).forEach(livingEntity -> {
            Creatures.get().getCreature(livingEntity).ifPresent(creature -> creature.piety().removeWorship(godSubject));
        });
        context.getSource().sendSuccess((Supplier<Component>) Component.translatable("schism.command.worship.remove.success"), true);
        return 0;
    }
}

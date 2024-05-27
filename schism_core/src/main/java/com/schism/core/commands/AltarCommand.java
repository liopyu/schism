package com.schism.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.schism.core.altars.AltarDefinition;
import com.schism.core.altars.AltarRepository;
import com.schism.core.altars.Altars;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.server.command.EnumArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class AltarCommand extends AbstractCommand
{
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> register()
    {
        LiteralArgumentBuilder<CommandSourceStack> altar = Commands.literal("altar").requires((stack) -> stack.hasPermission(3));
        altar.then(Commands.literal("check")
                .then(Commands.argument("altar", StringArgumentType.string())
                        .then(Commands.argument("area", IntegerArgumentType.integer(0, 40)).executes(this::checkAltar))
                )
        );
        altar.then(Commands.literal("build")
                .then(Commands.argument("altar", StringArgumentType.string())
                        .then(Commands.argument("rotation", EnumArgument.enumArgument(Rotation.class))
                            .then(Commands.argument("offset", BlockPosArgument.blockPos()).executes(this::buildAltar))
                        )
                )
        );
        return altar;
    }

    /**
     * Checks if the provided altar is present.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int checkAltar(CommandContext<CommandSourceStack> context)
    {
        if (!(context.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            context.getSource().sendFailure(Component.translatable("schism.command.altar.check.fail.player"));
            return 1;
        }

        List<String> altarSubjects = new ArrayList<>();
        altarSubjects.add(StringArgumentType.getString(context, "altar"));
        int area = IntegerArgumentType.getInteger(context, "area");

        Optional<AltarDefinition> optionalAltar = Altars.get().search(serverPlayer.getLevel(), serverPlayer.blockPosition(), area, altarSubjects);
        if (optionalAltar.isEmpty()) {
            context.getSource().sendSuccess((Supplier<Component>) Component.translatable("schism.command.altar.check.missing"), true);
        } else {
            context.getSource().sendSuccess((Supplier<Component>) Component.translatable("schism.command.altar.check.found"), true);
        }

        return 0;
    }

    /**
     * Builds the provided altar.
     * @param context The command context.
     * @return The command status code where 0 is successful and other numbers an error id.
     */
    private int buildAltar(CommandContext<CommandSourceStack> context)
    {
        if (!(context.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            context.getSource().sendFailure(Component.translatable("schism.command.altar.build.fail.player"));
            return 1;
        }

        BlockPos position = null;
        try {
            position = BlockPosArgument.getLoadedBlockPos(context, "offset");
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.translatable("schism.command.altar.build.fail.position"));
            return 2;
        }

        Rotation rotation = context.getArgument("rotation", Rotation.class);

        AltarDefinition altarDefinition = AltarRepository.get().getDefinition(StringArgumentType.getString(context, "altar")).orElse(null);
        if (altarDefinition == null) {
            context.getSource().sendFailure(Component.translatable("schism.command.altar.build.fail.altar"));
            return 3;
        }

        try {
            Altars.get().build(serverPlayer.getLevel(), position, rotation, altarDefinition);
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendFailure(Component.translatable("schism.command.altar.build.fail.altar"));
            return 4;
        }
        context.getSource().sendSuccess((Supplier<Component>) Component.translatable("schism.command.altar.build.success"), true);

        return 0;
    }
}

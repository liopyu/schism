package com.lycanitesmobs.core.command;

import com.lycanitesmobs.core.spawner.SpawnerManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class SpawnerCommand {
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("spawner")
				.then(Commands.literal("test")
						.then(Commands.argument("spawner", StringArgumentType.string())
								.then(Commands.argument("level", IntegerArgumentType.integer()).executes(SpawnerCommand::test))))
				.then(Commands.literal("lighttest").executes(SpawnerCommand::lightTest));
	}

	public static int test(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		String spawnerName = StringArgumentType.getString(context, "spawner");
		int level = Math.max(1, IntegerArgumentType.getInteger(context, "level"));
		if(!SpawnerManager.getInstance().spawners.containsKey(spawnerName)) {
			context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.spawner.test.unknown"), true);
			return 0;
		}

		World world = context.getSource().getLevel();
		PlayerEntity player = null;
		BlockPos pos = BlockPos.ZERO;
		if(context.getSource().getEntity() instanceof PlayerEntity) {
			player = (PlayerEntity)context.getSource().getEntity();
			pos = new BlockPos(player.position());
		}

		SpawnerManager.getInstance().spawners.get(spawnerName).trigger(world, player, null, pos, level, 1, 0);
		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.spawner.test"), true);
		return 0;
	}

	public static int lightTest(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		if(context.getSource().getEntity() == null) {
			return 0;
		}
		int level = context.getSource().getLevel().getMaxLocalRawBrightness(context.getSource().getEntity().blockPosition());
		String results = " Level: " + level;
		if(level <= 1) {
			context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.spawner.lighttest.dark").append(results), true);
		}
		else {
			context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.spawner.lighttest.light").append(results), true);
		}
		return 0;
	}
}

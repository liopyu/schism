package com.lycanitesmobs.core.command;

import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.core.config.ConfigDungeons;
import com.lycanitesmobs.core.dungeon.DungeonManager;
import com.lycanitesmobs.core.dungeon.instance.DungeonInstance;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

public class DungeonsCommand {
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("dungeons")
				.then(Commands.literal("reload").executes(DungeonsCommand::reload))
				.then(Commands.literal("enable").executes(DungeonsCommand::enable))
				.then(Commands.literal("disable").executes(DungeonsCommand::disable))
				.then(Commands.literal("locate").executes(DungeonsCommand::locate));
	}

	public static int reload(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		DungeonManager.getInstance().reload();
		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.dungeons.reload"), true);
		return 0;
	}

	public static int enable(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		ConfigDungeons.INSTANCE.dungeonsEnabled.set(true);
		ConfigDungeons.INSTANCE.dungeonsEnabled.save();
		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.dungeons.enable"), true);
		return 0;
	}

	public static int disable(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		ConfigDungeons.INSTANCE.dungeonsEnabled.set(false);
		ConfigDungeons.INSTANCE.dungeonsEnabled.save();
		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.dungeons.disable"), true);
		return 0;
	}

	public static int locate(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.dungeons.locate"), true);
		World world = context.getSource().getLevel();
		ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
		List<DungeonInstance> nearbyDungeons = extendedWorld.getNearbyDungeonInstances(new ChunkPos(new BlockPos(context.getSource().getPosition())), ConfigDungeons.INSTANCE.dungeonDistance.get() * 2);
		if(nearbyDungeons.isEmpty()) {
			context.getSource().sendSuccess(new TranslationTextComponent("common.none"), true);
			return 0;
		}
		for(DungeonInstance dungeonInstance : nearbyDungeons) {
			context.getSource().sendSuccess(new StringTextComponent(dungeonInstance.toString()), true);
		}
		return 0;
	}
}

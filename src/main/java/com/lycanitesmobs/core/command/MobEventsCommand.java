package com.lycanitesmobs.core.command;

import com.lycanitesmobs.core.config.ConfigMobEvent;
import com.lycanitesmobs.core.mobevent.MobEvent;
import com.lycanitesmobs.core.mobevent.MobEventManager;
import com.lycanitesmobs.core.mobevent.MobEventPlayerServer;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class MobEventsCommand {
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("mobevents")
				.then(Commands.literal("reload").executes(MobEventsCommand::reload))
				.then(Commands.literal("enable").executes(MobEventsCommand::enable))
				.then(Commands.literal("disable").executes(MobEventsCommand::disable))
				.then(Commands.literal("creative")
						.then(Commands.literal("enable").executes(MobEventsCommand::creativeEnable))
						.then(Commands.literal("disable").executes(MobEventsCommand::creativeDisable)))
				.then(Commands.literal("list").executes(MobEventsCommand::list));
	}

	public static int reload(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		MobEventManager.getInstance().reload();
		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.mobevents.reload"), true);
		return 0;
	}

	public static int enable(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		MobEventManager.getInstance().mobEventsEnabled = true;
		ConfigMobEvent.INSTANCE.mobEventsEnabled.set(true);
		ConfigMobEvent.INSTANCE.mobEventsEnabled.save();
		MobEventManager.getInstance().mobEventsRandom = true;
		ConfigMobEvent.INSTANCE.mobEventsRandom.set(true);
		ConfigMobEvent.INSTANCE.mobEventsRandom.save();
		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.mobevents.enable"), true);
		return 0;
	}

	public static int disable(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		MobEventManager.getInstance().mobEventsRandom = false;
		ConfigMobEvent.INSTANCE.mobEventsRandom.set(false);
		ConfigMobEvent.INSTANCE.mobEventsRandom.save();
		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.mobevents.disable"), true);
		return 0;
	}

	public static int creativeEnable(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		MobEventPlayerServer.testOnCreative = true;
		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.mobevents.creative.enable"), true);
		return 0;
	}

	public static int creativeDisable(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		MobEventPlayerServer.testOnCreative = false;
		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.mobevents.creative.disable"), true);
		return 0;
	}

	public static int list(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.mobevents.list"), true);
		for(MobEvent mobEvent : MobEventManager.getInstance().mobEvents.values()) {
			String eventName = mobEvent.name + " (" + mobEvent.getTitle().getString() + ")";
			context.getSource().sendSuccess(new StringTextComponent(eventName), true);
		}
		return 0;
	}
}

package com.lycanitesmobs.core.command;

import com.lycanitesmobs.core.info.CreatureManager;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

public class CreaturesCommand {
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("creatures")
				.then(Commands.literal("reload").executes(CreaturesCommand::reload));
	}

	public static int reload(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		CreatureManager.getInstance().reload();
		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.creatures.reload"), true);
		return 0;
	}
}

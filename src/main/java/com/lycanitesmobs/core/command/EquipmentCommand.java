package com.lycanitesmobs.core.command;

import com.lycanitesmobs.core.item.equipment.EquipmentPartManager;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

public class EquipmentCommand {
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("equipment")
				.then(Commands.literal("reload").executes(EquipmentCommand::reload));
	}

	public static int reload(final CommandContext<CommandSource> context) {
		if (!context.getSource().hasPermission(2)) {
			return 0;
		}
		EquipmentPartManager.getInstance().reload();
		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.equipment.reload"), true);
		return 0;
	}
}

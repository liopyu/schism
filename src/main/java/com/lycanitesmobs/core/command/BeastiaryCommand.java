package com.lycanitesmobs.core.command;

import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.Beastiary;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureKnowledge;
import com.lycanitesmobs.core.info.CreatureManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class BeastiaryCommand {
	public static ArgumentBuilder<CommandSource, ?> register() {
		return Commands.literal("beastiary")
				.then(Commands.literal("complete").then(Commands.argument("rank", IntegerArgumentType.integer()).executes(BeastiaryCommand::complete)))
				.then(Commands.literal("clear").executes(BeastiaryCommand::clear))
				.then(Commands.literal("add").then(Commands.argument("creature", StringArgumentType.string()).then(Commands.argument("rank", IntegerArgumentType.integer()).executes(BeastiaryCommand::add))));
	}

	public static int complete(final CommandContext<CommandSource> context) {
		if(!(context.getSource().getEntity() instanceof PlayerEntity) || !context.getSource().hasPermission(2)) {
			return 0;
		}

		int rank = Math.max(0, Math.min(3, IntegerArgumentType.getInteger(context, "rank")));
		ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer((PlayerEntity)context.getSource().getEntity());
		if(extendedPlayer == null) {
			return 0;
		}

		Beastiary beastiary = extendedPlayer.getBeastiary();
		for(CreatureInfo creatureInfo : CreatureManager.getInstance().creatures.values()) {
			beastiary.addCreatureKnowledge(new CreatureKnowledge(beastiary, creatureInfo.getName(), rank, 0), true);
		}
		beastiary.sendAllToClient();

		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.beastiary.complete"), true);
		return 0;
	}

	public static int clear(final CommandContext<CommandSource> context) {
		if(!(context.getSource().getEntity() instanceof PlayerEntity) || !context.getSource().hasPermission(2)) {
			return 0;
		}

		ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer((PlayerEntity)context.getSource().getEntity());
		if(extendedPlayer == null) {
			return 0;
		}

		Beastiary beastiary = extendedPlayer.getBeastiary();
		beastiary.creatureKnowledgeList.clear();
		beastiary.sendAllToClient();

		context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.beastiary.clear"), true);
		return 0;
	}

	public static int add(final CommandContext<CommandSource> context) {
		if(!(context.getSource().getEntity() instanceof PlayerEntity) || !context.getSource().hasPermission(2)) {
			return 0;
		}

		int rank = Math.max(0, Math.min(3, IntegerArgumentType.getInteger(context, "rank")));
		String creatureName = StringArgumentType.getString(context, "creature");
		ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer((PlayerEntity)context.getSource().getEntity());
		if(extendedPlayer == null) {
			return 0;
		}

		Beastiary beastiary = extendedPlayer.getBeastiary();
		CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(creatureName);
		if(creatureInfo == null) {
			context.getSource().sendSuccess(new TranslationTextComponent("lyc.command.beastiary.add.unknown"), true);
			return 0;
		}

		CreatureKnowledge creatureKnowledge = new CreatureKnowledge(beastiary, creatureInfo.getName(), rank, 0);
		if(beastiary.addCreatureKnowledge(creatureKnowledge, true)) {
			beastiary.sendAddedMessage(creatureKnowledge);
		}
		else {
			beastiary.sendKnownMessage(creatureKnowledge);
		}
		return 0;
	}
}

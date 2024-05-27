package com.schism.core.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public abstract class AbstractCommand
{
    public abstract ArgumentBuilder<CommandSourceStack, ?> register();
}

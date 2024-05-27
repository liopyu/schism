package com.schism.core.creatures;

import net.minecraft.server.level.ServerPlayer;

public interface ICanSync
{
    /**
     * Sends a network paket to sync with player clients in the same level. Safely ignored when called client side.
     */
    public abstract void syncClients();

    /**
     * Sends a network paket to sync with player clients. Safely ignored when called client side.
     * @param serverPlayer The player to sync to.
     */
    public abstract void syncClient(ServerPlayer serverPlayer);
}

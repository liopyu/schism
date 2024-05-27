package com.schism.core.projectiles.actions;

import com.schism.core.database.registryobjects.BlockRegistryObject;
import com.schism.core.database.DataStore;
import com.schism.core.projectiles.ProjectileDefinition;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectProjectileAction extends AbstractProjectileAction
{
    protected final BlockRegistryObject<MobEffect> cachedEffect;
    protected final int ticks;
    protected final int level;
    protected final float splashRadius;
    protected final boolean onImpact;
    protected final boolean onPierceTick;

    public EffectProjectileAction(ProjectileDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedEffect = new BlockRegistryObject<>(dataStore.stringProp("effect_id"), () -> ForgeRegistries.MOB_EFFECTS);
        this.ticks = dataStore.intProp("ticks");
        this.level = dataStore.intProp("level");
        this.splashRadius = dataStore.floatProp("splash_radius");
        this.onImpact = dataStore.booleanProp("on_impact");
        this.onPierceTick = dataStore.booleanProp("on_pierce_tick");
    }
}

package com.schism.core.projectiles.actions;

import com.schism.core.database.CachedDefinition;
import com.schism.core.database.CachedRegistryObject;
import com.schism.core.database.DataStore;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.elements.ElementRepository;
import com.schism.core.projectiles.ProjectileDefinition;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectProjectileAction extends AbstractProjectileAction
{
    protected final CachedRegistryObject<MobEffect> cachedEffect;
    protected final int ticks;
    protected final int level;
    protected final float splashRadius;
    protected final boolean onImpact;
    protected final boolean onPierceTick;

    public EffectProjectileAction(ProjectileDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedEffect = new CachedRegistryObject<>(dataStore.stringProp("effect_id"), () -> ForgeRegistries.MOB_EFFECTS);
        this.ticks = dataStore.intProp("ticks");
        this.level = dataStore.intProp("level");
        this.splashRadius = dataStore.floatProp("splash_radius");
        this.onImpact = dataStore.booleanProp("on_impact");
        this.onPierceTick = dataStore.booleanProp("on_pierce_tick");
    }
}

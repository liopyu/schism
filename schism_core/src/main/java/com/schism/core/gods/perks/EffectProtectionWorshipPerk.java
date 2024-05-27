package com.schism.core.gods.perks;

import com.schism.core.database.CachedRegistryObject;
import com.schism.core.database.DataStore;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectProtectionWorshipPerk extends AbstractWorshipPerk
{
    protected final CachedRegistryObject<MobEffect> cachedEffect;

    public EffectProtectionWorshipPerk(DataStore dataStore)
    {
        super(dataStore);

        this.cachedEffect = new CachedRegistryObject<>(dataStore.stringProp("effect_id"), () -> ForgeRegistries.MOB_EFFECTS);
    }

    @Override
    public boolean effectApplicable(MobEffectInstance effectInstance)
    {
        if (!this.cachedEffect.isPresent()) {
            return true;
        }
        return effectInstance.getEffect() != this.cachedEffect.get();
    }
}

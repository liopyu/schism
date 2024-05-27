package com.schism.core.items.actions;

import com.schism.core.database.registryobjects.BlockRegistryObject;
import com.schism.core.database.DataStore;
import com.schism.core.items.ItemDefinition;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectItemAction extends AbstractItemAction
{
    protected final BlockRegistryObject<MobEffect> cachedEffect;
    protected final int level;
    protected final int duration;

    public EffectItemAction(ItemDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedEffect = new BlockRegistryObject<>(dataStore.stringProp("effect_id"), () -> ForgeRegistries.MOB_EFFECTS);
        this.level = dataStore.intProp("level");
        this.duration = dataStore.intProp("duration");
    }

    @Override
    public InteractionResultHolder<ItemStack> onUse(ItemStack itemStack, LivingEntity userEntity, Entity targetEntity)
    {
        if (!this.cachedEffect.isPresent() || !(targetEntity instanceof LivingEntity targetLivingEntity)) {
            return InteractionResultHolder.fail(itemStack);
        }
        MobEffectInstance effectInstance = new MobEffectInstance(this.cachedEffect.get(), this.duration, Math.max(this.level - 1, 0));
        if (!targetLivingEntity.addEffect(effectInstance, userEntity)) {
            return InteractionResultHolder.fail(itemStack);
        }
        return InteractionResultHolder.success(itemStack);
    }
}

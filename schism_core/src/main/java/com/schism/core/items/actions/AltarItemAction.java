package com.schism.core.items.actions;

import com.schism.core.altars.AltarDefinition;
import com.schism.core.altars.Altars;
import com.schism.core.database.registryobjects.BlockRegistryObject;
import com.schism.core.database.DataStore;
import com.schism.core.items.ItemDefinition;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class AltarItemAction extends AbstractItemAction
{
    protected final BlockRegistryObject<MobEffect> cachedEffect;
    protected final String ritual;
    protected final int area;
    protected final List<String> altars;

    public AltarItemAction(ItemDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedEffect = new BlockRegistryObject<>(dataStore.stringProp("effect_id"), () -> ForgeRegistries.MOB_EFFECTS);
        this.ritual = dataStore.stringProp("ritual");
        this.area = dataStore.intProp("area");
        this.altars = dataStore.listProp("altars").stream().map(DataStore::stringValue).toList();
    }

    @Override
    public InteractionResultHolder<ItemStack> onUse(ItemStack itemStack, LivingEntity userEntity, Entity targetEntity)
    {
        AltarDefinition altar = Altars.get().search(userEntity.getLevel(), userEntity.blockPosition(), this.area, this.altars).orElse(null);
        if (altar == null) {
            return InteractionResultHolder.fail(itemStack);
        }
        altar.activate(userEntity, userEntity.blockPosition(), this.ritual, targetEntity);
        return InteractionResultHolder.success(itemStack);
    }
}

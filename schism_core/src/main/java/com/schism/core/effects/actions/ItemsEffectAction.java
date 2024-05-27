package com.schism.core.effects.actions;

import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

public class ItemsEffectAction extends AbstractEffectAction
{
    protected final boolean preventAny;
    protected final boolean preventFood;
    protected final boolean preventPotions;
    protected final boolean preventEquipment;

    public ItemsEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.preventAny = dataStore.booleanProp("prevent_any");
        this.preventFood = dataStore.booleanProp("prevent_food");
        this.preventPotions = dataStore.booleanProp("prevent_potions");
        this.preventEquipment = dataStore.booleanProp("prevent_equipment");
    }

    @Override
    public boolean item(MobEffectInstance effectInstance, LivingEntity livingEntity, ItemStack itemStack, int duration)
    {
        if (this.preventAny) {
            return false;
        }

        Item item = itemStack.getItem();
        if (this.preventFood && item.getFoodProperties() != null) {
            return false;
        }
        if (this.preventPotions && item instanceof PotionItem) {
            return false;
        }
        if (this.preventEquipment && (item instanceof TieredItem || item instanceof ProjectileWeaponItem)) {
            return false;
        }

        return true;
    }
}

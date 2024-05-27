package com.schism.core.effects;

import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.DataStore;
import com.schism.core.database.conditions.AbstractCondition;
import com.schism.core.effects.actions.AbstractEffectAction;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

public class EffectDefinition extends AbstractDefinition
{
    protected String group;
    protected int color;
    protected boolean beneficial;
    protected boolean detrimental;
    protected boolean persistent;
    protected List<AbstractCondition> conditions;
    protected List<AbstractEffectAction> actions;

    /**
     * Block definitions hold information about status effects.
     * @param subject The subject of this definition, this should be unique for a specific object like a block name, theme name, config category etc.
     * @param dataStore The data store that holds config data about this definition.
     */
    public EffectDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);

        this.group = dataStore.stringProp("group");
        try {
            this.color = Integer.decode(dataStore.stringProp("color"));
        } catch (Exception e) {
            throw new RuntimeException("Invalid color for the effect: " + this.subject());
        }
        this.beneficial = dataStore.booleanProp("beneficial");
        this.detrimental = dataStore.booleanProp("detrimental");
        this.persistent = dataStore.booleanProp("persistent");
        this.conditions = AbstractCondition.listFromMap(dataStore.mapProp("conditions"));
        this.actions = AbstractEffectAction.listFromMap(this, dataStore.mapProp("effect_actions"));
    }

    @Override
    public String type()
    {
        return EffectRepository.get().type();
    }

    /**
     * Returns the group of this effect. This is different to the vanilla mob effect category.
     * @return The group of this effect.
     */
    public String group()
    {
        return this.group;
    }

    /**
     * Returns the vanilla mob effect category this effect should use. This is different to the group.
     * @return The vanilla mob effect category.
     */
    public MobEffectCategory category()
    {
        if (this.beneficial && !this.detrimental) {
            return MobEffectCategory.BENEFICIAL;
        }
        if (!this.beneficial && this.detrimental) {
            return MobEffectCategory.HARMFUL;
        }
        return MobEffectCategory.NEUTRAL;
    }

    /**
     * Returns the color of this effect.
     * @return The effect particle color to use.
     */
    public int color()
    {
        return this.color;
    }

    /**
     * Returns if this effect should be considered persistent where it is resistant effect removal, etc.
     * @return True fi this effect is persistent.
     */
    public boolean persistent()
    {
        return this.persistent;
    }

    /**
     * Returns the conditions for the effect.
     * @return The conditions for the effect.
     */
    public List<AbstractCondition> conditions()
    {
        return this.conditions;
    }

    /**
     * Returns the actions for the effect.
     * @return The actions for the effect.
     */
    public List<AbstractEffectAction> actions()
    {
        return this.actions;
    }

    /**
     * Returns the effect this definition is for.
     * @return The effect instance.
     */
    public MobEffect effect()
    {
        return new Effect(this);
    }

    /**
     * Registers an effect.
     * @param registry The forge registry.
     */
    public void registerEffect(final IForgeRegistry<MobEffect> registry)
    {
        registry.register(this.effect());
    }
}

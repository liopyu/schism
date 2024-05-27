package com.schism.core.gods;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

/**
 * A worship that records an amount of worship towards a specific god.
 */
public class Worship implements INBTSerializable<CompoundTag>
{
    protected God god;
    protected int devotion;

    public Worship(God god, int devotion)
    {
        this.god = god;
        this.devotion = devotion;
    }

    public Worship(CompoundTag compoundTag)
    {
        this.deserializeNBT(compoundTag);
    }

    /**
     * Gets the god this worship is for.
     * @return The god this worship is for.
     */
    public God god()
    {
        return this.god;
    }

    /**
     * Gets a list of unlocked ranks that this worship has based on its devotion.
     * @return A list of unlocked worship ranks
     */
    public List<WorshipRank> unlockedRanks()
    {
        return this.god().definition().ranks().stream().filter(worshipRank -> this.devotion() >= worshipRank.devotion()).toList();
    }

    /**
     * Gets the devotion this worship has.
     * @return The amount of devotion.
     */
    public int devotion()
    {
        return this.devotion;
    }

    /**
     * Sets the devotion this worship has.
     */
    public void setDevotion(int devotion)
    {
        this.devotion = devotion;
    }

    /**
     * Adds an amount of devotion to this worship.
     */
    public void addDevotion(int devotion)
    {
        this.devotion += devotion;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("god", this.god.definition().subject());
        compoundTag.putInt("devotion", this.devotion());

        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag)
    {
        this.god = GodRepository.get().getDefinition(compoundTag.getString("god"))
                .map(GodDefinition::god)
                .orElse(null);
        this.devotion = compoundTag.getInt("devotion");
    }
}

package com.schism.core.creatures;

import com.schism.core.Schism;
import com.schism.core.config.ConfigRepository;
import com.schism.core.network.MessageRepository;
import com.schism.core.network.messages.CreatureStatsCompoundMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public class CreatureStats implements ICanSync, INBTSerializable<CompoundTag>
{
    protected final ICreature creature;

    protected int level;
    protected int experience;

    protected UUID attributesUUID;

    public CreatureStats(ICreature creature)
    {
        this.creature = creature;

        this.level = 1;
        this.experience = 0;

        this.attributesUUID = UUID.randomUUID();
    }

    /**
     * The creature these stats are for.
     * @return The creature instance.
     */
    public ICreature creature()
    {
        return this.creature;
    }

    /**
     * The current level of this creature.
     * @return The current level.
     */
    public int level()
    {
        return this.level;
    }

    /**
     * Sets the level of this creature.
     * @param level The level to set this creature to.
     * @return The current instance.
     */
    public CreatureStats setLevel(int level)
    {
        this.level = Math.max(Math.min(level, ConfigRepository.get().intSetting("creatures", "level_cap", 1000)), 1);
        this.experience = 0;
        this.refreshAttributes();
        this.syncClients();
        return this;
    }

    /**
     * Adds levels to this creature.
     * @param amount The amount of levels to add.
     * @return The current instance.
     */
    public CreatureStats addLevels(int amount)
    {
        this.level += amount;
        this.refreshAttributes();
        this.syncClients();
        if (this.creature().livingEntity() instanceof ServerPlayer serverPlayer) {
            this.creature().definition().sendNotification("level", "" + this.level(), serverPlayer);
        }
        return this;
    }

    /**
     * Sets the level of this creature to a random one based on its location.
     * @param level The level instance to get location info from.
     * @return The current instance.
     */
    public CreatureStats setRandomLevel(Level level)
    {
        long chunkTime = level.getChunkAt(this.creature().livingEntity().blockPosition()).getInhabitedTime();
        int chunkMins = (int)Math.round((double)chunkTime / 20 / 60);
        float levelsPerChunkMin = ConfigRepository.get().floatSetting("creatures", "levels_per_chunk_minute", 0);
        int randomLevel = ConfigRepository.get().randomIntSetting(level.getRandom(), "creatures", "random_level", 1);

        this.setLevel(randomLevel + Math.round(Math.max(levelsPerChunkMin * chunkMins, 1)) - 1);

        return this;
    }

    /**
     * The current experience this creature has towards the next level.
     * @return The current experience amount.
     */
    public int experience()
    {
        return this.experience;
    }

    /**
     * Adds experience to this creature and will trigger a level up if the target is met or exceeded, etc.
     * @param amount The amount of experience to add.
     * @return The current instance.
     */
    public CreatureStats addExperience(int amount)
    {
        if (amount <= 0) {
            return this;
        }
        int required = this.experienceTarget() - this.experience();
        if (amount >= required) {
            this.experience = 0;
            this.addLevels(1);
            return this.addExperience(amount - required);
        }
        this.experience += amount;
        this.syncClients();
        return this;
    }

    /**
     * Adds experience to this creature from killing, breeding, etc the provided entity.
     * @param livingEntity The entity to gain experience from.
     * @return The current instance.
     */
    public CreatureStats addExperience(LivingEntity livingEntity)
    {
        int amount = Math.round(livingEntity.getMaxHealth());
        return this.addExperience(amount);
    }

    /**
     * The target amount of experience to reach the next level.
     * @return The next level experience target.
     */
    public int experienceTarget()
    {
        return this.level() * 100;
    }

    /**
     * Gets the health stat of this creature. This is added to the creature's entity max health.
     * @return The health stat of this creature.
     */
    public float health()
    {
        return this.creature().definition().levellingHealth() * (this.level() - 1);
    }

    /**
     * Gets the attack stat of this creature. This is added to any damage dealt.
     * @return The attack stat of this creature.
     */
    public float attack()
    {
        return this.creature().definition().levellingAttack() * (this.level() - 1);
    }

    /**
     * Gets the defense stat of this creature. Any damage taken is subtracted by this down to a minimum of 1.
     * @return The defense stat of this creature.
     */
    public float defense()
    {
        return this.creature().definition().levellingDefense() * (this.level() - 1);
    }

    /**
     * Refreshes entity attribute modifiers based on creature stats.
     */
    public void refreshAttributes()
    {
        // Health:
        AttributeInstance healthAttribute = this.creature().livingEntity().getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            double originalHealthMax = healthAttribute.getValue();
            AttributeModifier healthMod = new AttributeModifier(this.attributesUUID, Schism.NAMESPACE + ".creature", this.health(), AttributeModifier.Operation.ADDITION);
            if (healthAttribute.hasModifier(healthMod)) {
                healthAttribute.removeModifier(this.attributesUUID);
            }
            healthAttribute.addPermanentModifier(healthMod);

            double currentHealthMax = healthAttribute.getValue();
            float currentHealth = this.creature().livingEntity().getHealth();
            if (currentHealthMax != originalHealthMax) {
                double healthNormal = currentHealth / originalHealthMax;
                this.creature().livingEntity().setHealth((float)(currentHealthMax * healthNormal));
                currentHealth = this.creature().livingEntity().getHealth();
            }
        }
    }

    /**
     * Scales the provided damage amount by this creature's attack stat and level.
     * @param amount The amount of damage to scale.
     * @return The scaled amount of damage.
     */
    public float scaleDamageDealt(float amount)
    {
        return amount + this.attack();
    }

    /**
     * Scales the provided damage amount by this creature's defense stat and level.
     * @param amount The amount of damage to scale.
     * @return The scaled amount of damage.
     */
    public float scaleDamageTaken(float amount)
    {
        if (amount < 1) {
            return amount;
        }
        return Math.max(amount - this.defense(), 1);
    }

    @Override
    public void syncClients()
    {
        MessageRepository.get().toLevel(new CreatureStatsCompoundMessage(this.creature().livingEntity().getId(), this.serializeNBT()), this.creature().livingEntity().getLevel());
    }

    @Override
    public void syncClient(ServerPlayer serverPlayer)
    {
        MessageRepository.get().toPlayer(new CreatureStatsCompoundMessage(this.creature().livingEntity().getId(), this.serializeNBT()), serverPlayer);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putInt("level", this.level);
        compoundTag.putInt("experience", this.experience);
        compoundTag.putUUID("attributeUUID", this.attributesUUID);

        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag)
    {
        if (compoundTag.contains("level")) {
            this.level = Math.max(compoundTag.getInt("level"), 1);
        }
        if (compoundTag.contains("experience")) {
            this.experience = compoundTag.getInt("experience");
        }
        if (compoundTag.contains("attributeUUID")) {
            this.attributesUUID = compoundTag.getUUID("attributeUUID");
        }
        this.refreshAttributes();
    }
}

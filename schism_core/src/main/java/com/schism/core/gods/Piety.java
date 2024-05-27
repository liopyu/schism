package com.schism.core.gods;

import com.schism.core.creatures.ICanSync;
import com.schism.core.creatures.ICreature;
import com.schism.core.gods.perks.AbstractWorshipPerk;
import com.schism.core.network.MessageRepository;
import com.schism.core.network.messages.PietyCompoundMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Piety implements ICanSync, INBTSerializable<CompoundTag>
{
    protected final ICreature creature;
    protected PantheonDefinition pantheon;
    protected final List<Worship> worships = new ArrayList<>();

    public Piety(ICreature creature)
    {
        this.creature = creature;
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
     * Adds an amount of devotion to a specified god.
     * This will change pantheons and clear gods from other pantheons if the provided god is of a different pantheon.
     * @param godSubject The subject name of the god to set devotion for.
     * @param devotion The amount of devotion to set.
     */
    public void addDevotion(String godSubject, int devotion)
    {
        God god = GodRepository.get().getDefinition(godSubject).map(GodDefinition::god).orElse(null);
        if (god == null) {
            return;
        }

        // Update Worship:
        Worship worship = this.worships().stream().filter(worshipEntry -> worshipEntry.god() == god).findFirst().orElse(null);
        if (worship != null) {
            worship.addDevotion(devotion);
            this.syncClients();
            if (this.creature.livingEntity() instanceof ServerPlayer serverPlayer) {
                god.definition().sendNotification("add", "+" + devotion, serverPlayer);
            }
            return;
        }

        // New Worship/Pantheon:
        if (this.pantheon().orElse(null) != god.definition().pantheon()) {
            if (this.creature.livingEntity() instanceof ServerPlayer serverPlayer) {
                this.worships().forEach(worshipEntry -> worshipEntry.god().definition().sendNotification("remove", "", serverPlayer));
            }
            this.worships().clear();
            this.pantheon = god.definition().pantheon();
        }
        this.worships().add(new Worship(god, devotion));
        this.syncClients();
        if (this.creature.livingEntity() instanceof ServerPlayer serverPlayer) {
            god.definition().sendNotification("new", "" + devotion, serverPlayer);
        }
    }

    /**
     * Sets the amount of devotion for a specified god.
     * This will change pantheons and clear gods from other pantheons if the provided god is of a different pantheon.
     * @param godSubject The subject name of the god to set devotion for.
     * @param devotion The amount of devotion to set.
     */
    public void setDevotion(String godSubject, int devotion)
    {
        God god = GodRepository.get().getDefinition(godSubject).map(GodDefinition::god).orElse(null);
        if (god == null) {
            return;
        }

        // Update Worship:
        Worship worship = this.worships.stream().filter(worshipEntry -> worshipEntry.god() == god).findFirst().orElse(null);
        if (worship != null) {
            worship.setDevotion(devotion);
            this.syncClients();
            return;
        }

        // New Worship/Pantheon:
        if (this.pantheon().orElse(null) != god.definition().pantheon()) {
            this.worships().clear();
            this.pantheon = god.definition().pantheon();
        }
        this.worships().add(new Worship(god, devotion));
        this.syncClients();
    }

    /**
     * Removes all worship for a specified god.
     * @param godSubject The subject name of the god to remove.
     */
    public void removeWorship(String godSubject)
    {
        God god = GodRepository.get().getDefinition(godSubject).map(GodDefinition::god).orElse(null);
        if (god == null) {
            return;
        }

        this.worships().removeIf(worshipEntry -> worshipEntry.god() == god);
        if (this.worships().isEmpty()) {
            this.pantheon = null;
        }
        this.syncClients();
        if (this.creature.livingEntity() instanceof ServerPlayer serverPlayer) {
            this.worships().forEach(worshipEntry -> worshipEntry.god().definition().sendNotification("remove", "", serverPlayer));
        }
    }

    /**
     * Gets the pantheon this piety is currently for.
     * @return An optional pantheon.
     */
    public Optional<PantheonDefinition> pantheon()
    {
        return Optional.ofNullable(this.pantheon);
    }

    /**
     * Gets a list of worships in this piety.
     * @return A list of worships containing god and devotion amount information.
     */
    public List<Worship> worships()
    {
        return this.worships;
    }

    /**
     * Gets a list of all ranks this piety has unlocked in its worships.
     * @return A list of unlocked worship ranks.
     */
    public List<WorshipRank> unlockedRanks()
    {
        return this.worships().stream().flatMap(worship -> worship.unlockedRanks().stream()).toList();
    }

    /**
     * Gets a list of all perks this piety has unlocked in its worship ranks.
     * @return A list of available perks.
     */
    public List<AbstractWorshipPerk> perks()
    {
        return this.unlockedRanks().stream().flatMap(worshipRank -> worshipRank.perks().stream()).toList();
    }

    @Override
    public void syncClients()
    {
        MessageRepository.get().toLevel(new PietyCompoundMessage(this.creature().livingEntity().getId(), this.serializeNBT()), this.creature().livingEntity().getLevel());
    }

    @Override
    public void syncClient(ServerPlayer serverPlayer)
    {
        MessageRepository.get().toPlayer(new PietyCompoundMessage(this.creature().livingEntity().getId(), this.serializeNBT()), serverPlayer);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compoundTag = new CompoundTag();

        compoundTag.putString("pantheon", this.pantheon().map(PantheonDefinition::subject).orElse(""));

        ListTag listTag = new ListTag();
        this.worships.forEach(worship -> listTag.add(worship.serializeNBT()));
        compoundTag.put("worships", listTag);

        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag)
    {
        if (compoundTag.contains("pantheon")) {
            this.pantheon = PantheonRepository.get().getDefinition(compoundTag.getString("pantheon")).orElse(null);
        }
        if (compoundTag.contains("worships")) {
            this.worships.clear();
            this.worships.addAll(compoundTag.getList("worships", 10).stream().map(tag -> {
                if (tag instanceof CompoundTag compoundTagEntry) {
                    return new Worship(compoundTagEntry);
                }
                return null;
            }).filter(Objects::nonNull).toList());
        }
    }
}

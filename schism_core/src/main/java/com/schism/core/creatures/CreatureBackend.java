package com.schism.core.creatures;

import com.schism.core.Log;
import com.schism.core.Schism;
import com.schism.core.config.ConfigDefinition;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.gods.Piety;
import com.schism.core.gods.perks.AbstractWorshipPerk;
import com.schism.core.items.ChargeItem;
import com.schism.core.network.MessageRepository;
import com.schism.core.network.messages.RequestCreatureMessage;
import com.schism.core.projectiles.ProjectileDefinition;
import com.schism.core.projectiles.ProjectileRepository;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class CreatureBackend implements ICreature, INBTSerializable<CompoundTag>
{
    protected final LivingEntity livingEntity;
    protected final CreatureDefinition definition;
    protected final CreatureStats stats;
    protected final Piety piety;
    protected boolean initialized = false;
    protected boolean generated = false;

    protected boolean playerDamaged = false;

    /**
     * Provides creature based logic to the provided living entity, should be attached via a Creature Capability Provider only.
     * @param livingEntity The living entity to attach to.
     * @param definition The creature definition to use.
     */
    public CreatureBackend(LivingEntity livingEntity, CreatureDefinition definition)
    {
        this.livingEntity = livingEntity;
        this.definition = definition;
        this.stats = new CreatureStats(this);
        this.piety = new Piety(this);
    }

    @Override
    public CreatureDefinition definition()
    {
        return this.definition;
    }

    @Override
    public LivingEntity livingEntity()
    {
        return this.livingEntity;
    }

    /**
     * Called on the first update tick, initializes this capability.
     */
    public void init()
    {
        this.initialized = true;

        // First Spawn:
        if (!this.generated && !this.livingEntity().getLevel().isClientSide()) {
            this.generated = true;
            if (!(this.livingEntity() instanceof Player)) {
                this.stats().setRandomLevel(this.livingEntity().getLevel());
            }
        }

        // Request Sync from Server:
        if (this.livingEntity().getLevel().isClientSide()) {
            MessageRepository.get().toServer(new RequestCreatureMessage(this.livingEntity().getId()));
        }
    }

    @Override
    public void tick()
    {
        if (!this.initialized) {
            this.init();
        }
    }

    @Override
    public boolean modifyNameTag(MutableComponent component)
    {
        component.append(" [");
        component.append(Component.translatable(Schism.NAMESPACE + ".stats.level.compact"));
        component.append("" + this.stats().level());
        component.append("]");
        return this.stats().level() > 1;
    }

    @Override
    public float onHurt(float amount, DamageSource damageSource, List<ElementDefinition> damageElements)
    {
        if (amount < 0) {
            return amount;
        }

        // Record Player Damage:
        if (damageSource.getEntity() instanceof Player) {
            this.playerDamaged = true;
        }

        // Resist Elements:
        for (ElementDefinition resistElement : this.resistElements()) {
            if (damageElements.contains(resistElement)) {
                amount *= 0.5F;
            }
        }

        // Weakness Elements:
        for (ElementDefinition weaknessElement : this.weaknessElements()) {
            if (damageElements.contains(weaknessElement)) {
                amount *= 2F;
            }
        }

        // Worship Perks:
        for (AbstractWorshipPerk perk : this.piety().perks()) {
            amount = perk.hurt(amount, damageSource, damageElements);
        }

        return this.stats().scaleDamageTaken(amount);
    }

    @Override
    public void onDeath(DamageSource damageSource, List<ElementDefinition> damageElements)
    {
        // Drop Charges if killed by a player:
        if (this.playerDamaged) {
            List<ElementDefinition> chargeElements = Stream.of(this.attackElements(), this.harmonyElements()).flatMap(Collection::stream).toList();
            List<ChargeItem> chargeItems = ProjectileRepository.get().withAllChargeElements(chargeElements).stream().map(ProjectileDefinition::chargeItem).toList();
            if (!chargeItems.isEmpty()) {
                int dropCount = 1 + Math.round((float) this.stats().level() * 0.1F);
                for (int i = 0; i < dropCount; i++) {
                    ItemStack itemStack = chargeItems.size() > 1 ? chargeItems.get(this.livingEntity().getRandom().nextInt(chargeItems.size())).getDefaultInstance() : chargeItems.get(0).getDefaultInstance();
                    this.livingEntity().getLevel().addFreshEntity(new ItemEntity(this.livingEntity().getLevel(), this.livingEntity().getX(), this.livingEntity().getY(1), this.livingEntity().getZ(), itemStack));
                }
            }
        }
    }

    @Override
    public float onDamage(float amount, DamageSource damageSource, List<ElementDefinition> damageElements, LivingEntity livingEntity)
    {
        if (amount < 0) {
            return amount;
        }
        return this.stats().scaleDamageDealt(amount);
    }

    @Override
    public void onKill(DamageSource damageSource, List<ElementDefinition> damageElements, LivingEntity livingEntity)
    {
        this.stats().addExperience(livingEntity);
    }

    @Override
    public boolean effectApplicable(MobEffectInstance effectInstance)
    {
        return this.piety().perks().stream().allMatch(perk -> perk.effectApplicable(effectInstance));
    }

    @Override
    public List<ElementDefinition> resistElements()
    {
        List<ElementDefinition> elementList = new ArrayList<>(this.definition().resistElements());
        this.piety().perks().forEach(perk -> perk.resistElement().ifPresent(elementList::add));
        return elementList;
    }

    @Override
    public List<ElementDefinition> weaknessElements()
    {
        return this.definition().weaknessElements();
    }

    @Override
    public List<ElementDefinition> attackElements()
    {
        List<ElementDefinition> elementList = new ArrayList<>();
        this.piety().perks().forEach(perk -> perk.attackElement().ifPresent(elementList::add));
        if (elementList.isEmpty()) {
            elementList.add(this.definition().baseElement());
        }
        return elementList;
    }

    @Override
    public List<ElementDefinition> harmonyElements()
    {
        List<ElementDefinition> elementList = new ArrayList<>();
        this.piety().perks().forEach(perk -> perk.harmonyElement().ifPresent(elementList::add));
        return elementList;
    }

    @Override
    public Color elementColor()
    {
        if (!this.attackElements().isEmpty()) {
            return this.attackElements().get(0).color();
        }
        return Color.GRAY;
    }

    @Override
    public CreatureStats stats()
    {
        return this.stats;
    }

    @Override
    public Piety piety()
    {
        return this.piety;
    }

    @Override
    public void syncClients()
    {
        this.stats().syncClients();
        this.piety().syncClients();
    }

    @Override
    public void syncClient(ServerPlayer serverPlayer)
    {
        this.stats().syncClient(serverPlayer);
        this.piety().syncClient(serverPlayer);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putBoolean("generated", this.generated);
        compoundTag.putBoolean("playerDamaged", this.playerDamaged);
        compoundTag.put("stats", this.stats().serializeNBT());
        compoundTag.put("piety", this.piety().serializeNBT());
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag)
    {
        if (compoundTag.contains("generated")) {
            this.generated = compoundTag.getBoolean("generated");
        }
        if (compoundTag.contains("playerDamaged")) {
            this.playerDamaged = compoundTag.getBoolean("playerDamaged");
        }
        if (compoundTag.contains("stats")) {
            this.stats().deserializeNBT(compoundTag.getCompound("stats"));
        }
        if (compoundTag.contains("piety")) {
            this.piety().deserializeNBT(compoundTag.getCompound("piety"));
        }
        this.syncClients();
    }
}

package com.schism.core.elements;

import com.schism.core.AbstractRepository;
import com.schism.core.creatures.Creatures;
import com.schism.core.database.DataStore;
import com.schism.core.resolvers.ElementResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ElementRepository extends AbstractRepository<ElementDefinition>
{
    private static final ElementRepository INSTANCE = new ElementRepository();

    protected Supplier<ElementDefinition> fallbackElementSupplier;

    /**
     * Gets the singleton ElementRepository instance.
     * @return The Element Repository which loads and stores elements.
     */
    public static ElementRepository get()
    {
        return INSTANCE;
    }

    protected ElementRepository()
    {
        super("element");
    }

    /**
     * Sets the fallback element, this is used for damage when no other element matches to ensure that there is always an element.
     * @param elementDefinitionSupplier An element definition supplier to get the fallback element from.
     */
    public void setFallback(Supplier<ElementDefinition> elementDefinitionSupplier)
    {
        this.fallbackElementSupplier = elementDefinitionSupplier;
    }

    @Override
    public ElementDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new ElementDefinition(subject, dataStore);
    }

    /**
     * Gets a list of elements for the provided damage source.
     * @param damageSource The damage source to determine the elements of.
     * @param level The level the damage is taking place in.
     * @param hitPosition The block position of the target hit by the damage.
     * @return A list of elements for calculating damage with.
     */
    public List<ElementDefinition> damageElements(DamageSource damageSource, Level level, BlockPos hitPosition)
    {
        List<ElementDefinition> elementList = new ArrayList<>();

        // Elements Damage Source
        if (damageSource instanceof IHasElements hasElements) {
            return hasElements.elements();
        }

        // Element Resolver High Priority:
        Optional<ElementDefinition> optionalPriorityElement = ElementResolver.get().fromDamageSource(damageSource, true);
        if (optionalPriorityElement.isPresent()) {
            elementList.add(optionalPriorityElement.get());
            return elementList;
        }

        // Creatures:
        if (damageSource.getEntity() instanceof LivingEntity livingEntity) {
            Creatures.get().getCreature(livingEntity).ifPresent(creature -> elementList.addAll(creature.attackElements()));
        }

        // Additive:
        if (damageSource.isExplosion() && this.getDefinition("quake").isPresent()) {
            elementList.add(this.getDefinition("quake").get());
        }
        if (damageSource == DamageSource.IN_FIRE && this.getDefinition("phase").isPresent()) {
            if (level.getBlockState(hitPosition).getBlock() == Blocks.SOUL_FIRE) {
                elementList.add(this.getDefinition("phase").get());
            }
        }

        // Element Resolver Low Priority:
        if (elementList.isEmpty()) {
            ElementResolver.get().fromDamageSource(damageSource, false).ifPresent(elementList::add);
        }

        // Fallback:
        if (elementList.isEmpty() && this.fallbackElementSupplier.get() != null) {
            elementList.add(this.fallbackElementSupplier.get());
        }

        return elementList;
    }
}

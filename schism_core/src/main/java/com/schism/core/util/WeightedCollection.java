package com.schism.core.util;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public class WeightedCollection<ItemT>
{
    private final NavigableMap<Double, ItemT> map = new TreeMap<>();
    private final Map<Double, ItemT> weightMap = new HashMap<>();
    private double total = 0;

    /**
     * A collection of items with weight values for weighted random selection.
     */
    public WeightedCollection()
    {
    }

    /**
     * Adds a new weighted item.
     * @param weight The weight, items with higher weights are more likely to be chosen.
     * @param item The item.
     * @return The instance.
     */
    public WeightedCollection<ItemT> add(double weight, ItemT item)
    {
        if (weight <= 0) {
            return this;
        }
        this.total += weight;
        this.map.put(total, item);
        this.weightMap.put(weight, item);
        return this;
    }

    /**
     * Adds a new weighted item.
     * @param entry A weight and item entry.
     * @return The instance.
     */
    public WeightedCollection<ItemT> add(Entry<ItemT> entry)
    {
        if (entry.weight() <= 0) {
            return this;
        }
        this.total += entry.weight();
        this.map.put(total, entry.item());
        this.weightMap.put(entry.weight, entry.item());
        return this;
    }

    public WeightedCollection<ItemT> addAll(WeightedCollection<ItemT> weightedCollection)
    {
        weightedCollection.weightMap.forEach(this::add);
        return this;
    }

    /**
     * Gets a weight random item.
     * @param random An instance of random to use.
     * @return A randomly selected item.
     */
    public ItemT get(Random random)
    {
        if (this.map.isEmpty()) {
            throw new RuntimeException("Tried to get a random item from a weighted collection that is empty.");
        }
        double roll = random.nextDouble() * this.total;
        return this.map.higherEntry(roll).getValue();
    }

    /**
     * Returns if the size of this collection.
     * @return The size of this collection.
     */
    public int size()
    {
        return this.map.size();
    }

    /**
     * Returns if this collection is empty.
     * @return True if this collection is empty.
     */
    public boolean isEmpty()
    {
        return this.map.isEmpty();
    }

    /**
     * An entry record for adding entries.
     * @param <ItemT> The item type.
     */
    public record Entry<ItemT>(double weight, ItemT item)
    {
    }

    /**
     * A collector class for streaming other collections into a weighted collection with.
     */
    public static class Collector<ItemT> implements java.util.stream.Collector<Entry<ItemT>, WeightedCollection<ItemT>, WeightedCollection<ItemT>>
    {
        /**
         * A collector class for streaming other collections into a weighted collection with.
         * @return A collector class for streaming other collections into a weighted collection with.
         */
        public static <ItemT> Collector<ItemT> toWeightedCollection()
        {
            return new Collector<>();
        }

        @Override
        public Supplier<WeightedCollection<ItemT>> supplier()
        {
            return WeightedCollection::new;
        }

        @Override
        public BiConsumer<WeightedCollection<ItemT>, Entry<ItemT>> accumulator()
        {
            return WeightedCollection::add;
        }

        @Override
        public BinaryOperator<WeightedCollection<ItemT>> combiner()
        {
            return WeightedCollection::addAll;
        }

        @Override
        public Function<WeightedCollection<ItemT>, WeightedCollection<ItemT>> finisher()
        {
            return itemTWeightedCollection -> itemTWeightedCollection;
        }

        @Override
        public Set<Characteristics> characteristics()
        {
            return Set.of(Characteristics.UNORDERED);
        }
    }
}

package com.schism.core.database;

import java.util.Optional;
import java.util.function.Supplier;

public class CachedObject<ObjectT>
{
    protected ObjectT cache;
    protected Supplier<ObjectT> supplier;
    protected boolean invalid;

    /**
     * Cached Objects take a supplier and will return the object with caching for performance.
     * @param supplier A supplier to get the object from.
     */
    public static <ObjectT> CachedObject<ObjectT> of(Supplier<ObjectT> supplier)
    {
        return new CachedObject<>(supplier);
    }

    /**
     * Cached Objects take a supplier and will return the object with caching for performance.
     * @param supplier A supplier to get the object from.
     */
    public CachedObject(Supplier<ObjectT> supplier)
    {
        this.supplier = supplier;
    }

    /**
     * Gets a cached object or null if the id was invalid (without constantly retrying for performance).
     * @return The cached object or null if the id was invalid.
     */
    public ObjectT get()
    {
        if (this.invalid) {
            return null;
        }
        if (this.cache == null) {
            this.cache = this.supplier.get();
            if (this.cache == null) {
                this.invalid = true;
            }
        }
        return this.cache;
    }

    /**
     * Returns if the cached object is present, will also attempt to resolve it if the cache is empty.
     * @return True if the cached object is present, false otherwise.
     */
    public boolean isPresent()
    {
        return this.get() != null;
    }

    /**
     * Returns if the cached object is empty, will also attempt to resolve it if the cache is empty.
     * @return True if the cached object is empty, false otherwise.
     */
    public boolean isEmpty()
    {
        return !this.isPresent();
    }

    /**
     * Returns the cached object if present, otherwise returns the provided alternative.
     * @param other The alternative to return if the object is not present.
     * @return The cached object or other if the object is not present.
     */
    public ObjectT orElse(ObjectT other)
    {
        if (!this.isPresent()) {
            return other;
        }
        return this.get();
    }

    /**
     * Returns an optional of this object which is empty when the object is not present.
     * @return An optional of the cached object.
     */
    public Optional<ObjectT> optional()
    {
        if (this.isPresent()) {
            return Optional.of(this.get());
        }
        return Optional.empty();
    }
}

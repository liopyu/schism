package com.schism.core.database;

import com.schism.core.AbstractRepository;

public class CachedDefinition<ObjectT extends AbstractDefinition> extends CachedObject<ObjectT>
{
    /**
     * Cached Definitions take a definition subject and will return the definition it matches with caching for performance.
     * @param subject The definition's subject.
     * @param repository The repository to get the object from.
     */
    public CachedDefinition(String subject, AbstractRepository<ObjectT> repository)
    {
        super(() -> repository.getDefinition(subject).orElse(null));
    }
}

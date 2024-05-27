package com.schism.core.database;

import java.util.List;
import java.util.Objects;

public class CachedList<ObjectT> extends CachedObject<List<ObjectT>>
{
    /**
     * Cached Lists take a list of cached objects.
     * @param list The list of cached objects.
     */
    public CachedList(List<? extends CachedObject<ObjectT>> list)
    {
        super(() -> list.stream().map(CachedObject::get).filter(Objects::nonNull).toList());
    }
}

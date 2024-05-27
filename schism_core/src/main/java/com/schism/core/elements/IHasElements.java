package com.schism.core.elements;

import java.util.List;

public interface IHasElements
{
    /**
     * Gets the element of this damage source.
     * @return The element of the damage.
     */
    public List<ElementDefinition> elements();
}

package com.schism.core.database;

public interface IHasDefinition<DefinitionT extends AbstractDefinition>
{
    /**
     * Gets the associated definition for this object, this contains mod specific information loaded from definition configs, etc.
     * @return The definition for this object.
     */
    DefinitionT definition();
}

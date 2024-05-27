package com.schism.core.database;

abstract public class Action<DefinitionT>
{
    protected final DefinitionT definition;

    /**
     * Constructor
     * @param dataStore The data store to load action behaviour from.
     */
    public Action(DefinitionT definition, DataStore dataStore)
    {
        this.definition = definition;
    }

    /**
     * Gets the definition that this action belongs to.
     * @return The definition this action belongs to.
     */
    public DefinitionT definition()
    {
        return this.definition;
    }
}

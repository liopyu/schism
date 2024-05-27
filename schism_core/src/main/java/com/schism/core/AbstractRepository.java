package com.schism.core;

import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.DataStore;
import com.schism.core.database.Database;
import com.schism.core.database.IDatabaseEventListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractRepository<DefinitionT extends AbstractDefinition> implements IDatabaseEventListener
{
    protected static final HashMap<String, AbstractRepository<?>> REPOSITORIES = new HashMap<>();

    public static Optional<AbstractRepository<?>> fromType(String type)
    {
        if (!REPOSITORIES.containsKey(type)) {
            return Optional.empty();
        }
        return Optional.of(REPOSITORIES.get(type));
    }

    protected String type;
    protected Map<String, DefinitionT> definitions = new HashMap<>();

    /**
     * Constructor
     * @param type The type of repository this is, this should match the type of data entries to pull from the database.
     */
    protected AbstractRepository(String type)
    {
        this.type = type;
        REPOSITORIES.put(type, this);
    }

    @Override
    public void onDatabaseLoaded(Database database)
    {
        database.definitionEntries().row(this.type).forEach((subject, definitionEntry) -> {
            DefinitionT currentDefinition = this.definitions.getOrDefault(subject, null);
            if (currentDefinition != null) {
                currentDefinition.update(definitionEntry);
                return;
            }
            this.addDefinition(this.createDefinition(subject, definitionEntry));
        });
    }

    /**
     * Gets the type of this repository.
     * @return The type of this repository.
     */
    public String type()
    {
        return this.type;
    }

    /**
     * Adds the provided definition to this repository.
     * @param definition The definition to add.
     */
    protected void addDefinition(DefinitionT definition)
    {
        this.definitions.put(definition.subject(), definition);
    }

    /**
     * Logs all definitions in this repository at info level, used for testing and debugging.
     */
    public void logDefinitions()
    {
        Log.infoTitle(this.type(), this.getClass().getSimpleName() + " Loaded Definitions");
        this.definitions.values().stream().sorted().forEach(definition -> Log.info(this.type(), definition.toString()));
    }

    /**
     * Creates a definition of a type based on the repository from the provided definition entry.
     * @param subject The subject that the definition is for.
     * @param dataStore The data store.
     * @return Returns the newly created definition.
     */
    public abstract DefinitionT createDefinition(String subject, DataStore dataStore);

    /**
     * Returns a collection of all definitions.
     * @return A collection of all definitions.
     */
    public Collection<DefinitionT> definitions()
    {
        return this.definitions.values();
    }

    /**
     * Gets a definition for the provided subject.
     * @param subject The subject of the definition to get (not a registry id).
     * @return An optional of the requested definition.
     */
    public Optional<DefinitionT> getDefinition(String subject)
    {
        if (!this.definitions.containsKey(subject)) {
            return Optional.empty();
        }
        return Optional.of(this.definitions.get(subject));
    }
}

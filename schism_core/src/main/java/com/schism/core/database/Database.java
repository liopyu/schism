package com.schism.core.database;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.schism.core.Log;
import com.schism.core.Schism;
import com.schism.core.altars.AltarRepository;
import com.schism.core.blocks.BlockRepository;
import com.schism.core.blocks.ThemeRepository;
import com.schism.core.config.ConfigRepository;
import com.schism.core.creatures.CreatureRepository;
import com.schism.core.creatures.CreatureRoleRepository;
import com.schism.core.creatures.CreatureTypeRepository;
import com.schism.core.effects.EffectRepository;
import com.schism.core.elements.ElementRepository;
import com.schism.core.gods.GodRepository;
import com.schism.core.gods.PantheonRepository;
import com.schism.core.items.ItemRepository;
import com.schism.core.lore.RealmRepository;
import com.schism.core.particles.ParticleRepository;
import com.schism.core.projectiles.ProjectileRepository;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the core mod database that stores all information loaded from every json config and potentially other sources.
 */
public class Database
{
    private static Database INSTANCE;

    /**
     * Gets the singleton Database instance, this is lazy loaded so can be called by other static initializers.
     * @return The core mod Database, this holds all information loaded from configs.
     */
    public static Database get()
    {
        if (INSTANCE == null) {
            INSTANCE = new Database();
        }
        return INSTANCE;
    }

    /**
     * A list of database event listeners, these are called whenever data is loaded or reloaded.
     */
    private final List<IDatabaseEventListener> loadListeners = new ArrayList<>();

    /**
     * A list of data sources (currently on json sources), these are used to locate data to load in.
     */
    private final List<DataSource> jsonDataSources = new ArrayList<>();

    /**
     * A map of the latest raw json data that has been loaded into this database by data source.
     */
    private final Map<DataSource, List<DatabaseJson>> jsonDatabase = new HashMap<>();

    private final Table<String, String, DataStore> definitionEntries = HashBasedTable.create();

    public Database()
    {
        this.jsonDataSources.add(new DataSource("common", DataSource.Location.INTERNAL));
        this.jsonDataSources.add(new DataSource("override", DataSource.Location.CONFIG));
    }

    /**
     * Adds all core load listener repositories.
     */
    public void coreLoadListeners()
    {
        Database.get().addLoadListener(ConfigRepository.get());
        Database.get().addLoadListener(RealmRepository.get());
        Database.get().addLoadListener(BlockRepository.get());
        Database.get().addLoadListener(ThemeRepository.get());
        Database.get().addLoadListener(AltarRepository.get());
        Database.get().addLoadListener(ItemRepository.get());
        Database.get().addLoadListener(EffectRepository.get());
        Database.get().addLoadListener(ElementRepository.get());
        Database.get().addLoadListener(ParticleRepository.get());
        Database.get().addLoadListener(GodRepository.get());
        Database.get().addLoadListener(PantheonRepository.get());
        Database.get().addLoadListener(CreatureRepository.get());
        Database.get().addLoadListener(CreatureTypeRepository.get());
        Database.get().addLoadListener(CreatureRoleRepository.get());
        Database.get().addLoadListener(ProjectileRepository.get());
    }

    /**
     * Logs out all core definitions that have been loaded.
     */
    public void logCoreDefinitions()
    {
        ConfigRepository.get().logDefinitions();
        BlockRepository.get().logDefinitions();
        ItemRepository.get().logDefinitions();
        ThemeRepository.get().logDefinitions();
        AltarRepository.get().logDefinitions();
        EffectRepository.get().logDefinitions();
        ElementRepository.get().logDefinitions();
        ParticleRepository.get().logDefinitions();
        GodRepository.get().logDefinitions();
        PantheonRepository.get().logDefinitions();
        CreatureRepository.get().logDefinitions();
        CreatureTypeRepository.get().logDefinitions();
        CreatureRoleRepository.get().logDefinitions();
        ProjectileRepository.get().logDefinitions();
    }

    /**
     * Loads (or reloads) all json configs into the database, first from common (internal json), then from config defaults and then config overrides.
     */
    public void loadJson()
    {
        this.jsonDatabase.clear();
        this.definitionEntries.clear();
        this.jsonDataSources.forEach(this::loadJsonFromSource);
        this.generateReferenceConfig();
        this.onLoaded();
    }

    /**
     * Loads json from the provided data source.
     * @param dataSource The data source to load the json from.
     */
    private void loadJsonFromSource(DataSource dataSource)
    {
        Log.info("database", "Loading json data from " + dataSource);
        List<DatabaseJson> databaseJsons = dataSource.loadJson(this.jsonDatabase);
        this.jsonDatabase.put(dataSource, databaseJsons);
        databaseJsons.forEach(databaseJson -> {
            // Merge Existing Definitions:
            DataStore originalEntry = this.definitionEntries.get(databaseJson.type(), databaseJson.subject());
            if (originalEntry != null) {
                originalEntry.mergeJsonElement(databaseJson.jsonObject());
                return;
            }

            // Add New Definition:
            this.definitionEntries.put(databaseJson.type(), databaseJson.subject(), new DataStore(databaseJson.jsonObject()));
        });
    }

    /**
     * Generates the reference config json files, these are for users to copy into the override configs.
     */
    private void generateReferenceConfig()
    {
        final List<DatabaseJson> defaultDatabaseJsons = new ArrayList<>();
        this.jsonDatabase.forEach((dataSource, databaseJsons) -> {
            if (dataSource.location() == DataSource.Location.CONFIG) {
                return;
            }
            defaultDatabaseJsons.addAll(databaseJsons);
        });

        String configReferencePath = (new File(".")) + "/config/" + Schism.NAMESPACE + "/reference/";
        try {
            FileUtils.deleteDirectory(new File(configReferencePath));
        } catch (IOException e) {
            Log.error("datasource", "Error clearing config reference folder: " + configReferencePath);
            throw new RuntimeException(e);
        }

        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        defaultDatabaseJsons.forEach(defaultDatabaseJson -> {
            File jsonFile = new File(configReferencePath + defaultDatabaseJson.relativePath());
            jsonFile.getParentFile().mkdirs();
            try (final PrintWriter printWriter = new PrintWriter(jsonFile)) {
                final JsonWriter jsonWriter = gson.newJsonWriter(printWriter);
                jsonWriter.setIndent("    ");
                gson.toJson(defaultDatabaseJson.jsonObject(), jsonWriter);
                printWriter.println();
            } catch (IOException e) {
                Log.error("datasource", "Error created reference config json file or folder: " + jsonFile);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Calls all load event listeners (in the order that they were added starting from the first added), this should only be called when data is loaded or reloaded so that listeners can react to the data changes.
     */
    private void onLoaded()
    {
        this.loadListeners.forEach(loadListener -> loadListener.onDatabaseLoaded(this));
    }

    /**
     * Adds a new database data load event listener, these are called (in the order that they were added) whenever data is loaded or reloaded.
     * @param loadListener The load event listener to add.
     */
    public void addLoadListener(IDatabaseEventListener loadListener)
    {
        this.loadListeners.add(loadListener);
    }

    /**
     * Gets a table of all definition entries stored in the database.
     * @return A table of definition entries by row type and column subject.
     */
    public Table<String, String, DataStore> definitionEntries()
    {
        return this.definitionEntries;
    }
}

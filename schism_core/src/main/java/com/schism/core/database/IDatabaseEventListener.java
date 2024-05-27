package com.schism.core.database;

public interface IDatabaseEventListener
{
    /**
     * Called when the database has data loaded or reloaded.
     */
    void onDatabaseLoaded(Database database);
}

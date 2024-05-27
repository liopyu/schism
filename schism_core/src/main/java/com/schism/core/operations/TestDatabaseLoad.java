package com.schism.core.operations;

import com.schism.core.blocks.BlockRepository;
import com.schism.core.blocks.ThemeRepository;
import com.schism.core.database.Database;
import com.schism.core.items.ItemRepository;

public class TestDatabaseLoad
{
    static {
        Database.get().coreLoadListeners();
    }

    /**
     * Tests database loading of json configs.
     * To use, copy the client run configuration and change the main class to this class then check the debug log for results.
     * @param args Launch arguments.
     */
    public static void main(String[] args)
    {
        Database.get().loadJson();
        Database.get().logCoreDefinitions();
    }
}

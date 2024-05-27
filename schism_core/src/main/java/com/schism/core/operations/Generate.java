package com.schism.core.operations;

import com.schism.core.Schism;
import com.schism.core.database.Database;
import com.schism.core.operations.generators.LangJsonGenerator;
import com.schism.core.operations.generators.SoundsJsonGenerator;

public class Generate
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
        SoundsJsonGenerator.get().generate(Schism.NAMESPACE);
        LangJsonGenerator.get().generate(Schism.NAMESPACE);
    }
}

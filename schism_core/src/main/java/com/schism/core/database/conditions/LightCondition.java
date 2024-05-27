package com.schism.core.database.conditions;

import com.schism.core.database.DataStore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class LightCondition extends AbstractCondition
{
    protected final int brightnessMin;
    protected final int brightnessMax;
    protected final boolean sunlight;
    protected final boolean moonlight;

    public LightCondition(DataStore dataStore)
    {
        super(dataStore);

        this.brightnessMin = dataStore.intProp("brightness_min");
        this.brightnessMax = dataStore.intProp("brightness_max");
        this.sunlight = dataStore.booleanProp("sunlight");
        this.moonlight = dataStore.booleanProp("moonlight");
    }

    @Override
    public boolean test(Level level, BlockPos blockPos)
    {
        int brightness = level.getMaxLocalRawBrightness(blockPos);
        if (brightness < this.brightnessMin || brightness > this.brightnessMax) {
            return false;
        }
        long dayTime = level.getDayTime() % 24000;
        if (this.sunlight && (dayTime >= 14000 || !level.canSeeSky(blockPos))) {
            return false;
        }
        if (this.moonlight && (dayTime < 14000 || !level.canSeeSky(blockPos))) {
            return false;
        }
        return true;
    }
}

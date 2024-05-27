package com.schism.core.altars;

import com.schism.core.util.Maths;
import com.schism.core.util.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;

import java.util.List;
import java.util.Optional;

public class Altars
{
    private static final Altars INSTANCE = new Altars();

    /**
     * Gets the singleton Altars instance.
     * @return Altars, a utility for finding and building altars in an area.
     */
    public static Altars get()
    {
        return INSTANCE;
    }

    /**
     * Searches for an altar from the provided position and area.
     * @param level The level to search in.
     * @param blockPos The position to search around.
     * @param area The area size of the search.
     * @param altarSubjects A list of altar subjects to search for.
     * @return An optional altar if one has been found.
     */
    public Optional<AltarDefinition> search(Level level, BlockPos blockPos, int area, List<String> altarSubjects)
    {
        List<AltarDefinition> altars = altarSubjects.stream()
                .map(subject -> AltarRepository.get().getDefinition(subject))
                .filter(Optional::isPresent).map(Optional::get).toList();
        for (AltarDefinition altar : altars) {
            List<Block> coreBlocks = altar.coreBlocks();
            if (coreBlocks.isEmpty()) {
                continue;
            }
            // Search in a cube area from the center outwards:
            for (int x = 1; x <= Maths.oddCenterRange(area); x++) {
                for (int y = 1; y <= Maths.oddCenterRange(area); y++) {
                    for (int z = 1; z <= Maths.oddCenterRange(area); z++) {
                        BlockPos searchPos = blockPos.offset(Maths.oddCenter(x), Maths.oddCenter(y), Maths.oddCenter(z));
                        Block searchBlock = level.getBlockState(searchPos).getBlock();
                        if (coreBlocks.contains(searchBlock) && altar.structure().isAt(level, searchPos, altar.corePosition)) {
                            return Optional.of(altar);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Builds an altar at the provided position.
     * @param level The level to build in.
     * @param originPos The position to build from, this is always y 0 of the structure but relative to xz of the core position.
     * @param rotation The altar rotation.
     * @param altar The altar to build.
     */
    public void build(Level level, BlockPos originPos, Rotation rotation, AltarDefinition altar)
    {
        altar.structure().buildAt(level, originPos, new Vec3(altar.corePosition().x(), 0, altar.corePosition().z()), rotation);
    }
}

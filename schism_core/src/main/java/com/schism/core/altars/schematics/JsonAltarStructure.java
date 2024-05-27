package com.schism.core.altars.schematics;

import com.schism.core.database.CachedObject;
import com.schism.core.database.CachedRegistryObject;
import com.schism.core.database.DataStore;
import com.schism.core.util.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonAltarStructure extends AbstractAltarStructure
{
    protected final Map<Character, List<CachedRegistryObject<Block>>> blockMap;
    protected final Map<Vec3, Character> blockKeys;
    protected final Vec3 size;

    public JsonAltarStructure(DataStore dataStore)
    {
        this.blockMap = dataStore.mapProp("block_map").entrySet().stream()
                .collect(Collectors.toMap(entrySet -> entrySet.getKey().toCharArray()[0], entrySet -> entrySet.getValue().arrayList().stream()
                        .map(blockIdData -> new CachedRegistryObject<>(blockIdData.stringValue(), () -> ForgeRegistries.BLOCKS)).toList()
                ));

        this.blockKeys = new HashMap<>();
        List<DataStore> layers = dataStore.listProp("layers");
        int width = 0;
        int height = 0;
        int depth = layers.size();
        for (int z = 0; z < layers.size(); z++) {
            List<DataStore> rows = layers.get(z).arrayList();
            height = Math.max(height, rows.size());
            for (int y = 0; y < rows.size(); y++) {
                char[] row = rows.get(rows.size() - 1 - y).stringValue().toCharArray(); // Json Structure Y is inverted.
                width = Math.max(width, row.length);
                for (int x = 0; x < row.length; x++) {
                    this.blockKeys.put(new Vec3(x, y, z), row[x]);
                }
            }
        }
        this.size = new Vec3(width, height, depth);
    }

    @Override
    public Vec3 size()
    {
        return this.size;
    }

    @Override
    public Map<Vec3, List<CachedRegistryObject<Block>>> layout()
    {
        Map<Vec3, List<CachedRegistryObject<Block>>> layout = new HashMap<>();
        this.blockKeys.forEach((key, value) -> layout.put(key, this.blockMap.get(value)));
        return layout;
    }

    @Override
    public List<Block> blocksAt(Vec3 position)
    {
        if (this.blockKeys.containsKey(position)) {
            char key = this.blockKeys.get(position);
            if (this.blockMap.containsKey(key)) {
                return this.blockMap.get(key).stream().filter(CachedObject::isPresent).map(CachedObject::get).toList();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public boolean isAt(Level level, BlockPos originPos, Vec3 offset)
    {
        boolean north = true;
        boolean east = true;
        boolean south = true;
        boolean west = true;
        BlockPos offsetPos = offset.blockPos();

        for (Map.Entry<Vec3, Character> entry : this.blockKeys.entrySet()) {
            if (!this.blockMap.containsKey(entry.getValue())) {
                continue;
            }
            List<Block> requiredBlocks = this.blockMap.get(entry.getValue()).stream().filter(CachedObject::isPresent).map(CachedObject::get).toList();
            if (requiredBlocks.isEmpty()) {
                continue;
            }

            BlockPos structurePos = entry.getKey().blockPos();

            if (north) {
                BlockPos blockPos = new BlockPos(originPos.offset(structurePos.subtract(offsetPos)));
                Block block = level.getBlockState(blockPos).getBlock();
                if (!requiredBlocks.contains(block)) {
                    north = false;
                }
            }

            if (east) {
                BlockPos blockPos = new BlockPos(originPos.offset(structurePos.rotate(Rotation.CLOCKWISE_90).subtract(offsetPos.rotate(Rotation.CLOCKWISE_90))));
                Block block = level.getBlockState(blockPos).getBlock();
                if (!requiredBlocks.contains(block)) {
                    east = false;
                }
            }

            if (south) {
                BlockPos blockPos = new BlockPos(originPos.offset(structurePos.rotate(Rotation.CLOCKWISE_180).subtract(offsetPos.rotate(Rotation.CLOCKWISE_180))));
                Block block = level.getBlockState(blockPos).getBlock();
                if (!requiredBlocks.contains(block)) {
                    south = false;
                }
            }

            if (west) {
                BlockPos blockPos = new BlockPos(originPos.offset(structurePos.rotate(Rotation.COUNTERCLOCKWISE_90).subtract(offsetPos.rotate(Rotation.COUNTERCLOCKWISE_90))));
                Block block = level.getBlockState(blockPos).getBlock();
                if (!requiredBlocks.contains(block)) {
                    west = false;
                }
            }
        }

        return north || east || south || west;
    }

    @Override
    public void buildAt(Level level, BlockPos originPos, Vec3 offset, Rotation rotation)
    {
        BlockPos offsetPos = offset.blockPos().rotate(rotation);
        for (Map.Entry<Vec3, Character> entry : this.blockKeys.entrySet()) {
            if (!this.blockMap.containsKey(entry.getValue())) {
                continue;
            }
            List<Block> blocks = this.blockMap.get(entry.getValue()).stream().filter(CachedObject::isPresent).map(CachedObject::get).toList();
            if (blocks.isEmpty()) {
                continue;
            }

            BlockPos structureBlockPos = entry.getKey().blockPos().rotate(rotation);
            BlockPos buildPos = originPos.offset(structureBlockPos.subtract(offsetPos));
            level.setBlock(buildPos, blocks.get(0).defaultBlockState(), 3);
        }
    }
}

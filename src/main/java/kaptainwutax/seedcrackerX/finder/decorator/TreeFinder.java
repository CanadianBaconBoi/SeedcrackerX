package kaptainwutax.seedcrackerX.finder.decorator;

import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.seedcrackerX.Features;
import kaptainwutax.seedcrackerX.SeedCracker;
import kaptainwutax.seedcrackerX.cracker.decorator.Tree;
import kaptainwutax.seedcrackerX.cracker.decorator.decoratorData.TreeData;
import kaptainwutax.seedcrackerX.finder.BlockFinder;
import kaptainwutax.seedcrackerX.finder.Finder;
import kaptainwutax.seedcrackerX.render.Color;
import kaptainwutax.seedcrackerX.render.Cube;
import kaptainwutax.seedcrackerX.render.Renderer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.List;

public class TreeFinder extends Finder {

    private final List<TreeData> treeDataList = new ArrayList<>();
    private final List<Renderer> importantLeaves = new ArrayList<>();
    private final List<Renderer> finalRenderers = new ArrayList<>();

    public TreeFinder(World world, ChunkPos chunkPos) {
        super(world, chunkPos);
    }

    @Override
    public List<BlockPos> findInChunk() {
        List<BlockPos> result = new ArrayList<>();

        BirchFinder birchFinder = new BirchFinder(this.world, this.chunkPos, this.chunkPos.getStartPos());
        OakFinder oakFinder = new OakFinder(this.world, this.chunkPos, this.chunkPos.getStartPos());
        result.addAll(birchFinder.findInChunk());
        result.addAll(oakFinder.findInChunk());

        result.removeIf(pos -> {
            importantLeaves.clear();
            if (!this.world.getBlockState(pos.add(0, -1, 0)).getBlock().equals(Blocks.DIRT))
                return true;
            Block type = this.world.getBlockState(pos).getBlock();
            int height = 0;
            while (this.world.getBlockState(pos.add(0,height,0)).getBlock() == type) {
                height++;
            }
            BlockPos trunk = pos.add(0,height,0).mutableCopy();
            Block leaves = this.world.getBlockState(trunk).getBlock();
            if (leaves.equals(Blocks.OAK_LEAVES)) {
                if (height < 4 || height > 6)
                    return true;
            } else if (leaves.equals(Blocks.BIRCH_LEAVES)) {
                if (height < 5 || height > 7)
                    return true;
            } else {
                return true;
            }
            TreeData data = new TreeData();

            BlockPos leaveLayer = trunk.add(0, -1,0);

            //this.renderers.add(new Cube(leaveLayer, new Color(255, 0, 0)));
            if (testLeaves(leaveLayer, leaves, 2, data))
                data.complete = false;

            leaveLayer = leaveLayer.add(0, -1, 0);

            if (testLeaves(leaveLayer, leaves, 3, data))
                data.complete = false;

            leaveLayer = leaveLayer.add(0, -1, 0);

            if (testLeaves(leaveLayer, leaves, 3, data))
                data.complete = false;

            data.setType(type);
            data.pos = new BlockPos(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
            data.height = height;

            treeDataList.add(data);
            if (data.complete) {
                finalRenderers.addAll(importantLeaves);
            }
            return false;
        });

        if (treeDataList.stream().filter(data -> data.complete).count() < 3)
            return new ArrayList<>();

        Tree.Data data = Features.TREE.at(this.chunkPos.x, this.chunkPos.z, Biomes.FOREST, treeDataList);

        if(SeedCracker.get().getDataStorage().addBaseData(data, data::onDataAdded)) {
            result.forEach(pos -> finalRenderers.add(new Cube(pos, new Color(0, 0, 255))));
            this.renderers.addAll(finalRenderers);
        }
        return result;
    }

    private boolean testLeaves(BlockPos leaveLayer, Block leaves, int size, TreeData data) {
        for (int x = -size; x <=size; x++) {
            for (int z = -size; z <=size; z++) {
                if (Math.abs(x) != size || Math.abs(z) != size) {
                    Block block = this.world.getBlockState(leaveLayer.add(x, 0, z)).getBlock();
                    if (Math.abs(x) == size || Math.abs(z) == size) {
                        if (block.equals(leaves))
                            return true;
                    } else if (Math.abs(x) == size-1 && Math.abs(z) == size-1) {
                        if (block.equals(leaves)) {
                            data.leaves.add(1);
                            importantLeaves.add(new Cube(leaveLayer.add(x, 0, z), new Color(0, 255, 0)));
                        } else if(block.equals(Blocks.AIR)) {
                            data.leaves.add(0);
                            importantLeaves.add(new Cube(leaveLayer.add(x, 0, z), new Color(255, 0, 0)));
                        } else {
                            data.leaves.add(-1);
                            return true;
                        }
                    } else {
                        if (x != 0 || z != 0) {
                            if (!block.equals(leaves))
                                return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isValidDimension(DimensionType dimension) {
        return this.isOverworld(dimension);
    }

    private static class BirchFinder extends BlockFinder {

        protected static List<BlockPos> SEARCH_POSITIONS = buildSearchPositions(CHUNK_POSITIONS, pos -> {
            return pos.getY() < 62;
        });

        public BirchFinder(World world, ChunkPos chunkPos, BlockPos xz) {
            super(world, chunkPos, Blocks.BIRCH_LOG);
            this.searchPositions = SEARCH_POSITIONS;
        }

        @Override
        public List<BlockPos> findInChunk() {
            return super.findInChunk();
        }

        @Override
        public boolean isValidDimension(DimensionType dimension) {
            return true;
        }

    }
    private static class OakFinder extends BlockFinder {

        protected static List<BlockPos> SEARCH_POSITIONS = buildSearchPositions(CHUNK_POSITIONS, pos -> {
            return pos.getY() < 62;
        });

        public OakFinder(World world, ChunkPos chunkPos, BlockPos xz) {
            super(world, chunkPos, Blocks.OAK_LOG);
            this.searchPositions = SEARCH_POSITIONS;
        }

        @Override
        public List<BlockPos> findInChunk() {
            return super.findInChunk();
        }

        @Override
        public boolean isValidDimension(DimensionType dimension) {
            return true;
        }

    }

    public static List<Finder> create(World world, ChunkPos chunkPos) {
        List<Finder> finders = new ArrayList<>();
        finders.add(new TreeFinder(world, chunkPos));

        finders.add(new TreeFinder(world, new ChunkPos(chunkPos.x - 1, chunkPos.z)));
        finders.add(new TreeFinder(world, new ChunkPos(chunkPos.x, chunkPos.z - 1)));
        finders.add(new TreeFinder(world, new ChunkPos(chunkPos.x - 1, chunkPos.z - 1)));

        finders.add(new TreeFinder(world, new ChunkPos(chunkPos.x + 1, chunkPos.z)));
        finders.add(new TreeFinder(world, new ChunkPos(chunkPos.x, chunkPos.z + 1)));
        finders.add(new TreeFinder(world, new ChunkPos(chunkPos.x + 1, chunkPos.z + 1)));

        finders.add(new TreeFinder(world, new ChunkPos(chunkPos.x + 1, chunkPos.z - 1)));
        finders.add(new TreeFinder(world, new ChunkPos(chunkPos.x - 1, chunkPos.z + 1)));
        return finders;
    }
}
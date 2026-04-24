package myst.synthetic.world.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.world.terrain.BedrockProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class MystChunkGenerator extends ChunkGenerator {

    public static final MapCodec<MystChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MystChunkGeneratorSettings.CODEC.fieldOf("settings").forGetter(MystChunkGenerator::settings)
    ).apply(instance, MystChunkGenerator::new));

    private static final int MIN_Y = 0;
    private static final int GEN_DEPTH = 384;

    private static final AtomicInteger CONSTRUCTOR_LOGS = new AtomicInteger();
    private static final AtomicInteger FILL_LOGS = new AtomicInteger();
    private static final AtomicInteger HEIGHT_LOGS = new AtomicInteger();
    private static final AtomicInteger COLUMN_LOGS = new AtomicInteger();

    private final MystChunkGeneratorSettings settings;

    public MystChunkGenerator(MystChunkGeneratorSettings settings) {
        super(
                new FixedBiomeSource(settings.biome()),
                holder -> holder.value().getGenerationSettings()
        );
        this.settings = settings;

        if (CONSTRUCTOR_LOGS.getAndIncrement() < 20) {
            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystGen] Constructed MystChunkGenerator: seed={}, biome={}, ground={}, sea={}, bedrock={}",
                    settings.seed(),
                    settings.biomeId(),
                    settings.groundLevel(),
                    settings.seaLevel(),
                    settings.bedrockProfile()
            );
        }
    }

    public MystChunkGeneratorSettings settings() {
        return this.settings;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Blender blender,
            RandomState randomState,
            StructureManager structureManager,
            ChunkAccess chunk
    ) {
        if (FILL_LOGS.getAndIncrement() < 200) {
            MystcraftSyntheticCodex.LOGGER.info(
                    "[MystGen] fillFromNoise chunk={}, biome={}, ground={}, sea={}",
                    chunk.getPos(),
                    this.settings.biomeId(),
                    this.settings.groundLevel(),
                    this.settings.seaLevel()
            );
        }

        buildChunk(chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void applyCarvers(
            WorldGenRegion region,
            long seed,
            RandomState randomState,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk
    ) {
    }

    @Override
    public void buildSurface(
            WorldGenRegion region,
            StructureManager structureManager,
            RandomState randomState,
            ChunkAccess chunk
    ) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() {
        return GEN_DEPTH;
    }

    @Override
    public int getSeaLevel() {
        return this.settings.seaLevel();
    }

    @Override
    public int getMinY() {
        return MIN_Y;
    }

    @Override
    public int getBaseHeight(
            int x,
            int z,
            Heightmap.Types heightmap,
            LevelHeightAccessor level,
            RandomState randomState
    ) {
        if (HEIGHT_LOGS.getAndIncrement() < 100) {
            MystcraftSyntheticCodex.LOGGER.info("[MystGen] getBaseHeight x={}, z={}, type={}", x, z, heightmap);
        }

        return Math.max(this.settings.groundLevel(), this.settings.seaLevel()) + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(
            int x,
            int z,
            LevelHeightAccessor level,
            RandomState randomState
    ) {
        if (COLUMN_LOGS.getAndIncrement() < 100) {
            MystcraftSyntheticCodex.LOGGER.info("[MystGen] getBaseColumn x={}, z={}", x, z);
        }

        BlockState[] states = new BlockState[this.getGenDepth()];
        for (int i = 0; i < states.length; i++) {
            states[i] = Blocks.AIR.defaultBlockState();
        }

        fillColumn(states, x, z);
        return new NoiseColumn(this.getMinY(), states);
    }

    @Override
    public void addDebugScreenInfo(List<String> lines, RandomState randomState, BlockPos pos) {
        lines.add("Mystcraft Generator");
        lines.add("Biome: " + this.settings.biomeId());
        lines.add("Ground Level: " + this.settings.groundLevel());
        lines.add("Sea Level: " + this.settings.seaLevel());
        lines.add("Bedrock Profile: " + this.settings.bedrockProfile().name());
    }

    private void buildChunk(ChunkAccess chunk) {
        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        ColumnMaterials materials = resolveColumnMaterials();

        for (int localX = 0; localX < 16; localX++) {
            int worldX = startX + localX;

            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = startZ + localZ;

                boolean[] bedrockMask = buildBedrockMask(worldX, worldZ);

                int maxY = Math.max(this.settings.groundLevel(), this.settings.seaLevel());
                for (int y = this.getMinY(); y <= maxY; y++) {
                    BlockState state = pickStateForY(y, materials, bedrockMask);
                    if (state.isAir()) {
                        continue;
                    }

                    pos.set(worldX, y, worldZ);
                    chunk.setBlockState(pos, state, 0);
                }
            }
        }
    }

    private void fillColumn(BlockState[] states, int worldX, int worldZ) {
        ColumnMaterials materials = resolveColumnMaterials();
        boolean[] bedrockMask = buildBedrockMask(worldX, worldZ);

        int maxY = Math.min(states.length - 1, Math.max(this.settings.groundLevel(), this.settings.seaLevel()));
        for (int y = this.getMinY(); y <= maxY; y++) {
            BlockState state = pickStateForY(y, materials, bedrockMask);
            states[y - this.getMinY()] = state == null ? Blocks.AIR.defaultBlockState() : state;
        }
    }

    private BlockState pickStateForY(int y, ColumnMaterials materials, boolean[] bedrockMask) {
        if (this.settings.bedrockProfile() == BedrockProfile.VANILLA_FLOOR
                && y >= 0
                && y < bedrockMask.length
                && bedrockMask[y]) {
            return Blocks.BEDROCK.defaultBlockState();
        }

        if (y > this.settings.groundLevel()) {
            return y <= this.settings.seaLevel()
                    ? materials.fluid
                    : Blocks.AIR.defaultBlockState();
        }

        if (y == this.settings.groundLevel()) {
            return materials.top;
        }

        if (y >= Math.max(1, this.settings.groundLevel() - materials.underDepth + 1)) {
            return materials.under;
        }

        return materials.base;
    }

    private boolean[] buildBedrockMask(int worldX, int worldZ) {
        boolean[] mask = new boolean[5];

        if (this.settings.bedrockProfile() == BedrockProfile.NONE) {
            return mask;
        }

        RandomSource random = RandomSource.create(
                this.settings.seed()
                        ^ (((long) worldX) * 341873128712L)
                        ^ (((long) worldZ) * 132897987541L)
        );

        mask[0] = true;
        for (int y = 1; y <= 4; y++) {
            mask[y] = y <= random.nextInt(5);
        }

        return mask;
    }

    private ColumnMaterials resolveColumnMaterials() {
        String path = this.settings.biomeIdentifier().getPath();

        if (path.contains("desert") || path.contains("beach")) {
            return new ColumnMaterials(
                    Blocks.SAND.defaultBlockState(),
                    Blocks.SAND.defaultBlockState(),
                    Blocks.SANDSTONE.defaultBlockState(),
                    Blocks.WATER.defaultBlockState(),
                    4
            );
        }

        if (path.contains("badlands")) {
            return new ColumnMaterials(
                    Blocks.RED_SAND.defaultBlockState(),
                    Blocks.RED_SAND.defaultBlockState(),
                    Blocks.RED_SANDSTONE.defaultBlockState(),
                    Blocks.WATER.defaultBlockState(),
                    4
            );
        }

        if (path.contains("deep_dark")) {
            return new ColumnMaterials(
                    Blocks.DEEPSLATE.defaultBlockState(),
                    Blocks.DEEPSLATE.defaultBlockState(),
                    Blocks.DEEPSLATE.defaultBlockState(),
                    Blocks.WATER.defaultBlockState(),
                    2
            );
        }

        return new ColumnMaterials(
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                4
        );
    }

    private record ColumnMaterials(
            BlockState top,
            BlockState under,
            BlockState base,
            BlockState fluid,
            int underDepth
    ) {
    }
}
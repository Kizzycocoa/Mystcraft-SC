package myst.synthetic.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import myst.synthetic.MystcraftSyntheticCodex;
import myst.synthetic.world.age.AgeRegistryData;
import myst.synthetic.world.dimension.AgeDimensionKeys;
import myst.synthetic.world.age.AgeRenderDataSynchronizer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import myst.synthetic.linking.LinkController;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class TpxCommand {

    private TpxCommand() {
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tpx")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("dimension", StringArgumentType.word())
                                .suggests(TpxCommand::suggestDimensions)
                                .executes(context -> teleport(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "dimension"),
                                        null
                                ))
                                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes(context -> teleport(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "dimension"),
                                                                new Vec3(
                                                                        DoubleArgumentType.getDouble(context, "x"),
                                                                        DoubleArgumentType.getDouble(context, "y"),
                                                                        DoubleArgumentType.getDouble(context, "z")
                                                                )
                                                        ))
                                                )
                                        )
                                )
                        )
        );
    }

    private static int teleport(CommandSourceStack source, String rawDimension, Vec3 requestedPosition) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("Only players can use /tpx right now."));
            return 0;
        }

        MinecraftServer server = source.getServer();
        ServerLevel destination = resolveLevel(server, rawDimension);

        if (destination == null) {
            source.sendFailure(Component.literal("Unknown dimension: " + rawDimension));
            return 0;
        }

        final Vec3 targetPosition = requestedPosition != null
                ? requestedPosition
                : findSafeArrival(destination, player);

        MystcraftSyntheticCodex.LOGGER.info(
                "[MystTPX] Teleporting '{}' from '{}' to '{}' at {}.",
                player.getScoreboardName(),
                player.level().dimension().identifier(),
                destination.dimension().identifier(),
                targetPosition
        );

        BlockPos targetBlock = BlockPos.containing(targetPosition);

        if (!LinkController.travelEntityToLevel(player, destination, targetBlock, player.getYRot())) {
            source.sendFailure(Component.literal("Teleport failed."));
            return 0;
        }

        AgeRenderDataSynchronizer.sendForLevel(player, destination);

        final String destinationName = destination.dimension().identifier().toString();
        final String targetX = formatCoord(targetPosition.x);
        final String targetY = formatCoord(targetPosition.y);
        final String targetZ = formatCoord(targetPosition.z);

        source.sendSuccess(
                () -> Component.literal(
                        "Teleported to " + destinationName
                                + " at "
                                + targetX + " "
                                + targetY + " "
                                + targetZ
                ),
                true
        );

        return 1;
    }

    private static Vec3 findSafeArrival(ServerLevel destination, ServerPlayer player) {
        int x = player.blockPosition().getX();
        int z = player.blockPosition().getZ();

        int y = destination.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos surface = new BlockPos(x, y, z);

        if (isSafeStandingPos(destination, surface)) {
            return Vec3.atBottomCenterOf(surface);
        }

        BlockPos originSurface = new BlockPos(0, destination.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0), 0);
        if (isSafeStandingPos(destination, originSurface)) {
            return Vec3.atBottomCenterOf(originSurface);
        }

        return new Vec3(0.5D, Math.max(destination.getSeaLevel() + 2, 66), 0.5D);
    }

    private static boolean isSafeStandingPos(ServerLevel level, BlockPos pos) {
        BlockPos head = pos.above();
        BlockPos ground = pos.below();

        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(head).getCollisionShape(level, head).isEmpty()
                && !level.getBlockState(ground).getCollisionShape(level, ground).isEmpty();
    }

    private static ServerLevel resolveLevel(MinecraftServer server, String rawDimension) {
        Identifier id = normalizeDimensionId(rawDimension);
        if (id == null) {
            return null;
        }

        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, id);
        return server.getLevel(key);
    }

    private static Identifier normalizeDimensionId(String rawDimension) {
        if (rawDimension == null || rawDimension.isBlank()) {
            return null;
        }

        String raw = rawDimension.trim();
        String lower = raw.toLowerCase(Locale.ROOT);

        if ("overworld".equals(lower) || "minecraft:overworld".equals(lower)) {
            return Identifier.fromNamespaceAndPath("minecraft", "overworld");
        }

        if ("nether".equals(lower) || "the_nether".equals(lower) || "minecraft:the_nether".equals(lower)) {
            return Identifier.fromNamespaceAndPath("minecraft", "the_nether");
        }

        if ("end".equals(lower) || "the_end".equals(lower) || "minecraft:the_end".equals(lower)) {
            return Identifier.fromNamespaceAndPath("minecraft", "the_end");
        }

        if (lower.matches("dim\\d+")) {
            return AgeDimensionKeys.location(lower);
        }

        Identifier parsed = Identifier.tryParse(raw);
        if (parsed != null) {
            return parsed;
        }

        return Identifier.fromNamespaceAndPath("minecraft", lower);
    }

    private static CompletableFuture<Suggestions> suggestDimensions(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        MinecraftServer server = context.getSource().getServer();

        suggest(builder, "minecraft:overworld");
        suggest(builder, "minecraft:the_nether");
        suggest(builder, "minecraft:the_end");
        suggest(builder, "overworld");
        suggest(builder, "nether");
        suggest(builder, "end");

        for (ServerLevel level : server.getAllLevels()) {
            suggest(builder, level.dimension().identifier().toString());
        }

        AgeRegistryData registry = AgeRegistryData.get(server.overworld());
        for (AgeRegistryData.AgeEntry entry : registry.ages().values()) {
            suggest(builder, AgeDimensionKeys.levelIdString(entry.dimensionUid()));
            suggest(builder, entry.dimensionUid().toLowerCase(Locale.ROOT));
        }

        return builder.buildFuture();
    }

    private static void suggest(SuggestionsBuilder builder, String value) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        if (value.toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(value);
        }
    }

    private static String formatCoord(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001D) {
            return Integer.toString((int) Math.rint(value));
        }

        return String.format(Locale.ROOT, "%.2f", value);
    }
}
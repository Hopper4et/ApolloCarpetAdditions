package Hopper4et.apollocarpetadditions.rules;


import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import Hopper4et.apollocarpetadditions.mixins.EndGatewayBlockEntityAccessor;
import Hopper4et.apollocarpetadditions.mixins.EndGatewayBlockEntityInvoker;
import Hopper4et.apollocarpetadditions.mixins.NetherPortalBlockInvoker;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;

import java.util.Optional;

public abstract class SpectatorCanUsePortals implements EndGatewayBlockEntityAccessor {
    public static void spectatorMove(PlayerEntity player) {
        if (
                !player.isSpectator() ||
                !ApolloCarpetAdditionsSettings.spectatorCanUsePortals ||
                !player.canUsePortals(false)
        ) {
            return;
        }

        Vec3d playerPos = player.getEyePos();
        BlockPos playerBlockPos = BlockPos.ofFloored(playerPos);
        ServerWorld world = (ServerWorld) player.getWorld();

        BlockState blockState = world.getBlockState(playerBlockPos);
        Block block = blockState.getBlock();

        if (block == Blocks.END_PORTAL || block == Blocks.NETHER_PORTAL || block == Blocks.END_GATEWAY) {
            if (player.hasPortalCooldown()) {
                player.resetPortalCooldown();
                return;
            }
            player.resetPortalCooldown();
            TeleportTarget target = getTeleportTarget(player, block, world, playerBlockPos);
            if (target != null) {
                player.teleportTo(target);
            }
        }
    }

    private static TeleportTarget getTeleportTarget(PlayerEntity player, Block block, ServerWorld originWorld, BlockPos portalBlockPos) {

        if (block == Blocks.END_PORTAL) {

            //End portal

            RegistryKey<World> destinationWorldKey = originWorld.getRegistryKey() == World.END ? World.OVERWORLD : World.END;
            ServerWorld destinationWorld = originWorld.getServer().getWorld(destinationWorldKey);
            if (destinationWorld == null) {
                return null;
            }
            boolean destinationIsEnd = destinationWorldKey == World.END;
            BlockPos destinationBlockPos = destinationIsEnd ?
                    ServerWorld.END_SPAWN_POS :
                    destinationWorld.getSpawnPos();

            Vec3d destinationPos = destinationBlockPos.toBottomCenterPos();

            if (destinationIsEnd) {
                return new TeleportTarget(
                        destinationWorld, destinationPos,
                        Vec3d.ZERO, 90.0F, 0.0F,
                        TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET
                );
            } else {
                return ((ServerPlayerEntity) player).getRespawnTarget(false, TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET);
            }

        } else if (block == Blocks.NETHER_PORTAL) {

            //Nether portal

            RegistryKey<World> destinationWorldKey = originWorld.getRegistryKey() == World.NETHER ? World.OVERWORLD : World.NETHER;
            ServerWorld destinationWorld = originWorld.getServer().getWorld(destinationWorldKey);
            if (destinationWorld == null) {
                return null;
            }
            boolean destinationIsNether = destinationWorld.getRegistryKey() == World.NETHER;
            WorldBorder worldBorder = destinationWorld.getWorldBorder();
            double scaleFactor = DimensionType.getCoordinateScaleFactor(originWorld.getDimension(), destinationWorld.getDimension());
            BlockPos scaledBlockPos = worldBorder.clampFloored(player.getX() * scaleFactor, player.getY(), player.getZ() * scaleFactor);
            Optional<BlockPos> destinationPortals = destinationWorld.getPortalForcer().getPortalPos(scaledBlockPos, destinationIsNether, worldBorder);
            BlockLocating.Rectangle rectangle;
            if (destinationPortals.isEmpty()) {
                return null;
            }
            BlockPos destinationblockPos = destinationPortals.get();
            BlockState destinationblockState = destinationWorld.getBlockState(destinationblockPos);
            rectangle = BlockLocating.getLargestRectangle(destinationblockPos, destinationblockState.get(Properties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, (posX) -> destinationWorld.getBlockState(posX) == destinationblockState);

            return NetherPortalBlockInvoker.getExitPortalTarget(player, portalBlockPos, rectangle, destinationWorld, TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET);

        } else {

            // End gateway

            BlockEntity blockEntity = originWorld.getBlockEntity(portalBlockPos);
            if (!(blockEntity instanceof EndGatewayBlockEntity endGatewayBlockEntity)) {
                return null;
            }
            BlockPos targetBlockPos = ((EndGatewayBlockEntityAccessor) endGatewayBlockEntity).getExitPortalPos();
            if (targetBlockPos == null) {
                return null;
            }

            BlockPos destinationBlockPos =
                    ((EndGatewayBlockEntityAccessor) endGatewayBlockEntity).getExactTeleport() ?
                            targetBlockPos :
                            EndGatewayBlockEntityInvoker.invokeFindBestPortalExitPos(originWorld, targetBlockPos);

            assert destinationBlockPos != null;
            Vec3d destinationPos = destinationBlockPos.toBottomCenterPos();
            return new TeleportTarget(
                    originWorld, destinationPos,
                    Vec3d.ZERO, 0.0F, 0.0F,
                    PositionFlag.combine(PositionFlag.DELTA, PositionFlag.ROT),
                    TeleportTarget.NO_OP
            );
        }

    }



}

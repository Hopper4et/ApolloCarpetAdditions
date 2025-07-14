package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.rules.SpectatorCanUsePortals;
import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.impl.util.log.Log;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Arrays;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Override
    public void move(MovementType type, Vec3d movement) {
        super.move(type, movement);
        SpectatorCanUsePortals.spectatorMove(this);
    }
}

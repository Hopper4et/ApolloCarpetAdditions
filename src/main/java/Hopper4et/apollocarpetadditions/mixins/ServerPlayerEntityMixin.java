package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.rules.SpectatorCanUsePortals;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player {
    public ServerPlayerEntityMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }
    //todo probably added by mojang
    /*
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Override
    public void move(MovementType type, Vec3d movement) {
        super.move(type, movement);
        SpectatorCanUsePortals.spectatorMove(this);
    }
 */
}

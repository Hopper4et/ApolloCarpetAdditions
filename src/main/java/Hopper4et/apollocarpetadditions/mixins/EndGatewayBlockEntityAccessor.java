package Hopper4et.apollocarpetadditions.mixins;

import net.minecraft.block.entity.EndGatewayBlockEntity;

import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EndGatewayBlockEntity.class)
public interface EndGatewayBlockEntityAccessor {
    @Accessor
    BlockPos getExitPortalPos();

    @Accessor
    boolean getExactTeleport();
}

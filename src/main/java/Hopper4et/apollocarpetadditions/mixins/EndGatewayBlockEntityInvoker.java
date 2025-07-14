package Hopper4et.apollocarpetadditions.mixins;

import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EndGatewayBlockEntity.class)
public interface EndGatewayBlockEntityInvoker {
    @Invoker("findBestPortalExitPos")
    static BlockPos invokeFindBestPortalExitPos(World world, BlockPos pos) {
        return null;
    }
}

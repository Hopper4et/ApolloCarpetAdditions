package Hopper4et.apollocarpetadditions.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TheEndGatewayBlockEntity.class)
public interface EndGatewayBlockEntityInvoker {

    @Invoker("findExitPosition")
    static BlockPos invokeFindBestPortalExitPos(Level level, BlockPos exitPortal) {
        return null;
    }

}

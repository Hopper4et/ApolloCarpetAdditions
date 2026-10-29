package Hopper4et.apollocarpetadditions.mixins;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.commands.arguments.TimeArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TimeArgument.class)
public interface TimeArgumentAccessor {
    @Accessor("UNITS")
    static Object2IntMap<String> getTimeArgumentUnits() {
        throw new AssertionError();
    }
}

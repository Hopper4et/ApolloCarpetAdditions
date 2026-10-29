package Hopper4et.apollocarpetadditions.mixins;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsSettings;
import net.minecraft.server.commands.TickCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TickCommand.class)
public class TickCommandMixin {
    @ModifyArg(
            method = "lambda$register$8",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/commands/SharedSuggestionProvider;suggest([Ljava/lang/String;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;"
            ),
            index = 0
    )
    private static String[] changeTickSpringSuggest(String[] strings) {
        return ApolloCarpetAdditionsSettings.newTimeArgumentValues ? new String[]{"1m", "20m", "1h"} : new String[]{"60s", "1d", "3d"};
    }
}

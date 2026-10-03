package Hopper4et.apollocarpetadditions.commands;

import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class ClientCommandsServer {
    private ClientCommandsServer() {}

    public static void initialize() {
        FabricLoader loader = FabricLoader.getInstance();
        if (loader.getEnvironmentType() != EnvType.SERVER || loader.isModLoaded("clientcommands")) return;

        PayloadTypeRegistry.serverboundPlay().register(OptInPayload.TYPE, OptInPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(OptInPayload.TYPE, (_, _) -> {});
    }

    private enum OptInPayload implements CustomPacketPayload {
        INSTANCE;

        static final Type<OptInPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("clientcommands", "opt_in"));
        static final StreamCodec<ByteBuf, OptInPayload> CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<OptInPayload> type() {
            return TYPE;
        }
    }
}
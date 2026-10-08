package Hopper4et.apollocarpetadditions.utils;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.server.level.ServerPlayer;

public final class FakePlayers {
	/** Bots from /player */
	public static boolean isFake(ServerPlayer player) {
		return player instanceof EntityPlayerMPFake;
	}
}

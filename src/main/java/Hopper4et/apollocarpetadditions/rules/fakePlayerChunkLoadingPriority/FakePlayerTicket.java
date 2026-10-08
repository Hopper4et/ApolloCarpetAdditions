package Hopper4et.apollocarpetadditions.rules.fakePlayerChunkLoadingPriority;

import Hopper4et.apollocarpetadditions.ApolloCarpetAdditionsServer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.TicketType;

/**
 * Loading ticket that fake players get on every chunk within view distance, without going through the vanilla
 * player ticket throttler. Loading only (simulation stays on the vanilla player_simulation ticket), no timeout,
 * not saved to disk, so removing the mod leaves nothing behind in the world.
 */
public final class FakePlayerTicket {
	public static TicketType TYPE;

	private FakePlayerTicket() {
	}

	/** Called from TicketTypeMixin while vanilla registers its own ticket types: mod entrypoints run after the registries are frozen. */
	public static void register() {
		TYPE = Registry.register(
			BuiltInRegistries.TICKET_TYPE,
			Identifier.fromNamespaceAndPath(ApolloCarpetAdditionsServer.MOD_ID.toLowerCase(), "fake_player"),
			new TicketType(TicketType.NO_TIMEOUT, TicketType.FLAG_LOADING)
		);
	}
}

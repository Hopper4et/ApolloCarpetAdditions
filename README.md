<img src="./src/main/resources/assets/apollocarpetadditions/icon.png" align="right" width="128px"/>

# ApolloCarpetAdditions

ApolloCarpetAdditions is an extension to [Carpet mod] from the [Apollotech SMP minecraft server]

[Carpet mod]: https://github.com/gnembon/fabric-carpet
[Apollotech SMP minecraft server]: https://discord.gg/xtUtuQjrkz

# Carpet Mod Settings
## endGatewaysLoadChunks
End gateways load chunks when an entity teleports through them.
* Type: `Boolean`
* Default value: `true`
* Allowed options: `true`, `false`
* Categories: `APOLLO`, `CREATIVE`

## lazyLinkingPre1_21Render
Returns the behavior of server sending packets with entity coordinates (MC-170907). This fixes lazy linking mob render.
* Type: `Boolean`
* Default value: `false`
* Allowed options: `true`, `false`
* Categories: `APOLLO`, `BUGFIX`

## pigCannonUnstuck
Fixed chunk loading behavior when player is a passenger of a very fast entity.
* Type: `Boolean`
* Default value: `false`
* Allowed options: `true`, `false`
* Categories: `APOLLO`, `BUGFIX`

## playerCommandNonOperatorSpawnInGamemode
Specifies in which gamemodes non-operators can spawn players with /player command.
* Type: `GamemodesOptions`
* Default value: `NONE`
* Allowed options: `NONE`, `SURVIVAL`, `SPECTATOR`, `SURVIVAL_SPECTATOR`, `ALL`
* Categories: `APOLLO`, `COMMAND`, `SURVIVAL`

## spectatorCanUsePortals
Allows spectators to teleport through Nether portals, End portals and End gateways without affecting the world.
* Type: `Boolean`
* Default value: `false`
* Allowed options: `true`, `false`
* Categories: `APOLLO`, `CREATIVE`

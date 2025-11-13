<img src="./src/main/resources/assets/apollocarpetadditions/icon.png" width="128" alt=""/>

# ApolloCarpetAdditions

ApolloCarpetAdditions is an extension to [Carpet mod] by [Apollotech SMP RU minecraft server]

[Carpet mod]: https://github.com/gnembon/fabric-carpet
[Apollotech SMP RU minecraft server]: https://discord.gg/xtUtuQjrkz

# Carpet rules:

## endGatewaysLoadChunks
End gateways load chunks when an entity teleports through them.
* Type: `Boolean`
* Default value: `true`
* Allowed options: `true`, `false`
* Categories: `APOLLO`, `CREATIVE`

## spectatorCanUsePortals
Allows spectators to teleport through Nether portals, End portals and End gateways without affecting the world.
* Type: `Boolean`
* Default value: `false`
* Allowed options: `true`, `false`
* Categories: `APOLLO`, `CREATIVE`



## pigCannonUnstuck
Fixed chunk loading behavior when player is a passenger of a very fast entity.
* Type: `Boolean`
* Default value: `false`
* Allowed options: `true`, `false`
* Categories: `APOLLO`, `BUGFIX`

## lazyLinkingPre1_21Render
Returns the behavior of server sending packets with entity coordinates (MC-170907). This fixes lazy linking mob render.
* Type: `Boolean`
* Default value: `false`
* Allowed options: `true`, `false`
* Categories: `APOLLO`, `BUGFIX`

## portalNoClipFix
Fixes a bug that teleports player through a portal after touching it in a no-clip (MC-279021).
* Type: `Boolean`
* Default value: `false`
* Allowed options: `true`, `false`
* Categories: `APOLLO`, `BUGFIX`, `SPECTATOR`



## playerCommandNonOperatorSpawnInGamemode
Specifies in which gamemodes non-operators can spawn players with /player command.
* Type: `GamemodesOptions`
* Default value: `NONE`
* Allowed options: `NONE`, `SURVIVAL`, `SPECTATOR`, `SURVIVAL_SPECTATOR`, `ALL`
* Categories: `APOLLO`, `COMMAND`, `SURVIVAL`

## allowEditOtherPlayersMacros
Determines what permission level allows editing other players' macros via /macro command.
* Type: `String`
* Default value: `ops`
* Allowed options: `true`, `false`, `ops`, `1`, `2`, `3`, `4`
* Categories: `APOLLO`, `COMMAND`



## commandMacro
Allows to create and execute a sequence of commands with a delay.
* Type: `String`
* Default value: `ops`
* Allowed options: `true`, `false`, `ops`, `1`, `2`, `3`, `4`
* Categories: `APOLLO`, `COMMAND`

# Commands:

## /macro
Allows to create and execute a sequence of commands with a delay.  
### Usage:
* `/macro create {name}` Creates macro.
* `/macro delete {name}` Deletes macro.
* `/macro edit {name} add {delay} {command}` Adds command with delay to macro.
* `/macro run {name}` Runs macro.

### example:
`/macro create turnMobSwitchWithBot`  
`/macro edit turnMobSwitchWithBot add 0t player mob_switch spawn at 217 64 918 facing 90 0 in minecraft:the_nether in survival`  
`/macro edit turnMobSwitchWithBot add 5s player mob_switch use once`  
`/macro edit turnMobSwitchWithBot add 1s player mob_switch kill`  

`/macro run turnMobSwitchWithBot`

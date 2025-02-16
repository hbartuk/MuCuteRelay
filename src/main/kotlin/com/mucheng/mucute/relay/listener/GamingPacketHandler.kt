package com.mucheng.mucute.relay.listener

import com.mucheng.mucute.relay.MuCuteRelaySession
import com.mucheng.mucute.relay.definition.Definitions
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleNamedDefinition
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.CameraPresetsPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import org.cloudburstmc.protocol.common.NamedDefinition
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry

@Suppress("MemberVisibilityCanBePrivate")
class GamingPacketHandler(
    val muCuteRelaySession: MuCuteRelaySession
) : MuCuteRelayPacketListener {

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        if (packet is StartGamePacket) {
            println("Start game, setting definitions")
            Definitions.itemDefinitions = SimpleDefinitionRegistry.builder<ItemDefinition>()
                .addAll(packet.itemDefinitions)
                .build()

            muCuteRelaySession.client!!.peer.codecHelper.itemDefinitions = Definitions.itemDefinitions
            muCuteRelaySession.server.peer.codecHelper.itemDefinitions = Definitions.itemDefinitions

            if (packet.isBlockNetworkIdsHashed) {
                muCuteRelaySession.client!!.peer.codecHelper.blockDefinitions = Definitions.blockDefinitionsHashed
                muCuteRelaySession.server.peer.codecHelper.blockDefinitions = Definitions.blockDefinitionsHashed
            } else {
                muCuteRelaySession.client!!.peer.codecHelper.blockDefinitions = Definitions.blockDefinitions
                muCuteRelaySession.server.peer.codecHelper.blockDefinitions = Definitions.blockDefinitions
            }
        }
        if (packet is CameraPresetsPacket) {
            println("Camera presets")
            val cameraDefinitions =
                SimpleDefinitionRegistry.builder<NamedDefinition>()
                    .addAll(List(packet.presets.size) {
                        SimpleNamedDefinition(packet.presets[it].identifier, it)
                    })
                    .build()

            muCuteRelaySession.client!!.peer.codecHelper.cameraPresetDefinitions = cameraDefinitions
            muCuteRelaySession.server.peer.codecHelper.cameraPresetDefinitions = cameraDefinitions
        }
        return false
    }

}
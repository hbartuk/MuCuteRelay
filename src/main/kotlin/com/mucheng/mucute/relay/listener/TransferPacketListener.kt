package com.mucheng.mucute.relay.listener

import com.mucheng.mucute.relay.MuCuteRelaySession
import com.mucheng.mucute.relay.address.MuCuteAddress
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.TransferPacket

@Suppress("MemberVisibilityCanBePrivate")
class TransferPacketListener(
    val muCuteRelaySession: MuCuteRelaySession
) : MuCuteRelayPacketListener {

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        if (packet is TransferPacket) {
            val remoteAddress = MuCuteAddress(packet.address, packet.port)
            val localAddress = muCuteRelaySession.muCuteRelay.localAddress
            muCuteRelaySession.muCuteRelay.remoteAddress = remoteAddress
            muCuteRelaySession.clientBoundImmediately(TransferPacket().apply {
                address = localAddress.hostName
                port = localAddress.port
            })

            muCuteRelaySession.muCuteRelay.muCuteRelaySession = null
            return true
        }
        return false
    }

}
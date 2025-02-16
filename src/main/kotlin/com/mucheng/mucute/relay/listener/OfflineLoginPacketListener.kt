package com.mucheng.mucute.relay.listener

import com.mucheng.mucute.relay.MuCuteRelaySession
import com.mucheng.mucute.relay.util.AuthUtils
import org.cloudburstmc.protocol.bedrock.data.PacketCompressionAlgorithm
import org.cloudburstmc.protocol.bedrock.packet.*
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils
import org.cloudburstmc.protocol.bedrock.util.JsonUtils
import org.jose4j.json.JsonUtil
import org.jose4j.json.internal.json_simple.JSONObject
import org.jose4j.jws.JsonWebSignature
import java.security.KeyPair


@Suppress("MemberVisibilityCanBePrivate")
class OfflineLoginPacketListener(
    val muCuteRelaySession: MuCuteRelaySession,
    val keyPair: KeyPair = DefaultKeyPair
) : MuCuteRelayPacketListener {

    companion object {

        val DefaultKeyPair: KeyPair = EncryptionUtils.createKeyPair()

    }

    private var chain: List<String>? = null

    private var extraData: JSONObject? = null

    private var skinData: JSONObject? = null

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        if (packet is LoginPacket) {
            chain = packet.chain
            extraData =
                JSONObject(
                    JsonUtils.childAsType(
                        EncryptionUtils.validateChain(chain).rawIdentityClaims(),
                        "extraData",
                        Map::class.java
                    )
                )

            println("Handle offline login data")

            val jws = JsonWebSignature()
            jws.compactSerialization = packet.extra

            skinData = JSONObject(JsonUtil.parseJson(jws.unverifiedPayload))
            connectServer()
            return true
        }
        return false
    }

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        if (packet is NetworkSettingsPacket) {
            val threshold = packet.compressionThreshold
            if (threshold > 0) {
                muCuteRelaySession.client!!.setCompression(packet.compressionAlgorithm)
                println("Compression threshold set to $threshold")
            } else {
                muCuteRelaySession.client!!.setCompression(PacketCompressionAlgorithm.NONE)
                println("Compression threshold set to 0")
            }

            try {
                val chain = AuthUtils.fetchOfflineChain(keyPair, extraData!!, chain!!)
                val skinData = AuthUtils.fetchOfflineSkinData(keyPair, skinData!!)

                val loginPacket = LoginPacket()
                loginPacket.protocolVersion = muCuteRelaySession.server.codec.protocolVersion
                loginPacket.chain.addAll(chain)
                loginPacket.extra = skinData
                muCuteRelaySession.serverBoundImmediately(loginPacket)

                println("Login success")
            } catch (e: Throwable) {
                muCuteRelaySession.clientBound(DisconnectPacket().apply {
                    kickMessage = e.toString()
                })
                println("Login failed: $e")
            }

            return true
        }
        return super.beforeServerBound(packet)
    }

    private fun connectServer() {
        muCuteRelaySession.muCuteRelay.connectToServer {
            println("Connected to server")

            val packet = RequestNetworkSettingsPacket()
            packet.protocolVersion = muCuteRelaySession.server.codec.protocolVersion
            muCuteRelaySession.serverBoundImmediately(packet)
        }
    }

}
package com.mucheng.mucute.relay.listener

import com.mucheng.mucute.relay.MuCuteRelaySession
import com.mucheng.mucute.relay.util.AuthUtils
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import org.cloudburstmc.protocol.bedrock.data.PacketCompressionAlgorithm
import org.cloudburstmc.protocol.bedrock.packet.*
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils
import org.cloudburstmc.protocol.bedrock.util.JsonUtils
import org.jose4j.json.JsonUtil
import org.jose4j.json.internal.json_simple.JSONObject
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.HeaderParameterNames
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@Suppress("MemberVisibilityCanBePrivate")
class OnlineLoginPacketListener(
    val muCuteRelaySession: MuCuteRelaySession,
    val fullBedrockSession: StepFullBedrockSession.FullBedrockSession
) : MuCuteRelayPacketListener {

    private var skinData: JSONObject? = null

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        if (packet is LoginPacket) {
            if (fullBedrockSession.isExpired) {
                muCuteRelaySession.server.disconnect("Your session was expired, you need to delete account then login again in the MuCuteClient")
                return true
            }

            println("Handle online login data")

            val jws = JsonWebSignature()
            jws.compactSerialization = packet.extra

            skinData = JSONObject(JsonUtil.parseJson(jws.unverifiedPayload))
            connectServer()
            return true
        }
        return false
    }

    @OptIn(ExperimentalEncodingApi::class)
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
                val chain = AuthUtils.fetchOnlineChain(fullBedrockSession)
                val skinData =
                    AuthUtils.fetchOnlineSkinData(
                        fullBedrockSession,
                        skinData!!,
                        muCuteRelaySession.muCuteRelay.remoteAddress!!
                    )

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
        if (packet is ServerToClientHandshakePacket) {
            val jws = JsonWebSignature().apply {
                compactSerialization = packet.jwt
            }

            val saltJwt = JSONObject(JsonUtil.parseJson(jws.unverifiedPayload))
            val x5u = jws.getHeader(HeaderParameterNames.X509_URL)
            val serverKey = EncryptionUtils.parseKey(x5u)
            val key = EncryptionUtils.getSecretKey(
                fullBedrockSession.mcChain.privateKey, serverKey,
                Base64.decode(JsonUtils.childAsType(saltJwt, "salt", String::class.java))
            )
            muCuteRelaySession.client!!.enableEncryption(key)
            println("Encryption enabled")

            muCuteRelaySession.serverBoundImmediately(ClientToServerHandshakePacket())
            return true
        }
        return false
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
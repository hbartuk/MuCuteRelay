import com.mucheng.mucute.relay.MuCuteRelaySession
import com.mucheng.mucute.relay.listener.MuCuteRelayPacketListener
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

@Suppress("MemberVisibilityCanBePrivate")
class MessagePacketListener(
    val muCuteRelaySession: MuCuteRelaySession
) : MuCuteRelayPacketListener {

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        if (packet is PlayerAuthInputPacket && packet.tick % 10 == 0L) {
            muCuteRelaySession.clientBound(TextPacket().apply {
                type = TextPacket.Type.TIP
                isNeedsTranslation = false
                sourceName = ""
                message = "[MuCuteRelay] v1.0"
                xuid = ""
                filteredMessage = ""
            })
        }
        return false
    }

}
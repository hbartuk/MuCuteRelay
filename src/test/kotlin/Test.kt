import com.mucheng.mucute.relay.address.MuCuteAddress
import com.mucheng.mucute.relay.definition.Definitions
import com.mucheng.mucute.relay.listener.AutoCodecPacketListener
import com.mucheng.mucute.relay.listener.GamingPacketHandler
import com.mucheng.mucute.relay.listener.TransferPacketListener
import com.mucheng.mucute.relay.listener.OnlineLoginPacketListener
import com.mucheng.mucute.relay.util.authorize
import com.mucheng.mucute.relay.util.captureGamePacket
import com.mucheng.mucute.relay.util.refresh
import net.raphimc.minecraftauth.MinecraftAuth

fun main() {
    val localAddress = MuCuteAddress("0.0.0.0", 19132)
    val remoteAddress = MuCuteAddress("ntest.easecation.net", 19132)

    Definitions.loadBlockPalette()

    var fullBedrockSession = authorize()
    if (fullBedrockSession.isExpired) {
        fullBedrockSession = fullBedrockSession.refresh()
    }

    captureGamePacket(
        localAddress = localAddress,
        remoteAddress = remoteAddress
    ) {
        listeners.add(AutoCodecPacketListener(this))
        listeners.add(OnlineLoginPacketListener(this, fullBedrockSession))
        listeners.add(GamingPacketHandler(this))
        listeners.add(TransferPacketListener(this))
        listeners.add(MessagePacketListener(this))
    }
    println("Relay started at ${localAddress.hostName}:${localAddress.port}")
}
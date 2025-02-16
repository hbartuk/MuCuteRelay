package com.mucheng.mucute.relay.address

import java.net.InetSocketAddress

data class MuCuteAddress(val hostName: String, val port: Int)

inline val MuCuteAddress.inetSocketAddress
    get() = InetSocketAddress(hostName, port)

inline val InetSocketAddress.muCuteAddress
    get() = MuCuteAddress(hostName, port)
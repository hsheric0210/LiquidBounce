package net.ccbluex.liquidbounce.injection.backend

import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ISPacketCustomPayload
import net.minecraft.network.play.server.S3FPacketCustomPayload

class SPacketCustomPayloadImpl<T : S3FPacketCustomPayload>(wrapped: T) : PacketImpl<T>(wrapped), ISPacketCustomPayload
{
	override val channelName: String
		get() = wrapped.channelName
}

inline fun ISPacketCustomPayload.unwrap(): S3FPacketCustomPayload = (this as SPacketCustomPayloadImpl<*>).wrapped
inline fun S3FPacketCustomPayload.wrap(): ISPacketCustomPayload = SPacketCustomPayloadImpl(this)
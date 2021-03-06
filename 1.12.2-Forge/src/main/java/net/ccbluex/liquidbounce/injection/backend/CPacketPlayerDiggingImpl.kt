package net.ccbluex.liquidbounce.injection.backend

import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketPlayerDigging
import net.ccbluex.liquidbounce.api.minecraft.util.IEnumFacing
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.injection.backend.utils.wrap
import net.minecraft.network.play.client.CPacketPlayerDigging

class CPacketPlayerDiggingImpl<out T : CPacketPlayerDigging>(wrapped: T) : PacketImpl<T>(wrapped), ICPacketPlayerDigging
{
	override val status: ICPacketPlayerDigging.WAction
		get() = wrapped.action.wrap()
	override val position: WBlockPos
		get() = wrapped.position.wrap()
	override val facing: IEnumFacing
		get() = wrapped.facing.wrap()
}

fun ICPacketPlayerDigging.unwrap(): CPacketPlayerDigging = (this as CPacketPlayerDiggingImpl<*>).wrapped
fun CPacketPlayerDigging.wrap(): ICPacketPlayerDigging = CPacketPlayerDiggingImpl(this)

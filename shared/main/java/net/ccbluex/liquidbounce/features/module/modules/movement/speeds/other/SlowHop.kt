/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class SlowHop : SpeedMode("SlowHop")
{
	override fun onMotion(eventState: EventState)
	{
		if (eventState != EventState.PRE) return

		val thePlayer = mc.thePlayer ?: return

		if (thePlayer.isInWater) return
		if (MovementUtils.isMoving(thePlayer))
		{
			if (thePlayer.onGround) jump(thePlayer) else MovementUtils.strafe(thePlayer, MovementUtils.getSpeed(thePlayer) * 1.011f)
		}
		else MovementUtils.zeroXZ(thePlayer)
	}

	override fun onUpdate()
	{
	}

	override fun onMove(event: MoveEvent)
	{
	}
}

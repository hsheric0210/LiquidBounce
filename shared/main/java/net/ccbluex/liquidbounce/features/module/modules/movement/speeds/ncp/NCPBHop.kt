/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class NCPBHop : SpeedMode("NCPBHop")
{
	override fun onEnable()
	{
		mc.timer.timerSpeed = 1.0865f
		super.onEnable()
	}

	override fun onDisable()
	{
		(mc.thePlayer ?: return).speedInAir = 0.02f
		mc.timer.timerSpeed = 1f
		super.onDisable()
	}

	override fun onMotion(eventState: EventState)
	{
	}

	override fun onUpdate()
	{
		val thePlayer = mc.thePlayer ?: return

		if (MovementUtils.isMoving(thePlayer))
		{
			if (thePlayer.onGround)
			{
				jump(thePlayer)

				thePlayer.speedInAir = 0.0223f
			}

			MovementUtils.strafe(thePlayer)
		}
		else MovementUtils.zeroXZ(thePlayer)
	}

	override fun onMove(event: MoveEvent)
	{
	}
}

/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class AAC3_0_3LowHop : SpeedMode("AAC3.0.3-LowHop") // Was AACBHop
{
	override fun onMotion(eventState: EventState)
	{
		if (eventState != EventState.PRE) return

		val thePlayer = mc.thePlayer ?: return

		if (thePlayer.isInWater) return

		val timer = mc.timer

		if (MovementUtils.isMoving(thePlayer))
		{
			timer.timerSpeed = 1.08f

			if (thePlayer.onGround)
			{
				val func = functions

				val dir = MovementUtils.getDirection(thePlayer)
				thePlayer.motionX -= func.sin(dir) * 0.2f
				thePlayer.motionZ += func.cos(dir) * 0.2f

				thePlayer.motionY = 0.399
				LiquidBounce.eventManager.callEvent(JumpEvent(0.399f))

				timer.timerSpeed = 2f
			}
			else
			{
				thePlayer.motionX *= 1.008
				thePlayer.motionZ *= 1.008

				thePlayer.motionY *= 0.97
			}
		}
		else
		{
			MovementUtils.zeroXZ(thePlayer)
			timer.timerSpeed = 1f
		}
	}

	override fun onUpdate()
	{
	}

	override fun onMove(event: MoveEvent)
	{
	}

	override fun onDisable()
	{
		mc.timer.timerSpeed = 1f
	}
}

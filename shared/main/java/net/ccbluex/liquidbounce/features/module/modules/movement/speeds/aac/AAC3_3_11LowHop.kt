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

class AAC3_3_11LowHop : SpeedMode("AAC3.3.11-LowHop") // Was AAC6BHop
{
	private var firstLegitJump = false
	override fun onUpdate()
	{
		val thePlayer = mc.thePlayer ?: return
		val timer = mc.timer

		timer.timerSpeed = 1f

		if (thePlayer.isInWater) return
		if (MovementUtils.isMoving(thePlayer))
		{
			if (thePlayer.onGround)
			{
				if (firstLegitJump)
				{

					MovementUtils.strafe(thePlayer, 0.15f)

					thePlayer.onGround = false

					thePlayer.motionY = 0.4
					LiquidBounce.eventManager.callEvent(JumpEvent(0.4f))

					firstLegitJump = false

					return
				}

				MovementUtils.strafe(thePlayer, 0.47458485f)

				thePlayer.motionY = 0.41
				LiquidBounce.eventManager.callEvent(JumpEvent(0.41f))
			}

			if (thePlayer.motionY < 0 && thePlayer.motionY > -0.2) timer.timerSpeed = (1.2f + thePlayer.motionY).toFloat()

			thePlayer.speedInAir = 0.022151f
		}
		else
		{
			firstLegitJump = true

			MovementUtils.zeroXZ(thePlayer)
		}
	}

	override fun onMotion(eventState: EventState)
	{
	}

	override fun onMove(event: MoveEvent)
	{
	}

	override fun onEnable()
	{
		firstLegitJump = true
	}

	override fun onDisable()
	{
		mc.timer.timerSpeed = 1f
		mc.thePlayer?.speedInAir = 0.02f
	}
}

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

		mc.timer.timerSpeed = 1f

		if (thePlayer.isInWater) return
		if (MovementUtils.isMoving)
		{
			if (thePlayer.onGround)
			{
				if (firstLegitJump)
				{
					thePlayer.motionY = 0.4
					MovementUtils.strafe(0.15f)
					thePlayer.onGround = false
					LiquidBounce.eventManager.callEvent(JumpEvent(0.4f))
					firstLegitJump = false
					return
				}

				thePlayer.motionY = 0.41
				MovementUtils.strafe(0.47458485f)
				LiquidBounce.eventManager.callEvent(JumpEvent(0.41f))
			}

			if (thePlayer.motionY < 0 && thePlayer.motionY > -0.2) mc.timer.timerSpeed = (1.2f + thePlayer.motionY).toFloat()

			thePlayer.speedInAir = 0.022151f
		}
		else
		{
			firstLegitJump = true

			thePlayer.motionX = 0.0
			thePlayer.motionZ = 0.0
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
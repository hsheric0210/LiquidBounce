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

class AAC3_3_9BHop : SpeedMode("AAC3.3.9-BHop") // Was AAC5BHop
{
	private var firstLegitJump = false

	override fun onTick()
	{
		val thePlayer = mc.thePlayer ?: return
		val timer = mc.timer

		timer.timerSpeed = 1f

		if (thePlayer.isInWater) return

		if (MovementUtils.isMoving(thePlayer))
		{
			when
			{
				// Jump
				thePlayer.onGround ->
				{
					// Legit jump on the first
					if (firstLegitJump)
					{
						jump(thePlayer)

						firstLegitJump = false

						return
					}

					MovementUtils.strafe(thePlayer, 0.374f)

					thePlayer.motionY = 0.41
					LiquidBounce.eventManager.callEvent(JumpEvent(0.41f))

					thePlayer.onGround = false
				}

				// Going down
				thePlayer.motionY < 0.0 ->
				{
					thePlayer.speedInAir = 0.0201f
					timer.timerSpeed = 1.02f
				}

				else -> timer.timerSpeed = 1.01f
			}
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

	override fun onUpdate()
	{
	}

	override fun onMove(event: MoveEvent)
	{
	}

	override fun onDisable()
	{
		(mc.thePlayer ?: return).speedInAir = 0.02f
		mc.timer.timerSpeed = 1f
	}
}

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

class AAC3_1_5LowHop : SpeedMode("AAC3.1.5-LowHop") // Was AACLowHop
{
	private var firstLegitJump = false
	override fun onEnable()
	{
		firstLegitJump = true
		super.onEnable()
	}

	override fun onMotion(eventState: EventState)
	{
		if (eventState != EventState.PRE) return

		val thePlayer = mc.thePlayer ?: return

		if (MovementUtils.isMoving(thePlayer))
		{
			if (thePlayer.onGround)
			{
				if (firstLegitJump)
				{
					jump(thePlayer)

					firstLegitJump = false

					return
				}

				MovementUtils.strafe(thePlayer, 0.534f)

				thePlayer.motionY = 0.343
				LiquidBounce.eventManager.callEvent(JumpEvent(0.343f))
			}
		}
		else
		{
			firstLegitJump = true

			MovementUtils.zeroXZ(thePlayer)
		}
	}

	override fun onUpdate()
	{
	}

	override fun onMove(event: MoveEvent)
	{
	}
}

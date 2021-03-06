package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe

/**
 * LiquidBounce Hacked Client A minecraft forge injection client using Mixin
 *
 * @author CCBlueX
 * @game   Minecraft
 */
class ACP : SpeedMode("ACP")
{
	override fun onMotion(eventState: EventState)
	{
		val thePlayer = mc.thePlayer ?: return

		if (isMoving(thePlayer))
		{
			val amplifier = MovementUtils.getSpeedEffectAmplifier(thePlayer)

			thePlayer.motionX *= 0.8
			thePlayer.motionZ *= 0.8

			val moveSpeed = if (thePlayer.onGround) when (amplifier)
			{
				1 -> 0.70F //0.31 +6 +6 +
				2 -> 0.75F // 0.37 - previous value
				3 -> 0.85F // 0.41
				4 -> 0.95F // 0.45
				5 -> 1.05F // 0.49
				6 -> 1.15F // 0.53
				else -> 0.5F
			}
			else when (amplifier)
			{
				1 -> 0.85F // 0.31 +6 +6 +
				2 -> 0.91F // 0.37 - previous value
				3 -> 1.01F // 0.41
				4 -> 1.12F // 0.45
				5 -> 1.23F // 0.49
				6 -> 1.35F // 0.53
				else -> 0.55F
			}

			strafe(thePlayer, moveSpeed)
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

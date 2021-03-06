/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.api.minecraft.client.block.IBlock
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityPlayerSP
import net.ccbluex.liquidbounce.api.minecraft.util.IAxisAlignedBB
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.roundToInt

class YPort : SpeedMode("YPort")
{
	private var moveSpeed = 0.2873
	private var step = 1
	private var lastSpeed = 0.0
	private var timerDelay = 0
	private var safeJump = false

	override fun onMotion(eventState: EventState)
	{
		if (eventState != EventState.PRE) return

		val thePlayer = mc.thePlayer ?: return

		val provider = classProvider

		if (!safeJump && !thePlayer.movementInput.jump && !thePlayer.isOnLadder && !thePlayer.isInWater && !thePlayer.isInLava && (!provider.isBlockAir(this.getBlock(thePlayer, -1.1)) && !provider.isBlockAir(this.getBlock(thePlayer, -1.1)) || !provider.isBlockAir(this.getBlock(thePlayer, -0.1)) && thePlayer.motionX != 0.0 && thePlayer.motionZ != 0.0 && !thePlayer.onGround && thePlayer.fallDistance < 3.0f && thePlayer.fallDistance > 0.05) && step == 3) thePlayer.motionY = -0.3994

		lastSpeed = hypot(thePlayer.posX - thePlayer.prevPosX, thePlayer.posZ - thePlayer.prevPosZ)

		if (!MovementUtils.isMoving(thePlayer)) safeJump = true else if (thePlayer.onGround) safeJump = false
	}

	override fun onUpdate()
	{
	}

	override fun onMove(event: MoveEvent)
	{
		val theWorld = mc.theWorld ?: return
		val thePlayer = mc.thePlayer ?: return
		val timer = mc.timer

		timerDelay += 1
		timerDelay %= 5

		if (timerDelay != 0) timer.timerSpeed = 1f
		else if (MovementUtils.hasMotion(thePlayer))
		{
			val d = 1.0199999809265137

			timer.timerSpeed = 1.3f
			thePlayer.motionX *= d
			thePlayer.motionZ *= d
		}

		if (thePlayer.onGround && MovementUtils.hasMotion(thePlayer)) step = 2

		if (round(thePlayer.posY - thePlayer.posY.roundToInt()) == round(0.138))
		{
			val d = 0.09316090325960147

			thePlayer.motionY -= 0.08
			event.y = event.y - d
			thePlayer.posY -= d
		}

		val baseMoveSpeed = getBaseMoveSpeed(thePlayer)
		val move = MovementUtils.isMoving(thePlayer)

		when (step)
		{
			1 -> if (move)
			{
				step = 2

				moveSpeed = 1.38 * baseMoveSpeed - 0.01
			}

			2 ->
			{
				step = 3

				val d = 0.399399995803833
				thePlayer.motionY = d
				event.y = d
				moveSpeed *= 2.149
			}

			3 ->
			{
				step = 4

				val difference = 0.66 * (lastSpeed - baseMoveSpeed)
				moveSpeed = lastSpeed - difference
			}

			else ->
			{
				if (theWorld.getCollidingBoundingBoxes(thePlayer, thePlayer.entityBoundingBox.offset(0.0, thePlayer.motionY, 0.0)).isNotEmpty() || thePlayer.isCollidedVertically) step = 1

				moveSpeed = lastSpeed - lastSpeed / 159.0
			}
		}

		moveSpeed = moveSpeed.coerceAtLeast(baseMoveSpeed)

		if (move)
		{
			val func = functions

			val dir = MovementUtils.getDirection(thePlayer)
			event.x = -func.sin(dir) * moveSpeed
			event.z = func.cos(dir) * moveSpeed

			thePlayer.stepHeight = 0.5f
		}
		else event.zeroXZ()
	}

	private fun getBaseMoveSpeed(thePlayer: IEntityPlayerSP): Double = 0.2873 * (1.0 + 0.2 * MovementUtils.getSpeedEffectAmplifier(thePlayer))

	private fun getBlock(axisAlignedBB: IAxisAlignedBB): IBlock?
	{
		val theWorld = mc.theWorld ?: return null

		val minX = floor(axisAlignedBB.minX).toInt()
		val maxX = floor(axisAlignedBB.maxX).toInt() + 1
		val minY = axisAlignedBB.minY.toInt()
		val minZ = floor(axisAlignedBB.minZ).toInt()
		val maxZ = floor(axisAlignedBB.maxZ).toInt() + 1

		(minX until maxX).forEach { x ->
			(minZ until maxZ).forEach { z ->
				return@getBlock theWorld.getBlockState(WBlockPos(x, minY, z)).block
			}
		}

		return null
	}

	private fun getBlock(thePlayer: IEntityPlayerSP, offset: Double): IBlock? = this.getBlock((thePlayer).entityBoundingBox.offset(0.0, offset, 0.0))

	private fun round(value: Double): Double = BigDecimal(value).setScale(3, RoundingMode.HALF_UP).toDouble()
}

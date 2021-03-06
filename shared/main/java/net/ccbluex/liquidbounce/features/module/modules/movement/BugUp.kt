/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max

@ModuleInfo(name = "BugUp", description = "Automatically setbacks you after falling a certain distance.", category = ModuleCategory.MOVEMENT)
class BugUp : Module()
{
	private val modeValue = ListValue("Mode", arrayOf("TeleportBack", "FlyFlag", "OnGroundSpoof", "MotionTeleport-Flag"), "FlyFlag")
	private val maxFallDistance = IntegerValue("MaxFallDistance", 10, 2, 255)
	private val maxVoidFallDistance = IntegerValue("MaxVoidFallDistance", 3, 1, 255)
	private val maxDistanceWithoutGround = FloatValue("MaxDistanceToSetback", 2.5f, 1f, 30f)

	private val flagYMotion = FloatValue("YMotion", 1f, -10f, 10f)
	private val motionTPYMotion = FloatValue("YTeleportMotion", 1f, -10f, 10f)

	private val flagTryTicks = IntegerValue("FlagTryTicks", 10, 5, 20)

	private val onlyCatchVoid = BoolValue("OnlyVoid", true)
	private val indicator = BoolValue("Indicator", true)

	private var detectedLocation: WBlockPos? = null
	private var lastFound = 0F
	private var prevX = 0.0
	private var prevY = 0.0
	private var prevZ = 0.0

	private val flagTimer = TickTimer()
	private var tryingFlag = false
	private var alreadyTriedFlag = false

	override fun onDisable()
	{
		prevX = 0.0
		prevY = 0.0
		prevZ = 0.0
	}

	@EventTarget
	fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent)
	{
		detectedLocation = null

		val theWorld = mc.theWorld ?: return
		val thePlayer = mc.thePlayer ?: return

		val posX = thePlayer.posX
		val posY = thePlayer.posY
		val posZ = thePlayer.posZ

		val provider = classProvider

		if (thePlayer.onGround && !provider.isBlockAir(BlockUtils.getBlock(theWorld, WBlockPos(posX, posY - 1.0, posZ))))
		{
			prevX = thePlayer.prevPosX
			prevY = thePlayer.prevPosY
			prevZ = thePlayer.prevPosZ
		}

		val networkManager = mc.netHandler.networkManager

		if (!thePlayer.onGround && !thePlayer.isOnLadder && !thePlayer.isInWater)
		{
			val fallingPlayer = FallingPlayer(theWorld, thePlayer, posX, posY, posZ, thePlayer.motionX, thePlayer.motionY, thePlayer.motionZ, thePlayer.rotationYaw, thePlayer.moveStrafing, thePlayer.moveForward)

			val fallDistance = thePlayer.fallDistance

			val detectedLocation = fallingPlayer.findCollision(60)?.pos

			this.detectedLocation = detectedLocation

			if (detectedLocation != null && (onlyCatchVoid.get() || abs(posY - detectedLocation.y) + fallDistance <= maxFallDistance.get())) lastFound = fallDistance

			if (detectedLocation == null && fallDistance <= maxVoidFallDistance.get()) lastFound = fallDistance

			if (fallDistance - lastFound > maxDistanceWithoutGround.get())
			{
				when (modeValue.get().toLowerCase())
				{
					"teleportback" ->
					{
						thePlayer.setPositionAndUpdate(prevX, prevY, prevZ)
						thePlayer.fallDistance = 0F
						thePlayer.motionY = 0.0
					}

					else -> if (!alreadyTriedFlag)
					{
						tryingFlag = true
						alreadyTriedFlag = true
					}
				}
			}
		}
		else alreadyTriedFlag = false

		if (tryingFlag)
		{
			if (!flagTimer.hasTimePassed(flagTryTicks.get()))
			{
				when (modeValue.get().toLowerCase())
				{
					"flyflag" ->
					{
						thePlayer.motionY = flagYMotion.get().toDouble()
						thePlayer.fallDistance = 0F
					}

					"ongroundspoof" -> networkManager.sendPacketWithoutEvent(provider.createCPacketPlayer(true))

					"motionteleport-flag" ->
					{
						thePlayer.setPositionAndUpdate(posX, posY + motionTPYMotion.get(), posZ)
						networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY, posZ, true))
						thePlayer.motionY = flagYMotion.get().toDouble()

						MovementUtils.strafe(thePlayer)
						thePlayer.fallDistance = 0f
					}
				}
			}
			else
			{
				tryingFlag = false
				flagTimer.reset()
			}
			flagTimer.update()
		}
		else flagTimer.reset()
	}

	@EventTarget
	fun onRender3D(@Suppress("UNUSED_PARAMETER") event: Render3DEvent)
	{
		val thePlayer = mc.thePlayer ?: return

		val detectedLocation = detectedLocation ?: return
		if (!indicator.get() || thePlayer.fallDistance + (thePlayer.posY - (detectedLocation.y + 1)) < 3) return

		val x = detectedLocation.x
		val y = detectedLocation.y
		val z = detectedLocation.z

		val renderManager = mc.renderManager
		val renderPosX = renderManager.renderPosX
		val renderPosY = renderManager.renderPosY
		val renderPosZ = renderManager.renderPosZ

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
		GL11.glEnable(GL11.GL_BLEND)
		GL11.glLineWidth(2f)
		GL11.glDisable(GL11.GL_TEXTURE_2D)
		GL11.glDisable(GL11.GL_DEPTH_TEST)
		GL11.glDepthMask(false)

		RenderUtils.glColor(Color(255, 0, 0, 90))

		val provider = classProvider

		RenderUtils.drawFilledBox(provider.createAxisAlignedBB(x - renderPosX, y + 1 - renderPosY, z - renderPosZ, x - renderPosX + 1.0, y + 1.2 - renderPosY, z - renderPosZ + 1.0))

		GL11.glEnable(GL11.GL_TEXTURE_2D)
		GL11.glEnable(GL11.GL_DEPTH_TEST)
		GL11.glDepthMask(true)
		GL11.glDisable(GL11.GL_BLEND)

		val fallDistance = floor(thePlayer.fallDistance + (thePlayer.posY - (y + 0.5))).toInt()

		RenderUtils.renderNameTag("${fallDistance}m (~${max(0, fallDistance - 3)} damage)", x + 0.5, y + 1.7, z + 0.5)

		provider.glStateManager.resetColor()
	}

	@EventTarget
	fun onPacket(event: PacketEvent)
	{
		val packet = event.packet
		if (classProvider.isSPacketPlayerPosLook(packet))
		{
			val mode = modeValue.get()

			if (!mode.equals("TeleportBack", true) && tryingFlag)
			{
				// Automatically stop to try flag after teleported back.
				ClientUtils.displayChatMessage(mc.thePlayer, "\u00A78[\u00A7c\u00A7lBugUp\u00A78] \u00A7cTeleported.")
				tryingFlag = false
			}
		}
	}

	override val tag: String
		get() = modeValue.get()
}

/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.enums.EnumFacingType
import net.ccbluex.liquidbounce.api.enums.StatType
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityPlayerSP
import net.ccbluex.liquidbounce.api.minecraft.client.multiplayer.IWorldClient
import net.ccbluex.liquidbounce.api.minecraft.potion.PotionType
import net.ccbluex.liquidbounce.api.minecraft.util.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.exploit.Damage
import net.ccbluex.liquidbounce.features.module.modules.render.BlockOverlay
import net.ccbluex.liquidbounce.features.module.modules.render.Bobbing
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.features.module.modules.world.Tower
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.MovementUtils.getDirection
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.value.*
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

@ModuleInfo(name = "Fly", description = "Allows you to fly in survival mode.", category = ModuleCategory.MOVEMENT, keyBind = Keyboard.KEY_F)
class Fly : Module()
{
	/**
	 * Mode
	 */
	val modeValue = ListValue("Mode", arrayOf("Vanilla", "SmoothVanilla", "Teleport",

		// NCP
		"NCP", "OldNCP",

		// AAC
		"AAC1.9.10", "AAC3.0.5", "AAC3.1.6-Gomme", "AAC3.3.12", "AAC3.3.12-Glide", "AAC3.3.13",

		// CubeCraft
		"CubeCraft",

		// Hypixel
		"Hypixel", "FreeHypixel", // BoostHypixel mode is merged with Hypixel mode

		// Rewinside
		"Rewinside", "TeleportRewinside",

		// Other server specific flys
		"Mineplex", "NeruxVace", "Minesucht", "RedeSky", "MCCentral",

		// Spartan
		"Spartan185", "Spartan194", "BugSpartan",

		// Other anticheats
		"MineSecure", "HawkEye", "HAC", "WatchCat", "ACP",

		// Other
		"Jetpack", "KeepAlive", "Flag"), "Vanilla")

	/**
	 * Damage on start
	 */
	private val damageOnStartValue = BoolValue("DamageOnStart", false)
	private val damageModeValue = ListValue("DamageMode", arrayOf("NCP", "Hypixel"), "NCP")

	/**
	 * Vanilla
	 */
	val vanillaSpeedValue = FloatValue("VanillaSpeed", 2f, 0f, 5f)
	private val vanillaKickBypassValue = BoolValue("VanillaKickBypass", false)

	/**
	 * Teleport
	 */
	private val teleportDistanceValue = FloatValue("TeleportDistance", 1.0f, 1.0f, 5.0f)
	private val teleportDelayValue = IntegerValue("TeleportDelay", 100, 0, 1000)

	/**
	 * NCP
	 */
	private val ncpMotionValue = FloatValue("NCPMotion", 0f, 0f, 1f)

	/**
	 * AAC
	 */
	private val aacSpeedValue = FloatValue("AAC1.9.10-Speed", 0.3f, 0f, 5f)
	private val aacFast = BoolValue("AAC3.0.5-Fast", true)
	private val aac3_3_12_motion = FloatValue("AAC3.3.12-Motion", 10f, 0.1f, 10f)
	private val aac3_3_13_motion = FloatValue("AAC3.3.13-Motion", 10f, 0.1f, 10f)

	/**
	 * Hypixel
	 */
	private val hypixelDMGBoost: BoolValue = object : BoolValue("Hypixel-DamageBoost", false)
	{
		override fun onChanged(oldValue: Boolean, newValue: Boolean)
		{
			if (modeValue.get().equals("Hypixel", ignoreCase = true) && newValue) damageOnStartValue.set(true)
		}
	}
	private val hypixelDMGBoostStartTiming = ListValue("Hypixel-DamageBoost-BoostTiming", arrayOf("Immediately", "AfterDamage"), "Immediately")
	private val hypixelDMGBoostAirStartMode = ListValue("Hypixel-DamageBoost-AirStartMode", arrayOf("WaitForDamage", "JustFlyWithoutDamageBoost"), "WaitForDamage")
	private val hypixelOnGroundValue = BoolValue("Hypixel-OnGround", false)
	private val hypixelYchIncValue = BoolValue("Hypixel-ychinc", true)
	private val hypixelJumpValue = BoolValue("Hypixel-Jump", false)

	private val hypixelTimerBoost = BoolValue("Hypixel-TimerBoost", true)
	private val hypixelTimerBoostDelay = IntegerValue("Hypixel-TimerBoost-BoostDelay", 1200, 0, 2000)
	private val hypixelTimerBoostTimer = FloatValue("Hypixel-TimerBoost-BoostTimer", 1f, 0f, 5f)

	/**
	 * Mineplex
	 */
	private val mineplexSpeedValue = FloatValue("MineplexSpeed", 1f, 0.5f, 10f)
	private val neruxVaceTicks = IntegerValue("NeruxVace-Ticks", 6, 0, 20)
	private val redeskyVClipHeight = FloatValue("RedeSky-Height", 4f, 1f, 7f)
	private val mccTimerSpeedValue = FloatValue("MCCentral-Timer", 2.0f, 1.0f, 5.0f)

	/**
	 * Reset Motions On Disable
	 */
	private val resetMotionOnDisable = BoolValue("ResetMotionOnDisable", false)

	/**
	 * Visuals
	 */
	private val bobValue = BoolValue("Bob", true)
	private val markValue = BoolValue("Mark", true)
	private val vanillaFlightRemainingTimeValue = BoolValue("VanillaFlightRemainingTimeCounter", false)

	/**
	 * Timers
	 */
	private val hypixelFlyTimer = MSTimer()
	private val groundTimer = MSTimer()
	private val mineSecureVClipTimer = MSTimer()
	private val spartanTimer = TickTimer()
	private val mineplexTimer = MSTimer()
	private val hypixelTimer = TickTimer()
	private val acpTickTimer = TickTimer()
	private val cubecraftTeleportTickTimer = TickTimer()
	val freeHypixelTimer = TickTimer()
	private val teleportTimer = MSTimer()
	private val minesuchtTP = MSTimer()

	private val vanillaRemainingTime = TickTimer()

	/**
	 * AAC mode related variables
	 */
	private var aac3_1_6_touchedVoid = false
	private var wasDead = false
	private var aacJump = 0.0
	private var aac3delay = 0
	private var aac3glideDelay = 0

	/**
	 * Visual variables
	 */
	private var startY = 0.0
	private var markStartY = 0.0

	/**
	 * Hypixel
	 */
	private var hypixelFlyStarted = false
	private var hypixelDamageBoostFailed = false
	private var canPerformHypixelDamageFly = false
	private var hypixelBoostStep = 1
	private var hypixelBoostSpeed = 0.0
	private var lastDistance = 0.0
	private var waitForDamage: Boolean = false

	/**
	 * FreeHypixel
	 */
	private var freeHypixelYaw = 0f
	private var freeHypixelPitch = 0f

	override fun onEnable()
	{
		val theWorld = mc.theWorld ?: return
		val thePlayer = mc.thePlayer ?: return

		hypixelFlyTimer.reset()
		vanillaRemainingTime.reset()

		val posX = thePlayer.posX
		val posY = thePlayer.posY
		val posZ = thePlayer.posZ
		val onGround = thePlayer.onGround

		val mode = modeValue.get()
		val damageOnStart = damageOnStartValue.get()

		val networkManager = mc.netHandler.networkManager

		if (damageOnStart && onGround)
		{
			when (damageModeValue.get().toLowerCase())
			{
				"ncp" -> Damage.ncpDamage()
				"hypixel" -> Damage.hypixelDamage()
			}
		}

		run {
			when (mode.toLowerCase())
			{
				"ncp" ->
				{
					if (!onGround) return@run

					if (!damageOnStart) Damage.ncpDamage()

					thePlayer.motionX *= 0.1
					thePlayer.motionZ *= 0.1

					thePlayer.swingItem()
				}

				"oldncp" ->
				{
					if (!onGround) return@run

					if (!damageOnStart) Damage.ncpDamage(motionSize = 1.01)

					// for (i in 0..3)
					// {
					// 	networkManager.sendPacketWithoutEvent(classProvider.createCPacketPlayerPosition(posX, posY + 1.01, posZ, false))
					// 	networkManager.sendPacketWithoutEvent(classProvider.createCPacketPlayerPosition(posX, posY, posZ, false))
					// }

					thePlayer.jump()
					thePlayer.swingItem()
				}

				"bugspartan" ->
				{
					if (!damageOnStart) Damage.ncpDamage()

					thePlayer.motionX *= 0.1
					thePlayer.motionZ *= 0.1
					thePlayer.swingItem()
				}

				"infinitycubecraft" -> ClientUtils.displayChatMessage(thePlayer, "\u00A78[\u00A7c\u00A7lCubeCraft-\u00A7a\u00A7lFly\u00A78] \u00A7aPlace a block before landing.")

				"infinityvcubecraft" ->
				{
					ClientUtils.displayChatMessage(thePlayer, "\u00A78[\u00A7c\u00A7lCubeCraft-\u00A7a\u00A7lFly\u00A78] \u00A7aPlace a block before landing.")

					thePlayer.setPosition(posX, posY + 2, posZ)
				}

				"hypixel" ->
				{
					val hypixelJump = hypixelJumpValue.get()
					if ((hypixelDMGBoost.get() && (hypixelDMGBoostAirStartMode.get().equals("WaitForDamage", ignoreCase = true) || onGround)).also { canPerformHypixelDamageFly = it })
					{
						if (onGround) // If player is on ground, try to damage.
						{
							if (!hypixelFlyStarted) if (hypixelDMGBoostStartTiming.get().equals("Immediately", ignoreCase = true))
							{
								if (hypixelJump) jump(theWorld, thePlayer)

								hypixelBoostStep = 1
								hypixelBoostSpeed = 0.1
								lastDistance = 0.0
								hypixelDamageBoostFailed = false
								hypixelFlyStarted = true
								hypixelFlyTimer.reset()
							}
							else waitForDamage = true
						}
						else waitForDamage = true
					}
					else if (hypixelJump && onGround) jump(theWorld, thePlayer)
				}

				"redesky" -> if (onGround) redeskyVClip(thePlayer, redeskyVClipHeight.get())

				"mccentral" -> mc.timer.timerSpeed = mccTimerSpeedValue.get()

				"acp" -> networkManager.sendPacketWithoutEvent(classProvider.createCPacketPlayerPosition(posX, posY + 0.4, posZ, onGround))
			}
		}

		startY = thePlayer.posY.also { markStartY = it } // apply y change caused by jump() and redeskyVClip()

		aacJump = -3.8

		if (mode.equals("freehypixel", ignoreCase = true))
		{
			freeHypixelTimer.reset()
			thePlayer.setPositionAndUpdate(posX, posY + 0.42, posZ)
			freeHypixelYaw = thePlayer.rotationYaw
			freeHypixelPitch = thePlayer.rotationPitch
		}
	}

	override fun onDisable()
	{
		wasDead = false

		val thePlayer = mc.thePlayer ?: return

		val isRedeSkyMode = modeValue.get().equals("Redesky", ignoreCase = true)
		if (isRedeSkyMode) redeskySpeed(thePlayer, 0)

		aac3_1_6_touchedVoid = false

		waitForDamage = false
		hypixelFlyStarted = false
		canPerformHypixelDamageFly = false

		if (resetMotionOnDisable.get()) MovementUtils.zeroXYZ(thePlayer)

		if (isRedeSkyMode) redeskyPacketHClip(thePlayer, 0.0)

		thePlayer.capabilities.isFlying = false
		mc.timer.timerSpeed = 1f
		thePlayer.speedInAir = 0.02f
	}

	@EventTarget
	fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent?)
	{
		val theWorld = mc.theWorld ?: return
		val thePlayer = mc.thePlayer ?: return
		val timer = mc.timer

		val posX = thePlayer.posX
		val posY = thePlayer.posY
		val posZ = thePlayer.posZ
		val onGround = thePlayer.onGround
		val rotationYaw = thePlayer.rotationYaw
		val rotationPitch = thePlayer.rotationPitch

		val networkManager = mc.netHandler.networkManager
		val gameSettings = mc.gameSettings
		val jumpKeyDown = gameSettings.keyBindJump.isKeyDown
		val sneakKeyDown = gameSettings.keyBindSneak.isKeyDown

		val vanillaSpeed = vanillaSpeedValue.get()

		vanillaRemainingTime.update()

		run {
			val provider = classProvider

			val func = functions

			when (modeValue.get().toLowerCase())
			{
				"vanilla" ->
				{
					thePlayer.capabilities.isFlying = false

					MovementUtils.zeroXYZ(thePlayer)

					if (jumpKeyDown) thePlayer.motionY += vanillaSpeed
					if (sneakKeyDown) thePlayer.motionY -= vanillaSpeed
					MovementUtils.strafe(thePlayer, vanillaSpeed)
					handleVanillaKickBypass(theWorld, thePlayer)
				}

				"smoothvanilla" ->
				{
					thePlayer.capabilities.isFlying = true
					handleVanillaKickBypass(theWorld, thePlayer)
				}

				"teleport" ->
				{
					thePlayer.sprinting = true
					MovementUtils.zeroXYZ(thePlayer)
					val isMoving = isMoving(thePlayer)
					if ((isMoving || jumpKeyDown || sneakKeyDown) && teleportTimer.hasTimePassed(teleportDelayValue.get().toLong()))
					{
						val yaw = getDirection(thePlayer)
						val speed = teleportDistanceValue.get().toDouble()
						var x = 0.0
						var y = 0.0
						var z = 0.0

						if (isMoving && !thePlayer.isCollidedHorizontally)
						{
							x = -func.sin(yaw) * speed
							z = func.cos(yaw) * speed
						}

						if (!thePlayer.isCollidedVertically) if (jumpKeyDown && !sneakKeyDown) y = speed else if (!jumpKeyDown && sneakKeyDown) y = -speed

						thePlayer.setPosition(x.let { thePlayer.posX += it; posX }, y.let { thePlayer.posY += it; posY }, z.let { thePlayer.posZ += it; posZ })
						teleportTimer.reset()
					}
				}

				"cubecraft" ->
				{
					timer.timerSpeed = 0.6f
					cubecraftTeleportTickTimer.update()
				}

				"ncp" ->
				{
					thePlayer.motionY = (-ncpMotionValue.get()).toDouble()
					if (sneakKeyDown) thePlayer.motionY = -0.5
					MovementUtils.strafe(thePlayer)
				}

				"oldncp" ->
				{
					if (startY > posY) thePlayer.motionY = -0.000000000000000000000000000000001
					if (sneakKeyDown) thePlayer.motionY = -0.2
					if (jumpKeyDown && posY < startY - 0.1) thePlayer.motionY = 0.2
					MovementUtils.strafe(thePlayer)
				}

				"aac1.9.10" ->
				{
					if (jumpKeyDown) aacJump += 0.2
					if (sneakKeyDown) aacJump -= 0.2

					if (startY + aacJump > posY)
					{
						networkManager.sendPacketWithoutEvent(provider.createCPacketPlayer(true))
						thePlayer.motionY = 0.8
						MovementUtils.strafe(thePlayer, aacSpeedValue.get())
					}

					MovementUtils.strafe(thePlayer)
				}

				"aac3.0.5" ->
				{
					if (aac3delay == 2) thePlayer.motionY = 0.1 else if (aac3delay > 2) aac3delay = 0
					if (aacFast.get())
					{
						if (thePlayer.movementInput.moveStrafe == 0.0f) thePlayer.jumpMovementFactor = 0.08f
						else thePlayer.jumpMovementFactor = 0f
					}
					aac3delay++
				}

				"aac3.1.6-gomme" ->
				{
					thePlayer.capabilities.isFlying = true

					if (aac3delay == 2) thePlayer.motionY += 0.05
					else if (aac3delay > 2)
					{
						thePlayer.motionY -= 0.05
						aac3delay = 0
					}

					aac3delay++

					if (!aac3_1_6_touchedVoid) networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY, posZ, onGround))
					if (posY <= 0.0) aac3_1_6_touchedVoid = true
				}

				"flag" ->
				{
					networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosLook(posX + thePlayer.motionX * 999, posY + (if (jumpKeyDown) 1.5624 else 0.00000001) - if (sneakKeyDown) 0.0624 else 0.00000002, posZ + thePlayer.motionZ * 999, rotationYaw, rotationPitch, true))
					networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosLook(posX + thePlayer.motionX * 999, posY - 6969, posZ + thePlayer.motionZ * 999, rotationYaw, rotationPitch, true))

					thePlayer.setPosition(posX + thePlayer.motionX * 11, posY, posZ + thePlayer.motionZ * 11)
					thePlayer.motionY = 0.0
				}

				"keepalive" ->
				{
					networkManager.sendPacketWithoutEvent(provider.createCPacketKeepAlive())

					thePlayer.capabilities.isFlying = false

					MovementUtils.zeroXYZ(thePlayer)

					if (jumpKeyDown) thePlayer.motionY += vanillaSpeed
					if (sneakKeyDown) thePlayer.motionY -= vanillaSpeed

					MovementUtils.strafe(thePlayer, vanillaSpeed)
				}

				"minesecure" ->
				{
					thePlayer.capabilities.isFlying = false

					if (!sneakKeyDown) thePlayer.motionY = -0.01

					MovementUtils.zeroXZ(thePlayer)

					MovementUtils.strafe(thePlayer, vanillaSpeed)

					if (mineSecureVClipTimer.hasTimePassed(150) && jumpKeyDown)
					{
						networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY + 5, posZ, false))
						networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(0.5, -1000.0, 0.5, false))

						val dir = WMathHelper.toRadians(rotationYaw)
						val x = -func.sin(dir) * 0.4
						val z = func.cos(dir) * 0.4

						thePlayer.setPosition(posX + x, posY, posZ + z)
						mineSecureVClipTimer.reset()
					}
				}

				"hac" ->
				{
					thePlayer.motionX *= 0.8
					thePlayer.motionZ *= 0.8
					thePlayer.motionY = if (thePlayer.motionY <= -0.42) 0.42 else -0.42
				}

				"hawkeye" -> thePlayer.motionY = if (thePlayer.motionY <= -0.42) 0.42 else -0.42

				"teleportrewinside" ->
				{
					val vectorStart = WVec3(posX, posY, posZ)

					val yaw = -rotationYaw
					val pitch = -rotationPitch
					val distance = 9.9

					val yawRadians = WMathHelper.toRadians(yaw)
					val yawSin = func.sin(yawRadians)
					val yawCos = func.cos(yawRadians)

					val pitchRadians = WMathHelper.toRadians(pitch)
					val pitchSin = func.sin(pitchRadians)
					val pitchCos = func.cos(pitchRadians)

					val vectorEnd = WVec3(yawSin * pitchCos * distance + vectorStart.xCoord, pitchSin * distance + vectorStart.yCoord, yawCos * pitchCos * distance + vectorStart.zCoord)

					networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(vectorEnd.xCoord, posY + 2, vectorEnd.zCoord, true))
					networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(vectorStart.xCoord, posY + 2, vectorStart.zCoord, true))

					thePlayer.motionY = 0.0
				}

				"minesucht" ->
				{
					if (!gameSettings.keyBindForward.isKeyDown) return@run

					if (minesuchtTP.hasTimePassed(99))
					{
						val vec3: WVec3 = thePlayer.getPositionEyes(0.0f)
						val vec31: WVec3 = thePlayer.getLook(0.0f)
						val vec32: WVec3 = vec3.addVector(vec31.xCoord * 7, vec31.yCoord * 7, vec31.zCoord * 7)

						if (thePlayer.fallDistance > 0.8)
						{
							networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY + 50, posZ, false))

							thePlayer.fall(100.0f, 100.0f)
							thePlayer.fallDistance = 0.0f

							networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY + 20, posZ, true))
						}

						networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(vec32.xCoord, posY + 50, vec32.zCoord, true))
						networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY, posZ, false))
						networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(vec32.xCoord, posY, vec32.zCoord, true))
						networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY, posZ, false))

						minesuchtTP.reset()
					}
					else
					{
						networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY, posZ, false))
						networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY, posZ, true))
					}
				}

				"jetpack" -> if (jumpKeyDown)
				{
					thePlayer.motionY += 0.15
					thePlayer.motionX *= 1.1
					thePlayer.motionZ *= 1.1
				}

				"mineplex" -> if (thePlayer.inventory.getCurrentItemInHand() == null)
				{
					if (jumpKeyDown && mineplexTimer.hasTimePassed(100))
					{
						thePlayer.setPosition(posX, posY + 0.6, posZ)
						mineplexTimer.reset()
					}

					if (thePlayer.sneaking && mineplexTimer.hasTimePassed(100))
					{
						thePlayer.setPosition(posX, posY - 0.6, posZ)
						mineplexTimer.reset()
					}

					val blockPos = WBlockPos(posX, thePlayer.entityBoundingBox.minY - 1, posZ)
					val vec: WVec3 = WVec3(blockPos).addVector(0.4, 0.4, 0.4).add(WVec3(provider.getEnumFacing(EnumFacingType.UP).directionVec))

					mc.playerController.onPlayerRightClick(thePlayer, theWorld, null, blockPos, provider.getEnumFacing(EnumFacingType.UP), WVec3(vec.xCoord * 0.4f, vec.yCoord * 0.4f, vec.zCoord * 0.4f))

					MovementUtils.strafe(thePlayer, 0.27f)

					timer.timerSpeed = 1 + mineplexSpeedValue.get()
				}
				else
				{
					timer.timerSpeed = 1.0f
					state = false
					ClientUtils.displayChatMessage(thePlayer, "\u00A78[\u00A7c\u00A7lMineplex-\u00A7a\u00A7lFly\u00A78] \u00A7aSelect an empty slot to fly.")
				}

				"aac3.3.12" ->
				{
					if (posY < -70) thePlayer.motionY = aac3_3_12_motion.get().toDouble()

					timer.timerSpeed = 1f

					if (sneakKeyDown) // Help you to MLG
					{
						timer.timerSpeed = 0.2f
						mc.rightClickDelayTimer = 0
					}
				}

				"aac3.3.12-glide" ->
				{
					if (!onGround) aac3glideDelay++

					if (aac3glideDelay < 12)
					{
						when (aac3glideDelay)
						{
							2 -> timer.timerSpeed = 1f
							12 -> timer.timerSpeed = 0.1f
						}
					}
					else if (!onGround)
					{
						aac3glideDelay = 0
						thePlayer.motionY = .015
					}
				}

				"aac3.3.13" ->
				{
					if (thePlayer.isDead) wasDead = true

					if (wasDead || onGround)
					{
						wasDead = false
						thePlayer.motionY = aac3_3_13_motion.get().toDouble()
						thePlayer.onGround = false
					}

					timer.timerSpeed = 1f

					if (sneakKeyDown) // Help you to MLG
					{
						timer.timerSpeed = 0.2f
						mc.rightClickDelayTimer = 0
					}
				}

				"watchcat" ->
				{
					MovementUtils.strafe(thePlayer, 0.15f)
					thePlayer.sprinting = true

					if (posY < startY + 2)
					{
						thePlayer.motionY = Math.random() * 0.5
						return@run
					}

					if (startY > posY) MovementUtils.strafe(thePlayer, 0f)
				}

				"spartan185" ->
				{
					thePlayer.motionY = 0.0

					spartanTimer.update()
					if (spartanTimer.hasTimePassed(12))
					{
						networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY + 8, posZ, true))
						networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY - 8, posZ, true))
						spartanTimer.reset()
					}
				}

				"spartan185glide" ->
				{
					MovementUtils.strafe(thePlayer, 0.264f)

					if (thePlayer.ticksExisted % 8 == 0) networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY + 10, posZ, true))
				}

				"neruxvace" ->
				{
					if (!onGround) aac3glideDelay++

					if (aac3glideDelay >= neruxVaceTicks.get() && !onGround)
					{
						aac3glideDelay = 0
						thePlayer.motionY = .015
					}
				}

				"hypixel" ->
				{
					val boostDelay = hypixelTimerBoostDelay.get().toLong()
					val boostTimer = hypixelTimerBoostTimer.get()

					when
					{
						hypixelFlyStarted ->
						{

							// Timer Boost
							if (hypixelTimerBoost.get())
							{
								if (hypixelFlyTimer.hasTimePassed(boostDelay)) timer.timerSpeed = 1.0F
								else timer.timerSpeed = 1.0F + boostTimer * (hypixelFlyTimer.hasTimeLeft(boostDelay).toFloat() / boostDelay.toFloat())
							}

							// ychinc
							if (hypixelYchIncValue.get() && !canPerformHypixelDamageFly)
							{
								hypixelTimer.update()

								if (hypixelTimer.hasTimePassed(2))
								{
									thePlayer.setPosition(posX, posY + 1.0E-5, posZ)
									hypixelTimer.reset()
								}
							}
						}

						!canPerformHypixelDamageFly ->
						{
							// Start without boost
							hypixelFlyStarted = true
							hypixelFlyTimer.reset()
						}

						waitForDamage && thePlayer.hurtTime > 0 ->
						{
							// Start boost after the player takes damage
							if (hypixelJumpValue.get()) jump(theWorld, thePlayer)

							hypixelBoostStep = 1
							hypixelBoostSpeed = 0.1
							lastDistance = 0.0
							hypixelDamageBoostFailed = false
							hypixelFlyStarted = true
							hypixelFlyTimer.reset()
							waitForDamage = false
							markStartY = thePlayer.posY // apply y change caused by jump()
						}
					}
				}

				"freehypixel" ->
				{
					if (freeHypixelTimer.hasTimePassed(10))
					{
						thePlayer.capabilities.isFlying = true
						return@run
					}

					// Watchdog Disabler Exploit
					RotationUtils.setTargetRotation(Rotation(freeHypixelYaw, freeHypixelPitch))

					thePlayer.motionY = 0.0
					thePlayer.motionZ = thePlayer.motionY
					thePlayer.motionX = thePlayer.motionZ

					if (startY == BigDecimal(posY).setScale(3, RoundingMode.HALF_DOWN).toDouble()) freeHypixelTimer.update()
				}

				"bugspartan" ->
				{
					thePlayer.capabilities.isFlying = false

					MovementUtils.zeroXYZ(thePlayer)

					if (jumpKeyDown) thePlayer.motionY += vanillaSpeed
					if (sneakKeyDown) thePlayer.motionY -= vanillaSpeed

					MovementUtils.strafe(thePlayer, vanillaSpeed)
				}

				"redesky" ->
				{
					timer.timerSpeed = 0.3f

					redeskyPacketHClip(thePlayer, 7.0)
					redeskyPacketVClip(thePlayer, 10.0)

					redeskyVClip(thePlayer, -0.5f)
					redeskyHClip(thePlayer, 2.0)

					redeskySpeed(thePlayer, 1)

					thePlayer.motionY = -0.01
				}

				"acp" ->
				{
					thePlayer.motionY = 0.0

					if (jumpKeyDown)
					{
						MovementUtils.zeroXZ(thePlayer)
						thePlayer.motionY = 0.42
					}

					if (sneakKeyDown)
					{
						MovementUtils.zeroXZ(thePlayer)
						thePlayer.motionY = -0.42
					}
				}
			}
		}
	}

	@EventTarget
	fun onMotion(event: MotionEvent)
	{
		val thePlayer = mc.thePlayer ?: return

		val posX = thePlayer.posX
		val posY = thePlayer.posY
		val posZ = thePlayer.posZ

		if (modeValue.get().equals("Hypixel", ignoreCase = true) && canPerformHypixelDamageFly && hypixelFlyStarted)
		{
			when (event.eventState)
			{
				EventState.PRE ->
				{
					if (hypixelYchIncValue.get())
					{
						hypixelTimer.update()
						if (hypixelTimer.hasTimePassed(2))
						{
							thePlayer.setPosition(posX, posY + 1.0E-5, posZ)
							hypixelTimer.reset()
						}
					}

					if (!hypixelDamageBoostFailed) thePlayer.motionY = 0.0
				}

				EventState.POST ->
				{
					val xDist = posX - thePlayer.prevPosX
					val zDist = posZ - thePlayer.prevPosZ
					lastDistance = hypot(xDist, zDist)
				}
			}
		}
		else if (modeValue.get().equals("ACP", ignoreCase = true))
		{
			acpTickTimer.update()
			if (acpTickTimer.hasTimePassed(4))
			{
				thePlayer.setPosition(posX, posY + 1.0E-5, posZ)
				acpTickTimer.reset()
			}
		}

		if (LiquidBounce.moduleManager[Bobbing::class.java].state && bobValue.get() && isMoving(thePlayer)) thePlayer.cameraYaw = 0.1f
	}

	@EventTarget
	fun onRender3D(@Suppress("UNUSED_PARAMETER") event: Render3DEvent?)
	{
		val mode = modeValue.get()

		if (!markValue.get() || mode.equals("Vanilla", ignoreCase = true) || mode.equals("SmoothVanilla", ignoreCase = true) || mode.equals("Hypixel", ignoreCase = true) && !hypixelFlyStarted) return
		val y = markStartY + 2.0

		RenderUtils.drawPlatform(y, if ((mc.thePlayer ?: return).entityBoundingBox.maxY < y) Color(0, 255, 0, 90) else Color(255, 0, 0, 90), 1.0)

		when (mode.toLowerCase())
		{
			"aac1.9.10" -> RenderUtils.drawPlatform(startY + aacJump, Color(0, 0, 255, 90), 1.0)
			"aac3.3.12" -> RenderUtils.drawPlatform(-70.0, Color(0, 0, 255, 90), 1.0)
		}
	}

	@EventTarget
	fun onRender2D(@Suppress("UNUSED_PARAMETER") event: Render2DEvent)
	{
		if (vanillaFlightRemainingTimeValue.get())
		{
			val theWorld = mc.theWorld ?: return
			val provider = classProvider

			GL11.glPushMatrix()

			val moduleManager = LiquidBounce.moduleManager

			val blockOverlay = moduleManager[BlockOverlay::class.java] as BlockOverlay
			if (blockOverlay.state && blockOverlay.infoValue.get() && blockOverlay.getCurrentBlock(theWorld) != null) GL11.glTranslatef(0f, 15f, 0f)

			val scaffold = moduleManager[Scaffold::class.java] as Scaffold
			val tower = moduleManager[Tower::class.java] as Tower
			if (scaffold.state && scaffold.counterDisplayValue.get() || tower.state && tower.counterDisplayValue.get()) GL11.glTranslatef(0f, 15f, 0f)

			val remainingTicks = 80 - vanillaRemainingTime.tick.coerceAtMost(80)
			val info = "You can fly ${if (remainingTicks <= 10) "\u00A7c" else ""}${remainingTicks}\u00A7r more ticks"
			val scaledResolution = provider.createScaledResolution(mc)

			RenderUtils.drawBorderedRect((scaledResolution.scaledWidth shr 1) - 2.0f, (scaledResolution.scaledHeight shr 1) + 5.0f, ((scaledResolution.scaledWidth shr 1) + Fonts.font40.getStringWidth(info)) + 2.0f, (scaledResolution.scaledHeight shr 1) + 16.0f, 3f, -16777216, -16777216)

			provider.glStateManager.resetColor()

			Fonts.font40.drawString(info, (scaledResolution.scaledWidth shr 1).toFloat(), (scaledResolution.scaledHeight shr 1) + 7.0f, 0xffffff)

			GL11.glPopMatrix()
		}
	}

	@EventTarget
	fun onPacket(event: PacketEvent)
	{
		val thePlayer = mc.thePlayer ?: return

		val mode = modeValue.get()

		val provider = classProvider

		if (provider.isCPacketPlayer(event.packet))
		{
			val packetPlayer = event.packet.asCPacketPlayer()

			if (mode.equals("NCP", ignoreCase = true) || mode.equals("Rewinside", ignoreCase = true) || mode.equals("Mineplex", ignoreCase = true) && thePlayer.inventory.getCurrentItemInHand() == null) packetPlayer.onGround = true
			if (mode.equals("Hypixel", ignoreCase = true) && hypixelFlyStarted) packetPlayer.onGround = hypixelOnGroundValue.get()
		}
		else if (provider.isSPacketPlayerPosLook(event.packet) && mode.equals("Hypixel", ignoreCase = true) && canPerformHypixelDamageFly && hypixelFlyStarted && !hypixelDamageBoostFailed)
		{
			hypixelDamageBoostFailed = true
			LiquidBounce.hud.addNotification("Hypixel Damage-Boost Fly", "A teleport has been detected. Disabled Damage-Boost to prevent more flags.", Color.red, 1000L)
		}
	}

	@EventTarget
	fun onMove(event: MoveEvent)
	{
		val thePlayer = mc.thePlayer ?: return

		val func = functions

		when (modeValue.get().toLowerCase())
		{
			"cubecraft" ->
			{
				val yaw = WMathHelper.toRadians(thePlayer.rotationYaw)
				if (cubecraftTeleportTickTimer.hasTimePassed(2))
				{
					event.x = -func.sin(yaw) * 2.4
					event.z = func.cos(yaw) * 2.4

					cubecraftTeleportTickTimer.reset()
				}
				else
				{
					event.x = -func.sin(yaw) * 0.2
					event.z = func.cos(yaw) * 0.2
				}
			}

			"hypixel" ->
			{
				if (!canPerformHypixelDamageFly || !hypixelFlyStarted) return

				if (!isMoving(thePlayer))
				{
					event.x = 0.0
					event.z = 0.0

					thePlayer.motionX = event.x
					thePlayer.motionZ = event.z

					return
				}

				if (hypixelDamageBoostFailed) return

				val step1Speed = if (thePlayer.isPotionActive(classProvider.getPotionEnum(PotionType.MOVE_SPEED))) 1.56 else 2.034
				val speedEffectAffect = 1 + 0.2 * MovementUtils.getSpeedEffectAmplifier(thePlayer)
				val baseSpeed = 0.29 * speedEffectAffect

				when (hypixelBoostStep)
				{
					1 ->
					{
						hypixelBoostSpeed = step1Speed * baseSpeed
						hypixelBoostStep = 2
					}

					2 ->
					{
						hypixelBoostSpeed *= 2.16
						hypixelBoostStep = 3
					}

					3 ->
					{
						hypixelBoostSpeed = lastDistance - (if (thePlayer.ticksExisted % 2 == 0) 0.0103 else 0.0123) * (lastDistance - baseSpeed)
						hypixelBoostStep = 4
					}

					else -> hypixelBoostSpeed = lastDistance - lastDistance / 159.8
				}

				hypixelBoostSpeed = max(hypixelBoostSpeed, 0.3)

				val dir = getDirection(thePlayer)

				event.x = -func.sin(dir) * hypixelBoostSpeed
				event.z = func.cos(dir) * hypixelBoostSpeed

				thePlayer.motionX = event.x
				thePlayer.motionZ = event.z
			}

			"freehypixel" -> if (!freeHypixelTimer.hasTimePassed(10)) event.zero()
		}
	}

	@EventTarget
	fun onBB(event: BlockBBEvent)
	{
		val thePlayer = mc.thePlayer ?: return

		val provider = classProvider

		val mode = modeValue.get()

		if (provider.isBlockAir(event.block) && (mode.equals("Hypixel", ignoreCase = true) && hypixelFlyStarted || mode.equals("Rewinside", ignoreCase = true) || mode.equals("MCCentral", ignoreCase = true) || mode.equals("Mineplex", ignoreCase = true) && thePlayer.inventory.getCurrentItemInHand() == null) && event.y < thePlayer.posY) event.boundingBox = provider.createAxisAlignedBB(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, thePlayer.posY, event.z + 1.0)
	}

	@EventTarget
	fun onJump(e: JumpEvent)
	{
		val thePlayer = mc.thePlayer ?: return

		val mode = modeValue.get()
		if (mode.equals("Hypixel", ignoreCase = true) && hypixelFlyStarted || mode.equals("Rewinside", ignoreCase = true) || mode.equals("MCCentral", ignoreCase = true) || mode.equals("Mineplex", ignoreCase = true) && thePlayer.inventory.getCurrentItemInHand() == null) e.cancelEvent()
	}

	@EventTarget
	fun onStep(e: StepEvent)
	{
		val thePlayer = mc.thePlayer ?: return

		val mode = modeValue.get()
		if (mode.equals("Hypixel", ignoreCase = true) && hypixelFlyStarted || mode.equals("Rewinside", ignoreCase = true) || mode.equals("MCCentral", ignoreCase = true) || mode.equals("Mineplex", ignoreCase = true) && thePlayer.inventory.getCurrentItemInHand() == null) e.stepHeight = 0f
	}

	private fun handleVanillaKickBypass(theWorld: IWorldClient, thePlayer: IEntityPlayerSP)
	{
		if (!vanillaKickBypassValue.get() || !groundTimer.hasTimePassed(1000)) return

		val networkManager = mc.netHandler.networkManager
		val provider = classProvider

		val ground = calculateGround(theWorld, thePlayer)

		val posX = thePlayer.posX
		val originalPosY = thePlayer.posY
		val posZ = thePlayer.posZ

		run {
			var posY = originalPosY
			while (posY > ground)
			{
				networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY, posZ, true))
				if (posY - 8.0 < ground) break // Prevent next step
				posY -= 8.0
			}
		}

		networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, ground, posZ, true))

		var posY = ground
		while (posY < originalPosY)
		{
			networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, posY, posZ, true))
			if (posY + 8.0 > originalPosY) break // Prevent next step
			posY += 8.0
		}

		networkManager.sendPacketWithoutEvent(provider.createCPacketPlayerPosition(posX, originalPosY, posZ, true))

		groundTimer.reset()
	}

	//<editor-fold desc="Redesky Fly">
	private fun redeskyHClip(thePlayer: IEntityPlayerSP, horizontal: Double)
	{
		val func = functions

		val playerYaw = WMathHelper.toRadians(thePlayer.rotationYaw)

		thePlayer.setPosition(thePlayer.posX + horizontal * -func.sin(playerYaw), thePlayer.posY, thePlayer.posZ + horizontal * func.cos(playerYaw))
	}

	private fun redeskyPacketHClip(thePlayer: IEntityPlayerSP, horizontal: Double)
	{
		val func = functions

		val playerYaw = WMathHelper.toRadians(thePlayer.rotationYaw)

		mc.netHandler.networkManager.sendPacketWithoutEvent(classProvider.createCPacketPlayerPosition(thePlayer.posX + horizontal * -func.sin(playerYaw), thePlayer.posY, thePlayer.posZ + horizontal * func.cos(playerYaw), false))
	}

	private fun redeskyVClip(thePlayer: IEntityPlayerSP, vertical: Float)
	{
		thePlayer.setPosition(thePlayer.posX, thePlayer.posY + vertical, thePlayer.posZ)
	}

	private fun redeskyPacketVClip(thePlayer: IEntityPlayerSP, vertical: Double)
	{
		mc.netHandler.networkManager.sendPacketWithoutEvent(classProvider.createCPacketPlayerPosition(thePlayer.posX, thePlayer.posY + vertical, thePlayer.posZ, false))
	}

	private fun redeskySpeed(thePlayer: IEntityPlayerSP, speed: Int)
	{
		val func = functions

		val playerYaw = WMathHelper.toRadians(thePlayer.rotationYaw)
		thePlayer.motionX = (speed * -func.sin(playerYaw)).toDouble()
		thePlayer.motionZ = (speed * func.cos(playerYaw)).toDouble()
	}

	//</editor-fold>

	// TODO: Make better and faster calculation lol
	private fun calculateGround(theWorld: IWorldClient, thePlayer: IEntityPlayerSP): Double
	{
		val playerBoundingBox: IAxisAlignedBB = thePlayer.entityBoundingBox
		var blockHeight = 1.0
		var ground = thePlayer.posY
		while (ground > 0.0)
		{
			val customBox = classProvider.createAxisAlignedBB(playerBoundingBox.maxX, ground + blockHeight, playerBoundingBox.maxZ, playerBoundingBox.minX, ground, playerBoundingBox.minZ)
			if (theWorld.checkBlockCollision(customBox))
			{
				if (blockHeight <= 0.05) return ground + blockHeight

				ground += blockHeight
				blockHeight = 0.05
			}
			ground -= blockHeight
		}
		return 0.0
	}

	private fun jump(theWorld: IWorldClient, thePlayer: IEntityPlayerSP)
	{
		val provider = classProvider

		val blockAboveState = BlockUtils.getState(WBlockPos(thePlayer.posX, thePlayer.posY + 2, thePlayer.posZ))
		val blockAbove = blockAboveState?.block
		val normalJumpY = 0.42 + MovementUtils.getEffectAmplifier(thePlayer, PotionType.JUMP) * 0.1f
		val jumpY = if (provider.isBlockAir(blockAbove)) normalJumpY else min(blockAboveState?.let { BlockUtils.getBlockCollisionBox(theWorld, it)?.minY?.plus(0.2) } ?: normalJumpY, normalJumpY)

		// Simulate Vanilla Player Jump
		thePlayer.setPosition(thePlayer.posX, thePlayer.posY + jumpY, thePlayer.posZ)

		// Jump Boost
		if (thePlayer.sprinting)
		{
			val func = functions

			val dir = getDirection(thePlayer)
			thePlayer.motionX -= func.sin(dir) * 0.2f
			thePlayer.motionZ += func.cos(dir) * 0.2f
		}
		thePlayer.isAirBorne = true

		// ForgeHooks.onLivingJump(thePlayer)
		thePlayer.triggerAchievement(provider.getStatEnum(StatType.JUMP_STAT))
	}

	override val tag: String
		get() = modeValue.get()

	val shouldDisableNoFall: Boolean
		get() = waitForDamage || modeValue.get().equals("AAC1.9.10", ignoreCase = true)
}

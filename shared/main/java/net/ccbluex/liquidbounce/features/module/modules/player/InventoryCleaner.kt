/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.enums.BlockType
import net.ccbluex.liquidbounce.api.enums.EnchantmentType
import net.ccbluex.liquidbounce.api.enums.EnumFacingType
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityPlayerSP
import net.ccbluex.liquidbounce.api.minecraft.inventory.IContainer
import net.ccbluex.liquidbounce.api.minecraft.item.IItem
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketPlayerDigging
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoArmor
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoPot
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.item.ArmorPiece
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.ccbluex.liquidbounce.utils.timer.Cooldown
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import kotlin.random.Random

@ModuleInfo(name = "InventoryCleaner", description = "Automatically throws away useless items.", category = ModuleCategory.PLAYER)
class InventoryCleaner : Module()
{

	/**
	 * OPTIONS
	 */

	val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 600, 0, 1000)
	{
		override fun onChanged(oldValue: Int, newValue: Int)
		{
			val minCPS = minDelayValue.get()
			if (minCPS > newValue) set(minCPS)
		}
	}

	val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 400, 0, 1000)
	{
		override fun onChanged(oldValue: Int, newValue: Int)
		{
			val maxDelay = maxDelayValue.get()
			if (maxDelay < newValue) set(maxDelay)
		}
	}

	private val maxHotbarDelayValue: IntegerValue = object : IntegerValue("MaxHotbarDelay", 250, 0, 1000)
	{
		override fun onChanged(oldValue: Int, newValue: Int)
		{
			val minDelay = minHotbarDelayValue.get()
			if (minDelay > newValue) this.set(minDelay)
		}
	}

	private val minHotbarDelayValue: IntegerValue = object : IntegerValue("MinHotbarDelay", 200, 0, 1000)
	{
		override fun onChanged(oldValue: Int, newValue: Int)
		{
			val maxDelay = maxHotbarDelayValue.get()
			if (maxDelay < newValue) this.set(maxDelay)
		}
	}

	// Bypass
	private val invOpenValue = BoolValue("InvOpen", false)
	private val simulateInventory = BoolValue("SimulateInventory", true)
	private val noMoveValue = BoolValue("NoMove", false)

	// Hotbar
	private val hotbarValue = BoolValue("Hotbar", true)

	// Bypass
	private val randomSlotValue = BoolValue("RandomSlot", false)
	private val itemDelayValue = IntegerValue("ItemDelay", 0, 0, 5000)

	private val allowMisclicksValue = BoolValue("ClickMistakes", false)
	private val misclicksRateValue = IntegerValue("ClickMistakeRate", 5, 0, 100)

	// Sort
	private val items = arrayOf("None", "Ignore", "Sword", "Bow", "Pickaxe", "Axe", "Food", "Block", "Water", "Gapple", "Pearl")

	private val sortValue = BoolValue("Sort", true)

	private val slot1Value = ListValue("Slot-1", items, "Sword")
	private val slot2Value = ListValue("Slot-2", items, "Bow")
	private val slot3Value = ListValue("Slot-3", items, "Pickaxe")
	private val slot4Value = ListValue("Slot-4", items, "Axe")
	private val slot5Value = ListValue("Slot-5", items, "None")
	private val slot6Value = ListValue("Slot-6", items, "None")
	private val slot7Value = ListValue("Slot-7", items, "Food")
	private val slot8Value = ListValue("Slot-8", items, "Block")
	private val slot9Value = ListValue("Slot-9", items, "Block")

	// Item Filter Options
	private val keepOldSwordValue = BoolValue("KeepOldSword", false)
	private val keepOldToolsValue = BoolValue("KeepOldTools", false)

	private val bowAndArrowValue = BoolValue("BowAndArrow", true)
	private val bucketValue = BoolValue("Bucket", true)
	private val compassValue = BoolValue("Compass", true)
	private val enderPearlValue = BoolValue("EnderPearl", true)
	private val bedValue = BoolValue("Bed", true)
	private val ironIngotValue = BoolValue("IronIngot", true)
	private val diamondValue = BoolValue("Diamond", true)
	private val potionValue = BoolValue("Potion", true)
	private val foodValue = BoolValue("Food", true)

	private val ignoreVehiclesValue = BoolValue("IgnoreVehicles", false)

	// Visuals
	private val indicateClick = BoolValue("ClickIndicationh", false)
	private val indicateLength = IntegerValue("ClickIndicationLength", 100, 50, 200)

	/**
	 * VALUES
	 */

	private var delay = 0L

	private val infoUpdateCooldown = Cooldown.getNewCooldownMiliseconds(100)

	private var cachedInfo: String? = null

	val advancedInformations: String
		get()
		{
			val cache = cachedInfo

			return if (cache == null || infoUpdateCooldown.attemptReset()) (if (!state) "InventoryCleaner is not active"
			else
			{
				val minDelay = minDelayValue.get()
				val maxDelay = maxDelayValue.get()
				val random = randomSlotValue.get()
				val noMove = noMoveValue.get()
				val hotbar = hotbarValue.get()
				val itemDelay = itemDelayValue.get()
				val misclick = allowMisclicksValue.get()
				val misclickRate = misclicksRateValue.get()

				"InventoryCleaner active [delay: ($minDelay ~ $maxDelay), itemdelay: $itemDelay, random: $random, nomove: $noMove, hotbar: $hotbar${if (misclick) ", misclick($misclickRate%)" else ""}]"
			}).apply { cachedInfo = this }
			else cache
		}

	@EventTarget
	fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent)
	{
		val thePlayer = mc.thePlayer ?: return
		val inventoryContainer = thePlayer.inventoryContainer
		val openContainer = thePlayer.openContainer

		// Delay, openContainer Check
		if (!InventoryUtils.CLICK_TIMER.hasTimePassed(delay) || openContainer != null && openContainer.windowId != 0) return

		val hotbar = hotbarValue.get()

		val provider = classProvider

		val screen = mc.currentScreen

		// Clean hotbar
		if (hotbar && !provider.isGuiInventory(screen))
		{
			val hotbarItems = items(36, 45, inventoryContainer)
			val garbageItemsHotbarSlots = hotbarItems.filter { !isUseful(thePlayer, it.key, it.value, container = inventoryContainer) }.keys.toMutableList()

			// Break if there is no garbage items in hotbar
			if (garbageItemsHotbarSlots.isNotEmpty())
			{
				val netHandler = mc.netHandler
				val randomSlot = randomSlotValue.get()

				var garbageHotbarItem = if (randomSlot) garbageItemsHotbarSlots[Random.nextInt(garbageItemsHotbarSlots.size)] else garbageItemsHotbarSlots.first()

				var misclick = false

				val misclickRate = misclicksRateValue.get()

				// Simulate Click Mistakes to bypass some anti-cheats
				if (allowMisclicksValue.get() && misclickRate > 0 && Random.nextInt(100) <= misclickRate)
				{
					val firstEmpty: Int = firstEmpty(hotbarItems, randomSlot)
					if (firstEmpty != -1) garbageHotbarItem = firstEmpty
					misclick = true
				}

				// Switch to the slot of garbage item

				netHandler.addToSendQueue(provider.createCPacketHeldItemChange(garbageHotbarItem - 36))

				// Drop items
				val amount = getAmount(garbageHotbarItem, inventoryContainer)
				val action = if (amount > 1 || (amount == 1 && Math.random() > 0.8)) ICPacketPlayerDigging.WAction.DROP_ALL_ITEMS else ICPacketPlayerDigging.WAction.DROP_ITEM
				netHandler.addToSendQueue(provider.createCPacketPlayerDigging(action, WBlockPos.ORIGIN, provider.getEnumFacing(EnumFacingType.DOWN)))

				if (indicateClick.get() && screen != null && provider.isGuiContainer(screen)) screen.asGuiContainer().highlight(garbageHotbarItem, indicateLength.get().toLong(), if (misclick) -2130771968 else -2147418368)

				// Back to the original holding slot
				netHandler.addToSendQueue(provider.createCPacketHeldItemChange(thePlayer.inventory.currentItem))

				delay = TimeUtils.randomDelay(minHotbarDelayValue.get(), maxHotbarDelayValue.get())
			}
		}

		// NoMove, AutoArmorLock Check
		if (noMoveValue.get() && MovementUtils.isMoving(thePlayer) || (LiquidBounce.moduleManager[AutoArmor::class.java] as AutoArmor).isLocked) return

		if (!provider.isGuiInventory(screen) && invOpenValue.get()) return

		// Sort hotbar
		if (sortValue.get()) sortHotbar(thePlayer)

		// Clean inventory
		cleanInventory(thePlayer, end = if (hotbar) 45 else 36, container = inventoryContainer)
	}

	private fun cleanInventory(thePlayer: IEntityPlayerSP, start: Int = 9, end: Int = 45, container: IContainer)
	{
		val controller = mc.playerController
		val netHandler = mc.netHandler
		val screen = mc.currentScreen

		val clickTimer = InventoryUtils.CLICK_TIMER

		while (clickTimer.hasTimePassed(delay))
		{
			val items = items(start, end, container)
			val garbageItems = items.filterNot { isUseful(thePlayer, it.key, it.value, container = container) }.keys.toMutableList()

			// Return true if there is no remaining garbage items in the inventory
			if (garbageItems.isEmpty()) return

			val randomSlot = randomSlotValue.get()
			val misclickRate = misclicksRateValue.get()

			var garbageItem = if (randomSlot) garbageItems[Random.nextInt(garbageItems.size)] else garbageItems.first()

			var misclick = false

			// Simulate Click Mistakes to bypass some anti-cheats
			if (allowMisclicksValue.get() && misclickRate > 0 && Random.nextInt(100) <= misclickRate)
			{
				val firstEmpty: Int = firstEmpty(items, randomSlot)
				if (firstEmpty != -1) garbageItem = firstEmpty
				misclick = true
			}

			val provider = classProvider

			// SimulateInventory
			val openInventory = simulateInventory.get() && !provider.isGuiInventory(screen)
			if (openInventory) netHandler.addToSendQueue(createOpenInventoryPacket())

			// Drop all useless items
			val amount = getAmount(garbageItem, container)

			if (amount > 1 || /* Mistake simulation */ Random.nextBoolean()) controller.windowClick(container.windowId, garbageItem, 1, 4, thePlayer) else controller.windowClick(container.windowId, garbageItem, 0, 4, thePlayer)

			if (indicateClick.get() && screen != null && provider.isGuiContainer(screen)) screen.asGuiContainer().highlight(garbageItem, indicateLength.get().toLong(), if (misclick) -2130771968 else -2147418368)

			clickTimer.reset() // For more compatibility with custom MSTimer(s)

			// SimulateInventory
			if (openInventory) netHandler.addToSendQueue(provider.createCPacketCloseWindow())

			delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
		}

		return
	}

	/**
	 * Checks if the item is useful
	 *
	 * @param slot Slot id of the item.
	 * @return Returns true when the item is useful
	 */
	fun isUseful(thePlayer: IEntityPlayerSP, slot: Int, itemStack: IItemStack, start: Int = 0, end: Int = 45, container: IContainer): Boolean
	{
		return try
		{
			val item = itemStack.item

			val provider = classProvider

			when
			{
				provider.isItemSword(item) || provider.isItemTool(item) ->
				{
					if ((provider.isItemSword(item) && keepOldSwordValue.get()) || (provider.isItemTool(item) && keepOldToolsValue.get())) return true

					if (slot >= 36 && findBetterItem(thePlayer, slot - 36, thePlayer.inventory.getStackInSlot(slot - 36)) == slot - 36) return true

					repeat(9) { if ((type(it).equals("sword", true) && provider.isItemSword(item) || type(it).equals("pickaxe", true) && provider.isItemPickaxe(item) || type(it).equals("axe", true) && provider.isItemAxe(item)) && findBetterItem(thePlayer, it, thePlayer.inventory.getStackInSlot(it)) == null) return@isUseful true }

					val damage = (itemStack.getAttributeModifier("generic.attackDamage").firstOrNull()?.amount ?: 0.0) + 1.25 * ItemUtils.getEnchantment(itemStack, provider.getEnchantmentEnum(EnchantmentType.SHARPNESS))

					items(start, end, container = container).none { (otherSlot, stack) -> otherSlot != slot && stack != itemStack && stack.javaClass == itemStack.javaClass && damage < (stack.getAttributeModifier("generic.attackDamage").firstOrNull()?.amount ?: 0.0) + 1.25 * ItemUtils.getEnchantment(stack, provider.getEnchantmentEnum(EnchantmentType.SHARPNESS)) }
				}

				bowAndArrowValue.get() && provider.isItemBow(item) ->
				{
					val currentPower = ItemUtils.getEnchantment(itemStack, provider.getEnchantmentEnum(EnchantmentType.POWER))

					items(start, end, container = container).none { (otherSlot, stack) -> otherSlot != slot && itemStack != stack && provider.isItemBow(stack.item) && currentPower < ItemUtils.getEnchantment(stack, provider.getEnchantmentEnum(EnchantmentType.POWER)) }
				}

				provider.isItemArmor(item) ->
				{
					val currentArmor = ArmorPiece(itemStack, slot)

					items(start, end, container = container).none { (otherSlot, otherStack) ->
						if (otherSlot != slot && otherStack != itemStack && provider.isItemArmor(otherStack.item))
						{
							val armor = ArmorPiece(otherStack, otherSlot)

							if (armor.armorType != currentArmor.armorType) false
							else AutoArmor.ARMOR_COMPARATOR.compare(currentArmor, armor) <= 0
						}
						else false
					}
				}

				compassValue.get() && itemStack.unlocalizedName == "item.compass" -> items(start, end, container = container).none { (otherSlot, stack) -> otherSlot != slot && itemStack != stack && stack.unlocalizedName == "item.compass" }

				else -> foodValue.get() && provider.isItemFood(item) || bowAndArrowValue.get() && itemStack.unlocalizedName == "item.arrow" || provider.isItemBlock(item) && !provider.isBlockBush(item?.asItemBlock()?.block) || bedValue.get() && provider.isItemBed(item) || diamondValue.get() && itemStack.unlocalizedName == "item.diamond" || ironIngotValue.get() && itemStack.unlocalizedName == "item.ingotIron" || potionValue.get() && provider.isItemPotion(item) && AutoPot.isPotionUseful(itemStack) || enderPearlValue.get() && provider.isItemEnderPearl(item) || provider.isItemEnchantedBook(item) || bucketValue.get() && provider.isItemBucket(item) || itemStack.unlocalizedName == "item.stick" || ignoreVehiclesValue.get() && (provider.isItemBoat(item) || provider.isItemMinecart(item))
			}
		}
		catch (ex: Exception)
		{
			ClientUtils.logger.error("(InventoryCleaner) Failed to check item: ${itemStack.unlocalizedName}.", ex)

			true
		}
	}

	/**
	 * INVENTORY SORTER
	 */

	/**
	 * Sort hotbar
	 */
	private fun sortHotbar(thePlayer: IEntityPlayerSP)
	{
		val provider = classProvider
		val netHandler = mc.netHandler

		(0..8).mapNotNull { it to (findBetterItem(thePlayer, it, thePlayer.inventory.getStackInSlot(it)) ?: return@mapNotNull null) }.firstOrNull { (index, bestItem) -> index != bestItem }?.let { (index, bestItem) ->
			val openInventory = !provider.isGuiInventory(mc.currentScreen) && simulateInventory.get()

			if (openInventory) netHandler.addToSendQueue(createOpenInventoryPacket())

			mc.playerController.windowClick(0, if (bestItem < 9) bestItem + 36 else bestItem, index, 2, thePlayer)

			if (openInventory) netHandler.addToSendQueue(provider.createCPacketCloseWindow())

			delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
		}
	}

	private fun findBetterItem(thePlayer: IEntityPlayerSP, targetSlot: Int, slotStack: IItemStack?): Int?
	{
		val type = type(targetSlot)

		val provider = classProvider

		when (type.toLowerCase())
		{
			"sword", "pickaxe", "axe" ->
			{
				// Supressed by kotlin compiler bug
				// https://youtrack.jetbrains.com/issue/KT-17018
				// https://youtrack.jetbrains.com/issue/KT-38704
				@Suppress("ConvertLambdaToReference")
				val currentTypeChecker: ((IItem?) -> Boolean) = when
				{
					type.equals("Sword", ignoreCase = true) -> { item: IItem? -> provider.isItemSword(item) }
					type.equals("Pickaxe", ignoreCase = true) -> { obj: IItem? -> provider.isItemPickaxe(obj) }
					type.equals("Axe", ignoreCase = true) -> { obj: IItem? -> provider.isItemAxe(obj) }
					else -> return null
				}

				var bestWeapon = if (currentTypeChecker(slotStack?.item)) targetSlot
				else -1

				thePlayer.inventory.mainInventory.asSequence().withIndex().filter { currentTypeChecker(it.value?.item) }.map { it.index to it.value as IItemStack }.filter { !type(it.first).equals(type, ignoreCase = true) }.forEach { (index, stack) ->
					if (bestWeapon == -1) bestWeapon = index
					else
					{
						val currDamage = (stack.getAttributeModifier("generic.attackDamage").firstOrNull()?.amount ?: 0.0) + 1.25 * ItemUtils.getEnchantment(stack, provider.getEnchantmentEnum(EnchantmentType.SHARPNESS))

						val bestStack = thePlayer.inventory.getStackInSlot(bestWeapon) ?: return@forEach
						val bestDamage = (bestStack.getAttributeModifier("generic.attackDamage").firstOrNull()?.amount ?: 0.0) + 1.25 * ItemUtils.getEnchantment(bestStack, provider.getEnchantmentEnum(EnchantmentType.SHARPNESS))

						if (bestDamage < currDamage) bestWeapon = index
					}
				}

				return if (bestWeapon != -1 || bestWeapon == targetSlot) bestWeapon else null
			}

			"bow" -> if (bowAndArrowValue.get())
			{
				var bestBow = if (provider.isItemBow(slotStack?.item)) targetSlot else -1
				var bestPower = if (bestBow != -1) ItemUtils.getEnchantment(slotStack, provider.getEnchantmentEnum(EnchantmentType.POWER))
				else 0

				thePlayer.inventory.mainInventory.asSequence().withIndex().filter { provider.isItemBow(it.value?.item) }.map { it.index to it.value as IItemStack }.filter { !type(it.first).equals(type, ignoreCase = true) }.forEach { (index, stack) ->
					if (bestBow == -1) bestBow = index
					else
					{
						val power = ItemUtils.getEnchantment(stack, provider.getEnchantmentEnum(EnchantmentType.POWER))

						if (ItemUtils.getEnchantment(stack, provider.getEnchantmentEnum(EnchantmentType.POWER)) > bestPower)
						{
							bestBow = index
							bestPower = power
						}
					}
				}

				return if (bestBow != -1) bestBow else null
			}

			"food" -> if (foodValue.get()) thePlayer.inventory.mainInventory.asSequence().withIndex().filter { provider.isItemFood(it.value?.item) }.map { it.index to it.value as IItemStack }.filter { !provider.isItemAppleGold(it.second) }.filter { !type(it.first).equals("Food", ignoreCase = true) }.toList().forEach { (index, stack) -> return@findBetterItem if (ItemUtils.isStackEmpty(slotStack) || !provider.isItemFood(stack.item)) index else null }
			"block" -> thePlayer.inventory.mainInventory.asSequence().withIndex().filter { provider.isItemBlock(it.value?.item) }.mapNotNull { it.index to (it.value?.item?.asItemBlock() ?: return@mapNotNull null) }.filter { !InventoryUtils.AUTOBLOCK_BLACKLIST.contains(it.second.block) }.filter { !type(it.first).equals("Block", ignoreCase = true) }.forEach { (index, item) -> return@findBetterItem if (ItemUtils.isStackEmpty(slotStack) || !provider.isItemBlock(item)) index else null }

			"water" -> if (bucketValue.get())
			{
				val flowingWater = provider.getBlockEnum(BlockType.FLOWING_WATER)
				thePlayer.inventory.mainInventory.asSequence().withIndex().filter { provider.isItemBucket(it.value?.item) }.mapNotNull { it.index to (it.value?.item?.asItemBucket() ?: return@mapNotNull null) }.filter { it.second.isFull == flowingWater }.filter { !type(it.first).equals("Water", ignoreCase = true) }.toList().forEach { (index, item) -> return@findBetterItem if (ItemUtils.isStackEmpty(slotStack) || !provider.isItemBucket(item) || (item.asItemBucket()).isFull != flowingWater) index else null }
			}

			"gapple" -> if (foodValue.get()) thePlayer.inventory.mainInventory.asSequence().withIndex().filter { provider.isItemAppleGold(it.value?.item) }.filter { !type(it.index).equals("Gapple", ignoreCase = true) }.forEach { return@findBetterItem if (ItemUtils.isStackEmpty(slotStack) || !provider.isItemAppleGold(slotStack?.item)) it.index else null }
			"pearl" -> if (enderPearlValue.get()) thePlayer.inventory.mainInventory.asSequence().withIndex().filter { provider.isItemEnderPearl(it.value?.item) }.filter { !type(it.index).equals("Pearl", ignoreCase = true) }.forEach { return@findBetterItem if (ItemUtils.isStackEmpty(slotStack) || !provider.isItemEnderPearl(slotStack?.item)) it.index else null }
		}

		return null
	}

	/**
	 * Get items in inventory
	 */
	private fun items(start: Int = 0, end: Int = 45, container: IContainer): Map<Int, IItemStack>
	{
		val items = mutableMapOf<Int, IItemStack>()

		val itemDelay = itemDelayValue.get()

		(end - 1 downTo start).mapNotNull { it to (container.getSlot(it).stack ?: return@mapNotNull null) }.filter { (slot, stack) -> !ItemUtils.isStackEmpty(stack) && (slot !in 36..44 || !type(slot).equals("Ignore", ignoreCase = true)) && System.currentTimeMillis() - (stack).itemDelay >= itemDelay }.forEach { (i, stack) -> items[i] = stack }

		return items
	}

	private fun firstEmpty(slots: Map<Int, IItemStack?>?, random: Boolean): Int
	{
		slots ?: return -1

		val emptySlots = mutableListOf<Int>()

		slots.forEach { map: Map.Entry<Int, IItemStack?> ->
			if (map.value == null) emptySlots.add(map.key)
		}

		if (emptySlots.isEmpty()) return -1

		return if (random) emptySlots[Random.nextInt(emptySlots.size)] else emptySlots.first()
	}

	private fun getAmount(slot: Int, container: IContainer): Int
	{
		val itemStack = container.inventorySlots[slot].stack ?: return -1
		itemStack.item ?: return -1
		return itemStack.stackSize
	}

	/**
	 * Get type of [targetSlot]
	 */
	private fun type(targetSlot: Int) = when (targetSlot)
	{
		0 -> slot1Value.get()
		1 -> slot2Value.get()
		2 -> slot3Value.get()
		3 -> slot4Value.get()
		4 -> slot5Value.get()
		5 -> slot6Value.get()
		6 -> slot7Value.get()
		7 -> slot8Value.get()
		8 -> slot9Value.get()
		else -> ""
	}

	override val tag: String
		get() = "${minDelayValue.get()} ~ ${maxDelayValue.get()}"
}

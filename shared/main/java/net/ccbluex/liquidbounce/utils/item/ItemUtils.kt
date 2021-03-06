/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils.item

import net.ccbluex.liquidbounce.api.minecraft.enchantments.IEnchantment
import net.ccbluex.liquidbounce.api.minecraft.item.IItem
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.api.minecraft.nbt.INBTTagCompound
import net.ccbluex.liquidbounce.utils.ClientUtils.logger
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import java.util.regex.Pattern
import kotlin.math.min

/**
 * @author MCModding4K
 */
object ItemUtils : MinecraftInstance()
{
	/**
	 * Allows you to create a item using the item json
	 *
	 * @param  itemArguments
	 * arguments of item
	 * @return               created item
	 * @author               MCModding4K
	 */
	@JvmStatic
	fun createItem(itemArguments: String): IItemStack?
	{
		return try
		{
			val fixedItemArguments = itemArguments.replace('&', '\u00A7') // Translate Colorcodes

			val provider = classProvider

			var item: IItem? = provider.createItem()
			var args: List<String>? = null

			val modeSize = min(12, fixedItemArguments.length - 2)


			(0 until modeSize).any {
				args = fixedItemArguments.substring(it).split(" ")
				item = functions.getObjectFromItemRegistry(provider.createResourceLocation((args ?: return@any false)[0]))

				item != null
			}

			val createdItem = item ?: return null

			val checkedArgs = args ?: return null
			val argsLength = checkedArgs.size

			// Item amount
			var amount = 1
			if (argsLength >= 2 && PATTERN.matcher(checkedArgs[1]).matches()) amount = checkedArgs[1].toInt()

			// Item meta
			var meta = 0
			if (argsLength >= 3 && PATTERN.matcher(checkedArgs[2]).matches()) meta = checkedArgs[2].toInt()

			val itemstack = provider.createItemStack(createdItem, amount, meta)

			// Build NBT tag
			if (argsLength >= 4)
			{
				val nbtBuilder = StringBuilder()

				for (nbtcount in 3 until argsLength) nbtBuilder.append(" ${checkedArgs[nbtcount]}")

				itemstack.tagCompound = provider.jsonToNBTInstance.getTagFromJson("$nbtBuilder")
			}

			itemstack
		}
		catch (e: Exception)
		{
			// noinspection StringConcatenationArgumentToLogCall
			logger.error("Can't create the item with arguments \"${itemArguments.take(64)}\"", e)
			null
		}
	}

	@JvmStatic
	fun getEnchantment(itemStack: IItemStack?, enchantment: IEnchantment): Int
	{
		val enchTagList = itemStack?.enchantmentTagList

		return if (enchTagList == null || enchTagList.hasNoTags()) 0 else (0 until enchTagList.tagCount()).map(enchTagList::getCompoundTagAt).firstOrNull { it.hasKey("ench") && it.getShort("ench").toInt() == enchantment.effectId || it.hasKey("id") && it.getShort("id").toInt() == enchantment.effectId }?.let { tagCompound: INBTTagCompound -> tagCompound.getShort("lvl").toInt() } ?: 0
	}

	@JvmStatic
	fun getEnchantmentCount(itemStack: IItemStack?): Int
	{
		val enchTagList = itemStack?.enchantmentTagList

		return if (enchTagList == null || enchTagList.hasNoTags()) 0 else (0 until enchTagList.tagCount()).map(enchTagList::getCompoundTagAt).count { it.hasKey("ench") || it.hasKey("id") }
	}

	@JvmStatic
	fun isStackEmpty(stack: IItemStack?): Boolean = stack == null || classProvider.isItemAir(stack.item)

	@JvmStatic
	private val PATTERN = Pattern.compile("\\d+")
}

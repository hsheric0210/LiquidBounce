/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.tabs

import net.ccbluex.liquidbounce.api.enums.BlockType
import net.ccbluex.liquidbounce.api.enums.ItemType
import net.ccbluex.liquidbounce.api.minecraft.item.IItem
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack
import net.ccbluex.liquidbounce.api.util.WrappedCreativeTabs
import net.ccbluex.liquidbounce.injection.backend.WrapperImpl.classProvider

class BlocksTab : WrappedCreativeTabs("Special blocks")
{

	/**
	 * Initialize of special blocks tab
	 */
	init
	{
		representedType.backgroundImageName = "item_search.png"
	}

	/**
	 * Add all items to tab
	 *
	 * @param itemList list of tab items
	 */
	override fun displayAllReleventItems(itemList: MutableList<IItemStack>)
	{
		val provider = classProvider

		itemList.add(provider.createItemStack(provider.getBlockEnum(BlockType.COMMAND_BLOCK)))
		itemList.add(provider.createItemStack(provider.getItemEnum(ItemType.COMMAND_BLOCK_MINECART)))
		itemList.add(provider.createItemStack(provider.getBlockEnum(BlockType.BARRIER)))
		itemList.add(provider.createItemStack(provider.getBlockEnum(BlockType.DRAGON_EGG)))
		itemList.add(provider.createItemStack(provider.getBlockEnum(BlockType.BROWN_MUSHROOM_BLOCK)))
		itemList.add(provider.createItemStack(provider.getBlockEnum(BlockType.RED_MUSHROOM_BLOCK)))
		itemList.add(provider.createItemStack(provider.getBlockEnum(BlockType.FARMLAND)))
		itemList.add(provider.createItemStack(provider.getBlockEnum(BlockType.MOB_SPAWNER)))
		itemList.add(provider.createItemStack(provider.getBlockEnum(BlockType.LIT_FURNACE)))
	}

	/**
	 * Return icon item of tab
	 *
	 * @return icon item
	 */
	override fun getTabIconItem(): IItem
	{
		val provider = classProvider

		return provider.createItemStack(provider.getBlockEnum(BlockType.COMMAND_BLOCK)).item!!
	}

	/**
	 * Return name of tab
	 *
	 * @return tab name
	 */
	override fun getTranslatedTabLabel() = "Special blocks"

	/**
	 * @return searchbar status
	 */
	override fun hasSearchBar() = true
}

/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */

package net.ccbluex.liquidbounce.api.util

import net.ccbluex.liquidbounce.LiquidBounce.wrapper
import net.ccbluex.liquidbounce.api.enums.ItemType
import net.ccbluex.liquidbounce.api.minecraft.creativetabs.ICreativeTabs
import net.ccbluex.liquidbounce.api.minecraft.item.IItem
import net.ccbluex.liquidbounce.api.minecraft.item.IItemStack

abstract class WrappedCreativeTabs(val name: String)
{
	lateinit var representedType: ICreativeTabs

	init
	{
		@Suppress("LeakingThis") wrapper.classProvider.wrapCreativeTab(name, this)
	}

	open fun displayAllReleventItems(itemList: MutableList<IItemStack>)
	{
	}

	open fun getTranslatedTabLabel(): String = "asdf"
	open fun getTabIconItem(): IItem = wrapper.classProvider.getItemEnum(ItemType.WRITABLE_BOOK)
	open fun hasSearchBar(): Boolean = true
}

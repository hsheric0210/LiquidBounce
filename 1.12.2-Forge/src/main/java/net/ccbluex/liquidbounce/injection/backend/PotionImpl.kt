/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */

package net.ccbluex.liquidbounce.injection.backend

import net.ccbluex.liquidbounce.api.minecraft.potion.IPotion
import net.minecraft.potion.Potion

class PotionImpl(val wrapped: Potion) : IPotion
{
	override val liquidColor: Int
		get() = wrapped.liquidColor
	override val id: Int
		get() = Potion.getIdFromPotion(wrapped)
	override val name: String
		get() = wrapped.name

	override fun equals(other: Any?): Boolean = other is PotionImpl && other.wrapped == wrapped
}

fun IPotion.unwrap(): Potion = (this as PotionImpl).wrapped
fun Potion.wrap(): IPotion = PotionImpl(this)

/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */

package net.ccbluex.liquidbounce.injection.backend

import net.ccbluex.liquidbounce.api.minecraft.client.render.IThreadDownloadImageData
import net.minecraft.client.renderer.ThreadDownloadImageData

class ThreadDownloadImageDataImpl<T : ThreadDownloadImageData>(wrapped: T) : AbstractTextureImpl<T>(wrapped), IThreadDownloadImageData
{
	override fun equals(other: Any?): Boolean = other is ThreadDownloadImageDataImpl<*> && other.wrapped == wrapped
}

fun IThreadDownloadImageData.unwrap(): ThreadDownloadImageData = (this as ThreadDownloadImageDataImpl<*>).wrapped
fun ThreadDownloadImageData.wrap(): IThreadDownloadImageData = ThreadDownloadImageDataImpl(this)

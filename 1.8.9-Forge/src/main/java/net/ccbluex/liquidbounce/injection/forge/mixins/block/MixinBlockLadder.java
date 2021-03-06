/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.block;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.movement.FastClimb;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockLadder.class)
@SideOnly(Side.CLIENT)
public abstract class MixinBlockLadder extends MixinBlock
{

	@Shadow
	@Final
	public static PropertyDirection FACING;

	/**
	 * @author CCBlueX
	 * @reason FastClimb AAC3.0.0
	 */
	@Overwrite
	public void setBlockBoundsBasedOnState(final IBlockAccess worldIn, final BlockPos pos)
	{
		final IBlockState state = worldIn.getBlockState(pos);

		// AAC 3.0.0 FastClimb
		if (state.getBlock() instanceof BlockLadder)
		{
			final FastClimb fastClimb = (FastClimb) LiquidBounce.moduleManager.get(FastClimb.class);
			final float boxSize = fastClimb.getState() && "AAC3.0.0".equalsIgnoreCase(fastClimb.getModeValue().get()) ? 0.99f : 0.125f;

			switch (state.getValue(FACING))
			{
				case NORTH:
					setBlockBounds(0.0F, 0.0F, 1.0F - boxSize, 1.0F, 1.0F, 1.0F);
					break;
				case SOUTH:
					setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, boxSize);
					break;
				case WEST:
					setBlockBounds(1.0F - boxSize, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
					break;
				case EAST:
				default:
					setBlockBounds(0.0F, 0.0F, 0.0F, boxSize, 1.0F, 1.0F);
			}
		}
	}
}

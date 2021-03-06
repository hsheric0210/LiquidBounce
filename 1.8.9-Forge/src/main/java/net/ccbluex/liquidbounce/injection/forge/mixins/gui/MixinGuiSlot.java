/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.injection.implementations.IMixinGuiSlot;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiSlot.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiSlot implements IMixinGuiSlot
{
	private int listWidth = 220;
	private boolean enableScissor;

	@Shadow
	protected boolean field_178041_q;

	@Shadow
	protected int mouseX;

	@Shadow
	protected int mouseY;

	@Shadow
	protected abstract void drawBackground();

	@Shadow
	protected abstract void bindAmountScrolled();

	@Shadow
	public int left;

	@Shadow
	public int top;

	@Shadow
	public int width;

	@Shadow
	protected float amountScrolled;

	@Shadow
	protected boolean hasListHeader;

	@Shadow
	protected abstract void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_);

	@Shadow
	protected abstract void drawSelectionBox(int p_148120_1_, int p_148120_2_, int mouseXIn, int mouseYIn);

	@Shadow
	public int right;

	@Shadow
	public int bottom;

	@Shadow
	@Final
	protected Minecraft mc;

	@Shadow
	public int height;

	@Shadow
	protected abstract int getContentHeight();

	@Shadow
	public abstract int func_148135_f();

	@Shadow
	protected abstract void func_148142_b(int p_148142_1_, int p_148142_2_);

	/**
	 * @author CCBlueX
	 * @reason
	 */
	@Overwrite
	public void drawScreen(final int mouseXIn, final int mouseYIn, final float p_148128_3_)
	{
		if (field_178041_q)
		{
			mouseX = mouseXIn;
			mouseY = mouseYIn;

			drawBackground();

			final int i = getScrollBarX();
			final int j = i + 6;

			bindAmountScrolled();
			GlStateManager.disableLighting();
			GlStateManager.disableFog();

			final Tessellator tessellator = Tessellator.getInstance();
			final WorldRenderer worldrenderer = tessellator.getWorldRenderer();

			// noinspection CallToSimpleGetterFromWithinClass
			final int k = left + (width >> 1) - (getListWidth() >> 1) + 2;
			final int l = top + 4 - (int) amountScrolled;

			if (hasListHeader)
				drawListHeader(k, l, tessellator);

			RenderUtils.makeScissorBox(left, top, right, bottom);

			GL11.glEnable(GL11.GL_SCISSOR_TEST);

			drawSelectionBox(k, l + 2, mouseXIn, mouseYIn + 2);

			GL11.glDisable(GL11.GL_SCISSOR_TEST);

			GlStateManager.disableDepth();

			final int i1 = 4;

			// ClientCode
			final ScaledResolution scaledResolution = new ScaledResolution(mc);

			Gui.drawRect(0, 0, scaledResolution.getScaledWidth(), top, Integer.MIN_VALUE);
			Gui.drawRect(0, bottom, scaledResolution.getScaledWidth(), height, Integer.MIN_VALUE);

			GL11.glEnable(GL11.GL_BLEND);
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 1);
			GlStateManager.disableAlpha();
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			GlStateManager.disableTexture2D();

			worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldrenderer.pos(left, top + i1, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
			worldrenderer.pos(right, top + i1, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
			worldrenderer.pos(right, top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
			worldrenderer.pos(left, top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
			tessellator.draw();

			worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			worldrenderer.pos(left, bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			worldrenderer.pos(right, bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
			worldrenderer.pos(right, bottom - i1, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
			worldrenderer.pos(left, bottom - i1, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
			tessellator.draw();

			final int j1 = func_148135_f();

			if (j1 > 0)
			{
				int k1 = (bottom - top) * (bottom - top) / getContentHeight();
				k1 = MathHelper.clamp_int(k1, 32, bottom - top - 8);
				int l1 = (int) amountScrolled * (bottom - top - k1) / j1 + top;
				if (l1 < top)
					l1 = top;

				worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				worldrenderer.pos(i, bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				worldrenderer.pos(j, bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
				worldrenderer.pos(j, top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
				worldrenderer.pos(i, top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
				tessellator.draw();

				worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				worldrenderer.pos(i, l1 + k1, 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
				worldrenderer.pos(j, l1 + k1, 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
				worldrenderer.pos(j, l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
				worldrenderer.pos(i, l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
				tessellator.draw();

				worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
				worldrenderer.pos(i, l1 + k1 - 1, 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
				worldrenderer.pos(j - 1, l1 + k1 - 1, 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
				worldrenderer.pos(j - 1, l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
				worldrenderer.pos(i, l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
				tessellator.draw();
			}

			func_148142_b(mouseXIn, mouseYIn);
			GlStateManager.enableTexture2D();
			GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.enableAlpha();
			GlStateManager.disableBlend();
		}
	}

	/**
	 * @author CCBlueX
	 * @reason
	 */
	@Overwrite
	protected int getScrollBarX()
	{
		return width - 5;
	}

	@Override
	public void setEnableScissor(final boolean value)
	{
		enableScissor = value;
	}

	/**
	 * @author CCBlueX (superblaubeere27)
	 * @reason
	 */
	@Overwrite
	public int getListWidth()
	{
		return listWidth;
	}

	@Override
	public void setListWidth(final int listWidth)
	{
		this.listWidth = listWidth;
	}
}

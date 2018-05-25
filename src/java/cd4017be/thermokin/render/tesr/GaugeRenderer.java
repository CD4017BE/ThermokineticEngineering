package cd4017be.thermokin.render.tesr;

import org.lwjgl.opengl.GL11;

import cd4017be.api.heat.IHeatReservoir;
import cd4017be.lib.render.Util;
import cd4017be.thermokin.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

/**
 * @author CD4017BE
 *
 */
public class GaugeRenderer {

	public static final ResourceLocation TEMP_DIAL_TEX = new ResourceLocation(Main.ID, "textures/tesr/temp_dial.png");
	public static final ResourceLocation POINTER_TEX = new ResourceLocation(Main.ID, "textures/tesr/pointer.png");
	private static final int[]
			dialModel = Util.texturedRect(-0.5F, -0.5F, 0, 1.0F, 1.0F, 0, 1, 1, -1),
			pointerModel = Util.texturedRect(-0.046875F, -0.5F, 0.03125F, 0.09375F, 1.0F, 0, 1, 1.125F, -1);

	public static void renderGauge(float a, ResourceLocation dial, double x, double y, double z, double size) {
		TextureManager re = Minecraft.getMinecraft().renderEngine;
		VertexBuffer t = Tessellator.getInstance().getBuffer();
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(x, y, z);
		GlStateManager.scale(size, size, size);
		re.bindTexture(dial);
		t.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		t.addVertexData(dialModel);
		Tessellator.getInstance().draw();
		
		GlStateManager.rotate(a, 0F, 0F, -1F);
		re.bindTexture(POINTER_TEX);
		t.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		t.addVertexData(pointerModel);
		Tessellator.getInstance().draw();
		
		GlStateManager.popMatrix();
	}

	public static void renderTemp(IHeatReservoir heat, double x, double y, double z, double size) {
		renderGauge(heat.T() * 0.15F, GaugeRenderer.TEMP_DIAL_TEX, x, y, z, size);
	}

}

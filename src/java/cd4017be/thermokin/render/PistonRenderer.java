package cd4017be.thermokin.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import cd4017be.thermokin.tileentity.PneumaticPiston;
import cd4017be.thermokin.tileentity.Shaft;
import cd4017be.thermokin.multiblock.ShaftPhysics;
import cd4017be.lib.render.TESRModelParser;
import cd4017be.lib.util.Utils;

public class PistonRenderer extends TileEntitySpecialRenderer<PneumaticPiston> {

	private static final ResourceLocation texture = new ResourceLocation("thermokin:textures/tileentity/piston.png");
	public static final String model = "thermokin:models/tileentity/piston";
	private final float offset = -0.4375F, size = 0.625F;
	
	@Override
	public void renderTileEntityAt(PneumaticPiston te, double x, double y, double z, float t, int destroyStage) {
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.enableCull();
		if (Minecraft.isAmbientOcclusionEnabled()) GlStateManager.shadeModel(7425);
		else GlStateManager.shadeModel(7424);
		byte dir = te.getOrientation();
		TileEntity te2 = Utils.getTileOnSide(te, dir); 
		float f;
		if (te2 != null && te2 instanceof Shaft) {
			ShaftPhysics sp = ((Shaft)te2).physics();
			f = sp.s + t * 0.05F * sp.v;
			f -= Math.floor(f);
			if (f < 0.5F) {//parabolic approximated sin() function
				f -= 0.25F;
				f = offset + f * f * 8F * size;
			} else {
				f -= 0.75F;
				f = offset + (1F - f * f * 8F) * size;
			}
		} else f = offset;
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
		if (dir < 2) GlStateManager.rotate(90, dir == 0 ? -1 : 1, 0, 0);
		else if (dir > 2) GlStateManager.rotate(dir == 3 ? 180F : dir == 4 ? 90F : -90F, 0, 1, 0);
		this.bindTexture(texture);
		VertexBuffer render = Tessellator.getInstance().getBuffer();
		render.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		TESRModelParser.renderWithOffsetAndBrightness(render, model, 0, 0, -f, te.getWorld().getCombinedLight(te.getPos(), 0));
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
	}

}

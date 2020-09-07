package cd4017be.kineng.render;

import static net.minecraft.client.renderer.GlStateManager.*;
import java.util.function.Function;
import org.lwjgl.opengl.GL11;
import cd4017be.kineng.physics.*;
import cd4017be.kineng.tileentity.ShaftPart;
import cd4017be.lib.render.Util;
import cd4017be.lib.render.model.IntArrayModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import static java.lang.Float.floatToIntBits;

/**
 * @author CD4017BE
 *
 */
public class ShaftRenderer extends TileEntitySpecialRenderer<ShaftPart> implements Function<String, IntArrayModel> {

	public static boolean debug = true;

	@Override
	public void render(ShaftPart te, double x, double y, double z, float t, int destroyStage, float alpha) {
		ShaftAxis shaft = te.getShaft();
		if (shaft == null) return;
		ShaftRenderInfo info = shaft.renderInfo;
		if (Util.RenderFrame == info.lastFrame) return;
		info.draw(shaft, this);
		BlockPos pos = info.origin.subtract(te.pos());
		x += pos.getX();
		y += pos.getY();
		z += pos.getZ();
		pushMatrix();
		Util.moveAndOrientToBlock(x, y, z, te.getOrientation());
		rotate((float)Math.toDegrees(shaft.φ() + t * Ticking.Δt * shaft.ω()), 0, 0, -1);
		disableLighting();
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Tessellator.getInstance().draw();
		popMatrix();
		if (debug) drawDebug(shaft, x, y, z);
	}

	private void drawDebug(ShaftAxis shaft, double x, double y, double z) {
		BlockPos pos = shaft.renderInfo.origin;
		double d0 = rendererDispatcher.entity.getDistanceSqToCenter(pos);
		if (d0 > 1024D) return; 
		float f = this.rendererDispatcher.entityYaw;
		float f1 = this.rendererDispatcher.entityPitch;
		FontRenderer fr = getFontRenderer();
		Axis axis = shaft.parts.get(0).axis();
		int l = shaft.parts.size();
		float X = (float)x + 0.5F, Y = (float)y + 0.5F, Z = (float)z + 0.5F;
		disableDepth();
		disableTexture2D();
		glBegin(GL11.GL_LINES);
		int c = shaft.struct.hashCode();
		color((c & 0xff) / 255F, (c >> 8 & 0xff) / 255F, (c >> 16 & 0xff) / 255F, 1);
		switch(axis) {
		case X:
			glVertex3f(X - .25F, Y, Z);
			glVertex3f(X - .75F + l, Y, Z);
			break;
		case Y:
			glVertex3f(X, Y - .25F, Z);
			glVertex3f(X, Y - .75F + l, Z);
			break;
		default:
			glVertex3f(X, Y, Z - .25F);
			glVertex3f(X, Y, Z - .75F + l);
		}
		for (Connection con : shaft.cons)
			if (con instanceof GearLink) {
				GearLink s2s = (GearLink)con;
				if (s2s.other == null) continue;
				color(0, 1, 0, 1);
				BlockPos pos1 = ((TileEntity)con.host).getPos().subtract(pos);
				glVertex3f(pos1.getX() + X, pos1.getY() + Y, pos1.getZ() + Z);
				Vec3d dp = new Vec3d(((TileEntity)s2s.other.host).getPos().subtract(pos).subtract(pos1)).normalize().scale(Math.abs(s2s.r) * 0.95);
				glVertex3f(pos1.getX() + X + (float)dp.x, pos1.getY() + Y + (float)dp.y, pos1.getZ() + Z + (float)dp.z);
			} else if (con instanceof ForceCon && ((ForceCon)con).force != null) {
				color(0, 0, 1, 1);
				BlockPos pos1 = ((TileEntity)con.host).getPos().subtract(pos);
				glVertex3f(pos1.getX() + X, pos1.getY() + Y, pos1.getZ() + Z);
				glVertex3f(pos1.getX() + X + (float)con.r, pos1.getY() + Y + (float)con.r, pos1.getZ() + Z + (float)con.r);
			}
		glEnd();
		enableDepth();
		enableTexture2D();
		EntityRenderer.drawNameplate(fr, String.format("%.3g x%.3g", shaft.J, shaft.x), X, Y + 0.5F, Z, 0, f, f1, false, false);
		/*for (IShaftPart part : shaft.parts) {
			if (!(part instanceof ShaftPart)) continue;
			BlockPos pos1 = ((ShaftPart)part).getPos().subtract(pos);
			EntityRenderer.drawNameplate(fr, String.format("%.3g kg*m", part.mass()), X + pos1.getX(), Y + pos1.getY() + 0.5F, Z + pos1.getZ(), 0, f, f1, false, false);
		}*/
	}

	@Override
	public IntArrayModel apply(String t) {
		int p = t.indexOf(' ');
		if (p < 0) return null;
		TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(t.substring(p + 1));
		if (t.startsWith("shaft "))
			return new IntArrayModel(gear(0)).withTexture(tex);
		if (t.startsWith("cap"))
			return new IntArrayModel(cap(t.charAt(3) == '1')).withTexture(tex);
		if (t.startsWith("gear"))
			return new IntArrayModel(gear(Integer.parseInt(t.substring(4, p)))).withTexture(tex);
		return null;
	}

	static int[] cap(boolean end) {
		int[] m = new int[28 * 4];
		float r = 0.25F;
		int x0 = floatToIntBits(r), y0 = floatToIntBits(0), z = floatToIntBits(end ? 0.5F : -0.5F);
		double a = 0, da = (end ? .25 : -.25) * Math.PI;
		for (int i = 0, j = 0; i < m.length; i+=28, j++) {
			m[i+0] = m[i+1] = floatToIntBits(0);
			m[i+7] = x0;
			m[i+8] = y0;
			m[i+2] = m[i+9] = m[i+16] = m[i+23] = z;
			a += da;
			m[i+14] = x0 = floatToIntBits(r * (float)Math.cos(a));
			m[i+15] = y0 = floatToIntBits(r * (float)Math.sin(a));
			a += da;
			m[i+21] = x0 = floatToIntBits(r * (float)Math.cos(a));
			m[i+22] = y0 = floatToIntBits(r * (float)Math.sin(a));
			m[i+3] = m[i+10] = m[i+17] = m[i+24] = -1;
			m[i+4] = m[i+25] = floatToIntBits(0);
			m[i+11] = m[i+18] = floatToIntBits(16);
			m[i+5] = m[i+12] = floatToIntBits((j & 3) << 2);
			m[i+19] = m[i+26] = floatToIntBits((j & 3) + 1 << 2);
		}
		return m;
	}

	static int[] gear(int n) {
		int[] m = new int[28 * (8 + 4 * n)];
		//shaft:
		float r = 0.25F;
		int x0 = floatToIntBits(r), y0 = floatToIntBits(0);
		double a = 0, da = .25 * Math.PI;
		for (int i = 0, j = 0; i < m.length; i+=28, j++) {
			m[i+0] = m[i+7] = x0;
			m[i+1] = m[i+8] = y0;
			m[i+2] = m[i+23] = floatToIntBits(0.5F);
			m[i+9] = m[i+16] = floatToIntBits(-0.5F);
			a += da;
			m[i+14] = m[i+21] = x0 = floatToIntBits(r * (float)Math.cos(a));
			m[i+15] = m[i+22] = y0 = floatToIntBits(r * (float)Math.sin(a));
			m[i+4] = m[i+25] = floatToIntBits(0);
			m[i+11] = m[i+18] = floatToIntBits(16);
			m[i+5] = m[i+12] = floatToIntBits((j & 3) << 2);
			m[i+19] = m[i+26] = floatToIntBits((j & 3) + 1 << 2);
		}
		//gear:
		float r0 = (n - 2) / 16F, r1 = (n + 2) / 16F;
		da = Math.PI / n; a = da * 0.5;
		x0 = floatToIntBits(r0 * (float)Math.cos(a));
		y0 = floatToIntBits(r0 * (float)Math.sin(a));
		int z0 = floatToIntBits(-0.125F), z1 = floatToIntBits(0.125F);
		for (int i = 28 * 8; i < m.length;) {
			m[i+2] = m[i+9] = m[i+16] = m[i+23] = m[i+58] = m[i+65] = m[i+86] = m[i+93] = z0;
			m[i+30] = m[i+37] = m[i+44] = m[i+51] = m[i+72] = m[i+79] = m[i+100] = m[i+107] = z1;
			m[i+7] = m[i+49] = m[i+8] = m[i+50] = floatToIntBits(0);
			m[i+0] = m[i+28] = m[i+56] = m[i+77] = x0;
			m[i+1] = m[i+29] = m[i+57] = m[i+78] = y0;
			a += da;
			m[i+21] = m[i+35] = m[i+63] = m[i+70] = m[i+84] = m[i+105] = floatToIntBits(r1 * (float)Math.cos(a));
			m[i+22] = m[i+36] = m[i+64] = m[i+71] = m[i+85] = m[i+106] = floatToIntBits(r1 * (float)Math.sin(a));
			a += da;
			m[i+14] = m[i+42] = m[i+91] = m[i+98] = x0 = floatToIntBits(r0 * (float)Math.cos(a));
			m[i+15] = m[i+43] = m[i+92] = m[i+99] = y0 = floatToIntBits(r0 * (float)Math.sin(a));
			for (int j = 4; j > 0; j--, i+=28) {
				m[i+4] = m[i+25] = floatToIntBits(0);
				m[i+11] = m[i+18] = floatToIntBits(16);
				m[i+5] = m[i+12] = floatToIntBits(0);
				m[i+19] = m[i+26] = floatToIntBits(16);
			}
		}
		for (int i = 3; i < m.length; i+=7) m[i] = -1;
		return m;
	}

}

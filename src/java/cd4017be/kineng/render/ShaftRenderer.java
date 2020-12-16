package cd4017be.kineng.render;

import static cd4017be.lib.util.TooltipUtil.formatNumber;
import static net.minecraft.client.renderer.GlStateManager.*;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import cd4017be.kineng.physics.*;
import cd4017be.kineng.tileentity.IGear;
import cd4017be.kineng.tileentity.ShaftPart;
import cd4017be.lib.render.Util;
import cd4017be.lib.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * @author CD4017BE
 *
 */
public class ShaftRenderer<T extends ShaftPart> extends TileEntitySpecialRenderer<T> {

	@Override
	public void render(T te, double x, double y, double z, float t, int destroyStage, float alpha) {
		ShaftAxis shaft = te.getShaft();
		if (shaft == null) return;
		ShaftRenderInfo info = shaft.renderInfo;
		if (Util.RenderFrame == info.lastFrame) return;
		Tessellator tess = Tessellator.getInstance();
		try (QuadBuilder qb = QuadBuilder.INSTANCE.init(tess.getBuffer(), false)) {
			info.draw(shaft, qb);
		}
		BlockPos pos = info.origin.subtract(te.pos());
		x += pos.getX();
		y += pos.getY();
		z += pos.getZ();
		pushMatrix();
		Orientation o = te.getOrientation();
		Util.moveAndOrientToBlock(x, y, z, o);
		pushMatrix();
		rotate((float)Math.toDegrees(shaft.ang() + t * Ticking.dt * shaft.av()), 0, 0, -1);
		disableLighting();
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		tess.draw();
		popMatrix();
		if (Minecraft.getMinecraft().gameSettings.showDebugInfo)
			drawDebug(shaft, te.axis(), o, t);
		popMatrix();
	}

	private void drawDebug(ShaftAxis shaft, Axis axis, Orientation o, float t) {
		BlockPos pos = shaft.renderInfo.origin;
		double d0 = rendererDispatcher.entity.getDistanceSqToCenter(pos);
		if (d0 > 1024D) return;
		Vec3d look = o.invRotate(rendererDispatcher.entity.getLook(t));
		FontRenderer fr = getFontRenderer();
		int l = shaft.parts.size();
		disableDepth();
		BufferBuilder vb = Tessellator.getInstance().getBuffer();
		vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		int c = shaft.struct.hashCode();
		double y = axis == Axis.Y ? -1 : 1, x = axis == Axis.X ? -1 : 1;
		Vec3d[] dirs = {
			new Vec3d(0, -y, 0),
			new Vec3d(0, y, 0),
			new Vec3d(-x, 0, 0),
			new Vec3d(x, 0, 0),
		};
		vert(vb, c, 0, 0, -.25);
		vert(vb, c, 0, 0, l - .75);
		pushMatrix();
		translate(0, 0, l * 0.5F - 0.5F);
		GL11.glMultMatrix(Util.matrices[9]);
		y = 1.0 / 64.0; x = Math.copySign(y, look.x);
		scale(x, y, x);
		print(fr, "J= " + formatNumber(shaft.J * 1000D, 3, 1, false, false) + "gm²", 0, -9, 0xffffffff);
		print(fr, String.format("x= %.3g", shaft.x), 0, 0, 0xffffffff);
		print(fr, "\u03C9= " + formatNumber(shaft.av(), 3, 1, true, false) + "r/s", 0, 9, 0xffffffff);
		popMatrix();
		IShaftPart host = null;
		int i = 0;
		for (Connection con : shaft.cons) {
			if (con.host != host) {
				host = con.host;
				i = 0;
			} else i++;
			BlockPos pos1 = ((TileEntity)host).getPos();
			int j = Utils.coord(pos1.subtract(pos), axis);
			Vec3d dp;
			if (con instanceof GearLink) {
				GearLink s2s = (GearLink)con;
				if (s2s.other == null) continue;
				c = 0xff00ffff;
				dp = o.invRotate(new Vec3d(
					((TileEntity)s2s.other.host).getPos().subtract(pos1)
				)).normalize();
			} else {
				c = 0xff0000ff;
				dp = dirs[i&3];
			}
			pushMatrix();
			translate(0, 0, j);
			double r = Math.abs(con.r);
			float s = (float)r / 64F, s1 = Math.copySign(s, -(float)look.z);
			GL11.glMultMatrix((FloatBuffer)BufferUtils.createFloatBuffer(16).put(new float[] {
				(float)dp.x * s1, (float)dp.y * s1, 0, 0,
				(float)dp.y * s, -(float)dp.x * s, 0, 0,
				0, 0, s1, 0,
				(float)(dp.x * r * 0.5), (float)(dp.y * r * 0.5), 0, 1
			}).flip());
			dp = dp.scale(Math.abs(con.r) * 0.95);
			vert(vb, c, 0, 0, j);
			vert(vb, c, dp.x, dp.y, dp.z + j);
			print(fr, String.format("r= %.1fm", r), 0, -6, c);
			float M = (float)(con.M / (con.maxF * Math.abs(shaft.x * con.r)));
			Vec3d dd = new Vec3d(dp.y, -dp.x, dp.z).scale(M / r).add(dp);
			M = Math.abs(M) * 255F;
			c = (int)M << 16 | 255 - (int)M << 8;
			vert(vb, c, dp.x, dp.y, dp.z + j);
			vert(vb, c, dd.x, dd.y, dd.z + j);
			print(fr, "F= " + formatNumber(con.M / (shaft.x * r), 3, 1, true, false) + "N", 0, 6, c);
			popMatrix();
		}
		disableTexture2D();
		glLineWidth(2F);
		Tessellator.getInstance().draw();
		glLineWidth(1F);
		enableTexture2D();
		enableDepth();
	}

	private static void vert(BufferBuilder vb, int c, double x, double y, double z) {
		vb.pos(x, y, z).color(c >> 16 & 0xff, c >> 8 & 0xff, c & 0xff, 0xff).endVertex();
	}

	private static void print(FontRenderer fr, String s, int x, int y, int c) {
		fr.drawString(s, x - (fr.getStringWidth(s) >> 1), y - (fr.FONT_HEIGHT >> 1), c);
	}

	@Override
	public boolean isGlobalRenderer(T te) {
		return te instanceof IGear || te.block().radius(te.getBlockState()) > 0.5;
	}

}

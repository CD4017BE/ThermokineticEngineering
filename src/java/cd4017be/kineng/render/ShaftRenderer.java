package cd4017be.kineng.render;

import static net.minecraft.client.renderer.GlStateManager.*;
import org.lwjgl.opengl.GL11;
import cd4017be.kineng.physics.*;
import cd4017be.kineng.tileentity.ShaftPart;
import cd4017be.lib.render.Util;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * @author CD4017BE
 *
 */
public class ShaftRenderer extends TileEntitySpecialRenderer<ShaftPart> {

	public static boolean debug = true, motionBlur = true, global = true;

	@Override
	public void render(ShaftPart te, double x, double y, double z, float t, int destroyStage, float alpha) {
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
		Util.moveAndOrientToBlock(x, y, z, te.getOrientation());
		if (motionBlur) t += Util.FakeMotionBlur;
		rotate((float)Math.toDegrees(shaft.ang() + t * Ticking.dt * shaft.av()), 0, 0, -1);
		disableLighting();
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		tess.draw();
		popMatrix();
		if (debug) drawDebug(shaft, te.axis(), x, y, z);
	}

	private void drawDebug(ShaftAxis shaft, Axis axis, double x, double y, double z) {
		BlockPos pos = shaft.renderInfo.origin;
		double d0 = rendererDispatcher.entity.getDistanceSqToCenter(pos);
		if (d0 > 1024D) return; 
		float f = this.rendererDispatcher.entityYaw;
		float f1 = this.rendererDispatcher.entityPitch;
		FontRenderer fr = getFontRenderer();
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
	public boolean isGlobalRenderer(ShaftPart te) {
		return global && te.block().radius(te.getBlockState()) > 0.5;
	}

}

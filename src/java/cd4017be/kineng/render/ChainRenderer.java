package cd4017be.kineng.render;

import static cd4017be.kineng.physics.Ticking.dt;
import static cd4017be.kineng.render.QuadBuilder.*;
import static cd4017be.math.cplx.CplxF.C_;
import static net.minecraft.client.renderer.GlStateManager.*;
import java.nio.ByteBuffer;
import cd4017be.kineng.Main;
import cd4017be.kineng.physics.GearLink;
import cd4017be.kineng.tileentity.Gear;
import cd4017be.lib.render.Util;
import cd4017be.math.cplx.CplxF;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;

/** 
 * @author CD4017BE */
public class ChainRenderer extends ShaftRenderer<Gear> {

	static final ResourceLocation CHAIN_TEX = new ResourceLocation(Main.ID, "textures/tesr/chain.png");
	static final float T_WIDTH = 0.5F;

	@Override
	public void render(Gear te, double x, double y, double z, float t, int destroyStage, float alpha) {
		super.render(te, x, y, z, t, destroyStage, alpha);
		if (te.chainLink() == null) {
			te.model = null;
			return;
		}
		GearLink con = te.getCon(null);
		if (con.other == null) return;
		color(1, 1, 1, 1);
		disableLighting();
		Tessellator tess = Tessellator.getInstance();
		try (QuadBuilder qb = QuadBuilder.INSTANCE.init(tess.getBuffer(), false)) {
			double r = Math.abs(con.r);
			//put/render model
			if (te.model != null) qb.vb.addVertexData(te.model);
			else {
				Vec3d d = te.getOrientation().invRotate(new Vec3d(te.chainLink().subtract(te.getPos())));
				drawChainLink(
					qb, (float)r, (float)Math.abs(con.other.r),
					C_((float)d.x, (float)d.y), te.chainStack
				);
				te.model = Util.extractData(qb.vb, 0, qb.vb.getVertexCount());
			}
			//set light and slide texture
			float ofs = (float)((con.axis.ang() + con.axis.av() * (double)t * dt) * r * 8.0 / Math.PI);
			ByteBuffer b = qb.vb.getByteBuffer();
			int l = te.getWorld().getCombinedLight(te.getPos(), 0);
			int n = qb.vb.getVertexCount() * 7;
			for (int i = 0; i < n; i+=7) {
				int j = i+EV << 2;
				b.putFloat(j, b.getFloat(j) + ofs);
				b.putInt(i+ELN << 2, l);
			}
			l = te.getWorld().getCombinedLight(te.chainLink(), 0);
			b.putInt(n-7+ELN << 2, l);
			b.putInt(n-14+ELN << 2, l);
			b.putInt(n-35+ELN << 2, l);
			b.putInt(n-42+ELN << 2, l);
		}
		pushMatrix();
		Util.moveAndOrientToBlock(x, y, z, te.getOrientation());
		bindTexture(CHAIN_TEX);
		tess.draw();
		popMatrix();
	}

	private void drawChainLink(QuadBuilder qb, float r0, float r1, CplxF d, ItemStack stack) {
		float u = stack.getMetadata() * T_WIDTH;
		int n = (int)(r0 * 8F);
		double a = Math.atan2(d.i, d.r) / Math.PI;
		float t0 = (float)(n * a);
		qb.uv(u, t0, u + T_WIDTH, t0 + 1F).all(EC, -1);
		CplxF rot = CplxF.C_pol(1, Math.PI / (double)n);
		CplxF v0 = CplxF.C_i(r0 / (float)Math.sqrt(d.asqF())).mul(d);
		CplxF v1 = new CplxF();
		for (int i = 0; i < n; i++) {
			v1.prod(v0, rot);
			qb.xyz(v0.r, v0.i, -.25F).next();
			qb.xyz(v0.r, v0.i, .25F).next();
			qb.xyz(v1.r, v1.i, .25F).next();
			qb.xyz(v1.r, v1.i, -.25F).next();
			qb.xyz(v0.r, v0.i, .25F).next();
			qb.xyz(v0.r, v0.i, -.25F).next();
			qb.xyz(v1.r, v1.i, -.25F).next();
			qb.xyz(v1.r, v1.i, .25F).next();
			v0.set(v1);
		}
		v1.sca(v0, r1 / r0).add(d);
		float t1 = (float)((int)(r1 * 8F) * a);
		t1 += (float)Math.rint(t0 - t1 + 8.0 / Math.PI * Math.sqrt(d.dif(v1, v0).asqF()));
		qb.uv(u, t0, u + T_WIDTH, t1);
		qb.xyz(v0.r, v0.i, -.25F).next();
		qb.xyz(v0.r, v0.i, .25F).next();
		qb.xyz(v1.r, v1.i, .25F).next();
		qb.xyz(v1.r, v1.i, -.25F).next();
		qb.xyz(v0.r, v0.i, .25F).next();
		qb.xyz(v0.r, v0.i, -.25F).next();
		qb.xyz(v1.r, v1.i, -.25F).next();
		qb.xyz(v1.r, v1.i, .25F).next();
	}

}

package cd4017be.kineng.render;

import static cd4017be.kineng.render.IPartModel.registerModel;
import static cd4017be.kineng.render.IPartModel.texture;
import static cd4017be.kineng.render.QuadBuilder.*;
import static cd4017be.math.cplx.CplxF.C_;
import static cd4017be.math.cplx.CplxF.C_pol;
import cd4017be.math.cplx.CplxF;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 
 * @author CD4017BE */
@SideOnly(Side.CLIENT)
public class PartModels {

	/** complex number describing a 45° rotation
	 * (much faster than using sin + cos) */
	private static final CplxF ROTATE45 = C_pol(1.0, Math.PI / 4.0);
	private static final CplxF ROTATE_32 = C_pol(1.0, Math.PI / 16.0);

	private static final IPartModel shaft_caps = (qb, args, z) -> {
		texture(qb, args[1]).all(EC, -1);
		z += Math.copySign(0.5F, args[2]);
		CplxF xy = C_(args[2] * 0.0625F);
		CplxF rot = args[2] > 0 ? ROTATE45 : ROTATE45.clone().conj();
		for (int i = 0; i < 4; i++) {
			int v = i << 2;
			qb.xyz(0   , 0   , z).uv(0, v).next();
			qb.xyz(xy.r, xy.i, z).uv(16, v).next();
			xy.mul(rot);
			qb.xyz(xy.r, xy.i, z).uv(16, v + 4).next();
			xy.mul(rot);
			qb.xyz(xy.r, xy.i, z).uv(0, v + 4).next();
		}
	};
	/** model arguments: [id, texture, radius*16]
	 * <br> radius < 0: -z cap, radius > 0: +z cap */
	public static final int SHAFT_CAPS = registerModel(shaft_caps);

	private static final IPartModel shaft = (qb, args, z0) -> {
		texture(qb, args[1]).all(EC, -1);
		float z1 = z0 + 0.5F; z0 -= 0.5F;
		CplxF xy = C_(args[2] * 0.0625F);
		for (int i = 0; i < 8; i++) {
			int v = (i & 3) << 2;
			qb.xyz(xy.r, xy.i, z1).uv(0, v).next();
			qb.xyz(xy.r, xy.i, z0).uv(16, v).next();
			xy.mul(ROTATE45);
			qb.xyz(xy.r, xy.i, z0).uv(16, v + 4).next();
			qb.xyz(xy.r, xy.i, z1).uv(0, v + 4).next();
		}
	};
	/** model arguments: [id, texture, radius*16] */
	public static final int SHAFT = registerModel(shaft);

	/** model arguments: [id, texture, shaft_radius*16, #teeth] */
	public static final int GEAR = registerModel((qb, args, z0) -> {
		shaft.render(qb, args, z0);
		qb.uv(0, 0, 16, 16);
		int n = args[3];
		float z1 = z0 + 0.125F; z0 -= 0.125F;
		CplxF rot = C_pol(1.0, Math.PI / (n<<1));
		CplxF xy0 = rot.clone().sca((n - 2) * 0.0625F);
		CplxF xy1 = rot.clone().sca((n + 2) * 0.0625F);
		CplxF xy2 = new CplxF();
		xy1.mul(rot.mul(rot));
		rot.mul(rot);
		for (int i = 0; i < n; i++) {
			xy2.prod(xy0, rot);
			qb.xyz(xy0.r, xy0.i, z0).next();
			qb.xyz(0    , 0    , z0).next();
			qb.xyz(xy2.r, xy2.i, z0).next();
			qb.xyz(xy1.r, xy1.i, z0).next();
			qb.xyz(xy0.r, xy0.i, z1).next();
			qb.xyz(xy1.r, xy1.i, z1).next();
			qb.xyz(xy2.r, xy2.i, z1).next();
			qb.xyz(0    , 0    , z1).next();
			qb.xyz(xy0.r, xy0.i, z0).next();
			qb.xyz(xy1.r, xy1.i, z0).next();
			qb.xyz(xy1.r, xy1.i, z1).next();
			qb.xyz(xy0.r, xy0.i, z1).next();
			qb.xyz(xy1.r, xy1.i, z0).next();
			qb.xyz(xy2.r, xy2.i, z0).next();
			qb.xyz(xy2.r, xy2.i, z1).next();
			qb.xyz(xy1.r, xy1.i, z1).next();
			xy1.mul(rot);
			xy0.set(xy2);
		}
	});

	/** model arguments: [id, shaft_texture, shaft_radius*16, wheel_texture, wheel_radius*16, wheel_width*16, outer_texture] */
	public static final int WHEEL = registerModel((qb, args, z0) -> {
		shaft.render(qb, args, z0);
		int n = args[4], t0 = args[3], t1 = args[6];
		float z1 = z0 + args[5] * 0.03125F; z0 -= args[5] * 0.03125F;
		CplxF rot = C_pol(1.0, 2.0 * Math.PI / (n & -2));
		CplxF xy0 = C_(n * 0.0625F);
		CplxF xy1 = new CplxF().prod(xy0, rot);
		CplxF xy2 = new CplxF();
		rot.mul(rot);
		n &= -2;
		for (int i = 0; i < n; i++) {
			xy2.prod(xy0, rot);
			texture(qb, t0).uv(0, 0, 16, 16);
			qb.xyz(0    , 0    , z0).next();
			qb.xyz(xy2.r, xy2.i, z0).next();
			qb.xyz(xy1.r, xy1.i, z0).next();
			qb.xyz(xy0.r, xy0.i, z0).next();
			qb.xyz(0    , 0    , z1).next();
			qb.xyz(xy0.r, xy0.i, z1).next();
			qb.xyz(xy1.r, xy1.i, z1).next();
			qb.xyz(xy2.r, xy2.i, z1).next();
			texture(qb, t1).uv(0, 0, 16, 16);
			qb.xyz(xy0.r, xy0.i, z0).next();
			qb.xyz(xy1.r, xy1.i, z0).next();
			qb.xyz(xy1.r, xy1.i, z1).next();
			qb.xyz(xy0.r, xy0.i, z1).next();
			qb.uv(16, 0, 0, 16);
			qb.xyz(xy1.r, xy1.i, z0).next();
			qb.xyz(xy2.r, xy2.i, z0).next();
			qb.xyz(xy2.r, xy2.i, z1).next();
			qb.xyz(xy1.r, xy1.i, z1).next();
			xy1.mul(rot);
			xy0.set(xy2);
		}
	});

	/** model arguments: [id, shaft_texture, shaft_radius*16, handle_radius*16 */
	public static final int MANUAL = registerModel((qb, args, z0) -> {
		shaft.render(qb, args, z0);
		float r0 = args[2] * 0.0625F, r1 = args[3] * 0.0625F;
		float d = 0.415F * r0, z1 = z0 + d; z0 -= d;
		CplxF xy = C_(1F).add(ROTATE45).sca(0.5F);
		CplxF pl0 = C_(r0, d), pl1 = C_(r1, d);
		CplxF pr0 = new CplxF().prod(pl0, xy);
		CplxF pr1 = new CplxF().prod(pl1, xy);
		pl0.conj().mul(xy);
		pl1.conj().mul(xy);
		for (int i = 0; i < 8; i++) {
			qb.xyz(pl1.r, pl1.i, z1).uv(0, 0).next();
			qb.xyz(pl1.r, pl1.i, z0).uv(0, 4).next();
			qb.xyz(pr1.r, pr1.i, z0).uv(4, 4).next();
			qb.xyz(pr1.r, pr1.i, z1).uv(4, 0).next();
			
			qb.xyz(pl0.r, pl0.i, z1).uv(0, 0).next();
			qb.xyz(pl0.r, pl0.i, z0).uv(0, 4).next();
			qb.xyz(pl1.r, pl1.i, z0).uv(16, 4).next();
			qb.xyz(pl1.r, pl1.i, z1).uv(16, 0).next();
			
			qb.xyz(pr0.r, pr0.i, z1).uv(0, 4).next();
			qb.xyz(pl0.r, pl0.i, z1).uv(0, 8).next();
			qb.xyz(pl1.r, pl1.i, z1).uv(16, 8).next();
			qb.xyz(pr1.r, pr1.i, z1).uv(16, 4).next();
			
			qb.xyz(pr0.r, pr0.i, z0).uv(0, 8).next();
			qb.xyz(pr0.r, pr0.i, z1).uv(0, 12).next();
			qb.xyz(pr1.r, pr1.i, z1).uv(16, 12).next();
			qb.xyz(pr1.r, pr1.i, z0).uv(16, 8).next();
			
			qb.xyz(pl0.r, pl0.i, z0).uv(0, 12).next();
			qb.xyz(pr0.r, pr0.i, z0).uv(0, 16).next();
			qb.xyz(pr1.r, pr1.i, z0).uv(16, 16).next();
			qb.xyz(pl1.r, pl1.i, z0).uv(16, 12).next();
			
			pl0.mul(ROTATE45);
			pr0.mul(ROTATE45);
			pl1.mul(ROTATE45);
			pr1.mul(ROTATE45);
		}
	});

	/** model arguments: [id, shaft_texture, shaft_radius*16, handle_radius*16 */
	public static final int WATER_WHEEL = registerModel((qb, args, z0) -> {
		texture(qb, args[1]);
		float r0 = args[2] * 0.0625F, r1 = args[4] * 0.0625F;
		float d = 0.415F * r0, z1 = z0 + 0.5F; z0 -= 0.5F;
		CplxF xy = C_(1F).add(ROTATE45).sca(0.5F);
		CplxF pl0 = C_(r0, d), pl1 = C_(r1, d);
		CplxF pr0 = new CplxF().prod(pl0, xy);
		CplxF pr1 = new CplxF().prod(pl1, xy);
		pl0.conj().mul(xy);
		pl1.conj().mul(xy);
		r0 = (args[3] * 0.0625F - d) / r1;
		r1 = (args[3] * 0.0625F + d) / r1;
		for (int i = 0; i < 8; i++) {
			qb.uv(0, 0, 16, 4);
			qb.xyz(pl1.r, pl1.i, z1).next();
			qb.xyz(pl1.r, pl1.i, z0).next();
			qb.xyz(pr1.r, pr1.i, z0).next();
			qb.xyz(pr1.r, pr1.i, z1).next();
			qb.uv(0, 0, 16, 16);
			qb.xyz(pl1.r, pl1.i, z1).next();
			qb.xyz(pl0.r, pl0.i, z1).next();
			qb.xyz(pl0.r, pl0.i, z0).next();
			qb.xyz(pl1.r, pl1.i, z0).next();
			qb.xyz(pr0.r, pr0.i, z1).next();
			qb.xyz(pr1.r, pr1.i, z1).next();
			qb.xyz(pr1.r, pr1.i, z0).next();
			qb.xyz(pr0.r, pr0.i, z0).next();
			qb.uv(0, 0, 16, 4);
			qb.xyz(pl0.r, pl0.i, z1).next();
			qb.xyz(pl1.r, pl1.i, z1).next();
			qb.xyz(pr1.r, pr1.i, z1).next();
			qb.xyz(pr0.r, pr0.i, z1).next();
			qb.xyz(pl1.r, pl1.i, z0).next();
			qb.xyz(pl0.r, pl0.i, z0).next();
			qb.xyz(pr0.r, pr0.i, z0).next();
			qb.xyz(pr1.r, pr1.i, z0).next();
			pl0.mul(ROTATE45);
			pl1.mul(ROTATE45);
			qb.uv(0, 0, 16, 16);
			qb.xyz(pr1.r * r0, pr1.i * r0, z1).next();
			qb.xyz(pl1.r * r0, pl1.i * r0, z1).next();
			qb.xyz(pl1.r * r0, pl1.i * r0, z0).next();
			qb.xyz(pr1.r * r0, pr1.i * r0, z0).next();
			qb.xyz(pl1.r * r1, pl1.i * r1, z1).next();
			qb.xyz(pr1.r * r1, pr1.i * r1, z1).next();
			qb.xyz(pr1.r * r1, pr1.i * r1, z0).next();
			qb.xyz(pl1.r * r1, pl1.i * r1, z0).next();
			qb.uv(0, 0, 16, 4);
			qb.xyz(pl1.r * r1, pl1.i * r1, z0).next();
			qb.xyz(pr1.r * r1, pr1.i * r1, z0).next();
			qb.xyz(pr1.r * r0, pr1.i * r0, z0).next();
			qb.xyz(pl1.r * r0, pl1.i * r0, z0).next();
			qb.xyz(pr1.r * r1, pr1.i * r1, z1).next();
			qb.xyz(pl1.r * r1, pl1.i * r1, z1).next();
			qb.xyz(pl1.r * r0, pl1.i * r0, z1).next();
			qb.xyz(pr1.r * r0, pr1.i * r0, z1).next();
			pr0.mul(ROTATE45);
			pr1.mul(ROTATE45);
		}
	});

	/** model arguments: [id, shaft_texture, shaft_radius*16, blade_radius*16, blade_texture, blade_width*16] */
	public static final int TURBINE = registerModel((qb, args, z0) -> {
		args[2] = -args[3] / 10;
		shaft_caps.render(qb, args, z0);
		args[2] = -args[2];
		shaft_caps.render(qb, args, z0);
		shaft.render(qb, args, z0);
		texture(qb, args[4]).uv(0, 0, 16, 16);
		float z1 = z0 + args[5] * 0.03125F; z0 -= args[5] * 0.03125F;
		CplxF xy0 = ROTATE_32.clone();
		CplxF xy1 = xy0.clone().conj().mul(ROTATE45);
		float r0 = args[2] * 0.0625F, r1 = args[3] * 0.0625F;
		for (int i = 0; i < 8; i++) {
			for (float r = r1; r > r0;) {
				float r_ = r * 0.5F;
				qb.xyz(xy0.r * r, xy0.i * r, z0).next();
				qb.xyz(xy0.r * r_, xy0.i * r_, z0).next();
				qb.xyz(xy1.r * r_, xy1.i * r_, z1).next();
				qb.xyz(xy1.r * r, xy1.i * r, z1).next();
				qb.xyz(xy1.r * r_, xy1.i * r_, z1).next();
				qb.xyz(xy0.r * r_, xy0.i * r_, z0).next();
				qb.xyz(xy0.r * r, xy0.i * r, z0).next();
				qb.xyz(xy1.r * r, xy1.i * r, z1).next();
				r = r_;
			}
			xy0.mul(ROTATE45);
			xy1.mul(ROTATE45);
		}
	});

	/** model arguments: [id, shaft_texture, shaft_radius*16, blade_radius*16, blade_texture, blade_width*16] */
	public static final int BLADES = registerModel((qb, args, z) -> {
		shaft.render(qb, args, z);
		texture(qb, args[4]);
		float r0 = args[2] * 0.0625F, r1 = args[3] * 0.0625F;
		float d = args[5] * 0.03125F, z0 = z - d, z1 = z + d;
		CplxF pc0 = C_(1F).add(ROTATE45).sca(0.5F), pc1 = new CplxF();
		CplxF pl0 = C_(r0, r0), pl1 = C_(r1, r0);
		CplxF pr0 = new CplxF().prod(pl0, pc0);
		CplxF pr1 = new CplxF().prod(pl1, pc0);
		pl0.conj().mul(pc0);
		pl1.conj().mul(pc0);
		pc1.sca(pc0, r1 * 1.125F); pc0.sca(r0);
		for (int i = 0; i < 8; i++) {
			qb.xyz(pc0.r, pc0.i, z0).uv( 0, 4).next();
			qb.xyz(pc1.r, pc1.i, z ).uv(16, 4).next();
			qb.xyz(pl1.r, pl1.i, z ).uv(16, 0).next();
			qb.xyz(pl0.r, pl0.i, z ).uv( 0, 0).next();
			
			qb.xyz(pc0.r, pc0.i, z1).uv( 0, 4).next();
			qb.xyz(pl0.r, pl0.i, z ).uv( 0, 8).next();
			qb.xyz(pl1.r, pl1.i, z ).uv(16, 8).next();
			qb.xyz(pc1.r, pc1.i, z ).uv(16, 4).next();
			
			qb.xyz(pc0.r, pc0.i, z1).uv( 0,12).next();
			qb.xyz(pc1.r, pc1.i, z ).uv(16,12).next();
			qb.xyz(pr1.r, pr1.i, z ).uv(16, 8).next();
			qb.xyz(pr0.r, pr0.i, z ).uv( 0, 8).next();
			
			qb.xyz(pc0.r, pc0.i, z0).uv( 0,12).next();
			qb.xyz(pr0.r, pr0.i, z ).uv( 0,16).next();
			qb.xyz(pr1.r, pr1.i, z ).uv(16,16).next();
			qb.xyz(pc1.r, pc1.i, z ).uv(16,12).next();
			
			pl0.mul(ROTATE45);
			pc0.mul(ROTATE45);
			pr0.mul(ROTATE45);
			pl1.mul(ROTATE45);
			pc1.mul(ROTATE45);
			pr1.mul(ROTATE45);
		}
	});

}

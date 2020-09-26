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

	/** model arguments: [id, texture, radius*16]
	 * <br> radius < 0: -z cap, radius > 0: +z cap */
	public static final int SHAFT_CAPS = registerModel((qb, args, z) -> {
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
	});

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

}

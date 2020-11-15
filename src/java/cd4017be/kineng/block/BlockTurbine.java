package cd4017be.kineng.block;

import static cd4017be.kineng.physics.Formula.torsionStrength_circle;
import java.util.Arrays;
import java.util.List;
import cd4017be.kineng.physics.Formula;
import cd4017be.lib.util.TooltipUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/** 
 * @author CD4017BE */
public class BlockTurbine extends BlockGear {

	public ShaftMaterial[] mat = new ShaftMaterial[6];
	public ItemStack[] scrap = new ItemStack[6];
	public double[] maxM = new double[6];
	public double[] maxF = new double[6];
	public int[] bladeTex;
	
	/**
	 * @param id
	 * @param m
	 * @param r
	 * @param tile
	 */
	public BlockTurbine(String id, ShaftMaterial m, double r, Class<? extends TileEntity> tile) {
		super(id, m, r, tile);
		Arrays.fill(mat, m);
	}

	public void setMaterial(int d, ShaftMaterial shaft, ShaftMaterial blade, double h, double str, double maxF) {
		mat[d] = shaft;
		scrap[d] = blade.scrap;
		maxM[d] = torsionStrength_circle(0.25) * shaft.strength;
		J_dens[d] = Formula.J_biCylinder(0.25, shaft.density, r * d, blade.density, h);
		if (av_max == null) av_max = new double[6];
		av_max[d] = Math.min(
			Formula.rip_vel(shaftMat.strength / shaftMat.density, 0.25),
			Formula.rip_vel(blade.strength / blade.density, r * d)
		) * str;
		this.maxF[d] = maxF;
	}

	@Override
	protected void addInformation(IBlockState state, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add(TooltipUtil.format("info.kineng.shaftstats", J(state) * 1000.0, maxM(state), maxAv(state)));
		tooltip.add(TooltipUtil.format("info.kineng.toolstats", radius(state), maxF[state.getValue(DIAMETER)]));
	}

	@Override
	public Material getMaterial(IBlockState state) {
		return mat[state.getValue(DIAMETER)].material;
	}

	@Override
	public double J(IBlockState state) {
		return J_dens[state.getValue(DIAMETER)];
	}

	@Override
	public double maxAv(IBlockState blockState) {
		if (av_max == null || Double.isNaN(av_max[1])) {
			av_max = new double[6];
			for (int i = 0; i < av_max.length; i++)
				av_max[i] = Math.sqrt(3.0 * mat[i].strength / mat[i].density) / (r * i);
		}
		return av_max[blockState.getValue(DIAMETER)];
	}

	@Override
	public int[] model(IBlockState state) {
		int d = state.getValue(DIAMETER);
		model[1] = mat[d].texture;
		model[3] = (int)(radius(state) * 16.0);
		model[4] = bladeTex[d];
		return model;
	}

	@Override
	public double maxM(IBlockState state) {
		return mat[state.getValue(DIAMETER)].strength * strength;
	}

}

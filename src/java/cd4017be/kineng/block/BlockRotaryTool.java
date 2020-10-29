package cd4017be.kineng.block;

import java.util.List;
import cd4017be.kineng.physics.Formula;
import cd4017be.lib.util.TooltipUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 
 * @author CD4017BE */
public class BlockRotaryTool extends BlockShaft {

	public double maxF;
	public final int type;
	public ItemStack scrap = ItemStack.EMPTY;

	/**
	 * @param id
	 * @param m
	 * @param r
	 * @param tile
	 */
	public BlockRotaryTool(String id, ShaftMaterial m, int type, double r, Class<? extends TileEntity> tile) {
		super(id, m, r, tile);
		this.type = type;
	}

	@Override
	public double J(IBlockState state) {
		return J_dens;
	}

	@Override
	public double getDebris(IBlockState state, List<ItemStack> items) {
		items.add(shaftMat.scrap);
		items.add(scrap);
		return r;
	}

	public void setMaterials(ShaftMaterial mat, double r, double h) {
		J_dens = Formula.J_biCylinder(0.25, shaftMat.density, r, mat.density, h);
		av_max = Math.min(
			Formula.rip_vel(shaftMat.strength / shaftMat.density, 0.25),
			Formula.rip_vel(mat.strength / mat.density, r)
		);
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void addInformation(IBlockState state, List<String> tooltip, ITooltipFlag advanced) {
		super.addInformation(state, tooltip, advanced);
		tooltip.add(TooltipUtil.format("info.kineng.toolstats", radius(state), maxF));
	}

}

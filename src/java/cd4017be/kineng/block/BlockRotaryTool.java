package cd4017be.kineng.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

/** 
 * @author CD4017BE */
public class BlockRotaryTool extends BlockShaft {

	public double maxF;
	public final int type;

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

}

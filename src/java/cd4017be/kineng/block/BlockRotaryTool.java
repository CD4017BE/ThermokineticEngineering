package cd4017be.kineng.block;

import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

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

}

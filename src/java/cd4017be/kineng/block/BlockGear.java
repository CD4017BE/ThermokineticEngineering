package cd4017be.kineng.block;

import static net.minecraft.block.BlockRotatedPillar.AXIS;
import static net.minecraft.util.EnumFacing.Axis.*;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/** @author CD4017BE */
public class BlockGear extends BlockShaft implements IFillBlockSrc {

	public static final PropertyInteger DIAMETER = PropertyInteger.create("diameter", 1, 5);

	public BlockGear(String id, ShaftMaterial m, Class<? extends TileEntity> tile) {
		super(id, m, 0.25, tile);
		setDefaultState(getDefaultState().withProperty(DIAMETER, 1));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AXIS, DIAMETER);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return super.getStateFromMeta(meta).withProperty(DIAMETER, meta / 3 + 1);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return super.getMetaFromState(state) + (state.getValue(DIAMETER) - 1) * 3;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(DIAMETER);
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		for(int i : DIAMETER.getAllowedValues())
			items.add(new ItemStack(this, 1, i));
	}

	@Override
	public boolean supportsFill(IBlockState state, EnumFacing side) {
		return true;
	}

	public static AxisAlignedBB gearSize(BlockPos pos, Axis ax, int d) {
		double r = d * 0.5 - 0.51, a = -0.01;
		return new AxisAlignedBB(pos).grow(ax == X ? a : r, ax == Y ? a : r, ax == Z ? a : r);
	}

}

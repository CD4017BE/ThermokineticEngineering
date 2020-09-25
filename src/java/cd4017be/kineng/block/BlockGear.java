package cd4017be.kineng.block;

import static cd4017be.kineng.physics.Formula.J_cylinder;
import static net.minecraft.block.BlockRotatedPillar.AXIS;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/** @author CD4017BE */
public class BlockGear extends BlockShaft {

	public static final PropertyInteger DIAMETER = PropertyInteger.create("diameter", 1, 5);

	private double[] J_dens = new double[6];

	public BlockGear(String id, ShaftMaterial m, Class<? extends TileEntity> tile) {
		super(id, m, 0.5, tile);
		setDefaultState(getDefaultState().withProperty(DIAMETER, 1));
		model = "gear";
	}

	@Override
	public BlockShaft setShape(double r0, double l) {
		for (int i = 0; i < J_dens.length; i++)
			J_dens[i] = J_cylinder(r0, 1.0 - l) + J_cylinder(i * r, l);
		return super.setShape(r0, l);
	}

	@Override
	public double J(IBlockState state) {
		return shaftMat.density * J_dens[state.getValue(DIAMETER)];
	}

	@Override
	public String model(IBlockState state) {
		return model + (state.getValue(DIAMETER) * 8) + ' ' + shaftMat.texture;
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
	public AxisAlignedBB getSize(BlockPos pos, IBlockState state) {
		return getSize(pos, state.getValue(AXIS), r * state.getValue(DIAMETER));
	}

	@Override
	public boolean supportsFill(IBlockState state, EnumFacing side) {
		return state.getValue(DIAMETER) > 1;
	}

}

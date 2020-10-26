package cd4017be.kineng.block;

import static cd4017be.kineng.physics.Formula.J_cylinder;
import static net.minecraft.block.BlockRotatedPillar.AXIS;
import java.util.List;
import cd4017be.kineng.tileentity.Gear;
import cd4017be.lib.util.TooltipUtil;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

/** @author CD4017BE */
public class BlockGear extends BlockShaft {

	public static final PropertyInteger DIAMETER = PropertyInteger.create("diameter", 1, 5);

	private double[] J_dens = new double[6];
	private double[] av_max;

	public BlockGear(String id, ShaftMaterial m, Class<? extends TileEntity> tile) {
		super(id, m, 0.5, tile);
		setDefaultState(getDefaultState().withProperty(DIAMETER, 1));
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
	public double maxAv(IBlockState blockState) {
		if (av_max == null || Double.isNaN(av_max[1])) {
			av_max = new double[6];
			for (int i = 0; i < av_max.length; i++)
				av_max[i] = Math.sqrt(3.0 * shaftMat.strength / shaftMat.density) / (r * i);
		}
		return av_max[blockState.getValue(DIAMETER)];
	}

	@Override
	public int[] model(IBlockState state) {
		model[3] = state.getValue(DIAMETER) << 3;
		model[1] = shaftMat.texture;
		return model;
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
	public double radius(IBlockState state) {
		return r * state.getValue(DIAMETER);
	}

	@Override
	public double getDebris(IBlockState state, List<ItemStack> items) {
		int d = state.getValue(DIAMETER);
		items.add(ItemHandlerHelper.copyStackWithSize(shaftMat.scrap, shaftMat.scrap.getCount() * (1 + d * d)));
		return r * d;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void addInformation(IBlockState state, List<String> tooltip, ITooltipFlag advanced) {
		super.addInformation(state, tooltip, advanced);
		tooltip.add(TooltipUtil.format("info.kineng.gear", radius(state), shaftMat.strength * Gear.A_CONTACT, shaftMat.friction * 100.0));
	}

}

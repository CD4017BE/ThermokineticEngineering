package cd4017be.kineng.block;

import cd4017be.lib.block.AdvancedBlock;
import static cd4017be.kineng.physics.Formula.J_cylinder;
import static cd4017be.kineng.physics.Formula.torsionStrength_circle;
import static net.minecraft.block.BlockRotatedPillar.AXIS;
import static net.minecraft.util.EnumFacing.Axis.*;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

/** @author CD4017BE */
public class BlockShaft extends AdvancedBlock implements IFillBlockSrc {

	private static final double DEFAULT_J_dens = J_cylinder(0.25, 1.0);
	private static final double DEFAULT_strength = torsionStrength_circle(0.25);

	public final ShaftMaterial shaftMat;
	public final double r;
	public double J_dens = DEFAULT_J_dens;
	public double strength = DEFAULT_strength;
	public int[] model;

	public BlockShaft(String id, ShaftMaterial m, double r, Class<? extends TileEntity> tile) {
		super(id, m.material, m.blockSound, 3, tile);
		this.shaftMat = m;
		this.r = r;
		setDefaultState(blockState.getBaseState().withProperty(AXIS, Axis.Y));
		if (r >= 0.5) 
			boundingBox = new AxisAlignedBB[] {FULL_BLOCK_AABB, FULL_BLOCK_AABB, FULL_BLOCK_AABB};
		else {
			double min = 0.5 - r, max = 0.5 + r;
			boundingBox = new AxisAlignedBB[] {
				new AxisAlignedBB(0, min, min, 1, max, max),
				new AxisAlignedBB(min, 0, min, max, 1, max),
				new AxisAlignedBB(min, min, 0, max, max, 1),
			};
		}
		setLightOpacity(0);
	}

	public BlockShaft setShape(double r0, double l) {
		J_dens = J_cylinder(r0, 1.0 - l) + J_cylinder(r, l);
		strength = torsionStrength_circle(r0);
		return this;
	}

	public void setModel(int... modelArgs) {
		this.model = modelArgs;
	}

	public double J(IBlockState state) {
		return shaftMat.density * J_dens;
	}

	public double maxM(IBlockState state) {
		return shaftMat.strength * strength;
	}

	public int[] model(IBlockState state) {
		model[1] = shaftMat.texture;
		return model;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AXIS);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(AXIS, Axis.values()[meta % 3]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(AXIS).ordinal();
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getStateForPlacement(
		World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
		EntityLivingBase placer
	) {
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(
			AXIS, facing.getAxis()
		);
	}

	@Override
	protected AxisAlignedBB getMainBB(IBlockState state, IBlockAccess world, BlockPos pos) {
		return boundingBox[state.getValue(AXIS).ordinal()];
	}

	public AxisAlignedBB getSize(BlockPos pos, IBlockState state) {
		return getSize(pos, state.getValue(AXIS), r);
	}

	public static AxisAlignedBB getSize(BlockPos pos, Axis ax, double r) {
		double a = -0.01; r -= 0.51;
		return new AxisAlignedBB(pos).grow(ax == X ? a : r, ax == Y ? a : r, ax == Z ? a : r);
	}

	@Override
	public boolean supportsFill(IBlockState state, EnumFacing side) {
		return r > 0.5;
	}

	public static class ShaftMaterial {

		public final Material material;
		public final SoundType blockSound;
		public int texture;
		/** [kg/m³] density*/
		public double density;
		/** [1] friction coefficient */
		public double friction;
		/** [N/m²] mechanical robustness */
		public double strength;
		//real values: Lead = 10...15 MPa, Tin = 15 MPa, Aluminum Alloy = 200...640 MPa
		//Cast Iron = 100...350 MPa, Titanium Alloy = 290...1200(Ti-Al6-V4) MPa, Steel = 310...690 MPa
		//Steel Alloy = 1100...1300 MPa, Carbon Nanotubes = 63 GPa

		public ShaftMaterial(Material material, SoundType blockSound) {
			this.material = material;
			this.blockSound = blockSound;
		}

	}

}

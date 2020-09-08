package cd4017be.kineng.block;

import cd4017be.lib.block.AdvancedBlock;
import static net.minecraft.block.BlockRotatedPillar.AXIS;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

/** @author CD4017BE */
public class BlockShaft extends AdvancedBlock {

	public final ShaftMaterial shaftMat;

	public BlockShaft(String id, ShaftMaterial m, double r, Class<? extends TileEntity> tile) {
		super(id, m.material, m.blockSound, 3, tile);
		this.shaftMat = m;
		setDefaultState(blockState.getBaseState().withProperty(AXIS, Axis.Y));
		double min = 0.5 - r, max = 0.5 + r;
		boundingBox = new AxisAlignedBB[] {
			new AxisAlignedBB(0, min, min, 1, max, max),
			new AxisAlignedBB(min, 0, min, max, 1, max),
			new AxisAlignedBB(min, min, 0, max, max, 1),
		};
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

	public static class ShaftMaterial {

		public final Material material;
		public final SoundType blockSound;
		public ResourceLocation texture;
		/** [kg/m³] density*/
		public double ρ;
		/** [1] friction coefficient */
		public double μR;
		/** [N/m²] mechanical robustness */
		public double R;
		//real values: Lead = 10...15 MPa, Tin = 15 MPa, Aluminum Alloy = 200...640 MPa
		//Cast Iron = 100...350 MPa, Titanium Alloy = 290...1200(Ti-Al6-V4) MPa, Steel = 310...690 MPa
		//Steel Alloy = 1100...1300 MPa, Carbon Nanotubes = 63 GPa

		public ShaftMaterial(Material material, SoundType blockSound) {
			this.material = material;
			this.blockSound = blockSound;
		}

	}

}

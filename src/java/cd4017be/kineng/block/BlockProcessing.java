package cd4017be.kineng.block;

import static cd4017be.kineng.tileentity.IKineticLink.T_SHAPE;
import static cd4017be.kineng.tileentity.IKineticLink.T_TIER;
import static cd4017be.lib.util.Orientation.*;
import static net.minecraft.block.BlockRotatedPillar.AXIS;
import cd4017be.lib.block.OrientedBlock;
import cd4017be.lib.property.PropertyOrientation;
import cd4017be.lib.util.Orientation;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


/** 
 * @author CD4017BE */
public class BlockProcessing extends OrientedBlock {

	static final PropertyOrientation ORIENT = new PropertyOrientation(
		"orient", N, E, S, W, Bn, Tn, Rw, Rn, Re, Rs, Be, Te
	) {
		@Override
		public EnumFacing[] rotations() {
			return EnumFacing.values();
		}

		@Override
		public Orientation getRotatedState(Orientation state, EnumFacing side) {
			return state.front == side || state.back == side ? state
				: ALL_AXIS.getRotatedState(state, side);
		}

		@Override
		public Orientation getPlacementState(boolean sneak, int y, int p, EnumFacing f, float X, float Y, float Z) {
			return ALL_AXIS.getPlacementState(sneak, y, p, f, X, Y, Z);
		}
	};

	public int types;

	public BlockProcessing(
		String id, Material m, SoundType sound, int flags, Class<? extends TileEntity> tile
	) {
		super(id, m, sound, flags, tile, ORIENT);
	}

	public BlockProcessing addRcp(int tier, int... type) {
		for (int t : type)
			types |= 0x100 << (t >> 8);
		types = Math.max(types & T_TIER, tier) | types & T_SHAPE;
		return this;
	}

	@Override
	public IBlockState getStateForPlacement(
		World world, BlockPos pos, EnumFacing s, float X, float Y, float Z, int m,
		EntityLivingBase placer, EnumHand hand
	) {
		return correctOrient(
			super.getStateForPlacement(world, pos, s, X, Y, Z, m, placer, hand),
			world, pos
		);
	}

	@Override
	public OrientedBlock setBlockBounds(AxisAlignedBB box) {
		super.setBlockBounds(box);
		box = Tn.rotate(boundingBox[Be.ordinal()]);
		for (int i = 8; i < 12; i++)
			boundingBox[i] = Orientation.values()[i].rotate(box);
		return this;
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		Orientation o = world.getBlockState(pos).getValue(orientProp);
		Orientation no = orientProp.getRotatedState(o, axis);
		if (no == o) return false;
		world.setBlockState(pos, correctOrient(
			getDefaultState().withProperty(orientProp, no), world, pos
		));
		return true;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block b, BlockPos src) {
		super.neighborChanged(state, world, pos, b, src);
		IBlockState state1 = correctOrient(state, world, pos);
		if (state1 != state) world.setBlockState(pos, state1);
	}

	private IBlockState correctOrient(IBlockState state, World world, BlockPos pos) {
		Orientation o = state.getValue(orientProp);
		pos = pos.offset(o.back);
		IBlockState nstate = world.getBlockState(pos);
		if (!(nstate.getBlock() instanceof BlockShaft)) return state;
		Axis axis = nstate.getValue(AXIS);
		if (axis == o.back.getAxis()) return state;
		int oi = o.ordinal();
		if ((oi & 4) != 0) //pointing vertical
			if ((oi & 1) != 0 ^ axis == Axis.Z)
				oi ^= 1; //90° flip vertical orientation
			else return state;
		else //pointing horizontal
			if ((oi & 8) != 0 ^ axis == Axis.Y)
				oi ^= 8|2; //90° flip horizontal orientation
			else return state;
		return state.withProperty(orientProp, Orientation.values()[oi]);
	}

}

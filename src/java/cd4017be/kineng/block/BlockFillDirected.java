package cd4017be.kineng.block;

import static net.minecraft.block.BlockDirectional.FACING;
import static cd4017be.kineng.block.BlockFillShared.*;
import cd4017be.kineng.Objects;
import cd4017be.lib.block.AdvancedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


/** 
 * @author CD4017BE */
public class BlockFillDirected extends AdvancedBlock implements IFillBlockSrc {

	public static final PropertyBool HALF = PropertyBool.create("half");

	/**
	 * @param id
	 * @param tile
	 */
	public BlockFillDirected(String id, Class<? extends TileEntity> tile) {
		super(id, Material.BARRIER, SoundType.STONE, 3, tile);
		boundingBox = new AxisAlignedBB[] {
			FULL_BLOCK_AABB,
			new AxisAlignedBB(0, 0, 0, 1, .5, 1),
			new AxisAlignedBB(0, .5, 0, 1, 1, 1),
			new AxisAlignedBB(0, 0, 0, 1, 1, .5),
			new AxisAlignedBB(0, 0, .5, 1, 1, 1),
			new AxisAlignedBB(0, 0, 0, .5, 1, 1),
			new AxisAlignedBB(.5, 0, 0, 1, 1, 1)
		};
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, HALF);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7)).withProperty(HALF, (meta & 8) != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).ordinal() | (state.getValue(HALF) ? 8 : 0);
	}

	@Override
	protected AxisAlignedBB getMainBB(IBlockState state, IBlockAccess world, BlockPos pos) {
		return boundingBox[state.getValue(HALF) ? state.getValue(FACING).ordinal() + 1 : 0];
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return !state.getValue(HALF);
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return !state.getValue(HALF);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block b, BlockPos src) {
		super.neighborChanged(state, world, pos, b, src);
		EnumFacing dir = state.getValue(FACING);
		IBlockState nstate = world.getBlockState(pos.offset(dir));
		Block block = nstate.getBlock();
		if (!(block instanceof IFillBlockSrc && ((IFillBlockSrc)block).supportsFill(nstate, dir.getOpposite())))
			world.setBlockToAir(pos);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		if (world.getBlockState(pos).getBlock() instanceof BlockFillShared) return;
		propagateBreak(world, pos.offset(state.getValue(FACING)));
	}

	@Override
	public boolean supportsFill(IBlockState state, EnumFacing side) {
		return state.getValue(FACING) != side;
	}

	public boolean place(World world, BlockPos pos, EnumFacing facing, boolean half) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block.isReplaceable(world, pos)) {
			block.dropBlockAsItem(world, pos, state, 0);
			return world.setBlockState(pos, getDefaultState().withProperty(FACING, facing).withProperty(HALF, half));
		}
		if (!(block instanceof BlockFillDirected && state.getValue(HALF))) return false;
		Orient o = Orient.of(state.getValue(FACING), facing);
		return o == null || world.setBlockState(pos, Objects.FILL_SHARE.getDefaultState().withProperty(ORIENT, o));
	}

	public static void placeCircle(World world, BlockPos pos, int diameter, EnumFacing dX, EnumFacing dY) {
		int rsq0 = 1;
		for (int r = 1, r1 = diameter >> 1; r <= r1; r++) {
			int full = (r < r1 ? 1 : 0) | (diameter & 1);
			int rsq1 = (r + full + 1) * r;
			for (int x = -r; x <= r; x++) {
				BlockPos pos1 = pos.offset(dX, x);
				for (int y = -r; y <= r; y++) {
					int dsq = x * x + y * y;
					if (dsq < rsq0 || dsq >= rsq1) continue;
					Objects.FILL_DIR.place(
						world, pos1.offset(dY, y),
						x > y ? (x >= -y ? dX.getOpposite() : dY) : (y > -x ? dY.getOpposite() : dX),
						full == 0 && (x == r || x == -r || y == r || y == -r)
					);
				}
			}
			rsq0 = rsq1;
		}
	}

	public static void propagateBreak(World world, BlockPos pos) {
		Block block;
		IBlockState state;
		while((block = (state = world.getBlockState(pos)).getBlock()) instanceof BlockFillDirected)
			pos = pos.offset(state.getValue(FACING));
		if (!(block instanceof IFillBlockSrc)) return;
		block.dropBlockAsItem(world, pos, state, 0);
		world.setBlockToAir(pos);
	}

}

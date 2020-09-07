package cd4017be.kineng.block;

import static net.minecraft.block.BlockDirectional.FACING;
import static cd4017be.kineng.block.BlockFillDirected.HALF;
import cd4017be.kineng.Objects;
import cd4017be.lib.block.AdvancedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import static net.minecraft.util.EnumFacing.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


/** 
 * @author CD4017BE */
public class BlockFillShared extends AdvancedBlock {

	public static final PropertyEnum<Orient> ORIENT = PropertyEnum.create("orient", Orient.class);

	/**
	 * @param id
	 * @param tile
	 */
	public BlockFillShared(String id, Class<? extends TileEntity> tile) {
		super(id, Material.BARRIER, SoundType.STONE, 3, tile);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ORIENT);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(ORIENT, Orient.values()[meta % 15]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(ORIENT).ordinal();
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block b, BlockPos src) {
		super.neighborChanged(state, world, pos, b, src);
		Orient o = state.getValue(ORIENT);
		IBlockState nstate = world.getBlockState(pos.offset(o.f1));
		Block block = nstate.getBlock();
		int supports = block instanceof IFillBlockSrc && ((IFillBlockSrc)block).supportsFill(nstate, o.f1.getOpposite()) ? 1 : 0;
		nstate = world.getBlockState(pos.offset(o.f2));
		block = nstate.getBlock();
		if (block instanceof IFillBlockSrc && ((IFillBlockSrc)block).supportsFill(nstate, o.f2.getOpposite())) supports |= 2;
		switch(supports) {
		case 0:
			world.setBlockToAir(pos);
			return;
		case 1:
			world.setBlockState(pos, Objects.FILL_DIR.getDefaultState().withProperty(FACING, o.f1).withProperty(HALF, true));
			return;
		case 2:
			world.setBlockState(pos, Objects.FILL_DIR.getDefaultState().withProperty(FACING, o.f2).withProperty(HALF, true));
			return;
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		if (world.getBlockState(pos).getBlock() instanceof IFillBlockSrc) return;
		Orient o = state.getValue(ORIENT);
		BlockFillDirected.propagateBreak(world, pos.offset(o.f1));
		BlockFillDirected.propagateBreak(world, pos.offset(o.f2));
	}

	public enum Orient implements IStringSerializable {
		BT(DOWN, UP), BN(DOWN, NORTH), BS(DOWN, SOUTH), BW(DOWN, WEST), BE(DOWN, EAST),
		TN(UP, NORTH), TS(UP, SOUTH), TW(UP, WEST), TE(UP, EAST),
		NS(NORTH, SOUTH), NW(NORTH, WEST), NE(NORTH, EAST),
		SW(SOUTH, WEST), SE(SOUTH, EAST),
		WE(WEST, EAST);

		public final EnumFacing f1, f2;

		private Orient(EnumFacing f1, EnumFacing f2) {
			this.f1 = f1;
			this.f2 = f2;
		}

		@Override
		public String getName() {
			return name().toLowerCase();
		}
		
		public static Orient of(EnumFacing f1, EnumFacing f2) {
			if (f1 == f2) return null;
			if (f1.ordinal() > f2.ordinal()) {
				EnumFacing f = f1;
				f1 = f2;
				f2 = f;
			}
			int i = f1.ordinal(), j = f2.ordinal() - 1;
			return values()[((9 - i) * i >> 1) + j];
		}
	}

}

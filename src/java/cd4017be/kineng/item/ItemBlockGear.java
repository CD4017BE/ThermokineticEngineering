package cd4017be.kineng.item;

import static net.minecraft.block.BlockRotatedPillar.AXIS;
import cd4017be.kineng.block.*;
import cd4017be.lib.item.BaseItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.Axis;
import static net.minecraft.util.EnumFacing.Axis.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

/** @author CD4017BE */
public class ItemBlockGear extends BaseItemBlock {

	final int diameter;

	public ItemBlockGear(BlockGear id) {
		super(id);
		this.diameter = 0;
		setHasSubtypes(true);
	}

	public ItemBlockGear(BlockShaft id, int d) {
		super(id);
		this.diameter = d;
	}

	@Override
	public boolean canPlaceBlockOnSide(
		World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack
	) {
		Block block = world.getBlockState(pos).getBlock();
		if (block == Blocks.SNOW_LAYER && block.isReplaceable(world, pos))
			side = EnumFacing.UP;
		else if (!block.isReplaceable(world, pos))
			pos = pos.offset(side);
		return world.mayPlace(this.block, pos, false, side, (Entity)null)
			&& hasEnoughSpace(world, pos, side.getAxis(), stack.getMetadata());
	}

	@Override
	public boolean placeBlockAt(
		ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
		float hitX, float hitY, float hitZ, IBlockState newState
	) {
		int d = block instanceof BlockGear ? (int)(((BlockGear)this.block).radius(newState) * 2.0) : diameter;
		Axis ax = newState.getValue(AXIS);
		if (!hasEnoughSpace(world, pos, ax, d) || !super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState))
			return false;
		if (ax == X) BlockFillDirected.placeCircle(world, pos, d, EnumFacing.SOUTH, EnumFacing.UP);
		else if (ax == Y) BlockFillDirected.placeCircle(world, pos, d, EnumFacing.EAST, EnumFacing.SOUTH);
		else BlockFillDirected.placeCircle(world, pos, d, EnumFacing.EAST, EnumFacing.UP);
		return true;
	}

	@Override
	public int getMetadata(int damage) {
		return (damage - 1) * 3;
	}

	public static boolean hasEnoughSpace(World world, BlockPos pos, Axis axis, int diameter) {
		MutableBlockPos pos1 = new MutableBlockPos();
		int r = diameter >> 1, full = (diameter & 1);
		int rsq1 = (r + full + 1) * r;
		for (int j = -r; j <= r; j++)
			for (int i = -r; i <= r; i++) {
				if (j * j + i * i >= rsq1) continue;
				switch(axis) {
				case X: pos1.setPos(pos.getX(), pos.getY() + i, pos.getZ() + j); break;
				case Y: pos1.setPos(pos.getX() + i, pos.getY(), pos.getZ() + j); break;
				default: pos1.setPos(pos.getX() + i, pos.getY() + j, pos.getZ()); break;
				}
				if (!canFillAt(
					world, pos1,
					full == 0 && (j == r || j == -r || i == r || i == -r)
				)) return false;
			}
		return true;
	}

	private static boolean canFillAt(World world, BlockPos pos, boolean half) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		return block.isReplaceable(world, pos)
			|| half && block instanceof BlockFillDirected && state.getValue(BlockFillDirected.HALF);
	}

}

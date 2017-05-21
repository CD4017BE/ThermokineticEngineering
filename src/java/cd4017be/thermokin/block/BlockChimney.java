package cd4017be.thermokin.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import cd4017be.lib.TileBlock;

public class BlockChimney extends TileBlock {

	public BlockChimney(String id, Material m, SoundType sound, int type) {
		super(id, m, sound, type);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		neighborChanged(state, world, pos, this);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block b) {
		IBlockState parent = world.getBlockState(pos.up());
		int n = parent.getBlock() instanceof BlockChimney ?
				Math.min(parent.getValue(prop) + 1, 15) : 0;
		if (n != state.getValue(prop)) {
			world.setBlockState(pos, state.withProperty(prop, n), 2);
			world.notifyBlockOfStateChange(pos.down(), b = this);
		}
		super.neighborChanged(state, world, pos, b);
	}

	public static final PropertyInteger prop = PropertyInteger.create("height", 0, 15);

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.blockState.getBaseState().withProperty(prop, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(prop);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void addProperties(ArrayList<IProperty> main) {
		main.add(prop);
	}

}

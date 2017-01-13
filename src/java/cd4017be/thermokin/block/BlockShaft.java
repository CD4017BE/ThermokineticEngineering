package cd4017be.thermokin.block;

import java.util.ArrayList;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import cd4017be.lib.templates.BlockPipe;
import cd4017be.lib.util.Utils;

public class BlockShaft extends BlockPipe {

	public BlockShaft(String id, Material m, int type) {
		super(id, m, SoundType.METAL, type);
		this.size = 0.5F;
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing s, float X, float Y, float Z, int m, EntityLivingBase placer) {
		int dir = placer.isSneaking() ? s.getIndex() : Utils.getLookDir(placer);
		return this.getStateFromMeta(dir / 2);
	}

	private static final PropertyInteger prop = PropertyInteger.create("dir", 0, 2);
	
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

package cd4017be.kineng.block;

import cd4017be.kineng.tileentity.CircularMultiblockPart;
import cd4017be.lib.block.AdvancedBlock;
import cd4017be.lib.property.PropertyBlockMimic;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockFillCustom extends AdvancedBlock {

	public static final PropertyBool linked = PropertyBool.create("link");

	public BlockFillCustom(String id, Material m, SoundType sound, Class<? extends TileEntity> tile) {
		super(id, m, sound, 3, tile);
		setDefaultState(getBlockState().getBaseState().withProperty(linked, false));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[]{linked}, new IUnlistedProperty[] {PropertyBlockMimic.instance});
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof CircularMultiblockPart)
			return state.withProperty(linked, ((CircularMultiblockPart)te).isLinked);
		return state;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof CircularMultiblockPart) {
			CircularMultiblockPart part = (CircularMultiblockPart)te;
			if (!part.isLinked)
				return ((IExtendedBlockState)state).withProperty(PropertyBlockMimic.instance, part.storedBlock);
		}
		return state;
	}

	@Override
	public float getBlockHardness(IBlockState blockState, World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof CircularMultiblockPart) {
			CircularMultiblockPart part = (CircularMultiblockPart)te;
			return part.storedBlock.getBlockHardness(world, pos);
		}
		return super.getBlockHardness(blockState, world, pos);
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof CircularMultiblockPart) {
			CircularMultiblockPart part = (CircularMultiblockPart)te;
			return part.storedBlock.getBlock().getExplosionResistance(world, pos, exploder, explosion);
		}
		return super.getExplosionResistance(world, pos, exploder, explosion);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof CircularMultiblockPart) {
			CircularMultiblockPart part = (CircularMultiblockPart)te;
			return part.storedBlock.getBlock().getPickBlock(part.storedBlock, target, world, pos, player);
		}
		return super.getPickBlock(state, target, world, pos, player);
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof CircularMultiblockPart) {
			CircularMultiblockPart part = (CircularMultiblockPart)te;
			return part.storedBlock.getBlock().canEntityDestroy(part.storedBlock, world, pos, entity);
		}
		return true;
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof CircularMultiblockPart) {
			CircularMultiblockPart part = (CircularMultiblockPart)te;
			IBlockState state = part.storedBlock;
			Block block = state.getBlock();
			if (state.getMaterial().isToolNotRequired())
			{
				return true;
			}

			ItemStack stack = player.getHeldItemMainhand();
			String tool = block.getHarvestTool(state);
			if (stack.isEmpty() || tool == null)
			{
				return player.canHarvestBlock(state);
			}

			int toolLevel = stack.getItem().getHarvestLevel(stack, tool, player, state);
			if (toolLevel < 0) {
				return player.canHarvestBlock(state);
			}

			return toolLevel >= block.getHarvestLevel(state);
		}
		return super.canHarvestBlock(world, pos, player);
	}

}

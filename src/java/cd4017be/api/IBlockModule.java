package cd4017be.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 
 * @author CD4017BE
 */
public interface IBlockModule {

	/**
	 * restores state from a given NBT tag
	 * @param nbt the CompoundTag to restore from
	 * @param k the name prefix to use
	 */
	public void readNBT(NBTTagCompound nbt, String k);

	/**
	 * saves state to a given NBT tag
	 * @param nbt the CompoundTag to save in
	 * @param k the name prefix to use
	 */
	public void writeNBT(NBTTagCompound nbt, String k);

	/**
	 * initializes state for a given position in world
	 * @param world 
	 * @param pos
	 */
	public void initialize(World world, BlockPos pos);

	/**
	 * invalidates state to perform cleanup
	 */
	public void invalidate();

}

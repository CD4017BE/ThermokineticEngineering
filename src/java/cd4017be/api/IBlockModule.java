package cd4017be.api;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * 
 * @author CD4017BE
 */
public interface IBlockModule extends ICapabilityProvider {

	/**
	 * restores state from a given NBT tag
	 * @param nbt the CompoundTag to restore from
	 * @param k the name prefix to use
	 */
	public void readNBT(NBTTagCompound nbt, String k, TileEntity te);

	/**
	 * saves state to a given NBT tag
	 * @param nbt the CompoundTag to save in
	 * @param k the name prefix to use
	 */
	public void writeNBT(NBTTagCompound nbt, String k);

	/**
	 * initializes state (called when TileEntity validates)
	 */
	public void initialize(TileEntity te);

	/**
	 * invalidates state to perform cleanup
	 */
	public void invalidate();

	/**
	 * @param cap
	 * @return whether {@link #hasCapability} or {@link #getCapability} should be called on this module to retrieve the given capability (it won't otherwise)
	 */
	public boolean supportsCapability(@Nonnull Capability<?> cap);

}

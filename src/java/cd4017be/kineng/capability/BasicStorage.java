package cd4017be.kineng.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.INBTSerializable;


/** 
 * @author CD4017BE */
public class BasicStorage<T extends INBTSerializable<NBTBase>> implements IStorage<T> {

	@Override
	public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
		return instance.serializeNBT();
	}

	@Override
	public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
		instance.deserializeNBT(nbt);
	}

}

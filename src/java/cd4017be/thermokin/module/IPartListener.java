package cd4017be.thermokin.module;

import java.util.List;

import cd4017be.thermokin.tileentity.ModularMachine;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IPartListener {

	void onPartChanged(ModularMachine m, int i, Part old);

	/**
	 * @param m
	 * @param nbt data stored in item used to place machine
	 */
	void onPlaced(ModularMachine m, NBTTagCompound nbt);

	void addDrops(ModularMachine m, NBTTagCompound nbt, List<ItemStack> drops);

	void onCfgChange(ModularMachine m, int i, int cfg);

}

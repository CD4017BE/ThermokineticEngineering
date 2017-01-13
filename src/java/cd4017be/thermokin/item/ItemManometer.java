package cd4017be.thermokin.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import cd4017be.api.circuits.ItemBlockSensor;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.multiblock.GasContainer;

public class ItemManometer extends ItemBlockSensor {

	public ItemManometer(String id) {
		super(id, 20F);
		setCreativeTab(Objects.tabThermokin);
	}

	@Override
	protected float measure(ItemStack sensor, NBTTagCompound nbt, World world, BlockPos pos, EnumFacing side) {
		TileEntity te = world.getTileEntity(pos);
		GasContainer gas = te != null ? te.getCapability(Objects.GAS_CAP, side) : null;
		return gas != null ? (float)gas.network.gas.P() : 0F;
	}

}

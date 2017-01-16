package cd4017be.thermokin.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import cd4017be.api.circuits.ItemBlockSensor;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.multiblock.IHeatReservoir;
import cd4017be.thermokin.recipe.Substances;

public class ItemThermometer extends ItemBlockSensor {

	public ItemThermometer(String id) {
		super(id, 20F);
		setCreativeTab(Objects.tabThermokin);
	}

	@Override
	protected float measure(ItemStack sensor, NBTTagCompound nbt, World world, BlockPos pos, EnumFacing side) {
		TileEntity te = world.getTileEntity(pos);
		IHeatReservoir heat = te != null ? te.getCapability(Objects.HEAT_CAP, side) : null;
		return heat != null ? heat.T() : Substances.getEnvFor(world).getTemp(world, pos);
	}

}

package cd4017be.thermokin.tileentity;

import cd4017be.api.IBlockModule;
import cd4017be.lib.block.AdvancedBlock.IInteractiveTile;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.module.HeatModule;
import cd4017be.thermokin.module.InventoryModule;
import cd4017be.thermokin.module.Layout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraftforge.items.IItemHandler;


/**
 * @author CD4017BE
 *
 */
public class SolidFuelOven extends ModularMachine implements ITickable, IInteractiveTile {

	public static int MAX_FUEL = 6400;
	public static float C = 10000, FUEL_VALUE = 10000F, BURN_TEMP = 2500F, HEATING_POWER = 10000F; // efficiency = 1.0 - (Tcase - Tenv) / BURN_TEMP

	public HeatModule heat = new HeatModule(C);
	private InventoryModule inventory = new InventoryModule(this, 1);
	private float fuel;
	private ItemStack lastItem = ItemStack.EMPTY;
	private int lastBurnTime;

	@Override
	public void update() {
		if (world.isRemote) return;
		heat.update();
		if (fuel > 0) {
			if (fuel <= HEATING_POWER) refuel();
			float dQ = Math.min(fuel, HEATING_POWER); //[J] released combustion energy
			float C = dQ / BURN_TEMP; //[J/K] heat capacity of combustion gases
			fuel -= dQ;
			heat.T = (heat.T * heat.C + heat.envT() * C + dQ) / (heat.C + C); //assume fuel air mixture starts cold, heats up in combustion, exchanges heat with casing and is finally dumped away.
		} else if ((world.getTotalWorldTime() & 31) == 0) refuel();
		
		if ((world.getTotalWorldTime() & 3) == 0) markUpdate();
	}

	private void refuel() {
		IItemHandler acc = inventory.getExtInventory(6);
		if (acc != null) {
			ItemStack stack = ItemFluidUtil.drain(acc, this::burnAm);
			fuel += (float)stack.getCount() * (float)lastBurnTime * FUEL_VALUE;
		}
	}

	private int burnAm(ItemStack item) {
		int n;
		if (item.isItemEqual(lastItem)) n = lastBurnTime;
		else n = lastBurnTime = TileEntityFurnace.getItemBurnTime(lastItem = item);
		return n == 0 ? 0 : (MAX_FUEL-1) / n + 1;
	}

	@Override
	public Layout getLayout() {
		return Objects.ovenL;
	}

	@Override
	public IBlockModule[] getModules() {
		return new IBlockModule[] {heat, inventory};
	}

	@Override
	protected void writeState(NBTTagCompound nbt) {
		nbt.setFloat("t", heat.T);//TODO user better sync mechanism
		super.writeState(nbt);
	}

	@Override
	protected void readState(NBTTagCompound nbt) {
		heat.T = nbt.getFloat("t");
		super.readState(nbt);
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z) {
		if (world.isRemote) return true;
		int n = item.getCount();
		if (n > 0 && (n = Math.min(n, burnAm(item))) > 0) {
			item.shrink(n);
			fuel += (float)n * (float)lastBurnTime * FUEL_VALUE;
		}
		return true;
	}

	@Override
	public void onClicked(EntityPlayer player) {
	}

	//TODO delete fuel and stop working when bottom casing breaks
}

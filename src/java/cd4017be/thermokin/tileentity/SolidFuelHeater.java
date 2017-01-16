package cd4017be.thermokin.tileentity;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;
import cd4017be.thermokin.multiblock.HeatReservoir;
import cd4017be.thermokin.recipe.Substances;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.util.Utils;

public class SolidFuelHeater extends AutomatedTile implements IGuiData {

	private static final float FuelEnergy = 10000F;
	private static final int BurnRate = 8;
	public int fuel, burn, speed;
	public float Tref, temp;
	HeatReservoir heat;

	public SolidFuelHeater() {
		//ints: maxFuel, curFuel, burn; floats: targetTemp, Temp
		heat = new HeatReservoir(10000F, Substances.def_con);
		inventory = new Inventory(2, 1, null).group(0, 0, 2, Utils.IN);
		speed = 1;
		Tref = 300F;
	}

	@Override
	public void update() {
		super.update();
		if (worldObj.isRemote) return;
		heat.update(this);
		if (burn < speed && heat.T < Tref && !worldObj.isBlockPowered(pos)) {
			for (int i = 0; i < 2; i++) {
				int n = TileEntityFurnace.getItemBurnTime(inventory.items[i]);
				if (n != 0) {
					fuel = n;
					burn += fuel;
					inventory.extractItem(i, 1, false);
					break;
				}
			}
		}
		if (burn != 0) {
			int p = Math.min(speed, burn);
			burn -= p;
			heat.T += p * FuelEnergy / heat.C;
		}
		temp = heat.T;
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if (cmd == 0) Tref = dis.readFloat();
		else if (cmd == 1 && speed < BurnRate) speed++;
		else if (cmd == 2 && speed > 1) speed--;
	}

	@Override
	public void onNeighborBlockChange(Block b) {
		heat.check = true;
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		heat.check = true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		heat.load(nbt, "heat");
		Tref = nbt.getFloat("refT");
		burn = nbt.getInteger("fuel");
		fuel = burn;
		speed = nbt.getInteger("burn");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		heat.save(nbt, "heat");
		nbt.setFloat("refT", Tref);
		nbt.setInteger("fuel", burn);
		nbt.setInteger("burn", speed);
		return super.writeToNBT(nbt);
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.addItemSlot(new SlotItemHandler(inventory, 0, 26, 16));
		container.addItemSlot(new SlotItemHandler(inventory, 1, 26, 34));
		container.addPlayerInventory(8, 68);
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 0, 2, false);
		return true;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{fuel, burn, speed, Float.floatToIntBits(Tref), Float.floatToIntBits(temp)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: fuel = v; break;
		case 1: burn = v; break;
		case 2: speed = v; break;
		case 3: Tref = Float.intBitsToFloat(v); break;
		case 4: temp = Float.intBitsToFloat(v); break;
		}
	}

}

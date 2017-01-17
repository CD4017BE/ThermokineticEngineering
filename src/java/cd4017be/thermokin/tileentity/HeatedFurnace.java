package cd4017be.thermokin.tileentity;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;
import cd4017be.thermokin.multiblock.HeatReservoir;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.util.Utils;

public class HeatedFurnace extends AutomatedTile implements IGuiData {

	public static float NeededTemp, TRwork, Energy, C0, R0;
	public HeatReservoir heat;
	private boolean done = true;
	public int num;
	public float progress, temp;

	public HeatedFurnace() {
		inventory = new Inventory(3, 2, null).group(0, 0, 1, Utils.IN).group(1, 1, 2, Utils.OUT);
		heat = new HeatReservoir(C0, R0);
	}

	@Override
	public void update() {
		super.update();
		if (worldObj.isRemote) return;
		heat.update(this);
		float e = (heat.T - NeededTemp) * heat.C;
		if (inventory.items[2] == null && heat.T > NeededTemp && inventory.items[0] != null && FurnaceRecipes.instance().getSmeltingResult(inventory.items[0]) != null) {
			inventory.items[2] = inventory.extractItem(0, (int)Math.ceil(e / Energy), false);
			done = false;
		}
		if (inventory.items[2] != null) {
			if (!done){
				int sz = inventory.items[2].stackSize;
				float req = (float)sz * Energy;
				if (progress < req && e > 0) {
					float dQ = e / TRwork;
					progress += dQ;
					heat.addHeat(-dQ);
				}
				if (progress >= req) {
					ItemStack item = FurnaceRecipes.instance().getSmeltingResult(inventory.items[2]);
					if (item != null) {
						progress -= req;
						inventory.items[2] = item.copy();
						inventory.items[2].stackSize *= sz;
					}
					done = true;
				}
			}
			if (done) {
				int n = inventory.items[2].getMaxStackSize();
				if (inventory.items[1] == null) 
					inventory.items[1] = inventory.extractItem(2, n, false);
				else if (inventory.items[1].isItemEqual(inventory.items[2]) && (n -= inventory.items[1].stackSize) > 0) 
					inventory.items[1].stackSize += inventory.extractItem(2, n, false).stackSize;
			}
		} else if (progress > 0) {
			heat.addHeat(progress);
			progress = 0;
		}
		temp = heat.T;
		num = inventory.items[2] == null ? 0 : inventory.items[2].stackSize;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		heat.load(nbt, "heat");
		progress = nbt.getFloat("progress");
		done = nbt.getBoolean("done");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		heat.save(nbt, "heat");
		nbt.setFloat("progress", progress);
		nbt.setBoolean("done", done);
		return super.writeToNBT(nbt);
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
	public void initContainer(DataContainer container) {
		TileContainer cont = (TileContainer)container;
		cont.addItemSlot(new SlotItemHandler(inventory, 0, 17, 16));
		cont.addItemSlot(new SlotItemType(inventory, 1, 71, 16));
		cont.addPlayerInventory(8, 68);
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 0, 1, false);
		return true;
	}

	public float getProgress() {
		return progress / Energy / (float)num;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{num, Float.floatToIntBits(progress), Float.floatToIntBits(temp)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: num = v; break;
		case 1: progress = Float.intBitsToFloat(v); break;
		case 2: temp = Float.intBitsToFloat(v); break;
		}
	}

}

package cd4017be.thermokin.tileentity;

import java.util.Random;

import cd4017be.api.IBlockModule;
import cd4017be.lib.capability.AbstractInventory;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.thermokin.item.ItemMachinePart;
import cd4017be.thermokin.module.IPartListener;
import cd4017be.thermokin.module.Part;
import cd4017be.thermokin.module.Part.Type;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

public abstract class ModularMachine extends BaseTileEntity {

	public static ItemMachinePart PART_ITEM;
	protected static final Random RAND = new Random();

	public final Part[] components = new Part[15];
	public final byte[] durability = new byte[15];

	public abstract IBlockModule[] getModules();
	public boolean isPartValid(int i, Part p) {
		return i < 6 && p.type == Type.CASING;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		int n = 0;
		for (IBlockModule m : getModules()) {
			m.readNBT(nbt, "M" + (n++));
			if (m instanceof IPartListener)
				((IPartListener)m).onPartsLoad(this);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		int n = 0;
		for (IBlockModule m : getModules())
			m.writeNBT(nbt, "M" + (n++));
		return super.writeToNBT(nbt);
	}

	@Override
	public void onChunkUnload() {
		for (IBlockModule m : getModules()) m.invalidate();
		super.onChunkUnload();
	}

	@Override
	public void invalidate() {
		for (IBlockModule m : getModules()) m.invalidate();
		super.invalidate();
	}

	@Override
	public void validate() {
		super.validate();
		for (IBlockModule m : getModules()) m.initialize(this);
	}

	public void setPart(int i, Part part) {
		Part old = components[i];
		if (part != old) {
			components[i] = part;
			for (IBlockModule m : getModules())
				if (m instanceof IPartListener)
					((IPartListener)m).onPartChanged(this, i);
			if (this instanceof IPartListener)
				((IPartListener)this).onPartChanged(this, i);
		}
	}

	public boolean damagePart(int i, float dmg) {
		if (dmg < 0) return false;
		int d = (durability[i] & 0xff) - MathHelper.floor(dmg);
		if (RAND.nextFloat() < dmg - (float)d) d--;
		if (d <= 0) {
			setPart(i, null);
			return true;
		} else {
			durability[i] = (byte)d;
			return false;
		}
	}

	public class PartInventory extends AbstractInventory {

		@Override
		public int getSlots() {
			return components.length;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			Part part = components[slot];
			if (part == null) return ItemStack.EMPTY;
			ItemStack item = part.item.copy();
			item.setItemDamage((Part.MAX_DUR - (durability[slot] & 0xff)) * item.getMaxDamage() / Part.MAX_DUR);
			return item;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			Part part = components[slot] = Part.getPart(stack);
			if (part != null) {
				int m = stack.getMaxDamage();
				durability[slot] = (byte) (m > 0 ? (m - stack.getItemDamage()) * Part.MAX_DUR / m : Part.MAX_DUR);
			}
		}

		@Override
		public int insertAm(int slot, ItemStack item) {
			Part p = Part.getPart(item);
			return p != null && isPartValid(slot, p) ? p.item.getCount() : 0;
		}

	}

}

package cd4017be.thermokin.module;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;

import cd4017be.api.IBlockModule;
import cd4017be.lib.capability.AbstractInventory;
import cd4017be.lib.capability.BasicInventory;
import cd4017be.lib.capability.BasicInventory.Restriction;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Utils;
import cd4017be.thermokin.module.PartIOModule.IOType;
import cd4017be.thermokin.tileentity.ModularMachine;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * Provides an internal inventory with integrated module handling to a ModularMachine 
 * @author CD4017BE
 */
public class InventoryModule extends AbstractInventory implements IPartListener, IBlockModule {

	/** internal processing slots */
	public ItemStack[] items;
	/** capabilities for external access */
	public final Access[] caps = new Access[6];

	private final ModularMachine tile;
	private final Restriction limiterIn;
	private final IntPredicate limiterOut;
	private final int size;

	/**
	 * creates an InventoryModule with unrestricted access to internal processing slots
	 * @param tile the machine this belongs to
	 * @param size amount of internal processing slots
	 */
	public InventoryModule(ModularMachine tile, int size) {
		this(tile, size, null, null);
	}

	/**
	 * creates an InventoryModule with restricted access to internal processing slots
	 * @param tilet he machine this belongs to
	 * @param size amount of internal processing slots
	 * @param maxInsert limiter function for insertion
	 * @param canExtract limiter function for extraction
	 */
	public InventoryModule(ModularMachine tile, int size, Restriction maxInsert, IntPredicate canExtract) {
		this.tile = tile;
		if (maxInsert == null ^ canExtract == null)
			if (canExtract == null) canExtract = (i)-> true;
			else maxInsert = BasicInventory::insertAmount;
		this.limiterIn = maxInsert;
		this.limiterOut = canExtract;
		this.size = size;
		this.items = new ItemStack[size];
		Arrays.fill(items, ItemStack.EMPTY);
	}

	@Override
	public void readNBT(NBTTagCompound nbt, String k, TileEntity te) {
		onPartsLoad();
		ItemFluidUtil.loadInventory(nbt.getTagList(k + "I", 0), items);
	}

	@Override
	public void writeNBT(NBTTagCompound nbt, String k) {
		nbt.setTag(k + "S", ItemFluidUtil.saveInventory(items));
	}

	@Override
	public void initialize(TileEntity te) {
	}

	@Override
	public void invalidate() {
	}

	@Override
	public void onPlaced(ModularMachine m, NBTTagCompound nbt) {
		onPartsLoad();
	}

	@Override
	public void addDrops(ModularMachine m, NBTTagCompound nbt, List<ItemStack> drops) {
		addToList(drops);
	}

	private void onPartsLoad() {
		int n = size;
		byte[] refs = new byte[6];
		for (int i = 0; i < caps.length; i++) {
			Part p = tile.components[i + 6];
			Access acc = null;
			if (p instanceof PartIOModule) {
				PartIOModule pi = (PartIOModule)p;
				if (pi.hasItem) {
					switch(pi.invType) {
					case EXT_ACC:
						int cfg = tile.getCfg(i);
						if (cfg < 0) break;
						if (cfg < 6)
							refs[i] = (byte)(cfg + 1);
						else if ((cfg -= 6) < size)
							acc = limiterIn == null ? new Access(cfg, 1) : new RestrictedAccess(cfg);
						break;
					case BUFFER:
						int l = pi.size;
						acc = new Access(n, l);
						n += l;
						break;
					default:
					}
				}
			}
			caps[i] = acc;
		}
		for (int i = 0; i < refs.length; i++) {
			byte b = refs[i];
			if (b != 0) caps[i] = caps[b - 1];
		}
		int l = items.length;
		if (n != l) {
			items = Arrays.copyOf(items, n);
			if (n > l) Arrays.fill(items, l, n, ItemStack.EMPTY);
		}
	}

	@Override
	public void onPartChanged(ModularMachine m, int i, Part old) {
		if (i < 6 || i >= 12) return;
		i -= 6;
		Access acc = caps[i];
		int s, l, nl = 0;
		byte refs = 0;
		if (acc != null && acc.s >= size && old instanceof PartIOModule && ((PartIOModule)old).invType == IOType.BUFFER) {
			l = acc.l;
			s = acc.s;
			for (int j = 0; j < caps.length; j++)
				if (caps[j] == acc) refs |= 1 << j;
		} else {
			l = 0;
			s = size;
			for (int j = 0; j < i; j++)
				if (caps[j] != null && caps[j].s == s)
					s += caps[j].l;
		}
		acc = null;
		Part p = m.components[i];
		if (p instanceof PartIOModule) {
			PartIOModule pi = (PartIOModule)p;
			if (pi.hasItem) {
				switch(pi.invType) {
				case EXT_ACC:
					int cfg = m.getCfg(i);
					if (cfg < 0) break;
					if (cfg < 6)
						acc = caps[cfg + 1];
					else if ((cfg -= 6) < size)
						acc = limiterIn == null ? new Access(cfg, 1) : new RestrictedAccess(cfg);
					break;
				case BUFFER:
					nl = pi.size;
					acc = new Access(s, nl);
					break;
				default:
				}
			}
		}
		if (refs == 0) caps[i] = acc;
		else for (int j = 0; j < 6; j++)
			if ((refs >> i & 1) != 0)
				caps[j] = acc;
		int dl = nl - l;
		if (dl != 0) {
			ItemStack[] nItems = new ItemStack[items.length + dl];
			System.arraycopy(items, 0, nItems, 0, s);
			for (int j = 0; j < l; j++) ItemFluidUtil.dropStack(getStackInSlot(j + s), m.getWorld(), m.getPos());
			Arrays.fill(nItems, s, s + nl, ItemStack.EMPTY);
			System.arraycopy(items, s + l, nItems, s + nl, items.length - s - l);
			for (int j = i + 1; j < caps.length; j++) {
				Access a = caps[j];
				if (a != null && a.s >= s && (p = m.components[j]) instanceof PartIOModule && ((PartIOModule)p).invType == IOType.BUFFER)
					a.s += dl;
			}
		}
	}

	@Override
	public void onCfgChange(ModularMachine m, int i, int cfg) {
		if (i < 6) {
			Part p = m.components[i + 6];
			if (!(p instanceof PartIOModule)) return;
			PartIOModule pi = (PartIOModule)p;
			if (!pi.hasItem || pi.invType != IOType.EXT_ACC) return;
			if (cfg < 0)
				caps[i] = null;
			else if (cfg < 6)
				caps[i] = caps[cfg];
			else if ((cfg -= 6) < size)
				caps[i] = limiterIn == null ? new Access(cfg, 1) : new RestrictedAccess(cfg);
		}
	}

	@Override
	public int getSlots() {
		return size;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		items[slot] = stack;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return items[slot];
	}

	/**
	 * @param port (>= 6) global resourceIO port id
	 * @return ItemHandler for port (either external inventory, internal buffer or null)
	 */
	public IItemHandler getExtInventory(int port) {
		int cfg = tile.getCfg(port);
		if (cfg < 0 || cfg >= 6) return null;
		IItemHandler acc = caps[cfg];
		if (acc != null) return acc;
		return Utils.neighborCapability(tile, EnumFacing.VALUES[cfg], CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	}

	private class Access extends AbstractInventory {

		int s;
		final int l;

		public Access(int s, int l) {
			this.s = s;
			this.l = l;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			items[slot + s] = stack;
		}

		@Override
		public int getSlots() {
			return l;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return items[slot + s];
		}

	}

	private class RestrictedAccess extends Access {

		public RestrictedAccess(int s) {
			super(s, 1);
		}

		@Override
		public int insertAm(int slot, ItemStack item) {
			return limiterIn.insertAmount(s, item);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (limiterOut.test(s))
				return super.extractItem(slot, amount, simulate);
			else return ItemStack.EMPTY;
		}

	}

}

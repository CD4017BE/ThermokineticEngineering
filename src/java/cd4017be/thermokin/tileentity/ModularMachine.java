package cd4017be.thermokin.tileentity;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import cd4017be.api.IBlockModule;
import cd4017be.lib.block.AdvancedBlock.ITilePlaceHarvest;
import cd4017be.lib.block.MultipartBlock.IModularTile;
import cd4017be.lib.capability.AbstractInventory;
import cd4017be.lib.property.PropertyByte;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.lib.util.Orientation;
import cd4017be.lib.util.Utils;
import cd4017be.thermokin.module.IPartListener;
import cd4017be.thermokin.module.Part;
import cd4017be.thermokin.module.Part.Type;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

public abstract class ModularMachine extends BaseTileEntity implements ITilePlaceHarvest, IModularTile {

	protected static final Random RAND = new Random();

	public final @Nonnull Part[] components = Utils.init(new Part[15], (i)-> Type.forSlot(i).NULL());
	public final byte[] durability = new byte[15];
	public Orientation orientation;

	public abstract IBlockModule[] getModules();
	public boolean isPartValid(int i, Part p) {
		Type t = p.type;
		return t.slotS <= i && i < t.slotE;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		byte[] dur = nbt.getByteArray("dur");
		System.arraycopy(dur, 0, durability, 0, Math.min(dur.length, durability.length));
		int[] comps = nbt.getIntArray("comp");
		if (comps.length >= 15)
			for (int i = 0; i < components.length; i++)
				components[i] = Part.getPart(Type.forSlot(i), comps[i]);
		int n = 0;
		for (IBlockModule m : getModules())
			m.readNBT(nbt, "M" + (n++));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		int n = 0;
		for (IBlockModule m : getModules())
			m.writeNBT(nbt, "M" + (n++));
		nbt.setByteArray("dur", durability);
		int[] comps = new int[components.length];
		for (int i = 0; i < comps.length; i++)
			comps[i] = components[i].id;
		nbt.setIntArray("comp", comps);
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
			setPart(i, Type.forSlot(i).NULL());
			return true;
		} else {
			durability[i] = (byte)d;
			return false;
		}
	}

	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) {
		NBTTagCompound nbt = item.getTagCompound();
		orientation = Orientation.fromFacing(entity.getHorizontalFacing().getOpposite());
		byte[] dur = nbt.getByteArray("dur");
		int[] comps = nbt.getIntArray("comp");
		if (dur.length >= 12 && comps.length >= 12)
			for (EnumFacing s : EnumFacing.values()) {
				int s0 = s.ordinal();
				int s1 = orientation.rotate(s).ordinal();
				durability[s1] = dur[s0];
				components[s1] = Part.getPart(Type.CASING, comps[s0]);
				durability[s1 + 6] = dur[s0 + 6];
				components[s1 + 6] = Part.getPart(Type.MODULE, comps[s0 + 6]);
			}
		for (int i = 12; i < comps.length && i < components.length; i++) {
			durability[i] = dur[i];
			components[i] = Part.getPart(Type.MAIN, comps[i]);
		}
		for (IBlockModule m : getModules())
			if (m instanceof IPartListener)
				((IPartListener)m).onPlaced(this, nbt);
	}

	@Override
	public List<ItemStack> dropItem(IBlockState state, int fortune) {
		NBTTagCompound nbt = new NBTTagCompound();
		byte[] dur = new byte[durability.length];
		int[] comps = new int[components.length];
		for (EnumFacing s : EnumFacing.values()) {
			int s0 = s.ordinal();
			int s1 = orientation.rotate(s).ordinal();
			dur[s0] = durability[s1];
			comps[s0] = components[s1].id;
			dur[s0 + 6] = durability[s1 + 6];
			comps[s0 + 6] = components[s1 + 6].id;
		}
		for (int i = 12; i < comps.length && i < components.length; i++) {
			dur[i] = durability[i];
			comps[i] = components[i].id;
		}
		nbt.setByteArray("dur", dur);
		nbt.setIntArray("comp", comps);
		List<ItemStack> list = makeDefaultDrops(nbt);
		for (IBlockModule m : getModules())
			if (m instanceof IPartListener)
				((IPartListener)m).addDrops(this, nbt, list);
		return list;
	}

	@Override
	public <T> T getModuleState(int m) {
		return PropertyByte.cast(components[m].modelId);
	}
	@Override
	public boolean isModulePresent(int m) {
		Part p = components[m];
		return p != Part.NULL_CASING && p != Part.NULL_MODULE && p != Part.NULL_MAIN;
	}

	public class PartInventory extends AbstractInventory {

		@Override
		public int getSlots() {
			return components.length;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			Part part = components[slot];
			if (part.item.getCount() == 0) return part.item;
			ItemStack item = part.item.copy();
			item.setItemDamage((Part.MAX_DUR - (durability[slot] & 0xff)) * item.getMaxDamage() / Part.MAX_DUR);
			return item;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			Part part = components[slot] = Part.getPart(stack);
			if (part.item.getCount() > 0) {
				int m = stack.getMaxDamage();
				durability[slot] = (byte) (m > 0 ? (m - stack.getItemDamage()) * Part.MAX_DUR / m : Part.MAX_DUR);
			}
		}

		@Override
		public int insertAm(int slot, ItemStack item) {
			Part p = Part.getPart(item);
			return isPartValid(slot, p) ? p.item.getCount() : 0;
		}

	}

}

package cd4017be.thermokin.tileentity;

import java.util.ArrayList;
import java.util.function.BiFunction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import cd4017be.thermokin.multiblock.ShaftComponent;
import cd4017be.thermokin.multiblock.ShaftPhysics;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.MultiblockTile;
import cd4017be.lib.util.Utils;

public class Shaft extends MultiblockTile<ShaftComponent, ShaftPhysics> implements IPipe {

	public static final ArrayList<BiFunction<ModTileEntity, ItemStack, ShaftComponent>> handlers = new ArrayList<BiFunction<ModTileEntity, ItemStack, ShaftComponent>>();//TODO add some shaft mount handler

	private ShaftComponent create(Shaft tile, ItemStack item) {
		if (item != null) {
			ShaftComponent newComp;
			for (BiFunction<ModTileEntity, ItemStack, ShaftComponent> h : handlers)
				if ((newComp = h.apply(this, item)) != null)
					return newComp;
		}
		return null;
	}

	private Cover cover;

	public Shaft() {
		this.comp = new ShaftComponent(this, 1000F);
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing dir, float X, float Y, float Z) {
		if (worldObj.isRemote) return true;
		if (cover != null) {
			if (player.isSneaking() && item == null) {
				this.dropStack(cover.item);
				cover = null;
				this.markUpdate();
				return true;
			}
			return false;
		}
		if (onClicked(player, hand, item)) return true;
		else if (item != null && !player.isSneaking() && (cover = Cover.create(item)) != null) {
			if (--item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
			this.markUpdate();
			return true;
		} else return false;
	}

	public boolean onClicked(EntityPlayer player, EnumHand hand, ItemStack item) {
		ShaftComponent newComp;
		if (player.isSneaking() && comp.type != null && item == null) {
			comp.onRemove();
			newComp = new ShaftComponent(this, 1000F);
		} else if (!player.isSneaking() && comp.type == null) {
			newComp = create(this, item);
			if (newComp == null) return false;
			player.setHeldItem(hand, --item.stackSize <= 0 ? null : item);
		} else return false;
		comp.network.exchangeComponent(comp, newComp);
		markUpdate();
		return true;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		comp.writeToNBT(nbt);
		if (cover != null) cover.write(nbt, "cover");
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		comp = ShaftComponent.readFromNBT(this, nbt);
		cover = Cover.read(nbt, "cover");
	}

	@Override
	public void handleUpdateTag(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		ShaftComponent nc = ShaftComponent.readFromNBT(this, nbt);
		if (comp.network == null) comp = nc; 
		else comp.network.exchangeComponent(comp, nc);
		cover = Cover.read(nbt, "cover");
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound nbt = pkt.getNbtCompound();
		if (nbt.hasKey("RotVel")) {
			comp.network.v = nbt.getFloat("RotVel");
		}
		if (nbt.hasKey("type")) {
			cover = Cover.read(nbt, "cover");
			NBTTagCompound tag = nbt.getCompoundTag("type");
			ItemStack type = tag.hasNoTags() ? null : ItemStack.loadItemStackFromNBT(tag);
			ShaftComponent nc;
			if (!ItemStack.areItemsEqual(type, comp.type) && (nc = create(this, type)) != null)
				comp.network.exchangeComponent(comp, nc);
			this.markUpdate();
		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		if (cover != null) cover.write(nbt, "cover");
		NBTTagCompound tag = new NBTTagCompound();
		nbt.setTag("type", comp.type != null ? comp.type.writeToNBT(tag) : tag);
		return new SPacketUpdateTileEntity(getPos(), -1, nbt);
	}

	@Override
	public void breakBlock() {
		super.breakBlock();
		if (cover != null) this.dropStack(cover.item);
		comp.onRemove();
	}

	@Override
	public int textureForSide(byte s) {
		if (s == -1 && worldObj.isRemote) this.checkRenderUpdate();
		if (s < 0 || s / 2 != this.getBlockMetadata()) return -1;
		TileEntity te = Utils.getTileOnSide(this, s);
		return te != null && te instanceof Shaft ? -2 : 0;
	}

	private void checkRenderUpdate() {
		int l = worldObj.getCombinedLight(pos, 0);
		if (l != lastLight) {
			lastLight = l;
			comp.network.model = null;
		}
	}

	@Override
	public Cover getCover() {
		return cover;
	}

	public ShaftPhysics physics() {
		return comp.network;
	}

	@SideOnly(Side.CLIENT)
	private int lastLight = 0;

}
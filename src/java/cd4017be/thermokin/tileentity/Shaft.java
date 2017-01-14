package cd4017be.thermokin.tileentity;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import cd4017be.api.Capabilities;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.multiblock.ShaftComponent;
import cd4017be.thermokin.multiblock.ShaftPhysics;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.MultiblockTile;
import cd4017be.lib.util.Utils;

public class Shaft extends MultiblockTile<ShaftComponent, ShaftPhysics> implements IPipe {

	public static final ArrayList<IShaftMountHandler> handlers = new ArrayList<IShaftMountHandler>();

	public interface IShaftMountHandler {
		public ShaftComponent create(Shaft tile, ItemStack item);
		public ShaftComponent create(Shaft tile, int id);
	}

	private Cover cover;

	public Shaft() {
		this.comp = new ShaftComponent(this, 1000F);
	}

	@Override
	public void update() {
		super.update();//TODO Add update for (comps implements ITickable) in super class
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
		ShaftComponent newComp = null;
		if (player.isSneaking() && item == null && comp.type != null) {
			comp.onRemove();
			newComp = new ShaftComponent(this, 1000F);
		} else if (!player.isSneaking() && item != null && comp.type == null) {
			for (IShaftMountHandler h : handlers)
				if ((newComp = h.create(this, item)) != null) {
					player.setHeldItem(hand, --item.stackSize <= 0 ? null : item);
					break;
				}
		} else return false;
		if (newComp == null) return false;
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
		cover = Cover.read(nbt, "cover");
		//TODO handle shaft update
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound nbt = pkt.getNbtCompound();
		if (nbt.hasKey("RotVel")) {
			comp.network.v = nbt.getFloat("RotVel");
		}
		if (nbt.hasKey("type")) {
			cover = Cover.read(nbt, "cover");
			//byte con = nbt.getByte("con");
			//if (con != shaft.con) {
			//	shaft.con = con;
			//	shaft.updateCon = true;
			//}
			//TODO handle shaft update
			this.markUpdate();
		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		if (cover != null) cover.write(nbt, "cover");
		nbt.setByte("type", comp.type);
		//nbt.setByte("con", shaft.con);
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

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing s) {
		//TODO Add capability check for (comps implements ICapabilityProvider) in super class
		if (cap == Capabilities.ELECTRIC_CAPABILITY) return energy != null;
		else return super.hasCapability(cap, s);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing s) {
		if (cap == Capabilities.ELECTRIC_CAPABILITY) return (T)energy;
		else return super.getCapability(cap, s);
	}

	@SideOnly(Side.CLIENT)
	private int lastLight = 0;

}

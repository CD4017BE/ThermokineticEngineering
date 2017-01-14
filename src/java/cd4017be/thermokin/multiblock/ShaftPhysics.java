package cd4017be.thermokin.multiblock;

import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.SharedNetwork;

public class ShaftPhysics extends SharedNetwork<ShaftComponent, ShaftPhysics> {

	/**
	 * set of connected mechanical components
	 */
	public final HashMap<Long, IKineticComp> connectors = new HashMap<Long, IKineticComp>();
	/**
	 * the "handle" radius at which components interact. 
	 * Used for calculating the effective rotation mass of shaft parts.
	 */
	public static final float R = (float)(1D / Math.PI);
	/**
	 * @field m [kg] the total effective rotation mass of the whole shaft structure
	 * @field v [m/s] the speed of the shaft
	 * @field s [m] the shaft's current position (1m = one full rotation)
	 */
	public float m = 0, v = 0, s = 0;
	private float diff = 1;
	
	public ShaftPhysics(ShaftComponent core) {
		super(core);
		m = core.m;
		pos = ((TileEntity)core.tile).getPos();
	}
	
	public ShaftPhysics(HashMap<Long, ShaftComponent> comps) {
		super(comps);
	}

	@Override
	public ShaftPhysics onSplit(HashMap<Long, ShaftComponent> comps) {
		ShaftPhysics network = new ShaftPhysics(comps);
		for (ShaftComponent comp : comps.values()) {
			for (byte i = 0, c = comp.getCon(); c != 0; c >>>= 1, i++)
				if ((c & 1) != 0) {
					long id = SharedNetwork.SidedPosUID(comp.getUID(), i);
					IKineticComp con = connectors.remove(id);
					if (con != null) network.connectors.put(id, con);
				}
			network.m += comp.m;
		}
		network.pos = pos;
		network.axis = axis;
		network.s = this.s;
		network.v = this.v;
		this.m -= network.m;
		model = null;
		return network;
	}

	@Override
	public void onMerged(ShaftPhysics network) {
		super.onMerged(network);
		this.v *= this.m;
		this.v += network.v * network.m;
		this.m += network.m;
		this.v /= this.m;
		this.connectors.putAll(network.connectors);
		model = null;
	}

	@Override
	public void remove(ShaftComponent comp) {
		if (this.components.containsKey(comp.getUID())) {
			this.m -= comp.m;
			for (byte i = 0, c = comp.getCon(); c != 0; c >>>= 1, i++)
				if ((c & 1) != 0) {
					IKineticComp con = this.connectors.remove(SharedNetwork.SidedPosUID(comp.getUID(), i));
					if (con != null) con.setShaft(null);
				}	
		}
		super.remove(comp);
		model = null;
	}

	/**
	 * add a component to the shafts mechanical structure
	 * @param comp
	 */
	public void addCon(ShaftComponent comp, IKineticComp con) {
		byte side = (byte)(con.getConSide()^1);
		con.setShaft(comp);
		connectors.put(SharedNetwork.SidedPosUID(comp.getUID(), side), con);
		comp.setCon(side, true);
	}
	
	@Override
	public void updateCompCon(ShaftComponent comp) {
		if (axis < 0) axis = (byte)((TileEntity)comp.tile).getBlockMetadata(); 
		IKineticComp con;
		super.updateCompCon(comp);
		for (byte i = 0; i < 6; i++) {
			ICapabilityProvider te = comp.tile.getTileOnSide(EnumFacing.VALUES[i]);
			if (te == null) continue;
			if (te instanceof IKineticComp && (con = (IKineticComp)te).getConSide() == (i^1) && comp.supports(con, i))
				this.addCon(comp, con);
		}
	}

	public void exchangeComponent(ShaftComponent original, ShaftComponent newOne) {
		float mass = newOne.m - original.m;
		if (mass > 0) v *= m / (m + mass);
		m += mass;
		long uid = original.getUID();
		components.remove(uid);
		newOne.network = this;
		newOne.setUID(uid);
	}

	@Override
	protected void updatePhysics() {
		float dt = 0.05F;
		float ds = v * dt, F = 0, a, E, v1;
		if (((TileEntity)core.tile).getWorld().isRemote) {
			s += ds;//just simulate constant rotation speed
			if (s > 1F) s -= Math.floor(s);
			return;
		}
		Iterator<IKineticComp> it = connectors.values().iterator();
		while (it.hasNext()) {//get the estimated total force of all components on the shaft
			IKineticComp comp = it.next();
			if (comp.valid()) F += comp.estimatedForce(ds);
			else {
				it.remove();
				if (comp.getShaft() != null) comp.getShaft().setCon(comp.getConSide() ^ 1, false);
			}
		}
		if (v == 0 && F <= 0) return; //can't slow down any further if already stopped
		a = F / m; //convert to acceleration
		v1 = (v + a * dt) * dt; //calculate estimated target velocity
		if (v1 < 0) {dt = -v / a; v1 = 0;} //if rotation would stop, only calculate till that point
		ds = 0.5F * a * dt * dt + v * dt; //now use the real distance moved
		E = 0.5F * m * v * v; //get the kinetic Energy of the shaft and add the work of all components to it
		for (IKineticComp comp : connectors.values()) E += comp.work(ds, v1);
		v = (float)Math.sqrt(2F * E / m); //convert back to speed
		if (Float.isNaN(v)) v = 0;
		s += ds; //move the shaft forward
		if (s > 1F) s -= Math.floor(s);
		if (v == 0 || (diff += dt) >= 1F) updateClient();
	}
	
	private void updateClient() {
		diff = 0;
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setFloat("RotVel", v);
		nbt.setFloat("RotPos", s);
		ModTileEntity tile = (ModTileEntity)core.tile;
		BlockPos pos = tile.getPos();
		((WorldServer)tile.getWorld()).getMinecraftServer().getPlayerList().sendToAllNearExcept(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 64D, tile.dimensionId, new SPacketUpdateTileEntity(pos, -1, nbt));
	}

	public interface IKineticComp {
		public byte getConSide();
		public ShaftComponent getShaft();
		public void setShaft(ShaftComponent shaft);
		public boolean valid();
		/**
		 * calculate the estimated force of this part on the shaft. 
		 * This value doesn't need to be exact, but it shouldn't be greater than the actual force added during work().
		 * Otherwise the shaft could probably get negative kinetic energy.
		 * @param ds [m] the distance the shaft will move during this calculation tick
		 * @return [kg*m/s²] the force on the shaft (positive values accelerate, negative slow down)
		 */
		public float estimatedForce(float ds);
		/**
		 * update the mechanical physics of this connected part
		 * @param ds [m] the distance the shaft will move during this calculation tick
		 * @param v [20m/s] the speed that would result from force
		 * @return [J] the amount of work added to the shaft
		 */
		public float work(float ds, float v);
	}
	
	public interface IShaft {
		public ShaftComponent getShaft();
	}

	//client rendering stuff

	/**	used in combination with {@link TickHandler.tick} to check whether this has already been rendered */
	@SideOnly(Side.CLIENT)
	public int lastRendered = -1;
	/** cached structure model for fast rendering */
	public int[] model = null;
	/** reference location for cached model */
	public BlockPos pos;
	/** structure orientation */
	public byte axis = -1;
}

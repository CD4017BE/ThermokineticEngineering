package cd4017be.api.heat;

import cd4017be.api.IBlockModule;
import cd4017be.api.registry.Environment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Simple implementation of {@link IHeatAccess} and {@link IHeatReservoir} with constant heat capacity.<br>
 * Warning: temperature must be initialized with {@link #initialize initialize()}, {@link #readNBT readNBT()} or setting {@link #T} directly, otherwise it will be NaN.
 * @author CD4017BE
 */
public class SimpleHeatReservoir implements IHeatAccess, IBlockModule {

	/**[J/K] heat capacity */
	public final float C;
	/**[K*t/J] heat conduction resistance */
	protected float R;
	/**[K] current temperature */
	public float T = Float.NaN;
	/**currently connected heat conductor */
	public HeatConductor link;

	/**
	 * creates a SimpleHeatReservoir with fixed heat capacity and initial heat conduction resistance
	 * @param C [J/K] heat capacity
	 * @param R [K*t/J] heat conduction resistance
	 */
	public SimpleHeatReservoir(float C, float R) {
		this.C = C;
		this.setR(R);
	}

	/**@param R [K*t/J] new value to set the heat conduction resistance to */
	public void setR(float R) {
		this.R = Math.max(R, 1F / C);
		if (link != null) link.updateHeatCond();
	}

	@Override
	public float T() {
		return T;
	}

	@Override
	public void addHeat(float dQ) {
		T += dQ / C;
	}

	@Override
	public HeatConductor getLink() {
		return link;
	}

	@Override
	public void setLink(HeatConductor c) {
		if (link != null && c != null && link != c) link.disconnect();
		link = c;
	}

	@Override
	public float R() {
		return R;
	}

	@Override
	public void readNBT(NBTTagCompound nbt, String k, TileEntity tile) {
		this.T = nbt.getFloat(k + "T");
	}

	@Override
	public void writeNBT(NBTTagCompound nbt, String k) {
		nbt.setFloat(k + "T", T);
	}

	@Override
	public void initialize(TileEntity te) {
		if (Float.isNaN(T))
			T = Environment.getEnvFor(te.getWorld()).getTemp(te.getWorld(), te.getPos());
	}

	@Override
	public void invalidate() {
		if (link != null) link.disconnect();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return (T)this;
	}

	@Override
	public boolean supportsCapability(Capability<?> cap) {
		return cap == IHeatAccess.CAPABILITY_HEAT_ACCESS;
	}

}

package cd4017be.thermokin.multiblock;

import net.minecraft.nbt.NBTTagCompound;
import cd4017be.api.IAbstractTile;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.physics.LiquidState;
import cd4017be.thermokin.physics.Substance;

/**
 * This represents a special component of a LiquidPhysics network, that stores liquid.
 * The liquid pressure is defined via a connected gas reservoir that acts like as pneumatic spring.
 * @author CD4017BE
 */
public class LiquidContainer extends LiquidComponent {

	public final LiquidState liquid;
	protected final GasState bufferGas;
	protected final GasContainer bufferGasC;

	/**
	 * Creates a LiquidContainer using a constant GasState for fixed pressure (usually normal air pressure)
	 * @param tile the TileEntity owning this
	 * @param Vmax the maximum liquid capacity
	 * @param buffer the gas used to define pressure
	 */
	public LiquidContainer(IAbstractTile tile, double Vmax, GasState buffer) {
		super(tile);
		this.liquid = new LiquidState(null, Vmax, 0, 0);
		this.bufferGas = buffer;
		this.bufferGasC = null;
	}

	/**
	 * Creates a LiquidContainer that is connected to a real GasPhysics network
	 * @param tile the TileEntity owning this
	 * @param Vmax the maximum liquid capacity
	 * @param buffer the gas reservoir this is connected to
	 */
	public LiquidContainer(IAbstractTile tile, double Vmax, GasContainer buffer) {
		super(tile);
		this.liquid = new LiquidState(null, Vmax, 0, 0);
		this.bufferGas = null;
		this.bufferGasC = buffer;
	}

	public GasState getBufferGas() {
		return bufferGasC != null ? bufferGasC.network.gas : bufferGas;
	}

	public LiquidState getLiquid() {
		return new LiquidState(network.content.type, liquid.Vmax, liquid.V, liquid.T);
	}

	public void setLiquid(LiquidState liq) {
		double dV = liq.V - liquid.V;
		liquid.T = liq.T;
		if (dV != 0) {
			liquid.V = dV < 0 && liq.V < LiquidState.NULL ? 0 : liq.V;
			liquid.type = liq.type;
			network.content.V += dV;
			if (network.content.V < LiquidState.NULL) {network.content.type = null; network.content.V = 0;}
			else if (network.content.type == null) network.content.type = liq.type;
			if (bufferGas != null) bufferGas.adiabat(bufferGas.V - dV);
		}
	}

	public static LiquidContainer readFromNBT(IAbstractTile tile, NBTTagCompound nbt, String k, double Vliq, double Vgas, double P0) {
		LiquidContainer pipe = new LiquidContainer(tile, Vliq, new GasState(Substance.Default, Vgas, P0, Vgas));
		new LiquidPhysics(pipe);
		pipe.setLiquid(LiquidState.readFromNBT(nbt, k, Vliq));
		return pipe;
	}

	public void writeToNBT(NBTTagCompound nbt, String k) {
		liquid.writeToNBT(nbt, k);
	}

}

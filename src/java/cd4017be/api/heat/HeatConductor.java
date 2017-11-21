package cd4017be.api.heat;

import javax.annotation.Nonnull;

import cd4017be.api.CommutativeTickHandler;
import cd4017be.api.CommutativeTickHandler.ICommutativeTickable;

/**
 * Used to simulate heat exchange between the heat reservoirs behind two {@link IHeatAccess} objects.<br>
 * Automatically registers itself for physics updates upon construction, so no extra updating required.<br>
 * <b>Use this server side only!</b>
 * @author CD4017BE
 */
public class HeatConductor implements ICommutativeTickable {

	@Nonnull
	public final IHeatAccess A, B;
	/** [K*t/J] heat resistance */
	private float R;
	/** [J/t] heat flow A -> B */
	private float dQ;

	/**
	 * creates a HeatConductor that connects two heat reservoirs with each other
	 * @param A access to the one reservoir
	 * @param B access to the other reservoir
	 */
	public HeatConductor(@Nonnull IHeatAccess A, @Nonnull IHeatAccess B) {
		this.A = A;
		this.B = B;
		A.setLink(this);
		B.setLink(this);
		updateHeatCond();
		CommutativeTickHandler.register(this);
	}

	/**
	 * breaks this connection <br>
	 * should be called by {@link IHeatAccess} providers when their heat reservoir becomes no longer accessible
	 */
	public void disconnect() {
		A.setLink(null);
		B.setLink(null);
		CommutativeTickHandler.invalidate(this);
	}

	/**
	 * recalculates the total heat conductivity of this connection
	 */
	public void updateHeatCond() {
		this.R = A.R() + B.R();
	}

	/**@return [J/t] amount of thermal energy transmitted A->B last tick (for calorimetric measurement)*/
	public float dQ() {
		return dQ;
	}

	@Override
	public void prepareTick() {
		dQ = (A.T() - B.T()) / R;
	}

	@Override
	public void runTick() {
		A.addHeat(-dQ);
		B.addHeat(dQ);
	}

}

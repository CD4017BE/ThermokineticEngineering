package cd4017be.api.heat;

/**
 * Implementation of a HeatReservoir that never changes it's temperature and thus acts as unlimited heat source or sink.<br>
 * Useful to handle heat exchange with the environment.
 * @author CD4017BE
 */
public class InfiniteReservoir implements IHeatAccess {

	private HeatConductor link;
	public float T;
	private float R;

	/**
	 * @param T [K] constant Temperature
	 * @param R [K*t/J] initial heat conduction resistance
	 */
	public InfiniteReservoir(float T, float R) {
		this.T = T;
		this.R = R;
	}

	/**@param R [K*t/J] new value to set the heat conduction resistance to */
	public void setR(float R) {
		this.R = R;
		if (link != null) link.updateHeatCond();
	}

	@Override
	public float T() {
		return T;
	}

	@Override
	public void addHeat(float dQ) {
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

}

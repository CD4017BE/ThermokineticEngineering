package cd4017be.api.heat;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Machines may provide implementations of this interface as capability to allow interaction with its heat reservoir. <br>
 * Use {@link HeatConductor} to perform the actual heat exchange between machines.
 * @author CD4017BE
 */
public interface IHeatAccess extends IHeatReservoir {

	/**@return the connection of this reservoir */
	HeatConductor getLink();

	/**@param c the connection to apply on this reservoir */
	void setLink(HeatConductor c);

	/**@return [K*t/J] heat conduction resistance <br>
	 * Implementors should call {@code getLink().updateHeatCond()} whenever they change this value */
	float R();

	@CapabilityInject(IHeatAccess.class)
	public static final Capability<IHeatAccess> CAPABILITY_HEAT_ACCESS = null;

}

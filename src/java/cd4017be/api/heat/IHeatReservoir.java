package cd4017be.api.heat;

/**
 * 
 * @author CD4017BE
 */
public interface IHeatReservoir {

	/**@return [K] current temperature */
	float T();

	/**@param dQ [J] amount of thermal energy to add to this reservoir */
	void addHeat(float dQ);

}

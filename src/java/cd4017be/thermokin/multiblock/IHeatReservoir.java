package cd4017be.thermokin.multiblock;

import cd4017be.lib.util.ICachableInstance;

public interface IHeatReservoir extends ICachableInstance{
	public float T();
	public float C();
	public float R();
	public void addHeat(float dQ);
}

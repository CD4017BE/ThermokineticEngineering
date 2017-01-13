package cd4017be.thermokin.physics;

import cd4017be.thermokin.recipe.Substances.Environment;
import net.minecraft.block.state.IBlockState;

/**
 * This interface is meant to be implemented by Blocks to allow them defining their own heat resistance values
 * @author CD4017BE
 */
public interface IHeatBlock {

	/**
	 * gets the heat resistance for this block used as cover
	 * @param state used for state dependent handling
	 * @return [K/W] heat resistance
	 */
	public float getHeatResistance(IBlockState state);

	/**
	 * get the heat resistance for this as adjacent block in the world
	 * @param state used for state dependent handling
	 * @param env the worlds environment instance
	 * @return [K/W] heat resistance
	 */
	public float getHeatResistance(IBlockState state, Environment env);

}

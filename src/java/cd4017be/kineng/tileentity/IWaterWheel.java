package cd4017be.kineng.tileentity;

import cd4017be.lib.util.ICachableInstance;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;

/** 
 * @author CD4017BE */
public interface IWaterWheel extends ICachableInstance {

	/**@param vsq [m²/s²] input square velocity
	 * @param liquid
	 * @return [m²/s²] output square velocity */
	double passLiquid(double vsq, FluidStack liquid, EnumFacing dir);

}

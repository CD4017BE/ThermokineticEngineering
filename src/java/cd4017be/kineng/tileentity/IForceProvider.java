package cd4017be.kineng.tileentity;

import cd4017be.kineng.physics.DynamicForce;
import net.minecraft.util.EnumFacing;

/** 
 * @author CD4017BE */
public interface IForceProvider {

	/**@param link the shaft torque handle to connect with
	 * @param side the side from which to connect
	 * @return the dynamic force handler or null if connection not possible */
	DynamicForce connect(IKineticLink link, EnumFacing side);

}

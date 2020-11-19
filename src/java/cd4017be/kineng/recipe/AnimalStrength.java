package cd4017be.kineng.recipe;

import cd4017be.kineng.tileentity.ManualPower;
import cd4017be.lib.script.obj.Error;
import cd4017be.lib.script.obj.IOperand;
import cd4017be.lib.script.obj.Vector;
import cd4017be.math.cplx.CplxF;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;


/** 
 * @author CD4017BE */
public class AnimalStrength implements IOperand {

	@Override
	public boolean asBool() throws Error {
		return true;
	}

	@Override
	public Object value() {
		return this;
	}

	@Override
	public void put(IOperand idx, IOperand val) {
		String name = idx.value().toString();
		Class<? extends Entity> ec = "player".equals(name) ? EntityPlayer.class
			: EntityList.getClass(new ResourceLocation(name));
		if (ec == null || !(val instanceof Vector)) return;
		double[] v = ((Vector)val).value;
		ManualPower.ENTITY_STRENGTH.put(ec, CplxF.C_((float)v[0], (float)v[1]));
	}

}

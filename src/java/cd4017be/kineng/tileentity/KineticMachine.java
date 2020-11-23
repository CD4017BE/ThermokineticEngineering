package cd4017be.kineng.tileentity;

import cd4017be.kineng.block.BlockRotaryTool;
import cd4017be.kineng.physics.*;

/** 
 * @author CD4017BE */
public abstract class KineticMachine extends ShaftPart {

	protected ForceCon con;

	@Override
	public double setShaft(ShaftAxis shaft, double v0) {
		if (con == null && shaft != null) {
			BlockRotaryTool block = block();
			con = new ForceCon(this, block.r);
			con.maxF = block.maxF;
			con.link(createForce(block));
		}
		if (con != null) con.setShaft(shaft);
		return super.setShaft(shaft, vSave);
	}

	protected abstract DynamicForce createForce(BlockRotaryTool block);

	@SuppressWarnings("unchecked")
	protected <T extends DynamicForce> T getForce() {
		return (T)con.force;
	}

}

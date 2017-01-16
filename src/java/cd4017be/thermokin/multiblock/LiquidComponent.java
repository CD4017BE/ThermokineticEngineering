package cd4017be.thermokin.multiblock;

import net.minecraftforge.common.capabilities.Capability;
import cd4017be.api.IAbstractTile;
import cd4017be.lib.templates.MultiblockComp;
import cd4017be.thermokin.Objects;

/**
 * This represents a basic component of a LiquidPhysics network.
 * It has no special properties except connecting other components together.
 * @author CD4017BE
 */
public class LiquidComponent extends MultiblockComp<LiquidComponent, LiquidPhysics> {

	public LiquidComponent(IAbstractTile tile) {
		super(tile);
	}

	public void setUID(long uid) {
		super.setUID(uid);
		if (network == null) new LiquidPhysics(this);
	}

	@Override
	public Capability<LiquidComponent> getCap() {
		return Objects.LIQUID_CAP;
	}

}

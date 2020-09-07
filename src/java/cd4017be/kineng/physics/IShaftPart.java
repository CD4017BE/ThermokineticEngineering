package cd4017be.kineng.physics;

import cd4017be.kineng.physics.ForceFlow.OverloadHandler;
import net.minecraft.util.EnumFacing.Axis;

/** @author CD4017BE */
public interface IShaftPart extends OverloadHandler {

	/** Note: Implementors should always call {@link ShaftAxis#updateMass()} after changing this.
	 * @return [kg*m] this part's moment of inertia. */
	double J();

	/** @return [N*m] max torque allowed on this part before ... something happens. */
	double maxTorque();

	/** @param dir towards positive axis
	 * @return neighboring IShaftPart on the given side. */
	IShaftPart next(boolean dir);

	/** @return the Shaft this belongs to */
	ShaftAxis getShaft();

	/** Note: Implementors should always call {@link #setShaft(ShaftAxis, double)} before changing
	 * {@link #getShaft()} and also call {@link Connection#setShaft(ShaftAxis)} on their
	 * connectors (even if shaft didn't change).
	 * @param shaft the new shaft to assign
	 * @return [r/s] retained angular velocity */
	double setShaft(ShaftAxis shaft);

	/** Helper method to implement {@link #setShaft(ShaftAxis)}
	 * @param shaft the new shaft to assign
	 * @param ω0 default initial angular velocity
	 * @return retained shaft velocity */
	default double setShaft(ShaftAxis shaft, double ω0) {
		ShaftAxis old = getShaft();
		if (old == null) return ω0;
		if(shaft != old) old.markInvalid();
		return old.ω();
	}

	/** @return the part's orientation axis */
	Axis axis();

	/** Connects this with its neighboring IShaftParts to form a Shaft. */
	default void connect(boolean client) {
		if(invalid() || getShaft() != null) return;
		ShaftAxis shaft = null;
		IShaftPart part = this, part1;
		do {
			if((shaft = part.getShaft()) != null) break;
			part = part.next(true);
		} while(part != null);
		part = this;
		while((part1 = part.next(false)) != null) {
			part = part1;
			if(shaft == null) shaft = part.getShaft();
		}
		if(shaft == null) shaft = new ShaftAxis(client);
		else {
			if (shaft.struct.client() != client) throw new IllegalStateException("mixed server and client structures!");
			if(shaft.invalid) shaft.rescan();
			shaft.parts.clear();
		}
		do {
			shaft.parts.add(part);
			part = part.next(true);
		} while(part != null);
		shaft.refreshParts();
	}

	/** Called server side by the ShaftStructure to make this part synchronize shaft speed to the client. */
	void syncToClient();

	String model();
	String capModel(boolean end);

	/** whether this part is invalid, usually reflects tileEntityInvalid */
	boolean invalid();
}

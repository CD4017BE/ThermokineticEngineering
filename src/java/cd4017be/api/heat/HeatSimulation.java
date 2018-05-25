package cd4017be.api.heat;

import java.util.Arrays;

import cd4017be.api.CommutativeTickHandler.ICommutativeTickable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * @author CD4017BE
 *
 */
public class HeatSimulation implements ICommutativeTickable {

	public static HeatSimulation instance;

	public static void register() {
		if (instance == null) {
			instance = new HeatSimulation();
			MinecraftForge.EVENT_BUS.register(instance);
		}
	}

	private HeatConductor[] conductors = new HeatConductor[8];
	private int count;

	public void add(HeatConductor c) {
		if (c.id >= 0) return;
		if (count == conductors.length) conductors = Arrays.copyOf(conductors, conductors.length << 1);
		conductors[count] = c;
		c.id = count++;
	}

	public void remove(HeatConductor c) {
		if (c.id < 0) return;
		int i = c.id;
		if (conductors[i] != c) throw new IllegalStateException("HeatConductor not found");
		HeatConductor c_ = conductors[--count];
		conductors[c_.id = i] = c_;
		conductors[count] = null;
		c.id = -1;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void tick(TickEvent.ServerTickEvent ev) {
		if (ev.phase != TickEvent.Phase.END || count == 0) return;
		prepareTick();
		runTick();
	}

	@Override
	public void prepareTick() {
		for (int i = 0; i < count; i++) {
			HeatConductor c = conductors[i];
			c.dQ = (c.A.T() - c.B.T()) / c.R;
		}
	}

	@Override
	public void runTick() {
		for (int i = 0; i < count; i++) {
			HeatConductor c = conductors[i];
			float dQ = c.dQ;
			c.A.addHeat(-dQ);
			c.B.addHeat(dQ);
		}
	}

}

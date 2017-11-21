package cd4017be.api;

import java.util.ArrayList;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * handles updating of {@link ICommutativeTickable} objects
 * @author CD4017BE
 */
public class CommutativeTickHandler {

	private final ArrayList<ICommutativeTickable> toUpdate, toRemove;

	private CommutativeTickHandler() {
		toUpdate = new ArrayList<ICommutativeTickable>();
		toRemove = new ArrayList<ICommutativeTickable>();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent ev) {
		if (ev.phase != TickEvent.Phase.END) return;
		if (!toRemove.isEmpty()) {
			toUpdate.removeAll(toRemove);
			toRemove.clear();
		}
		if (!toUpdate.isEmpty()) {
			//indexed iteration so elements can be added without ConcurrentModificationException
			for (int i = 0; i < toUpdate.size(); i++)
				toUpdate.get(i).prepareTick();
			for (ICommutativeTickable c : toUpdate)
				c.runTick();
		}
	}

	/**
	 * Used for commutative simulation processes that should run independent of the order in which individual nodes are updated.
	 */
	public static interface ICommutativeTickable {
		/**
		 * called at the end of each server tick <br>
		 * This is meant for preparation calculations only and should not manipulate any state yet that other mechanics may access.<br>
		 * It's allowed to register other tickables during this call, these will then also have {@code prepareTick()} called later on within this update cycle.
		 */
		public void prepareTick();
		/**
		 * called just after {@link #prepareTick()} has been called on <b> all registered </b> IsotropicTickable objects.<br>
		 * This is meant to actually simulate the physics but should better <b> not </b> do things that may instantly cause structural changes to the world (breaking/placing blocks, loading/unloading chunks, ...).
		 */
		public void runTick();
	}

	private static CommutativeTickHandler instance;

	/**
	 * registers a tickable to make it receive update ticks. <br>
	 * This should be called <b>server side only</b> and <b> never more than once </b> for the same object.
	 * @param tickable
	 */
	public static void register(ICommutativeTickable tickable) {
		if (instance == null) instance = new CommutativeTickHandler();
		instance.toUpdate.add(tickable);
	}

	/**
	 * removes a tickable so it no longer receives update ticks. <br>
	 * Make sure this is called when an object becomes invalid.
	 * @param tickable
	 */
	public static void invalidate(ICommutativeTickable tickable) {
		if (instance == null) return;
		instance.toRemove.add(tickable);
	}

	/**
	 * used to cancel all remaining updates in case of a server unload
	 */
	public static void cancelAll() {
		if (instance == null) return;
		instance.toRemove.clear();
		instance.toUpdate.clear();
	}

}

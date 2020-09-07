package cd4017be.kineng.physics;

import java.util.ArrayList;
import static cd4017be.kineng.Main.LOG;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

/** @author CD4017BE */
public class Ticking {

	/** [s] time passing per tick */
	public static final double Δt = 0.05;
	public static final Ticking SERVER = new Ticking(false);
	private static Ticking CLIENT;
	static boolean DEBUG = true;

	public static void init() {
		MinecraftForge.EVENT_BUS.register(SERVER);
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
			CLIENT = new Ticking(true);
	}

	public static Ticking of(TileEntity te) {
		return te.getWorld().isRemote ? CLIENT : SERVER;
	}

	public static Ticking of(boolean client) {
		return client ? CLIENT : SERVER;
	}

	public final boolean client;
	public final ArrayList<ShaftStructure> structs = new ArrayList<>();
	public final ArrayList<IShaftPart> updateCon = new ArrayList<>();
	/** "thread local" reusable general purpose list to avoid unnecessary object allocation */
	public final ArrayList<ShaftAxis> AXIS_STACK = new ArrayList<>();

	private Ticking(boolean client) {
		this.client = client;
	}

	@SubscribeEvent
	public void onTick(TickEvent event) {
		if (event.phase != Phase.START) return;
		if (event.side == Side.CLIENT)
			CLIENT.tickShafts();
		else this.tickShafts();
	}

	public void tickShafts() {
		if (!updateCon.isEmpty()) {
			int l = structs.size();
			for (IShaftPart part : updateCon)
				part.connect(client);
			if (DEBUG) LOG.info("shaft part updates: {}, structures: {} -> {}", updateCon.size(), l, structs.size());
			updateCon.clear();
		}
		for (int i = 0; i < structs.size(); i++) {
			ShaftStructure struct = structs.get(i);
			struct.tick();
			if (struct.axes.isEmpty()) {
				if (DEBUG) LOG.info("removing {}", struct);
				struct.registered = false;
				struct = structs.remove(structs.size() - 1);
				if (i >= structs.size()) break;
				structs.set(i, struct);
			}
		}
	}

}

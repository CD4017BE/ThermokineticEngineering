package cd4017be.kineng.physics;

import java.util.ArrayList;
import java.util.Random;
import static cd4017be.kineng.Main.LOG;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
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
	public final ArrayList<ShaftStructure> updateStruct = new ArrayList<>();
	public final ArrayList<IShaftPart> updateCon = new ArrayList<>();
	public static final ArrayList<OverloadHandler> overloads = new ArrayList<>();
	/** "thread local" reusable general purpose list to avoid unnecessary object allocation */
	public final ArrayList<ShaftAxis> AXIS_STACK = new ArrayList<>();

	private Ticking(boolean client) {
		this.client = client;
	}

	@SubscribeEvent
	public void onTick(TickEvent event) {
		if (event.phase != Phase.START) return;
		switch(event.type) {
		case SERVER:
			this.updateShafts();
			this.tickServer();
			break;
		case CLIENT:
			CLIENT.updateShafts();
			tickClient(CLIENT.structs);
			break;
		default:
		}
	}

	/** shaft structure maintenance code */
	private void updateShafts() {
		if (!updateCon.isEmpty()) {
			int l = structs.size();
			for (IShaftPart part : updateCon)
				part.connect(client);
			if (DEBUG) LOG.info("shaft part updates: {}, structures: {} -> {}", updateCon.size(), l, structs.size());
			updateCon.clear();
		}
		if (!updateStruct.isEmpty()) {
			int l = structs.size();
			for (ShaftStructure struct : updateStruct) {
				struct.update();
			}
			if (DEBUG) LOG.info("structure updates: {}, structures: {} -> {}", updateStruct.size(), l, structs.size());
			updateStruct.clear();
			for (int i = 0; i < structs.size(); i++) {
				ShaftStructure struct = structs.get(i);
				if (struct.isEmpty()) {
					if (DEBUG) LOG.info("removing {}", struct.toString());
					struct = structs.remove(structs.size() - 1);
					if (i >= structs.size()) break;
					structs.set(i--, struct);
				}
			}
		}
	}

	public static final Random RAND = new Random();

	private static void tickClient(ArrayList<ShaftStructure> structs) {
		for (ShaftStructure struct : structs)
			struct.tickClient();
	}

	private void tickServer() {
		int shift = RAND.nextInt(16);
		for (int i = 0, l = structs.size(); i < l; i++)
			structs.get(i).tickServer((i + shift & 15) == 0);
		if (!overloads.isEmpty()) {
			for (OverloadHandler part : overloads)
				part.handleOverload();
			overloads.clear();
		}
	}

	/**
	 * 
	 */
	public static void clear() {
		LOG.info(
			"Server shutdown: canceled {} destructions, {} part updates, {} structure updates and removed {} structures.",
			overloads.size(), SERVER.updateCon.size(), SERVER.updateStruct.size(), SERVER.structs.size()
		);
		overloads.clear();
		SERVER.structs.clear();
		SERVER.updateCon.clear();
		SERVER.updateStruct.clear();
		SERVER.AXIS_STACK.clear();
	}

	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event) {
		if (event.getWorld().isRemote && CLIENT != null) {
			LOG.info(
				"Client shutdown: canceled {} part updates, {} structure updates and removed {} structures.",
				CLIENT.updateCon.size(), CLIENT.updateStruct.size(), CLIENT.structs.size()
			);
			CLIENT.structs.clear();
			CLIENT.updateCon.clear();
			CLIENT.updateStruct.clear();
			CLIENT.AXIS_STACK.clear();
		}
	}

}

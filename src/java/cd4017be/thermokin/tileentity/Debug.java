package cd4017be.thermokin.tileentity;

import java.io.IOException;

import cd4017be.api.IBlockModule;
import cd4017be.api.heat.HeatConductor;
import cd4017be.api.heat.IHeatAccess;
import cd4017be.lib.BlockGuiHandler.ClientPacketReceiver;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.block.AdvancedBlock.INeighborAwareTile;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.module.HeatModule;
import cd4017be.thermokin.module.Layout;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;


/**
 * @author CD4017BE
 *
 */
public class Debug extends ModularMachine implements ITickable, IGuiData, ClientPacketReceiver, INeighborAwareTile {

	final HeatModule heat;
	public float fixT, Q, envT;
	public float[] dQ = new float[8];
	public byte connect;

	public Debug() {
		heat = new HeatModule(10000F);
	}

	@Override
	public Layout getLayout() {
		return Objects.ovenL;
	}

	@Override
	public IBlockModule[] getModules() {
		return new IBlockModule[] {heat};
	}

	@Override
	public void update() {
		if (world.isRemote) return;
		heat.update();
		float dQ = (heat.T - fixT) * heat.C;
		heat.T = fixT;
		Q += dQ;
		this.dQ[7] = dQ;
		HeatConductor hc = heat.getLink();
		this.dQ[6] = hc == null ? Float.NaN : -hc.dQ();
		for (EnumFacing s : EnumFacing.values()) {
			IHeatAccess acc = heat.getCapability(IHeatAccess.CAPABILITY_HEAT_ACCESS, s);
			dQ = Float.NaN;
			if (acc != null && (hc = acc.getLink()) != null)
				dQ = hc.A == acc ? -hc.dQ() : hc.dQ();
			this.dQ[s.ordinal()] = dQ;
		}
	}

	@Override
	public void initContainer(DataContainer container) {
	}

	@Override
	public int[] getSyncVariables() {
		int[] data = new int[11];
		for (int i = 0; i < dQ.length; i++) data[i] = Float.floatToIntBits(dQ[i]);
		data[8] = Float.floatToIntBits(Q);
		data[9] = Float.floatToIntBits(fixT);
		data[10] = Float.floatToIntBits(heat.envT());
		return data;
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 8: Q = Float.intBitsToFloat(v); break;
		case 9: fixT = Float.intBitsToFloat(v); break;
		case 10: envT = Float.intBitsToFloat(v); break;
		default: if (i < dQ.length) dQ[i] = Float.intBitsToFloat(v);
		}
	}

	@Override
	public boolean detectAndSendChanges(DataContainer container, PacketBuffer dos) {
		return false;
	}

	@Override
	public void updateClientChanges(DataContainer container, PacketBuffer dis) {
	}

	@Override
	public void onPacketFromClient(PacketBuffer data, EntityPlayer sender) throws IOException {
		byte cmd = data.readByte();
		switch(cmd) {
		case 0: fixT = data.readFloat(); break;
		case 1: Q = 0; break;
		}
	}

	@Override
	public void neighborBlockChange(Block b, BlockPos src) {
		if (world.isRemote) return;
		heat.markUpdate();
	}

	@Override
	public void neighborTileChange(TileEntity te, EnumFacing side) {
		heat.markUpdate();
	}

}

package cd4017be.thermokin.tileentity;

import java.io.IOException;
import java.util.List;

import cd4017be.lib.BlockGuiHandler.ClientPacketReceiver;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.block.AdvancedBlock.ITilePlaceHarvest;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.capability.BasicInventory;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.lib.util.Utils;
import cd4017be.thermokin.module.IMachineData;
import cd4017be.thermokin.module.Layout;
import cd4017be.thermokin.module.Part;
import cd4017be.thermokin.module.IMachineData.IMachineAccess;
import cd4017be.thermokin.module.Part.Type;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.SlotItemHandler;

/**
 * 
 * @author cd4017be
 */
public class Assembler extends BaseTileEntity implements ITilePlaceHarvest, IMachineData, IMachineAccess, IGuiData, ClientPacketReceiver {

	private BasicInventory inv = new BasicInventory(16);
	private Part[] parts = Utils.init(new Part[15], (i)-> Type.forSlot(i).NULL());
	private Layout layout = Layout.NULL;
	/**bits[0-23 6*4]: module settings (slot * value), bits[24-60 12*3]: resource settings (id * value) */
	private long cfg;
	/**0: invalid recipe, 1:missing parts, 2:complete */
	private int status;

	public Assembler() {
		inv.onModify = this::updateItem;
		inv.restriction = this::insert;
	}

	private void updateStatus() {
		status = layout == Layout.NULL ? 0 : layout.isComplete(parts) ? 2 : 1;
	}

	private void updateItem(ItemStack stack, int slot) {
		if (slot == 15 || stack.isItemEqual(inv.items[slot])) return;
		Part p = Part.getPart(stack);
		parts[slot] = p == Part.NULL_MAIN ? Type.forSlot(slot).NULL() : p;
		if (world.isRemote) return;
		if (slot >= 12) {
			inv.items[slot] = stack;
			layout = Layout.fromRecipe(inv.items[12], inv.items[13], inv.items[14]);
		}
		updateStatus();
	}

	private int insert(int slot, ItemStack stack) {
		if (slot != 15 && inv.items[slot].isEmpty()) {
			Part p = Part.getPart(stack);
			if (slot < p.type.slotS || slot >= p.type.slotE)
				return 0;
		}
		return BasicInventory.insertAmount(slot, stack);
	}

	private void craft() {
		if (status != 2) return;
		ItemStack item = layout.getResult(parts, inv.items, cfg);
		if (item.isEmpty() || !inv.insertItem(15, item, false).isEmpty()) return;
		boolean update = false;
		for (int i = 0; i < 15; i++) {
			item = inv.items[i];
			int n = item.getCount();
			if (n > 0) {
				item.setCount(n - 1);
				update |= n == 1 && i >= 12;
			}
		}
		if (update) layout = Layout.fromRecipe(inv.items[12], inv.items[13], inv.items[14]);
		updateStatus();
	}

	@Override
	public Part getPart(int i) {
		return parts[i];
	}

	@Override
	public Layout getLayout() {
		return layout;
	}

	@Override
	public int getCfg(int i) {
		return (int)(i < 6 ? cfg >> (i * 4) & 15L : cfg >> (i * 3 - 6) & 7L) - 1;
	}

	@Override
	public boolean setCfg(int i, int v) {
		if (IMachineData.super.setCfg(i, v)) {
			boolean mod = i < 6;
			cfg = Utils.setState(cfg, mod ? i * 4 : i * 3 - 6, mod ? 15L : 7L, v + 1);
			return true;
		}
		return false;
	}

	@Override
	public void initContainer(DataContainer container) {
		TileContainer cont = (TileContainer)container;
		for (int j = 0; j < 2; j++)
			for (int i = 0; i < 6; i++)
				cont.addItemSlot(new SlotItemHandler(inv, i + j * 6, 30 + i * 18, 16 + j * 36));
		for (int j = 0; j < 3; j++)
			cont.addItemSlot(new SlotItemHandler(inv, j + 12, 147, 16 + 18 * j));
		cont.addItemSlot(new SlotItemType(inv, 15, 174, 34));
		cont.addPlayerInventory(30, 86);
	}

	@Override
	public int[] getSyncVariables() {
		return new int[] {(int)cfg, (int)(cfg >> 32), status, layout.id};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		if (i == 0) cfg = cfg & 0xffffffff00000000L | (long)v & 0xffffffffL;
		else if (i == 1) cfg = cfg & 0xffffffffL | (long)v << 32 & 0xffffffff00000000L;
		else if (i == 2) status = v;
		else if (i == 3) layout = Layout.fromId(v);
	}

	@Override
	public boolean detectAndSendChanges(DataContainer container, PacketBuffer dos) {
		return false;
	}

	@Override
	public void updateClientChanges(DataContainer container, PacketBuffer dis) {
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void onPacketFromClient(PacketBuffer data, EntityPlayer sender) throws IOException {
		byte cmd = data.readByte();
		if (cmd == -20) craft();
		else if (cmd <= 0) {
			if (!setCfg(-cmd, data.readByte()))
				setCfg(-cmd, -1);
		}
	}

	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) {
	}

	@Override
	public List<ItemStack> dropItem(IBlockState state, int fortune) {
		List<ItemStack> list = makeDefaultDrops(null);
		inv.addToList(list);
		return list;
	}

	@Override
	public IMachineData getMachine() {
		return this;
	}

	@Override
	public boolean isAssembled() {
		return false;
	}

}

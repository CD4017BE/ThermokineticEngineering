package cd4017be.thermokin.item;

import java.io.IOException;
import java.util.Arrays;

import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.BlockGuiHandler.ClientItemPacketReceiver;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.GlitchSaveSlot;
import cd4017be.lib.Gui.IGuiItem;
import cd4017be.lib.Gui.ItemGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.capability.BasicInventory;
import cd4017be.lib.item.BaseItem;
import cd4017be.thermokin.module.IMachineData;
import cd4017be.thermokin.module.IMachineData.IMachineAccess;
import cd4017be.thermokin.render.gui.GuiAssembler;
import cd4017be.thermokin.tileentity.ModularMachine;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;


/**
 * @author CD4017BE
 *
 */
public class ItemWrench extends BaseItem implements IGuiItem, ClientItemPacketReceiver {

	/**
	 * @param id
	 */
	public ItemWrench(String id) {
		super(id);
	}

	@Override
	public Container getContainer(ItemStack item, EntityPlayer player, World world, BlockPos pos, int slot) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof ModularMachine) return new TileContainer(new GuiData((ModularMachine)te, slot), player);
		return null;
	}

	@Override
	public GuiContainer getGui(ItemStack item, EntityPlayer player, World world, BlockPos pos, int slot) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof ModularMachine) return new GuiAssembler(new GuiData((ModularMachine)te, slot), player);
		return null;
	}

	@Override
	public void onPacketFromClient(PacketBuffer data, EntityPlayer player, ItemStack item, int slot) throws IOException {
		if (player.openContainer instanceof TileContainer) {
			TileContainer cont = (TileContainer)player.openContainer;
			byte cmd = data.readByte();
			if (cmd == 1) {//set all reference ItemStacks to null, so the server thinks they changed and sends the data again.
				for (Slot s : cont.inventorySlots)
					if (s instanceof GlitchSaveSlot)
						cont.inventoryItemStacks.set(s.slotNumber, ItemStack.EMPTY);
				Arrays.fill(cont.refInts, 0);
			} else if (cmd <= 0 && cmd > -20 && cont.data instanceof GuiData) {
				GuiData gd = (GuiData)cont.data;
				if (!gd.tile.setCfg(-cmd, data.readByte()))
					gd.tile.setCfg(-cmd, -1);
			}
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) return EnumActionResult.SUCCESS;
		if (world.getTileEntity(pos) instanceof ModularMachine) {
			BlockGuiHandler.openGui(player, world, pos, hand == EnumHand.MAIN_HAND ? player.inventory.currentItem : 40);
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.FAIL;
	}

	class GuiData extends ItemGuiData implements IMachineAccess {

		public final ModularMachine tile;
		public final int slot;

		/**
		 * @param item
		 */
		public GuiData(ModularMachine tile, int slot) {
			super(ItemWrench.this);
			this.tile = tile;
			this.slot = slot;
		}

		@Override
		public void initContainer(DataContainer container) {
			TileContainer cont = (TileContainer)container;
			IItemHandler inv;
			if (tile.getWorld().isRemote) {
				inv = new BasicInventory(tile.components.length);
				//Workaround to fix an inventory sync bug. Sends a request to server that it should send the inventory data again.
				PacketBuffer dos = BlockGuiHandler.getPacketForItem(slot);
				dos.writeByte(1);
				BlockGuiHandler.sendPacketToServer(dos);
			} else inv = tile.new PartInventory();
			for (int j = 0; j < 2; j++)
				for (int i = 0; i < 6; i++)
					cont.addItemSlot(new GlitchSaveSlot(inv, i + j * 6, 30 + i * 18, 16 + j * 36));
			for (int j = 0; j < 3; j++)
				cont.addItemSlot(new GlitchSaveSlot(inv, j + 12, 147, 16 + 18 * j));
			cont.addPlayerInventory(30, 86, false, true);
		}

		@Override
		public boolean canPlayerAccessUI(EntityPlayer player) {
			return super.canPlayerAccessUI(player) && !tile.invalid();
		}

		@Override
		public int[] getSyncVariables() {
			return new int[] {(int)tile.cfg, (int)(tile.cfg >> 32), tile.isComplete ? 1 : 0};
		}

		@Override
		public void setSyncVariable(int i, int v) {
			if (i == 0) tile.cfg = tile.cfg & 0xffff_ffff_0000_0000L | (long)v & 0xffff_ffffL;
			else if (i == 1) tile.cfg = tile.cfg & 0x0000_0000_ffff_ffffL | (long)v << 32;
			else tile.isComplete = v != 0;
		}

		@Override
		public IMachineData getMachine() {
			return tile;
		}

		@Override
		public boolean isAssembled() {
			return true;
		}

	}

}

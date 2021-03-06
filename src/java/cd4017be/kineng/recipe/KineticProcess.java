package cd4017be.kineng.recipe;

import static cd4017be.lib.Gui.comp.Progressbar.H_FILL;
import static net.minecraftforge.items.ItemHandlerHelper.canItemStacksStack;
import static net.minecraftforge.items.ItemHandlerHelper.copyStackWithSize;
import java.util.Arrays;
import java.util.List;
import cd4017be.kineng.physics.DynamicForce;
import cd4017be.kineng.physics.ForceCon;
import cd4017be.lib.Gui.*;
import cd4017be.lib.Gui.AdvancedContainer.IStateInteractionHandler;
import cd4017be.lib.Gui.comp.*;
import cd4017be.lib.network.*;
import cd4017be.math.cplx.CplxD;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.*;


/** 
 * @author CD4017BE */
public class KineticProcess extends DynamicForce
implements IItemHandlerModifiable, IStateInteractionHandler, INBTSerializable<NBTTagCompound> {

	public static double FRICTION_V0;

	public final ItemStack[] inv;
	public ProcessingRecipes recipes;
	public KineticRecipe rcp;
	/** [m] distance moved for current operation */
	public double s;
	public double v;
	/** 1: working, 0: idle, -1: no recipe */
	public byte working = -1;
	/** bit[31]: unloaded, bit[8..30]: recipeListId, bit[0..7]: tier */
	public int mode = Integer.MAX_VALUE;

	public KineticProcess(int slots) {
		Arrays.fill(inv = new ItemStack[slots], ItemStack.EMPTY);
	}

	public KineticProcess setMode(int mode) {
		this.recipes = ProcessingRecipes.getRecipeList(mode);
		this.mode = mode;
		this.working = -1;
		this.rcp = null;
		this.v = 0;
		updateRecipe();
		return this;
	}

	@Override
	public int getSlots() {
		return inv.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inv[slot];
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		inv[slot] = stack;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (slot != 0) return stack;
		ItemStack stack0 = inv[slot];
		int n = stack0.getCount();
		int m = Math.min(Math.min(getSlotLimit(slot), stack.getMaxStackSize()) - n, stack.getCount()); 
		if (m <= 0) return stack;
		if (n == 0) {
			if (!simulate) {
				inv[slot] = copyStackWithSize(stack, m);
				updateRecipe();
			}
		} else if (canItemStacksStack(stack0, stack)) {
			if (!simulate) {
				stack0.grow(m);
				if (working == 0) updateState();
			}
		} else return stack;
		return copyStackWithSize(stack, stack.getCount() - m);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (slot == 0 && working >= 0) return ItemStack.EMPTY;
		ItemStack stack0 = inv[slot];
		if (stack0.getCount() < amount) amount = stack0.getCount();
		if (amount <= 0) return ItemStack.EMPTY;
		if (!simulate) {
			if (amount == stack0.getCount()) inv[slot] = ItemStack.EMPTY;
			else stack0.shrink(amount);
			if (working == 0 && slot != 0) updateState();
		}
		return ItemHandlerHelper.copyStackWithSize(stack0, amount);
	}

	private void updateRecipe() {
		if (recipes == null) return;
		if (rcp == null || !canItemStacksStack(rcp.io[0], inv[0])) {
			KineticRecipe rcp1 = recipes.get(inv[0]);
			if (rcp1 != rcp) {
				rcp = rcp1 == null || rcp1.io.length > inv.length ? null : rcp1;
				s = 0; //reset progress because it's not compatible between recipes
			}
		}
		updateState();
	}

	private void updateState() {
		if (recipes == null) return;
		working = (byte)(rcp == null ? -1 : limit() > 0 ? 1 : 0);
	}

	protected int limit() {
		int l = inv[0].getCount() / rcp.io[0].getCount(), n;
		for (int i = rcp.io.length - 1; i > 0; i--) {
			ItemStack stack = rcp.io[i];
			if ((n = stack.getCount()) == 0) continue;
			int m = inv[i].getCount();
			if (m > 0 && !canItemStacksStack(inv[i], stack)) return 0;
			l = Math.min((stack.getMaxStackSize() - m + n - 1) / n, l);
		}
		return l;
	}

	protected void output(int slot, ItemStack stack, int n) {
		if ((n *= stack.getCount()) == 0) return;
		ItemStack stack0 = inv[slot];
		if (stack0.isEmpty())
			(inv[slot] = stack.copy()).setCount(n);
		else stack0.grow(n);
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public void work(double dE, double ds, double v) {
		this.v = v;
		if (working <= 0 || (s -= dE / rcp.F) < rcp.s) return;
		int l = limit();
		int n = (int)Math.floor(s / rcp.s);
		if (n >= l) {
			n = l;
			working = 0;
		}
		s -= rcp.s * n;
		inv[0].shrink(n * rcp.io[0].getCount());
		for (int i = rcp.io.length - 1; i > 0; i--)
			output(i, rcp.io[i], n);
	}

	@Override
	public ForceCon getM(CplxD M, double av) {
		Fdv = working > 0 ? -rcp.F / (Math.abs(av * r) + FRICTION_V0) : 0;
		return super.getM(M, av);
	}

	private static final StateSynchronizer.Builder ssb = StateSynchronizer.builder().addFix(4, 4, 4, 4, 2, 1);

	public AdvancedContainer getContainer(EntityPlayer player) {
		AdvancedContainer cont = new AdvancedContainer(this, ssb.build(player.world.isRemote), player);
		cont.addItemSlot(new GlitchSaveSlot(this, 0, 35, 25), false);
		cont.addItemSlot(new GlitchSaveSlot(this, 1, 107, 25), false);
		cont.addItemSlot(new GlitchSaveSlot(this, 2, 125, 25), false);
		cont.addPlayerInventory(8, 68);
		cont.transferHandlers.add((stack, inv)-> inv.mergeItemStack(stack, 0, 1, false));
		return cont;
	}

	@SideOnly(Side.CLIENT)
	public ModularGui getGuiScreen(EntityPlayer player) {
		ModularGui gui = new ModularGui(getContainer(player));
		GuiFrame frame = new GuiFrame(gui, 176, 150, 6)
		.background(ProcessingRecipes.GUI_TEX, 0, 0)
		.title("gui.kineng.processing.name", 0.5F);
		
		new Button(frame, 8, 16, 8, 25, 0, this::status, (a)-> gui.sendPkt(A_STOP))
		.texture(200, 0).tooltip("gui.kineng.pr_status#");
		new Button(frame, 16, 16, 53, 25, 0, this::recipeMode, this::showRecipes)
		.texture(208, 0).tooltip("gui.kineng.pr_mode#");
		new Progressbar(frame, 32, 10, 72, 28, 224, 0, H_FILL, this::progress);
		new Tooltip(frame, 32, 10, 72, 28, "\\%.3um / %.3um", this::progressInfo);
		new FormatText(frame, 32, 8, 72, 16, "\\%.3uN", this::forceInfo).align(0.5F);
		new FormatText(frame, 32, 8, 72, 42, "\\%.3um/s", this::speedInfo).align(0.5F);
		gui.compGroup = frame;
		return gui;
	}

	@Override
	public void writeState(StateSyncServer state, AdvancedContainer cont) {
		state.putAll(
			(float)(rcp != null ? rcp.F : 0),
			(float)(rcp != null ? rcp.s : 0),
			(float)s, (float)v,
			mode, working
		).endFixed();
	}

	@Override
	public void readState(StateSyncClient state, AdvancedContainer cont) throws Exception {
		F = state.get((float)F);
		Fdv = state.get((float)Fdv);
		s = state.get((float)s);
		v = state.get((float)v);
		mode = state.get(mode);
		working = (byte)state.get(working);
	}

	@Override
	public boolean canInteract(EntityPlayer player, AdvancedContainer cont) {
		return !player.isDead && mode >= 0;
	}

	public static final byte A_STOP = 0;

	@Override
	public void handleAction(PacketBuffer pkt, EntityPlayerMP sender) throws Exception {
		if(pkt.readByte() == A_STOP) {
			rcp = null;
			updateState();
		}
	}

	public double progress() {
		return s / Fdv;
	}

	public Object[] progressInfo() {
		return new Object[] {s, Fdv};
	}

	public int status() {
		return working + 1;
	}

	public Object[] forceInfo() {
		return new Object[] {F};
	}

	public Object[] speedInfo() {
		return new Object[] {Math.abs(v)};
	}

	public int recipeMode() {
		return (short)(mode >> 8);
	}

	public void showRecipes(int mb) {
		if (ProcessingRecipes.JEI_SHOW_RECIPES != null)
			ProcessingRecipes.JEI_SHOW_RECIPES.accept(mode);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		for (int i = 0; i < inv.length; i++)
			if (!inv[i].isEmpty())
				nbt.setTag("i" + i, inv[i].writeToNBT(new NBTTagCompound()));
		nbt.setDouble("s", s);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		for (int i = 0; i < inv.length; i++) {
			String key = "i" + i;
			inv[i] = nbt.hasKey(key, NBT.TAG_COMPOUND) ?
				new ItemStack(nbt.getCompoundTag(key)) : ItemStack.EMPTY;
		}
		s = nbt.getDouble("s");
	}

	public void unload() {
		mode |= Integer.MIN_VALUE;
		if (con != null) con.link(null);
	}

	public void addItems(List<ItemStack> list) {
		for (ItemStack stack : inv)
			if (!stack.isEmpty())
				list.add(stack);
	}

}

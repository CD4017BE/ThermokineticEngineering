package cd4017be.thermokin.render.gui;

import java.util.Arrays;
import cd4017be.lib.Gui.AdvancedGui;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.util.TooltipUtil;
import cd4017be.thermokin.module.IMachineData;
import cd4017be.thermokin.module.Layout;
import cd4017be.thermokin.module.Part;
import cd4017be.thermokin.module.PartIOModule;
import cd4017be.thermokin.module.PartIOModule.IOType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * 
 * @author cd4017be
 */
public class GuiAssembler extends AdvancedGui {

	private final IMachineData data;
	private final Part[] parts = new Part[9];

	public GuiAssembler(IGuiData tile, EntityPlayer player) {
		super(new TileContainer(tile, player));
		this.data = (IMachineData)tile;
		this.MAIN_TEX = new ResourceLocation("thermokin:textures/gui/assembler.png");
		this.drawBG = 5;
	}

	@Override
	public void initGui() {
		xSize = 198;
		ySize = 168;
		super.initGui();
		for (int i = 0; i < parts.length; i++)
			parts[i] = data.getPart(i + 6);
		for (int i = 0; i < 6; i++)
			guiComps.add(new ModuleCfg(i, 29, 42, 18, 9));
		Layout layout = data.getLayout();
		guiComps.add(new MainCfg(6, 7, 15, layout.ioCount()).setTooltip(layout.name));
		guiComps.add(new Text<>(7, 29, 4, 108, 8, "assembler.case").center());
		guiComps.add(new Text<>(8, 29, 34, 108, 8, "assembler.mod").center());
		guiComps.add(new Text<>(9, 146, 4, 45, 8, "assembler.core").center());
		guiComps.add(new InfoTab(10, 7, 6, 7, 8, "assembler.info"));
		guiComps.add(new Button(11, 174, 52, 16, 16, 0, ()-> data.getStatus(), (b)-> send((byte)-20)).texture(224, 96).setTooltip("assembler.do#"));
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		for (int i = 0; i < parts.length; i++) {
			Part p = data.getPart(i + 6);
			if (p != parts[i]) {
				parts[i] = p;
				if (i < 6) ((ModuleCfg)guiComps.get(i)).onPartChange();
				else {
					MainCfg comp = (MainCfg)guiComps.get(6);
					Layout layout = data.getLayout();
					int n = layout.ioCount();
					if (comp.n != n)
						guiComps.set(6, comp = new MainCfg(6, 7, 15, n));
					comp.setTooltip(layout.name);
				}
			}
		}
	}

	class ModuleCfg extends GuiComp<Integer> {

		boolean cfg;
		String[] info;

		public ModuleCfg(int id, int px, int py, int w, int h) {
			super(id, px, py, w, h, null, null, null);
			onPartChange();
		}

		public void onPartChange() {
			Part p = data.getPart(id + 6);
			if (p instanceof PartIOModule) {
				PartIOModule m = (PartIOModule)p;
				cfg = m.invType == IOType.EXT_ACC;
				info = TooltipUtil.translate(data.getLayout().name + (m.hasItem ? ".item" : m.hasFluid ? ".fluid" : "")).split("\n");
			} else {
				cfg = false;
				info = new String[0];
			}
		}

		@Override
		public void drawOverlay(int mx, int my) {
			if (!cfg) return;
			int s = data.getCfg(id);
			if (s < 0) return;
			else if (s < 6)
				drawTexturedModalRect(guiLeft + 30 + 18 * s, guiTop + 43, 198, 16, 16, 8);
			else if (s - 6 < info.length)
				drawHoveringText(Arrays.asList(info[s - 6]), mx, my);
		}

		@Override
		public void draw() {
			if (cfg)
				drawTexturedModalRect(px, py, 247, 9 * data.getCfg(id) + 9, w, h);
		}

		@Override
		public boolean mouseIn(int x, int y, int b, int d) {
			if (!cfg) return false;
			int next;
			if (d == 1) next = b == 0 ? 2 : 16;
			else if (d == 3) next = b > 0 ? 2 : 16;		
			else return false;
			int s = data.getCfg(id), k = s;
			do {
				k = (k + next & 15) - 1;
				if (data.setCfg(id, k) || k >= 6 && next == 2 && data.setCfg(id, k = -1)) {
					send((byte)-id, (byte)k);
					break;
				}
			} while(k != s);
			return true;
		}

	}

	class MainCfg extends GuiComp<Integer> {

		String[] info = new String[0];
		final int n;

		public MainCfg(int id, int px, int py, int n) {
			super(id, px, py, 18, n * 9, null, null, null);
			this.n = n;
		}

		@Override
		public GuiComp<Integer> setTooltip(String s) {
			info = TooltipUtil.translate(s).split("\n");
			return super.setTooltip(s);
		}

		@Override
		public void drawOverlay(int mx, int my) {
			int i = (my - py) / 9;
			int s = data.getCfg(i + 6);
			if (s >= 0)
				drawTexturedModalRect(guiLeft + 30 + 18 * s, guiTop + 43, 198, 16, 16, 8);
			if (i < info.length)
				drawHoveringText(Arrays.asList(info[i]), mx, my);
		}

		@Override
		public void draw() {
			mc.renderEngine.bindTexture(MAIN_TEX);
			drawTexturedModalRect(px - 7, py, 198, 159 - h, 25, h + 7);
			Layout layout = data.getLayout();
			int n = 0;
			for (int i = 0; i < layout.invIn; i++, n++)
				drawTexturedModalRect(px, py + n * 9, 237, 0, 10, 9);
			for (int i = 0; i < layout.invOut; i++, n++)
				drawTexturedModalRect(px, py + n * 9, 237, 9, 10, 9);
			for (int i = 0; i < layout.tankIn; i++, n++)
				drawTexturedModalRect(px, py + n * 9, 237, 18, 10, 9);
			for (int i = 0; i < layout.tankOut; i++, n++)
				drawTexturedModalRect(px, py + n * 9, 237, 27, 10, 9);
			for (int i = 0; i < n; i++)
				drawTexturedModalRect(px + 11, py + i * 9, 248, 9 * data.getCfg(i + 6) + 9, 8, 9);
		}

		@Override
		public boolean mouseIn(int x, int y, int b, int d) {
			int next;
			if (d == 1) next = b == 0 ? 2 : 7;
			else if (d == 3) next = b > 0 ? 2 : 7;		
			else return false;
			int i = (y - py) / 9 + 6;
			int s = data.getCfg(i), k = s;
			do {
				k = (k + next) % 7 - 1;
				if (data.setCfg(i, k)) {
					send((byte)-i, (byte)k);
					break;
				}
			} while(k != s);
			return true;
		}

	}

}

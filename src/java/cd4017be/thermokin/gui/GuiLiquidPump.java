package cd4017be.thermokin.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import cd4017be.thermokin.tileentity.LiquidPump;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

public class GuiLiquidPump extends GuiMachine {

	private final LiquidPump tile;

	public GuiLiquidPump(LiquidPump tile, EntityPlayer player) {
		super(new TileContainer(tile, player));
		this.tile = tile;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/tesla.png");
		this.bgTexY = 192;
	}

	@Override
	public void initGui() {
		this.xSize = 122;
		this.ySize = 58;
		super.initGui();
		guiComps.add(new Slider(0, 93, 16, 34, 212, 54, 12, 4, false));
		guiComps.add(new Button(1, 73, 42, 9, 9, 1).texture(122, 144));
		guiComps.add(new Button(2, 82, 42, 9, 9, 2).texture(122, 144));
		guiComps.add(new Button(3, 7, 15, 18, 18, 0).texture(194, 0).setTooltip("rstCtr"));
		guiComps.add(new Button(4, 7, 33, 18, 18, 0).texture(176, 0).setTooltip("rstCtr"));
		guiComps.add(new ProgressBar(5, 104, 16, 10, 34, 122, 216, (byte)3));
		guiComps.add(new Text(6, 26, 16, 64, 24, "pump.stat"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 0: return 1F - tile.dA / LiquidPump.Amax;
		case 1: return EnumFacing.VALUES[((tile.cfg & 0xf) + tile.getOrientation()) % 6];
		case 2: return EnumFacing.VALUES[((tile.cfg >> 4 & 0xf) + tile.getOrientation()) % 6];
		case 3: return tile.cfg >> 9 & 1;
		case 4: return tile.cfg >> 8 & 3;
		case 5: return tile.speed / LiquidPump.Amax;
		case 6: return new Object[]{tile.dA * 1000F, tile.speed * 1000F, tile.power / 1000F};
		default: return null;
		}
		
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 0: dos.writeByte(0).writeFloat(tile.dA = (1F - (Float)obj) * LiquidPump.Amax); break;
		case 1: dos.writeByte(1).writeInt(tile.cfg = tile.cfg & ~0x0f | ((tile.cfg & 0xf) + ((Integer)obj == 0 ? 1 : 5)) % 6); break;
		case 2: dos.writeByte(1).writeInt(tile.cfg = tile.cfg & ~0xf0 | (((tile.cfg >> 4 & 0xf) + ((Integer)obj == 0 ? 1 : 5)) % 6) << 4); break;
		case 3: dos.writeByte(1).writeInt(tile.cfg ^= 0x200); break;
		case 4: dos.writeByte(1).writeInt(tile.cfg ^= 0x100); break;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}

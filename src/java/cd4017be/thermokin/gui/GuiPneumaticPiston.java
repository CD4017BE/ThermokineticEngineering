package cd4017be.thermokin.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import cd4017be.thermokin.tileentity.PneumaticPiston;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

public class GuiPneumaticPiston extends GuiMachine {

	private final PneumaticPiston tile;

	public GuiPneumaticPiston(PneumaticPiston tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("thermokin", "textures/gui/misc.png");
		this.bgTexY = 128;
	}

	@Override
	public void initGui() {
		this.xSize = 122;
		this.ySize = 58;
		super.initGui();
		guiComps.add(new Slider(0, 81, 16, 34, 212, 54, 12, 4, false));
		guiComps.add(new Button(1, 61, 42, 9, 9, 1).texture(122, 144));
		guiComps.add(new Button(2, 70, 42, 9, 9, 2).texture(122, 144));
		guiComps.add(new Button(3, 7, 15, 18, 18, 0).texture(194, 0).setTooltip("rstCtr"));
		guiComps.add(new Button(4, 7, 33, 18, 18, 0).texture(176, 0).setTooltip("rstCtr"));
		guiComps.add(new ProgressBar(5, 93, 16, 20, 34, 131, 144, (byte)3));
		guiComps.add(new Text(6, 26, 16, 64, 24, "piston.stat"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 0: return 1F - tile.Ain / PneumaticPiston.Amax;
		case 1: return EnumFacing.VALUES[((tile.cfg & 0xf) + tile.getOrientation()) % 6];
		case 2: return EnumFacing.VALUES[((tile.cfg >> 4 & 0xf) + tile.getOrientation()) % 6];
		case 3: return tile.cfg >> 9 & 1;
		case 4: return tile.cfg >> 8 & 3;
		case 5: return tile.Aout / PneumaticPiston.Amax;
		case 6: return new Object[]{tile.Ain * 1000F, tile.Aout * 1000F, tile.power / 1000F};
		default: return null;
		}
		
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 0: dos.writeByte(AutomatedTile.CmdOffset).writeFloat(tile.Ain = (1F - (Float)obj) * PneumaticPiston.Amax); break;
		case 1: dos.writeByte(AutomatedTile.CmdOffset + 1).writeInt(tile.cfg = tile.cfg & ~0x0f | ((tile.cfg & 0xf) + ((Integer)obj == 0 ? 1 : 5)) % 6); break;
		case 2: dos.writeByte(AutomatedTile.CmdOffset + 1).writeInt(tile.cfg = tile.cfg & ~0xf0 | (((tile.cfg >> 4 & 0xf) + ((Integer)obj == 0 ? 1 : 5)) % 6) << 4); break;
		case 3: dos.writeByte(AutomatedTile.CmdOffset + 1).writeInt(tile.cfg ^= 0x200); break;
		case 4: dos.writeByte(AutomatedTile.CmdOffset + 1).writeInt(tile.cfg ^= 0x100); break;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}

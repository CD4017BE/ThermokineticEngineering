package cd4017be.thermokin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLLog;

import org.apache.logging.log4j.Level;

import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.ConfigurationFile;
import cd4017be.thermokin.multiblock.IGear;
import cd4017be.thermokin.multiblock.IMagnet;
import cd4017be.thermokin.recipe.Converting;
import cd4017be.thermokin.recipe.Substances;
import cd4017be.thermokin.recipe.Substances.BlockEntry;
import cd4017be.thermokin.tileentity.Crystallizer;
import cd4017be.thermokin.tileentity.Evaporator;
import cd4017be.thermokin.tileentity.GasPipe;
import cd4017be.thermokin.tileentity.GasVent;
import cd4017be.thermokin.tileentity.HeatedFurnace;
import cd4017be.thermokin.tileentity.LiquidPump;
import cd4017be.thermokin.tileentity.LiquidReservoir;
import cd4017be.thermokin.tileentity.PneumaticPiston;
import cd4017be.thermokin.tileentity.Shaft;
import cd4017be.thermokin.tileentity.SolidFuelHeater;

public class Config {

	public static ConfigurationFile data = new ConfigurationFile();

	public static void loadConfig(File file) {
		if (file != null) {
			FMLLog.log("thermokin", Level.INFO, "Loading config from File");
			try {
				data.load(file);
			} catch (IOException e) {
				FMLLog.log("thermokin", Level.WARN, e, "Config file loading failed!");
			}
		} else {
			FMLLog.log("thermokin", Level.WARN, "No config data loaded!");
		}
		
		addMat("default", null, 2.5F, 2.5F, 1.0F);
		addMat("IRON", Material.IRON, 0.01F, 0.01F, 1.0F);
		addMat("GLASS", Material.GLASS, 1.0F, 1.0F, 1.0F);
		addMat("ROCK", Material.ROCK, 1.2F, 1.2F, 1.0F);
		addMat("CLAY", Material.CLAY, 1.0F, 1.0F, 1.0F);
		addMat("GROUND", Material.GROUND, 1.5F, 1.5F, 1.0F);
		addMat("GRASS", Material.GRASS, 2.0F, 2.0F, 1.0F);
		addMat("SAND", Material.SAND, 4.0F, 4.0F, 1.0F);
		addMat("WOOD", Material.WOOD, 5.0F, 5.0F, 1.0F);
		addMat("PACKED_ICE", Material.PACKED_ICE, 0.4F, 0.4F, 1.0F);
		addMat("ICE", Material.ICE, 0.5F, 0.5F, 1.0F);
		addMat("CRAFTED_SNOW", Material.CRAFTED_SNOW, 12.0F, 12.0F, 1.0F);
		addMat("AIR", Material.AIR, 25.0F, 0.0F, 10.0F);
		addMat("SNOW", Material.SNOW, 15.0F, 15.0F, 1.0F);
		addMat("CLOTH", Material.CLOTH, 100.0F, 100.0F, 1.0F);
		addMat("LAVA", Material.LAVA, 2.0F, 1.6F, 0.5F);
		addMat("WATER", Material.WATER, 1.2F, 1.0F, 0.25F);
		
		Crystallizer.C0 = data.getFloat("crystallizer.C", 5000F);
		Crystallizer.R0 = data.getFloat("crystallizer.R", 0.004F);
		Crystallizer.SizeL = data.getDouble("crystallizer.Vl", 1.0);
		Crystallizer.SizeG = data.getDouble("crystallizer.Vg", 4.0);
		Evaporator.C0 = data.getFloat("evaporator.C", 5000F);
		Evaporator.R0 = data.getFloat("evaporator.R", 0.004F);
		Evaporator.SizeL = data.getDouble("evaporator.Vl", 0.8);
		Evaporator.SizeG = data.getDouble("evaporator.Vg", 1.0);
		HeatedFurnace.C0 = data.getFloat("hFurnace.C", 10000F);
		HeatedFurnace.R0 = data.getFloat("hFurnace.R", 0.004F);
		HeatedFurnace.Energy = data.getFloat("hFurnace.Ew", 250000F);
		HeatedFurnace.NeededTemp = data.getFloat("hFurnace.Tw", 1200F);
		HeatedFurnace.TRwork = data.getFloat("hFurnace.Rw", 20F);
		LiquidReservoir.C0 = data.getFloat("liqReservoir.C", 5000F);
		LiquidReservoir.R0 = data.getFloat("liqReservoir.R", 0.004F);
		LiquidReservoir.SizeL = data.getDouble("liqReservoir.Vl", 1.0);
		LiquidReservoir.SizeG = data.getDouble("liqReservoir.Vg", 4.0);
		LiquidReservoir.P0 = data.getDouble("liqReservoir.P0", 101300);
		SolidFuelHeater.C0 = data.getFloat("sfHeater.C", 10000F);
		SolidFuelHeater.R0 = data.getFloat("sfHeater.R", 0.004F);
		SolidFuelHeater.FuelEnergy = data.getFloat("sfHeater.E", 10000F);
		GasPipe.size = data.getDouble("gasPipe.Vg", 0.25);
		GasVent.size = data.getDouble("gasVent.Vg", 5F);
		LiquidPump.Amin = data.getFloat("liqPump.Amin", 0.0001F);
		LiquidPump.Amax = data.getFloat("liqPump.Amax", 0.01F);
		PneumaticPiston.Amin = data.getFloat("piston.Amin", 0.001F);
		PneumaticPiston.Amax = data.getFloat("piston.Amax", 0.1F);
		Shaft.M0 = data.getFloat("shaft.mass", 1000F);
	}

	public static void postInit() {
		Entry[] data = convert("shaftGear", new Entry[0]);
		final int Ngears = data.length;
		data = convert("shaftMagnet", data);
		int l = data.length;
		Arrays.sort(data);
		final int[] shaftMounts = new int[l];
		final float[] mountMult = new float[l],
				mountMass = new float[l];
		for (int i = 0; i < l; i++) {
			Entry e = data[i];
			shaftMounts[i] = e.id;
			mountMass[i] = e.m;
			mountMult[i] = e.s;
		}
		Shaft.handlers.add((tile, item) -> {
			int i = Arrays.binarySearch(shaftMounts, Converting.hashItem(item));
			return i < 0 ? null : i < Ngears ? 
				new IGear.SimpleGear(tile, mountMass[i], mountMult[i]) : 
				new IMagnet.SimpleMagnet(tile, mountMass[i], mountMult[i]);
		});
	}

	private static void addMat(String tag, Material m, float R, float Re, float Xe) {
		float[] args = data.getFloatArray("Rmat." + tag);
		if (args.length == 3) {
			R = args[0];
			Re = args[1];
			Xe = args[2];
		}
		BlockEntry e = new BlockEntry(R, Re, Xe);
		if (m != null) Substances.materials.put(m, e);
		else Substances.def_block = e;
	}

	private static Entry[] convert(String key, Entry[] prev) {
		float[] mult = data.getFloatArray(key + ".mult"),
				mass = data.getFloatArray(key + ".mass");
		String[] names = data.getStringArray(key + ".item");
		int l = Math.min(names.length, Math.min(mult.length, mass.length));
		int p = prev.length;
		Entry[] arr = Arrays.copyOf(prev, p + l);
		for (int i = 0; i < l; i++) {
			String[] name = names[i].split(":");
			int id;
			if (name.length == 1) {
				ItemStack item = BlockItemRegistry.stack(name[0], 1);
				id = item == null ? 0 : Converting.hashItem(item);
			} else {
				id = Item.getIdFromItem(Item.getByNameOrId(name[0] + ":" + name[1]));
				if (name.length > 2) try {
					id |= (Short.parseShort(name[2]) & 0xffff) << 16;
				} catch(NumberFormatException e) {}
			}
			arr[i + p] = new Entry(id, mass[i], mult[i]);
		}
		return arr;
	}

	private static class Entry implements Comparable<Entry>{
		Entry(int id, float m, float s) {this.id = id; this.m = m; this.s = s;}
		int id;
		float m, s;
		@Override
		public int compareTo(Entry o) {return id - o.id;}
	}

}

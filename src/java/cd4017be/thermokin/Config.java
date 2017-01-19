package cd4017be.thermokin;

import java.io.File;
import java.io.IOException;
import net.minecraftforge.fml.common.FMLLog;

import org.apache.logging.log4j.Level;

import cd4017be.lib.ConfigurationFile;
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

}

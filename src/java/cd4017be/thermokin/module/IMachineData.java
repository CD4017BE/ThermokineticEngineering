package cd4017be.thermokin.module;

import cd4017be.thermokin.module.PartIOModule.IOType;

public interface IMachineData {

	/**
	 * @param i 0-5: casing, 6-11: modules, 12-14: core
	 */
	Part getPart(int i);

	Layout getLayout();

	int getStatus();
	/**
	 * @param i 0-5: modules, 6-x: resourceIO
	 * @return -1: none, 0-5: sides, 6-x: resources
	 */
	int getCfg(int i);
	/**
	 * @param i 0-5: modules, 6-x: resourceIO
	 * @param v -1: none, 0-5: sides, 6-x: resources
	 * @return valid
	 */
	default boolean setCfg(int i, int v) {
		if (v < 0) return v == -1;
		if (i < 6) {
			if (v == i) return false;
			Layout layout = getLayout();
			Part p = getPart(i + 6), q;
			if (p instanceof PartIOModule) {
				PartIOModule n = (PartIOModule)p;
				if (n.invType != IOType.EXT_ACC) return false;
				if (v >= 6) {
					return v - 6 < (n.hasItem ? layout.invAcc : n.hasFluid ? layout.tankAcc : 0);
				} else if ((q = getPart(v + 6)) instanceof PartIOModule) {
					PartIOModule m = (PartIOModule)q;
					return m.invType == IOType.BUFFER && (!n.hasItem || m.hasItem) && (!n.hasFluid || m.hasFluid);
				}
			}
		} else if (v < 6) {
			Layout layout = getLayout();
			Part p = getPart(v + 6);
			if (p instanceof PartIOModule) {
				PartIOModule m = (PartIOModule)p;
				if (m.invType == IOType.EXT_ACC) return false;
				int n = 6;
				if (i < (n += layout.invIn + layout.invOut))
					return m.hasItem;
				if (i < (n += layout.tankIn + layout.tankOut))
					return m.hasFluid;
			}
		}
		return false;
	}

}

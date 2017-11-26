package cd4017be.thermokin.module;

import cd4017be.thermokin.tileentity.ModularMachine;

public interface IPartListener {

	void onPartChanged(ModularMachine m, int i);

	default void onPartsLoad(ModularMachine m) {
		for (int i = 0; i < m.components.length; i++)
			onPartChanged(m, i);
	}

}

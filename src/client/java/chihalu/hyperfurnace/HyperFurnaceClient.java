package chihalu.hyperfurnace;

import chihalu.hyperfurnace.network.HyperFurnaceNetworking;
import net.fabricmc.api.ClientModInitializer;

public class HyperFurnaceClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HyperFurnaceNetworking.registerCommon();
	}
}

package chihalu.hyperfurnace;

import chihalu.hyperfurnace.network.HyperFurnaceNetworking;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HyperFurnace implements ModInitializer {
	public static final String MOD_ID = "hyperfurnace";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		HyperFurnaceNetworking.registerServer();
		LOGGER.info("HyperFurnace initialized - furnace speed control enabled.");
	}
}

package chihalu.hyperfurnace.network;

import chihalu.hyperfurnace.network.payload.SetFurnaceSpeedPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.screen.FurnaceScreenHandler;

public final class HyperFurnaceClientNetworking {
    private HyperFurnaceClientNetworking() {
    }

    public static void sendSpeedUpdate(FurnaceScreenHandler handler, int speedLevel) {
        ClientPlayNetworking.send(new SetFurnaceSpeedPayload(handler.syncId, speedLevel));
    }
}

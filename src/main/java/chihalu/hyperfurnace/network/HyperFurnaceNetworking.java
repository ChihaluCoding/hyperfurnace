package chihalu.hyperfurnace.network;

import chihalu.hyperfurnace.furnace.FurnaceScreenHandlerAccess;
import chihalu.hyperfurnace.furnace.FurnaceSpeedAccessor;
import chihalu.hyperfurnace.furnace.FurnaceSpeedLevels;
import chihalu.hyperfurnace.network.payload.SetFurnaceSpeedPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public final class HyperFurnaceNetworking {
    private static boolean registered;

    private HyperFurnaceNetworking() {
    }

    public static void registerCommon() {
        if (registered) {
            return;
        }
        registered = true;
        PayloadTypeRegistry.playC2S().register(SetFurnaceSpeedPayload.ID, SetFurnaceSpeedPayload.CODEC);
    }

    public static void registerServer() {
        registerCommon();
        ServerPlayNetworking.registerGlobalReceiver(SetFurnaceSpeedPayload.ID, (payload, context) -> handleSpeedUpdate((ServerPlayerEntity) context.player(), payload.syncId(), payload.speedLevel()));
    }

    private static void handleSpeedUpdate(ServerPlayerEntity player, int syncId, int requestedSpeed) {
        if (player.currentScreenHandler == null || player.currentScreenHandler.syncId != syncId) {
            return;
        }

        if (!(player.currentScreenHandler instanceof FurnaceScreenHandler furnaceHandler)) {
            return;
        }

        int clampedSpeed = FurnaceSpeedLevels.clamp(requestedSpeed);
        FurnaceScreenHandlerAccess access = (FurnaceScreenHandlerAccess) furnaceHandler;
        Inventory inventory = access.hyperfurnace$getInventory();
        if (inventory instanceof FurnaceBlockEntity furnace) {
            ((FurnaceSpeedAccessor) furnace).hyperfurnace$setSpeedLevel(clampedSpeed);
        }
    }
}

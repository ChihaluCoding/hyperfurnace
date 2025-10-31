package chihalu.hyperfurnace.furnace;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.PropertyDelegate;

public interface FurnaceScreenHandlerAccess {
    PropertyDelegate hyperfurnace$getPropertyDelegate();

    Inventory hyperfurnace$getInventory();
}

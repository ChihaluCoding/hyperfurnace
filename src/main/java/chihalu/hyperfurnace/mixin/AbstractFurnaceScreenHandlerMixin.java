package chihalu.hyperfurnace.mixin;

import chihalu.hyperfurnace.furnace.FurnaceScreenHandlerAccess;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractFurnaceScreenHandler.class)
public abstract class AbstractFurnaceScreenHandlerMixin implements FurnaceScreenHandlerAccess {
    @Shadow @Final Inventory inventory;

    @Shadow @Final private PropertyDelegate propertyDelegate;

    @Override
    public PropertyDelegate hyperfurnace$getPropertyDelegate() {
        return this.propertyDelegate;
    }

    @Override
    public Inventory hyperfurnace$getInventory() {
        return this.inventory;
    }

    @ModifyConstant(
            method = "<init>*",
            constant = @Constant(intValue = 4),
            require = 0
    )
    private static int hyperfurnace$expandClientPropertySize(int original) {
        return 5;
    }
}

package chihalu.hyperfurnace.mixin.client;

import chihalu.hyperfurnace.furnace.FurnaceScreenHandlerAccess;
import chihalu.hyperfurnace.furnace.FurnaceSpeedLevels;
import chihalu.hyperfurnace.furnace.FurnaceSpeedPropertyDelegate;
import chihalu.hyperfurnace.network.HyperFurnaceClientNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceScreen.class)
public abstract class FurnaceScreenMixin extends HandledScreen<AbstractFurnaceScreenHandler> {
    @Unique
    private ButtonWidget hyperfurnace$speedButton;

    @Unique
    private int hyperfurnace$speedValue;
    @Unique
    private static final int hyperfurnace$buttonWidth = 80;
    @Unique
    private static final int hyperfurnace$buttonHeight = 20;

    protected FurnaceScreenMixin(AbstractFurnaceScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void hyperfurnace$addSpeedButton(CallbackInfo ci) {
        if (!(this.handler instanceof FurnaceScreenHandler)) {
            return;
        }

        this.hyperfurnace$speedValue = this.hyperfurnace$getDelegateSpeed();
        int buttonX = this.hyperfurnace$getButtonX();
        int buttonY = this.hyperfurnace$getButtonY();

        this.hyperfurnace$speedButton = ButtonWidget.builder(Text.empty(), button -> {
            int nextValue = FurnaceSpeedLevels.next(this.hyperfurnace$speedValue);
            this.hyperfurnace$setSpeed(nextValue, true);
            HyperFurnaceClientNetworking.sendSpeedUpdate((FurnaceScreenHandler) this.handler, nextValue);
        }).dimensions(buttonX, buttonY, hyperfurnace$buttonWidth, hyperfurnace$buttonHeight).build();
        this.hyperfurnace$speedButton.setTooltip(Tooltip.of(Text.translatable("gui.hyperfurnace.speed.tooltip")));

        this.hyperfurnace$refreshSpeedLabel();
        this.addDrawableChild(this.hyperfurnace$speedButton);
    }

    @Inject(method = "drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V", at = @At("HEAD"))
    private void hyperfurnace$syncSpeedFromServer(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (!(this.handler instanceof FurnaceScreenHandler)) {
            return;
        }

        if (this.hyperfurnace$speedButton != null) {
            this.hyperfurnace$speedButton.setPosition(this.hyperfurnace$getButtonX(), this.hyperfurnace$getButtonY());
        }

        int delegateValue = this.hyperfurnace$getDelegateSpeed();
        if (delegateValue != this.hyperfurnace$speedValue) {
            this.hyperfurnace$setSpeed(delegateValue, false);
        }
    }

    @Unique
    private int hyperfurnace$getDelegateSpeed() {
        if (!(this.handler instanceof FurnaceScreenHandler)) {
            return 0;
        }
        PropertyDelegate delegate = ((FurnaceScreenHandlerAccess) this.handler).hyperfurnace$getPropertyDelegate();
        return FurnaceSpeedLevels.clamp(delegate.get(FurnaceSpeedPropertyDelegate.SPEED_INDEX));
    }

    @Unique
    private void hyperfurnace$setSpeed(int newValue, boolean updateDelegate) {
        this.hyperfurnace$speedValue = FurnaceSpeedLevels.clamp(newValue);
        if (updateDelegate) {
            PropertyDelegate delegate = ((FurnaceScreenHandlerAccess) this.handler).hyperfurnace$getPropertyDelegate();
            delegate.set(FurnaceSpeedPropertyDelegate.SPEED_INDEX, this.hyperfurnace$speedValue);
        }
        this.hyperfurnace$refreshSpeedLabel();
    }

    @Unique
    private void hyperfurnace$refreshSpeedLabel() {
        if (this.hyperfurnace$speedButton != null) {
            this.hyperfurnace$speedButton.setMessage(Text.translatable("gui.hyperfurnace.speed.button", FurnaceSpeedLevels.multiplier(this.hyperfurnace$speedValue)));
        }
    }

    @Unique
    private int hyperfurnace$getButtonX() {
        return this.x + this.backgroundWidth - hyperfurnace$buttonWidth - 6;
    }

    @Unique
    private int hyperfurnace$getButtonY() {
        return this.y + 35 + 24;
    }
}

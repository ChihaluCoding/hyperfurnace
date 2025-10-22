package chihalu.hyperfurnace.mixin;

import chihalu.hyperfurnace.furnace.FurnaceSpeedAccessor;
import chihalu.hyperfurnace.furnace.FurnaceSpeedLevels;
import chihalu.hyperfurnace.furnace.FurnaceSpeedPropertyDelegate;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin implements FurnaceSpeedAccessor {
    private static final String SPEED_KEY = "hyperfurnace_speed";

    @Mutable
    @Shadow
    @Final
    protected PropertyDelegate propertyDelegate;

    @Shadow
    int cookingTimeSpent;

    @Shadow
    int cookingTotalTime;

    @Unique
    private int hyperfurnace$speedLevel;

    @Unique
    private int hyperfurnace$baseCookTime;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hyperfurnace$init(BlockEntityType<?> type, BlockPos pos, BlockState state, RecipeType<? extends AbstractCookingRecipe> recipeType, CallbackInfo ci) {
        if ((Object) this instanceof FurnaceBlockEntity) {
            this.propertyDelegate = new FurnaceSpeedPropertyDelegate(this.propertyDelegate, this);
        }
    }

    @Inject(method = "writeData", at = @At("TAIL"))
    private void hyperfurnace$writeSpeed(WriteView data, CallbackInfo ci) {
        if ((Object) this instanceof FurnaceBlockEntity) {
            data.putInt(SPEED_KEY, this.hyperfurnace$speedLevel);
        }
    }

    @Inject(method = "readData", at = @At("TAIL"))
    private void hyperfurnace$readSpeed(ReadView view, CallbackInfo ci) {
        if ((Object) this instanceof FurnaceBlockEntity) {
            this.hyperfurnace$setSpeedLevelInternal(view.getInt(SPEED_KEY, 0), false);
        }
    }

    @Inject(method = "getCookTime", at = @At("RETURN"), cancellable = true)
    private static void hyperfurnace$adjustCookTime(ServerWorld world, AbstractFurnaceBlockEntity furnace, CallbackInfoReturnable<Integer> cir) {
        if (!(furnace instanceof FurnaceBlockEntity)) {
            return;
        }

        int base = Math.max(1, cir.getReturnValue());
        FurnaceSpeedAccessor accessor = (FurnaceSpeedAccessor) furnace;
        accessor.hyperfurnace$setBaseCookTime(base);

        int speed = accessor.hyperfurnace$getSpeedLevel();
        int adjusted = hyperfurnace$computeCookTime(base, speed);
        if (adjusted == base) {
            return;
        }

        cir.setReturnValue(adjusted);
        ((AbstractFurnaceBlockEntityMixin) (Object) furnace).hyperfurnace$applyCookTimeAdjustment(base, adjusted);
    }

    @Override
    public int hyperfurnace$getSpeedLevel() {
        return this.hyperfurnace$speedLevel;
    }

    @Override
    public void hyperfurnace$setSpeedLevel(int level) {
        if ((Object) this instanceof FurnaceBlockEntity) {
            this.hyperfurnace$setSpeedLevelInternal(level, true);
        }
    }

    @Override
    public void hyperfurnace$setSpeedLevelFromSync(int level) {
        this.hyperfurnace$setSpeedLevelInternal(level, false);
    }

    @Override
    public void hyperfurnace$setBaseCookTime(int cookTime) {
        this.hyperfurnace$baseCookTime = cookTime;
    }

    @Override
    public int hyperfurnace$getBaseCookTime() {
        return this.hyperfurnace$baseCookTime;
    }

    @Unique
    private void hyperfurnace$setSpeedLevelInternal(int level, boolean markDirty) {
        int clamped = FurnaceSpeedLevels.clamp(level);
        if (clamped == this.hyperfurnace$speedLevel) {
            return;
        }

        int previousSpeed = this.hyperfurnace$speedLevel;
        this.hyperfurnace$speedLevel = clamped;

        int base = this.hyperfurnace$baseCookTime;
        if (base <= 0) {
            base = Math.max(1, Math.round((float) this.cookingTotalTime * FurnaceSpeedLevels.multiplier(previousSpeed)));
        }

        this.hyperfurnace$baseCookTime = base;
        int newTotal = hyperfurnace$computeCookTime(base, clamped);
        this.hyperfurnace$applyCookTimeAdjustment(Math.max(1, base), newTotal);

        if (markDirty) {
            ((AbstractFurnaceBlockEntity) (Object) this).markDirty();
        }
    }

    @Unique
    private void hyperfurnace$applyCookTimeAdjustment(int base, int adjusted) {
        if (this.cookingTotalTime == adjusted) {
            return;
        }

        if (this.cookingTotalTime > 0) {
            this.cookingTimeSpent = MathHelper.clamp((int) Math.round(this.cookingTimeSpent * (double) adjusted / this.cookingTotalTime), 0, adjusted);
        }
        this.cookingTotalTime = adjusted;
    }

    @Unique
    private static int hyperfurnace$computeCookTime(int base, int level) {
        int multiplier = FurnaceSpeedLevels.multiplier(level);
        return Math.max(1, (int) Math.round((double) base / multiplier));
    }
}

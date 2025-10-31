package chihalu.hyperfurnace.mixin;

import chihalu.hyperfurnace.HyperFurnace;
import chihalu.hyperfurnace.furnace.FurnaceSpeedAccessor;
import chihalu.hyperfurnace.furnace.FurnaceSpeedLevels;
import chihalu.hyperfurnace.furnace.FurnaceSpeedPropertyDelegate;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin implements FurnaceSpeedAccessor {
    private static final String SPEED_KEY = "hyperfurnace_speed";

    @Mutable
    @Shadow
    @Final
    protected PropertyDelegate propertyDelegate;

    @Unique
    private int hyperfurnace$speedLevel;

    @Unique
    private int hyperfurnace$baseCookTime;

    @Unique
    private static boolean hyperfurnace$loggedStorageError;

    @Unique
    private static Field hyperfurnace$cookTimeField;

    @Unique
    private static Field hyperfurnace$cookTimeTotalField;

    @Unique
    private static boolean hyperfurnace$loggedCookTimeFieldError;

    @Unique
    private static boolean hyperfurnace$loggedCookTimeTotalFieldError;

    @Unique
    private static boolean hyperfurnace$loggedCookTimeAccessError;

    @Unique
    private static boolean hyperfurnace$loggedCookTimeTotalAccessError;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hyperfurnace$init(BlockEntityType<?> type, BlockPos pos, BlockState state, RecipeType<? extends AbstractCookingRecipe> recipeType, CallbackInfo ci) {
        if ((Object) this instanceof FurnaceBlockEntity) {
            this.propertyDelegate = new FurnaceSpeedPropertyDelegate(this.propertyDelegate, this);
        }
    }

    @Inject(method = "writeData", at = @At("TAIL"), require = 0)
    private void hyperfurnace$writeSpeedModern(@Coerce Object data, CallbackInfo ci) {
        if ((Object) this instanceof FurnaceBlockEntity) {
            hyperfurnace$putInt(data, SPEED_KEY, this.hyperfurnace$speedLevel);
        }
    }

    @Inject(method = "readData", at = @At("TAIL"), require = 0)
    private void hyperfurnace$readSpeedModern(@Coerce Object view, CallbackInfo ci) {
        if ((Object) this instanceof FurnaceBlockEntity) {
            this.hyperfurnace$setSpeedLevelInternal(hyperfurnace$getInt(view, SPEED_KEY), false);
        }
    }

    @Inject(method = "writeNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)V", at = @At("TAIL"), require = 0)
    private void hyperfurnace$writeSpeedModernVoid(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup, CallbackInfo ci) {
        if ((Object) this instanceof FurnaceBlockEntity) {
            hyperfurnace$putInt(nbt, SPEED_KEY, this.hyperfurnace$speedLevel);
        }
    }

    @Inject(method = "readNbt(Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)V", at = @At("TAIL"), require = 0)
    private void hyperfurnace$readSpeedModern(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup, CallbackInfo ci) {
        if ((Object) this instanceof FurnaceBlockEntity) {
            this.hyperfurnace$setSpeedLevelInternal(hyperfurnace$getInt(nbt, SPEED_KEY), false);
        }
    }

    @Inject(method = "getCookTime", at = @At("RETURN"), cancellable = true)
    private static void hyperfurnace$adjustCookTime(@Coerce World world, AbstractFurnaceBlockEntity furnace, CallbackInfoReturnable<Integer> cir) {
        hyperfurnace$adjustCookTimeCommon(furnace, cir);
    }

    @Unique
    private static void hyperfurnace$adjustCookTimeCommon(AbstractFurnaceBlockEntity furnace, CallbackInfoReturnable<Integer> cir) {
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
            base = Math.max(1, Math.round((float) this.hyperfurnace$getCookTimeTotalValue() * FurnaceSpeedLevels.multiplier(previousSpeed)));
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
        int currentTotal = this.hyperfurnace$getCookTimeTotalValue();
        if (currentTotal == adjusted) {
            return;
        }

        int currentCookTime = this.hyperfurnace$getCookTimeValue();
        if (currentTotal > 0) {
            currentCookTime = MathHelper.clamp((int) Math.round(currentCookTime * (double) adjusted / currentTotal), 0, adjusted);
        } else {
            currentCookTime = MathHelper.clamp(currentCookTime, 0, adjusted);
        }
        this.hyperfurnace$setCookTimeValue(currentCookTime);
        this.hyperfurnace$setCookTimeTotalValue(adjusted);
    }

    @Unique
    private static int hyperfurnace$computeCookTime(int base, int level) {
        int multiplier = FurnaceSpeedLevels.multiplier(level);
        return Math.max(1, (int) Math.ceil((double) base / multiplier));
    }

    @Unique
    private static Field hyperfurnace$getCookTimeField() {
        if (hyperfurnace$cookTimeField == null && !hyperfurnace$loggedCookTimeFieldError) {
            hyperfurnace$cookTimeField = hyperfurnace$findField("cookTime", "cookingTimeSpent");
            if (hyperfurnace$cookTimeField == null) {
                HyperFurnace.LOGGER.warn("HyperFurnace could not locate cook time field on AbstractFurnaceBlockEntity. Speed syncing may be inaccurate.");
                hyperfurnace$loggedCookTimeFieldError = true;
            }
        }
        return hyperfurnace$cookTimeField;
    }

    @Unique
    private static Field hyperfurnace$getCookTimeTotalField() {
        if (hyperfurnace$cookTimeTotalField == null && !hyperfurnace$loggedCookTimeTotalFieldError) {
            hyperfurnace$cookTimeTotalField = hyperfurnace$findField("cookTimeTotal", "cookingTotalTime");
            if (hyperfurnace$cookTimeTotalField == null) {
                HyperFurnace.LOGGER.warn("HyperFurnace could not locate cook time total field on AbstractFurnaceBlockEntity. Speed syncing may be inaccurate.");
                hyperfurnace$loggedCookTimeTotalFieldError = true;
            }
        }
        return hyperfurnace$cookTimeTotalField;
    }

    @Unique
    private static Field hyperfurnace$findField(String... names) {
        Class<?> owner = AbstractFurnaceBlockEntity.class;
        for (String name : names) {
            try {
                Field field = owner.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    @Unique
    private int hyperfurnace$getCookTimeValue() {
        Field field = hyperfurnace$getCookTimeField();
        if (field == null) {
            return 0;
        }
        try {
            return field.getInt(this);
        } catch (IllegalAccessException ex) {
            if (!hyperfurnace$loggedCookTimeAccessError) {
                HyperFurnace.LOGGER.warn("HyperFurnace could not read furnace cook time field.", ex);
                hyperfurnace$loggedCookTimeAccessError = true;
            }
            return 0;
        }
    }

    @Unique
    private int hyperfurnace$getCookTimeTotalValue() {
        Field field = hyperfurnace$getCookTimeTotalField();
        if (field == null) {
            return 0;
        }
        try {
            return field.getInt(this);
        } catch (IllegalAccessException ex) {
            if (!hyperfurnace$loggedCookTimeTotalAccessError) {
                HyperFurnace.LOGGER.warn("HyperFurnace could not read furnace total cook time field.", ex);
                hyperfurnace$loggedCookTimeTotalAccessError = true;
            }
            return 0;
        }
    }

    @Unique
    private void hyperfurnace$setCookTimeValue(int value) {
        Field field = hyperfurnace$getCookTimeField();
        if (field == null) {
            return;
        }
        try {
            field.setInt(this, value);
        } catch (IllegalAccessException ex) {
            if (!hyperfurnace$loggedCookTimeAccessError) {
                HyperFurnace.LOGGER.warn("HyperFurnace could not write furnace cook time field.", ex);
                hyperfurnace$loggedCookTimeAccessError = true;
            }
        }
    }

    @Unique
    private void hyperfurnace$setCookTimeTotalValue(int value) {
        Field field = hyperfurnace$getCookTimeTotalField();
        if (field == null) {
            return;
        }
        try {
            field.setInt(this, value);
        } catch (IllegalAccessException ex) {
            if (!hyperfurnace$loggedCookTimeTotalAccessError) {
                HyperFurnace.LOGGER.warn("HyperFurnace could not write furnace total cook time field.", ex);
                hyperfurnace$loggedCookTimeTotalAccessError = true;
            }
        }
    }

    @Unique
    private static void hyperfurnace$putInt(Object storage, String key, int value) {
        if (storage == null) {
            return;
        }
        try {
            Method method = storage.getClass().getMethod("putInt", String.class, int.class);
            method.invoke(storage, key, value);
        } catch (NoSuchMethodException ex) {
            hyperfurnace$logStorageReflectionFailure(storage, ex);
        } catch (ReflectiveOperationException ex) {
            hyperfurnace$logStorageReflectionFailure(storage, ex);
        }
    }

    @Unique
    private static int hyperfurnace$getInt(Object storage, String key) {
        if (storage == null) {
            return 0;
        }
        try {
            Method method = storage.getClass().getMethod("getInt", String.class, int.class);
            Object result = method.invoke(storage, key, 0);
            if (result instanceof Integer integer) {
                return integer;
            }
        } catch (NoSuchMethodException ignored) {
            try {
                Method method = storage.getClass().getMethod("getInt", String.class);
                Object result = method.invoke(storage, key);
                if (result instanceof Integer integer) {
                    return integer;
                }
            } catch (NoSuchMethodException ex) {
                hyperfurnace$logStorageReflectionFailure(storage, ex);
            } catch (ReflectiveOperationException ex) {
                hyperfurnace$logStorageReflectionFailure(storage, ex);
            }
        } catch (ReflectiveOperationException ex) {
            hyperfurnace$logStorageReflectionFailure(storage, ex);
        }
        return 0;
    }

    @Unique
    private static void hyperfurnace$logStorageReflectionFailure(Object storage, ReflectiveOperationException ex) {
        if (!hyperfurnace$loggedStorageError) {
            HyperFurnace.LOGGER.warn("Failed to access furnace storage for HyperFurnace speed value on {}", storage.getClass().getName(), ex);
            hyperfurnace$loggedStorageError = true;
        }
    }
}

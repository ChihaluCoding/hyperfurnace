package chihalu.hyperfurnace.furnace;

import net.minecraft.screen.PropertyDelegate;

public final class FurnaceSpeedPropertyDelegate implements PropertyDelegate {
    public static final int SPEED_INDEX = 4;

    private final PropertyDelegate base;
    private final FurnaceSpeedAccessor accessor;

    public FurnaceSpeedPropertyDelegate(PropertyDelegate base, FurnaceSpeedAccessor accessor) {
        this.base = base;
        this.accessor = accessor;
    }

    @Override
    public int get(int index) {
        if (index == SPEED_INDEX) {
            return this.accessor.hyperfurnace$getSpeedLevel();
        }
        return this.base.get(index);
    }

    @Override
    public void set(int index, int value) {
        if (index == SPEED_INDEX) {
            this.accessor.hyperfurnace$setSpeedLevelFromSync(FurnaceSpeedLevels.clamp(value));
            return;
        }
        this.base.set(index, value);
    }

    @Override
    public int size() {
        return this.base.size() + 1;
    }
}

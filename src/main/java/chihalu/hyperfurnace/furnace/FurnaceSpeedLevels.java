package chihalu.hyperfurnace.furnace;

import net.minecraft.util.math.MathHelper;

public final class FurnaceSpeedLevels {
    private static final int[] MULTIPLIERS = {1, 10, 20, 30, 40, 50};

    private FurnaceSpeedLevels() {
    }

    public static int clamp(int level) {
        return MathHelper.clamp(level, 0, MULTIPLIERS.length - 1);
    }

    public static int next(int level) {
        return (level + 1) % MULTIPLIERS.length;
    }

    public static int multiplier(int level) {
        return MULTIPLIERS[clamp(level)];
    }

    public static int count() {
        return MULTIPLIERS.length;
    }
}

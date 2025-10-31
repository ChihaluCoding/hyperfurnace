package chihalu.hyperfurnace.furnace;

public interface FurnaceSpeedAccessor {
    int hyperfurnace$getSpeedLevel();

    void hyperfurnace$setSpeedLevel(int level);

    void hyperfurnace$setSpeedLevelFromSync(int level);

    void hyperfurnace$setBaseCookTime(int cookTime);

    int hyperfurnace$getBaseCookTime();
}

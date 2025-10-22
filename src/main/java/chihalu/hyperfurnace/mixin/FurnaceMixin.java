package chihalu.hyperfurnace.mixin;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Furnace cooking time reduction mixin
 * Makes furnaces cook items 20x faster
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceMixin {

	/**
	 * Hook the static getCookTime method so every furnace recipe finishes 20x sooner.
	 */
	@Inject(
		method = "getCookTime(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;)I",
		at = @At("RETURN"),
		cancellable = true,
		require = 0
	)
	private static void speedUpCooking(ServerWorld world, AbstractFurnaceBlockEntity furnace, CallbackInfoReturnable<Integer> cir) {
		int originalCookTime = cir.getReturnValue();

		// shrink the base cook time by 20x but never drop below 1 tick
		int fasterCookTime = Math.max(1, originalCookTime / 20);

		cir.setReturnValue(fasterCookTime);
	}
}

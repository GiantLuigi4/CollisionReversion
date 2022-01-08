package tfc.collisionreversion.mixin;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.collisionreversion.api.ContextAABB;

import java.util.Optional;

@Mixin(AxisAlignedBB.class)
public abstract class AxisAlignedBBMixin {
	@Shadow
	public abstract AxisAlignedBB intersect(AxisAlignedBB other);
	
	// TODO: figure out how this affects performance
	private static final ThreadLocal<AxisAlignedBB> box = new ThreadLocal<>();
	private static final ThreadLocal<Boolean> isCalculatingHit = ThreadLocal.withInitial(() -> false);
	
	@Inject(at = @At("RETURN"), method = "rayTrace(Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/util/math/vector/Vector3d;)Ljava/util/Optional;")
	public void postRaytrace(Vector3d from, Vector3d to, CallbackInfoReturnable<Optional<Vector3d>> cir) {
		box.remove();
	}
	
	@Inject(at = @At("HEAD"), method = "rayTrace(Ljava/lang/Iterable;Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockRayTraceResult;")
	private static void preRaytrace(Iterable<AxisAlignedBB> boxes, Vector3d start, Vector3d end, BlockPos pos, CallbackInfoReturnable<BlockRayTraceResult> cir) {
		isCalculatingHit.set(true);
	}
	
	@Inject(at = @At("RETURN"), method = "rayTrace(Ljava/lang/Iterable;Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockRayTraceResult;")
	private static void postRaytrace(Iterable<AxisAlignedBB> boxes, Vector3d start, Vector3d end, BlockPos pos, CallbackInfoReturnable<BlockRayTraceResult> cir) {
		if (box.get() instanceof ContextAABB) cir.getReturnValue().hitInfo = ((ContextAABB) box.get()).getContext();
		box.remove();
		isCalculatingHit.remove();
	}
	
	@Inject(at = @At("RETURN"), method = "calcSideHit")
	private static void postCalcSideHit(AxisAlignedBB aabb, Vector3d start, double[] minDistance, Direction facing, double deltaX, double deltaY, double deltaZ, CallbackInfoReturnable<Direction> cir) {
		if (cir.getReturnValue() != null) {
			if (isCalculatingHit.get()) box.set(aabb);
			else isCalculatingHit.remove();
		}
	}
}

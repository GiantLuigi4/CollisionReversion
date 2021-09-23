package tfc.collisionreversion.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.collisionreversion.Config;
import tfc.collisionreversion.utils.EntityMixinHelper;
import tfc.collisionreversion.utils.EnumVCollisionType;

import static tfc.collisionreversion.utils.CommonUtils.*;

@Mixin(Entity.class)
public abstract class MixinEntity {
	@Unique
	EnumVCollisionType legacyVerticalCollision = EnumVCollisionType.NONE;
	
	@Unique
	boolean legacyHorizontalColiision = false;
	
	@Shadow
	public World world;
	
	@Shadow
	protected boolean onGround;
	
	@Shadow
	public boolean collidedVertically;
	
	@Shadow
	public boolean collidedHorizontally;
	
	@Shadow
	protected abstract AxisAlignedBB getBoundingBox(Pose pose);
	
	@Shadow public abstract boolean equals(Object p_equals_1_);
	
	@Unique
	private final EntityMixinHelper mixinHelper = new EntityMixinHelper((Entity) (Object) this);
	
	@Inject(method = "getAllowedMovement(Lnet/minecraft/util/math/vector/Vector3d;)Lnet/minecraft/util/math/vector/Vector3d;", at = @At("HEAD"), cancellable = true)
	public void LegacyCollision_preMove(Vector3d pos, CallbackInfoReturnable<Vector3d> cir) {
		legacyVerticalCollision = EnumVCollisionType.NONE;
		legacyHorizontalColiision = false;
		// TODO:  migrate these two runnables to fields on EntityMixinHelper
		mixinHelper.preMove(pos, (value) -> legacyVerticalCollision = value, (value) -> legacyHorizontalColiision = value);
		if (Config.COMMON.cancelVanillaCollision.get()) cir.setReturnValue(pos);
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;updateFallState(DZLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V"), method = "move")
	public void LegacyCollision_preUpdateFallState(MoverType typeIn, Vector3d pos, CallbackInfo ci) {
		if (legacyVerticalCollision == EnumVCollisionType.FLOOR) {
			onGround = true;
			collidedVertically = true;
		} else if (legacyVerticalCollision == EnumVCollisionType.ROOF) collidedVertically = true;
		if (legacyHorizontalColiision) collidedHorizontally = true;
	}
	
	@Inject(at = @At("HEAD"), method = "isPoseClear", cancellable = true)
	public void LegacyCollision_preCheckPoseClear(Pose pose, CallbackInfoReturnable<Boolean> cir) {
		AxisAlignedBB shrunkBB = getBoundingBox(pose).shrink(1.0E-7D);
		if (
				!hasNoCollisions((Entity) (Object) this, shrunkBB)
//						!hasNoCollisions((Entity) (Object) this, shrunkBB.offset(0, 1.0E-6D, 0))
		) cir.setReturnValue(false);
	}
}
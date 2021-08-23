package tfc.collisionreversion.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.collisionreversion.DotTwelveCollisionEntity;
import tfc.collisionreversion.code.EntityMixin;

@Mixin(Entity.class)
public abstract class MixinEntity implements DotTwelveCollisionEntity {
	@Unique
	boolean legacyVerticalCollision = false;
	
	@Unique
	boolean legacyHorizontalColiision = false;
	
	@Override
	public boolean legacyCollidedVertically() {
		return legacyVerticalCollision;
	}
	
	@Shadow
	public World world;
	
	@Shadow
	protected boolean onGround;
	
	@Shadow
	public boolean collidedVertically;
	
	@Shadow public float stepHeight;
	
	@Shadow public boolean collidedHorizontally;
	
	@Inject(method = "getAllowedMovement(Lnet/minecraft/util/math/vector/Vector3d;)Lnet/minecraft/util/math/vector/Vector3d;", at = @At("HEAD"))
	public void LegacyCollision_preMove(Vector3d pos, CallbackInfoReturnable<Vector3d> cir) {
		new EntityMixin().preMove(pos, (Entity) (Object) this, stepHeight, world, (value)->legacyVerticalCollision = value, (value)->legacyHorizontalColiision = value);
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;updateFallState(DZLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V"), method = "move")
	public void LegacyCollision_preUpdateFallState(MoverType typeIn, Vector3d pos, CallbackInfo ci) {
		if (legacyVerticalCollision && pos.y <= 0) {
			onGround = true;
			collidedVertically = true;
		}
		if (legacyHorizontalColiision) collidedHorizontally = true;
	}
}
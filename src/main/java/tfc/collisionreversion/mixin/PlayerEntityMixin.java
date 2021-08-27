package tfc.collisionreversion.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import tfc.collisionreversion.api.CollisionReversionAPI;

import static tfc.collisionreversion.utils.CommonUtils.*;

@Mixin(value = PlayerEntity.class, priority = -1000)
public class PlayerEntityMixin {
	@Unique Vector3d LegacyCollision_vec;
	@Unique	World LegacyCollision_world;
	@Unique	AxisAlignedBB LegacyCollision_boundingBox;
	@Unique	double LegacyCollision_stepHeight;
	
	@Inject(at = @At("HEAD"), method = "maybeBackOffFromEdge")
	public void LegacyCollision_preBackOffFromEdge(Vector3d vec, MoverType mover, CallbackInfoReturnable<Vector3d> cir) {
		if (!CollisionReversionAPI.useCollision()) return;
		this.LegacyCollision_vec = vec;
		this.LegacyCollision_world = ((Entity)(Object)this).world;
		this.LegacyCollision_boundingBox = ((Entity)(Object)this).getBoundingBox();
	}
	
	@ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/AxisAlignedBB;offset(DDD)Lnet/minecraft/util/math/AxisAlignedBB;"), method = "maybeBackOffFromEdge")
	public void LegacyCollision_preOffset(Args args) {
		if (!CollisionReversionAPI.useCollision()) return;
		if (((Object)this) instanceof PlayerList) return;
		LegacyCollision_stepHeight = args.get(1);
		LegacyCollision_stepHeight *= -1;
	}
	
	// TODO: see if there's maybe a way to optimize this, as this seems slow
	@ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/vector/Vector3d;<init>(DDD)V"), method = "maybeBackOffFromEdge")
	public void LegacyCollision_preCheckNoCollisions(Args args) {
		if (!CollisionReversionAPI.useCollision()) return;
		double xOff = LegacyCollision_vec.x;
		double zOff = LegacyCollision_vec.z;
		double factor = 0.05D;
		
		Entity entity = (Entity) (Object) this;
		
		while (xOff != 0.0D && hasNoCollisions(entity, LegacyCollision_boundingBox.offset(xOff, (double) (-this.LegacyCollision_stepHeight), 0.0D))) {
			if (xOff < factor && xOff >= -factor) xOff = 0.0D;
			else if (xOff > 0.0D) xOff -= factor;
			else xOff += factor;
		}
		
		while (zOff != 0.0D && hasNoCollisions(entity, LegacyCollision_boundingBox.offset(0.0D, (double) (-this.LegacyCollision_stepHeight), zOff))) {
			if (zOff < factor && zOff >= -factor) zOff = 0.0D;
			else if (zOff > 0.0D) zOff -= factor;
			else zOff += factor;
		}
		
		while (xOff != 0.0D && zOff != 0.0D && hasNoCollisions(entity, LegacyCollision_boundingBox.offset(xOff, (double) (-this.LegacyCollision_stepHeight), zOff))) {
			if (xOff < factor && xOff >= -factor) xOff = 0.0D;
			else if (xOff > 0.0D) xOff -= factor;
			else xOff += factor;
			
			if (zOff < factor && zOff >= -factor) zOff = 0.0D;
			else if (zOff > 0.0D) zOff -= factor;
			else zOff += factor;
		}
		
		// if legacy collision crouch result brings the player further than vanilla crouch result, use the legacy collision crouch result
		if (Math.abs(((Double) args.get(0))) < Math.abs(xOff)) args.set(0, xOff);
		if (Math.abs(((Double) args.get(2))) < Math.abs(zOff)) args.set(2, zOff);
	}
}

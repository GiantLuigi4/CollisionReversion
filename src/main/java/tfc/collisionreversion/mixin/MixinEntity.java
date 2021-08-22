package tfc.collisionreversion.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
import tfc.collisionreversion.LegacyAxisAlignedBoundingBox;
import tfc.collisionreversion.api.CollisionReversionAPI;
import tfc.collisionreversion.api.collision.CollisionLookup;

import java.util.ArrayList;

@Mixin(Entity.class)
public abstract class MixinEntity implements DotTwelveCollisionEntity {
	@Unique
	boolean legacyVerticalCollision = false;
	
	@Override
	public boolean legacyCollidedVertically() {
		return legacyVerticalCollision;
	}
	
	@Shadow
	public boolean noClip;
	
	@Shadow
	public World world;
	
	@Shadow
	public abstract AxisAlignedBB getBoundingBox();
	
	@Shadow
	public abstract void setBoundingBox(AxisAlignedBB bb);
	
	@Shadow
	public abstract void setMotion(Vector3d motionIn);
	
	@Shadow
	public abstract Vector3d getMotion();
	
	@Shadow
	protected boolean onGround;
	
	@Shadow
	public boolean collidedVertically;
	
	@Shadow public float stepHeight;
	
	@Shadow public abstract double getPosY();
	
	@Shadow protected abstract AxisAlignedBB getBoundingBox(Pose pose);
	
	@Inject(method = "getAllowedMovement(Lnet/minecraft/util/math/vector/Vector3d;)Lnet/minecraft/util/math/vector/Vector3d;", at = @At("HEAD"))
	public void LegacyCollision_preMove(Vector3d pos, CallbackInfoReturnable<Vector3d> cir) {
		if (!CollisionReversionAPI.useCollision() || this.getBoundingBox() == null) return;
		ArrayList<LegacyAxisAlignedBoundingBox> boxes = new ArrayList<>();
		{
			ArrayList<AxisAlignedBB> boundingBoxes = new ArrayList<>();
			{
				AxisAlignedBB aabb = this.getBoundingBox().expand(pos.x, pos.y, pos.z);
				int x1 = MathHelper.floor(aabb.minX) - 1;
				int x2 = MathHelper.ceil(aabb.maxX) + 1;
				int y1 = MathHelper.floor(aabb.minY) - 1;
				int y2 = MathHelper.ceil(aabb.maxY) + 1 + MathHelper.ceil(stepHeight);
				int z1 = MathHelper.floor(aabb.minZ) - 1;
				int z2 = MathHelper.ceil(aabb.maxZ) + 1;
				for (int x = x1; x < x2; x++) {
					for (int y = y1; y < y2; y++) {
						for (int z = z1; z < z2; z++) {
							CollisionLookup.getBoundingBoxes(world, new BlockPos(x, y, z), (Entity) (Object) this, boundingBoxes);
						}
					}
				}
			}
			for (int i = 0; i < boundingBoxes.size(); i++)
				boxes.add(i, new LegacyAxisAlignedBoundingBox(boundingBoxes.get(i)));
			double newX = pos.x;
			double newY = pos.y;
			double newZ = pos.z;
			AxisAlignedBB entityBB = this.getBoundingBox();
			boxes.sort((box1, box2) -> {
				Vector3d box1Dist;
				{
					double xOff = 0;
					double yOff = 0;
					double zOff = 0;
					if (newX != 0) xOff = box1.calculateXOffset(entityBB, newX);
					if (newY != 0) yOff = box1.calculateYOffset(entityBB, newY);
					if (newZ != 0) zOff = box1.calculateZOffset(entityBB, newZ);
					box1Dist = new Vector3d(xOff, yOff, zOff);
				}
				Vector3d box2Dist;
				{
					double xOff = 0;
					double yOff = 0;
					double zOff = 0;
					if (newX != 0) xOff = box2.calculateXOffset(entityBB, newX);
					if (newY != 0) yOff = box2.calculateYOffset(entityBB, newY);
					if (newZ != 0) zOff = box2.calculateZOffset(entityBB, newZ);
					box2Dist = new Vector3d(xOff, yOff, zOff);
				}
				int ySort = Double.compare(box1Dist.y, box2Dist.y);
				if (ySort != 0) return ySort;
				else return Double.compare(box1Dist.distanceTo(Vector3d.ZERO), box2Dist.distanceTo(Vector3d.ZERO));
			});
		}
		double oldX = pos.x;
		double oldY = pos.y;
		double oldZ = pos.z;
		
		double newX = pos.x;
		double newY = pos.y;
		double newZ = pos.z;
		
		double finalX = pos.x;
		double finalY = pos.y;
		double finalZ = pos.z;
		
		double stepY = 0;
		
		AxisAlignedBB thisBB = this.getBoundingBox();
		legacyVerticalCollision = false;
		double stepHeight = this.stepHeight;
		boolean stepAssist = false;
		double stepAssistMagicNumber = stepHeight / 7.5;
		
		boolean cancelXIfStepFail = false;
		boolean cancelZIfStepFail = false;
		
		for (LegacyAxisAlignedBoundingBox box : boxes) {
			if (newY != 0) {
				newY = box.calculateYOffset(this.getBoundingBox(), newY);
				if (newY != oldY) {
					if (oldY < 0) legacyVerticalCollision = true;
					this.setBoundingBox(this.getBoundingBox().offset(0.0D, newY, 0.0D));
					this.setMotion(this.getMotion().mul(1, 0, 1));
					finalY = newY;
					newY = oldY = stepY = 0;
				}
			}
			if (newX != 0) {
				newX = box.calculateXOffset(this.getBoundingBox(), newX);
				if (newX != oldX) {
					if (box.maxY < this.getPosY() + stepHeight) {
						stepY = Math.max((box.maxY - this.getPosY()) + stepAssistMagicNumber, newY);
						cancelXIfStepFail = true;
					} else {
						this.setBoundingBox(this.getBoundingBox().offset(newX, 0.0D, 0.0D));
						finalX = newX;
						newX = oldX = 0;
					}
				}
			}
			if (newZ != 0) {
				newZ = box.calculateZOffset(this.getBoundingBox(), newZ);
				if (newZ != oldZ) {
					if (box.maxY < this.getPosY() + stepHeight) {
						stepY = Math.max((box.maxY - this.getPosY()) + stepAssistMagicNumber, newY);
//						stepAssist = true;
						cancelZIfStepFail = true;
					} else {
						this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, newZ));
						finalZ = newZ;
						newZ = oldZ = 0;
					}
				}
			}
		}
		double startY = this.getBoundingBox().minY;
		for (LegacyAxisAlignedBoundingBox box : boxes) {
			oldY = stepY;
			if (stepY != 0) {
				stepY = box.calculateYOffset(this.getBoundingBox(), stepY);
				if (stepY > oldY) {
					if (oldY < 0) legacyVerticalCollision = true;
					this.setBoundingBox(this.getBoundingBox().offset(0.0D, stepY, 0.0D));
					this.setMotion(this.getMotion().mul(1, 0, 1));
					finalY = stepY;
					stepAssist = true;
//					newY = finalY = oldY = stepY = 0;
//					finalY = newY = oldY = stepY = 0;
				} else if (stepY != oldY) {
					this.setBoundingBox(this.getBoundingBox().offset(0, startY - this.getBoundingBox().minY, 0));
					if (cancelXIfStepFail) {
						finalX = newX = 0;
						newX = oldX = 0;
						this.setBoundingBox(this.getBoundingBox().offset(newX, 0.0D, 0.0D));
					}
					if (cancelZIfStepFail) {
						finalZ = newZ = 0;
						newZ = oldZ = 0;
						this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, newZ));
					}
					finalY = 0;
					stepY = 0;
				}
			}
		}
		if (finalX == 0) this.setMotion(this.getMotion().mul(0, 1, 1));
		if (finalY == 0 && stepY == 0) this.setMotion(this.getMotion().mul(1, 0, 1));
		if (finalZ == 0) this.setMotion(this.getMotion().mul(1, 1, 0));
		this.setBoundingBox(thisBB);
		if (stepAssist) {
//			this.setBoundingBox(this.getBoundingBox().offset(0.0D, stepY, 0.0D));
			finalY = 0;
		}
		pos.x = finalX;
		pos.y = (finalY == 0 ? stepY : finalY);
		pos.z = finalZ;
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;updateFallState(DZLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V"), method = "move")
	public void LegacyCollision_preUpdateFallState(MoverType typeIn, Vector3d pos, CallbackInfo ci) {
		if (legacyVerticalCollision) {
			onGround = true;
			collidedVertically = true;
		}
	}
}
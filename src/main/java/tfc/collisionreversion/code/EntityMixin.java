package tfc.collisionreversion.code;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import tfc.collisionreversion.LegacyAxisAlignedBoundingBox;
import tfc.collisionreversion.api.CollisionReversionAPI;
import tfc.collisionreversion.api.collision.CollisionLookup;

import java.util.ArrayList;
import java.util.function.Consumer;

public class EntityMixin {
	private Entity entity;
	private double stepHeight;
	private World world;
	
	private AxisAlignedBB getBoundingBox() {
		return entity.getBoundingBox();
	}
	
	public void preMove(Vector3d vec, Entity cThis, double stepHeight1, World thisWorld, Consumer<Boolean> verticalCollisionMarker, Consumer<Boolean> horizontalCollisionMarker) {
		entity = cThis;
		if (!CollisionReversionAPI.useCollision() || getBoundingBox() == null) return;
		stepHeight = stepHeight1;
		world = thisWorld;
		ArrayList<LegacyAxisAlignedBoundingBox> boxes = getBoxes(vec);
		calcMove(vec, boxes, verticalCollisionMarker, horizontalCollisionMarker);
	}
	
	private final StepResult stepResult = new StepResult();
	
	private void calcMove(Vector3d motion, ArrayList<LegacyAxisAlignedBoundingBox> boxes, Consumer<Boolean> verticalCollisionMarker, Consumer<Boolean> horizontalCollisionMarker) {
		verticalCollisionMarker.accept(false);
		horizontalCollisionMarker.accept(false);
		AxisAlignedBB workerBB = entity.getBoundingBox();
		Vector3d motionVec = new Vector3d(0, 0, 0); // yes, I am using an immutable class to pull out three doubles
		stepResult.stepUpX = false;
		stepResult.stepUpZ = false;
		workerBB = adjustBoundingBox(workerBB, motion, motionVec, boxes, verticalCollisionMarker, horizontalCollisionMarker, true);
		workerBB = handleStepAssist(workerBB, motion, boxes, verticalCollisionMarker, horizontalCollisionMarker);
		
		double x = motionVec.x;
		double y = motionVec.y;
		double z = motionVec.z;
		
		entity.setMotion(x == 0 ? 0 : entity.getMotion().x, y == 0 ? 0 : entity.getMotion().getY(), z == 0 ? 0 : entity.getMotion().getZ());
		entity.setBoundingBox(workerBB);
		entity.resetPositionToBB();
	}
	
	private AxisAlignedBB handleStepAssist(AxisAlignedBB workerBB, Vector3d motion, ArrayList<LegacyAxisAlignedBoundingBox> boxes, Consumer<Boolean> verticalCollisionMarker, Consumer<Boolean> horizontalCollisionMarker) {
		boolean stepX = stepResult.stepUpX;
		boolean stepZ = stepResult.stepUpZ;
		if (!stepX && !stepZ) return workerBB;
		Vector3d outStep = new Vector3d(0, 0, 0);
		Vector3d outReference = new Vector3d(0, 0, 0);
		
		getOffset(workerBB, new Vector3d(motion.x, stepHeight, motion.z), outStep, boxes);
		getOffset(workerBB.expand(motion.x, 0, motion.z), new Vector3d(motion.x, 0, motion.z), outReference, boxes);
		if (outStep.y <= stepHeight) {
			if (Entity.horizontalMag(outStep) >= Entity.horizontalMag(outReference)) {
				if (Entity.horizontalMag(outStep) > Entity.horizontalMag(motion)) {
					motion.y = outStep.y;
				}
			}
//			Vector3d assist = new Vector3d(0.0D, -outStep.y + motion.y, 0.0D);
			Vector3d assist = new Vector3d(0.0D, -outStep.y, 0.0D);
			assist = getOffset(workerBB.offset(outStep), assist, assist, boxes);
			motion.x += assist.x;
			motion.y += assist.y;
			motion.z += assist.z;
			if (motion.y <= 0) {
				workerBB = adjustBoundingBox(workerBB, motion, motion, boxes, verticalCollisionMarker, horizontalCollisionMarker, false);
			}
//			entity.setBoundingBox(workerBB);
//			entity.resetPositionToBB();
		} else {
			workerBB = adjustBoundingBox(workerBB, motion, motion, boxes, verticalCollisionMarker, horizontalCollisionMarker, false);
		}
		return workerBB;
	}
	
	private Vector3d getOffset(AxisAlignedBB workerBB, Vector3d motion, Vector3d outVec, ArrayList<LegacyAxisAlignedBoundingBox> boxes) {
		double x = motion.x;
		double y = motion.y;
		double z = motion.z;
		
		double offX = 0;
		double offY = 0;
		double offZ = 0;
		
		if (y != 0) {
			for (LegacyAxisAlignedBoundingBox box : boxes) {
				y = box.calculateYOffset(workerBB, y);
				if (y != motion.y) {
					offY += y;
					workerBB = workerBB.offset(0, y, 0);
					motion.y = y = 0;
					break;
				}
			}
		}
		offY += y;
		workerBB = workerBB.offset(0, y, 0);
		
		if (x != 0 && z != 0) {
			for (LegacyAxisAlignedBoundingBox box : boxes) {
				if (x != 0) {
					x = box.calculateXOffset(workerBB, x);
					if (x != motion.x) {
						offX += x;
						workerBB = workerBB.offset(x, 0, 0);
						motion.x = x = 0;
					}
				}
				
				if (z != 0) {
					z = box.calculateZOffset(workerBB, z);
					if (z != motion.z) {
						offZ += z;
						workerBB = workerBB.offset(0, 0, z);
						motion.z = z = 0;
					}
				}
			}
		}
		offX += x;
		offZ += z;
		
		if (outVec != null) {
			outVec.x = offX;
			outVec.y = offY;
			outVec.z = offZ;
		}
		return outVec;
	}
	
	private AxisAlignedBB adjustBoundingBox(AxisAlignedBB workerBB, Vector3d motion, Vector3d outVec, ArrayList<LegacyAxisAlignedBoundingBox> boxes, Consumer<Boolean> verticalCollisionMarker, Consumer<Boolean> horizontalCollisionMarker, boolean allowStepUp) {
		double x = motion.x;
		double y = motion.y;
		double z = motion.z;
		
		allowStepUp = allowStepUp && entity.isOnGround();
		
		if (y != 0) {
			for (LegacyAxisAlignedBoundingBox box : boxes) {
				y = box.calculateYOffset(workerBB, y);
				if (y != motion.y) {
					workerBB = workerBB.offset(0, y, 0);
					motion.y = y = 0;
					verticalCollisionMarker.accept(true);
					break;
				}
			}
		}
		workerBB = workerBB.offset(0, y, 0);
		
		if (x != 0 && z != 0) {
			for (LegacyAxisAlignedBoundingBox box : boxes) {
				double stepY = entity.getPosY() + entity.stepHeight;
				
				if (x != 0) {
					double oldX = x;
					x = box.calculateXOffset(workerBB, x);
					if (x != motion.x) {
						if (!allowStepUp || box.maxY > stepY) {
							workerBB = workerBB.offset(x, 0, 0);
							motion.x = x = 0;
							horizontalCollisionMarker.accept(true);
//							stepResult.stepUpX = false;
						} else {
							x = oldX;
							stepResult.stepUpX = true;
						}
					}
				}
				
				if (z != 0) {
					double oldZ = z;
					z = box.calculateZOffset(workerBB, z);
					if (z != motion.z) {
						if (!allowStepUp || box.maxY > stepY) {
							workerBB = workerBB.offset(0, 0, z);
							motion.z = z = 0;
							horizontalCollisionMarker.accept(true);
//							stepResult.stepUpZ = false;
						} else {
							z = oldZ;
							stepResult.stepUpZ = true;
						}
					}
				}
				if (x == 0 && z == 0) break;
			}
		}
		workerBB = workerBB.offset(0, -y, 0);
		
		if (outVec != null) {
			outVec.x = x;
			outVec.y = y;
			outVec.z = z;
		}
		return workerBB;
	}
	
	private ArrayList<LegacyAxisAlignedBoundingBox> getBoxes(Vector3d motion) {
		ArrayList<LegacyAxisAlignedBoundingBox> boxes = new ArrayList<>();
		{
			ArrayList<AxisAlignedBB> boundingBoxes = new ArrayList<>();
			{
				AxisAlignedBB aabb = getBoundingBox().expand(motion.x, motion.y, motion.z);
				int x1 = MathHelper.floor(aabb.minX) - 1;
				int x2 = MathHelper.ceil(aabb.maxX) + 1;
				int y1 = MathHelper.floor(aabb.minY) - 1;
				int y2 = MathHelper.ceil(aabb.maxY) + 1 + MathHelper.ceil(stepHeight);
				int z1 = MathHelper.floor(aabb.minZ) - 1;
				int z2 = MathHelper.ceil(aabb.maxZ) + 1;
				for (int x = x1; x < x2; x++) {
					for (int y = y1; y < y2; y++) {
						for (int z = z1; z < z2; z++) {
							CollisionLookup.getBoundingBoxes(world, new BlockPos(x, y, z), entity, boundingBoxes);
						}
					}
				}
			}
			for (int i = 0; i < boundingBoxes.size(); i++) {
				AxisAlignedBB bb = boundingBoxes.get(i);
				if (bb == null) continue;
				boxes.add(new LegacyAxisAlignedBoundingBox(bb));
			}
			double newX = motion.x;
			double newY = motion.y;
			double newZ = motion.z;
			AxisAlignedBB entityBB = getBoundingBox();
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
		return boxes;
	}
}

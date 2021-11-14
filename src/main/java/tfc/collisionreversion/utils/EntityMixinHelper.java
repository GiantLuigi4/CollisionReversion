package tfc.collisionreversion.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.collisionreversion.LegacyAxisAlignedBB;
import tfc.collisionreversion.api.CollisionReversionAPI;
import tfc.collisionreversion.api.StepHeightGetter;
import tfc.collisionreversion.api.lookup.CollisionLookup;
import tfc.collisionreversion.api.lookup.LegacyContext;

import java.util.List;
import java.util.function.Consumer;

import static tfc.collisionreversion.utils.CommonUtils.makeList;

public class EntityMixinHelper {
//	Thread thread = Thread.currentThread();
	
	private Entity entity;
	private double stepHeight;
	private World world;
	
	public EntityMixinHelper(Entity entity) {
		this.entity = entity;
	}
	
	private AxisAlignedBB getBoundingBox() {
		return entity.getBoundingBox();
	}
	
	public void preMove(Vector3d vec, Consumer<EnumVCollisionType> verticalCollisionMarker, Consumer<Boolean> horizontalCollisionMarker) {
//		if (Thread.currentThread() != thread) throw new RuntimeException("Thread somehow changed");
		if (!CollisionReversionAPI.useCollision() || getBoundingBox() == null) return;
		stepHeight = StepHeightGetter.getStepHeightFor(entity);
		world = entity.world;
		List<LegacyAxisAlignedBB> boxes = getBoxes(vec);
		calcMove(vec, boxes, verticalCollisionMarker, horizontalCollisionMarker);
	}
	
	private final StepResult stepResult = new StepResult();
	
	private void calcMove(Vector3d motion, List<LegacyAxisAlignedBB> boxes, Consumer<EnumVCollisionType> verticalCollisionMarker, Consumer<Boolean> horizontalCollisionMarker) {
		verticalCollisionMarker.accept(EnumVCollisionType.NONE);
		horizontalCollisionMarker.accept(false);
		AxisAlignedBB workerBB = entity.getBoundingBox();
		Vector3d motionVec = new Vector3d(0, 0, 0); // yes, I am using an immutable class to pull out three doubles
		stepResult.stepUpX = false;
		stepResult.stepUpZ = false;
//		AxisAlignedBB tempBB = workerBB;
//		tempBB = adjustBoundingBox(tempBB, new Vector3d(motion.x, motion.y, motion.z), new Vector3d(0,0 ,0), boxes, (v)->{}, (v)->{}, false);
		workerBB = adjustBoundingBox(workerBB, motion, motionVec, boxes, verticalCollisionMarker, horizontalCollisionMarker, true);
		if (entity.isOnGround()) workerBB = handleStepAssist(workerBB, motion, boxes, verticalCollisionMarker, horizontalCollisionMarker, null);
		
		double x = motionVec.x;
		double y = motionVec.y;
		double z = motionVec.z;
		
		entity.setMotion(x == 0 ? 0 : entity.getMotion().x, y == 0 ? 0 : entity.getMotion().getY(), z == 0 ? 0 : entity.getMotion().getZ());
		entity.setBoundingBox(workerBB);
		entity.resetPositionToBB();
	}
	
	private AxisAlignedBB handleStepAssist(AxisAlignedBB workerBB, Vector3d motion, List<LegacyAxisAlignedBB> boxes, Consumer<EnumVCollisionType> verticalCollisionMarker, Consumer<Boolean> horizontalCollisionMarker, AxisAlignedBB fallbackBB) {
		boolean stepX = stepResult.stepUpX;
		boolean stepZ = stepResult.stepUpZ;
		if (!stepX && !stepZ) return workerBB;
		Vector3d outStep = new Vector3d(0, 0, 0);
		Vector3d outReference = new Vector3d(0, 0, 0);
		
		getOffset(workerBB, new Vector3d(motion.x, stepHeight, motion.z), outStep, boxes);
		getOffset(workerBB.expand(motion.x, 0, motion.z), new Vector3d(motion.x, 0, motion.z), outReference, boxes);
		if (outStep.y <= stepHeight) {
			if (Entity.horizontalMag(outStep) >= Entity.horizontalMag(outReference)) {
				if (Entity.horizontalMag(outStep) < Entity.horizontalMag(motion)) {
					motion.y = outStep.y;
				}
			}
//			Vector3d assist = new Vector3d(0.0D, -outStep.y + motion.y, 0.0D);
			Vector3d assist = new Vector3d(0.0D, -outStep.y, 0.0D);
//			workerBB = workerBB.offset(outStep);
			getOffset(workerBB.offset(outStep), assist, assist, boxes);
//			if (outStep.y <= 0) return workerBB;
			if (outStep.y + assist.y <= 0) {
				motion.y = 0;
//				workerBB = workerBB.offset(outStep.scale(-1));
				workerBB = adjustBoundingBox(workerBB, motion, motion, boxes, verticalCollisionMarker, horizontalCollisionMarker, false);
//				motion.x = 0;
//				motion.y = 0;
//				motion.z = 0;
				return workerBB;
			}
			workerBB = workerBB.offset(0, outStep.y, 0);
			motion.x += assist.x;
			motion.y += assist.y;
			motion.z += assist.z;
//			entity.setBoundingBox(workerBB);
//			entity.resetPositionToBB();
		} else {
			workerBB = adjustBoundingBox(workerBB, motion, motion, boxes, verticalCollisionMarker, horizontalCollisionMarker, false);
		}
		return workerBB;
	}
	
	private Vector3d getOffset(AxisAlignedBB workerBB, Vector3d motion, Vector3d outVec, List<LegacyAxisAlignedBB> boxes) {
		double x = motion.x;
		double y = motion.y;
		double z = motion.z;
		
		double offX = x;
		double offY = 0;
		double offZ = z;
		
		double yOut = y;
		
		if (y != 0) {
			for (LegacyAxisAlignedBB box : boxes) {
				double newYOut = box.calculateYOffset(workerBB, yOut);
				if (Math.abs(newYOut) < Math.abs(yOut)) yOut = newYOut;
//				y = box.calculateYOffset(workerBB, y);
				if (yOut != motion.y) {
//				if (y != motion.y) {
					offY = yOut;
//					offY += y;
					if (yOut < 0) {
						yOut = 0;
						break;
					}
//					motion.y = y = 0;
//					break;
				}
			}
		}
		y = yOut;
		offY += y;
		workerBB = workerBB.offset(0, y, 0);
		
		double xOut = x;
		double zOut = z;
		int iter = 0;
		
		while (
				(
						xOut != 0
								|| zOut != 0
								|| x != 0
								|| z != 0
				)
						&& iter++ < 2
		) {
			for (LegacyAxisAlignedBB box : boxes) {
				if (box == null) continue;
				
				if (xOut != 0) {
					double newXOut = box.calculateXOffset(workerBB, xOut);
					if (Math.abs(newXOut) < Math.abs(xOut)) xOut = newXOut;
					if (xOut != x) {
						offX = xOut;
						if (xOut < 0) xOut = 0;
					}
				}
				
				if (zOut != 0) {
					double newZOut = box.calculateZOffset(workerBB, zOut);
					if (Math.abs(newZOut) < Math.abs(zOut)) zOut = newZOut;
					if (zOut != z) {
						offZ = zOut;
						if (zOut < 0) zOut = 0;
					}
				}
				
				if (xOut == 0 && zOut == 0) break;
			}
			
			if (xOut != x && zOut != z) {
				if (Math.abs(xOut) == Math.abs(zOut)) {
//					workerBB = workerBB.offset(xOut, 0, zOut);
					motion.x = motion.z = x = z = 0;
					break;
				}
			}
			if ((Math.abs(xOut) < Math.abs(zOut) || zOut == z) && xOut != x) {
				workerBB = workerBB.offset(xOut, 0, 0);
				motion.x = x = xOut = 0;
				zOut = z;
				if (iter == 1) offZ = 0;
			} else if (zOut != z) {
				workerBB = workerBB.offset(0, 0, zOut);
				motion.z = z = zOut = 0;
				xOut = x;
				if (iter == 1) offX = 0;
			}
		}
//		offX += x;
//		offZ += z;
		
		if (outVec != null) {
			outVec.x = offX;
			outVec.y = offY;
			outVec.z = offZ;
		}
		return outVec;
	}
	
	private AxisAlignedBB adjustBoundingBox(AxisAlignedBB workerBB, Vector3d motion, Vector3d outVec, List<LegacyAxisAlignedBB> boxes, Consumer<EnumVCollisionType> verticalCollisionMarker, Consumer<Boolean> horizontalCollisionMarker, boolean allowStepUp) {
		double x = motion.x;
		double y = motion.y;
		double z = motion.z;
		
		allowStepUp = allowStepUp && entity.isOnGround();
		
		double yOut = y;
		
		if (y != 0) {
			for (LegacyAxisAlignedBB box : boxes) {
				if (box == null) continue;
				double newYOut = box.calculateYOffset(workerBB, yOut);
				if (Math.abs(newYOut) < Math.abs(yOut)) yOut = newYOut;
////				y = box.calculateYOffset(workerBB, y);
				if (yOut != motion.y) {
////				if (y != motion.y) {
////					workerBB = workerBB.offset(0, y, 0);
//					yOut = 0;
////					motion.y = y = 0;
////					break;
					if (motion.y <= 0) verticalCollisionMarker.accept(EnumVCollisionType.FLOOR);
					else verticalCollisionMarker.accept(EnumVCollisionType.ROOF);
				}
			}
		}
		y = yOut;
		workerBB = workerBB.offset(0, y, 0);
		
		double xOut = x;
		double zOut = z;
		int iter = 0;
		
		boolean checkX;
		boolean checkZ;
		
		while (
				(
						xOut != 0
								|| zOut != 0
								|| x != 0
								|| z != 0
				)
						&& iter++ < 2
		) {
			checkX = true;
			checkZ = true;
			for (LegacyAxisAlignedBB box : boxes) {
				double stepY = entity.getPosY() + stepHeight;
				
				if (checkX && xOut != 0) {
					double newXOut = box.calculateXOffset(workerBB, xOut);
					if (Math.abs(newXOut) < Math.abs(xOut)) {
						if (newXOut != xOut) {
							if (!allowStepUp || box.maxY > stepY) {
								xOut = newXOut;
								horizontalCollisionMarker.accept(true);
								if (xOut >= 0) checkX = false;
								// a box which collides on the x axis should never be able to collide on the z axis as well
								continue;
							} else {
								stepResult.stepUpX = true;
							}
						}
					}
				}
				
				if (checkZ && zOut != 0) {
					double newZOut = box.calculateZOffset(workerBB, zOut);
					if (Math.abs(newZOut) < Math.abs(zOut)) {
						if (newZOut != zOut) {
							if (!allowStepUp || box.maxY > stepY) {
								zOut = newZOut;
								horizontalCollisionMarker.accept(true);
								if (zOut >= 0) checkZ = false;
							} else {
								stepResult.stepUpZ = true;
							}
						}
					}
				}
				if (xOut == 0 && zOut == 0) break;
			}
			
			if (xOut != x && zOut != z) {
				if (Math.abs(xOut) == Math.abs(zOut)) {
					workerBB = workerBB.offset(xOut, 0, zOut);
					x = z = 0;
					break;
				}
			}
			if ((Math.abs(xOut) < Math.abs(zOut) || zOut == z) && xOut != x) {
				workerBB = workerBB.offset(xOut, 0, 0);
				x = xOut = 0;
				zOut = z;
			} else if (zOut != z) {
				workerBB = workerBB.offset(0, 0, zOut);
				z = zOut = 0;
				xOut = x;
			}
		}
		
		motion.x = x;
		motion.y = y;
		motion.z = z;
		
		workerBB = workerBB.offset(0, -y, 0);
		
		if (outVec != null) {
			outVec.x = x;
			outVec.y = y;
			outVec.z = z;
		}
		return workerBB;
	}
	
	private final LegacyContext context = new LegacyContext();
	
	private List<LegacyAxisAlignedBB> getBoxes(Vector3d motion) {
//		if (Thread.currentThread() != thread) throw new RuntimeException("Thread somehow changed");
		List<LegacyAxisAlignedBB> boxes = makeList();
		{
			List<AxisAlignedBB> boundingBoxes = makeList();
			AxisAlignedBB aabb = getBoundingBox()
					.expand(motion.x, motion.y, motion.z)
					.expand(0, stepHeight, 0);
			{
				int x1 = MathHelper.floor(aabb.minX) - 1;
				int x2 = MathHelper.ceil(aabb.maxX) + 1;
				int y1 = MathHelper.floor(aabb.minY) - 1;
				int y2 = MathHelper.ceil(aabb.maxY) + 1;
				int z1 = MathHelper.floor(aabb.minZ) - 1;
				int z2 = MathHelper.ceil(aabb.maxZ) + 1;
				for (int x = x1; x < x2; x++) {
					for (int y = y1; y < y2; y++) {
						for (int z = z1; z < z2; z++) {
//							if (Thread.currentThread() != thread) throw new RuntimeException("Thread somehow changed");
							CollisionLookup.getBoundingBoxes(world, new BlockPos(x, y, z), entity, boundingBoxes, context, aabb, true);
						}
					}
				}
			}
			for (int i = 0; i < boundingBoxes.size(); i++) {
				AxisAlignedBB bb = boundingBoxes.get(i);
				if (bb == null) continue;
				if (!FMLEnvironment.production) {
					if (!aabb.intersects(bb)) {
						throw new RuntimeException("[DEV ENV ERR]: Bounding Box: `" + bb.toString() + "` does not intersect the motion box: `" + aabb + "`");
					}
				}
				if (bb instanceof LegacyAxisAlignedBB) boxes.add((LegacyAxisAlignedBB) bb);
				else boxes.add(new LegacyAxisAlignedBB(bb));
			}
//			boxes.sort((box1, box2)->compareBoxes(entityBB, box1, box2));
//			double newX = motion.x;
//			double newY = motion.y;
//			double newZ = motion.z;
//			boxes.sort((box1, box2)->CommonUtils.compareBoxes(entityBB, box1, box2));
		}
		return boxes;
	}
}

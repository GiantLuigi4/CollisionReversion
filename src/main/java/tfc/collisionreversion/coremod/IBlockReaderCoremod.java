package tfc.collisionreversion.coremod;

import net.minecraft.entity.Entity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import tfc.collisionreversion.Config;
import tfc.collisionreversion.EnumDefaultedBoolean;
import tfc.collisionreversion.api.ContextAABB;
import tfc.collisionreversion.api.lookup.CollisionLookup;
import tfc.collisionreversion.api.lookup.LegacyContext;
import tfc.collisionreversion.api.lookup.SelectionLookup;
import tfc.collisionreversion.api.lookup.VisualShapeLookup;

import java.util.List;

import static tfc.collisionreversion.utils.CommonUtils.makeList;

public class IBlockReaderCoremod {
	public static BlockRayTraceResult raytraceLegacy(BlockRayTraceResult result, RayTraceContext context) {
		IProfiler profiler = null;
		if (context.context.getEntity() != null) profiler = context.context.getEntity().getEntityWorld().getProfiler();
		boolean isProfilerPresent = profiler != null;
		if (isProfilerPresent) profiler.startSection("legacyRaytrace");
		boolean selectionReversion = Config.COMMON.useSelectionReversion.get();
		boolean collisionReversion = Config.COMMON.useCollisionReversion.get();
		boolean visualShapeReversion = Config.COMMON.useVisualShapeReversion.get();
		if (!selectionReversion && !collisionReversion && !visualShapeReversion) {
			if (isProfilerPresent) profiler.endSection();
			return result;
		}
		switch (context.blockMode) {
			case COLLIDER:
				if (!collisionReversion) {
					if (isProfilerPresent) profiler.endSection();
					return result;
				}
				break;
			case OUTLINE:
				if (!selectionReversion) {
					if (isProfilerPresent) profiler.endSection();
					return result;
				}
				break;
			case VISUAL:
				if (!visualShapeReversion) {
					if (isProfilerPresent) profiler.endSection();
					return result;
				}
				break;
			default:
				if (isProfilerPresent) profiler.endSection();
				return result;
		}
		if (isProfilerPresent) profiler.startSection("setup");
		Vector3d eyeVec = context.getStartVec();
		Vector3d endVec = context.getEndVec();
		double bestDist = result.getHitVec().distanceTo(eyeVec);
		BlockRayTraceResult result1 = null;
		Entity entity = context.context.getEntity();
		World world = null;
		if (entity != null) world = entity.getEntityWorld();
		if (isProfilerPresent) profiler.endStartSection("raytrace");
		switch (context.blockMode) {
			case OUTLINE:
				result1 = raytrace(eyeVec, endVec, EnumDefaultedBoolean.ON, bestDist, world, entity, profiler, isProfilerPresent);
				break;
			case COLLIDER:
				result1 = raytrace(eyeVec, endVec, EnumDefaultedBoolean.OFF, bestDist, world, entity, profiler, isProfilerPresent);
				break;
			case VISUAL:
				result1 = raytrace(eyeVec, endVec, EnumDefaultedBoolean.AUTO, bestDist, world, entity, profiler, isProfilerPresent);
				break;
		}
		if (isProfilerPresent) {
			profiler.endSection();
			profiler.endSection();
		}
		if (result1 == null) return result;
		return result1;
	}
	
	private static BlockRayTraceResult raytrace(Vector3d eyeVec, Vector3d endVec, EnumDefaultedBoolean selection, double bestDist, World world, Entity entity, IProfiler profiler, boolean isProfilerPresent) {
		BlockRayTraceResult bestResult = null;
		{
			{
				Vector3d vector3d = endVec.subtract(eyeVec);
				Vector3d vector3d1 = eyeVec.subtract(vector3d.normalize().scale(0.001D));
				eyeVec = vector3d1;
			}
			if (isProfilerPresent) profiler.startSection("region");
			AxisAlignedBB region = new AxisAlignedBB(eyeVec.x, eyeVec.y, eyeVec.z, endVec.x, endVec.y, endVec.z);
			int x1 = MathHelper.floor(region.minX) - 1;
			int x2 = MathHelper.ceil(region.maxX) + 1;
			int y1 = MathHelper.floor(region.minY) - 1;
			int y2 = MathHelper.ceil(region.maxY) + 1;
			int z1 = MathHelper.floor(region.minZ) - 1;
			int z2 = MathHelper.ceil(region.maxZ) + 1;
			List<AxisAlignedBB> boundingBoxes = makeList();
			LegacyContext context = new LegacyContext();
			if (isProfilerPresent) profiler.endStartSection("loop");
			for (int x = x1; x < x2; x++) {
				for (int y = y1; y < y2; y++) {
					for (int z = z1; z < z2; z++) {
						BlockPos pos = new BlockPos(x, y, z);
						if (isProfilerPresent) profiler.startSection("getBoxes");
						if (selection == EnumDefaultedBoolean.ON)
							SelectionLookup.getBoundingBoxes(world, pos, entity, boundingBoxes, context, region, eyeVec, endVec);
						else if (selection == EnumDefaultedBoolean.OFF)
							CollisionLookup.getBoundingBoxes(world, pos, entity, boundingBoxes, context, region, true);
						else
							VisualShapeLookup.getBoundingBoxes(world, pos, entity, boundingBoxes, context, region, eyeVec, endVec);
						boolean invertFace = false;
						AxisAlignedBB resultBB = null;
						if (isProfilerPresent) profiler.endStartSection("checkInside");
						for (int i = 0; i < boundingBoxes.size(); i++) {
							AxisAlignedBB bb = boundingBoxes.get(i);
							if (bb.contains(eyeVec)) {
								invertFace = true;
								resultBB = bb;
							}
							boundingBoxes.set(i, bb.offset(-x, -y, -z));
						}
						BlockRayTraceResult result;
						if (!invertFace) {
							if (isProfilerPresent) profiler.endStartSection("runTrace");
							result = AxisAlignedBB.rayTrace(boundingBoxes, eyeVec, endVec, pos);
						} else {
							if (isProfilerPresent) profiler.endStartSection("calcResult");
							Vector3d vector3d = endVec.subtract(eyeVec);
							Vector3d vector3d1 = eyeVec.add(vector3d.normalize().scale(0.002D));
//							Vector3d vector3d1 = eyeVec;
							result = new BlockRayTraceResult(vector3d1, Direction.getFacingFromVector(vector3d.x, vector3d.y, vector3d.z).getOpposite(), pos, true);
							if (resultBB instanceof ContextAABB) result.hitInfo = ((ContextAABB) resultBB).getContext();
						}
						boundingBoxes.clear();
						if (result == null) {
							if (isProfilerPresent) profiler.endSection();
							continue;
						}
						if (isProfilerPresent) profiler.endStartSection("checkBest");
						double dist1 = result.getHitVec().distanceTo(eyeVec);
						if (dist1 <= bestDist) {
							bestDist = dist1;
							bestResult = result;
							if (invertFace) {
								if (isProfilerPresent) profiler.endSection();
								break;
							}
						}
						if (isProfilerPresent) profiler.endSection();
					}
				}
			}
			if (isProfilerPresent) profiler.endSection();
		}
		return bestResult;
	}
}

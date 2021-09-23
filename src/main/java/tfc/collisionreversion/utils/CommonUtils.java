package tfc.collisionreversion.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import tfc.collisionreversion.LegacyAxisAlignedBB;
import tfc.collisionreversion.api.lookup.CollisionLookup;
import tfc.collisionreversion.api.lookup.LegacyContext;

import java.util.*;

public class CommonUtils {
	public static boolean hasNoCollisions(Entity entity, AxisAlignedBB aabb) {
		World world = entity.world;
		List<AxisAlignedBB> boundingBoxes = makeList();
		{
			int x1 = MathHelper.floor(aabb.minX) - 1;
			int x2 = MathHelper.ceil(aabb.maxX) + 1;
			int y1 = MathHelper.floor(aabb.minY) - 1;
			int y2 = MathHelper.ceil(aabb.maxY) + 1;
			int z1 = MathHelper.floor(aabb.minZ) - 1;
			int z2 = MathHelper.ceil(aabb.maxZ) + 1;
			LegacyContext context = new LegacyContext();
			for (int x = x1; x < x2; x++) {
				for (int y = y1; y < y2; y++) {
					for (int z = z1; z < z2; z++) {
						BlockPos pos = new BlockPos(x, y, z);
						CollisionLookup.getBoundingBoxes(world, pos, entity, boundingBoxes, context, aabb, false);
						for (int i = 0; i < boundingBoxes.size(); i++) {
							AxisAlignedBB boundingBox = boundingBoxes.get(i);
							if (boundingBox == null) continue;
							if (aabb.intersects(boundingBox)) return false;
						}
						boundingBoxes.clear();
					}
				}
			}
		}
		return true;
	}

	private static final boolean isJ8 = System.getProperty("java.specification.version").equals("1.8");
	// I would have J9 here, but apparently my J9 is J12
	private static final boolean isJ12 = System.getProperty("java.specification.version").equals("12");
	private static final boolean isJ16 = System.getProperty("java.specification.version").equals("16");
	private static final boolean isGraal = System.getProperty("java.vendor").startsWith("GraalVM");
	// on J11 graal, my custom list causes large lag spikes
	// however, on J8 graal, it is the best performing
	// and on J16 graal, all lists get equal performance
	// TODO: config on this
	private static boolean useOptimizedList = (isJ8 || isJ16) && isGraal;
	
	/**
	* this was interesting to look into
	*
	* tested with 98304 elements, adding 1 element at a time
	*
	* on J8 ReferenceArrayList performs the best
	 * on J11, CustomArrayList performs the best
	 * on J12, ObjectArrayList performs the best
	 * can't remember why I set J16 to use ReferenceArrayList
	*/
	public static <T> List<T> makeList() {
		if (!useOptimizedList) {
			if (isJ16 || isJ8) return new ReferenceArrayList<>();
			else if (isJ12) return new ObjectArrayList<>();
		}
		return new CustomArrayList<>();
//		return new ReferenceArrayList<>();
//		return NonNullList.create();
//		return new ArrayList<>();
//		return new ObjectArrayList<>();
//		return new LinkedList<>();
	}
	
	public static boolean contains(AxisAlignedBB box1, AxisAlignedBB box2) {
		return box1.contains(box2.minX, box2.minY, box2.minZ) &&
				box1.getXSize() > box2.getXSize() &&
				box1.getYSize() > box2.getYSize() &&
				box1.getZSize() > box2.getZSize();
	}
	
	public static int compareBoxes(AxisAlignedBB entityBB, LegacyAxisAlignedBB box1, LegacyAxisAlignedBB box2) {
//				Vector3d box1Dist;
//				{
//					double xOff = 0;
//					double yOff = 0;
//					double zOff = 0;
//					if (newX != 0) xOff = box1.calculateXOffset(entityBB, newX);
//					if (newY != 0) yOff = box1.calculateYOffset(entityBB, newY);
//					if (newZ != 0) zOff = box1.calculateZOffset(entityBB, newZ);
//					box1Dist = new Vector3d(xOff, yOff, zOff);
//				}
//				Vector3d box2Dist;
//				{
//					double xOff = 0;
//					double yOff = 0;
//					double zOff = 0;
//					if (newX != 0) xOff = box2.calculateXOffset(entityBB, newX);
//					if (newY != 0) yOff = box2.calculateYOffset(entityBB, newY);
//					if (newZ != 0) zOff = box2.calculateZOffset(entityBB, newZ);
//					box2Dist = new Vector3d(xOff, yOff, zOff);
//				}
//				int ySort = Double.compare(box1Dist.y, box2Dist.y);
		int ySort = Double.compare(box1.shortestDistY(entityBB), box2.shortestDistY(entityBB));
		if (ySort != 0) return -ySort;
//				else return Double.compare(box1Dist.distanceTo(Vector3d.ZERO), box2Dist.distanceTo(Vector3d.ZERO));
		else return Double.compare(box1.shortestDist(entityBB), box2.shortestDist(entityBB));
	}
}

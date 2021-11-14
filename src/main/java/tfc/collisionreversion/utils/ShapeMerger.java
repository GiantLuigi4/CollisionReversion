package tfc.collisionreversion.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

/**
 * This tries to optimize the list of boxes which it is given, I'd advise only using this if you plan to cache the output
 */
public class ShapeMerger {
	private final List<AxisAlignedBB> boxesOut;
	private List<AxisAlignedBB> workerList = CommonUtils.makeList();
	
	public ShapeMerger(List<AxisAlignedBB> boxesOut) {
		this.boxesOut = boxesOut;
	}
	
	public void addBox(AxisAlignedBB boundingBox) {
		workerList.add(boundingBox);
		// doing this once here so it can gradually make it as simple as possible as boxes get added
		// will probably not completely optimize it, but it will optimize it well enough
		checkMerge();
	}
	
	// TODO: optimize this more
	public boolean checkMerge() {
		boolean mergedAny = false;
		List<AxisAlignedBB> out = CommonUtils.makeList();
		List<AxisAlignedBB> used = CommonUtils.makeList();
		for (AxisAlignedBB axisAlignedBB : workerList) {
			if (used.contains(axisAlignedBB)) continue; // if box1 has already been merged into something, skip it
			for (AxisAlignedBB alignedBB : workerList) {
				if (used.contains(alignedBB)) continue; // if box2 has already been merged into something, skip it
				// if box1 completely contains box2, remove box2
				if (CommonUtils.contains(axisAlignedBB, alignedBB)) {
					used.add(alignedBB);
					mergedAny = true;
					continue;
				}
				// if box1 and box2 have one side which is equal and are directly touching eachother, merge them into one
				Pair<AxisAlignedBB, AxisAlignedBB> pair = merge(axisAlignedBB, alignedBB);
				if (pair.getSecond() == null) {
					out.add(pair.getFirst());
					used.add(axisAlignedBB);
					used.add(alignedBB);
					mergedAny = true;
				}
			}
		}
		// remove all boxes which have been merged into another box
		workerList.removeAll(used);
		// add all the merged boxes
		workerList.addAll(out);
		return mergedAny;
	}
	
	private Pair<AxisAlignedBB, AxisAlignedBB> merge(AxisAlignedBB box1, AxisAlignedBB box2) {
		if (box1.contains(box2.minX, box2.minY, box2.minZ) &&
				box1.contains(box2.maxX, box2.maxY, box2.maxZ))
			return Pair.of(box1, null);
		if (box2.contains(box1.minX, box1.minY, box1.minZ) &&
				box2.contains(box1.maxX, box1.maxY, box1.maxZ))
			return Pair.of(box2, null);
		
		if (
				box1.maxZ == box2.maxZ &&
						box1.minZ == box2.minZ &&
						box1.maxY == box2.maxY &&
						box1.minY == box2.minY
		) {
			if (box1.maxX == box2.minX)
				return Pair.of(new AxisAlignedBB(box1.minX, box1.minY, box1.minZ, box2.maxX, box2.maxY, box2.maxZ), null);
			else if (box2.maxX == box1.minX)
				return Pair.of(new AxisAlignedBB(box2.minX, box1.minY, box1.minZ, box1.maxX, box2.maxY, box2.maxZ), null);
		}
		if (
				box1.maxX == box2.maxX &&
						box1.minX == box2.minX &&
						box1.maxY == box2.maxY &&
						box1.minY == box2.minY
		) {
			if (box1.maxZ == box2.minZ)
				return Pair.of(new AxisAlignedBB(box1.minX, box1.minY, box1.minZ, box2.maxX, box2.maxY, box2.maxZ), null);
			else if (box2.maxZ == box1.minZ)
				return Pair.of(new AxisAlignedBB(box1.minX, box1.minY, box2.minZ, box2.maxX, box2.maxY, box1.maxZ), null);
		}
		if (
				box1.maxX == box2.maxX &&
						box1.minX == box2.minX &&
						box1.maxZ == box2.maxZ &&
						box1.minZ == box2.minZ
		) {
			if (box1.maxY == box2.minY)
				return Pair.of(new AxisAlignedBB(box1.minX, box1.minY, box1.minZ, box2.maxX, box2.maxY, box2.maxZ), null);
			else if (box2.maxY == box1.minY)
				return Pair.of(new AxisAlignedBB(box1.minX, box2.minY, box1.minZ, box2.maxX, box1.maxY, box2.maxZ), null);
		}
		
		return Pair.of(box1, box2);
	}
	
	public void finish() {
		while (checkMerge()) ;
		boxesOut.addAll(workerList);
	}
}

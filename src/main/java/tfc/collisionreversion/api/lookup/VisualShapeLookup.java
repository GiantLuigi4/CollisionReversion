package tfc.collisionreversion.api.lookup;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import tfc.collisionreversion.api.IContextConsumer;
import tfc.collisionreversion.api.ILegacyContext;

import java.util.ArrayList;
import java.util.List;

/**
 * visual shape is what the third person camera raytraces against
 * adding boxes to this will add a box which will obstruct the camera
 */
public class VisualShapeLookup {
	private static final ArrayList<IContextConsumer<ILegacyContext>> boxFillers = new ArrayList<>();
	
	public static List<AxisAlignedBB> getBoundingBoxes(World world, BlockPos pos, Entity entity, List<AxisAlignedBB> boundingBoxes, LegacyContext context, AxisAlignedBB box, Vector3d start, Vector3d end) {
		for (IContextConsumer<ILegacyContext> boxFiller : boxFillers) {
			context.boxes = boundingBoxes;
			context.pos = pos;
			context.world = world;
			context.entity = entity;
			context.state = null;
			context.motionBox = box;
			context.boxCheck = false;
			context.start = start;
			context.end = end;
			boxFiller.accept(context);
		}
		return boundingBoxes;
	}
	
	public static void registerBoxFiller(IContextConsumer<ILegacyContext> filler) {
		boxFillers.add(filler);
	}
}

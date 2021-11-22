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
 * Selection shapes is what the cursor raytraces over
 * Adding a box to this will add a box which can be selected by the player to select the targetted block
 */
public class SelectionLookup {
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

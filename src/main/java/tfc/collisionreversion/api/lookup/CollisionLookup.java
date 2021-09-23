package tfc.collisionreversion.api.lookup;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tfc.collisionreversion.api.IContextConsumer;
import tfc.collisionreversion.api.ILegacyContext;
import tfc.collisionreversion.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class CollisionLookup {
	private static final ArrayList<IContextConsumer<ILegacyContext>> boxFillers = new ArrayList<>();
	
	public static List<AxisAlignedBB> getBoundingBoxes(World world, BlockPos pos, Entity entity, List<AxisAlignedBB> boundingBoxes, LegacyContext context, AxisAlignedBB box, boolean checkBox) {
		for (IContextConsumer<ILegacyContext> boxFiller : boxFillers) {
			context.boxes = boundingBoxes;
			context.pos = pos;
			context.world = world;
			context.entity = entity;
			context.state = null;
			context.motionBox = box;
			context.boxCheck = checkBox;
			boxFiller.accept(context);
		}
		// TODO: optimize collision shapes
		return boundingBoxes;
	}
	
	public static void registerBoxFiller(IContextConsumer<ILegacyContext> filler) {
		System.out.println("Registering a box filler");
		boxFillers.add(filler);
	}
}

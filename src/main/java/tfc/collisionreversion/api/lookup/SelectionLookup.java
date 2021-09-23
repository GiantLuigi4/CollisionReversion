package tfc.collisionreversion.api.lookup;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tfc.collisionreversion.api.IContextConsumer;
import tfc.collisionreversion.api.ILegacyContext;

import java.util.ArrayList;
import java.util.List;

public class SelectionLookup {
	private static final ArrayList<IContextConsumer<ILegacyContext>> boxFillers = new ArrayList<>();
	
	public static List<AxisAlignedBB> getBoundingBoxes(World world, BlockPos pos, Entity entity, List<AxisAlignedBB> boundingBoxes, LegacyContext context, AxisAlignedBB box) {
		for (IContextConsumer<ILegacyContext> boxFiller : boxFillers) {
			context.boxes = boundingBoxes;
			context.pos = pos;
			context.world = world;
			context.entity = entity;
			context.state = null;
			context.motionBox = box;
			context.boxCheck = false;
			boxFiller.accept(context);
		}
		return boundingBoxes;
	}
	
	public static void registerBoxFiller(IContextConsumer<ILegacyContext> filler) {
		boxFillers.add(filler);
	}
}

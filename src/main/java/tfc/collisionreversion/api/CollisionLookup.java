package tfc.collisionreversion.api;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.function.Consumer;

public class CollisionLookup {
	private static final ArrayList<Consumer<CollisionContext>> boxFillers = new ArrayList<>();
	
	private static final CollisionContext context = new CollisionContext();
	
	public static ArrayList<AxisAlignedBB> getBoundingBoxes(World world, BlockPos pos, Entity entity, ArrayList<AxisAlignedBB> boundingBoxes) {
		for (Consumer<CollisionContext> boxFiller : boxFillers) {
			context.boxes = boundingBoxes;
			context.pos = pos;
			context.world = world;
			context.entity = entity;
			boxFiller.accept(context);
		}
		return boundingBoxes;
	}
	
	public static void registerBoxFiller(Consumer<CollisionContext> filler) {
		boxFillers.add(filler);
	}
}

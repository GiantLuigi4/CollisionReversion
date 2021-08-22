package tfc.collisionreversion.api.selection;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.function.Consumer;

public class SelectionLookup {
	private static final ArrayList<Consumer<SelectionContext>> boxFillers = new ArrayList<>();
	
	private static final SelectionContext context = new SelectionContext();
	
	public static ArrayList<AxisAlignedBB> getBoundingBoxes(World world, BlockPos pos, Entity entity, ArrayList<AxisAlignedBB> boundingBoxes) {
		for (Consumer<SelectionContext> boxFiller : boxFillers) {
			context.boxes = boundingBoxes;
			context.pos = pos;
			context.world = world;
			context.entity = entity;
			context.state = null;
			boxFiller.accept(context);
		}
		return boundingBoxes;
	}
	
	public static void registerBoxFiller(Consumer<SelectionContext> filler) {
		boxFillers.add(filler);
	}
}

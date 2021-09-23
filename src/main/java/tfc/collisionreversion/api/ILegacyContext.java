package tfc.collisionreversion.api;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;

import java.util.List;

public interface ILegacyContext {
	Entity getEntity();
	
	World getWorld();
	
	BlockPos getPos();
	
	List<AxisAlignedBB> getBoxes();
	
	ISelectionContext getSelectionContext();
	
	BlockState getBlockState();
	
	/**
	 * for sake of optimization, only add a box if it intersects this one
	 * this is the entity's bounding box expanded by the motion vector
	 *
	 * @return the box which contains all possible points for the entity to collide with
	 */
	AxisAlignedBB boundingBox();
	
	/**
	 * checks if the aabb can be collided with in the current game tick
	 * if the bounding box provided is within the entity's bounding box, then it cannot be collided with (entity is already inside it, and thus the two edges cannot meet)
	 * if the bounding box is outside of the motion bounding box, then it cannot be collided with (edge of motion bounding box is the edge of the area which the entity will move to within the current tick)
	 *
	 * @param box the collision box you want to check
	 * @return if it meets the criteria above
	 */
	boolean checkBoundingBox(AxisAlignedBB box);
}

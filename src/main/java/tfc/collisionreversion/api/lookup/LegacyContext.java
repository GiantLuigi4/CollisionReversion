package tfc.collisionreversion.api.lookup;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.EntitySelectionContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import net.minecraftforge.server.permission.context.IContext;
import tfc.collisionreversion.api.ILegacyContext;

import java.util.ArrayList;
import java.util.List;

import static tfc.collisionreversion.utils.CommonUtils.contains;

public class LegacyContext implements ILegacyContext {
	protected Entity entity;
	protected World world;
	protected BlockPos pos;
	protected List<AxisAlignedBB> boxes;
	protected BlockState state = null;
	protected EntitySelectionContext context;
	protected AxisAlignedBB motionBox;
	protected boolean boxCheck;
	
	public Entity getEntity() {
		return entity;
	}
	
	public World getWorld() {
		return world;
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public List<AxisAlignedBB> getBoxes() {
		return boxes;
	}
	
	public ISelectionContext getSelectionContext() {
		if (context == null || context.getEntity() != entity)
			context = (EntitySelectionContext) ISelectionContext.forEntity(entity);
		return context;
	}
	
	public BlockState getBlockState() {
		if (state == null) state = world.getBlockState(pos);
		return state;
	}
	
	/**
	 * for sake of optimization, only add a box if it intersects this one
	 * this is the entity's bounding box expanded by the motion vector
	 *
	 * @return the box which contains all possible points for the entity to collide with
	 */
	@Override
	public AxisAlignedBB boundingBox() {
		return motionBox;
	}
	
	/**
	 * checks if the aabb can be collided with in the current game tick
	 * if the bounding box provided is within the entity's bounding box, then it cannot be collided with (entity is already inside it, and thus the two edges cannot meet)
	 * if the bounding box is outside of the motion bounding box, then it cannot be collided with (edge of motion bounding box is the edge of the area which the entity will move to within the current tick)
	 *
	 * @param box the collision box you want to check
	 * @return if it meets the criteria above
	 */
	@Override
	public boolean checkBoundingBox(AxisAlignedBB box) {
		if (boxCheck) {
			if (!motionBox.intersects(box)) return false;
//			return !contains(entity.getBoundingBox(), box);
			return !entity.getBoundingBox().intersects(box);
//			return true;
		}
//		return !entity.getBoundingBox().intersects(box);
		return true;
	}
}
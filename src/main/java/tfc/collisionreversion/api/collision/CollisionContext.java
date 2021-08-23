package tfc.collisionreversion.api.collision;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.EntitySelectionContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;

import java.util.ArrayList;

public class CollisionContext {
	protected Entity entity;
	protected World world;
	protected BlockPos pos;
	protected ArrayList<AxisAlignedBB> boxes;
	protected BlockState state = null;
	protected EntitySelectionContext context;
	
	public Entity getEntity() {
		return entity;
	}
	
	public World getWorld() {
		return world;
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public ArrayList<AxisAlignedBB> getBoxes() {
		return boxes;
	}
	
	public ISelectionContext getContext() {
		if (context == null || context.getEntity() != entity) context = (EntitySelectionContext) ISelectionContext.forEntity(entity);
		return context;
	}
	
	public BlockState getBlockState() {
		if (state == null) state = world.getBlockState(pos);
		return state;
	}
}

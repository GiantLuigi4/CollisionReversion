package tfc.collisionreversion.api.selection;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class SelectionContext {
	protected Entity entity;
	protected World world;
	protected BlockPos pos;
	protected ArrayList<AxisAlignedBB> boxes;
	protected BlockState state = null;
	
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
	
	public BlockState getBlockState() {
		if (state == null) state = world.getBlockState(pos);
		return state;
	}
}

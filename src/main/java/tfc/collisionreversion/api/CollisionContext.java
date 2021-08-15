package tfc.collisionreversion.api;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class CollisionContext {
	protected Entity entity;
	protected World world;
	protected BlockPos pos;
	protected ArrayList<AxisAlignedBB> boxes;
	
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
}

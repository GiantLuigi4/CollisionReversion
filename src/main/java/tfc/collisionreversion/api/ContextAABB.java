package tfc.collisionreversion.api;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

// this is only for raytrace shapes
// this allows you to add additional data to the raytrace result
public class ContextAABB extends AxisAlignedBB {
	private Object context;
	
	public ContextAABB(double x1, double y1, double z1, double x2, double y2, double z2) {
		super(x1, y1, z1, x2, y2, z2);
	}
	
	public ContextAABB(BlockPos pos) {
		super(pos);
	}
	
	public ContextAABB(BlockPos pos1, BlockPos pos2) {
		super(pos1, pos2);
	}
	
	public ContextAABB(Vector3d min, Vector3d max) {
		super(min, max);
	}
	
	public void setContext(Object obj) {
		context = obj;
	}
	
	public Object getContext() {
		return context;
	}
}

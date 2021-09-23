package tfc.collisionreversion;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class AdjustmentAABB extends LegacyAxisAlignedBB {
	public AdjustmentAABB(double x1, double y1, double z1, double x2, double y2, double z2) {
		super(x1, y1, z1, x2, y2, z2);
	}
	
	public AdjustmentAABB(BlockPos pos) {
		super(pos);
	}
	
	public AdjustmentAABB(BlockPos pos1, BlockPos pos2) {
		super(pos1, pos2);
	}
	
	public AdjustmentAABB(Vector3d min, Vector3d max) {
		super(min, max);
	}
	
	public AdjustmentAABB(AxisAlignedBB bb) {
		super(bb);
	}
}

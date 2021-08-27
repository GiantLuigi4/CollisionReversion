package tfc.collisionreversion;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class LegacyAxisAlignedBB extends AxisAlignedBB {
	public LegacyAxisAlignedBB(double x1, double y1, double z1, double x2, double y2, double z2) {
		super(x1, y1, z1, x2, y2, z2);
	}
	
	public LegacyAxisAlignedBB(BlockPos pos) {
		super(pos);
	}
	
	public LegacyAxisAlignedBB(BlockPos pos1, BlockPos pos2) {
		super(pos1, pos2);
	}
	
	public LegacyAxisAlignedBB(Vector3d min, Vector3d max) {
		super(min, max);
	}
	
	public LegacyAxisAlignedBB(AxisAlignedBB bb) {
		this(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
	}
	
	public double calculateXOffset(AxisAlignedBB other, double offsetX) {
		if (other.maxY > this.minY && other.minY < this.maxY && other.maxZ > this.minZ && other.minZ < this.maxZ) {
			if (offsetX > 0.0D && other.maxX <= this.minX) {
				double deltaX = this.minX - other.maxX;
				
				if (deltaX < offsetX) offsetX = deltaX;
			} else if (offsetX < 0.0D && other.minX >= this.maxX) {
				double deltaX = this.maxX - other.minX;
				
				if (deltaX > offsetX) offsetX = deltaX;
			}
		}
		return offsetX;
	}
	
	public double calculateYOffset(AxisAlignedBB other, double offsetY) {
		if (other.maxX > this.minX && other.minX < this.maxX && other.maxZ > this.minZ && other.minZ < this.maxZ) {
			if (offsetY > 0.0D && other.maxY <= this.minY) {
				double d1 = this.minY - other.maxY;
				
				if (d1 < offsetY) {
					offsetY = d1;
				}
			} else if (offsetY < 0.0D && other.minY >= this.maxY) {
				double d0 = this.maxY - other.minY;
				
				if (d0 > offsetY) {
					offsetY = d0;
				}
			}
		}
		return offsetY;
	}
	
	public double calculateZOffset(AxisAlignedBB other, double offsetZ) {
		if (other.maxX > this.minX && other.minX < this.maxX && other.maxY > this.minY && other.minY < this.maxY) {
			if (offsetZ > 0.0D && other.maxZ <= this.minZ) {
				double d1 = this.minZ - other.maxZ;
				
				if (d1 < offsetZ) {
					offsetZ = d1;
				}
			} else if (offsetZ < 0.0D && other.minZ >= this.maxZ) {
				double d0 = this.maxZ - other.minZ;
				
				if (d0 > offsetZ) {
					offsetZ = d0;
				}
			}
		}
		return offsetZ;
	}
	
	public double shortestDist(AxisAlignedBB otherBB) {
		if (otherBB.intersects(this)) return 0;
		double x = shortestDistX(otherBB);
		double y = shortestDistY(otherBB);
		double z = shortestDistZ(otherBB);
		return MathHelper.sqrt(x * x + y * y + z * z);
	}
	
	public double shortestDistX(AxisAlignedBB otherBB) {
		if (this.minX < otherBB.minX && this.maxX > otherBB.maxX) return 0;
		if (this.maxX < otherBB.minX) return Math.abs(this.maxX - otherBB.minX);
		return Math.abs(this.minX - otherBB.maxX);
	}
	
	public double shortestDistY(AxisAlignedBB otherBB) {
		if (this.minY < otherBB.minY && this.maxY > otherBB.maxY) return 0;
		if (this.maxY < otherBB.minY) return Math.abs(this.maxZ - otherBB.minY);
		return Math.abs(this.minY - otherBB.maxY);
	}
	
	public double shortestDistZ(AxisAlignedBB otherBB) {
		if (this.minZ < otherBB.minZ && this.maxZ > otherBB.maxZ) return 0;
		if (this.maxZ < otherBB.minZ) return Math.abs(this.maxZ - otherBB.minZ);
		return Math.abs(this.minZ - otherBB.maxZ);
	}
}

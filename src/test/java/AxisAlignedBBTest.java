import tfc.collisionreversion.LegacyAxisAlignedBB;

public class AxisAlignedBBTest {
	public static void main(String[] args) {
		LegacyAxisAlignedBB box1 = new LegacyAxisAlignedBB(0, 0, 0, 1, 1, 1);
		LegacyAxisAlignedBB box2 = new LegacyAxisAlignedBB(10, 23, -51, 11, 24, -50);
		
		System.out.println(box1.shortestDistX(box2));
		System.out.println(box1.shortestDistZ(box2));
	}
}

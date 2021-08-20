package tfc.collisionreversion.api;

public class CollisionReversionAPI {
	private static boolean collisionEnabled = true;
	
	public static boolean useCollision() {
		return collisionEnabled;
	}
	
	private static boolean selectionEnabled = true;
	
	public static boolean useSelection() {
		return selectionEnabled;
	}
}

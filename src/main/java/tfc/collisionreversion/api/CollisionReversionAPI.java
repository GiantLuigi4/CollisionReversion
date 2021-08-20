package tfc.collisionreversion.api;

import tfc.collisionreversion.Config;

public class CollisionReversionAPI {
//	private static boolean collisionEnabled = true;
	
	public static boolean useCollision() {
		return Config.COMMON.useCollisionReversion.get();
	}

//	private static boolean selectionEnabled = true;
	
	public static boolean useSelection() {
		return Config.COMMON.useSelectionReversion.get();
	}
}

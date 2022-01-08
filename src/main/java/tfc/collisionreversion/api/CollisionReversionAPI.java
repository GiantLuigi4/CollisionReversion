package tfc.collisionreversion.api;

import tfc.collisionreversion.Config;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "unused", "RedundantSuppression"})
public class CollisionReversionAPI {
	public static boolean useCollision() {
		return Config.COMMON.useCollisionReversion.get();
	}

	public static boolean useSelection() {
		return Config.COMMON.useSelectionReversion.get();
	}

	public static boolean useVisualShapeReversion() {
		return Config.COMMON.useVisualShapeReversion.get();
	}
}

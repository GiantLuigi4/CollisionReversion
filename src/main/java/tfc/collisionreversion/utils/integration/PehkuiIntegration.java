package tfc.collisionreversion.utils.integration;

import tfc.collisionreversion.api.StepHeightGetter;
import virtuoel.pehkui.api.ScaleType;

public class PehkuiIntegration {
	public static void setup() {
		StepHeightGetter.registerHeightModifier((entity, aDouble) -> ScaleType.MOTION.getScaleData(entity).getScale(1) * aDouble);
	}
}

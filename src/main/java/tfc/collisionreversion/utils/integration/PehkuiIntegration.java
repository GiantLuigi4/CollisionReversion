package tfc.collisionreversion.utils.integration;

import net.minecraft.util.ResourceLocation;
import tfc.collisionreversion.api.StepHeightGetter;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;

public class PehkuiIntegration {
	private static final ScaleType motion =
			ScaleRegistries.getEntry(ScaleRegistries.SCALE_TYPES, new ResourceLocation("pehkui", "motion"));
	
	public static void setup() {
		StepHeightGetter.registerHeightModifier((entity, aDouble) -> motion.getScaleData(entity).getScale(1) * aDouble);
	}
}

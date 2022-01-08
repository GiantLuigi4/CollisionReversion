package tfc.collisionreversion.utils.integration;

import net.minecraft.util.ResourceLocation;
import tfc.collisionreversion.api.StepHeightGetter;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;

public class PehkuiIntegration {
	private static final ScaleType motion = getType();
	
	public static void setup() {
		StepHeightGetter.registerHeightModifier((entity, aDouble) -> motion.getScaleData(entity).getScale(1) * aDouble);
	}
	
	public static ScaleType getType() {
		ScaleType t = ScaleRegistries.getEntry(ScaleRegistries.SCALE_TYPES, new ResourceLocation("pehkui", "step_height"));
		if (t == null) t = ScaleRegistries.getEntry(ScaleRegistries.SCALE_TYPES, new ResourceLocation("pehkui", "motion"));
		return t;
	}
}

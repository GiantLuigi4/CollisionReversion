package tfc.collisionreversion.api;

import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.function.BiFunction;

public class StepHeightGetter {
	private static final ArrayList<BiFunction<Entity, Double, Double>> stepHeightModifiers = new ArrayList<>();
	
	public static double getStepHeightFor(Entity entity) {
		double stepHeight = entity.stepHeight;
		for (BiFunction<Entity, Double, Double> stepHeightModifier : stepHeightModifiers) stepHeight = stepHeightModifier.apply(entity, stepHeight);
		return stepHeight;
	}
	
	public static void registerHeightModifier(BiFunction<Entity, Double, Double> modifier) {
		stepHeightModifiers.add(modifier);
	}
}

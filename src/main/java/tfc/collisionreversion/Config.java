package tfc.collisionreversion;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;

public class Config {
	public static final Config COMMON;
	static final ForgeConfigSpec commonSpec;
	
	static {
		{
			final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
			commonSpec = specPair.getRight();
			COMMON = specPair.getLeft();
		}
	}
	
	/* Collision Options */
	public final ForgeConfigSpec.BooleanValue useCollisionReversion;
	public final ForgeConfigSpec.BooleanValue globalLegacy;
	public final ForgeConfigSpec.BooleanValue cancelVanillaCollision;
	
	/* Selection Options */
	public final ForgeConfigSpec.BooleanValue useSelectionReversion;
	
	/* Visual Shape Options */
	public final ForgeConfigSpec.BooleanValue useVisualShapeReversion;
	
	/* list options */
	public final ForgeConfigSpec.EnumValue<EnumDefaultedBoolean> allowCustomList;
	public final ForgeConfigSpec.EnumValue<EnumListType> listType;
	public final ForgeConfigSpec.IntValue minGrowth;
	public final ForgeConfigSpec.IntValue listGrowthRate;
	
	/* Dev Envro Options */
	public final DevOptions devOptions;
	
	public static final class DevOptions {
		/* Collision Options */
		public final ForgeConfigSpec.BooleanValue collisionNonsense;
		public final ForgeConfigSpec.IntValue collisionNonsenseSize;
		public final ForgeConfigSpec.BooleanValue trapEntities;
		public final ForgeConfigSpec.DoubleValue trapPadding;
		
		/* Selection Options */
		public final ForgeConfigSpec.BooleanValue fullSelectionShapes;
		
		public DevOptions(ForgeConfigSpec.Builder builder) {
			builder.comment("Dev Envro Options").push("dev");
			
			{
				builder.comment("Collision Settings").push("collision");
				
				collisionNonsense = builder
						.comment("Splits blocks into a N*N*N collision grid, for sake of testing performance")
						.translation("config.collisionreversion.collision_nonsense")
						.define("CollisionNonsense", false);
				
				collisionNonsenseSize = builder
						.comment("Size of the grid to split the collision into\nMin: 1, Max: 64")
						.translation("config.collisionreversion.collision_nonsense_size")
						.defineInRange("CollisionNonsenseSize", 16, 1, 64);
				
				trapEntities = builder
						.comment("Traps all entities using a box surrounding their collision box, used for testing precision of the mod")
						.translation("config.collisionreversion.trap_entities")
						.define("TrapEntities", false);
				
				trapPadding = builder
						.comment("Padding on the entity trap\nMin: 0, Max:1000")
						.translation("config.collisionreversion.trap_padding")
						.defineInRange("TrapPadding", 0.0, 0, 1000);
				
				builder.pop();
			}
			
			{
				builder.comment("Selection Settings").push("selection");
				
				fullSelectionShapes = builder
						.comment("If legacy collision should make all blocks be a full block AABB for selection")
						.translation("config.collisionreversion.full_aabbs")
						.define("FullSelectionBoxes", false);
				
				builder.pop();
			}
			
			builder.pop();
		}
	}
	
	public Config(ForgeConfigSpec.Builder builder) {
		{
			builder.comment("Selection Settings").push("selection");
			
			useSelectionReversion = builder
					.comment("If the mod show allow selection reversion")
					.translation("config.collisionreversion.selection")
					.define("SelectionReversion", true);
			
			builder.pop();
		}
		
		{
			builder.comment("Visual Shape Settings").push("visual shape");
			
			useVisualShapeReversion = builder
					.comment("If the mod show allow visual (third person camera obstructing) shape reversion")
					.translation("config.collisionreversion.visual")
					.define("VisualShapeReversion", true);
			
			builder.pop();
		}
		
		{
			builder.comment("List Settings").push("lists");
			
			{
				EnumDefaultedBoolean[] acceptableValues = EnumDefaultedBoolean.values();
				allowCustomList = defineEnum(
						builder
								.comment("Collision Reversion has it's own array list implementation, which can sometimes outperform the standard JDK's array list implementation.")
								.translation("config.collisionreversion.allow_custom_arraylist"),
						"CustomArrayList", acceptableValues);
			}
			
			{
				EnumListType[] acceptableValues = EnumListType.values();
				listType = defineEnum(
						builder
								.comment("NoCommentYet")
								.translation("config.collisionreversion.list_type"),
						"ListType", acceptableValues);
			}
			
			{
				builder.comment("Custom List Options").push("custom_list");
				
				listGrowthRate = builder
						.comment("When a list is expanded, it doesn't gain one single element, it gains multiple elements.\nThis option is the number to multiply the length by each time the list must be expanded.")
						.translation("config.collisionreversion.growth_multiplier")
						.defineInRange("GrowthMultiplier", 3, 2, 100);
				
				minGrowth = builder
						.comment("Minimum amount to expand the list by when a new element is added")
						.translation("config.collisionreversion.min_growth")
						.defineInRange("MinGrowth", 1, 1, 1000);
				
				builder.pop();
			}
			
			builder.pop();
		}
		
		{
			builder.comment("Collision Settings").push("collision");
			
			useCollisionReversion = builder
					.comment("If the mod show allow collision reversion")
					.translation("config.collisionreversion.collision")
					.define("CollisionReversion", true);
			
			globalLegacy = builder
					.comment("Causes collision reversion to use 1.12 collision for vanilla blocks along side 1.16 collision\nSlightly slower, but a lot more precise")
					.translation("config.collisionreversion.global_legacy")
					.define("GlobalLegacy", !FMLEnvironment.production);
			
			cancelVanillaCollision = builder
					.comment("Cancels vanilla collision entirely, making the game only use collision reversion's collision system\nThis will change how the game plays, granted probably will be hard to notice")
					.translation("config.collisionreversion.legacy_only")
					.define("LegacyOnly", !FMLEnvironment.production);
			
			builder.pop();
		}
		
		if (!FMLEnvironment.production) devOptions = new DevOptions(builder);
		else devOptions = null;
	}
	
	private static <T extends Enum<T>> ForgeConfigSpec.EnumValue<T> defineEnum(ForgeConfigSpec.Builder builder, String name, T[] values) {
		return builder.defineEnum(name, (T) values[0], Arrays.asList(values));
	}
}

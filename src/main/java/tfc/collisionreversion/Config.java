package tfc.collisionreversion;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.tuple.Pair;

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
}

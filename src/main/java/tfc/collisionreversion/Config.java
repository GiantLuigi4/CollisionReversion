package tfc.collisionreversion;

import net.minecraftforge.common.ForgeConfigSpec;
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
	
	public final ForgeConfigSpec.BooleanValue useSelectionReversion;
	public final ForgeConfigSpec.BooleanValue useCollisionReversion;
	public final ForgeConfigSpec.BooleanValue globalLegacy;
	
	public Config(ForgeConfigSpec.Builder builder) {
		builder.comment("Selection Settings").push("selection");
		
		useSelectionReversion = builder
				.comment("If the mod show allow selection reversion")
				.translation("config.collisionreversion.selection")
				.define("SelectionReversion", true);
		
		builder.pop();
		
		builder.comment("Collision Settings").push("collision");
		
		useCollisionReversion = builder
				.comment("If the mod show allow collision reversion")
				.translation("config.collisionreversion.collision")
				.define("CollisionReversion", true);
		
		globalLegacy = builder
				.comment("Causes collision reversion to use 1.12 collision for vanilla blocks along side 1.16 collision\nSlightly slower, but a lot more precise")
				.translation("config.collisionreversion.global_legacy")
				.define("GlobalLegacy", true);
		
		builder.pop();
	}
}

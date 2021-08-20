package tfc.collisionreversion;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfc.collisionreversion.api.collision.CollisionLookup;

import java.util.ArrayList;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("collision_reversion")
public class CollisionReversion {
	
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	public CollisionReversion() {
		if (!FMLEnvironment.production) {
			CollisionLookup.registerBoxFiller(
					(context)-> {
						BlockPos pos = context.getPos();
						World world = context.getWorld();
						ArrayList<AxisAlignedBB> boxes = context.getBoxes();
						Entity entity = context.getEntity();
//						if (world.getBlockState(pos).getBlock() instanceof SugarCaneBlock) {
////							for (int i = 0; i < 100; i++) {
////								boxes.add(
////										new AxisAlignedBB(pos.getX(), entity.getPosY() - 0.01, pos.getZ(), pos.getX() + 1, entity.getPosY() - 0.01, pos.getZ() + 1)
////								);
////							}
//							boxes.add(
//									new AxisAlignedBB(pos.getX(), entity.getPosY() + entity.getHeight() + 0.01, pos.getZ(), pos.getX() + 1, entity.getPosY() + entity.getHeight() + 0.01, pos.getZ() + 1)
//							);
//						}
						if (world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
							if (!entity.isCrouching()) {
//								if (pos.getY() + 1 <= entity.getPosY()) {
//									boxes.add(
////											new AxisAlignedBB(pos.getX(), pos.getY() + 0.5, pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
//											new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
//									);
//								}
								if (pos.getY() == (int) entity.getPosY()) {
									boxes.add(
											new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 0.5, pos.getZ() + 1)
									);
								}
							}
						}
					}
			);
		}
	}
}

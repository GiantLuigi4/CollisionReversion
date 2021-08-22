package tfc.collisionreversion;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfc.collisionreversion.api.collision.CollisionLookup;
import tfc.collisionreversion.api.selection.SelectionLookup;

import java.util.ArrayList;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("collision_reversion")
public class CollisionReversion {
	
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	public CollisionReversion() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
		
		CollisionLookup.registerBoxFiller((context)->{
			if (Config.COMMON.globalLegacy.get()) {
				BlockState state = context.getBlockState();
				if (state.isAir()) return;
				ISelectionContext iselectioncontext = ISelectionContext.forEntity(context.getEntity());
				VoxelShape shape = state.getCollisionShape(context.getWorld(), context.getPos(), iselectioncontext);
//				if (shape == null) shape = state.getShape(context.getWorld(), context.getPos(), iselectioncontext);
				ArrayList<AxisAlignedBB> boxes = context.getBoxes();
				for (AxisAlignedBB axisAlignedBB : shape.toBoundingBoxList()) boxes.add(axisAlignedBB.offset(context.getPos()));
			}
		});
		
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
			
			SelectionLookup.registerBoxFiller((context)->{
				if (context.getBlockState().getBlock() instanceof HopperBlock) {
					BlockPos pos = context.getPos();
					ArrayList<AxisAlignedBB> boxes = context.getBoxes();
					boxes.add(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1));
				}
			});
		}
	}
}

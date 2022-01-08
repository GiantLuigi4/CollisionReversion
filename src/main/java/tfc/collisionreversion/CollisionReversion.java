package tfc.collisionreversion;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfc.collisionreversion.api.lookup.CollisionLookup;
import tfc.collisionreversion.api.lookup.SelectionLookup;
import tfc.collisionreversion.utils.CustomArrayList;
import tfc.collisionreversion.utils.integration.PehkuiIntegration;

import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("collision_reversion")
public class CollisionReversion {
	
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		if (ModList.get().isLoaded("pehkui")) PehkuiIntegration.setup();
	}
	
	public static void onConfigEvent(ModConfig.ModConfigEvent event) {
		if (event.getConfig().getModId().equals("collision_reversion")) {
			CustomArrayList.growthRate = Config.COMMON.listGrowthRate.get() - 1;
			CustomArrayList.minGrowth = Config.COMMON.minGrowth.get();
		}
	}
	
	public CollisionReversion() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(CollisionReversion::onCommonSetup);
		
		MinecraftForge.EVENT_BUS.addListener(CollisionReversion::onConfigEvent);
		
		CollisionLookup.registerBoxFiller((context)->{
			if (Config.COMMON.globalLegacy.get() || Config.COMMON.cancelVanillaCollision.get()) {
				BlockState state = context.getBlockState();
				if (state == null) return;
				ISelectionContext iselectioncontext = context.getSelectionContext();
				VoxelShape shape = state.getCollisionShape(context.getWorld(), context.getPos(), iselectioncontext);
//				if (shape == null) shape = state.getShape(context.getWorld(), context.getPos(), iselectioncontext);
				if (shape.isEmpty()) return;
				List<AxisAlignedBB> boxes = context.getBoxes();
				for (AxisAlignedBB axisAlignedBB : shape.toBoundingBoxList()) {
					axisAlignedBB = axisAlignedBB.offset(context.getPos());
//					if (!context.boundingBox().intersects(axisAlignedBB)) continue;
					if (!context.checkBoundingBox(axisAlignedBB)) continue;
					if (!Config.COMMON.cancelVanillaCollision.get()) boxes.add(new AdjustmentAABB(axisAlignedBB));
					else boxes.add(axisAlignedBB);
				}
			}
		});
		
		if (!FMLEnvironment.production) {
			CollisionLookup.registerBoxFiller(
					(context)-> {
						List<AxisAlignedBB> boxes = context.getBoxes();
						Entity entity = context.getEntity();
						
//						if (true) return;
						
						if (Config.COMMON.devOptions.trapEntities.get()) {
							if (!entity.isSneaking()) {
								AxisAlignedBB[] boxes1 = new AxisAlignedBB[6];
								double padding = Config.COMMON.devOptions.trapPadding.get();
								boxes1[0] = (new AxisAlignedBB(
										entity.getBoundingBox().minX, entity.getBoundingBox().minY, entity.getBoundingBox().minZ,
										entity.getBoundingBox().maxX, entity.getBoundingBox().minY - padding, entity.getBoundingBox().maxZ
								));
								boxes1[1] = (new AxisAlignedBB(
										entity.getBoundingBox().minX - padding, entity.getBoundingBox().minY, entity.getBoundingBox().minZ,
										entity.getBoundingBox().minX, entity.getBoundingBox().maxY, entity.getBoundingBox().maxZ
								));
								boxes1[2] = (new AxisAlignedBB(
										entity.getBoundingBox().maxX, entity.getBoundingBox().minY, entity.getBoundingBox().minZ,
										entity.getBoundingBox().maxX + padding, entity.getBoundingBox().maxY, entity.getBoundingBox().maxZ
								));
								boxes1[3] = (new AxisAlignedBB(
										entity.getBoundingBox().minX, entity.getBoundingBox().minY, entity.getBoundingBox().minZ - padding,
										entity.getBoundingBox().maxX, entity.getBoundingBox().maxY, entity.getBoundingBox().minZ
								));
								boxes1[4] = (new AxisAlignedBB(
										entity.getBoundingBox().minX, entity.getBoundingBox().minY, entity.getBoundingBox().maxZ,
										entity.getBoundingBox().maxX, entity.getBoundingBox().maxY, entity.getBoundingBox().maxZ + padding
								));
								boxes1[5] = (new AxisAlignedBB(
										entity.getBoundingBox().minX, entity.getBoundingBox().maxY, entity.getBoundingBox().minZ,
										entity.getBoundingBox().maxX, entity.getBoundingBox().maxY + padding, entity.getBoundingBox().maxZ
								));
								for (AxisAlignedBB axisAlignedBB : boxes1) {
									if (context.checkBoundingBox(axisAlignedBB)) {
										boxes.add(axisAlignedBB);
									}
								}
							}
						}
						
						if (Config.COMMON.devOptions.collisionNonsense.get()) {
							VoxelShape shape = context.getBlockState().getCollisionShape(context.getWorld(), context.getPos(), context.getSelectionContext());
							for (AxisAlignedBB axisAlignedBB : shape.toBoundingBoxList()) {
								int scl = Config.COMMON.devOptions.collisionNonsenseSize.get();
								for (int x = 0; x < scl; x++) {
									for (int y = 0; y < scl; y++) {
										for (int z = 0; z < scl; z++) {
											if (axisAlignedBB.contains(new Vector3d(x / (double) scl, y / (double) scl, z / (double) scl))) {
												AxisAlignedBB aabb1 = new AxisAlignedBB(0, 0, 0, 1 / (double) scl, 1 / (double) scl, 1 / (double) scl).offset(context.getPos()).offset(new Vector3d(x / (double) scl, y / (double) scl, z / (double) scl));
												if (!context.checkBoundingBox(aabb1)) continue;
												boxes.add(aabb1);
											}
										}
									}
								}
							}
						}
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
//						if (world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
//							if (!entity.isCrouching() || true) {
////								if (pos.getY() + 1 <= entity.getPosY()) {
////									boxes.add(
//////											new AxisAlignedBB(pos.getX(), pos.getY() + 0.5, pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
////											new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
////									);
////								}
//								if (pos.getY() == (int) entity.getPosY()) {
//									boxes.add(
//											new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 0.5, pos.getZ() + 1)
//									);
//								}
//							}
//						}
					}
			);
			
			SelectionLookup.registerBoxFiller((context)->{
				if (Config.COMMON.devOptions.fullSelectionShapes.get()) {
					if (!context.getBlockState().isAir()) {
						BlockPos pos = context.getPos();
						List<AxisAlignedBB> boxes = context.getBoxes();
						boxes.add(new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(pos));
					}
				}
			});
		}
	}
}

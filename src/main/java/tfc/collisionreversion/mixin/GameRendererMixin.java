package tfc.collisionreversion.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private Minecraft mc;
	
	@Inject(at = @At("TAIL"), method = "getMouseOver")
	public void LegacyCollision_postGetObjectMouseOver(float partialTicks, CallbackInfo ci) {
//		if (!CollisionReversionAPI.useSelection()) return;
//		Entity entity = mc.player;
//		if (entity == null) return;
//		if (mc.playerController == null) return;
//		Vector3d eyeVec = entity.getEyePosition(partialTicks);
//		double bestDist = Double.MAX_VALUE;
//		if (mc.objectMouseOver != null) bestDist = mc.objectMouseOver.getHitVec().distanceTo(eyeVec);
//		double reach = mc.playerController.getBlockReachDistance();
//		Vector3d reachVec = entity.getLook(1.0F);
//		Vector3d endVec = eyeVec.add(reachVec.scale(reach));
//		RayTraceResult bestResult = mc.objectMouseOver;
//		{
//			AxisAlignedBB region = new AxisAlignedBB(eyeVec.x, eyeVec.y, eyeVec.z, endVec.x, endVec.y, endVec.z);
//			int x1 = MathHelper.floor(region.minX) - 1;
//			int x2 = MathHelper.ceil(region.maxX) + 1;
//			int y1 = MathHelper.floor(region.minY) - 1;
//			int y2 = MathHelper.ceil(region.maxY) + 1;
//			int z1 = MathHelper.floor(region.minZ) - 1;
//			int z2 = MathHelper.ceil(region.maxZ) + 1;
//			List<AxisAlignedBB> boundingBoxes = makeSortedList();
//			LegacyContext context = new LegacyContext();
//			for (int x = x1; x < x2; x++) {
//				for (int y = y1; y < y2; y++) {
//					for (int z = z1; z < z2; z++) {
//						BlockPos pos = new BlockPos(x, y, z);
//						SelectionLookup.getBoundingBoxes(mc.world, pos, entity, boundingBoxes, context);
//						boolean invertFace = false;
//						for (int i = 0; i < boundingBoxes.size(); i++) {
//							AxisAlignedBB bb = boundingBoxes.get(i);
//							if (bb.contains(eyeVec)) invertFace = true;
//							boundingBoxes.set(i, bb.offset(-x, -y, -z));
//						}
//						BlockRayTraceResult result = AxisAlignedBB.rayTrace(boundingBoxes, eyeVec, endVec, pos);
//						boundingBoxes.clear();
//						if (invertFace) {
//							Vector3d vector3d = endVec.subtract(eyeVec);
//							Vector3d vector3d1 = eyeVec.add(vector3d.scale(0.001D));
//							result = new BlockRayTraceResult(vector3d1, Direction.getFacingFromVector(vector3d.x, vector3d.y, vector3d.z).getOpposite(), pos, true);
//						}
//						if (result == null) continue;
//						double dist1 = result.getHitVec().distanceTo(eyeVec);
//						if (dist1 < bestDist) {
//							bestDist = dist1;
//							bestResult = result;
//							if (invertFace) break;
//						}
//					}
//				}
//			}
//		}
//		mc.objectMouseOver = bestResult;
	}
}

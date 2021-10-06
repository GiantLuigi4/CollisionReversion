import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import tfc.collisionreversion.coremod.IBlockReaderCoremod;

import javax.annotation.Nullable;

public class Transformers implements IBlockReader {
	public static MethodNode transformRaytrace(MethodNode node) {
		System.out.println("Transforming method " + node.name);
		int arrayLength = node.instructions.size();
		AbstractInsnNode target_instruction = null;
		int maxStore = 0;
		for (int i = 0; i < arrayLength; i++) {
			AbstractInsnNode insn = node.instructions.get(i);
			int value = insn.getOpcode();
			if (value == Opcodes.ARETURN) {
				System.out.println("RETURN");
			} else {
				if (insn instanceof TypeInsnNode) System.out.println(((TypeInsnNode)insn).desc);
				else if (insn instanceof VarInsnNode) System.out.println(((VarInsnNode) insn).var);
				else if (insn instanceof InvokeDynamicInsnNode) System.out.println("IDynamic " + ((InvokeDynamicInsnNode) insn).name + ((InvokeDynamicInsnNode) insn).desc);
				else if (insn instanceof LabelNode) System.out.println("Label: " + ((LabelNode) insn).getLabel().toString());
				else if (insn instanceof LineNumberNode) System.out.println("Line: " + ((LineNumberNode) insn).line);
				else if (insn instanceof MethodInsnNode) System.out.println("Method: " + ((MethodInsnNode) insn).name);
				else System.out.println(insn.getClass());
			}
			if (insn instanceof VarInsnNode)
				if (insn.getOpcode() == Opcodes.ASTORE)
					if (maxStore < ((VarInsnNode) insn).var) maxStore = ((VarInsnNode) insn).var + 1;
			if (insn instanceof MethodInsnNode) {
				if (((MethodInsnNode) insn).name.equals("doRayTrace")) {
					target_instruction = insn;
//					break;
				}
			}
		}
		InsnList list = new InsnList();
		list.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/util/math/BlockRayTraceResult"));
		list.add(new VarInsnNode(Opcodes.ASTORE, maxStore));
		list.add(new VarInsnNode(Opcodes.ALOAD, 1));
		list.add(new VarInsnNode(Opcodes.ALOAD, maxStore));
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tfc/collisionreversion/coremod/IBlockReaderCoremod", "raytraceLegacy", "(Lnet/minecraft/util/math/RayTraceContext;Lnet/minecraft/util/math/BlockRayTraceResult;)Lnet/minecraft/util/math/BlockRayTraceResult;"));
		list.add(new VarInsnNode(Opcodes.ASTORE, maxStore));
		list.add(new VarInsnNode(Opcodes.ALOAD, maxStore));
		node.instructions.insert(target_instruction, list);
		return node;
	}
	
	public BlockRayTraceResult test(RayTraceContext context) {
		BlockRayTraceResult result = IBlockReader.doRayTrace(context, (p_217297_1_, p_217297_2_) -> {
			BlockState blockstate = this.getBlockState(p_217297_2_);
			FluidState fluidstate = this.getFluidState(p_217297_2_);
			Vector3d vector3d = p_217297_1_.getStartVec();
			Vector3d vector3d1 = p_217297_1_.getEndVec();
			VoxelShape voxelshape = p_217297_1_.getBlockShape(blockstate, this, p_217297_2_);
			BlockRayTraceResult blockraytraceresult = this.rayTraceBlocks(vector3d, vector3d1, p_217297_2_, voxelshape, blockstate);
			VoxelShape voxelshape1 = p_217297_1_.getFluidShape(fluidstate, this, p_217297_2_);
			BlockRayTraceResult blockraytraceresult1 = voxelshape1.rayTrace(vector3d, vector3d1, p_217297_2_);
			double d0 = blockraytraceresult == null ? Double.MAX_VALUE : p_217297_1_.getStartVec().squareDistanceTo(blockraytraceresult.getHitVec());
			double d1 = blockraytraceresult1 == null ? Double.MAX_VALUE : p_217297_1_.getStartVec().squareDistanceTo(blockraytraceresult1.getHitVec());
			return d0 <= d1 ? blockraytraceresult : blockraytraceresult1;
		}, (p_217302_0_) -> {
			Vector3d vector3d = p_217302_0_.getStartVec().subtract(p_217302_0_.getEndVec());
			return BlockRayTraceResult.createMiss(p_217302_0_.getEndVec(), Direction.getFacingFromVector(vector3d.x, vector3d.y, vector3d.z), new BlockPos(p_217302_0_.getEndVec()));
		});
		result = IBlockReaderCoremod.raytraceLegacy(result, context);
		return result;
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return null;
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		return null;
	}
	
	@Override
	public FluidState getFluidState(BlockPos pos) {
		return null;
	}
}

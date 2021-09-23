function initializeCoreMod() {
    var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
    var TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
    var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
    var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
    var LineNumberNode = Java.type("org.objectweb.asm.tree.LineNumberNode");
    var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
    var InvokeDynamicInsnNode = Java.type("org.objectweb.asm.tree.InvokeDynamicInsnNode");
    var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
//    var FMLEnvironment = Java.type('net.minecraftforge.fml.loading.FMLEnvironment');
    var mappedMethodName = ASMAPI.mapMethod("func_217299_a");
    var mappedTargetName = ASMAPI.mapMethod("func_217300_a");

    var TraceMethodVisitor = Java.type('org.objectweb.asm.util.TraceMethodVisitor');
    var Textifier = Java.type('org.objectweb.asm.util.Textifier');

	// afaik, this should not wind up being incompatible with stuff
	// what I'm doing, is I'm storing the
	return {
		'coremodmethod': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.world.IBlockReader',
				'methodName': mappedMethodName,
				'methodDesc': '(Lnet/minecraft/util/math/RayTraceContext;)Lnet/minecraft/util/math/BlockRayTraceResult;'
			},
            'transformer': function(node) {
//				if (!FMLEnvironment.dist.isProduction)
//					print("Transforming method " + node.name);
				var arrayLength = node.instructions.size();
				var targetInstruction = null;
//				var maxStore = 0;
				for (var i = 0; i < arrayLength; i++) {
					var insn = node.instructions.get(i);
//					if (insn instanceof VarInsnNode) {
//						if (insn.getOpcode() == Opcodes.ASTORE) {
//							if (maxStore < insn.var) {
//								maxStore = insn.var + 1;
//							}
//						}
//					}
					if (insn instanceof MethodInsnNode) {
						if (insn.name.equals(mappedTargetName)) {
							targetInstruction = insn;
						}
					}
				}

				var visitor = new TraceMethodVisitor(new Textifier());
				for(var iter = node.instructions.iterator(); iter.hasNext();){
					iter.next().accept(visitor);
				}
				print(visitor.p.getText());

				var list = new InsnList();
				list.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/util/math/BlockRayTraceResult"));
//				list.add(new VarInsnNode(Opcodes.ASTORE, maxStore));
				list.add(new VarInsnNode(Opcodes.ALOAD, 1));
//				list.add(new VarInsnNode(Opcodes.ALOAD, maxStore));
				list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tfc/collisionreversion/coremod/IBlockReaderCoremod", "raytraceLegacy", "(Lnet/minecraft/util/math/BlockRayTraceResult;Lnet/minecraft/util/math/RayTraceContext;)Lnet/minecraft/util/math/BlockRayTraceResult;"));
//				list.add(new VarInsnNode(Opcodes.ASTORE, maxStore));
//				list.add(new VarInsnNode(Opcodes.ALOAD, maxStore));
				node.instructions.insert(targetInstruction, list);

				return node;
            }
		}
	}
}
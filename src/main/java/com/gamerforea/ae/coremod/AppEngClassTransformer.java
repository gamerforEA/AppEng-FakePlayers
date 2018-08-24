package com.gamerforea.ae.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

public final class AppEngClassTransformer implements IClassTransformer
{
	private static final Logger LOGGER = LogManager.getLogger("AppEng");

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (transformedName.equals("codechicken.multipart.BlockMultipart"))
			return transformBlockMultipart(basicClass);
		return basicClass;
	}

	private static byte[] transformBlockMultipart(byte[] basicClass)
	{
		ClassNode classNode = new ClassNode();
		new ClassReader(basicClass).accept(classNode, 0);

		String name = "dropAndDestroy";
		String desc = "(Lnet/minecraft/world/World;III)V";
		String owner = "codechicken/multipart/BlockMultipart";
		for (MethodNode methodNode : classNode.methods)
		{
			for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); )
			{
				AbstractInsnNode insnNode = iterator.next();
				if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL && insnNode instanceof MethodInsnNode)
				{
					MethodInsnNode methodInsn = (MethodInsnNode) insnNode;
					if (methodInsn.name.equals(name) && methodInsn.desc.equals(desc) && methodInsn.owner.equals(owner))
					{
						iterator.set(new MethodInsnNode(Opcodes.INVOKESTATIC, MethodHooks.OWNER, MethodHooks.NAME, MethodHooks.DESC, false));
						LOGGER.info("Method call {}.{}{} in {}.{}{} replaced to {}.{}{}", owner, name, desc, classNode.name, methodNode.name, methodNode.desc, MethodHooks.OWNER, MethodHooks.NAME, MethodHooks.DESC);
					}
				}
			}
		}

		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(classWriter);
		byte[] bytes = classWriter.toByteArray();
		LOGGER.info("Class {} transformed", classNode.name);
		return bytes;
	}
}

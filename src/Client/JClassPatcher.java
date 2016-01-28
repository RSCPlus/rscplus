/**
 *	rscplus
 *
 *	This file is part of rscplus.
 *
 *	rscplus is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	rscplus is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with rscplus.  If not, see <http://www.gnu.org/licenses/>.
 *
 *	Authors: see <https://github.com/OrN/rscplus>
 */

package Client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public class JClassPatcher
{
	public static byte[] patch(byte data[])
	{
		ClassReader reader = new ClassReader(data);
		ClassNode node = new ClassNode();
		reader.accept(node, ClassReader.SKIP_DEBUG);

		if(node.name.equals("ua"))
			patchRenderer(node);
		else if(node.name.equals("lb"))
			patchCamera(node);
		else if(node.name.equals("e"))
			patchApplet(node);
		else if(node.name.equals("client"))
			patchClient(node);

		patchGeneric(node);

		if(Settings.DEBUG)
			dumpClass(node);

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		return writer.toByteArray();
	}

	private static void patchGeneric(ClassNode node)
	{
		Iterator<MethodNode> methodNodeList = node.methods.iterator();
		while(methodNodeList.hasNext())
		{
			MethodNode methodNode = methodNodeList.next();

			hookClassVariable(methodNode, "e", "m", "I", "Game/Renderer", "width", "I", false, true);
			hookClassVariable(methodNode, "e", "a", "I", "Game/Renderer", "height", "I", false, true);

			hookClassVariable(methodNode, "client", "Wd", "I", "Game/Renderer", "width", "I", false, true);
			hookClassVariable(methodNode, "client", "Oi", "I", "Game/Renderer", "height_client", "I", false, true);

			//hookClassVariable(methodNode, "client", "Tb", "[Lta;", "Game/Client", "players", "[Ljava/lang/Object;", true, false);
		}
	}

	private static void patchApplet(ClassNode node)
	{
		Logger.Info("Patching applet (" + node.name + ".class)");

		Iterator<MethodNode> methodNodeList = node.methods.iterator();
		while(methodNodeList.hasNext())
		{
			MethodNode methodNode = methodNodeList.next();

			if(methodNode.name.equals("run") && methodNode.desc.equals("()V"))
			{
				// Mouse and keyboard listener hooks
				AbstractInsnNode findNode = methodNode.instructions.getFirst();
				for(;;)
				{
					AbstractInsnNode next = findNode.getNext();

					if(next == null)
						break;

					if(findNode.getOpcode() == Opcodes.ALOAD && next.getOpcode() == Opcodes.ALOAD)
					{
						AbstractInsnNode invokeNode = next.getNext();
						MethodInsnNode invoke = (MethodInsnNode)invokeNode;
						methodNode.instructions.remove(next);
						methodNode.instructions.remove(invokeNode);
						if(invoke.name.equals("addMouseListener"))
						{
							FieldInsnNode put = new FieldInsnNode(Opcodes.PUTSTATIC, "Game/MouseHandler", "listener_mouse", "Ljava/awt/event/MouseListener;");
							methodNode.instructions.insert(findNode, put);
						}
						else if(invoke.name.equals("addMouseMotionListener"))
						{
							FieldInsnNode put = new FieldInsnNode(Opcodes.PUTSTATIC, "Game/MouseHandler", "listener_mouse_motion", "Ljava/awt/event/MouseMotionListener;");
							methodNode.instructions.insert(findNode, put);
						}
						else if(invoke.name.equals("addKeyListener"))
						{
							FieldInsnNode put = new FieldInsnNode(Opcodes.PUTSTATIC, "Game/KeyboardHandler", "listener_key", "Ljava/awt/event/KeyListener;");
							methodNode.instructions.insert(findNode, put);
						}
					}
					findNode = findNode.getNext();
				}
			}

		}
	}

	private static void patchClient(ClassNode node)
	{
		Logger.Info("Patching client (" + node.name + ".class)");

		Iterator<MethodNode> methodNodeList = node.methods.iterator();
		while(methodNodeList.hasNext())
		{
			MethodNode methodNode = methodNodeList.next();

			// I (I)V is where most of the interface is processed
			if(methodNode.name.equals("I") && methodNode.desc.equals("(I)V"))
			{
				// Show combat menu
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while(insnNodeList.hasNext())
				{
					AbstractInsnNode insnNode = insnNodeList.next();

					if(insnNode.getOpcode() == Opcodes.BIPUSH)
					{
						IntInsnNode bipush = (IntInsnNode)insnNode;

						if(bipush.operand == -9)
						{
							AbstractInsnNode findNode = insnNode;
							while(findNode.getOpcode() != Opcodes.IF_ICMPEQ)
								findNode = findNode.getNext();

							LabelNode label = ((JumpInsnNode)findNode).label;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Client/Settings", "COMBAT_MENU", "Z"));
							methodNode.instructions.insertBefore(insnNode, new JumpInsnNode(Opcodes.IFGT, label));
							break;
						}
					}
				}
			}
			else if(methodNode.name.equals("O") && methodNode.desc.equals("(I)V"))
			{
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while(insnNodeList.hasNext())
				{
					AbstractInsnNode insnNode = insnNodeList.next();

					// Chatbox fix
					if(insnNode.getOpcode() == Opcodes.SIPUSH)
					{
						IntInsnNode call = (IntInsnNode)insnNode;
						if(call.operand == 324)
						{
							call.operand = 334 - call.operand;
							FieldInsnNode widthField = new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height_client", "I");
							InsnNode add = new InsnNode(Opcodes.ISUB);
							methodNode.instructions.insertBefore(insnNode, widthField);
							methodNode.instructions.insert(insnNode, add);
						}
						else if(call.operand == 269)
						{
							call.operand = 334 - call.operand;
							FieldInsnNode widthField = new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height_client", "I");
							InsnNode add = new InsnNode(Opcodes.ISUB);
							methodNode.instructions.insertBefore(insnNode, widthField);
							methodNode.instructions.insert(insnNode, add);
						}
						/*else if(call.operand == 502)
						{
							call.operand = 512 - call.operand;
							FieldInsnNode widthField = new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I");
							InsnNode add = new InsnNode(Opcodes.ISUB);
							methodNode.instructions.insertBefore(insnNode, widthField);
							methodNode.instructions.insert(insnNode, add);
						}*/
					}
				}
			}
			else if(methodNode.name.equals("L") && methodNode.desc.equals("(I)V"))
			{
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while(insnNodeList.hasNext())
				{
					AbstractInsnNode insnNode = insnNodeList.next();

					// Right click bounds fix
					if(insnNode.getOpcode() == Opcodes.SIPUSH)
					{
						IntInsnNode call = (IntInsnNode)insnNode;
						AbstractInsnNode nextNode = insnNode.getNext();

						if(call.operand == 510)
						{
							call.operand = 512 - call.operand;
							FieldInsnNode widthField = new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I");
							InsnNode add = new InsnNode(Opcodes.ISUB);
							methodNode.instructions.insertBefore(insnNode, widthField);
							methodNode.instructions.insert(insnNode, add);
						}
						else if(call.operand == 315)
						{
							call.operand = 334 - call.operand;
							FieldInsnNode heightField = new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height_client", "I");
							InsnNode add = new InsnNode(Opcodes.ISUB);
							methodNode.instructions.insertBefore(insnNode, heightField);
							methodNode.instructions.insert(insnNode, add);
						}
						else if(call.operand == -316)
						{
							call.operand = 334 - (call.operand * -1);
							FieldInsnNode heightField = new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height_client", "I");
							InsnNode add = new InsnNode(Opcodes.ISUB);
							InsnNode neg = new InsnNode(Opcodes.INEG);
							methodNode.instructions.insertBefore(insnNode, heightField);
							methodNode.instructions.insert(insnNode, neg);
							methodNode.instructions.insert(insnNode, add);
						}
					}
				}
			}
			else if(methodNode.name.equals("a") && methodNode.desc.equals("(ZZ)V"))
			{
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while(insnNodeList.hasNext())
				{
					AbstractInsnNode insnNode = insnNodeList.next();

					// Friends chat mouse fix
					if(insnNode.getOpcode() == Opcodes.SIPUSH)
					{
						IntInsnNode call = (IntInsnNode)insnNode;
						if(call.operand == 489 || call.operand == 429)
						{
							call.operand = 512 - call.operand;
							FieldInsnNode widthField = new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I");
							InsnNode add = new InsnNode(Opcodes.ISUB);
							methodNode.instructions.insertBefore(insnNode, widthField);
							methodNode.instructions.insert(insnNode, add);
						}
						if(call.operand == -430)
						{
							call.operand = 512 - (call.operand * -1);
							FieldInsnNode widthField = new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I");
							InsnNode add = new InsnNode(Opcodes.ISUB);
							InsnNode neg = new InsnNode(Opcodes.INEG);
							methodNode.instructions.insertBefore(insnNode, widthField);
							methodNode.instructions.insert(insnNode, neg);
							methodNode.instructions.insert(insnNode, add);
						}
					}
				}
			}
			else if(methodNode.name.equals("i") && methodNode.desc.equals("(I)V"))
			{
				AbstractInsnNode lastNode = methodNode.instructions.getLast().getPrevious();

				// Send combat style option
				LabelNode label = new LabelNode();

				methodNode.instructions.insert(lastNode, label);

				// Format
				methodNode.instructions.insert(lastNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "da", "b", "(I)V", false));
				methodNode.instructions.insert(lastNode, new IntInsnNode(Opcodes.SIPUSH, 21294));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
				methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

				// Write byte
				methodNode.instructions.insert(lastNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "ja", "c", "(II)V", false));
				methodNode.instructions.insert(lastNode, new IntInsnNode(Opcodes.BIPUSH, -80));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETSTATIC, "Client/Settings", "COMBAT_STYLE", "I"));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETFIELD, "da", "f", "Lja;"));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
				methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

				// Create Packet
				methodNode.instructions.insert(lastNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "da", "b", "(II)V", false));
				methodNode.instructions.insert(lastNode, new InsnNode(Opcodes.ICONST_0));
				methodNode.instructions.insert(lastNode, new IntInsnNode(Opcodes.BIPUSH, 29));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
				methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

				// Skip combat packet if style is already controlled
				methodNode.instructions.insert(lastNode, new JumpInsnNode(Opcodes.IF_ICMPLE, label));
				methodNode.instructions.insert(lastNode, new InsnNode(Opcodes.ICONST_0));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETSTATIC, "Client/Settings", "COMBAT_STYLE", "I"));

				// Client init_game
				methodNode.instructions.insert(lastNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "init_game", "()V", false));
			}
			else if(methodNode.name.equals("o") && methodNode.desc.equals("(I)V"))
			{
				// Client.init_login patch
				AbstractInsnNode findNode = methodNode.instructions.getFirst();
				MethodInsnNode call = new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "init_login", "()V", false);
				methodNode.instructions.insertBefore(findNode, call);
			}
			else if(methodNode.name.equals("a") && methodNode.desc.equals("(B)V"))
			{
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while(insnNodeList.hasNext())
				{
					AbstractInsnNode insnNode = insnNodeList.next();

					// Camera view distance crash fix
					if(insnNode.getOpcode() == Opcodes.SIPUSH)
					{
						IntInsnNode call = (IntInsnNode)insnNode;
						if(call.operand == 15000)
						{
							call.operand = 32767;
						}
					}
				}

				// Client.init patch
				AbstractInsnNode findNode = methodNode.instructions.getFirst();
				methodNode.instructions.insertBefore(findNode, new VarInsnNode(Opcodes.ALOAD, 0));
				methodNode.instructions.insertBefore(findNode, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "instance", "Ljava/lang/Object;"));
				methodNode.instructions.insertBefore(findNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "init", "()V", false));
			}
			else if(methodNode.name.equals("G") && methodNode.desc.equals("(I)V"))
			{
				// TODO: This can be shortened, I'll fix it another time

				// NPC Dialogue keyboard
				AbstractInsnNode lastNode = methodNode.instructions.getLast().getPrevious();

				IntInsnNode packetOpcode = new IntInsnNode(Opcodes.BIPUSH, 116);
				IntInsnNode unknown1 = new IntInsnNode(Opcodes.BIPUSH, 115);
				IntInsnNode unknown2 = new IntInsnNode(Opcodes.SIPUSH, 21294);
				MethodInsnNode createPacket = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "da", "b", "(II)V", false);
				FieldInsnNode instance2 = new FieldInsnNode(Opcodes.GETFIELD, "da", "f", "Lja;");
				MethodInsnNode writeByte = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "ja", "c", "(II)V", false);
				MethodInsnNode formatPacket = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "da", "b", "(I)V", false);
				LabelNode label = new LabelNode();

				methodNode.instructions.insert(lastNode, label);

				// Hide dialogue
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.PUTFIELD, "client", "Ph", "Z"));
				methodNode.instructions.insert(lastNode, new InsnNode(Opcodes.ICONST_0));
				methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

				// Format
				methodNode.instructions.insert(lastNode, formatPacket);
				methodNode.instructions.insert(lastNode, unknown2);
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
				methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

				// Write byte
				methodNode.instructions.insert(lastNode, writeByte);
				methodNode.instructions.insert(lastNode, unknown1);
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/KeyboardHandler", "dialogue_option", "I"));
				methodNode.instructions.insert(lastNode, instance2);
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
				methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

				// Create Packet
				methodNode.instructions.insert(lastNode, createPacket);
				methodNode.instructions.insert(lastNode, new InsnNode(Opcodes.ICONST_0));
				methodNode.instructions.insert(lastNode, packetOpcode);
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
				methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

				// Check if dialogue option is pressed
				methodNode.instructions.insert(lastNode, new JumpInsnNode(Opcodes.IF_ICMPGE, label));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Id", "I")); // Menu option count
				methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/KeyboardHandler", "dialogue_option", "I"));
				methodNode.instructions.insert(lastNode, new JumpInsnNode(Opcodes.IFLT, label));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/KeyboardHandler", "dialogue_option", "I"));
			}
			else if(methodNode.name.equals("f") && methodNode.desc.equals("(I)V"))
			{
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while(insnNodeList.hasNext())
				{
					AbstractInsnNode insnNode = insnNodeList.next();

					// Hide Roof option
					if(insnNode.getOpcode() == Opcodes.GETFIELD)
					{
						FieldInsnNode field = (FieldInsnNode)insnNode;

						if(field.owner.equals("client") && field.name.equals("yj"))
						{
							AbstractInsnNode nextNode = insnNode.getNext();
							if(nextNode.getOpcode() == Opcodes.IFNE)
							{
								LabelNode label = ((JumpInsnNode)nextNode).label;
								methodNode.instructions.insert(nextNode, new JumpInsnNode(Opcodes.IFGT, label));
								methodNode.instructions.insert(nextNode, new FieldInsnNode(Opcodes.GETSTATIC, "Client/Settings", "HIDE_ROOFS", "Z"));
							}
						}
					}

					// Move wilderness skull
					if(insnNode.getOpcode() == Opcodes.SIPUSH)
					{
						IntInsnNode call = (IntInsnNode)insnNode;
						if(call.operand == 465 || call.operand == 453)
						{
							call.operand = 512 - call.operand;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
						}
					}
				}
			}
			else if(methodNode.name.equals("a") && methodNode.desc.equals("(IIZ)Z"))
			{
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while(insnNodeList.hasNext())
				{
					AbstractInsnNode insnNode = insnNodeList.next();

					// Move the load screen text dialogue
					if(insnNode.getOpcode() == Opcodes.SIPUSH)
					{
						IntInsnNode call = (IntInsnNode)insnNode;
						if(call.operand == 256)
						{
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
						else if(call.operand == 192)
						{
							// TODO: Not 100% sure on this offset, it's not 346/2
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IADD));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 19));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
					}
				}
			}
			else if(methodNode.name.equals("d") && methodNode.desc.equals("(B)V"))
			{
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while(insnNodeList.hasNext())
				{
					AbstractInsnNode insnNode = insnNodeList.next();

					// Center logout dialogue
					if(insnNode.getOpcode() == Opcodes.SIPUSH || insnNode.getOpcode() == Opcodes.BIPUSH)
					{
						IntInsnNode call = (IntInsnNode)insnNode;
						if(call.operand == 256)
						{
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
						else if(call.operand == 173)
						{
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
						else if(call.operand == 126)
						{
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 130));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
						else if(call.operand == 137)
						{
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 36));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
					}
				}
			}
			else if(methodNode.name.equals("j") && methodNode.desc.equals("(I)V"))
			{
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while(insnNodeList.hasNext())
				{
					AbstractInsnNode insnNode = insnNodeList.next();

					// Center welcome box
					if(insnNode.getOpcode() == Opcodes.SIPUSH || insnNode.getOpcode() == Opcodes.BIPUSH)
					{
						IntInsnNode call = (IntInsnNode)insnNode;
						if(call.operand == 256)
						{
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
						else if(call.operand == 167)
						{
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 6));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
						else if(call.operand == 56)
						{
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 200));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
						else if(call.operand == -87)
						{
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.INEG));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 169));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
						else if(call.operand == 426)
						{
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IADD));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 170));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
						else if(call.operand == 106)
						{
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 150));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
						else if(call.operand == 406)
						{
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IADD));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 150));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
					}
				}
			}
			else if(methodNode.name.equals("k") && methodNode.desc.equals("(B)V"))
			{
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while(insnNodeList.hasNext())
				{
					AbstractInsnNode insnNode = insnNodeList.next();

					// Save settings from combat menu
					if(insnNode.getOpcode() == Opcodes.PUTFIELD)
					{
						FieldInsnNode field = (FieldInsnNode)insnNode;

						if(field.owner.equals("client") && field.name.equals("Fg"))
						{
							methodNode.instructions.insert(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Client/Settings", "Save", "()V", false));
							methodNode.instructions.insert(insnNode, new FieldInsnNode(Opcodes.PUTSTATIC, "Client/Settings", "COMBAT_STYLE", "I"));
							methodNode.instructions.insert(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Fg", "I"));
							methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
						}
					}
				}
			}
			else if(methodNode.name.equals("a") && methodNode.desc.equals("(IIIIIIII)V"))
			{
				AbstractInsnNode insnNode = methodNode.instructions.getFirst();

				
			}

			hookClassVariable(methodNode, "client", "oh", "[I", "Game/Client", "current_level", "[I", true, false);
			hookClassVariable(methodNode, "client", "cg", "[I", "Game/Client", "base_level", "[I", true, false);
			hookClassVariable(methodNode, "client", "Vk", "[Ljava/lang/String;", "Game/Client", "skill_name", "[Ljava/lang/String;", true, false);
			hookClassVariable(methodNode, "client", "Ak", "[I", "Game/Client", "xp", "[I", true, false);
			hookClassVariable(methodNode, "client", "vg", "I", "Game/Client", "fatigue", "I", true, false);
			hookClassVariable(methodNode, "client", "Fg", "I", "Game/Client", "combat_style", "I", true, true);

			hookClassVariable(methodNode, "client", "Ph", "Z", "Game/Client", "show_questionmenu", "Z", true, false);

			hookClassVariable(methodNode, "client", "lc", "I", "Game/Client", "inventory_count", "I", true, false);

			hookClassVariable(methodNode, "client", "ug", "I", "Game/Camera", "rotation", "I", true, true);
			hookClassVariable(methodNode, "client", "ac", "I", "Game/Camera", "zoom", "I", false, true);

			hookStaticVariable(methodNode, "client", "il", "[Ljava/lang/String;", "Game/Client", "strings", "[Ljava/lang/String;", true, true);
		}
	}

	private static void patchCamera(ClassNode node)
	{
		Logger.Info("Patching camera (" + node.name + ".class)");

		Iterator<MethodNode> methodNodeList = node.methods.iterator();
		while(methodNodeList.hasNext())
		{
			MethodNode methodNode = methodNodeList.next();

			hookClassVariable(methodNode, "lb", "Mb", "I", "Game/Camera", "distance1", "I", false, true);
			hookClassVariable(methodNode, "lb", "X", "I", "Game/Camera", "distance2", "I", false, true);
			hookClassVariable(methodNode, "lb", "P", "I", "Game/Camera", "distance3", "I", false, true);
			hookClassVariable(methodNode, "lb", "G", "I", "Game/Camera", "distance4", "I", false, true);
		}
	}

	private static void patchRenderer(ClassNode node)
	{
		Logger.Info("Patching renderer (" + node.name + ".class)");

		Iterator<MethodNode> methodNodeList = node.methods.iterator();
		while(methodNodeList.hasNext())
		{
			MethodNode methodNode = methodNodeList.next();

			// Renderer present hook
			if(methodNode.desc.equals("(Ljava/awt/Graphics;III)V"))
			{
				AbstractInsnNode findNode = methodNode.instructions.getFirst();
				FieldInsnNode imageNode = null;

				while(findNode.getOpcode() != Opcodes.POP)
				{
					findNode = findNode.getNext();
					if(findNode == null)
					{
						Logger.Error("Unable to find present hook");
						break;
					}
				}

				while(findNode.getOpcode() != Opcodes.INVOKESPECIAL)
				{
					if(findNode.getOpcode() == Opcodes.GETFIELD)
						imageNode = (FieldInsnNode)findNode;

					AbstractInsnNode prev = findNode.getPrevious();
					methodNode.instructions.remove(findNode);
					findNode = prev;
				}

				VarInsnNode aload0 = new VarInsnNode(Opcodes.ALOAD, 0);
				VarInsnNode aload1 = new VarInsnNode(Opcodes.ALOAD, 1);
				MethodInsnNode call = new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Renderer", "present", "(Ljava/awt/Graphics;Ljava/awt/Image;)V", false);
				FieldInsnNode newImageNode = new FieldInsnNode(Opcodes.GETFIELD, node.name, imageNode.name, imageNode.desc);

				methodNode.instructions.insert(findNode, call);
				methodNode.instructions.insert(findNode, newImageNode);
				methodNode.instructions.insert(findNode, aload0);
				methodNode.instructions.insert(findNode, aload1);
			}
			else if(methodNode.name.equals("a") && methodNode.desc.equals("(IILjava/lang/String;IIBI)V"))
			{
				AbstractInsnNode start = null;

				// ~###~ string patch to increase it to ~####~
				// Fixes higher resolutions for the friends list
				// FIXME: This will break any use of ~###~ by players, although it doesn't really matter
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while(insnNodeList.hasNext())
				{
					AbstractInsnNode insnNode = insnNodeList.next();

					if(insnNode.getOpcode() == Opcodes.ICONST_4)
					{
						AbstractInsnNode nextNode = insnNode.getPrevious();
						if(nextNode.getOpcode() == Opcodes.IXOR)
						{
							start = insnNode;
						}
					}
				}

				AbstractInsnNode begin = start;
				while(begin != null)
				{
					if(begin.getOpcode() == Opcodes.ICONST_4)
					{
						AbstractInsnNode remove = begin;
						InsnNode newOpcode = new InsnNode(Opcodes.ICONST_5);
						methodNode.instructions.insertBefore(begin, newOpcode);
						begin = begin.getNext();
						methodNode.instructions.remove(remove);
					}
					else if(begin.getOpcode() == Opcodes.BIPUSH)
					{
						IntInsnNode push = (IntInsnNode)begin;
						if(push.operand == -4)
							push.operand = -5;
						begin = begin.getNext();
					}
					else if(begin.getOpcode() == Opcodes.IINC)
					{
						IincInsnNode iinc = (IincInsnNode)begin;
						if(iinc.incr == 4)
							iinc.incr = 5;
						begin = begin.getNext();
					}
					else
					{
						begin = begin.getNext();
					}
				}
			}
		}
	}

	private static void hookClassVariable(MethodNode methodNode, String owner, String var, String desc,
					      String newClass, String newVar, String newDesc, boolean canRead, boolean canWrite)
	{
		Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
		while(insnNodeList.hasNext())
		{
			AbstractInsnNode insnNode = insnNodeList.next();

			int opcode = insnNode.getOpcode();
			if(opcode == Opcodes.GETFIELD || opcode == Opcodes.PUTFIELD)
			{
				FieldInsnNode field = (FieldInsnNode)insnNode;
				if(field.owner.equals(owner) && field.name.equals(var) && field.desc.equals(desc))
				{
					if(opcode == Opcodes.GETFIELD && canWrite)
					{
						FieldInsnNode newField = new FieldInsnNode(Opcodes.GETSTATIC, newClass, newVar, newDesc);
						InsnNode pop = new InsnNode(Opcodes.POP);
						methodNode.instructions.insert(insnNode, newField);
						methodNode.instructions.insert(insnNode, pop);
					}
					else if(opcode == Opcodes.PUTFIELD && canRead)
					{
						InsnNode dup = new InsnNode(Opcodes.DUP_X1);
						FieldInsnNode newPut = new FieldInsnNode(Opcodes.PUTSTATIC, newClass, newVar, newDesc);
						methodNode.instructions.insertBefore(insnNode, dup);
						methodNode.instructions.insert(insnNode, newPut);
					}
				}
			}
		}
	}

	private static void hookStaticVariable(MethodNode methodNode, String owner, String var, String desc,
					       String newClass, String newVar, String newDesc, boolean canRead, boolean canWrite)
	{
		Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
		while(insnNodeList.hasNext())
		{
			AbstractInsnNode insnNode = insnNodeList.next();

			int opcode = insnNode.getOpcode();
			if(opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC)
			{
				FieldInsnNode field = (FieldInsnNode)insnNode;
				if(field.owner.equals(owner) && field.name.equals(var) && field.desc.equals(desc))
				{
					if(opcode == Opcodes.GETSTATIC && canWrite)
					{
						field.owner = newClass;
						field.name = newVar;
						field.desc = newDesc;
					}
					else if(opcode == Opcodes.PUTSTATIC && canRead)
					{
						field.owner = newClass;
						field.name = newVar;
						field.desc = newDesc;
					}
				}
			}
		}
	}

	private static void dumpClass(ClassNode node)
	{
		BufferedWriter writer = null;

		try
		{
			File file = new File(Settings.Dir.DUMP + "/" + node.name + ".dump");
			writer = new BufferedWriter(new FileWriter(file));

			writer.write(decodeAccess(node.access) + node.name + " extends " + node.superName + ";\n");
			writer.write("\n");

			Iterator<FieldNode> fieldNodeList = node.fields.iterator();
			while(fieldNodeList.hasNext())
			{
				FieldNode fieldNode = fieldNodeList.next();
				writer.write(decodeAccess(fieldNode.access) + fieldNode.desc + " " + fieldNode.name + ";\n");
			}

			writer.write("\n");

			Iterator<MethodNode> methodNodeList = node.methods.iterator();
			while(methodNodeList.hasNext())
			{
				MethodNode methodNode = methodNodeList.next();
				writer.write(decodeAccess(methodNode.access) + methodNode.name + " " + methodNode.desc + ":\n");

				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while(insnNodeList.hasNext())
				{
					AbstractInsnNode insnNode = insnNodeList.next();
					String instruction = decodeInstruction(insnNode);
					writer.write(instruction);
				}
				writer.write("\n");
			}

			writer.close();
		}
		catch(Exception e)
		{
			try { writer.close(); } catch(Exception e2) {}
		}
	}

	private static String decodeAccess(int access)
	{
		String res = "";

		if((access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC)
			res += "public ";
		if((access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE)
			res += "private ";
		if((access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED)
			res += "protected ";

		if((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC)
			res += "static ";
		if((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL)
			res += "final ";
		if((access & Opcodes.ACC_VOLATILE) == Opcodes.ACC_VOLATILE)
			res += "protected ";
		if((access & Opcodes.ACC_SYNCHRONIZED) == Opcodes.ACC_SYNCHRONIZED)
			res += "synchronized ";
		if((access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT)
			res += "abstract ";
		if((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE)
			res += "interface ";

		return res;
	}

	private static String decodeInstruction(AbstractInsnNode insnNode)
	{
		insnNode.accept(mp);
		StringWriter sw = new StringWriter();
		printer.print(new PrintWriter(sw));
		printer.getText().clear();
		return sw.toString();
	}

	private static Printer printer = new Textifier();
	private static TraceMethodVisitor mp = new TraceMethodVisitor(printer);
}

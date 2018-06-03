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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * Singleton class which hooks variables and patches classes.
 */
public class JClassPatcher {
	
	// Singleton
	private static JClassPatcher instance = null;
	
	private Printer printer = new Textifier();
	private TraceMethodVisitor mp = new TraceMethodVisitor(printer);
	
	private JClassPatcher() {
		// Empty private constructor to prevent extra instances from being created.
	}
	
	public byte[] patch(byte[] data) {
		ClassReader reader = new ClassReader(data);
		ClassNode node = new ClassNode();
		reader.accept(node, ClassReader.SKIP_DEBUG);
		
		if (node.name.equals("ua"))
			patchRenderer(node);
		else if (node.name.equals("e"))
			patchApplet(node);
		else if (node.name.equals("qa"))
			patchMenu(node);
		else if (node.name.equals("m"))
			patchData(node);
		else if (node.name.equals("client"))
			patchClient(node);
		else if (node.name.equals("f"))
			patchRandom(node);
		else if (node.name.equals("da"))
			patchGameApplet(node);
		
		// Patch applied to all classes
		patchGeneric(node);
		
		if (Settings.DISASSEMBLE) {
			Logger.Info("Disassembling file: " + node.name + ".class");
			dumpClass(node);
		}
		
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		return writer.toByteArray();
	}
	
	private void patchGeneric(ClassNode node) {
		Iterator<MethodNode> methodNodeList = node.methods.iterator();
		while (methodNodeList.hasNext()) {
			MethodNode methodNode = methodNodeList.next();
			
			hookClassVariable(methodNode, "ua", "fb", "Ljava/awt/image/ImageConsumer;", "Game/Renderer", "image_consumer", "Ljava/awt/image/ImageConsumer;", true, true);
			hookClassVariable(methodNode, "ua", "u", "I", "Game/Renderer", "width", "I", false, true);
			hookClassVariable(methodNode, "ua", "k", "I", "Game/Renderer", "height", "I", false, true);
			hookClassVariable(methodNode, "ua", "rb", "[I", "Game/Renderer", "pixels", "[I", true, true);
			
			hookClassVariable(methodNode, "e", "Ob", "Ljava/lang/String;", "Game/Client", "pm_enteredText", "Ljava/lang/String;", true, true);
			hookClassVariable(methodNode, "client", "Ob", "Ljava/lang/String;", "Game/Client", "pm_enteredText", "Ljava/lang/String;", true, true);
			hookClassVariable(methodNode, "e", "x", "Ljava/lang/String;", "Game/Client", "pm_text", "Ljava/lang/String;", true, true);
			hookClassVariable(methodNode, "client", "x", "Ljava/lang/String;", "Game/Client", "pm_text", "Ljava/lang/String;", true, true);
			
			hookClassVariable(methodNode, "client", "li", "Lba;", "Game/Renderer", "instance", "Ljava/lang/Object;", true, false);
			
			hookClassVariable(methodNode, "ba", "u", "I", "Game/Renderer", "width", "I", false, true);
			hookClassVariable(methodNode, "ba", "k", "I", "Game/Renderer", "height", "I", false, true);
			hookClassVariable(methodNode, "ba", "rb", "[I", "Game/Renderer", "pixels", "[I", true, true);
			
			hookStaticVariable(methodNode, "n", "g", "I", "Game/Client", "friends_count", "I");
			hookStaticVariable(methodNode, "ua", "h", "[Ljava/lang/String;", "Game/Client", "friends", "[Ljava/lang/String;");
			hookStaticVariable(methodNode, "ac", "z", "[Ljava/lang/String;", "Game/Client", "friends_world", "[Ljava/lang/String;");
			hookStaticVariable(methodNode, "cb", "c", "[Ljava/lang/String;", "Game/Client", "friends_formerly", "[Ljava/lang/String;");
			hookStaticVariable(methodNode, "client", "Fj", "[I", "Game/Client", "friends_online", "[I");
			
			hookStaticVariable(methodNode, "db", "g", "I", "Game/Client", "ignores_count", "I");
			hookStaticVariable(methodNode, "l", "c", "[Ljava/lang/String;", "Game/Client", "ignores", "[Ljava/lang/String;");
			hookStaticVariable(methodNode, "ia", "g", "[Ljava/lang/String;", "Game/Client", "ignores_formerly", "[Ljava/lang/String;");
			hookStaticVariable(methodNode, "ia", "a", "[Ljava/lang/String;", "Game/Client", "ignores_copy", "[Ljava/lang/String;");
			hookStaticVariable(methodNode, "ua", "wb", "[Ljava/lang/String;", "Game/Client", "ignores_formerly_copy", "[Ljava/lang/String;");
			
			hookClassVariable(methodNode, "client", "Wd", "I", "Game/Renderer", "width", "I", false, true);
			hookClassVariable(methodNode, "client", "Oi", "I", "Game/Renderer", "height_client", "I", false, true);
			
			hookClassVariable(methodNode, "e", "m", "I", "Game/Renderer", "width", "I", false, true);
			hookClassVariable(methodNode, "e", "a", "I", "Game/Renderer", "height", "I", false, true);
			hookClassVariable(methodNode, "e", "Ib", "I", "Game/Replay", "frame_time_slice", "I", true, true);
			hookClassVariable(methodNode, "client", "fc", "I", "Game/Replay", "connection_port", "I", true, true);
			
			hookClassVariable(methodNode, "lb", "pb", "[I", "Game/Renderer", "pixels", "[I", true, true);
			
			hookStaticVariable(methodNode, "client", "il", "[Ljava/lang/String;", "Game/Client", "strings", "[Ljava/lang/String;");
			
			hookStaticVariable(methodNode, "ac", "x", "[Ljava/lang/String;", "Game/Item", "item_name", "[Ljava/lang/String;");
			hookStaticVariable(methodNode, "lb", "ac", "[Ljava/lang/String;", "Game/Item", "item_commands", "[Ljava/lang/String;");
			
			hookClassVariable(methodNode, "lb", "Mb", "I", "Game/Camera", "distance1", "I", false, true);
			hookClassVariable(methodNode, "lb", "X", "I", "Game/Camera", "distance2", "I", false, true);
			hookClassVariable(methodNode, "lb", "P", "I", "Game/Camera", "distance3", "I", false, true);
			hookClassVariable(methodNode, "lb", "G", "I", "Game/Camera", "distance4", "I", false, true);
			
			hookClassVariable(methodNode, "client", "cl", "I", "Game/Client", "max_inventory", "I", true, false);
			hookClassVariable(methodNode, "client", "bk", "[Z", "Game/Client", "prayers_on", "[Z", true, false);
			hookClassVariable(methodNode, "client", "Fc", "[I", "Game/Client", "current_equipment_stats", "[I", true, false);
			hookClassVariable(methodNode, "client", "oh", "[I", "Game/Client", "current_level", "[I", true, false);
			hookClassVariable(methodNode, "client", "cg", "[I", "Game/Client", "base_level", "[I", true, false);
			hookClassVariable(methodNode, "client", "Vk", "[Ljava/lang/String;", "Game/Client", "skill_name", "[Ljava/lang/String;", true, false);
			hookClassVariable(methodNode, "client", "Ak", "[I", "Game/Client", "xp", "[I", true, false);
			hookClassVariable(methodNode, "client", "vg", "I", "Game/Client", "fatigue", "I", true, false);
			hookClassVariable(methodNode, "client", "Fg", "I", "Game/Client", "combat_style", "I", true, true);
			if (Settings.SAVE_LOGININFO)
				hookClassVariable(methodNode, "client", "Xd", "I", "Game/Client", "login_screen", "I", false, true);
			
			hookClassVariable(methodNode, "client", "Ek", "Llb;", "Game/Camera", "instance", "Ljava/lang/Object;", true, false);
			hookClassVariable(methodNode, "client", "qd", "I", "Game/Camera", "fov", "I", false, true);
			
			hookClassVariable(methodNode, "client", "ai", "I", "Game/Client", "combat_timer", "I", true, true);
			hookClassVariable(methodNode, "client", "Fe", "Z", "Game/Client", "show_bank", "Z", true, false);
			hookClassVariable(methodNode, "client", "dd", "Z", "Game/Client", "show_duel", "Z", true, false);
			hookClassVariable(methodNode, "client", "Pj", "Z", "Game/Client", "show_duelconfirm", "Z", true, false);
			hookClassVariable(methodNode, "client", "Bj", "I", "Game/Client", "show_friends", "I", true, true);
			hookClassVariable(methodNode, "client", "qc", "I", "Game/Client", "show_menu", "I", true, false);
			hookClassVariable(methodNode, "client", "Ph", "Z", "Game/Client", "show_questionmenu", "Z", true, false);
			hookClassVariable(methodNode, "client", "Vf", "I", "Game/Client", "show_report", "I", true, false);
			hookClassVariable(methodNode, "client", "uk", "Z", "Game/Client", "show_shop", "Z", true, false);
			hookClassVariable(methodNode, "client", "Qk", "Z", "Game/Client", "show_sleeping", "Z", true, false);
			hookClassVariable(methodNode, "client", "Hk", "Z", "Game/Client", "show_trade", "Z", true, false);
			hookClassVariable(methodNode, "client", "Xj", "Z", "Game/Client", "show_tradeconfirm", "Z", true, false);
			hookClassVariable(methodNode, "client", "Oh", "Z", "Game/Client", "show_welcome", "Z", true, true);
			
			hookClassVariable(methodNode, "client", "Qd", "Ljava/lang/String;", "Game/Client", "pm_username", "Ljava/lang/String;", true, true);
			
			hookClassVariable(methodNode, "client", "wh", "Ljava/lang/String;", "Game/Client", "username_login", "Ljava/lang/String;", true, true);
			hookClassVariable(methodNode, "client", "Vh", "I", "Game/Client", "autologin_timeout", "I", true, true);
			
			hookClassVariable(methodNode, "client", "lc", "I", "Game/Client", "inventory_count", "I", true, false);
			hookClassVariable(methodNode, "client", "vf", "[I", "Game/Client", "inventory_items", "[I", true, false);
			
			hookClassVariable(methodNode, "client", "ug", "I", "Game/Camera", "rotation", "I", true, true);
			hookClassVariable(methodNode, "client", "ac", "I", "Game/Camera", "zoom", "I", false, true);
			
			// Chat menu
			hookClassVariable(methodNode, "client", "yd", "Lqa;", "Game/Menu", "chat_menu", "Ljava/lang/Object;", true, false);
			hookClassVariable(methodNode, "client", "Fh", "I", "Game/Menu", "chat_type1", "I", true, false);
			hookClassVariable(methodNode, "client", "bh", "I", "Game/Menu", "chat_input", "I", true, false);
			hookClassVariable(methodNode, "client", "ud", "I", "Game/Menu", "chat_type2", "I", true, false);
			hookClassVariable(methodNode, "client", "mc", "I", "Game/Menu", "chat_type3", "I", true, false);
			
			// Quest menu
			hookClassVariable(methodNode, "client", "fe", "Lqa;", "Game/Menu", "quest_menu", "Ljava/lang/Object;", true, false);
			hookClassVariable(methodNode, "client", "lk", "I", "Game/Menu", "quest_handle", "I", true, false);
			
			// Friends menu
			hookClassVariable(methodNode, "client", "zk", "Lqa;", "Game/Menu", "friend_menu", "Ljava/lang/Object;", true, false);
			hookClassVariable(methodNode, "client", "Hi", "I", "Game/Menu", "friend_handle", "I", true, false);
			
			// Spell menu
			hookClassVariable(methodNode, "client", "Mc", "Lqa;", "Game/Menu", "spell_menu", "Ljava/lang/Object;", true, false);
			hookClassVariable(methodNode, "client", "Ud", "I", "Game/Menu", "spell_handle", "I", true, false);
			
			// Player name
			hookClassVariable(methodNode, "client", "wi", "Lta;", "Game/Client", "player_object", "Ljava/lang/Object;", true, false);
			// coordinates related
			hookClassVariable(methodNode, "client", "Qg", "I", "Game/Client", "regionX", "I", true, false);
			hookClassVariable(methodNode, "client", "zg", "I", "Game/Client", "regionY", "I", true, false);
			hookClassVariable(methodNode, "client", "Lf", "I", "Game/Client", "localRegionX", "I", true, false);
			hookClassVariable(methodNode, "client", "sh", "I", "Game/Client", "localRegionY", "I", true, false);
			hookClassVariable(methodNode, "client", "Ki", "I", "Game/Client", "planeWidth", "I", true, false);
			hookClassVariable(methodNode, "client", "sk", "I", "Game/Client", "planeHeight", "I", true, false);
			hookClassVariable(methodNode, "client", "bc", "I", "Game/Client", "planeIndex", "I", true, false);
			hookClassVariable(methodNode, "client", "Ub", "Z", "Game/Client", "loadingArea", "Z", true, false);
			
			// Last mouse activity
			// hookClassVariable(methodNode, "client", "sb", "I", "Game/Client", "lastMouseAction", "I", true, true);
			
			// Client version
			hookStaticVariable(methodNode, "fa", "d", "I", "Game/Client", "version", "I");
			
			// Shell strings
			hookStaticVariable(methodNode, "e", "Sb", "[Ljava/lang/String;", "Game/Renderer", "shellStrings", "[Ljava/lang/String;");
			
			hookClassVariable(methodNode, "client", "Jh", "Lda;", "Game/Client", "clientStream", "Ljava/lang/Object;",
					true, false);
			
			// Bank related vars
			hookClassVariable(methodNode, "client", "ci", "[I", "Game/Client", "new_bank_items", "[I", true, true);
			hookClassVariable(methodNode, "client", "Xe", "[I", "Game/Client", "new_bank_items_count", "[I", true, true);
			hookClassVariable(methodNode, "client", "ae", "[I", "Game/Client", "bank_items", "[I", true, true);
			hookClassVariable(methodNode, "client", "di", "[I", "Game/Client", "bank_items_count", "[I", true, true);
			hookClassVariable(methodNode, "client", "fj", "I", "Game/Client", "new_count_items_bank", "I", true, true);
			hookClassVariable(methodNode, "client", "vj", "I", "Game/Client", "count_items_bank", "I", true, true);
			hookClassVariable(methodNode, "client", "xg", "I", "Game/Client", "bank_active_page", "I", true, true);
			
			hookClassVariable(methodNode, "client", "sj", "I", "Game/Client", "selectedItem", "I", true, false);
			hookClassVariable(methodNode, "client", "Rd", "I", "Game/Client", "selectedItemSlot", "I", true, false);
		}
	}
	
	private void patchMenu(ClassNode node) {
		Logger.Info("Patching menu (" + node.name + ".class)");
		
		Iterator<MethodNode> methodNodeList = node.methods.iterator();
		while (methodNodeList.hasNext()) {
			MethodNode methodNode = methodNodeList.next();
			
			// Menu swap hook
			if (methodNode.name.equals("e") && methodNode.desc.equals("(II)V")) {
				AbstractInsnNode first = methodNode.instructions.getFirst();
				
				LabelNode label = new LabelNode();
				methodNode.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 0));
				methodNode.instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Menu", "switchList", "(Ljava/lang/Object;)Z"));
				methodNode.instructions.insertBefore(first, new JumpInsnNode(Opcodes.IFGT, label));
				methodNode.instructions.insertBefore(first, new InsnNode(Opcodes.RETURN));
				methodNode.instructions.insertBefore(first, label);
			}
		}
	}
	
	private void patchData(ClassNode node) {
		Logger.Info("Patching data (" + node.name + ".class)");
		
		Iterator<MethodNode> methodNodeList = node.methods.iterator();
		while (methodNodeList.hasNext()) {
			MethodNode methodNode = methodNodeList.next();
			
			if (methodNode.name.equals("a") && methodNode.desc.equals("([BBZ)V")) {
				// Data hook patches
				AbstractInsnNode lastNode = methodNode.instructions.getLast();
				methodNode.instructions.insertBefore(lastNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Item", "patchItemNames", "()V", false));
				methodNode.instructions.insertBefore(lastNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Item", "patchItemCommands", "()V", false));
			}
		}
	}
	
	private void patchApplet(ClassNode node) {
		Logger.Info("Patching applet (" + node.name + ".class)");
		
		Iterator<MethodNode> methodNodeList = node.methods.iterator();
		while (methodNodeList.hasNext()) {
			MethodNode methodNode = methodNodeList.next();
			
			if (methodNode.name.equals("run") && methodNode.desc.equals("()V")) {
				// Mouse and keyboard listener hooks
				AbstractInsnNode findNode = methodNode.instructions.getFirst();
				for (;;) {
					AbstractInsnNode next = findNode.getNext();
					
					if (next == null)
						break;
					
					if (findNode.getOpcode() == Opcodes.ALOAD && next.getOpcode() == Opcodes.ALOAD) {
						AbstractInsnNode invokeNode = next.getNext();
						MethodInsnNode invoke = (MethodInsnNode)invokeNode;
						methodNode.instructions.remove(next);
						methodNode.instructions.remove(invokeNode);
						if (invoke.name.equals("addMouseListener"))
							methodNode.instructions.insert(findNode, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/MouseHandler", "listener_mouse", "Ljava/awt/event/MouseListener;"));
						else if (invoke.name.equals("addMouseMotionListener"))
							methodNode.instructions.insert(findNode,
									new FieldInsnNode(Opcodes.PUTSTATIC, "Game/MouseHandler", "listener_mouse_motion", "Ljava/awt/event/MouseMotionListener;"));
						else if (invoke.name.equals("addKeyListener"))
							methodNode.instructions.insert(findNode, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/KeyboardHandler", "listener_key", "Ljava/awt/event/KeyListener;"));
					}
					findNode = findNode.getNext();
				}
				
				// Throwable crash patch
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					
					if (nextNode == null)
						break;
					
					if (insnNode.getOpcode() == Opcodes.INVOKESTATIC && nextNode.getOpcode() == Opcodes.ATHROW) {
						MethodInsnNode call = (MethodInsnNode)insnNode;
						methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.RETURN));
					}
				}
			} else if (methodNode.name.equals("a") && methodNode.desc.equals("(IB)V")) {
				// FPS hook
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					
					if (nextNode == null)
						break;
					
					if (insnNode.getOpcode() == Opcodes.GETSTATIC) {
						FieldInsnNode call = (FieldInsnNode)insnNode;
						methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Replay", "getFPS", "()I", false));
						methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ISTORE, 1));
					}
				}
			} else if (methodNode.name.equals("a") && methodNode.desc.equals("(Ljava/lang/String;Z)V")) {
				// this part shows error_game_
				// we want to call disconnect hook for instance in error_game_crash
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					
					if (nextNode == null)
						break;
					
					if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode)insnNode).name.equals("println")) {
						LabelNode call = (LabelNode)insnNode.getNext();
						methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 1));
						methodNode.instructions.insertBefore(call, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "error_game_hook", "(Ljava/lang/String;)V", false));
					}
				}
			}
		}
	}
	
	private void patchClient(ClassNode node) {
		Logger.Info("Patching client (" + node.name + ".class)");
		
		Iterator<MethodNode> methodNodeList = node.methods.iterator();
		while (methodNodeList.hasNext()) {
			MethodNode methodNode = methodNodeList.next();
			
			// handlePacket
			if (methodNode.name.equals("a") && methodNode.desc.equals("(III)V")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				AbstractInsnNode insnNode = insnNodeList.next();
				methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Replay", "patchClient", "()V", false));
				// save encrypted opcodes (used when decrypted opcode = 182)
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 3));
				methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Replay", "saveEncOpcode", "(I)V", false));
				
				//fetch opcode for packets
				insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					insnNode = insnNodeList.next();
					
					if (insnNode.getOpcode() == Opcodes.ISTORE && ((VarInsnNode)insnNode).var == 3) {
						VarInsnNode call = (VarInsnNode)insnNode.getNext();
						
						// check opcode against checkpoint 182 - code for welcome screen info
						methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 3));
						methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 2));
						methodNode.instructions.insertBefore(call, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Replay", "checkPoint", "(II)V", false));
						break;
					}
				}
			}
			// sendLogout
			else if (methodNode.name.equals("B") && methodNode.desc.equals("(I)V")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				AbstractInsnNode insnNode = insnNodeList.next();
				methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Replay", "closeReplayPlayback", "()V", false));
			}
			// I (I)V is where most of the interface is processed
			else if (methodNode.name.equals("I") && methodNode.desc.equals("(I)V")) {
				// Show combat menu
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					
					if (insnNode.getOpcode() == Opcodes.BIPUSH) {
						IntInsnNode bipush = (IntInsnNode)insnNode;
						
						if (bipush.operand == -9) {
							AbstractInsnNode findNode = insnNode;
							while (findNode.getOpcode() != Opcodes.IF_ICMPEQ)
								findNode = findNode.getNext();
							
							LabelNode label = ((JumpInsnNode)findNode).label;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Client/Settings", "COMBAT_MENU", "Z"));
							methodNode.instructions.insertBefore(insnNode, new JumpInsnNode(Opcodes.IFGT, label));
							break;
						}
					}
				}
			} else if (methodNode.name.equals("a") && methodNode.desc.equals("(ILjava/lang/String;Ljava/lang/String;Z)V")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				int xteaIndex = 0;
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					
					// Dump login xtea keys
					if (xteaIndex < 4 && insnNode.getOpcode() == Opcodes.IASTORE) {
						methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Replay", "hookXTEAKey", "(I)I", false));
						xteaIndex++;
					}
				}
				
				insnNodeList = methodNode.instructions.iterator();
				
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					AbstractInsnNode twoNextNodes = nextNode.getNext();
					
					if (nextNode == null || twoNextNodes == null)
						break;
					
					if (insnNode.getOpcode() == Opcodes.GETSTATIC && ((FieldInsnNode)insnNode).name.equals("il") &&
							nextNode.getOpcode() == Opcodes.SIPUSH
							&& ((IntInsnNode)nextNode).operand == 439
							&& twoNextNodes.getOpcode() == Opcodes.AALOAD) {
						// just do the logic in client
						MethodInsnNode call = (MethodInsnNode)(twoNextNodes.getNext());
						// loginresponse
						methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 11));
						// reconnecting
						methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 4));
						// xtea keys
						methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 8));
						methodNode.instructions.insertBefore(call, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client",
								"login_attempt_hook", "(IZ[I)V", false));
						break;
					}
				}
			} else if (methodNode.name.equals("u") && methodNode.desc.equals("(I)V")) {
				// Replay pause hook
				// TODO: Not sure but it seems like it gets broken upon starting another replay sometimes?
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				AbstractInsnNode findNode = insnNodeList.next();
				
				LabelNode label = new LabelNode();
				methodNode.instructions.insertBefore(findNode, new InsnNode(Opcodes.ICONST_0));
				methodNode.instructions.insertBefore(findNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Replay", "paused", "Z"));
				methodNode.instructions.insertBefore(findNode, new JumpInsnNode(Opcodes.IFEQ, label));
				methodNode.instructions.insertBefore(findNode, new InsnNode(Opcodes.RETURN));
				methodNode.instructions.insertBefore(findNode, label);
				
			} else if (methodNode.name.equals("J") && methodNode.desc.equals("(I)V")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					
					// Chat command patch
					if (insnNode.getOpcode() == Opcodes.SIPUSH) {
						IntInsnNode call = (IntInsnNode)insnNode;
						if (call.operand == 627) {
							AbstractInsnNode jmpNode = insnNode;
							while (jmpNode.getOpcode() != Opcodes.IFEQ)
								jmpNode = jmpNode.getNext();
							
							AbstractInsnNode insertNode = insnNode;
							while (insertNode.getOpcode() != Opcodes.INVOKEVIRTUAL)
								insertNode = insertNode.getPrevious();
							
							JumpInsnNode jmp = (JumpInsnNode)jmpNode;
							methodNode.instructions.insert(insertNode, new VarInsnNode(Opcodes.ASTORE, 2));
							methodNode.instructions.insert(insertNode,
									new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "processChatCommand", "(Ljava/lang/String;)Ljava/lang/String;"));
							methodNode.instructions.insert(insertNode, new VarInsnNode(Opcodes.ALOAD, 2));
						}
					}
				}
				
				insnNodeList = methodNode.instructions.iterator();
				
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					AbstractInsnNode twoNextNode = nextNode.getNext();
					
					if (nextNode == null || twoNextNode == null)
						break;
					
					if (insnNode.getOpcode() == Opcodes.IMUL && nextNode.getOpcode() == Opcodes.IADD && twoNextNode.getOpcode() == Opcodes.PUTFIELD
							&& ((FieldInsnNode)twoNextNode).name.equals("ug")) {
						// entry point when its true to close it
						// intercept first time camera rotation
						// TODO: the setting constant at first may give bug in camera so im leaving it out for the
						// moment
						// AbstractInsnNode call = twoNextNode.getNext();
						// methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
						// this constant is from the one in Camera
						// methodNode.instructions.insertBefore(call, new IntInsnNode(Opcodes.BIPUSH, 126));
						// methodNode.instructions.insertBefore(call, new FieldInsnNode(Opcodes.PUTFIELD, "client",
						// "ug", "I"));
						break;
					}
				}
			} else if (methodNode.name.equals("h") && methodNode.desc.equals("(B)V")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					
					// Private chat command patch
					if (insnNode.getOpcode() == Opcodes.GETFIELD) {
						FieldInsnNode field = (FieldInsnNode)insnNode;
						if (field.owner.equals("client") && field.name.equals("Ob") && insnNode.getPrevious().getPrevious().getOpcode() != Opcodes.INVOKEVIRTUAL) {
							insnNode = insnNode.getPrevious().getPrevious();
							methodNode.instructions.insert(insnNode, new FieldInsnNode(Opcodes.PUTFIELD, "client", "Ob", "Ljava/lang/String;"));
							methodNode.instructions.insert(insnNode,
									new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "processPrivateCommand", "(Ljava/lang/String;)Ljava/lang/String;"));
							methodNode.instructions.insert(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Ob", "Ljava/lang/String;"));
							methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
							methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
							break;
						}
					}
				}
			} else if (methodNode.name.equals("a") && methodNode.desc.equals("(IIIIIIII)V")) {
				// Draw NPC hook
				AbstractInsnNode insnNode = methodNode.instructions.getLast();
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 1));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 7));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 4));
				
				methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "e", "Mb", "[Ljava/lang/String;"));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
				methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Tb", "[Lta;"));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 6));
				methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
				methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "t", "I"));
				methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
				// extended npc hook to include current hits and max hits
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
				methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Tb", "[Lta;"));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 6));
				methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
				methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "B", "I"));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
				methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Tb", "[Lta;"));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 6));
				methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
				methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "G", "I"));
				methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "drawNPC", "(IIIILjava/lang/String;II)V"));
			} else if (methodNode.name.equals("b") && methodNode.desc.equals("(IIIIIIII)V")) {
				// Draw Player hook
				AbstractInsnNode insnNode = methodNode.instructions.getLast();
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 5));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 6));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 2));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 7));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
				methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "rg", "[Lta;"));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
				methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
				methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "C", "Ljava/lang/String;"));
				// extended player hook to include current hits and max hits
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
				methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "rg", "[Lta;"));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
				methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
				methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "B", "I"));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
				methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "rg", "[Lta;"));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
				methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
				methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "G", "I"));
				methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "drawPlayer", "(IIIILjava/lang/String;II)V"));
			} else if (methodNode.name.equals("b") && methodNode.desc.equals("(IIIIIII)V")) {
				// Draw Item hook
				// ILOAD 4 is item id
				AbstractInsnNode insnNode = methodNode.instructions.getLast();
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 3));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 7));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 5));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 1));
				methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 4));
				methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "drawItem", "(IIIII)V"));
			} else if (methodNode.name.equals("L") && methodNode.desc.equals("(I)V")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					
					// Right click bounds fix
					if (insnNode.getOpcode() == Opcodes.SIPUSH) {
						IntInsnNode call = (IntInsnNode)insnNode;
						AbstractInsnNode nextNode = insnNode.getNext();
						
						if (call.operand == 510) {
							call.operand = 512 - call.operand;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
						} else if (call.operand == 315) {
							call.operand = 334 - call.operand;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height_client", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
						} else if (call.operand == -316) {
							call.operand = 334 - (call.operand * -1);
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height_client", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.INEG));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
						}
					}
				}
				
				// Hook that gives out the message on X action such as npcs, items and prints them top left corner
				// TODO: use the hook
				insnNodeList = methodNode.instructions.iterator();
				
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					
					// getfield client.li:ba
					if (insnNode.getOpcode() == Opcodes.GETFIELD) {
						FieldInsnNode fieldNode = ((FieldInsnNode)insnNode);
						if (fieldNode.owner.equals("client") && fieldNode.name.equals("li") && nextNode.getOpcode() == Opcodes.ALOAD && ((VarInsnNode)nextNode).var == 5) {
							methodNode.instructions.insert(fieldNode,
									new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "mouse_action_hook", "(Ljava/lang/String;)V", false));
							methodNode.instructions.insert(fieldNode, new VarInsnNode(Opcodes.ALOAD, 5));
							break;
						}
					}
				}
			} else if (methodNode.name.equals("a") && methodNode.desc.equals("(ZZ)V")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					
					// Friends chat mouse fix
					if (insnNode.getOpcode() == Opcodes.SIPUSH) {
						IntInsnNode call = (IntInsnNode)insnNode;
						if (call.operand == 489 || call.operand == 429) {
							call.operand = 512 - call.operand;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
						}
						if (call.operand == -430) {
							call.operand = 512 - (call.operand * -1);
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.INEG));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
						}
					}
				}
			} else if (methodNode.name.equals("i") && methodNode.desc.equals("(I)V")) {
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
			} else if (methodNode.name.equals("o") && methodNode.desc.equals("(I)V")) {
				// Client.init_login patch
				AbstractInsnNode findNode = methodNode.instructions.getLast();
				methodNode.instructions.insertBefore(findNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "init_login", "()V", false));
			} else if (methodNode.name.equals("a") && methodNode.desc.equals("(B)V")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					
					// Camera view distance crash fix
					if (insnNode.getOpcode() == Opcodes.SIPUSH) {
						IntInsnNode call = (IntInsnNode)insnNode;
						if (call.operand == 15000) {
							call.operand = 32767;
						}
					}
				}
				
				// Client.init patch
				AbstractInsnNode findNode = methodNode.instructions.getFirst();
				methodNode.instructions.insertBefore(findNode, new VarInsnNode(Opcodes.ALOAD, 0));
				methodNode.instructions.insertBefore(findNode, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "instance", "Ljava/lang/Object;"));
				methodNode.instructions.insertBefore(findNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "init", "()V", false));
			} else if (methodNode.name.equals("G") && methodNode.desc.equals("(I)V")) {
				// TODO: This can be shortened, I'll fix it another time
				
				// NPC Dialogue keyboard
				AbstractInsnNode lastNode = methodNode.instructions.getLast().getPrevious();
				
				LabelNode label = new LabelNode();
				
				methodNode.instructions.insert(lastNode, label);
				
				// Hide dialogue
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.PUTFIELD, "client", "Ph", "Z"));
				methodNode.instructions.insert(lastNode, new InsnNode(Opcodes.ICONST_0));
				methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));
				
				// Format
				methodNode.instructions.insert(lastNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "da", "b", "(I)V", false));
				methodNode.instructions.insert(lastNode, new IntInsnNode(Opcodes.SIPUSH, 21294));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
				methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));
				
				// Write byte
				methodNode.instructions.insert(lastNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "ja", "c", "(II)V", false));
				methodNode.instructions.insert(lastNode, new IntInsnNode(Opcodes.BIPUSH, 115));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/KeyboardHandler", "dialogue_option", "I"));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETFIELD, "da", "f", "Lja;"));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
				methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));
				
				// Create Packet
				methodNode.instructions.insert(lastNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "da", "b", "(II)V", false));
				methodNode.instructions.insert(lastNode, new InsnNode(Opcodes.ICONST_0));
				methodNode.instructions.insert(lastNode, new IntInsnNode(Opcodes.BIPUSH, 116));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
				methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));
				
				// Check if dialogue option is pressed
				methodNode.instructions.insert(lastNode, new JumpInsnNode(Opcodes.IF_ICMPGE, label));
				// Menu option count
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Id", "I"));
				methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/KeyboardHandler", "dialogue_option", "I"));
				methodNode.instructions.insert(lastNode, new JumpInsnNode(Opcodes.IFLT, label));
				methodNode.instructions.insert(lastNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/KeyboardHandler", "dialogue_option", "I"));
			} else if (methodNode.name.equals("f") && methodNode.desc.equals("(I)V")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					
					// Hide Roof option
					if (insnNode.getOpcode() == Opcodes.GETFIELD) {
						FieldInsnNode field = (FieldInsnNode)insnNode;
						
						if (field.owner.equals("client") && field.name.equals("yj")) {
							AbstractInsnNode nextNode = insnNode.getNext();
							if (nextNode.getOpcode() == Opcodes.IFNE) {
								LabelNode label = ((JumpInsnNode)nextNode).label;
								methodNode.instructions.insert(nextNode, new JumpInsnNode(Opcodes.IFGT, label));
								methodNode.instructions.insert(nextNode, new FieldInsnNode(Opcodes.GETSTATIC, "Client/Settings", "HIDE_ROOFS", "Z"));
							}
						}
					}
					
					// Move wilderness skull
					if (insnNode.getOpcode() == Opcodes.SIPUSH) {
						IntInsnNode call = (IntInsnNode)insnNode;
						if (call.operand == 465 || call.operand == 453) {
							call.operand = 512 - call.operand;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
						}
					}
				}
			} else if (methodNode.name.equals("a") && methodNode.desc.equals("(IIZ)Z")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					
					// Move the load screen text dialogue
					if (insnNode.getOpcode() == Opcodes.SIPUSH) {
						IntInsnNode call = (IntInsnNode)insnNode;
						if (call.operand == 256) {
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						} else if (call.operand == 192) {
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IADD));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 19));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
					}
				}
			} else if (methodNode.name.equals("d") && methodNode.desc.equals("(B)V")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					
					// Center logout dialogue
					if (insnNode.getOpcode() == Opcodes.SIPUSH || insnNode.getOpcode() == Opcodes.BIPUSH) {
						IntInsnNode call = (IntInsnNode)insnNode;
						if (call.operand == 256) {
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						} else if (call.operand == 173) {
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						} else if (call.operand == 126) {
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 130));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						} else if (call.operand == 137) {
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 36));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
					}
				}
			} else if (methodNode.name.equals("j") && methodNode.desc.equals("(I)V")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					
					// Center welcome box
					if (insnNode.getOpcode() == Opcodes.SIPUSH || insnNode.getOpcode() == Opcodes.BIPUSH) {
						IntInsnNode call = (IntInsnNode)insnNode;
						if (call.operand == 256) {
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						} else if (call.operand == 167) {
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 6));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						} else if (call.operand == 56) {
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 200));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						} else if (call.operand == -87) {
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.INEG));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 169));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						} else if (call.operand == 426) {
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IADD));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 170));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						} else if (call.operand == 106) {
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 150));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						} else if (call.operand == 406) {
							call.operand = 2;
							methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IADD));
							methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 150));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
						}
					}
				}
			} else if (methodNode.name.equals("k") && methodNode.desc.equals("(B)V")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					
					// Save settings from combat menu
					if (insnNode.getOpcode() == Opcodes.PUTFIELD) {
						FieldInsnNode field = (FieldInsnNode)insnNode;
						
						if (field.owner.equals("client") && field.name.equals("Fg")) {
							methodNode.instructions.insert(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Client/Settings", "save", "()V", false));
							methodNode.instructions.insert(insnNode, new FieldInsnNode(Opcodes.PUTSTATIC, "Client/Settings", "COMBAT_STYLE", "I"));
							methodNode.instructions.insert(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Fg", "I"));
							methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
						}
					}
				}
			} else if (methodNode.name.equals("a") && methodNode.desc.equals("(ZLjava/lang/String;ILjava/lang/String;IILjava/lang/String;Ljava/lang/String;)V")) {
				AbstractInsnNode first = methodNode.instructions.getFirst();
				methodNode.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 7));
				methodNode.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 4));
				methodNode.instructions.insertBefore(first, new VarInsnNode(Opcodes.ILOAD, 5));
				methodNode.instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "messageHook", "(Ljava/lang/String;Ljava/lang/String;I)V"));
			} else if (methodNode.name.equals("b") && methodNode.desc.equals("(ZI)V")) {
				// Fix on swap between command and use, if 635 is received make it 650 by hook
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					
					if (nextNode == null)
						break;
					
					if (insnNode.getOpcode() == Opcodes.ISTORE && ((VarInsnNode)insnNode).var == 3) {
						VarInsnNode call = (VarInsnNode)nextNode;
						methodNode.instructions.insertBefore(nextNode, new VarInsnNode(Opcodes.ILOAD, 3));
						methodNode.instructions.insertBefore(nextNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "swapUseMenuHook", "(I)I"));
						methodNode.instructions.insertBefore(nextNode, new VarInsnNode(Opcodes.ISTORE, 3));
						break;
					}
				}
				
				// Throwable crash patch
				insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					
					if (nextNode == null)
						break;
					
					if (insnNode.getOpcode() == Opcodes.INVOKESTATIC && nextNode.getOpcode() == Opcodes.ATHROW) {
						MethodInsnNode call = (MethodInsnNode)insnNode;
						methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.RETURN));
					}
				}
				
				// Fix on sleep, so packets are not managed directly
				insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode prevNode = insnNode.getPrevious();
					
					if (prevNode == null)
						continue;
					
					// patch before the sequence of command checks
					if (insnNode.getOpcode() == Opcodes.ASTORE && ((VarInsnNode)insnNode).var == 9 && prevNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
						VarInsnNode call = (VarInsnNode)insnNode;
						LabelNode label = ((LabelNode)insnNode.getNext());
						
						methodNode.instructions.insert(call, new VarInsnNode(Opcodes.ISTORE, 4));
						methodNode.instructions.insert(call, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Client",
								"sleepBagIdx", "I"));
						methodNode.instructions.insert(call, new VarInsnNode(Opcodes.ISTORE, 3));
						methodNode.instructions.insert(call, new IntInsnNode(Opcodes.SIPUSH, (short)640));
						methodNode.instructions.insert(call, new JumpInsnNode(Opcodes.IF_ICMPNE, label));
						methodNode.instructions.insert(call, new InsnNode(Opcodes.ICONST_1));
						methodNode.instructions.insert(call, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Client",
								"sleepCmdSent", "Z"));
					}
				}
				
				// Turn off sleep cmd flag
				insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					AbstractInsnNode twoNextNodes = nextNode.getNext();
					
					if (nextNode == null || twoNextNodes == null)
						break;
					
					if (insnNode.getOpcode() == Opcodes.ALOAD && nextNode.getOpcode() == Opcodes.GETFIELD && twoNextNodes.getOpcode() == Opcodes.BIPUSH
							&& ((IntInsnNode)twoNextNodes).operand == 90) {
						VarInsnNode call = (VarInsnNode)insnNode;
						methodNode.instructions.insert(call, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client",
								"sleepCmdSent", "Z"));
						methodNode.instructions.insert(call, new InsnNode(Opcodes.ICONST_0));
					}
				}
			} else if (methodNode.name.equals("C") && methodNode.desc.equals("(I)V")) {
				// Hook updateBankItems
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode prevNode = insnNode.getPrevious();
					
					if (prevNode == null)
						continue;
					
					if (insnNode.getOpcode() == Opcodes.ISTORE && prevNode.getOpcode() == Opcodes.GETSTATIC) {
						VarInsnNode call = (VarInsnNode)insnNode;
						methodNode.instructions.insert(call, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Bank", "updateBankItemsHook", "()V"));
						break;
					}
				}
				// plus hook final bank items
				insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode prevNode = insnNode.getPrevious();
					
					if (prevNode == null)
						continue;
					
					if (insnNode.getOpcode() == Opcodes.ISTORE && prevNode.getOpcode() == Opcodes.IDIV) {
						VarInsnNode call = (VarInsnNode)insnNode;
						methodNode.instructions.insert(call, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Bank", "finalBankItemsHook", "()V"));
						break;
					}
				}
				
			} else if (methodNode.name.equals("b") && methodNode.desc.equals("(IBI)V")) {
				// hook first time opened bank interface
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					
					if (nextNode == null)
						continue;
					if (insnNode.getOpcode() == Opcodes.ICONST_1 && nextNode.getOpcode() == Opcodes.PUTFIELD && ((FieldInsnNode)nextNode).name.equals("Fe")) {
						InsnNode call = (InsnNode)insnNode;
						methodNode.instructions.insertBefore(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
								"Game/Bank", "openedBankInterfaceHook", "()V"));
						break;
					}
				}
				
				// hook onto npc attack info
				insnNodeList = methodNode.instructions.iterator();
				// two times it gets found, first is one for player second for npc
				int pos = -1;
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					AbstractInsnNode twoNextNode = nextNode.getNext();
					
					if (nextNode == null || twoNextNode == null)
						break;
					if (insnNode.getOpcode() == Opcodes.SIPUSH && ((IntInsnNode)insnNode).operand == -235) {
						pos = 0; // player combat hook
						continue;
					}
					if (insnNode.getOpcode() == Opcodes.BIPUSH && ((IntInsnNode)insnNode).operand == 104 && nextNode.getOpcode() == Opcodes.ILOAD) {
						pos = 1; // npc combat hook
						continue;
					}
					if (insnNode.getOpcode() == Opcodes.ALOAD && ((VarInsnNode)insnNode).var == 7 && nextNode.getOpcode() == Opcodes.ILOAD && ((VarInsnNode)nextNode).var == 9
							&& twoNextNode.getOpcode() == Opcodes.PUTFIELD && ((FieldInsnNode)twoNextNode).owner.equals("ta") && ((FieldInsnNode)twoNextNode).name.equals("u")) {
						methodNode.instructions.insert(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC,
								"Game/Client", "inCombatHook", "(IIIIILjava/lang/Object;)V"));
						methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ALOAD, 7));
						// indicate packet was from player cmd
						if (pos == 0) {
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ICONST_0));
						}
						// indicate packet was from npc cmd
						else if (pos == 1) {
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ICONST_1));
						}
						methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ILOAD, 6));
						methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ILOAD, 11));
						methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ILOAD, 10));
						methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ILOAD, 9));
						continue;
					}
				}
				
				// hook onto received menu options
				insnNodeList = methodNode.instructions.iterator();
				LabelNode lblNode = null;
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					AbstractInsnNode twoNextNode = nextNode.getNext();
					
					if (nextNode == null || twoNextNode == null)
						break;
					
					if (insnNode.getOpcode() == Opcodes.ALOAD && ((VarInsnNode)insnNode).var == 0 && nextNode.getOpcode() == Opcodes.GETFIELD
							&& ((FieldInsnNode)nextNode).owner.equals("client") && ((FieldInsnNode)nextNode).name.equals("ah")
							&& twoNextNode.getOpcode() == Opcodes.ILOAD && ((VarInsnNode)twoNextNode).var == 5
							&& insnNode.getPrevious().getOpcode() == Opcodes.IF_ICMPLE) {
						// in here is part where menu options are received, is a loop
						lblNode = ((JumpInsnNode)insnNode.getPrevious()).label;
						continue;
					}
					
					if (lblNode != null && insnNode instanceof LabelNode && ((LabelNode)insnNode).equals(lblNode) && twoNextNode.getOpcode() == Opcodes.RETURN) {
						InsnNode call = (InsnNode)twoNextNode;
						methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
						methodNode.instructions.insertBefore(call, new FieldInsnNode(Opcodes.GETFIELD, "client", "ah", "[Ljava/lang/String;"));
						// could also be client.Id but more lines would be needed
						methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 4));
						methodNode.instructions.insertBefore(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
								"Game/Client", "receivedOptionsHook", "([Ljava/lang/String;I)V"));
					}
				}
				
			}
			// hook menu item
			else if (methodNode.name.equals("a") && methodNode.desc.equals("(IZ)V")) {
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				LabelNode firstLabel = null;
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode prevNode = insnNode.getPrevious();
					AbstractInsnNode twoPrevNodes = null;
					if (prevNode != null)
						twoPrevNodes = prevNode.getPrevious();
					
					if (insnNode.getNext() == null)
						continue;
					
					if (prevNode == null || twoPrevNodes == null)
						continue;
					
					if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL && prevNode.getOpcode() == Opcodes.LDC && prevNode instanceof LdcInsnNode
							&& ((LdcInsnNode)prevNode).cst instanceof String && ((String)((LdcInsnNode)prevNode).cst).equals("")
							&& twoPrevNodes.getOpcode() == Opcodes.AALOAD) {
						methodNode.instructions.insert(prevNode, new InsnNode(Opcodes.RETURN));
						methodNode.instructions.insert(prevNode, new MethodInsnNode(Opcodes.INVOKESTATIC,
								"Game/Client", "redrawMenuHook", "(Ljava/lang/Object;IILjava/lang/String;Ljava/lang/String;)V"));
						methodNode.instructions.insert(prevNode, new InsnNode(Opcodes.AALOAD));
						methodNode.instructions.insert(prevNode, new VarInsnNode(Opcodes.ILOAD, 6));
						methodNode.instructions.insert(prevNode, new FieldInsnNode(Opcodes.GETSTATIC, "ac",
								"x", "[Ljava/lang/String;"));
						methodNode.instructions.insert(prevNode, new InsnNode(Opcodes.AALOAD));
						methodNode.instructions.insert(prevNode, new VarInsnNode(Opcodes.ILOAD, 6));
						methodNode.instructions.insert(prevNode, new FieldInsnNode(Opcodes.GETSTATIC, "lb",
								"ac", "[Ljava/lang/String;"));
						methodNode.instructions.insert(prevNode, new VarInsnNode(Opcodes.ILOAD, 6));
						methodNode.instructions.insert(prevNode, new VarInsnNode(Opcodes.ILOAD, 5));
						methodNode.instructions.insert(prevNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "zh", "Lwb;"));
						methodNode.instructions.insert(prevNode, new VarInsnNode(Opcodes.ALOAD, 0));
						continue;
					}
				}
			} else if (methodNode.name.equals("x") && methodNode.desc.equals("(I)V")) {
				// Login button press hook
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				
				AbstractInsnNode findNode = null;
				int count = 0;
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					if (insnNode.getOpcode() == Opcodes.PUTFIELD) {
						FieldInsnNode field = (FieldInsnNode)insnNode;
						if (count != 1 && field.owner.equals("client") && field.name.equals("wh") &&
								field.desc.equals("Ljava/lang/String;")) {
							findNode = insnNode.getNext();
							count++;
						}
					}
				}
				
				methodNode.instructions.insertBefore(findNode, new MethodInsnNode(Opcodes.INVOKESTATIC,
						"Game/Client", "login_hook", "()V", false));
			} else if (methodNode.name.equals("a") && methodNode.desc.equals("(ZI)V")) {
				// Disconnect hook (::closecon)
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					
					if (nextNode == null)
						break;
					
					if (insnNode.getOpcode() == Opcodes.SIPUSH && ((IntInsnNode)insnNode).operand == -6924 && nextNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
						// entry point when its true to close it
						methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "disconnect_hook", "()V", false));
						break;
					}
				}
			} else if (methodNode.name.equals("s") && methodNode.desc.equals("(I)V")) {
				 // bypass npc attack on left option, regardless of level difference if user wants it that way
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode prevNode = insnNode.getPrevious();
					
					if (prevNode == null)
						continue;
					
					if (insnNode.getOpcode() == Opcodes.ILOAD && ((VarInsnNode)insnNode).var == 12 && prevNode.getOpcode() == Opcodes.GETFIELD
							&& ((FieldInsnNode)prevNode).owner.equals("ta") && ((FieldInsnNode)prevNode).name.equals("b")) {
						methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ILOAD, 12));
						methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ISTORE, 12));
						methodNode.instructions.insert(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "attack_menu_hook", "(I)I", false));
						break;
					}
				}
			}
			
			if (methodNode.name.equals("e") && methodNode.desc.equals("(I)V")) {
				// handleGameInput
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				
				AbstractInsnNode insnNode = insnNodeList.next();
				methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "update", "()V", false));
			}
		}
	}
	
	private void patchRenderer(ClassNode node) {
		Logger.Info("Patching renderer (" + node.name + ".class)");
		
		Iterator<MethodNode> methodNodeList = node.methods.iterator();
		while (methodNodeList.hasNext()) {
			MethodNode methodNode = methodNodeList.next();
			
			// Renderer present hook
			if (methodNode.desc.equals("(Ljava/awt/Graphics;III)V")) {
				AbstractInsnNode findNode = methodNode.instructions.getFirst();
				FieldInsnNode imageNode = null;
				
				while (findNode.getOpcode() != Opcodes.POP) {
					findNode = findNode.getNext();
					if (findNode == null) {
						Logger.Error("Unable to find present hook");
						break;
					}
				}
				
				while (findNode.getOpcode() != Opcodes.INVOKESPECIAL) {
					if (findNode.getOpcode() == Opcodes.GETFIELD)
						imageNode = (FieldInsnNode)findNode;
					
					AbstractInsnNode prev = findNode.getPrevious();
					methodNode.instructions.remove(findNode);
					findNode = prev;
				}
				
				methodNode.instructions.insert(findNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Renderer", "present", "(Ljava/awt/Graphics;Ljava/awt/Image;)V", false));
				methodNode.instructions.insert(findNode, new FieldInsnNode(Opcodes.GETFIELD, node.name, imageNode.name, imageNode.desc));
				methodNode.instructions.insert(findNode, new VarInsnNode(Opcodes.ALOAD, 0));
				methodNode.instructions.insert(findNode, new VarInsnNode(Opcodes.ALOAD, 1));
			} else if (methodNode.name.equals("a") && methodNode.desc.equals("(IILjava/lang/String;IIBI)V")) {
				AbstractInsnNode start = methodNode.instructions.getFirst();
				while (start != null) {
					if (start.getOpcode() == Opcodes.ALOAD && start.getNext().getOpcode() == Opcodes.ILOAD && start.getNext().getNext().getOpcode() == Opcodes.INVOKEVIRTUAL
							&& start.getNext().getNext().getNext().getOpcode() == Opcodes.ISTORE) {
						break;
					}
					
					start = start.getNext();
				}
				start = start.getPrevious();
				
				LabelNode finishLabel = ((JumpInsnNode)start.getPrevious().getPrevious()).label;
				LabelNode failLabel = new LabelNode();
				
				methodNode.instructions.insertBefore(start, new IntInsnNode(Opcodes.BIPUSH, 126));
				methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ALOAD, 3));
				methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ILOAD, 10));
				methodNode.instructions.insertBefore(start, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"));
				methodNode.instructions.insertBefore(start, new JumpInsnNode(Opcodes.IF_ICMPNE, failLabel));
				
				methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ILOAD, 10));
				methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.ICONST_5));
				methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.IADD));
				methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ALOAD, 3));
				methodNode.instructions.insertBefore(start, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I"));
				methodNode.instructions.insertBefore(start, new JumpInsnNode(Opcodes.IF_ICMPGE, failLabel));
				
				methodNode.instructions.insertBefore(start, new IntInsnNode(Opcodes.BIPUSH, 126));
				methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ALOAD, 3));
				methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ILOAD, 10));
				methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.ICONST_5));
				methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.IADD));
				methodNode.instructions.insertBefore(start, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"));
				methodNode.instructions.insertBefore(start, new JumpInsnNode(Opcodes.IF_ICMPNE, failLabel));
				
				methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ALOAD, 3));
				methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ILOAD, 10));
				methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.ICONST_1));
				methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.IADD));
				methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ILOAD, 10));
				methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.ICONST_5));
				methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.IADD));
				methodNode.instructions.insertBefore(start, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;"));
				methodNode.instructions.insertBefore(start, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I"));
				methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ISTORE, 4));
				methodNode.instructions.insertBefore(start, new IincInsnNode(10, 5));
				
				methodNode.instructions.insertBefore(start, new JumpInsnNode(Opcodes.GOTO, finishLabel));
				
				methodNode.instructions.insertBefore(start, failLabel);
			} else if (methodNode.name.equals("a") && methodNode.desc.equals("(ILjava/lang/String;IIII)V")) {
				// method hook for drawstringCenter, reserved testing
			}
		}
	}
	
	private void patchRandom(ClassNode node) {
		Logger.Info("Patching random (" + node.name + ".class)");
		
		Iterator<MethodNode> methodNodeList = node.methods.iterator();
		while (methodNodeList.hasNext()) {
			MethodNode methodNode = methodNodeList.next();
			
			if (methodNode.name.equals("a")) {
				// System.out.println(methodNode.desc);
				if (methodNode.desc.equals("(ILtb;)V")) {
					Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
					while (insnNodeList.hasNext()) {
						AbstractInsnNode insnNode = insnNodeList.next();
						AbstractInsnNode nextNode = insnNode.getNext();
						
						if (nextNode == null)
							break;
						
						if (insnNode.getOpcode() == Opcodes.ALOAD && nextNode.getOpcode() == Opcodes.ICONST_0) {
							VarInsnNode call = (VarInsnNode)insnNode;
							Logger.Info("Patching validation...");
							
							methodNode.instructions.insert(insnNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/Random", "nextBytes", "([B)V"));
							methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ALOAD, 2));
							methodNode.instructions.insert(insnNode, new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/Random", "<init>", "()V"));
							methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.DUP));
							methodNode.instructions.insert(insnNode, new TypeInsnNode(Opcodes.NEW, "java/util/Random"));
						}
					}
				}
			}
		}
	}
	
	private void patchGameApplet(ClassNode node) {
		Logger.Info("Patching GameApplet (" + node.name + ".class)");
		
		Iterator<MethodNode> methodNodeList = node.methods.iterator();
		while (methodNodeList.hasNext()) {
			MethodNode methodNode = methodNodeList.next();
			
			if (methodNode.name.equals("a")) {
				if (methodNode.desc.equals("(Ljava/net/URL;ZZ)[B")) {
					Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
					AbstractInsnNode insnNode = insnNodeList.next();
					methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
					methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/GameApplet", "cacheURLHook", "(Ljava/net/URL;)Ljava/net/URL;"));
					methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ASTORE, 0));
				} else if (methodNode.desc.equals("(Z)V")) {
					// Disconnect hook (::lostcon)
					Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
					AbstractInsnNode insnNode = insnNodeList.next();
					methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "disconnect_hook", "()V", false));
				} else if (methodNode.desc.equals("([BIII)V")) {
					// dump whole input stream!
					Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
					while (insnNodeList.hasNext()) {
						AbstractInsnNode insnNode = insnNodeList.next();
						AbstractInsnNode nextNode = insnNode.getNext();
						
						// entry point
						if (insnNode.getOpcode() == Opcodes.ILOAD && ((VarInsnNode)insnNode).var == 5 && nextNode.getOpcode() == Opcodes.ILOAD
								&& ((VarInsnNode)nextNode).var == 7) {
							VarInsnNode call = (VarInsnNode)insnNode;
							methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 1)); // byte[]
							methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 2)); // n
							methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 3)); // n2
							// methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 4)); // n3
							// methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 6)); // n4
							methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 5)); // n5
							methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 7)); // bytes read
							methodNode.instructions.insertBefore(call, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Replay", "dumpRawInputStream", "([BIIII)V"));
							break;
						}
					}
				}
			} else if (methodNode.name.equals("run")) {
				
				// dump whole output stream!
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					AbstractInsnNode nextNode = insnNode.getNext();
					
					// entry point
					if (insnNode.getOpcode() == Opcodes.ALOAD && ((VarInsnNode)insnNode).var == 0 && nextNode.getOpcode() == Opcodes.GETFIELD
							&& ((FieldInsnNode)nextNode).name.equals("Q") && ((FieldInsnNode)nextNode).desc.equals("Ljava/io/OutputStream;")) {
						methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
						methodNode.instructions.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETFIELD, "da", "Y", "[B"));
						methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 2));
						methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 1));
						methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Replay", "dumpRawOutputStream", "([BII)V"));
						break;
					}
				}
			}
		}
	}
	
	/**
	 * TODO: Complete JavaDoc
	 * 
	 * @param methodNode
	 * @param owner The class of the variable to be hooked
	 * @param var The variable to be hooked
	 * @param desc
	 * @param newClass The class the hooked variable will be stored in
	 * @param newVar The variable name the hooked variable will be stored in
	 * @param newDesc
	 * @param canRead Specifies if the hooked variable should be readable
	 * @param canWrite Specifies if the hooked variable should be writable
	 */
	private void hookClassVariable(MethodNode methodNode, String owner, String var, String desc, String newClass, String newVar, String newDesc, boolean canRead,
			boolean canWrite) {
		Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
		while (insnNodeList.hasNext()) {
			AbstractInsnNode insnNode = insnNodeList.next();
			
			int opcode = insnNode.getOpcode();
			if (opcode == Opcodes.GETFIELD || opcode == Opcodes.PUTFIELD) {
				FieldInsnNode field = (FieldInsnNode)insnNode;
				if (field.owner.equals(owner) && field.name.equals(var) && field.desc.equals(desc)) {
					if (opcode == Opcodes.GETFIELD && canWrite) {
						methodNode.instructions.insert(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, newClass, newVar, newDesc));
						methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.POP));
					} else if (opcode == Opcodes.PUTFIELD && canRead) {
						methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.DUP_X1));
						methodNode.instructions.insert(insnNode, new FieldInsnNode(Opcodes.PUTSTATIC, newClass, newVar, newDesc));
					}
				}
			}
		}
	}
	
	/**
	 * TODO: Complete JavaDoc
	 * 
	 * @param methodNode
	 * @param owner The class of the variable to be hooked
	 * @param var The variable to be hooked
	 * @param desc
	 * @param newClass The class the hooked variable will be stored in
	 * @param newVar The variable name the hooked variable will be stored in
	 * @param newDesc
	 */
	private void hookStaticVariable(MethodNode methodNode, String owner, String var, String desc, String newClass, String newVar, String newDesc) {
		Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
		while (insnNodeList.hasNext()) {
			AbstractInsnNode insnNode = insnNodeList.next();
			
			int opcode = insnNode.getOpcode();
			if (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC) {
				FieldInsnNode field = (FieldInsnNode)insnNode;
				if (field.owner.equals(owner) && field.name.equals(var) && field.desc.equals(desc)) {
					field.owner = newClass;
					field.name = newVar;
					field.desc = newDesc;
				}
			}
		}
	}
	
	private void dumpClass(ClassNode node) {
		BufferedWriter writer = null;
		
		try {
			File file = new File(Settings.Dir.DUMP + "/" + node.name + ".dump");
			writer = new BufferedWriter(new FileWriter(file));
			
			writer.write(decodeAccess(node.access) + node.name + " extends " + node.superName + ";\n");
			writer.write("\n");
			
			Iterator<FieldNode> fieldNodeList = node.fields.iterator();
			while (fieldNodeList.hasNext()) {
				FieldNode fieldNode = fieldNodeList.next();
				writer.write(decodeAccess(fieldNode.access) + fieldNode.desc + " " + fieldNode.name + ";\n");
			}
			
			writer.write("\n");
			
			Iterator<MethodNode> methodNodeList = node.methods.iterator();
			while (methodNodeList.hasNext()) {
				MethodNode methodNode = methodNodeList.next();
				writer.write(decodeAccess(methodNode.access) + methodNode.name + " " + methodNode.desc + ":\n");
				
				Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
				while (insnNodeList.hasNext()) {
					AbstractInsnNode insnNode = insnNodeList.next();
					String instruction = decodeInstruction(insnNode);
					writer.write(instruction);
				}
				writer.write("\n");
			}
			
			writer.close();
		} catch (Exception e) {
			try {
				writer.close();
			} catch (Exception e2) {
			}
		}
	}
	
	private String decodeAccess(int access) {
		String res = "";
		
		if ((access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC)
			res += "public ";
		if ((access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE)
			res += "private ";
		if ((access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED)
			res += "protected ";
		
		if ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC)
			res += "static ";
		if ((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL)
			res += "final ";
		if ((access & Opcodes.ACC_VOLATILE) == Opcodes.ACC_VOLATILE)
			res += "protected ";
		if ((access & Opcodes.ACC_SYNCHRONIZED) == Opcodes.ACC_SYNCHRONIZED)
			res += "synchronized ";
		if ((access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT)
			res += "abstract ";
		if ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE)
			res += "interface ";
		
		return res;
	}
	
	private String decodeInstruction(AbstractInsnNode insnNode) {
		insnNode.accept(mp);
		StringWriter sw = new StringWriter();
		printer.print(new PrintWriter(sw));
		printer.getText().clear();
		return sw.toString();
	}
	
	public static JClassPatcher getInstance() {
		if (instance == null) {
			synchronized (JClassPatcher.class) {
				instance = new JClassPatcher();
			}
		}
		return instance;
	}
	
}

/**
 * rscplus
 *
 * <p>This file is part of rscplus.
 *
 * <p>rscplus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>rscplus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with rscplus. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * <p>Authors: see <https://github.com/RSCPlus/rscplus>
 */
package Client;

import Client.Settings.Dir;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
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

/** Singleton class which hooks variables and patches classes. */
public class JClassPatcher {

  // Singleton
  private static JClassPatcher instance = null;

  public static List<String> ExceptionSignatures = new ArrayList<String>();
  public static List<String> InstructionBytecode = new ArrayList<String>();

  private Printer printer = new Textifier();
  private TraceMethodVisitor mp = new TraceMethodVisitor(printer);

  private JClassPatcher() {
    // Empty private constructor to prevent extra instances from being created.
  }

  public byte[] patch(byte[] data) {
    ClassReader reader = new ClassReader(data);
    ClassNode node = new ClassNode();
    reader.accept(node, ClassReader.SKIP_DEBUG);

    if (node.name.equals("ua")) patchRenderer(node);
    else if (node.name.equals("e")) patchApplet(node);
    else if (node.name.equals("qa")) patchMenu(node);
    else if (node.name.equals("m")) patchData(node);
    else if (node.name.equals("client")) patchClient(node);
    else if (node.name.equals("f")) patchRandom(node);
    else if (node.name.equals("da")) patchGameApplet(node);
    else if (node.name.equals("lb")) patchRendererHelper(node);
    else if (node.name.equals("sa")) patchSoundHelper(node);
    else if (node.name.equals("wb")) patchRightClickMenu(node);
    else if (node.name.equals("pb")) patchSoundPlayerJava(node);

    // Patch applied to all classes
    patchGeneric(node);

    if (Settings.DISASSEMBLE.get(Settings.currentProfile)) {
      Settings.Dir.DUMP = Dir.JAR + "/" + Settings.DISASSEMBLE_DIRECTORY.get("custom");
      Util.makeDirectory(Dir.DUMP);
      Logger.Info("Disassembling file: " + node.name + ".class");
      dumpClass(node);
    }

    // Dev Bytecode tracer, do not leave these uncommented in live builds!
    // if (node.name.equals("client")) patchTracer(node);
    // if (node.name.equals("lb")) patchTracer(node);

    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    node.accept(writer);
    return writer.toByteArray();
  }

  private void patchGeneric(ClassNode node) {
    Iterator<MethodNode> methodNodeList = node.methods.iterator();

    while (methodNodeList.hasNext()) {
      MethodNode methodNode = methodNodeList.next();

      // General byte patch
      Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
      while (insnNodeList.hasNext()) {
        AbstractInsnNode insnNode = insnNodeList.next();

        if (insnNode.getOpcode() == Opcodes.INVOKESTATIC) {
          MethodInsnNode call = (MethodInsnNode) insnNode;
          if (call.owner.equals("u") && call.name.equals("a") && call.desc.equals("(IJ)V")) {
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "shadowSleep", "(IJ)V"));
            methodNode.instructions.remove(insnNode);
          }
        }

        if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
          MethodInsnNode call = (MethodInsnNode) insnNode;

          // Patch calls to System.out.println and route them to Logger.Game
          if (call.owner.equals("java/io/PrintStream") && call.name.equals("println")) {
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Client/Logger", "Game", "(Ljava/lang/String;)V"));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.POP));
            methodNode.instructions.remove(insnNode);
          }
        }
        if (insnNode.getOpcode() == Opcodes.ATHROW) {
          int index = ExceptionSignatures.size();
          ExceptionSignatures.add(node.name + "." + methodNode.name + methodNode.desc);
          methodNode.instructions.insertBefore(insnNode, new IntInsnNode(Opcodes.SIPUSH, index));
          methodNode.instructions.insertBefore(
              insnNode,
              new MethodInsnNode(
                  Opcodes.INVOKESTATIC,
                  "Game/Client",
                  "HandleException",
                  "(Ljava/lang/Throwable;I)Ljava/lang/Throwable;"));
        }
      }

      hookClassVariable(
          methodNode,
          "ua",
          "fb",
          "Ljava/awt/image/ImageConsumer;",
          "Game/Renderer",
          "image_consumer",
          "Ljava/awt/image/ImageConsumer;",
          true,
          true);
      hookClassVariable(methodNode, "ua", "u", "I", "Game/Renderer", "width", "I", false, true);
      hookClassVariable(methodNode, "ua", "k", "I", "Game/Renderer", "height", "I", false, true);
      hookClassVariable(methodNode, "ua", "rb", "[I", "Game/Renderer", "pixels", "[I", true, true);

      hookClassVariable(
          methodNode,
          "e",
          "x",
          "Ljava/lang/String;",
          "Game/Client",
          "pm_enteredText",
          "Ljava/lang/String;",
          true,
          true);
      hookClassVariable(
          methodNode,
          "client",
          "x",
          "Ljava/lang/String;",
          "Game/Client",
          "pm_enteredText",
          "Ljava/lang/String;",
          true,
          true);
      hookClassVariable(
          methodNode,
          "e",
          "Ob",
          "Ljava/lang/String;",
          "Game/Client",
          "pm_text",
          "Ljava/lang/String;",
          true,
          true);
      hookClassVariable(
          methodNode,
          "client",
          "Ob",
          "Ljava/lang/String;",
          "Game/Client",
          "pm_text",
          "Ljava/lang/String;",
          true,
          true);

      hookClassVariable(
          methodNode,
          "e",
          "e",
          "Ljava/lang/String;",
          "Game/Client",
          "modal_enteredText",
          "Ljava/lang/String;",
          true,
          true);
      hookClassVariable(
          methodNode,
          "client",
          "e",
          "Ljava/lang/String;",
          "Game/Client",
          "modal_enteredText",
          "Ljava/lang/String;",
          true,
          true);
      hookClassVariable(
          methodNode,
          "e",
          "Cb",
          "Ljava/lang/String;",
          "Game/Client",
          "modal_text",
          "Ljava/lang/String;",
          true,
          true);
      hookClassVariable(
          methodNode,
          "client",
          "Cb",
          "Ljava/lang/String;",
          "Game/Client",
          "modal_text",
          "Ljava/lang/String;",
          true,
          true);

      hookClassVariable(
          methodNode,
          "client",
          "li",
          "Lba;",
          "Game/Renderer",
          "instance",
          "Ljava/lang/Object;",
          true,
          false);

      hookClassVariable(
          methodNode,
          "client",
          "Xb",
          "Ljava/awt/Graphics;",
          "Game/Renderer",
          "graphicsInstance",
          "Ljava/awt/Graphics;",
          true,
          false);

      hookClassVariable(methodNode, "ba", "u", "I", "Game/Renderer", "width", "I", false, true);
      hookClassVariable(methodNode, "ba", "k", "I", "Game/Renderer", "height", "I", false, true);
      hookClassVariable(methodNode, "ba", "rb", "[I", "Game/Renderer", "pixels", "[I", true, true);

      hookStaticVariable(methodNode, "ua", "Bb", "[I", "Game/Renderer", "itemSprites", "[I");
      hookStaticVariable(methodNode, "h", "c", "[I", "Game/Renderer", "itemSpriteMasks", "[I");

      hookStaticVariable(methodNode, "u", "e", "I", "Game/Client", "shadowSleepCount", "I");
      hookStaticVariable(methodNode, "n", "g", "I", "Game/Client", "friends_count", "I");
      hookStaticVariable(methodNode, "n", "a", "[I", "Game/GameApplet", "characterWidth", "[I");
      hookStaticVariable(
          methodNode,
          "ua",
          "h",
          "[Ljava/lang/String;",
          "Game/Client",
          "friends",
          "[Ljava/lang/String;");
      hookStaticVariable(
          methodNode,
          "ac",
          "z",
          "[Ljava/lang/String;",
          "Game/Client",
          "friends_world",
          "[Ljava/lang/String;");
      hookStaticVariable(
          methodNode,
          "cb",
          "c",
          "[Ljava/lang/String;",
          "Game/Client",
          "friends_formerly",
          "[Ljava/lang/String;");
      hookStaticVariable(methodNode, "client", "Fj", "[I", "Game/Client", "friends_online", "[I");

      hookStaticVariable(methodNode, "db", "g", "I", "Game/Client", "ignores_count", "I");
      hookStaticVariable(
          methodNode,
          "l",
          "c",
          "[Ljava/lang/String;",
          "Game/Client",
          "ignores",
          "[Ljava/lang/String;");
      hookStaticVariable(
          methodNode,
          "ia",
          "g",
          "[Ljava/lang/String;",
          "Game/Client",
          "ignores_formerly",
          "[Ljava/lang/String;");
      hookStaticVariable(
          methodNode,
          "ia",
          "a",
          "[Ljava/lang/String;",
          "Game/Client",
          "ignores_copy",
          "[Ljava/lang/String;");
      hookStaticVariable(
          methodNode,
          "ua",
          "wb",
          "[Ljava/lang/String;",
          "Game/Client",
          "ignores_formerly_copy",
          "[Ljava/lang/String;");

      hookClassVariable(
          methodNode, "client", "Wd", "I", "Game/Renderer", "width", "I", false, true);
      hookClassVariable(
          methodNode, "client", "Oi", "I", "Game/Renderer", "height_client", "I", false, true);
      hookClassVariable(
          methodNode, "client", "tg", "I", "Game/Renderer", "sprite_media", "I", true, false);

      hookClassVariable(methodNode, "e", "m", "I", "Game/Renderer", "width", "I", false, true);
      hookClassVariable(methodNode, "e", "a", "I", "Game/Renderer", "height", "I", false, true);
      hookClassVariable(
          methodNode, "e", "Ib", "I", "Game/Replay", "frame_time_slice", "I", true, true);
      hookClassVariable(
          methodNode, "client", "fc", "I", "Game/Replay", "connection_port", "I", true, true);
      hookClassVariable(
          methodNode,
          "client",
          "Dh",
          "Ljava/lang/String;",
          "Game/Client",
          "server_address",
          "Ljava/lang/String;",
          true,
          true);
      hookClassVariable(
          methodNode, "client", "xd", "I", "Game/Client", "serverjag_port", "I", true, true);

      hookClassVariable(methodNode, "lb", "pb", "[I", "Game/Renderer", "pixels", "[I", true, true);

      hookClassVariable(
          methodNode, "lb", "Kb", "I", "Game/Camera", "pitch_internal", "I", true, true);

      hookStaticVariable(
          methodNode,
          "client",
          "il",
          "[Ljava/lang/String;",
          "Game/Client",
          "strings",
          "[Ljava/lang/String;");

      hookStaticVariable(
          methodNode,
          "ac",
          "x",
          "[Ljava/lang/String;",
          "Game/Item",
          "item_name",
          "[Ljava/lang/String;");
      hookStaticVariable(
          methodNode,
          "lb",
          "ac",
          "[Ljava/lang/String;",
          "Game/Item",
          "item_commands",
          "[Ljava/lang/String;");
      hookStaticVariable(methodNode, "kb", "b", "[I", "Game/Item", "item_price", "[I");
      hookStaticVariable(methodNode, "fa", "e", "[I", "Game/Item", "item_stackable", "[I");
      hookStaticVariable(methodNode, "ka", "c", "[I", "Game/Item", "item_members", "[I");

      hookConditionalClassVariable(
          methodNode,
          "lb",
          "Mb",
          "I",
          "Game/Camera",
          "distance1",
          "I",
          false,
          true,
          "VIEW_DISTANCE_BOOL");
      hookConditionalClassVariable(
          methodNode,
          "lb",
          "X",
          "I",
          "Game/Camera",
          "distance2",
          "I",
          false,
          true,
          "VIEW_DISTANCE_BOOL");
      hookConditionalClassVariable(
          methodNode,
          "lb",
          "P",
          "I",
          "Game/Camera",
          "distance3",
          "I",
          false,
          true,
          "VIEW_DISTANCE_BOOL");
      hookConditionalClassVariable(
          methodNode,
          "lb",
          "G",
          "I",
          "Game/Camera",
          "distance4",
          "I",
          false,
          true,
          "VIEW_DISTANCE_BOOL");

      hookClassVariable(
          methodNode, "client", "cl", "I", "Game/Client", "max_inventory", "I", true, false);
      hookClassVariable(
          methodNode, "client", "bk", "[Z", "Game/Client", "prayers_on", "[Z", true, false);
      hookClassVariable(
          methodNode,
          "client",
          "Fc",
          "[I",
          "Game/Client",
          "current_equipment_stats",
          "[I",
          true,
          false);
      hookClassVariable(
          methodNode, "client", "oh", "[I", "Game/Client", "current_level", "[I", true, false);
      hookClassVariable(
          methodNode, "client", "cg", "[I", "Game/Client", "base_level", "[I", true, false);
      hookClassVariable(
          methodNode,
          "client",
          "Vk",
          "[Ljava/lang/String;",
          "Game/Client",
          "skill_name",
          "[Ljava/lang/String;",
          true,
          false);
      hookClassVariable(methodNode, "client", "Ak", "[I", "Game/Client", "xp", "[I", true, false);
      hookClassVariable(
          methodNode, "client", "vg", "I", "Game/Client", "fatigue", "I", true, false);
      hookClassVariable(
          methodNode, "client", "Fg", "I", "Game/Client", "combat_style", "I", true, true);
      hookClassVariable(
          methodNode, "client", "Xd", "I", "Game/Client", "login_screen", "I", true, true);

      hookClassVariable(
          methodNode,
          "client",
          "Ek",
          "Llb;",
          "Game/Camera",
          "instance",
          "Ljava/lang/Object;",
          true,
          false);
      hookConditionalClassVariable(
          methodNode, "client", "qd", "I", "Game/Camera", "fov", "I", false, true, "FOV_BOOL");

      hookClassVariable(
          methodNode, "client", "ai", "I", "Game/Client", "combat_timer", "I", true, true);
      hookClassVariable(
          methodNode, "client", "Fe", "Z", "Game/Client", "show_bank", "Z", true, false);
      hookClassVariable(
          methodNode, "client", "dd", "Z", "Game/Client", "show_duel", "Z", true, false);
      hookClassVariable(
          methodNode, "client", "Pj", "Z", "Game/Client", "show_duelconfirm", "Z", true, false);
      hookClassVariable(
          methodNode, "client", "Bj", "I", "Game/Client", "show_friends", "I", true, true);
      hookClassVariable(
          methodNode, "client", "qc", "I", "Game/Client", "show_menu", "I", true, false);
      hookClassVariable(
          methodNode, "client", "zd", "I", "Game/Client", "show_stats_or_quests", "I", true, false);
      hookClassVariable(
          methodNode, "client", "Ph", "Z", "Game/Client", "show_questionmenu", "Z", true, false);
      hookClassVariable(
          methodNode, "client", "Vf", "I", "Game/Client", "show_report", "I", true, false);
      hookClassVariable(
          methodNode, "client", "uk", "Z", "Game/Client", "show_shop", "Z", true, false);
      hookClassVariable(
          methodNode, "client", "Qk", "Z", "Game/Client", "show_sleeping", "Z", true, false);
      hookClassVariable(
          methodNode, "client", "Hk", "Z", "Game/Client", "show_trade", "Z", true, false);
      hookClassVariable(
          methodNode, "client", "Xj", "Z", "Game/Client", "show_tradeconfirm", "Z", true, false);
      hookClassVariable(
          methodNode, "client", "Oh", "Z", "Game/Client", "show_welcome", "Z", true, true);
      hookClassVariable(
          methodNode, "client", "Kg", "Z", "Game/Client", "show_appearance", "Z", true, true);
      hookClassVariable(
          methodNode, "client", "ne", "Z", "Game/SoundEffects", "sounds_disabled", "Z", true, true);
      hookClassVariable(
          methodNode,
          "pb",
          "w",
          "Ljavax/sound/sampled/SourceDataLine;",
          "Game/SoundEffects",
          "mudClientSourceDataLine",
          "Ljavax/sound/sampled/SourceDataLine;",
          true,
          true);

      hookClassVariable(
          methodNode,
          "client",
          "Qd",
          "Ljava/lang/String;",
          "Game/Client",
          "pm_username",
          "Ljava/lang/String;",
          true,
          true);

      hookClassVariable(
          methodNode,
          "client",
          "wh",
          "Ljava/lang/String;",
          "Game/Client",
          "username_login",
          "Ljava/lang/String;",
          true,
          true);
      hookClassVariable(
          methodNode,
          "client",
          "wh",
          "Ljava/lang/String;",
          "Game/Client",
          "password_login",
          "Ljava/lang/String;",
          true,
          true);
      hookClassVariable(
          methodNode, "client", "Vh", "I", "Game/Client", "autologin_timeout", "I", true, true);

      hookClassVariable(
          methodNode, "client", "eh", "I", "Game/Client", "objectCount", "I", true, true);
      hookClassVariable(
          methodNode, "client", "bg", "[I", "Game/Client", "objectDirections", "[I", true, true);
      hookClassVariable(
          methodNode, "client", "Se", "[I", "Game/Client", "objectX", "[I", true, true);
      hookClassVariable(
          methodNode, "client", "ye", "[I", "Game/Client", "objectY", "[I", true, true);
      hookClassVariable(
          methodNode, "client", "vc", "[I", "Game/Client", "objectID", "[I", true, true);

      hookClassVariable(
          methodNode, "client", "lc", "I", "Game/Client", "inventory_count", "I", true, false);
      hookClassVariable(
          methodNode, "client", "vf", "[I", "Game/Client", "inventory_items", "[I", true, false);
      hookConditionalClassVariable(
          methodNode,
          "client",
          "kg",
          "I",
          "Game/Camera",
          "lookat_x",
          "I",
          false,
          true,
          "CAMERA_MOVABLE_BOOL");
      hookConditionalClassVariable(
          methodNode,
          "client",
          "Si",
          "I",
          "Game/Camera",
          "lookat_y",
          "I",
          false,
          true,
          "CAMERA_MOVABLE_BOOL");
      hookClassVariable(
          methodNode, "client", "Wc", "I", "Game/Camera", "auto_speed", "I", true, true);
      hookClassVariable(
          methodNode, "client", "Be", "I", "Game/Camera", "rotation_y", "I", true, true);
      hookClassVariable(methodNode, "client", "Kh", "Z", "Game/Camera", "auto", "Z", true, true);
      hookClassVariable(methodNode, "client", "si", "I", "Game/Camera", "angle", "I", true, true);

      hookConditionalClassVariable(
          methodNode,
          "client",
          "ug",
          "I",
          "Game/Camera",
          "rotation",
          "I",
          false,
          true,
          "CAMERA_ROTATABLE_BOOL");
      hookConditionalClassVariable(
          methodNode,
          "client",
          "ac",
          "I",
          "Game/Camera",
          "zoom",
          "I",
          false,
          true,
          "CAMERA_ZOOMABLE_BOOL");

      // Chat menu
      hookClassVariable(
          methodNode,
          "client",
          "yd",
          "Lqa;",
          "Game/Menu",
          "chat_menu",
          "Ljava/lang/Object;",
          true,
          false);
      hookClassVariable( // Selected tab
          methodNode, "client", "Zh", "I", "Game/Menu", "chat_selected", "I", true, false);
      hookClassVariable( // Chat history
          methodNode, "client", "Fh", "I", "Game/Menu", "chat_type1", "I", true, false);
      hookClassVariable( // All messages
          methodNode, "client", "bh", "I", "Game/Menu", "chat_input", "I", true, false);
      hookClassVariable( // Quest history
          methodNode, "client", "ud", "I", "Game/Menu", "chat_type2", "I", true, false);
      hookClassVariable( // Private history
          methodNode, "client", "mc", "I", "Game/Menu", "chat_type3", "I", true, false);

      // Quest menu
      hookClassVariable(
          methodNode,
          "client",
          "fe",
          "Lqa;",
          "Game/Menu",
          "quest_menu",
          "Ljava/lang/Object;",
          true,
          false);
      hookClassVariable(
          methodNode, "client", "lk", "I", "Game/Menu", "quest_handle", "I", true, false);

      // Friends menu
      hookClassVariable(
          methodNode,
          "client",
          "zk",
          "Lqa;",
          "Game/Menu",
          "friend_menu",
          "Ljava/lang/Object;",
          true,
          false);
      hookClassVariable(
          methodNode, "client", "Hi", "I", "Game/Menu", "friend_handle", "I", true, false);

      // Spell menu
      hookClassVariable(
          methodNode,
          "client",
          "Mc",
          "Lqa;",
          "Game/Menu",
          "spell_menu",
          "Ljava/lang/Object;",
          true,
          false);
      hookClassVariable(
          methodNode, "client", "Ud", "I", "Game/Menu", "spell_handle", "I", true, false);

      // Player name
      hookClassVariable(
          methodNode,
          "client",
          "wi",
          "Lta;",
          "Game/Client",
          "player_object",
          "Ljava/lang/Object;",
          true,
          false);
      // coordinates related
      hookClassVariable(
          methodNode, "client", "Qg", "I", "Game/Client", "regionX", "I", true, false);
      hookClassVariable(
          methodNode, "client", "zg", "I", "Game/Client", "regionY", "I", true, false);
      hookClassVariable(
          methodNode, "client", "Lf", "I", "Game/Client", "localRegionX", "I", true, false);
      hookClassVariable(
          methodNode, "client", "sh", "I", "Game/Client", "localRegionY", "I", true, false);
      hookClassVariable(
          methodNode, "client", "Ki", "I", "Game/Client", "planeWidth", "I", true, false);
      hookClassVariable(
          methodNode, "client", "sk", "I", "Game/Client", "planeHeight", "I", true, false);
      hookClassVariable(
          methodNode, "client", "bc", "I", "Game/Client", "planeIndex", "I", true, false);
      hookClassVariable(
          methodNode, "client", "Ub", "Z", "Game/Client", "loadingArea", "Z", true, false);

      hookClassVariable(
          methodNode, "client", "Ug", "I", "Game/Client", "tileSize", "I", true, false);

      // Last mouse activity
      // hookClassVariable(methodNode, "client", "sb", "I", "Game/Client", "lastMouseAction", "I",
      // true, true);

      // Client version
      hookStaticVariable(methodNode, "fa", "d", "I", "Game/Client", "version", "I");

      // Client modulus and exponent
      hookStaticVariable(
          methodNode,
          "s",
          "c",
          "Ljava/math/BigInteger;",
          "Game/Client",
          "exponent",
          "Ljava/math/BigInteger;");
      hookStaticVariable(
          methodNode,
          "ja",
          "K",
          "Ljava/math/BigInteger;",
          "Game/Client",
          "modulus",
          "Ljava/math/BigInteger;");

      // Max retries for stream
      hookStaticVariable(methodNode, "d", "l", "I", "Game/Client", "maxRetries", "I");

      // Shell strings
      hookStaticVariable(
          methodNode,
          "e",
          "Sb",
          "[Ljava/lang/String;",
          "Game/Renderer",
          "shellStrings",
          "[Ljava/lang/String;");

      // game font size
      hookStaticVariable(methodNode, "b", "c", "I", "Game/GameApplet", "gameFontSize", "I");

      // game fonts
      hookStaticVariable(methodNode, "m", "b", "[[B", "Game/GameApplet", "gameFonts", "[[B");

      // game font states
      hookStaticVariable(methodNode, "fb", "k", "[Z", "Game/GameApplet", "gameFontStates", "[Z");

      // game font data
      hookStaticVariable(methodNode, "qb", "k", "[B", "Game/GameApplet", "gameFontData", "[B");

      hookClassVariable(
          methodNode,
          "client",
          "Jh",
          "Lda;",
          "Game/Client",
          "clientStream",
          "Ljava/lang/Object;",
          true,
          false);

      hookClassVariable(
          methodNode,
          "client",
          "mg",
          "Lja;",
          "Game/Client",
          "packetsIncoming",
          "Ljava/lang/Object;",
          true,
          false);

      // Bank related vars
      hookClassVariable(
          methodNode, "client", "ci", "[I", "Game/Client", "new_bank_items", "[I", true, true);
      hookClassVariable(
          methodNode,
          "client",
          "Xe",
          "[I",
          "Game/Client",
          "new_bank_items_count",
          "[I",
          true,
          true);
      hookClassVariable(
          methodNode, "client", "ae", "[I", "Game/Client", "bank_items", "[I", true, true);
      hookClassVariable(
          methodNode, "client", "di", "[I", "Game/Client", "bank_items_count", "[I", true, true);
      hookClassVariable(
          methodNode, "client", "fj", "I", "Game/Client", "new_count_items_bank", "I", true, true);
      hookClassVariable(
          methodNode, "client", "Gi", "I", "Game/Client", "bank_items_max", "I", true, true);
      hookClassVariable(
          methodNode, "client", "vj", "I", "Game/Client", "count_items_bank", "I", true, true);
      hookClassVariable(
          methodNode, "client", "xg", "I", "Game/Client", "bank_active_page", "I", true, true);

      hookClassVariable(
          methodNode, "client", "sj", "I", "Game/Client", "selectedItem", "I", true, false);
      hookClassVariable(
          methodNode, "client", "Rd", "I", "Game/Client", "selectedItemSlot", "I", true, false);

      //// Panel UI Manipulation
      hookClassVariable(
          methodNode,
          "client",
          "ge",
          "Lqa;",
          "Game/Client",
          "panelWelcome",
          "Ljava/lang/Object;",
          true,
          false);

      hookClassVariable(
          methodNode,
          "client",
          "yi",
          "Lqa;",
          "Game/Client",
          "panelLogin",
          "Ljava/lang/Object;",
          true,
          false);

      hookClassVariable(
          methodNode, "client", "ng", "I", "Game/Client", "loginUserInput", "I", true, false);

      hookClassVariable(
          methodNode, "client", "Ih", "I", "Game/Client", "loginPassInput", "I", true, false);

      hookClassVariable(
          methodNode, "client", "Zb", "I", "Game/Client", "login_delay", "I", true, false);

      hookClassVariable(
          methodNode, "client", "Kg", "Z", "Game/Client", "showAppearanceChange", "Z", true, false);

      hookClassVariable(
          methodNode, "client", "Qi", "I", "Game/Client", "controlLoginTop", "I", true, false);

      hookClassVariable(
          methodNode, "client", "td", "I", "Game/Client", "controlLoginBottom", "I", true, false);

      hookClassVariable(
          methodNode, "client", "Sb", "I", "Game/Client", "recoveryChangeDays", "I", true, false);

      hookClassVariable(
          methodNode, "client", "Cf", "I", "Game/Client", "mouse_click", "I", true, true);

      hookClassVariable(
          methodNode, "client", "Yh", "Z", "Game/Client", "singleButtonMode", "Z", true, true);

      hookClassVariable(methodNode, "client", "Pg", "Z", "Game/Client", "members", "Z", true, true);

      hookClassVariable(
          methodNode, "client", "cf", "Z", "Game/Client", "veterans", "Z", true, true);

      hookClassVariable(
          methodNode,
          "client",
          "Hh",
          "Lk;",
          "Game/Client",
          "worldInstance",
          "Ljava/lang/Object;",
          true,
          false);

      hookClassVariable(
          methodNode, "client", "yj", "I", "Game/Client", "lastHeightOffset", "I", true, true);

      // Game data hooks
      hookStaticVariableClone(
          methodNode,
          "l",
          "a",
          "[Ljava/lang/String;",
          "Game/JGameData",
          "sceneryNames",
          "[Ljava/lang/String;");

      // current ground items
      hookClassVariable(
          methodNode, "client", "Zf", "[I", "Game/Item", "groundItemX", "[I", true, false);
      hookClassVariable(
          methodNode, "client", "Ni", "[I", "Game/Item", "groundItemY", "[I", true, false);
      hookClassVariable(
          methodNode, "client", "Le", "[I", "Game/Item", "groundItemZ", "[I", true, false);
      hookClassVariable(
          methodNode, "client", "Gj", "[I", "Game/Item", "groundItemId", "[I", true, false);
      hookClassVariable(
          methodNode, "client", "Ah", "I", "Game/Item", "groundItemCount", "I", true, false);

      // Showing right-click menu hook
      hookClassVariable(
          methodNode,
          "client",
          "se",
          "Z",
          "Game/Renderer",
          "showingRightClickMenu",
          "Z",
          true,
          false);
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
        methodNode.instructions.insertBefore(
            first,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC, "Game/Menu", "switchList", "(Ljava/lang/Object;)Z"));
        methodNode.instructions.insertBefore(first, new JumpInsnNode(Opcodes.IFGT, label));
        methodNode.instructions.insertBefore(first, new InsnNode(Opcodes.RETURN));
        methodNode.instructions.insertBefore(first, label);
      }
      // drawPanel
      if (methodNode.name.equals("a") && methodNode.desc.equals("(B)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.BIPUSH
              && ((IntInsnNode) insnNode).operand == 14
              && nextNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) nextNode).var == 0) {
            methodNode.instructions.insertBefore(
                insnNode,
                new FieldInsnNode(
                    Opcodes.GETSTATIC, "Game/Client", "panelRegister", "Ljava/lang/Object;"));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 2));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "qa", "U", "[I"));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 2));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.IALOAD));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Panel",
                    "draw_extra_hook",
                    "(Ljava/lang/Object;II)V",
                    false));
          }
        }
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
        methodNode.instructions.insertBefore(
            lastNode,
            new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Item", "patchItemNames", "()V", false));
        methodNode.instructions.insertBefore(
            lastNode,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC, "Game/Item", "patchItemCommands", "()V", false));
        methodNode.instructions.insertBefore(
            lastNode,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC, "Client/WorldMapWindow", "initScenery", "()V", false));
        methodNode.instructions.insertBefore(
            lastNode,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC, "Client/WorldMapWindow", "initBoundaries", "()V", false));
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
        for (; ; ) {
          AbstractInsnNode next = findNode.getNext();

          if (next == null) break;

          if (findNode.getOpcode() == Opcodes.ALOAD && next.getOpcode() == Opcodes.ALOAD) {
            AbstractInsnNode invokeNode = next.getNext();
            MethodInsnNode invoke = (MethodInsnNode) invokeNode;
            methodNode.instructions.remove(next);
            methodNode.instructions.remove(invokeNode);
            if (invoke.name.equals("addMouseListener"))
              methodNode.instructions.insert(
                  findNode,
                  new FieldInsnNode(
                      Opcodes.PUTSTATIC,
                      "Game/MouseHandler",
                      "listener_mouse",
                      "Ljava/awt/event/MouseListener;"));
            else if (invoke.name.equals("addMouseMotionListener"))
              methodNode.instructions.insert(
                  findNode,
                  new FieldInsnNode(
                      Opcodes.PUTSTATIC,
                      "Game/MouseHandler",
                      "listener_mouse_motion",
                      "Ljava/awt/event/MouseMotionListener;"));
            else if (invoke.name.equals("addKeyListener"))
              methodNode.instructions.insert(
                  findNode,
                  new FieldInsnNode(
                      Opcodes.PUTSTATIC,
                      "Game/KeyboardHandler",
                      "listener_key",
                      "Ljava/awt/event/KeyListener;"));
          }
          findNode = findNode.getNext();
        }

        // Throwable crash patch
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.INVOKESTATIC
              && nextNode.getOpcode() == Opcodes.ATHROW) {
            MethodInsnNode call = (MethodInsnNode) insnNode;
            methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.RETURN));
          }
        }

        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          // entry point
          if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
              && ((MethodInsnNode) insnNode).name.equals("b")) {
            methodNode.instructions.insertBefore(nextNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(nextNode, new IntInsnNode(Opcodes.SIPUSH, 1000));
            methodNode.instructions.insertBefore(nextNode, new VarInsnNode(Opcodes.ILOAD, 4));
            methodNode.instructions.insertBefore(nextNode, new InsnNode(Opcodes.IMUL));
            methodNode.instructions.insertBefore(nextNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                nextNode, new FieldInsnNode(Opcodes.GETFIELD, "e", "Ib", "I"));
            methodNode.instructions.insertBefore(nextNode, new IntInsnNode(Opcodes.SIPUSH, 256));
            methodNode.instructions.insertBefore(nextNode, new InsnNode(Opcodes.IMUL));
            // added to prevent seeks div by 0
            methodNode.instructions.insertBefore(nextNode, new InsnNode(Opcodes.ICONST_1));
            methodNode.instructions.insertBefore(
                nextNode,
                new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Math", "max", "(II)I", false));
            methodNode.instructions.insertBefore(nextNode, new InsnNode(Opcodes.IDIV));
            methodNode.instructions.insertBefore(
                nextNode, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "fps", "I"));
            methodNode.instructions.insertBefore(nextNode, new InsnNode(Opcodes.POP));
            break;
          }
        }
      }

      // draw loading screen
      if (methodNode.name.equals("a") && methodNode.desc.equals("(Ljava/lang/String;II)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
              && ((MethodInsnNode) insnNode).name.equals("setColor")) {

            insnNode = insnNode.getPrevious().getPrevious().getPrevious();

            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));

            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Client/ScaledWindow",
                    "hookLoadingGraphics",
                    "()Ljava/awt/Graphics;",
                    false));

            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.PUTFIELD, "e", "u", "Ljava/awt/Graphics;"));

            break;
          }
        }
      }

      if (methodNode.name.equals("a") && methodNode.desc.equals("(IB)V")) {
        // FPS hook
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.GETSTATIC) {
            FieldInsnNode call = (FieldInsnNode) insnNode;
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Replay", "getFPS", "()I", false));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ISTORE, 1));
          }
        }
      }
      if (methodNode.name.equals("a") && methodNode.desc.equals("(Ljava/lang/String;Z)V")) {
        // this part shows error_game_
        // we want to call disconnect hook for instance in error_game_crash
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
              && ((MethodInsnNode) insnNode).name.equals("println")) {
            LabelNode call = (LabelNode) insnNode.getNext();
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 1));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "error_game_hook",
                    "(Ljava/lang/String;)V",
                    false));
          }
        }
      }
      if ((Settings.javaVersion >= 9 || Settings.javaVersion == -1)
          && methodNode.name.equals("isDisplayable")
          && methodNode.desc.equals("()Z")) {
        Logger.Warn(
            "Applying Java 9+ compatibility fix in "
                + node.name
                + "."
                + methodNode.name
                + methodNode.desc);
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        AbstractInsnNode insnNode = insnNodeList.next().getNext();

        // To fix java 9+, just forward the call to super.isDisplayable()
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode,
            new MethodInsnNode(
                Opcodes.INVOKESPECIAL, "java/applet/Applet", "isDisplayable", "()Z", false));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.IRETURN));
      }
      if ((Settings.javaVersion >= 9 || Settings.javaVersion == -1)
          && (methodNode.name.equals("mousePressed") || methodNode.name.equals("mouseDragged"))
          && methodNode.desc.equals("(Ljava/awt/event/MouseEvent;)V")) {
        Logger.Warn(
            "Applying Java 9+ compatibility fix in "
                + node.name
                + "."
                + methodNode.name
                + methodNode.desc);
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
              && ((MethodInsnNode) insnNode).name.equals("isMetaDown")) {
            // Use SwingUtilities to determine if click is right click to support java 9+ right
            // clicking
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "javax/swing/SwingUtilities",
                    "isRightMouseButton",
                    "(Ljava/awt/event/MouseEvent;)Z",
                    false));
            methodNode.instructions.remove(insnNode);
          }
        }
      }

      // Patch font loading
      if (methodNode.name.equals("b") && methodNode.desc.equals("(B)Z")) {
        Logger.Info("Patching font loading...");

        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        LabelNode label = new LabelNode();
        LabelNode skipLabel = new LabelNode();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.PUTFIELD
              && ((FieldInsnNode) insnNode).name.equals("C")) {
            methodNode.instructions.insert(insnNode, new JumpInsnNode(Opcodes.IFGT, skipLabel));
            methodNode.instructions.insert(
                insnNode,
                new FieldInsnNode(
                    Opcodes.GETSTATIC, "Client/Settings", "USE_JAGEX_FONTS_BOOL", "Z"));
            methodNode.instructions.insert(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Client/Settings",
                    "updateInjectedVariables",
                    "()V",
                    false));
          }

          if (insnNode.getOpcode() == Opcodes.ICONST_1
              && nextNode.getNext().getOpcode() == Opcodes.IRETURN) {
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Client/Settings",
                    "updateInjectedVariables",
                    "()V",
                    false));
            methodNode.instructions.insertBefore(
                insnNode,
                new FieldInsnNode(
                    Opcodes.GETSTATIC, "Client/Settings", "USE_JAGEX_FONTS_BOOL", "Z"));
            methodNode.instructions.insertBefore(insnNode, new JumpInsnNode(Opcodes.IFEQ, label));
            methodNode.instructions.insertBefore(insnNode, skipLabel);
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/GameApplet", "loadJfFonts", "()V", false));
            methodNode.instructions.insertBefore(insnNode, label);
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

      if (methodNode.name.equals("f") && methodNode.desc.equals("(I)V")) {
        AbstractInsnNode start = methodNode.instructions.getFirst();

        // Scale "ZZZ" sleep effect rendering to client size

        // Scale first ZZZ box height
        while (start != null) {
          if (start.getOpcode() == Opcodes.LDC) {
            LdcInsnNode ldcNode = (LdcInsnNode) start;

            if (ldcNode.cst instanceof Double && (double) ldcNode.cst == 334.0) {
              methodNode.instructions.insertBefore(
                  start,
                  new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height_client", "I"));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.I2D));

              methodNode.instructions.remove(start);

              break;
            }
          }

          start = start.getNext();
        }

        start = methodNode.instructions.getFirst();

        // Scale first ZZZ box width
        while (start != null) {
          if (start.getOpcode() == Opcodes.LDC) {
            LdcInsnNode ldcNode = (LdcInsnNode) start;

            if (ldcNode.cst instanceof Double && (double) ldcNode.cst == 80.0) {
              methodNode.instructions.insertBefore(
                  start, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.I2D));
              methodNode.instructions.insertBefore(start, new LdcInsnNode(0.5));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.DMUL));
              methodNode.instructions.insertBefore(start, new LdcInsnNode(176.0));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.DSUB));

              methodNode.instructions.remove(start);

              break;
            }
          }

          start = start.getNext();
        }

        start = methodNode.instructions.getFirst();

        // Scale second ZZZ box height
        while (start != null) {
          if (start.getOpcode() == Opcodes.LDC) {
            LdcInsnNode ldcNode = (LdcInsnNode) start;

            if (ldcNode.cst instanceof Double && (double) ldcNode.cst == 334.0) {
              methodNode.instructions.insertBefore(
                  start,
                  new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height_client", "I"));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.I2D));

              methodNode.instructions.remove(start);

              break;
            }
          }

          start = start.getNext();
        }

        start = methodNode.instructions.getFirst();

        // Scale second ZZZ box width
        while (start != null) {
          if (start.getOpcode() == Opcodes.LDC) {
            LdcInsnNode ldcNode = (LdcInsnNode) start;
            if (ldcNode.cst instanceof Double && (double) ldcNode.cst == 80.0) {
              methodNode.instructions.insertBefore(
                  start, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.I2D));
              methodNode.instructions.insertBefore(start, new LdcInsnNode(0.5));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.DMUL));
              methodNode.instructions.insertBefore(start, new LdcInsnNode(176.0));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.DSUB));

              methodNode.instructions.remove(start);

              break;
            }
          }

          start = start.getNext();
        }

        start = methodNode.instructions.getFirst();

        // Adjust second ZZZ box width calculation
        while (start != null) {
          if (start.getOpcode() == Opcodes.SIPUSH) {
            IntInsnNode call = (IntInsnNode) start;

            if (call.operand == 512) {
              methodNode.instructions.insertBefore(
                  start, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));

              methodNode.instructions.remove(start);

              break;
            }
          }

          start = start.getNext();
        }

        start = methodNode.instructions.getFirst();

        // Adjust frequency of first ZZZ draw to approximate scale
        while (start != null) {
          if (start.getOpcode() == Opcodes.LDC) {
            LdcInsnNode ldcNode = (LdcInsnNode) start;

            if (ldcNode.cst instanceof Double && (double) ldcNode.cst == 0.15) {
              methodNode.instructions.insertBefore(
                  start, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.I2D));
              methodNode.instructions.insertBefore(
                  start,
                  new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height_client", "I"));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.I2D));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.DADD));
              methodNode.instructions.insertBefore(start, new LdcInsnNode(5640.0));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.DDIV));

              methodNode.instructions.remove(start);

              break;
            }
          }

          start = start.getNext();
        }

        start = methodNode.instructions.getFirst();

        // Adjust frequency of second ZZZ draw to approximate scale
        while (start != null) {
          if (start.getOpcode() == Opcodes.LDC) {
            LdcInsnNode ldcNode = (LdcInsnNode) start;

            if (ldcNode.cst instanceof Double && (double) ldcNode.cst == 0.15) {
              methodNode.instructions.insertBefore(
                  start, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.I2D));
              methodNode.instructions.insertBefore(
                  start,
                  new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height_client", "I"));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.I2D));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.DADD));
              methodNode.instructions.insertBefore(start, new LdcInsnNode(5640.0));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.DDIV));

              methodNode.instructions.remove(start);

              break;
            }
          }

          start = start.getNext();
        }

        start = methodNode.instructions.getFirst();

        // Makes the underground flickering toggleable
        while (start != null) {
          if (start.getOpcode() == Opcodes.LDC) {
            LdcInsnNode ldcNode = (LdcInsnNode) start;
            if (ldcNode.cst instanceof Double && (double) ldcNode.cst == 3.0) {
              JumpInsnNode insnNode = (JumpInsnNode) ldcNode.getPrevious().getPrevious();

              methodNode.instructions.insert(
                  insnNode, new JumpInsnNode(Opcodes.IFGT, insnNode.label));

              methodNode.instructions.insert(
                  insnNode,
                  new FieldInsnNode(
                      Opcodes.GETSTATIC,
                      "Client/Settings",
                      "DISABLE_UNDERGROUND_LIGHTING_BOOL",
                      "Z"));

              methodNode.instructions.insert(
                  insnNode,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC,
                      "Client/Settings",
                      "updateInjectedVariables",
                      "()V",
                      false));

              break;
            }
          }

          start = start.getNext();
        }
      }

      // This fixes the rendering bug that happens during end of days replays.
      // It resizes a few arrays with messages, teleport, and action bubbles to allow more data
      Iterator<AbstractInsnNode> insnNodeList2 = methodNode.instructions.iterator();
      while (insnNodeList2.hasNext()) {
        AbstractInsnNode insnNode = insnNodeList2.next();
        AbstractInsnNode nextNode = insnNode.getNext();
        if ((insnNode.getOpcode() == Opcodes.BIPUSH || insnNode.getOpcode() == Opcodes.SIPUSH)
            && (nextNode.getOpcode() == Opcodes.NEWARRAY
                || nextNode.getOpcode() == Opcodes.ANEWARRAY)) {
          IntInsnNode sizeNode = (IntInsnNode) insnNode;
          if (sizeNode.operand == 50) {
            methodNode.instructions.insertBefore(nextNode, new IntInsnNode(Opcodes.SIPUSH, 200));
            methodNode.instructions.remove(insnNode);
          }
        }
      }

      // URL check removal at launch
      if (methodNode.name.equals("a") && methodNode.desc.equals("(B)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          if (insnNode.getOpcode() == Opcodes.IFEQ) {
            JumpInsnNode jmpNode = (JumpInsnNode) insnNode;
            JumpInsnNode gotoNode = new JumpInsnNode(Opcodes.GOTO, jmpNode.label);
            methodNode.instructions.insert(insnNode, gotoNode);
            methodNode.instructions.remove(jmpNode.getPrevious());
            methodNode.instructions.remove(jmpNode.getPrevious());
            methodNode.instructions.remove(jmpNode);
            break;
          }
        }

        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          if (insnNode.getOpcode() == Opcodes.INVOKESPECIAL
              && ((MethodInsnNode) insnNode).name.equals("t")
              && ((MethodInsnNode) insnNode).desc.equals("(I)V")) {
            // draw any extra panels when starting game
            VarInsnNode call = (VarInsnNode) insnNode.getNext();
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "initCreateExtraPanelsHook",
                    "()V",
                    false));
            break;
          }
        }
      }
      // handlePacket
      if (methodNode.name.equals("a") && methodNode.desc.equals("(III)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        AbstractInsnNode insnNode = insnNodeList.next();
        methodNode.instructions.insertBefore(
            insnNode,
            new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Replay", "patchClient", "()V", false));
        // save encrypted opcodes (used when decrypted opcode = 182)
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 3));
        methodNode.instructions.insertBefore(
            insnNode,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC, "Game/Replay", "saveEncOpcode", "(I)V", false));

        // fetch opcode for packets
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          insnNode = insnNodeList.next();

          if (insnNode.getOpcode() == Opcodes.ISTORE && ((VarInsnNode) insnNode).var == 3) {
            VarInsnNode call = (VarInsnNode) insnNode.getNext();

            // check opcode against checkpoint 182 - code for welcome screen info
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 3));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 2));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Replay", "checkPoint", "(II)V", false));
            break;
          }
        }
      }
      // sendLogout
      if (methodNode.name.equals("B") && methodNode.desc.equals("(I)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        AbstractInsnNode insnNode = insnNodeList.next();
        methodNode.instructions.insertBefore(
            insnNode,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC, "Game/Replay", "closeReplayPlayback", "()V", false));
      }
      // I (I)V is where most of the interface is processed
      if (methodNode.name.equals("I") && methodNode.desc.equals("(I)V")) {
        AbstractInsnNode first = methodNode.instructions.getFirst();

        methodNode.instructions.insert(
            first, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "is_hover", "Z"));
        methodNode.instructions.insert(first, new InsnNode(Opcodes.ICONST_0));

        // Show combat menu
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          if (insnNode.getOpcode() == Opcodes.BIPUSH) {
            IntInsnNode bipush = (IntInsnNode) insnNode;

            if (bipush.operand == 9) {
              AbstractInsnNode findNode = null;

              // Hide combat menu patch
              findNode = insnNode;
              while (findNode.getOpcode() != Opcodes.ALOAD) findNode = findNode.getNext();
              LabelNode label = new LabelNode();
              LabelNode skipLabel = new LabelNode();
              methodNode.instructions.insertBefore(
                  findNode,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC,
                      "Client/Settings",
                      "updateInjectedVariables",
                      "()V",
                      false));
              methodNode.instructions.insertBefore(
                  findNode,
                  new FieldInsnNode(
                      Opcodes.GETSTATIC, "Client/Settings", "COMBAT_MENU_HIDDEN_BOOL", "Z"));
              methodNode.instructions.insertBefore(findNode, new JumpInsnNode(Opcodes.IFGT, label));
              methodNode.instructions.insertBefore(findNode, skipLabel);
              methodNode.instructions.insertBefore(findNode, new InsnNode(Opcodes.ICONST_1));
              methodNode.instructions.insertBefore(
                  findNode,
                  new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Renderer", "combat_menu_shown", "Z"));
              methodNode.instructions.insert(findNode.getNext().getNext(), label);

              // Show combat menu patch
              JumpInsnNode jumpNode = (JumpInsnNode) insnNode.getNext();
              LabelNode exitLabel = jumpNode.label;
              LabelNode runLabel = (LabelNode) jumpNode.getNext();
              label = new LabelNode();
              jumpNode.label = label;
              methodNode.instructions.insert(jumpNode, new JumpInsnNode(Opcodes.GOTO, exitLabel));
              methodNode.instructions.insert(jumpNode, new JumpInsnNode(Opcodes.IFGT, skipLabel));
              methodNode.instructions.insert(
                  jumpNode,
                  new FieldInsnNode(
                      Opcodes.GETSTATIC, "Client/Settings", "COMBAT_MENU_SHOWN_BOOL", "Z"));
              methodNode.instructions.insert(
                  jumpNode,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC,
                      "Client/Settings",
                      "updateInjectedVariables",
                      "()V",
                      false));
              methodNode.instructions.insert(jumpNode, label);
              methodNode.instructions.insert(jumpNode, new JumpInsnNode(Opcodes.GOTO, runLabel));

              findNode = insnNode.getPrevious();
              while (findNode.getOpcode() != Opcodes.BIPUSH) findNode = findNode.getPrevious();
              methodNode.instructions.insertBefore(findNode, new InsnNode(Opcodes.ICONST_0));
              methodNode.instructions.insertBefore(
                  findNode,
                  new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Renderer", "combat_menu_shown", "Z"));

              break;
            }
          }
        }
      }

      // resetGame
      if (methodNode.name.equals("i") && methodNode.desc.equals("(I)V")) {

        // Skip clearing chat history when enabled
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          if (insnNode.getOpcode() == Opcodes.GETFIELD) {
            FieldInsnNode field = (FieldInsnNode) insnNode;
            if (field.owner.equals("client") && field.name.equals("yd")) {
              LabelNode skipLabel = new LabelNode();

              insnNode = insnNode.getPrevious();

              methodNode.instructions.insertBefore(
                  insnNode,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC,
                      "Client/Settings",
                      "updateInjectedVariables",
                      "()V",
                      false));

              methodNode.instructions.insertBefore(
                  insnNode,
                  new FieldInsnNode(
                      Opcodes.GETSTATIC, "Client/Settings", "LOAD_CHAT_HISTORY_BOOL", "Z"));

              methodNode.instructions.insertBefore(
                  insnNode, new JumpInsnNode(Opcodes.IFGT, skipLabel));

              for (int i = 0; i < 19; i++) insnNode = insnNode.getNext();

              methodNode.instructions.insertBefore(insnNode, skipLabel);

              break;
            }
          }
        }

        // reset bank drawn flag
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          if (insnNode.getOpcode() == Opcodes.PUTFIELD
              && ((FieldInsnNode) insnNode).owner.equals("client")
              && ((FieldInsnNode) insnNode).name.equals("Fe")
              && ((FieldInsnNode) insnNode).desc.equals("Z")) {
            insnNode = insnNode.getNext();
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_0));
            methodNode.instructions.insertBefore(
                insnNode,
                new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "bank_interface_drawn", "Z"));

            break;
          }
        }
      }

      if (methodNode.name.equals("a")
          && methodNode.desc.equals("(ILjava/lang/String;Ljava/lang/String;Z)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        int xteaIndex = 0;
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          // Dump login xtea keys
          if (xteaIndex < 4 && insnNode.getOpcode() == Opcodes.IASTORE) {
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Replay", "hookXTEAKey", "(I)I", false));
            xteaIndex++;
          }
        }

        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNodes = nextNode.getNext();

          if (nextNode == null || twoNextNodes == null) break;

          if (insnNode.getOpcode() == Opcodes.GETSTATIC
              && ((FieldInsnNode) insnNode).name.equals("il")
              && nextNode.getOpcode() == Opcodes.SIPUSH
              && ((IntInsnNode) nextNode).operand == 439
              && twoNextNodes.getOpcode() == Opcodes.AALOAD) {
            // just do the logic in client
            MethodInsnNode call = (MethodInsnNode) (twoNextNodes.getNext());
            // loginresponse
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 11));
            // reconnecting
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 4));
            // xtea keys
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 8));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "login_attempt_hook", "(IZ[I)V", false));
            break;
          }
        }

        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
              && nextNode.getOpcode() == Opcodes.ICONST_0) {
            insnNode = nextNode.getNext().getNext();
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 9));
            methodNode.instructions.insertBefore(insnNode, new LdcInsnNode(-422797528));
            methodNode.instructions.insertBefore(insnNode, new IntInsnNode(Opcodes.BIPUSH, -22));
            methodNode.instructions.insertBefore(
                insnNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "tb", "b", "(II)V", false));
          }

          if (insnNode.getOpcode() == Opcodes.BIPUSH && ((IntInsnNode) insnNode).operand == -6) {
            methodNode.instructions.insertBefore(insnNode, new IntInsnNode(Opcodes.BIPUSH, -5));
            methodNode.instructions.remove(insnNode);
            break;
          }
        }
      }
      if (methodNode.name.equals("u") && methodNode.desc.equals("(I)V")) {
        // Replay pause hook
        // TODO: Not sure but it seems like it gets broken upon starting another replay sometimes?
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        AbstractInsnNode findNode = insnNodeList.next();

        LabelNode label = new LabelNode();
        methodNode.instructions.insertBefore(findNode, new InsnNode(Opcodes.ICONST_0));
        methodNode.instructions.insertBefore(
            findNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Replay", "paused", "Z"));
        methodNode.instructions.insertBefore(findNode, new JumpInsnNode(Opcodes.IFEQ, label));
        methodNode.instructions.insertBefore(findNode, new InsnNode(Opcodes.RETURN));
        methodNode.instructions.insertBefore(findNode, label);
        methodNode.instructions.insertBefore(
            findNode,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC, "Game/Replay", "disconnect_hook", "()V", false));
      }
      if (methodNode.name.equals("J") && methodNode.desc.equals("(I)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          // Chat command patch
          if (insnNode.getOpcode() == Opcodes.SIPUSH) {
            IntInsnNode call = (IntInsnNode) insnNode;
            if (call.operand == 627) {
              AbstractInsnNode jmpNode = insnNode;
              while (jmpNode.getOpcode() != Opcodes.IFEQ) jmpNode = jmpNode.getNext();

              AbstractInsnNode insertNode = insnNode;
              while (insertNode.getOpcode() != Opcodes.INVOKEVIRTUAL)
                insertNode = insertNode.getPrevious();

              JumpInsnNode jmp = (JumpInsnNode) jmpNode;
              methodNode.instructions.insert(insertNode, new VarInsnNode(Opcodes.ASTORE, 2));
              methodNode.instructions.insert(
                  insertNode,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC,
                      "Game/Client",
                      "processChatCommand",
                      "(Ljava/lang/String;)Ljava/lang/String;"));
              methodNode.instructions.insert(insertNode, new VarInsnNode(Opcodes.ALOAD, 2));
            }
          }
        }

        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNode = nextNode.getNext();

          if (nextNode == null || twoNextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.ICONST_0
              && nextNode.getOpcode() == Opcodes.ISTORE
              && ((VarInsnNode) nextNode).var == 2) {
            LabelNode label = new LabelNode();
            InsnNode call = (InsnNode) insnNode;

            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                call, new FieldInsnNode(Opcodes.GETFIELD, "client", "Bb", "I"));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                call, new FieldInsnNode(Opcodes.GETFIELD, "client", "xb", "I"));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                call, new FieldInsnNode(Opcodes.GETFIELD, "client", "Qb", "I"));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                call, new FieldInsnNode(Opcodes.GETFIELD, "client", "I", "I"));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "gameInputHook", "(IIII)Z", false));
            methodNode.instructions.insertBefore(call, new JumpInsnNode(Opcodes.IFGT, label));
            methodNode.instructions.insertBefore(call, new InsnNode(Opcodes.RETURN));
            methodNode.instructions.insertBefore(call, label);
            break;
          }
        }

        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNode = nextNode.getNext();

          if (nextNode == null || twoNextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.IMUL
              && nextNode.getOpcode() == Opcodes.IADD
              && twoNextNode.getOpcode() == Opcodes.PUTFIELD
              && ((FieldInsnNode) twoNextNode).name.equals("ug")) {
            // entry point when its true to close it
            // intercept first time camera rotation
            // TODO: the setting constant at first may give bug in camera so im leaving it out for
            // the
            // moment
            // AbstractInsnNode call = twoNextNode.getNext();
            // methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
            // this constant is from the one in Camera
            // methodNode.instructions.insertBefore(call, new IntInsnNode(Opcodes.BIPUSH, 126));
            // methodNode.instructions.insertBefore(call, new FieldInsnNode(Opcodes.PUTFIELD,
            // "client",
            // "ug", "I"));
            break;
          }
        }

        // find part of "click" around report abuse tab
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode startNode, targetNode;

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.ICONST_1
              && nextNode.getOpcode() == Opcodes.PUTFIELD
              && ((FieldInsnNode) nextNode).owner.equals("client")
              && ((FieldInsnNode) nextNode).name.equals("Vf")) {
            LabelNode label = new LabelNode();
            startNode = insnNode;
            targetNode = nextNode.getNext();

            // find starting block in condition block
            int foundTimes = 0;
            while (startNode.getOpcode() != Opcodes.ALOAD && foundTimes < 2) {
              // has to be two times here
              startNode = startNode.getPrevious();
              if (startNode.getOpcode() == Opcodes.ALOAD) ++foundTimes;
            }

            // find ending block in condition block
            while (targetNode.getOpcode() != Opcodes.PUTFIELD) {
              // indicated by subsequent putfield after client.Vf = 1
              targetNode = targetNode.getNext();
            }
            targetNode = targetNode.getNext();

            methodNode.instructions.insertBefore(
                startNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "skipActionReportAbuseTabHook", "()Z"));
            methodNode.instructions.insertBefore(startNode, new JumpInsnNode(Opcodes.IFGT, label));

            methodNode.instructions.insertBefore(targetNode, label);
            break;
          }
        }
      }
      if (methodNode.name.equals("h") && methodNode.desc.equals("(B)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          // Private chat command patch
          if (insnNode.getOpcode() == Opcodes.GETFIELD) {
            FieldInsnNode field = (FieldInsnNode) insnNode;
            if (field.owner.equals("client")
                && field.name.equals("Ob")
                && insnNode.getPrevious().getPrevious().getOpcode() != Opcodes.INVOKEVIRTUAL) {
              insnNode = insnNode.getPrevious().getPrevious();
              methodNode.instructions.insert(
                  insnNode,
                  new FieldInsnNode(Opcodes.PUTFIELD, "client", "Ob", "Ljava/lang/String;"));
              methodNode.instructions.insert(
                  insnNode,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC,
                      "Game/Client",
                      "processPrivateCommand",
                      "(Ljava/lang/String;)Ljava/lang/String;"));
              methodNode.instructions.insert(
                  insnNode,
                  new FieldInsnNode(Opcodes.GETFIELD, "client", "Ob", "Ljava/lang/String;"));
              methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
              methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
              break;
            }
          }
        }
      }
      if (methodNode.name.equals("a") && methodNode.desc.equals("(IIIIIIII)V")) {
        // Draw NPC hook
        AbstractInsnNode insnNode = methodNode.instructions.getLast();
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 1));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 7));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 4));

        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "e", "Mb", "[Ljava/lang/String;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Tb", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 6));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "t", "I"));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        // extended npc hook to include current hits and max hits
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Tb", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 6));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "B", "I"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Tb", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 6));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "G", "I"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Tb", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 6));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "t", "I"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Tb", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 6));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "b", "I"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Tb", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 6));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "i", "I"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Tb", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 6));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "K", "I"));
        methodNode.instructions.insertBefore(
            insnNode,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC, "Game/Client", "drawNPC", "(IIIILjava/lang/String;IIIIII)V"));
      }
      if (methodNode.name.equals("b") && methodNode.desc.equals("(IIIIIIII)V")) {
        // Draw Player hook
        AbstractInsnNode insnNode = methodNode.instructions.getLast();
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 5));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 6));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 2));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 7));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "rg", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "C", "Ljava/lang/String;"));
        // extended player hook to include current hits and max hits
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "rg", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "B", "I"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "rg", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "G", "I"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "rg", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "b", "I"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "rg", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "s", "I"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "rg", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "i", "I"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "rg", "[Lta;"));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETFIELD, "ta", "K", "I"));
        methodNode.instructions.insertBefore(
            insnNode,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "Game/Client",
                "drawPlayer",
                "(IIIILjava/lang/String;IIIIII)V"));
      }
      if (methodNode.name.equals("b") && methodNode.desc.equals("(IIIIIII)V")) {
        // Draw Item hook
        // ILOAD 4 is item id
        AbstractInsnNode insnNode = methodNode.instructions.getLast();
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 3));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 7));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 5));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 1));
        methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 4));
        methodNode.instructions.insertBefore(
            insnNode,
            new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "drawItem", "(IIIII)V"));
      }
      if (methodNode.name.equals("L") && methodNode.desc.equals("(I)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          // Right click bounds fix
          if (insnNode.getOpcode() == Opcodes.SIPUSH) {
            IntInsnNode call = (IntInsnNode) insnNode;
            AbstractInsnNode nextNode = insnNode.getNext();

            if (call.operand == 510) {
              call.operand = 512 - call.operand;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
            } else if (call.operand == 315) {
              call.operand = 334 - call.operand;
              methodNode.instructions.insertBefore(
                  insnNode,
                  new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height_client", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
            } else if (call.operand == -316) {
              call.operand = 334 - (call.operand * -1);
              methodNode.instructions.insertBefore(
                  insnNode,
                  new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height_client", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.INEG));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
            }
          }
        }

        // Hook that gives out the message on X action such as npcs, items and prints them top left
        // corner
        // TODO: use the hook
        insnNodeList = methodNode.instructions.iterator();

        LabelNode lblNode = null;
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          // this part checks to see if there's a string to render for the two cases below
          if (insnNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) insnNode).var == 3
              && nextNode.getOpcode() == Opcodes.IFNONNULL) {
            lblNode = ((JumpInsnNode) nextNode.getNext()).label;
          }

          // is_hover = 1, also getfield client.li:ba
          if (insnNode.getOpcode() == Opcodes.GETFIELD) {
            FieldInsnNode fieldNode = ((FieldInsnNode) insnNode);
            if (fieldNode.owner.equals("client")
                && fieldNode.name.equals("li")
                && nextNode.getOpcode() == Opcodes.ALOAD
                && ((VarInsnNode) nextNode).var == 5) {
              methodNode.instructions.insert(fieldNode, new VarInsnNode(Opcodes.ASTORE, 5));
              methodNode.instructions.insert(
                  fieldNode,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC,
                      "Game/Client",
                      "mouse_action_hook",
                      "(Ljava/lang/String;)Ljava/lang/String;",
                      false));
              methodNode.instructions.insert(fieldNode, new VarInsnNode(Opcodes.ALOAD, 5));
              methodNode.instructions.insert(
                  fieldNode, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "is_hover", "Z"));
              methodNode.instructions.insert(fieldNode, new InsnNode(Opcodes.ICONST_1));
              continue;
            }
          }

          // is_hover = 0
          if (insnNode instanceof LabelNode
              && lblNode != null
              && ((LabelNode) insnNode).equals(lblNode)) {
            methodNode.instructions.insert(
                insnNode, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "is_hover", "Z"));
            methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ICONST_0));
          }
        }
      }
      if (methodNode.name.equals("a") && methodNode.desc.equals("(ZZ)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          // Friends chat mouse fix
          if (insnNode.getOpcode() == Opcodes.SIPUSH) {
            IntInsnNode call = (IntInsnNode) insnNode;
            if (call.operand == 489 || call.operand == 429) {
              call.operand = 512 - call.operand;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
            }
            if (call.operand == -430) {
              call.operand = 512 - (call.operand * -1);
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.INEG));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
            }
          }
        }
      }
      if (methodNode.name.equals("i") && methodNode.desc.equals("(I)V")) {
        AbstractInsnNode lastNode = methodNode.instructions.getLast().getPrevious();

        // Send combat style option
        LabelNode label = new LabelNode();

        methodNode.instructions.insert(lastNode, label);

        // Format
        methodNode.instructions.insert(
            lastNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "da", "b", "(I)V", false));
        methodNode.instructions.insert(lastNode, new IntInsnNode(Opcodes.SIPUSH, 21294));
        methodNode.instructions.insert(
            lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
        methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

        // Write byte
        methodNode.instructions.insert(
            lastNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "ja", "c", "(II)V", false));
        methodNode.instructions.insert(lastNode, new IntInsnNode(Opcodes.BIPUSH, -80));
        methodNode.instructions.insert(
            lastNode,
            new FieldInsnNode(Opcodes.GETSTATIC, "Client/Settings", "COMBAT_STYLE_INT", "I"));
        methodNode.instructions.insert(
            lastNode,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "Client/Settings",
                "updateInjectedVariables",
                "()V",
                false)); // TODO Remove this line when COMBAT_STYLE_INT is eliminated
        methodNode.instructions.insert(
            lastNode, new FieldInsnNode(Opcodes.GETFIELD, "da", "f", "Lja;"));
        methodNode.instructions.insert(
            lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
        methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

        // Create Packet
        methodNode.instructions.insert(
            lastNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "da", "b", "(II)V", false));
        methodNode.instructions.insert(lastNode, new InsnNode(Opcodes.ICONST_0));
        methodNode.instructions.insert(lastNode, new IntInsnNode(Opcodes.BIPUSH, 29));
        methodNode.instructions.insert(
            lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
        methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

        // Skip combat packet if style is already controlled
        methodNode.instructions.insert(lastNode, new JumpInsnNode(Opcodes.IF_ICMPLE, label));
        methodNode.instructions.insert(lastNode, new InsnNode(Opcodes.ICONST_0));
        methodNode.instructions.insert(
            lastNode,
            new FieldInsnNode(Opcodes.GETSTATIC, "Client/Settings", "COMBAT_STYLE_INT", "I"));
        methodNode.instructions.insert(
            lastNode,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "Client/Settings",
                "updateInjectedVariables",
                "()V",
                false)); // TODO Remove this line when COMBAT_STYLE_INT is eliminated

        // Client init_game
        methodNode.instructions.insert(
            lastNode,
            new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "init_game", "()V", false));
      }
      if (methodNode.name.equals("o") && methodNode.desc.equals("(I)V")) {
        // Client.init_login patch
        AbstractInsnNode findNode = methodNode.instructions.getLast();
        methodNode.instructions.insertBefore(
            findNode,
            new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "init_login", "()V", false));
      }
      if (methodNode.name.equals("a") && methodNode.desc.equals("(B)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          // Camera view distance crash fix
          if (insnNode.getOpcode() == Opcodes.SIPUSH) {
            IntInsnNode call = (IntInsnNode) insnNode;
            if (call.operand == 15000) {
              call.operand = 32767;
            }
          }
        }

        // Client.init patch
        AbstractInsnNode findNode = methodNode.instructions.getFirst();
        methodNode.instructions.insertBefore(findNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insertBefore(
            findNode,
            new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "instance", "Ljava/lang/Object;"));
        methodNode.instructions.insertBefore(
            findNode,
            new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "init", "()V", false));
      }
      if (methodNode.name.equals("G") && methodNode.desc.equals("(I)V")) {
        // TODO: This can be shortened, I'll fix it another time

        // NPC Dialogue keyboard
        AbstractInsnNode lastNode = methodNode.instructions.getLast().getPrevious();

        LabelNode label = new LabelNode();

        methodNode.instructions.insert(lastNode, label);

        // Hide dialogue
        methodNode.instructions.insert(
            lastNode, new FieldInsnNode(Opcodes.PUTFIELD, "client", "Ph", "Z"));
        methodNode.instructions.insert(lastNode, new InsnNode(Opcodes.ICONST_0));
        methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

        // Format
        methodNode.instructions.insert(
            lastNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "da", "b", "(I)V", false));
        methodNode.instructions.insert(lastNode, new IntInsnNode(Opcodes.SIPUSH, 21294));
        methodNode.instructions.insert(
            lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
        methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

        // Write byte
        methodNode.instructions.insert(
            lastNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "ja", "c", "(II)V", false));
        methodNode.instructions.insert(lastNode, new IntInsnNode(Opcodes.BIPUSH, 115));
        methodNode.instructions.insert(
            lastNode,
            new FieldInsnNode(Opcodes.GETSTATIC, "Game/KeyboardHandler", "dialogue_option", "I"));
        methodNode.instructions.insert(
            lastNode, new FieldInsnNode(Opcodes.GETFIELD, "da", "f", "Lja;"));
        methodNode.instructions.insert(
            lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
        methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

        // Create Packet
        methodNode.instructions.insert(
            lastNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "da", "b", "(II)V", false));
        methodNode.instructions.insert(lastNode, new InsnNode(Opcodes.ICONST_0));
        methodNode.instructions.insert(lastNode, new IntInsnNode(Opcodes.BIPUSH, 116));
        methodNode.instructions.insert(
            lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jh", "Lda;"));
        methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));

        // Check if dialogue option is pressed
        methodNode.instructions.insert(lastNode, new JumpInsnNode(Opcodes.IF_ICMPGE, label));
        // Menu option count
        methodNode.instructions.insert(
            lastNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Id", "I"));
        methodNode.instructions.insert(lastNode, new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.insert(
            lastNode,
            new FieldInsnNode(Opcodes.GETSTATIC, "Game/KeyboardHandler", "dialogue_option", "I"));
        methodNode.instructions.insert(lastNode, new JumpInsnNode(Opcodes.IFLT, label));
        methodNode.instructions.insert(
            lastNode,
            new FieldInsnNode(Opcodes.GETSTATIC, "Game/KeyboardHandler", "dialogue_option", "I"));
      }
      if (methodNode.name.equals("f") && methodNode.desc.equals("(I)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        boolean roofHidePatched = false;
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          // Hide Roof option
          if (insnNode.getOpcode() == Opcodes.BIPUSH) {
            IntInsnNode field = (IntInsnNode) insnNode;

            if (!roofHidePatched && field.operand == 118) {
              JumpInsnNode end =
                  (JumpInsnNode)
                      insnNode
                          .getNext()
                          .getNext()
                          .getNext()
                          .getNext()
                          .getNext()
                          .getNext()
                          .getNext();
              AbstractInsnNode ifStart =
                  insnNode
                      .getPrevious()
                      .getPrevious()
                      .getPrevious()
                      .getPrevious()
                      .getPrevious()
                      .getPrevious()
                      .getPrevious()
                      .getPrevious()
                      .getPrevious()
                      .getPrevious();
              methodNode.instructions.insertBefore(
                  ifStart,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC,
                      "Client/Settings",
                      "updateInjectedVariables",
                      "()V",
                      false));
              methodNode.instructions.insertBefore(
                  ifStart,
                  new FieldInsnNode(Opcodes.GETSTATIC, "Client/Settings", "HIDE_ROOFS_BOOL", "Z"));
              methodNode.instructions.insertBefore(
                  ifStart, new JumpInsnNode(Opcodes.IFGT, end.label));
              roofHidePatched = true;
            }
          }

          // Move wilderness skull
          if (insnNode.getOpcode() == Opcodes.SIPUSH) {
            IntInsnNode call = (IntInsnNode) insnNode;
            if (call.operand == 465 || call.operand == 453) {
              call.operand = 512 - call.operand;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insert(
                  insnNode, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "wild_level", "I"));
              methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ILOAD, 3));
              methodNode.instructions.insert(
                  insnNode, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "is_in_wild", "Z"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ICONST_1));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
            }
          }

          // unset is_in_wild flag if not in wild
          AbstractInsnNode nextNode = insnNode.getNext();
          if (insnNode.getOpcode() == Opcodes.ICONST_M1
              && nextNode.getOpcode() == Opcodes.ILOAD
              && ((VarInsnNode) nextNode).var == 2) {
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "is_in_wild", "Z"));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_M1));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "wild_level", "I"));
          }
        }

        // Retro FPS overlay, Native Text draw method
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          if (insnNode.getOpcode() == Opcodes.INVOKESPECIAL
              && ((MethodInsnNode) insnNode).name.equals("l")
              && ((MethodInsnNode) insnNode).desc.equals("(I)V")) {
            InsnNode call = (InsnNode) insnNode.getNext();
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                call, new FieldInsnNode(Opcodes.GETFIELD, "client", "li", "Lba;"));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "drawNativeTextHook",
                    "(Ljava/lang/Object;)V",
                    false));
            break;
          }
        }

        // conditionally render any new panels?
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (insnNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) insnNode).var == 0
              && nextNode.getOpcode() == Opcodes.GETFIELD
              && ((FieldInsnNode) nextNode).name.equals("Qk")) {
            LabelNode label = new LabelNode();
            VarInsnNode call = (VarInsnNode) insnNode;

            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "drawGameHook", "()Z", false));
            methodNode.instructions.insertBefore(call, new JumpInsnNode(Opcodes.IFGT, label));
            methodNode.instructions.insertBefore(call, new InsnNode(Opcodes.RETURN));
            methodNode.instructions.insertBefore(call, label);
            break;
          }
        }
      }
      if (methodNode.name.equals("a") && methodNode.desc.equals("(IIZ)Z")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          // Move the load screen text dialogue
          if (insnNode.getOpcode() == Opcodes.SIPUSH) {
            IntInsnNode call = (IntInsnNode) insnNode;
            if (call.operand == 256) {
              call.operand = 2;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
            } else if (call.operand == 192) {
              call.operand = 2;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IADD));
              methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 19));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
            }
          }
        }
      }
      if (methodNode.name.equals("d") && methodNode.desc.equals("(B)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          // Center logout dialogue
          if (insnNode.getOpcode() == Opcodes.SIPUSH || insnNode.getOpcode() == Opcodes.BIPUSH) {
            IntInsnNode call = (IntInsnNode) insnNode;
            if (call.operand == 256) {
              call.operand = 2;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
            } else if (call.operand == 173) {
              call.operand = 2;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
            } else if (call.operand == 126) {
              call.operand = 2;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
              methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 130));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
            } else if (call.operand == 137) {
              call.operand = 2;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
              methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 36));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
            }
          }
        }
      }
      if (methodNode.name.equals("j") && methodNode.desc.equals("(I)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          // Center welcome box
          if (insnNode.getOpcode() == Opcodes.SIPUSH || insnNode.getOpcode() == Opcodes.BIPUSH) {
            IntInsnNode call = (IntInsnNode) insnNode;
            if (call.operand == 256) {
              call.operand = 2;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
            } else if (call.operand == 167) {
              call.operand = 2;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "height", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
              methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 6));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
            } else if (call.operand == 56) {
              call.operand = 2;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
              methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 200));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
            } else if (call.operand == -87) {
              call.operand = 2;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.INEG));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
              methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 169));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
            } else if (call.operand == 426) {
              call.operand = 2;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IADD));
              methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 170));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
            } else if (call.operand == 106) {
              call.operand = 2;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.ISUB));
              methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 150));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
            } else if (call.operand == 406) {
              call.operand = 2;
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Renderer", "width", "I"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IADD));
              methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 150));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IDIV));
            }
          }
        }

        // dynamic size welcome box with show recovery
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode callNode;

          if (insnNode.getOpcode() == Opcodes.BIPUSH && ((IntInsnNode) insnNode).operand == 65) {
            callNode = insnNode.getNext().getNext();
            methodNode.instructions.insertBefore(callNode, new VarInsnNode(Opcodes.ILOAD, 2));
            methodNode.instructions.insertBefore(
                callNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "welcome_screen_size", "(I)I", false));
            methodNode.instructions.insertBefore(callNode, new VarInsnNode(Opcodes.ISTORE, 2));
            break;
          }
        }

        // recovery questions not set
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode startNode, targetNode;

          if (insnNode.getOpcode() == Opcodes.SIPUSH && ((IntInsnNode) insnNode).operand == 663) {
            startNode = insnNode;
            while (startNode.getOpcode() != Opcodes.IINC || ((IincInsnNode) startNode).incr != 15) {
              // find incr += 15 before Do this from the 'account management' area on our front
              // webpage
              startNode = startNode.getPrevious();
            }
            targetNode = insnNode;
            while (targetNode.getOpcode() != Opcodes.IINC
                || ((IincInsnNode) targetNode).incr != 15) {
              // find incr += 15 after Do this from the 'account management' area on our front
              // webpage
              targetNode = targetNode.getNext();
            }

            LabelNode label = new LabelNode();

            methodNode.instructions.insertBefore(
                startNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "showSecuritySettings", "()Z"));
            methodNode.instructions.insertBefore(startNode, new JumpInsnNode(Opcodes.IFGT, label));

            methodNode.instructions.insertBefore(targetNode, label);
            break;
          }
        }

        // recovery questions recently set or changed
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode startNode, targetNode;

          if (insnNode.getOpcode() == Opcodes.SIPUSH && ((IntInsnNode) insnNode).operand == 666) {
            startNode = insnNode;
            while (startNode.getOpcode() != Opcodes.ALOAD || ((VarInsnNode) startNode).var != 0) {
              // find start section of "you changed your recovery questions"
              startNode = startNode.getPrevious();
            }
            targetNode = insnNode;
            while (targetNode.getOpcode() != Opcodes.SIPUSH
                || ((IntInsnNode) targetNode).operand != 663) {
              // find near end section of "Do this from the 'account management' area on our front
              // webpage"
              targetNode = targetNode.getNext();
            }

            while (targetNode.getOpcode() != Opcodes.IINC
                || ((IincInsnNode) targetNode).incr != 15) {
              // find incr += 15 after Do this from the 'account management' area on our front
              // webpage
              targetNode = targetNode.getNext();
            }
            targetNode = targetNode.getNext();

            LabelNode label = new LabelNode();

            methodNode.instructions.insertBefore(
                startNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "showSecuritySettings", "()Z"));
            methodNode.instructions.insertBefore(startNode, new JumpInsnNode(Opcodes.IFGT, label));

            methodNode.instructions.insertBefore(targetNode, label);
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ILOAD, 2));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ILOAD, 3));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                targetNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "I", "I"));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                targetNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "xb", "I"));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                targetNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Cf", "I"));
            methodNode.instructions.insertBefore(
                targetNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/AccountManagement",
                    "welcome_changed_recent_recovery_hook",
                    "(IIIII)I"));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ILOAD, 3));
            methodNode.instructions.insertBefore(targetNode, new InsnNode(Opcodes.IADD));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ISTORE, 3));
            break;
          }
        }

        // have the "Click here to close window" not shown if showSecuritySettings
        // plus security tips
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode targetNode;
          AbstractInsnNode jumpToNode;

          if (insnNode.getOpcode() == Opcodes.BIPUSH && ((IntInsnNode) insnNode).operand == 126) {
            targetNode = insnNode;
            while (targetNode.getOpcode() != Opcodes.LDC
                || !((LdcInsnNode) targetNode).cst.equals(16777215)) {
              // find start section of "Click here to close window"
              targetNode = targetNode.getPrevious();
            }
            targetNode = targetNode.getNext().getNext(); // after istore5
            jumpToNode = targetNode;
            while (jumpToNode.getOpcode() != Opcodes.BIPUSH
                || ((IntInsnNode) jumpToNode).operand != 126) {
              // find label "Click here to close window"
              jumpToNode = jumpToNode.getNext();
            }
            while (jumpToNode.getOpcode() != Opcodes.INVOKEVIRTUAL) {
              // find where "Click here to close window" is actually drawn to jump past
              jumpToNode = jumpToNode.getNext();
            }
            jumpToNode = jumpToNode.getNext();
            LabelNode label = new LabelNode();
            LabelNode label2 = new LabelNode();
            LabelNode label3 = new LabelNode();

            methodNode.instructions.insertBefore(jumpToNode, label3);

            methodNode.instructions.insertBefore(
                targetNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "showWelcomeClickToClose", "()Z"));
            methodNode.instructions.insertBefore(targetNode, new JumpInsnNode(Opcodes.IFGT, label));
            methodNode.instructions.insertBefore(
                targetNode, new JumpInsnNode(Opcodes.GOTO, label3));
            methodNode.instructions.insertBefore(targetNode, label);
            methodNode.instructions.insertBefore(
                targetNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "showSecurityTipOfDay", "()Z"));
            methodNode.instructions.insertBefore(
                targetNode, new JumpInsnNode(Opcodes.IFLE, label2));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ILOAD, 2));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ILOAD, 3));
            methodNode.instructions.insertBefore(
                targetNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/AccountManagement",
                    "welcome_security_tip_day_hook",
                    "(II)I"));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ILOAD, 3));
            methodNode.instructions.insertBefore(targetNode, new InsnNode(Opcodes.IADD));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ISTORE, 3));
            methodNode.instructions.insertBefore(targetNode, label2);
            break;
          }
        }
      }
      if (methodNode.name.equals("k") && methodNode.desc.equals("(B)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          // Save settings from combat menu
          if (insnNode.getOpcode() == Opcodes.PUTFIELD) {
            FieldInsnNode field = (FieldInsnNode) insnNode;

            if (field.owner.equals("client") && field.name.equals("Fg")) {
              methodNode.instructions.insert(
                  insnNode,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC, "Client/Settings", "save", "()V", false));
              methodNode.instructions.insert(
                  insnNode,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC,
                      "Client/Settings",
                      "outputInjectedVariables",
                      "()V",
                      false));
              methodNode.instructions.insert(
                  insnNode,
                  new FieldInsnNode(Opcodes.PUTSTATIC, "Client/Settings", "COMBAT_STYLE_INT", "I"));
              methodNode.instructions.insert(
                  insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Fg", "I"));
              methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            }
          }
        }
      }
      if (methodNode.name.equals("a")
          && methodNode.desc.equals(
              "(ZLjava/lang/String;ILjava/lang/String;IILjava/lang/String;Ljava/lang/String;)V")) {
        AbstractInsnNode first = methodNode.instructions.getFirst();

        LabelNode continueLabel = new LabelNode();

        methodNode.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 7));
        methodNode.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 4));
        methodNode.instructions.insertBefore(first, new VarInsnNode(Opcodes.ILOAD, 5));
        methodNode.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 8));
        methodNode.instructions.insertBefore(
            first,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "Game/Client",
                "messageHook",
                "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)Z"));
        methodNode.instructions.insertBefore(first, new JumpInsnNode(Opcodes.IFGT, continueLabel));
        methodNode.instructions.insertBefore(first, new InsnNode(Opcodes.RETURN));
        methodNode.instructions.insertBefore(first, continueLabel);

        // Replay seeking don't show messages hook
        // LabelNode label = new LabelNode();
        // methodNode.instructions.insertBefore(first, new InsnNode(Opcodes.ICONST_0));
        // methodNode.instructions.insertBefore(first, new FieldInsnNode(Opcodes.GETSTATIC,
        // "Game/Replay", "isSeeking", "Z"));
        // methodNode.instructions.insertBefore(first, new JumpInsnNode(Opcodes.IFEQ, label));
        // methodNode.instructions.insertBefore(first, new InsnNode(Opcodes.RETURN));
        // methodNode.instructions.insertBefore(first, label);
      }
      if (methodNode.name.equals("b") && methodNode.desc.equals("(ZI)V")) {
        // Fix on swap between command and use, if 635 is received make it 650 by hook
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.ISTORE && ((VarInsnNode) insnNode).var == 3) {
            VarInsnNode call = (VarInsnNode) nextNode;
            methodNode.instructions.insertBefore(nextNode, new VarInsnNode(Opcodes.ILOAD, 3));
            methodNode.instructions.insertBefore(
                nextNode,
                new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "swapUseMenuHook", "(I)I"));
            methodNode.instructions.insertBefore(nextNode, new VarInsnNode(Opcodes.ISTORE, 3));
            break;
          }
        }

        // Throwable crash patch
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.INVOKESTATIC
              && nextNode.getOpcode() == Opcodes.ATHROW) {
            MethodInsnNode call = (MethodInsnNode) insnNode;
            methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.RETURN));
          }
        }

        // Fix on sleep, so packets are not managed directly
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode prevNode = insnNode.getPrevious();

          if (prevNode == null) continue;

          // patch before the sequence of command checks
          if (insnNode.getOpcode() == Opcodes.ASTORE
              && ((VarInsnNode) insnNode).var == 9
              && prevNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
            VarInsnNode call = (VarInsnNode) insnNode;
            LabelNode label = ((LabelNode) insnNode.getNext());

            methodNode.instructions.insert(call, new VarInsnNode(Opcodes.ISTORE, 4));
            methodNode.instructions.insert(
                call, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Client", "sleepBagIdx", "I"));
            methodNode.instructions.insert(call, new VarInsnNode(Opcodes.ISTORE, 3));
            methodNode.instructions.insert(call, new IntInsnNode(Opcodes.SIPUSH, (short) 640));
            methodNode.instructions.insert(call, new JumpInsnNode(Opcodes.IF_ICMPNE, label));
            methodNode.instructions.insert(call, new InsnNode(Opcodes.ICONST_1));
            methodNode.instructions.insert(
                call, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Client", "sleepCmdSent", "Z"));
          }
        }

        // Turn off sleep cmd flag
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNodes = nextNode.getNext();

          if (nextNode == null || twoNextNodes == null) break;

          if (insnNode.getOpcode() == Opcodes.ALOAD
              && nextNode.getOpcode() == Opcodes.GETFIELD
              && twoNextNodes.getOpcode() == Opcodes.BIPUSH
              && ((IntInsnNode) twoNextNodes).operand == 90) {
            VarInsnNode call = (VarInsnNode) insnNode;
            methodNode.instructions.insert(
                call, new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "sleepCmdSent", "Z"));
            methodNode.instructions.insert(call, new InsnNode(Opcodes.ICONST_0));
          }
        }

        // catch on position clicked
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNodes = nextNode.getNext();
          AbstractInsnNode threeNextNodes = twoNextNodes.getNext();

          if (nextNode == null || twoNextNodes == null || threeNextNodes == null) break;

          if (insnNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) insnNode).var == 0
              && nextNode.getOpcode() == Opcodes.GETFIELD
              && twoNextNodes.getOpcode() == Opcodes.ILOAD
              && ((VarInsnNode) twoNextNodes).var == 2
              && threeNextNodes.getOpcode() == Opcodes.SIPUSH
              && ((IntInsnNode) threeNextNodes).operand == -4126) {

            VarInsnNode call = (VarInsnNode) insnNode;

            methodNode.instructions.insertBefore(
                call, new TypeInsnNode(Opcodes.NEW, "java/lang/Integer"));
            methodNode.instructions.insertBefore(call, new InsnNode(Opcodes.DUP));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 3));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Integer", "<init>", "(I)V"));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 4));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 5));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "gameClickHook",
                    "(Ljava/lang/Integer;II)V"));
            break;
          }
        }

        // patch nature rune alching
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          if (insnNode.getOpcode() == Opcodes.ICONST_4) {
            LabelNode itemLabel = new LabelNode();
            LabelNode originalLabel = new LabelNode();
            LabelNode skipLabel = new LabelNode();

            insnNode = insnNodeList.next().getPrevious().getPrevious().getPrevious();

            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Client/Settings",
                    "updateInjectedVariables",
                    "()V",
                    false));
            methodNode.instructions.insertBefore(
                insnNode,
                new FieldInsnNode(
                    Opcodes.GETSTATIC, "Client/Settings", "PROTECT_NAT_RUNE_ALCH_BOOL", "Z"));
            methodNode.instructions.insertBefore(
                insnNode, new JumpInsnNode(Opcodes.IFEQ, originalLabel));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 5));
            methodNode.instructions.insertBefore(insnNode, new IntInsnNode(Opcodes.SIPUSH, 10));
            methodNode.instructions.insertBefore(
                insnNode, new JumpInsnNode(Opcodes.IF_ICMPEQ, itemLabel));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 5));
            methodNode.instructions.insertBefore(insnNode, new IntInsnNode(Opcodes.SIPUSH, 28));
            methodNode.instructions.insertBefore(
                insnNode, new JumpInsnNode(Opcodes.IF_ICMPNE, originalLabel));
            methodNode.instructions.insertBefore(insnNode, itemLabel);
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "vf", "[I"));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 4));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.IALOAD));
            methodNode.instructions.insertBefore(insnNode, new IntInsnNode(Opcodes.SIPUSH, 40));
            methodNode.instructions.insertBefore(
                insnNode, new JumpInsnNode(Opcodes.IF_ICMPNE, originalLabel));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_0));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ACONST_NULL));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_0));
            methodNode.instructions.insertBefore(
                insnNode,
                new LdcInsnNode("@whi@Nature rune alchemy protection is currently enabled"));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_3));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_0));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ACONST_NULL));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ACONST_NULL));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESPECIAL,
                    "client",
                    "a",
                    "(ZLjava/lang/String;ILjava/lang/String;IILjava/lang/String;Ljava/lang/String;)V",
                    false));
            methodNode.instructions.insertBefore(
                insnNode, new JumpInsnNode(Opcodes.GOTO, skipLabel));
            methodNode.instructions.insertBefore(insnNode, originalLabel);
            for (int i = 0; i < 21; i++) insnNode = insnNode.getNext();
            methodNode.instructions.insertBefore(insnNode, skipLabel);

            break;
          }
        }
      }

      if (methodNode.name.equals("b") && methodNode.desc.equals("(IBI)V")) {
        Iterator<AbstractInsnNode> insnNodeList;

        // hook onto npc attack info
        insnNodeList = methodNode.instructions.iterator();
        // two times it gets found, first is one for player second for npc
        int pos = -1;
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNode = nextNode.getNext();

          if (nextNode == null || twoNextNode == null) break;
          if (insnNode.getOpcode() == Opcodes.SIPUSH && ((IntInsnNode) insnNode).operand == -235) {
            pos = 0; // player combat hook
            continue;
          }
          if (insnNode.getOpcode() == Opcodes.BIPUSH
              && ((IntInsnNode) insnNode).operand == 104
              && nextNode.getOpcode() == Opcodes.ILOAD) {
            pos = 1; // npc combat hook
            continue;
          }
          if (insnNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) insnNode).var == 7
              && nextNode.getOpcode() == Opcodes.ILOAD
              && ((VarInsnNode) nextNode).var == 9
              && twoNextNode.getOpcode() == Opcodes.PUTFIELD
              && ((FieldInsnNode) twoNextNode).owner.equals("ta")
              && ((FieldInsnNode) twoNextNode).name.equals("u")) {
            methodNode.instructions.insert(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "inCombatHook",
                    "(IIIIILjava/lang/Object;)V"));
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

        // setLoadingArea
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNode = nextNode.getNext();

          if (nextNode == null || twoNextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.ILOAD
              && ((VarInsnNode) insnNode).var == 5
              && nextNode.getOpcode() == Opcodes.IFNE
              && twoNextNode.getOpcode() == Opcodes.GOTO) {
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 5));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "isLoadingHook", "(Z)V"));
          }
        }

        // hook onto received menu options
        insnNodeList = methodNode.instructions.iterator();
        LabelNode lblNode = null;
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNode = nextNode.getNext();

          if (nextNode == null || twoNextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) insnNode).var == 0
              && nextNode.getOpcode() == Opcodes.GETFIELD
              && ((FieldInsnNode) nextNode).owner.equals("client")
              && ((FieldInsnNode) nextNode).name.equals("ah")
              && twoNextNode.getOpcode() == Opcodes.ILOAD
              && ((VarInsnNode) twoNextNode).var == 5
              && insnNode.getPrevious().getOpcode() == Opcodes.IF_ICMPLE) {
            // in here is part where menu options are received, is a loop
            lblNode = ((JumpInsnNode) insnNode.getPrevious()).label;
            continue;
          }

          if (lblNode != null
              && insnNode instanceof LabelNode
              && ((LabelNode) insnNode).equals(lblNode)
              && twoNextNode.getOpcode() == Opcodes.RETURN) {
            InsnNode call = (InsnNode) twoNextNode;
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                call, new FieldInsnNode(Opcodes.GETFIELD, "client", "ah", "[Ljava/lang/String;"));
            // could also be client.Id but more lines would be needed
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 4));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "receivedOptionsHook",
                    "([Ljava/lang/String;I)V"));
          }
        }

        // hook onto some "other" opcode received
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          LabelNode label = new LabelNode();

          if (nextNode == null) continue;
          if (insnNode.getOpcode() == Opcodes.LDC
              && insnNode instanceof LdcInsnNode
              && ((LdcInsnNode) insnNode).cst instanceof Integer
              && ((Integer) ((LdcInsnNode) insnNode).cst).equals(2097151)
              && nextNode.getOpcode() == Opcodes.ACONST_NULL) {
            LdcInsnNode call = (LdcInsnNode) insnNode;
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 1));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 3));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "newOpcodeReceivedHook", "(II)Z", false));
            methodNode.instructions.insertBefore(call, new JumpInsnNode(Opcodes.IFGT, label));
            methodNode.instructions.insertBefore(call, new InsnNode(Opcodes.RETURN));
            methodNode.instructions.insertBefore(call, label);
            break;
          }
        }

        // hook onto "existing" opcode received
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          LabelNode label = new LabelNode();

          if (insnNode.getOpcode() == Opcodes.PUTSTATIC
              && ((FieldInsnNode) insnNode).name.equals("Qj")) {
            AbstractInsnNode call = insnNode.getNext();
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 1));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 3));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "gameOpcodeReceivedHook", "(II)Z", false));
            methodNode.instructions.insertBefore(call, new JumpInsnNode(Opcodes.IFGT, label));
            methodNode.instructions.insertBefore(call, new InsnNode(Opcodes.RETURN));
            methodNode.instructions.insertBefore(call, label);
            break;
          }
        }
      }
      // hook onto selected menu option
      if (methodNode.name.equals("G") && methodNode.desc.equals("(I)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNode = nextNode.getNext();

          if (nextNode == null || twoNextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) insnNode).var == 0
              && nextNode.getOpcode() == Opcodes.GETFIELD
              && ((FieldInsnNode) nextNode).owner.equals("client")
              && ((FieldInsnNode) nextNode).name.equals("Jh")
              && twoNextNode.getOpcode() == Opcodes.BIPUSH
              && ((IntInsnNode) twoNextNode).operand == 116) {
            VarInsnNode call = (VarInsnNode) insnNode;
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                call, new FieldInsnNode(Opcodes.GETFIELD, "client", "ah", "[Ljava/lang/String;"));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 2));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "selectedOptionHook",
                    "([Ljava/lang/String;I)V"));
          }
        }
      }
      // hook menu item
      if (methodNode.name.equals("a") && methodNode.desc.equals("(IZ)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        LabelNode firstLabel = null;
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode prevNode = insnNode.getPrevious();
          AbstractInsnNode twoPrevNodes = null;
          if (prevNode != null) twoPrevNodes = prevNode.getPrevious();

          if (insnNode.getNext() == null) continue;

          if (prevNode == null || twoPrevNodes == null) continue;

          if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
              && prevNode.getOpcode() == Opcodes.LDC
              && prevNode instanceof LdcInsnNode
              && ((LdcInsnNode) prevNode).cst instanceof String
              && ((String) ((LdcInsnNode) prevNode).cst).equals("")
              && twoPrevNodes.getOpcode() == Opcodes.AALOAD) {
            methodNode.instructions.insert(prevNode, new InsnNode(Opcodes.RETURN));
            methodNode.instructions.insert(
                prevNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "redrawMenuHook",
                    "(Ljava/lang/Object;IILjava/lang/String;Ljava/lang/String;)V"));
            methodNode.instructions.insert(prevNode, new InsnNode(Opcodes.AALOAD));
            methodNode.instructions.insert(prevNode, new VarInsnNode(Opcodes.ILOAD, 6));
            methodNode.instructions.insert(
                prevNode, new FieldInsnNode(Opcodes.GETSTATIC, "ac", "x", "[Ljava/lang/String;"));
            methodNode.instructions.insert(prevNode, new InsnNode(Opcodes.AALOAD));
            methodNode.instructions.insert(prevNode, new VarInsnNode(Opcodes.ILOAD, 6));
            methodNode.instructions.insert(
                prevNode, new FieldInsnNode(Opcodes.GETSTATIC, "lb", "ac", "[Ljava/lang/String;"));
            methodNode.instructions.insert(prevNode, new VarInsnNode(Opcodes.ILOAD, 6));
            methodNode.instructions.insert(prevNode, new VarInsnNode(Opcodes.ILOAD, 5));
            methodNode.instructions.insert(
                prevNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "zh", "Lwb;"));
            methodNode.instructions.insert(prevNode, new VarInsnNode(Opcodes.ALOAD, 0));
            continue;
          }
        }
      }
      // hook onto (windowed) server message hook
      if (methodNode.name.equals("l") && methodNode.desc.equals("(B)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          if (insnNode.getOpcode() == Opcodes.PUTSTATIC) {
            methodNode.instructions.insert(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "serverMessageHook",
                    "(Ljava/lang/String;)V"));
            methodNode.instructions.insert(
                insnNode,
                new FieldInsnNode(Opcodes.GETFIELD, "client", "Cj", "Ljava/lang/String;"));
            methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            break;
          }
        }
      }
      if (methodNode.name.equals("p") && methodNode.desc.equals("(I)V")) {
        // Login panel hook
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode prevNode = insnNode.getPrevious();

          if (prevNode == null) continue;

          if (insnNode.getOpcode() == Opcodes.ILOAD
              && ((VarInsnNode) insnNode).var == 1
              && prevNode.getOpcode() == Opcodes.GETFIELD
              && ((FieldInsnNode) prevNode).owner.equals("client")
              && ((FieldInsnNode) prevNode).name.equals("ge")) {
            methodNode.instructions.insertBefore(prevNode, new VarInsnNode(Opcodes.ILOAD, 1));
            methodNode.instructions.insertBefore(
                prevNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/AccountManagement",
                    "panel_welcome_hook",
                    "(I)V",
                    false));
            break;
          }
        }

        // Another hook to get added "I've lost my password"
        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
              && nextNode.getOpcode() == Opcodes.PUTFIELD
              && ((FieldInsnNode) nextNode).owner.equals("client")
              && ((FieldInsnNode) nextNode).name.equals("Xi")) {
            AbstractInsnNode call = nextNode.getNext();
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 1));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 2));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/AccountManagement",
                    "panel_login_hook",
                    "(II)V",
                    false));
            break;
          }
        }

        // Add text if server is free and non veterans
        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode call;

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.ACONST_NULL
              && nextNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) nextNode).var == 3) {
            call = insnNode;

            LabelNode label = new LabelNode();

            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                call, new FieldInsnNode(Opcodes.GETFIELD, "client", "Pg", "Z"));
            methodNode.instructions.insertBefore(call, new JumpInsnNode(Opcodes.IFNE, label));
            methodNode.instructions.insertBefore(
                call, new LdcInsnNode("You need an account to use this server"));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ASTORE, 3));
            methodNode.instructions.insertBefore(call, label);

            break;
          }
        }

        // Hook reference of control text of server type
        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode targetNode;
          AbstractInsnNode call;

          if (insnNode.getOpcode() == Opcodes.ACONST_NULL) {

            targetNode = insnNode;
            while (targetNode.getOpcode() != Opcodes.INVOKEVIRTUAL
                || !((MethodInsnNode) targetNode).desc.equals("(ZBIILjava/lang/String;I)I")) {
              // find the method of addText
              targetNode = targetNode.getNext();
            }
            targetNode = targetNode.getNext();

            methodNode.instructions.insert(
                targetNode,
                new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "controlServerType", "I"));
            methodNode.instructions.insertBefore(targetNode, new IntInsnNode(Opcodes.BIPUSH, 0));

            break;
          }
        }
      }
      if (methodNode.name.equals("k") && methodNode.desc.equals("(I)V")) {
        // pre-game display hook
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNodes = nextNode.getNext();

          if (nextNode == null || twoNextNodes == null) break;

          if (insnNode.getOpcode() == Opcodes.ICONST_2
              && nextNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) nextNode).var == 0
              && twoNextNodes.getOpcode() == Opcodes.GETFIELD
              && ((FieldInsnNode) twoNextNodes).name.equals("Xd")) {
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/AccountManagement", "pregame_hook", "()V", false));
          }
        }
      }
      if (methodNode.name.equals("b")
          && methodNode.desc.equals("(BLjava/lang/String;Ljava/lang/String;)V")) {
        // show server response status text
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.ICONST_2
              && nextNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) nextNode).var == 0) {
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 3));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 2));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/AccountManagement",
                    "response_display_hook",
                    "(Ljava/lang/String;Ljava/lang/String;)V",
                    false));
          }
        }
      }
      if (methodNode.name.equals("a") && methodNode.desc.equals("(BI)V")) {
        // Handle key press hook (not logged in)
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNode = nextNode.getNext();

          if (nextNode == null || twoNextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) insnNode).var == 0
              && nextNode.getOpcode() == Opcodes.GETFIELD
              && ((FieldInsnNode) nextNode).name.equals("Xd")
              && twoNextNode.getOpcode() == Opcodes.ICONST_M1) {
            methodNode.instructions.insertBefore(insnNode, new IntInsnNode(Opcodes.BIPUSH, -12));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 2));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/AccountManagement",
                    "account_panels_key_hook",
                    "(II)V",
                    false));
            break;
          }
        }

        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNode = nextNode.getNext();
          AbstractInsnNode findNode, nextFindNode, call;
          LabelNode labelNode, exitNode;

          if (nextNode == null || twoNextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) insnNode).var == 0
              && nextNode.getOpcode() == Opcodes.GETFIELD
              && ((FieldInsnNode) nextNode).name.equals("qg")
              && twoNextNode.getOpcode() == Opcodes.ICONST_M1) {

            findNode = twoNextNode;
            while (findNode.getOpcode() != Opcodes.IF_ICMPEQ) {
              // find part of checking is logged in
              findNode = findNode.getNext();
            }
            nextFindNode = findNode.getNext();

            labelNode = ((JumpInsnNode) findNode).label;
            exitNode = ((JumpInsnNode) nextFindNode).label;
            call = insnNode.getPrevious();

            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                call, new FieldInsnNode(Opcodes.GETFIELD, "client", "qg", "I"));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 2));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "gameKeyPressHook", "(II)I"));
            methodNode.instructions.insertBefore(
                insnNode, new JumpInsnNode(Opcodes.IFNE, exitNode));

            break;
          }
        }
      }
      if (methodNode.name.equals("x") && methodNode.desc.equals("(I)V")) {
        // Login button press hook, from login panel
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        AbstractInsnNode findNode = null;
        int count = 0;
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          if (insnNode.getOpcode() == Opcodes.PUTFIELD) {
            FieldInsnNode field = (FieldInsnNode) insnNode;
            if (count != 1
                && field.owner.equals("client")
                && field.name.equals("wh")
                && field.desc.equals("Ljava/lang/String;")) {
              findNode = insnNode.getNext();
              count++;
            }
          }
        }

        methodNode.instructions.insertBefore(
            findNode,
            new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "login_hook", "()V", false));

        // Lost password press hook, from login panel
        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNode = nextNode.getNext();
          LabelNode label = new LabelNode();

          if (nextNode == null || twoNextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.GETFIELD
              && ((FieldInsnNode) insnNode).name.equals("Ih")
              && nextNode.getOpcode() == Opcodes.BIPUSH
              && ((IntInsnNode) nextNode).operand == -88
              && twoNextNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {

            FrameNode call = (FrameNode) (twoNextNode.getNext().getNext());
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "loginOtherButtonCheckHook",
                    "()V",
                    false));
            break;
          }
        }

        // Register button press hook
        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNode = nextNode.getNext();

          if (nextNode == null || twoNextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) insnNode).var == 0
              && nextNode.getOpcode() == Opcodes.GETFIELD
              && ((FieldInsnNode) nextNode).name.equals("ge")
              && twoNextNode.getOpcode() == Opcodes.BIPUSH
              && ((IntInsnNode) twoNextNode).operand == -98) {
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/AccountManagement",
                    "welcome_new_user_hook",
                    "()V",
                    false));
          }
        }

        // Login button press hook, from welcome screen (don't clear out if save login info)
        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.PUTFIELD
              && ((FieldInsnNode) insnNode).name.equals("Xd")
              && Settings.SAVE_LOGININFO.get(Settings.currentProfile)) {

            methodNode.instructions.insertBefore(nextNode, new InsnNode(Opcodes.ICONST_0));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "keep_login_info_hook", "()V", false));
            methodNode.instructions.insertBefore(nextNode, new InsnNode(Opcodes.RETURN));
          }
        }

        // Non welcome screen hook
        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.IADD
              && nextNode.getOpcode() == Opcodes.PUTSTATIC
              && ((FieldInsnNode) nextNode).name.equals("Zi")) {
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Bb", "I"));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "xb", "I"));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Qb", "I"));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "I", "I"));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/AccountManagement",
                    "account_panels_input_hook",
                    "(IIII)V",
                    false));
          }
        }
      }
      if (methodNode.name.equals("b") && methodNode.desc.equals("(IZ)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        // move down original text "to change your contact details, etc" since should be 5 px down
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNode = nextNode.getNext();
          AbstractInsnNode findNode;

          if (nextNode == null || twoNextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.GETSTATIC
              && ((FieldInsnNode) insnNode).name.equals("il")
              && nextNode.getOpcode() == Opcodes.SIPUSH
              && ((IntInsnNode) nextNode).operand == 145) {
            findNode = insnNode.getPrevious().getPrevious();
            methodNode.instructions.insertBefore(findNode, new VarInsnNode(Opcodes.ILOAD, 7));
            methodNode.instructions.insertBefore(
                findNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "wrenchFixHook", "()I", false));
            methodNode.instructions.insertBefore(findNode, new InsnNode(Opcodes.IADD));
            methodNode.instructions.insertBefore(findNode, new VarInsnNode(Opcodes.ISTORE, 7));
            break;
          }
        }

        // correct the offset of clicking with previous text correction
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNode = nextNode.getNext();
          AbstractInsnNode findNode;

          if (nextNode == null || twoNextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.IINC
              && ((IincInsnNode) insnNode).var == 7
              && ((IincInsnNode) insnNode).incr == 15
              && nextNode.getOpcode() == Opcodes.IINC
              && ((IincInsnNode) nextNode).var == 7
              && ((IincInsnNode) nextNode).incr == 15
              && twoNextNode.getOpcode() == Opcodes.IINC
              && ((IincInsnNode) twoNextNode).var == 7
              && ((IincInsnNode) twoNextNode).incr == 15) {
            findNode = insnNode.getNext().getNext().getNext();
            methodNode.instructions.insertBefore(findNode, new VarInsnNode(Opcodes.ILOAD, 7));
            methodNode.instructions.insertBefore(
                findNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "wrenchFixHook", "()I", false));
            methodNode.instructions.insertBefore(findNode, new InsnNode(Opcodes.IADD));
            methodNode.instructions.insertBefore(findNode, new VarInsnNode(Opcodes.ISTORE, 7));
            break;
          }
        }

        // move up text "always logout when you finish" if in tutorial island
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode targetNode;

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.GETSTATIC
              && ((FieldInsnNode) insnNode).name.equals("il")
              && nextNode.getOpcode() == Opcodes.SIPUSH
              && ((IntInsnNode) nextNode).operand == 134) {
            targetNode = nextNode;
            while (targetNode.getOpcode() != Opcodes.IINC
                || ((IincInsnNode) targetNode).incr != 15) {
              // find the part of the += 15 jump inside skip tutorial
              targetNode = targetNode.getNext();
            }
            ((IincInsnNode) targetNode).incr = 10; // should have been 10 instead of 15
            break;
          }
        }

        // move up click pos "always logout when you finish" if in tutorial island
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode targetNode;

          if (insnNode.getOpcode() == Opcodes.IINC && ((IincInsnNode) insnNode).incr == 35) {
            targetNode = insnNode;
            while (targetNode.getOpcode() != Opcodes.IINC
                || ((IincInsnNode) targetNode).incr != 5) {
              // start section of click for skip tutorial
              targetNode = targetNode.getNext();
            }

            while (targetNode.getOpcode() != Opcodes.IINC
                || ((IincInsnNode) targetNode).incr != 15) {
              // end section of click for skip tutorial
              targetNode = targetNode.getNext();
            }
            ((IincInsnNode) targetNode).incr = 10; // should have been 10 instead of 15
            break;
          }
        }

        // bigger "Ypos" click area for options menu because when player in tutorial island, the
        // menu goes further down and wasn't
        // adapted since it was introduced
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          IntInsnNode targetNode;

          if (insnNode.getOpcode() == Opcodes.SIPUSH && ((IntInsnNode) insnNode).operand == -266) {
            targetNode = (IntInsnNode) insnNode;
            targetNode.operand = -286;
            break;
          }
        }

        // Options menu hook
        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode startNode;
          AbstractInsnNode targetNode;

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.SIPUSH && ((IntInsnNode) insnNode).operand == 145) {
            startNode = targetNode = insnNode;
            while (startNode.getOpcode() != Opcodes.ALOAD) {
              // find aload0, to insert jump to skip "To change your contact details,password,
              // recovery questions, etc..please select 'account management'"
              startNode = startNode.getPrevious();
            }

            while (targetNode.getOpcode() != Opcodes.SIPUSH
                || ((IntInsnNode) targetNode).operand != 139) {
              // find "Privacy settings. Will be applied to" keymarker to indicate block to jump to
              targetNode = targetNode.getNext();
            }
            // back off to find the corresponding aload0
            while (targetNode.getOpcode() != Opcodes.ALOAD) {
              targetNode = targetNode.getPrevious();
            }

            LabelNode label = new LabelNode();

            methodNode.instructions.insertBefore(
                startNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "showSecuritySettings", "()Z"));
            methodNode.instructions.insertBefore(startNode, new JumpInsnNode(Opcodes.IFGT, label));

            methodNode.instructions.insertBefore(targetNode, label);
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ILOAD, 6));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ILOAD, 7));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                targetNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "I", "I"));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                targetNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "xb", "I"));
            methodNode.instructions.insertBefore(
                targetNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/AccountManagement",
                    "options_security_hook",
                    "(IIII)I"));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ILOAD, 7));
            methodNode.instructions.insertBefore(targetNode, new InsnNode(Opcodes.IADD));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ISTORE, 7));

            break;
          }
        }

        // Options menu click hook
        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNode = nextNode.getNext();
          AbstractInsnNode startNode;
          AbstractInsnNode targetNode;
          AbstractInsnNode call;

          if (nextNode == null || twoNextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.IINC
              && ((IincInsnNode) insnNode).var == 7
              && ((IincInsnNode) insnNode).incr == 15
              && nextNode.getOpcode() == Opcodes.IINC
              && ((IincInsnNode) nextNode).var == 7
              && ((IincInsnNode) nextNode).incr == 15
              && twoNextNode.getOpcode() == Opcodes.IINC
              && ((IincInsnNode) twoNextNode).var == 7
              && ((IincInsnNode) twoNextNode).incr == 15) {
            // start part of for clicking "To change your contact details,password, recovery
            // questions, etc..please select 'account management'"
            startNode =
                targetNode =
                    insnNode.getPrevious(); // at this point got corrected with the 5px offset

            while (targetNode.getOpcode() != Opcodes.IINC
                || ((IincInsnNode) targetNode).incr != 35
                || targetNode.getNext().getOpcode() != Opcodes.ICONST_0) {
              // find part close to click "Privacy settings. Will be applied to"
              targetNode = targetNode.getNext();
            }
            targetNode = targetNode.getNext(); // iconst0

            LabelNode label = new LabelNode();

            methodNode.instructions.insertBefore(
                startNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "showSecuritySettings", "()Z"));
            methodNode.instructions.insertBefore(startNode, new JumpInsnNode(Opcodes.IFGT, label));

            methodNode.instructions.insertBefore(targetNode, label);
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ILOAD, 6));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ILOAD, 7));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                targetNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "I", "I"));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                targetNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "xb", "I"));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                targetNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Cf", "I"));
            methodNode.instructions.insertBefore(
                targetNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/AccountManagement",
                    "options_security_click_hook",
                    "(IIIII)I"));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ILOAD, 7));
            methodNode.instructions.insertBefore(targetNode, new InsnNode(Opcodes.IADD));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ISTORE, 7));

            break;
          }
        }
      }
      if (methodNode.name.equals("c") && methodNode.desc.equals("(B)V")) {
        // Hook inputPopupType >= 10 for drawInputPopup
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode cmpNode = nextNode;
          for (int i = 0; i < 3; i++) {
            if (cmpNode == null) break;
            cmpNode = cmpNode.getNext();
          }
          AbstractInsnNode targetNode;

          if (nextNode == null || cmpNode == null) break;

          if (insnNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) insnNode).var == 0
              && nextNode.getOpcode() == Opcodes.GETFIELD
              && ((FieldInsnNode) nextNode).name.equals("gc")
              && cmpNode.getOpcode() == Opcodes.BIPUSH
              && ((IntInsnNode) cmpNode).operand == -10) {
            // point after this.inputPopupType == 9 check
            targetNode = cmpNode.getNext().getNext();

            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                targetNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "gc", "I"));
            methodNode.instructions.insertBefore(targetNode, new VarInsnNode(Opcodes.ALOAD, 2));
            methodNode.instructions.insertBefore(
                targetNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "drawInputPopupHook",
                    "(ILjava/lang/String;)V"));
            break;
          }
        }
        // limit buy and sell x to 65535
        // 221 and 236 are the seeked on opcodes
        insnNodeList = methodNode.instructions.iterator();
        int[] opcodes = {221, 236};
        boolean exited = false;
        for (int i = 0; i < 2; i++) {
          while (insnNodeList.hasNext()) {
            AbstractInsnNode insnNode = insnNodeList.next();
            AbstractInsnNode nextNode = insnNode.getNext();
            AbstractInsnNode peekedNode, callNode;

            if (nextNode == null
                || ((peekedNode = nextNode.getNext().getNext().getNext()) == null)) {
              exited = true;
              break;
            }

            if (insnNode.getOpcode() == Opcodes.INVOKESTATIC
                && ((MethodInsnNode) insnNode).name.equals("parseInt")
                && ((MethodInsnNode) insnNode).desc.equals("(Ljava/lang/String;)I")
                && nextNode.getOpcode() == Opcodes.ISTORE
                && peekedNode.getOpcode() == Opcodes.SIPUSH
                && (((IntInsnNode) peekedNode).operand == opcodes[0]
                    || ((IntInsnNode) peekedNode).operand == opcodes[1])) {
              callNode = nextNode.getNext();
              methodNode.instructions.insertBefore(callNode, new VarInsnNode(Opcodes.ALOAD, 2));
              methodNode.instructions.insertBefore(
                  callNode,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC,
                      "Client/Util",
                      "boundUnsignedShort",
                      "(Ljava/lang/String;)I"));
              methodNode.instructions.insertBefore(callNode, new VarInsnNode(Opcodes.ISTORE, 4));
              continue;
            }
          }
          if (exited) {
            break;
          }
        }

        // fix the ArrayOutOfBounds exception that happens if shopindex = -1 (i.e. item focus lost)
        // and player tries the Buy or Sell X
        // 221 and 236 are the seeked on opcodes
        insnNodeList = methodNode.instructions.iterator();
        exited = false;
        for (int i = 0; i < 2; i++) {
          while (insnNodeList.hasNext()) {
            AbstractInsnNode insnNode = insnNodeList.next();
            AbstractInsnNode nextNode = insnNode.getNext();
            AbstractInsnNode currNode, targetNode, startNode;

            if (nextNode == null) {
              exited = true;
              break;
            }

            if (insnNode.getOpcode() == Opcodes.GETFIELD
                && ((FieldInsnNode) insnNode).name.equals("Jh")
                && ((FieldInsnNode) insnNode).desc.equals("Lda;")
                && nextNode.getOpcode() == Opcodes.SIPUSH
                && (((IntInsnNode) nextNode).operand == opcodes[0]
                    || ((IntInsnNode) nextNode).operand == opcodes[1])) {
              currNode = insnNode.getNext();

              while (currNode.getOpcode() != Opcodes.ILOAD || ((VarInsnNode) currNode).var != 3) {
                // find iload3, to insert jump to if the shopindex is -1
                currNode = currNode.getPrevious();
              }
              targetNode = currNode;

              while (currNode.getOpcode() != Opcodes.GETFIELD
                  || !((FieldInsnNode) currNode).owner.equals("client")
                  || !((FieldInsnNode) currNode).name.equals("Rj")) {
                // find client.Rj:int[]
                currNode = currNode.getPrevious();
              }
              currNode = currNode.getPrevious();
              startNode = currNode;

              LabelNode label = new LabelNode();

              methodNode.instructions.insertBefore(startNode, new InsnNode(Opcodes.ICONST_M1));
              methodNode.instructions.insertBefore(startNode, new VarInsnNode(Opcodes.ISTORE, 3));
              methodNode.instructions.insertBefore(startNode, new VarInsnNode(Opcodes.ALOAD, 0));
              methodNode.instructions.insertBefore(
                  startNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Di", "I"));
              methodNode.instructions.insertBefore(
                  startNode, new JumpInsnNode(Opcodes.IFLT, label));

              methodNode.instructions.insertBefore(targetNode, label);

              continue;
            }
          }
          if (exited) {
            break;
          }
        }
      }
      if (methodNode.name.equals("a") && methodNode.desc.equals("(ZI)V")) {
        // Disconnect hook (::closecon)
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.SIPUSH
              && ((IntInsnNode) insnNode).operand == -6924
              && nextNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
            // entry point when its true to close it
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "disconnect_hook", "()V", false));
            break;
          }
        }
      }
      if (methodNode.name.equals("s") && methodNode.desc.equals("(I)V")) {
        // bypass npc attack on left option, regardless of level difference if user wants it that
        // way
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode prevNode = insnNode.getPrevious();

          if (prevNode == null) continue;

          if (insnNode.getOpcode() == Opcodes.ILOAD
              && ((VarInsnNode) insnNode).var == 12
              && prevNode.getOpcode() == Opcodes.GETFIELD
              && ((FieldInsnNode) prevNode).owner.equals("ta")
              && ((FieldInsnNode) prevNode).name.equals("b")) {
            methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ILOAD, 12));
            methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ISTORE, 12));
            methodNode.instructions.insert(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "attack_menu_hook", "(I)I", false));
            break;
          }
        }
      }
      // add on info for objects
      if (methodNode.name.equals("s") && methodNode.desc.equals("(I)V")) {
        boolean foundExaminePos = false;

        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        AbstractInsnNode insnNode = insnNodeList.next();

        while (!foundExaminePos && insnNodeList.hasNext()) {
          insnNode = insnNodeList.next();
          if (insnNode.getOpcode() == Opcodes.SIPUSH && ((IntInsnNode) insnNode).operand == 3400) {
            foundExaminePos = true;
          }
        }
        if (foundExaminePos) {
          while (insnNodeList.hasNext()) {
            insnNode = insnNodeList.next();

            if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                && ((MethodInsnNode) insnNode).name.equals("toString")) {
              // id
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 10));
              // direction
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "bg", "[I"));
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 9));
              methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.IALOAD));
              // x
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Se", "[I"));
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 9));
              methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.IALOAD));
              // y
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "ye", "[I"));
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 9));
              methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.IALOAD));
              // mark as scenery by inserting Game/MouseText.SCENERY
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/MouseText", "SCENERY", "I"));
              methodNode.instructions.insertBefore(
                  insnNode,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC,
                      "Game/Client",
                      "appendDetailsHook",
                      "(IIIII)Ljava/lang/String;",
                      false));
              methodNode.instructions.insertBefore(
                  insnNode,
                  new MethodInsnNode(
                      Opcodes.INVOKEVIRTUAL,
                      "java/lang/StringBuilder",
                      "append",
                      "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                      false));
              break;
            }
          }
        }
      }
      // add on info for wall objects
      if (methodNode.name.equals("s") && methodNode.desc.equals("(I)V")) {
        boolean foundExaminePos = false;

        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        AbstractInsnNode insnNode = insnNodeList.next();

        while (!foundExaminePos && insnNodeList.hasNext()) {
          insnNode = insnNodeList.next();
          if (insnNode.getOpcode() == Opcodes.SIPUSH && ((IntInsnNode) insnNode).operand == 3300) {
            foundExaminePos = true;
          }
        }
        if (foundExaminePos) {
          while (insnNodeList.hasNext()) {
            insnNode = insnNodeList.next();

            if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                && ((MethodInsnNode) insnNode).name.equals("toString")) {
              // id
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 10));
              // direction
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Hj", "[I"));
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 9));
              methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.IALOAD));
              // x
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Jd", "[I"));
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 9));
              methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.IALOAD));
              // y
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
              methodNode.instructions.insertBefore(
                  insnNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "yk", "[I"));
              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 9));
              methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.IALOAD));
              // mark as boundary by inserting Game/MouseText.BOUNDARY
              methodNode.instructions.insertBefore(
                  insnNode,
                  new FieldInsnNode(Opcodes.GETSTATIC, "Game/MouseText", "BOUNDARY", "I"));
              methodNode.instructions.insertBefore(
                  insnNode,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC,
                      "Game/Client",
                      "appendDetailsHook",
                      "(IIIII)Ljava/lang/String;",
                      false));
              methodNode.instructions.insertBefore(
                  insnNode,
                  new MethodInsnNode(
                      Opcodes.INVOKEVIRTUAL,
                      "java/lang/StringBuilder",
                      "append",
                      "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                      false));
              break;
            }
          }
        }
      }
      if (methodNode.name.equals("a") && methodNode.desc.equals("(ILjava/lang/String;)V")) {
        // hook onto sound effect played
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          if (insnNode.getOpcode() == Opcodes.PUTSTATIC) {
            methodNode.instructions.insert(
                insnNode,
                new FieldInsnNode(
                    Opcodes.PUTSTATIC, "Game/Client", "lastSoundEffect", "Ljava/lang/String;"));
            methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ALOAD, 2));

            insnNode = insnNodeList.next();
            LabelNode label = new LabelNode();
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Replay", "isSeeking", "Z"));
            methodNode.instructions.insertBefore(insnNode, new JumpInsnNode(Opcodes.IFEQ, label));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.RETURN));
            methodNode.instructions.insertBefore(insnNode, label);
            break;
          }
        }
      }
      // drawGame
      if (methodNode.name.equals("f") && methodNode.desc.equals("(I)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        AbstractInsnNode insnNode = insnNodeList.next();
        LabelNode label = new LabelNode();
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Replay", "isSeeking", "Z"));
        methodNode.instructions.insertBefore(insnNode, new JumpInsnNode(Opcodes.IFEQ, label));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.RETURN));
        methodNode.instructions.insertBefore(insnNode, label);
      }

      // drawTextBox
      if (methodNode.name.equals("a")
          && methodNode.desc.equals("(Ljava/lang/String;BLjava/lang/String;)V")) {
        // hook onto sound effect played
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        AbstractInsnNode insnNode = insnNodeList.next();
        LabelNode label = new LabelNode();
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_0));
        methodNode.instructions.insertBefore(
            insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Replay", "isRestarting", "Z"));
        methodNode.instructions.insertBefore(insnNode, new JumpInsnNode(Opcodes.IFEQ, label));
        methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.RETURN));
        methodNode.instructions.insertBefore(insnNode, label);
      }

      if (methodNode.name.equals("e") && methodNode.desc.equals("(I)V")) {
        // handleGameInput
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        AbstractInsnNode insnNode = insnNodeList.next();
        methodNode.instructions.insertBefore(
            insnNode,
            new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "update", "()V", false));
      }

      if (methodNode.name.equals("a") && methodNode.desc.equals("(IIBZIIIIZ)Z")) {
        // hook into walk to source
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.IINC
              && ((IincInsnNode) insnNode).var == 10
              && ((IincInsnNode) insnNode).incr == -1) {

            VarInsnNode call = (VarInsnNode) insnNode.getNext();

            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 2));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 1));
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "walkSourceHook", "(II)V"));
            break;
          }
        }
      }
      if (methodNode.name.equals("e") && methodNode.desc.equals("(B)V")) {
        // reset login screen vars method
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.PUTFIELD
              && ((FieldInsnNode) insnNode).owner.equals("client")
              && ((FieldInsnNode) insnNode).name.equals("wh")) {

            FieldInsnNode call = (FieldInsnNode) insnNode.getNext();

            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "resetLoginHook", "()V", false));
            break;
          }
        }
      }
      if (methodNode.name.equals("t") && methodNode.desc.equals("(I)V")) {
        // appearance panel
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode findNode;

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.GETSTATIC
              && ((FieldInsnNode) insnNode).name.equals("il")
              && nextNode.getOpcode() == Opcodes.BIPUSH
              && ((IntInsnNode) nextNode).operand == 91) {

            findNode = nextNode;

            for (int i = 0; i < 3; i++) {
              // node of iload3 (ypos)
              findNode = findNode.getNext();
            }

            AbstractInsnNode call = (MethodInsnNode) findNode;
            int offset = Settings.PATCH_GENDER.get(Settings.currentProfile) ? -8 : 0;

            methodNode.instructions.insertBefore(call, new IntInsnNode(Opcodes.BIPUSH, offset));
            methodNode.instructions.insertBefore(call, new InsnNode(Opcodes.IADD));

            while (findNode.getOpcode() != Opcodes.POP) {
              // in case of patch need one additional addText, see below
              findNode = findNode.getNext();
            }

            call = (VarInsnNode) findNode.getNext();

            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                call, new FieldInsnNode(Opcodes.GETFIELD, "client", "Af", "Lqa;"));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 4));
            methodNode.instructions.insertBefore(call, new InsnNode(Opcodes.INEG));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 2));
            methodNode.instructions.insertBefore(call, new InsnNode(Opcodes.IADD));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 3));

            // see if addTextTo should be added, i.e. "gender" is patched
            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "patch_gender_hook",
                    "(Ljava/lang/Object;II)V",
                    false));
          }
        }
      }

      if (methodNode.name.equals("I") && methodNode.desc.equals("(I)V")) {
        // menu ui
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode findNode;
          LabelNode targetNode;
          LabelNode labelNode;

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.INVOKESPECIAL
              && ((MethodInsnNode) insnNode).name.equals("d")
              && ((MethodInsnNode) insnNode).desc.equals("(B)V")
              && nextNode.getOpcode() == Opcodes.ILOAD
              && ((VarInsnNode) nextNode).var == 4) {
            findNode = nextNode.getNext();
            targetNode = ((JumpInsnNode) findNode).label;

            // find last else to insert else if
            while (findNode.getOpcode() != Opcodes.ICONST_1
                || findNode.getNext().getOpcode() != Opcodes.ISTORE) {
              findNode = findNode.getNext();
            }

            labelNode = new LabelNode();
            methodNode.instructions.insertBefore(findNode, labelNode);

            methodNode.instructions.insertBefore(
                labelNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "shouldShowTextInputDialog", "()Z"));
            methodNode.instructions.insertBefore(
                labelNode, new JumpInsnNode(Opcodes.IFEQ, labelNode));
            methodNode.instructions.insertBefore(labelNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                labelNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "I", "I"));
            methodNode.instructions.insertBefore(labelNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                labelNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "xb", "I"));
            methodNode.instructions.insertBefore(labelNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                labelNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "Cf", "I"));
            methodNode.instructions.insertBefore(
                labelNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "drawTextInputDialogMouseHook",
                    "(III)V",
                    false));
            methodNode.instructions.insertBefore(
                labelNode, new JumpInsnNode(Opcodes.GOTO, targetNode));
          }
        }
      }

      if (methodNode.name.equals("n") && methodNode.desc.equals("(B)V")) {
        // bug-fix of remove-x in resizable
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode call;
          LabelNode altNode, contNode;

          if (insnNode.getOpcode() == Opcodes.GETSTATIC
              && ((FieldInsnNode) insnNode).owner.equals("ua")
              && ((FieldInsnNode) insnNode).name.equals("Kb")) {
            call = insnNode.getNext();

            altNode = new LabelNode();
            contNode = new LabelNode();

            methodNode.instructions.insertBefore(call, altNode);
            methodNode.instructions.insertBefore(
                call,
                new FieldInsnNode(
                    Opcodes.GETSTATIC,
                    "Game/Client",
                    "items_remove_message",
                    "[Ljava/lang/String;"));
            methodNode.instructions.insertBefore(call, contNode);

            // new reference to add in patch
            call = insnNode.getNext();
            methodNode.instructions.insertBefore(call, new JumpInsnNode(Opcodes.IFNULL, altNode));
            methodNode.instructions.insertBefore(
                call, new FieldInsnNode(Opcodes.GETSTATIC, "ua", "Kb", "[Ljava/lang/String;"));
            methodNode.instructions.insertBefore(call, new JumpInsnNode(Opcodes.GOTO, contNode));
            break;
          }
        }
      }

      if (methodNode.name.equals("A") && methodNode.desc.equals("(I)V")) {
        // drawChatMessageTabs to allow wiki integration
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          // check if chat tabs should be changed
          AbstractInsnNode findNode = methodNode.instructions.getFirst();
          methodNode.instructions.insertBefore(
              findNode,
              new MethodInsnNode(
                  Opcodes.INVOKESTATIC, "Game/Client", "checkChatTabs", "()V", false));
          break;
        }

        insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode startNode, targetNode;

          if (insnNode.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) insnNode).var == 0) {
            LabelNode label = new LabelNode();
            startNode = targetNode = insnNode;

            while (targetNode.getOpcode() != Opcodes.INVOKEVIRTUAL) {
              // find this.surface.drawSprite(...)
              targetNode = targetNode.getNext();
            }
            targetNode = targetNode.getNext();

            methodNode.instructions.insertBefore(
                startNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "displayAltTabsHook", "()Z"));
            methodNode.instructions.insertBefore(startNode, new JumpInsnNode(Opcodes.IFGT, label));

            methodNode.instructions.insertBefore(targetNode, label);
            break;
          }
        }

        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNodes = nextNode.getNext();
          AbstractInsnNode startNode, targetNode;

          if (nextNode == null || twoNextNodes == null) break;

          if (insnNode.getOpcode() == Opcodes.GETSTATIC
              && ((FieldInsnNode) insnNode).name.equals("il")
              && nextNode.getOpcode() == Opcodes.BIPUSH
              && ((IntInsnNode) nextNode).operand == 120
              && twoNextNodes.getOpcode() == Opcodes.AALOAD) {

            LabelNode label = new LabelNode();
            // back off to find the corresponding aload0
            startNode = insnNode;
            while (startNode.getOpcode() != Opcodes.ALOAD) {
              startNode = startNode.getPrevious();
            }

            targetNode = insnNode;
            while (targetNode.getOpcode() != Opcodes.INVOKEVIRTUAL) {
              // find this.surface.drawstringCenter(...)
              targetNode = targetNode.getNext();
            }
            targetNode = targetNode.getNext();

            methodNode.instructions.insertBefore(
                startNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "hideReportAbuseHook", "()Z"));
            methodNode.instructions.insertBefore(startNode, new JumpInsnNode(Opcodes.IFGT, label));

            methodNode.instructions.insertBefore(targetNode, label);
            break;
          }
        }
      }

      if (methodNode.name.equals("D") && methodNode.desc.equals("(I)V")) {
        // hook to control random minimap rotation
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode call;
          FieldInsnNode field;
          if (insnNode.getOpcode() == Opcodes.PUTFIELD) {
            field = (FieldInsnNode) insnNode;
            call = insnNode;
            if (field.owner.equals("client")
                && (field.name.equals("Df") || field.name.equals("sd"))) {
              methodNode.instructions.insertBefore(
                  call,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC, "Game/Client", "minimapRotation", "(I)I", false));
            }
          }
        }
      }

      if (methodNode.name.equals("m") && methodNode.desc.equals("(B)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNodes = nextNode.getNext();
          AbstractInsnNode findNode;

          if (nextNode == null || twoNextNodes == null) break;

          if (insnNode.getOpcode() == Opcodes.GETSTATIC
              && ((FieldInsnNode) insnNode).name.equals("il")
              && nextNode.getOpcode() == Opcodes.BIPUSH
              && ((IntInsnNode) nextNode).operand == 103
              && twoNextNodes.getOpcode() == Opcodes.AALOAD) {
            // find and copy over index.dat to memory
            findNode = insnNode;
            while (findNode.getOpcode() != Opcodes.ASTORE) {
              // find ASTORE 3, the reference where the index.dat byte array is
              findNode = findNode.getNext();
            }
            findNode = findNode.getNext();
            methodNode.instructions.insertBefore(findNode, new VarInsnNode(Opcodes.ALOAD, 3));
            methodNode.instructions.insertBefore(
                findNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "cloneMediaIndex", "([B)V"));
            break;
          }
        }

        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();
          AbstractInsnNode twoNextNodes = nextNode.getNext();
          AbstractInsnNode startNode, targetNode;

          if (nextNode == null || twoNextNodes == null) break;

          if (insnNode.getOpcode() == Opcodes.GETSTATIC
              && ((FieldInsnNode) insnNode).name.equals("il")
              && nextNode.getOpcode() == Opcodes.BIPUSH
              && ((IntInsnNode) nextNode).operand == 97
              && twoNextNodes.getOpcode() == Opcodes.AALOAD) {

            LabelNode label = new LabelNode();
            startNode = insnNode;
            while (startNode.getOpcode() != Opcodes.INVOKEVIRTUAL) {
              // find this.surface.parseSprite(...)
              startNode = startNode.getNext();
            }
            startNode = startNode.getNext();

            targetNode = startNode;

            methodNode.instructions.insertBefore(startNode, new InsnNode(Opcodes.ICONST_1));
            methodNode.instructions.insertBefore(
                startNode,
                new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "drawOldChatTabs", "(Z)Z"));
            methodNode.instructions.insertBefore(startNode, new JumpInsnNode(Opcodes.IFLE, label));

            methodNode.instructions.insertBefore(startNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                startNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "li", "Lba;"));
            methodNode.instructions.insertBefore(startNode, new IntInsnNode(Opcodes.BIPUSH, 23));
            methodNode.instructions.insertBefore(startNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                startNode, new FieldInsnNode(Opcodes.GETFIELD, "client", "tg", "I"));
            methodNode.instructions.insertBefore(startNode, new InsnNode(Opcodes.IADD));
            methodNode.instructions.insertBefore(startNode, new InsnNode(Opcodes.ICONST_1));
            methodNode.instructions.insertBefore(
                startNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Client", "readDataOldChatTabs", "()[B"));
            methodNode.instructions.insertBefore(startNode, new IntInsnNode(Opcodes.BIPUSH, 104));
            methodNode.instructions.insertBefore(startNode, new VarInsnNode(Opcodes.ALOAD, 3));
            methodNode.instructions.insertBefore(
                startNode,
                new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "ba", "a", "(II[BI[B)V", false));

            methodNode.instructions.insertBefore(targetNode, label);

            break;
          }
        }
      }

      // draw bank interface
      if (methodNode.name.equals("r") && methodNode.desc.equals("(I)V")) {
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          // reset bank drawn flag
          if (insnNode.getOpcode() == Opcodes.PUTFIELD
              && ((FieldInsnNode) insnNode).owner.equals("client")
              && ((FieldInsnNode) insnNode).name.equals("Fe")
              && ((FieldInsnNode) insnNode).desc.equals("Z")) {
            insnNode = insnNode.getNext();
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_0));
            methodNode.instructions.insertBefore(
                insnNode,
                new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "bank_interface_drawn", "Z"));

            break;
          }
        }

        // set bank drawn flag
        insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
              && ((MethodInsnNode) insnNode).owner.equals("ba")
              && ((MethodInsnNode) insnNode).name.equals("b")
              && ((MethodInsnNode) insnNode).desc.equals("(IIIIB)V")) {
            insnNode = insnNodeList.next();

            LabelNode skipLabel = new LabelNode();
            methodNode.instructions.insertBefore(
                insnNode,
                new FieldInsnNode(Opcodes.GETSTATIC, "Game/Client", "bank_interface_drawn", "Z"));
            methodNode.instructions.insertBefore(
                insnNode, new JumpInsnNode(Opcodes.IFGT, skipLabel));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_1));
            methodNode.instructions.insertBefore(
                insnNode,
                new FieldInsnNode(Opcodes.PUTSTATIC, "Game/Client", "bank_interface_drawn", "Z"));
            methodNode.instructions.insertBefore(insnNode, skipLabel);

            break;
          }
        }
      }

      // hookTracer(node, methodNode);
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

        LabelNode label = new LabelNode();
        methodNode.instructions.insertBefore(findNode, new InsnNode(Opcodes.ICONST_0));
        methodNode.instructions.insertBefore(
            findNode, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Replay", "isSeeking", "Z"));
        methodNode.instructions.insertBefore(findNode, new JumpInsnNode(Opcodes.IFEQ, label));
        methodNode.instructions.insertBefore(findNode, new InsnNode(Opcodes.RETURN));
        methodNode.instructions.insertBefore(findNode, label);

        while (findNode.getOpcode() != Opcodes.POP) {
          findNode = findNode.getNext();
          if (findNode == null) {
            Logger.Error("Unable to find present hook");
            break;
          }
        }

        while (findNode.getOpcode() != Opcodes.INVOKESPECIAL) {
          if (findNode.getOpcode() == Opcodes.GETFIELD) imageNode = (FieldInsnNode) findNode;

          AbstractInsnNode prev = findNode.getPrevious();
          methodNode.instructions.remove(findNode);
          findNode = prev;
        }

        methodNode.instructions.insert(
            findNode,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC, "Game/Renderer", "present", "(Ljava/awt/Image;)V", false));
        methodNode.instructions.insert(
            findNode,
            new FieldInsnNode(Opcodes.GETFIELD, node.name, imageNode.name, imageNode.desc));
        methodNode.instructions.insert(findNode, new VarInsnNode(Opcodes.ALOAD, 0));
      }
      if (methodNode.name.equals("a") && methodNode.desc.equals("(IILjava/lang/String;IIBI)V")) {
        AbstractInsnNode start = methodNode.instructions.getFirst();

        // Option to remove @ran@ effect
        LabelNode defaultRanColorLabel = new LabelNode();
        LabelNode skipRanColorLabel = new LabelNode();

        while (start != null) {
          if (start.getOpcode() == Opcodes.LDC) {
            LdcInsnNode ldcNode = (LdcInsnNode) start;

            if (ldcNode.cst instanceof Double && (double) ldcNode.cst == 1.6777215E7) {
              methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ILOAD, 5));
              methodNode.instructions.insertBefore(
                  start,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC,
                      "Client/RanOverrideEffect",
                      "getRanEffectOverrideColour",
                      "(I)I",
                      false));
              methodNode.instructions.insertBefore(
                  start, new JumpInsnNode(Opcodes.GOTO, skipRanColorLabel));
              methodNode.instructions.insert(
                  start.getNext().getNext().getNext(), skipRanColorLabel);

              break;
            }
          }

          start = start.getNext();
        }

        start = methodNode.instructions.getFirst();

        while (start != null) {
          if (start.getOpcode() == Opcodes.ALOAD
              && start.getNext().getOpcode() == Opcodes.ILOAD
              && start.getNext().getNext().getOpcode() == Opcodes.INVOKEVIRTUAL
              && start.getNext().getNext().getNext().getOpcode() == Opcodes.ISTORE) {
            break;
          }

          start = start.getNext();
        }
        start = start.getPrevious();

        LabelNode finishLabel = ((JumpInsnNode) start.getPrevious().getPrevious()).label;
        LabelNode failLabel = new LabelNode();

        methodNode.instructions.insertBefore(start, new IntInsnNode(Opcodes.BIPUSH, 126));
        methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ALOAD, 3));
        methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ILOAD, 10));
        methodNode.instructions.insertBefore(
            start, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"));
        methodNode.instructions.insertBefore(start, new JumpInsnNode(Opcodes.IF_ICMPNE, failLabel));

        methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ILOAD, 10));
        methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.ICONST_5));
        methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.IADD));
        methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ALOAD, 3));
        methodNode.instructions.insertBefore(
            start, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I"));
        methodNode.instructions.insertBefore(start, new JumpInsnNode(Opcodes.IF_ICMPGE, failLabel));

        methodNode.instructions.insertBefore(start, new IntInsnNode(Opcodes.BIPUSH, 126));
        methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ALOAD, 3));
        methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ILOAD, 10));
        methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.ICONST_5));
        methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.IADD));
        methodNode.instructions.insertBefore(
            start, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C"));
        methodNode.instructions.insertBefore(start, new JumpInsnNode(Opcodes.IF_ICMPNE, failLabel));

        methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ALOAD, 3));
        methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ILOAD, 10));
        methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.ICONST_1));
        methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.IADD));
        methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ILOAD, 10));
        methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.ICONST_5));
        methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.IADD));
        methodNode.instructions.insertBefore(
            start,
            new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;"));
        methodNode.instructions.insertBefore(
            start,
            new MethodInsnNode(
                Opcodes.INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I"));
        methodNode.instructions.insertBefore(start, new VarInsnNode(Opcodes.ISTORE, 4));
        methodNode.instructions.insertBefore(start, new IincInsnNode(10, 5));

        methodNode.instructions.insertBefore(start, new JumpInsnNode(Opcodes.GOTO, finishLabel));

        methodNode.instructions.insertBefore(start, failLabel);
      }
      if (methodNode.name.equals("a") && methodNode.desc.equals("(ILjava/lang/String;IIII)V")) {
        // method hook for drawstringCenter, reserved testing
      }
      if (methodNode.name.equals("a") && methodNode.desc.equals("(Z)V")) {
        AbstractInsnNode start = methodNode.instructions.getFirst();
        while (start != null) {
          if (start.getOpcode() == Opcodes.ICONST_0
                  && start.getPrevious().getOpcode() == Opcodes.IINC
              || start.getOpcode() == Opcodes.ICONST_0
                  && start.getPrevious().getOpcode() == Opcodes.ILOAD) {
            methodNode.instructions.insertBefore(
                start,
                new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Renderer", "getClearColor", "()I"));
            start = start.getNext();
            methodNode.instructions.remove(start.getPrevious());
            continue;
          }

          start = start.getNext();
        }
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

            if (nextNode == null) break;

            if (insnNode.getOpcode() == Opcodes.ALOAD && nextNode.getOpcode() == Opcodes.ICONST_0) {
              VarInsnNode call = (VarInsnNode) insnNode;
              Logger.Info("Patching validation...");

              methodNode.instructions.insert(
                  insnNode,
                  new MethodInsnNode(
                      Opcodes.INVOKEVIRTUAL, "java/util/Random", "nextBytes", "([B)V"));
              methodNode.instructions.insert(insnNode, new VarInsnNode(Opcodes.ALOAD, 2));
              methodNode.instructions.insert(
                  insnNode,
                  new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/Random", "<init>", "()V"));
              methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.DUP));
              methodNode.instructions.insert(
                  insnNode, new TypeInsnNode(Opcodes.NEW, "java/util/Random"));
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
          methodNode.instructions.insertBefore(
              insnNode,
              new MethodInsnNode(
                  Opcodes.INVOKESTATIC,
                  "Game/GameApplet",
                  "cacheURLHook",
                  "(Ljava/net/URL;)Ljava/net/URL;"));
          methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ASTORE, 0));
        }
        if (methodNode.desc.equals("(Z)V")) {
          // Disconnect hook (::lostcon)
          Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
          AbstractInsnNode insnNode = insnNodeList.next();
          methodNode.instructions.insertBefore(
              insnNode,
              new MethodInsnNode(
                  Opcodes.INVOKESTATIC, "Game/Client", "disconnect_hook", "()V", false));
        }
        if (methodNode.desc.equals("([BIII)V")) {
          // dump whole input stream!
          Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
          while (insnNodeList.hasNext()) {
            AbstractInsnNode insnNode = insnNodeList.next();
            AbstractInsnNode nextNode = insnNode.getNext();

            // entry point
            if (insnNode.getOpcode() == Opcodes.ILOAD
                && ((VarInsnNode) insnNode).var == 5
                && nextNode.getOpcode() == Opcodes.ILOAD
                && ((VarInsnNode) nextNode).var == 7) {
              VarInsnNode call = (VarInsnNode) insnNode;
              methodNode.instructions.insertBefore(
                  call, new VarInsnNode(Opcodes.ALOAD, 1)); // byte[]
              methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 2)); // n
              methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 3)); // n2
              // methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 4)); //
              // n3
              // methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 6)); //
              // n4
              methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ILOAD, 5)); // n5
              methodNode.instructions.insertBefore(
                  call, new VarInsnNode(Opcodes.ILOAD, 7)); // bytes read
              methodNode.instructions.insertBefore(
                  call,
                  new MethodInsnNode(
                      Opcodes.INVOKESTATIC, "Game/Replay", "dumpRawInputStream", "([BIIII)V"));
              break;
            }
          }
        }
      }
      if (methodNode.name.equals("run") && methodNode.desc.equals("()V")) {

        // dump whole output stream!
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          // entry point
          if (insnNode.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) insnNode).var == 0
              && nextNode.getOpcode() == Opcodes.GETFIELD
              && ((FieldInsnNode) nextNode).name.equals("Q")
              && ((FieldInsnNode) nextNode).desc.equals("Ljava/io/OutputStream;")) {
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "da", "Y", "[B"));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 2));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 1));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC, "Game/Replay", "dumpRawOutputStream", "([BII)V"));
            break;
          }
        }
      }
    }
  }

  private void patchTracer(ClassNode node) {
    Logger.Info("Patching tracer (" + node.name + ".class)");

    Iterator<MethodNode> methodNodeList = node.methods.iterator();
    while (methodNodeList.hasNext()) {
      MethodNode methodNode = methodNodeList.next();

      hookTracer(node, methodNode);
    }
  }

  private void patchRendererHelper(ClassNode node) {
    Logger.Info("Patching renderer helper (" + node.name + ".class)");

    Iterator<MethodNode> methodNodeList = node.methods.iterator();
    while (methodNodeList.hasNext()) {
      MethodNode methodNode = methodNodeList.next();

      // Renderbug fix
      if (methodNode.name.equals("b") && methodNode.desc.equals("(IZ)V")) {
        LabelNode skipLabel = new LabelNode();

        AbstractInsnNode start = methodNode.instructions.getFirst();
        while (start != null) {
          if (start.getOpcode() == Opcodes.GETFIELD
              && ((FieldInsnNode) start).owner.equals("lb")
              && ((FieldInsnNode) start).name.equals("D")) {
            AbstractInsnNode insnNode = start.getPrevious();

            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "k", "e", "J"));
            methodNode.instructions.insertBefore(insnNode, new LdcInsnNode(1072741824L));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.LCMP));
            methodNode.instructions.insertBefore(
                insnNode, new JumpInsnNode(Opcodes.IFLE, skipLabel));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.LCONST_0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.PUTSTATIC, "k", "e", "J"));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "jb", "o", "I"));
            methodNode.instructions.insertBefore(
                insnNode, new IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_LONG));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.PUTFIELD, "lb", "D", "[J"));

            methodNode.instructions.insertBefore(insnNode, new LdcInsnNode("RENDERBUG SQUASHED"));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Client/Logger",
                    "Debug",
                    "(Ljava/lang/String;)V",
                    false));

            methodNode.instructions.insertBefore(insnNode, skipLabel);

            break;
          }

          start = start.getNext();
        }
      }

      if (methodNode.name.equals("c") && methodNode.desc.equals("(I)V")) {
        // Throwable crash patch - a condition of indexoutbounds was reported on this method
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();
          AbstractInsnNode nextNode = insnNode.getNext();

          if (nextNode == null) break;

          if (insnNode.getOpcode() == Opcodes.INVOKESTATIC
              && nextNode.getOpcode() == Opcodes.ATHROW) {
            int index = ExceptionSignatures.size();
            ExceptionSignatures.add(node.name + "." + methodNode.name + methodNode.desc);
            methodNode.instructions.insertBefore(nextNode, new IntInsnNode(Opcodes.SIPUSH, index));
            methodNode.instructions.insertBefore(
                nextNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "CrashFixRoutine",
                    "(Ljava/lang/Throwable;I)V"));
            methodNode.instructions.insertBefore(nextNode, new InsnNode(Opcodes.RETURN));
            methodNode.instructions.remove(nextNode);
          }
        }
      }
      if (methodNode.name.equals("a") && methodNode.desc.equals("(IIIIIIII)V")) {
        AbstractInsnNode start = methodNode.instructions.getFirst();
        while (start != null) {
          if (start.getOpcode() == Opcodes.PUTFIELD
              && start.getPrevious().getOpcode() == Opcodes.IADD) {
            FieldInsnNode insnNode = (FieldInsnNode) start;
            if (insnNode.name.equals("o")) {
              methodNode.instructions.insertBefore(
                  start, new FieldInsnNode(Opcodes.GETSTATIC, "Game/Camera", "offset_height", "I"));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.INEG));
              methodNode.instructions.insertBefore(start, new InsnNode(Opcodes.IADD));
            }
          }

          start = start.getNext();
        }
      }

      if (methodNode.name.equals("c") && methodNode.desc.equals("(I)V")) {
        AbstractInsnNode start = methodNode.instructions.getFirst();
        while (start != null) {
          if (start.getOpcode() == Opcodes.IASTORE
              && start.getPrevious().getOpcode() == Opcodes.IADD
              && start.getPrevious().getPrevious().getOpcode() == Opcodes.IDIV) {
            methodNode.instructions.insertBefore(
                start.getPrevious(),
                new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Renderer", "getFogColor", "(II)I"));
            methodNode.instructions.remove(start.getPrevious());
          }

          start = start.getNext();
        }
      }

      // Set camera routine
      if (methodNode.name.equals("a") && methodNode.desc.equals("(IIIIIIII)V")) {
        Logger.Info("patching setCamera()");

        AbstractInsnNode findNode = methodNode.instructions.getLast();
        methodNode.instructions.insertBefore(
            findNode,
            new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Camera", "postSetCamera", "()V", false));
      }
    }
  }

  private void patchSoundHelper(ClassNode node) {
    Logger.Info("Patching sound helper (" + node.name + ".class)");

    Iterator<MethodNode> methodNodeList = node.methods.iterator();
    while (methodNodeList.hasNext()) {
      MethodNode methodNode = methodNodeList.next();

      if (methodNode.name.equals("a")
          && methodNode.desc.equals("(Lc;Ljava/awt/Component;II)Lsa;")) {
        // Hook loadSounds to keep same gameContainer reference when called on subsequent loadSound
        Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();

        while (insnNodeList.hasNext()) {
          AbstractInsnNode insnNode = insnNodeList.next();

          if (insnNode.getOpcode() == Opcodes.GETSTATIC) {
            AbstractInsnNode call = insnNode;

            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 1));

            methodNode.instructions.insertBefore(
                call,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "getAndSetSoundGameContainer",
                    "(Ljava/awt/Component;)Ljava/awt/Component;",
                    false));
            methodNode.instructions.insertBefore(call, new VarInsnNode(Opcodes.ASTORE, 1));

            break;
          }
        }
      }
    }
  }

  private void patchRightClickMenu(ClassNode node) {
    Logger.Info("Patching right-click menu (" + node.name + ".class)");

    Iterator<MethodNode> methodNodeList = node.methods.iterator();
    while (methodNodeList.hasNext()) {
      MethodNode methodNode = methodNodeList.next();

      // Hook right-click menu draw
      if (methodNode.name.equals("a") && methodNode.desc.equals("(IIIIIZ)I")) {
        AbstractInsnNode start = methodNode.instructions.getFirst();

        // Highlighting items in the right-click menu
        while (start != null) {
          if (start.getOpcode() == Opcodes.DUP) {
            LabelNode skipLabel = new LabelNode();

            AbstractInsnNode insnNode = start;
            insnNode = insnNode.getPrevious().getPrevious().getPrevious();

            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ACONST_NULL));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ASTORE, 13));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Client/Settings",
                    "updateInjectedVariables",
                    "()V",
                    false));
            methodNode.instructions.insertBefore(
                insnNode,
                new FieldInsnNode(
                    Opcodes.GETSTATIC, "Client/Settings", "HIGHLIGHT_ITEMS_MENU_BOOL", "Z"));
            methodNode.instructions.insertBefore(
                insnNode, new JumpInsnNode(Opcodes.IFEQ, skipLabel));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "wb", "b", "[Lt;"));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 10));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "t", "o", "Ljava/lang/String;"));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ASTORE, 14));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "wb", "b", "[Lt;"));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 10));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.AALOAD));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "t", "p", "Ljava/lang/String;"));
            methodNode.instructions.insertBefore(insnNode, new LdcInsnNode("Take"));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/String",
                    "equals",
                    "(Ljava/lang/Object;)Z",
                    false));
            methodNode.instructions.insertBefore(
                insnNode, new JumpInsnNode(Opcodes.IFEQ, skipLabel));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 14));
            methodNode.instructions.insertBefore(insnNode, new LdcInsnNode("@lre@"));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/String",
                    "startsWith",
                    "(Ljava/lang/String;)Z",
                    false));
            methodNode.instructions.insertBefore(
                insnNode, new JumpInsnNode(Opcodes.IFEQ, skipLabel));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 14));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Renderer",
                    "getHighlightColour",
                    "(Ljava/lang/String;)Ljava/lang/Integer;",
                    false));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.DUP));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ASTORE, 13));
            methodNode.instructions.insertBefore(
                insnNode, new JumpInsnNode(Opcodes.IFNULL, skipLabel));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 14));
            methodNode.instructions.insertBefore(insnNode, new LdcInsnNode("@lre@"));
            methodNode.instructions.insertBefore(insnNode, new LdcInsnNode(""));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/String",
                    "replaceFirst",
                    "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                    false));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ASTORE, 14));
            methodNode.instructions.insertBefore(insnNode, new LdcInsnNode("Take "));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Client",
                    "calcStringLength",
                    "(Ljava/lang/String;)I",
                    false));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ISTORE, 15));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "wb", "t", "Lba;"));
            methodNode.instructions.insertBefore(insnNode, new LdcInsnNode("Take "));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 4));
            methodNode.instructions.insertBefore(insnNode, new IntInsnNode(Opcodes.BIPUSH, -2));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ISUB));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 11));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_0));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "wb", "i", "I"));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL, "ba", "a", "(Ljava/lang/String;IIIZI)V", false));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "wb", "t", "Lba;"));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 14));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 4));
            methodNode.instructions.insertBefore(insnNode, new IntInsnNode(Opcodes.BIPUSH, -2));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ISUB));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 15));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.IADD));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 8));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 13));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.ICONST_0));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "wb", "i", "I"));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL, "ba", "a", "(Ljava/lang/String;IIIZI)V", false));
            methodNode.instructions.insertBefore(insnNode, skipLabel);
            LabelNode skipLabel2 = new LabelNode();
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 13));
            methodNode.instructions.insertBefore(
                insnNode, new JumpInsnNode(Opcodes.IFNONNULL, skipLabel2));
            AbstractInsnNode insnNode2 = start;
            for (int i = 0; i < 26; i++) insnNode2 = insnNode2.getNext();
            methodNode.instructions.insertBefore(insnNode2, skipLabel2);

            break;
          }

          start = start.getNext();
        }

        start = methodNode.instructions.getFirst();

        // Set right click menu bounds
        while (start != null) {
          if (start.getOpcode() == Opcodes.SIPUSH && ((IntInsnNode) start).operand == 160) {
            AbstractInsnNode insnNode = start;
            insnNode = insnNode.getPrevious().getPrevious();

            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 4));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ILOAD, 3));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "wb", "D", "I"));
            methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.insertBefore(
                insnNode, new FieldInsnNode(Opcodes.GETFIELD, "wb", "I", "I"));
            methodNode.instructions.insertBefore(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Game/Renderer",
                    "setRightClickMenuBounds",
                    "(IIII)V",
                    false));

            break;
          }

          start = start.getNext();
        }
      }
    }
  }

  private void patchSoundPlayerJava(ClassNode node) {
    Logger.Info("Patching java sound player (" + node.name + ".class)");

    Iterator<MethodNode> methodNodeList = node.methods.iterator();
    while (methodNodeList.hasNext()) {
      MethodNode methodNode = methodNodeList.next();

      if (Settings.FIX_SFX_DELAY.get(Settings.currentProfile)) {
        if (methodNode.name.equals("b") && methodNode.desc.equals("(I)V")) {
          AbstractInsnNode start = methodNode.instructions.getFirst();

          while (start != null) {
            if (start.getOpcode() == Opcodes.INVOKEINTERFACE) {
              AbstractInsnNode insnNode = start;

              methodNode.instructions.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 0));
              methodNode.instructions.insertBefore(
                  insnNode,
                  new FieldInsnNode(
                      Opcodes.GETFIELD, "pb", "y", "Ljavax/sound/sampled/AudioFormat;"));
              // 0.1s delay * 22050 (sample rate) = 2205 samples * 16 bits = 35280 / 8 = 4410 bytes
              methodNode.instructions.insertBefore(insnNode, new IntInsnNode(Opcodes.SIPUSH, 4410));
              methodNode.instructions.insertBefore(
                  insnNode,
                  new MethodInsnNode(
                      Opcodes.INVOKEINTERFACE,
                      "javax/sound/sampled/SourceDataLine",
                      "open",
                      "(Ljavax/sound/sampled/AudioFormat;I)V",
                      true));
              methodNode.instructions.remove(insnNode);

              break;
            }

            start = start.getNext();
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
  private void hookClassVariable(
      MethodNode methodNode,
      String owner,
      String var,
      String desc,
      String newClass,
      String newVar,
      String newDesc,
      boolean canRead,
      boolean canWrite) {
    Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
    while (insnNodeList.hasNext()) {
      AbstractInsnNode insnNode = insnNodeList.next();

      int opcode = insnNode.getOpcode();
      if (opcode == Opcodes.GETFIELD || opcode == Opcodes.PUTFIELD) {
        FieldInsnNode field = (FieldInsnNode) insnNode;
        if (field.owner.equals(owner) && field.name.equals(var) && field.desc.equals(desc)) {
          if (opcode == Opcodes.GETFIELD && canWrite) {
            methodNode.instructions.insert(
                insnNode, new FieldInsnNode(Opcodes.GETSTATIC, newClass, newVar, newDesc));
            methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.POP));
          } else if (opcode == Opcodes.PUTFIELD && canRead) {
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.DUP_X1));
            methodNode.instructions.insert(
                insnNode, new FieldInsnNode(Opcodes.PUTSTATIC, newClass, newVar, newDesc));
          }
        }
      }
    }
  }

  private void hookTracer(ClassNode node, MethodNode methodNode) {
    // Tracer function, called on every instruction
    Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
    while (insnNodeList.hasNext()) {
      AbstractInsnNode insnNode = insnNodeList.next();

      if (insnNode.getType() == AbstractInsnNode.FRAME
          || insnNode.getType() == AbstractInsnNode.LABEL) continue;

      int index = InstructionBytecode.size();
      String instruction = decodeInstruction(insnNode).replaceAll("\n", "").replaceAll("    ", "");
      instruction = node.name + "." + methodNode.name + methodNode.desc + ": " + instruction;
      InstructionBytecode.add(instruction);
      methodNode.instructions.insertBefore(
          insnNode, new IntInsnNode(Opcodes.SIPUSH, (index >> 16) & 0xFFFF));
      methodNode.instructions.insertBefore(
          insnNode, new IntInsnNode(Opcodes.SIPUSH, index & 0xFFFF));
      methodNode.instructions.insertBefore(
          insnNode,
          new MethodInsnNode(Opcodes.INVOKESTATIC, "Game/Client", "TracerHandler", "(II)V"));
    }
  }

  private void hookConditionalClassVariable(
      MethodNode methodNode,
      String owner,
      String var,
      String desc,
      String newClass,
      String newVar,
      String newDesc,
      boolean canRead,
      boolean canWrite,
      String boolTrigger) {
    Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
    while (insnNodeList.hasNext()) {
      AbstractInsnNode insnNode = insnNodeList.next();

      int opcode = insnNode.getOpcode();
      if (opcode == Opcodes.GETFIELD || opcode == Opcodes.PUTFIELD) {
        FieldInsnNode field = (FieldInsnNode) insnNode;
        if (field.owner.equals(owner) && field.name.equals(var) && field.desc.equals(desc)) {
          if (opcode == Opcodes.GETFIELD && canWrite) {
            LabelNode label = new LabelNode();
            methodNode.instructions.insert(insnNode, label);
            methodNode.instructions.insert(
                insnNode, new FieldInsnNode(Opcodes.GETSTATIC, newClass, newVar, newDesc));
            methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.POP));
            methodNode.instructions.insert(insnNode, new JumpInsnNode(Opcodes.IFEQ, label));
            methodNode.instructions.insert(
                insnNode,
                new FieldInsnNode(Opcodes.GETSTATIC, "Client/Settings", boolTrigger, "Z"));
            methodNode.instructions.insert(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Client/Settings",
                    "updateInjectedVariables",
                    "()V",
                    false));
          } else if (opcode == Opcodes.PUTFIELD && canRead) {
            LabelNode label_end = new LabelNode();
            LabelNode label = new LabelNode();
            methodNode.instructions.insertBefore(insnNode, new InsnNode(Opcodes.DUP_X1));
            methodNode.instructions.insert(insnNode, label_end);
            methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.POP));
            methodNode.instructions.insert(insnNode, label);
            methodNode.instructions.insert(insnNode, new JumpInsnNode(Opcodes.GOTO, label_end));
            methodNode.instructions.insert(
                insnNode, new FieldInsnNode(Opcodes.PUTSTATIC, newClass, newVar, newDesc));
            methodNode.instructions.insert(insnNode, new JumpInsnNode(Opcodes.IFEQ, label));
            methodNode.instructions.insert(
                insnNode,
                new FieldInsnNode(Opcodes.GETSTATIC, "Client/Settings", boolTrigger, "Z"));
            methodNode.instructions.insert(
                insnNode,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "Client/Settings",
                    "updateInjectedVariables",
                    "()V",
                    false));
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
  private void hookStaticVariable(
      MethodNode methodNode,
      String owner,
      String var,
      String desc,
      String newClass,
      String newVar,
      String newDesc) {
    Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
    while (insnNodeList.hasNext()) {
      AbstractInsnNode insnNode = insnNodeList.next();

      int opcode = insnNode.getOpcode();
      if (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC) {
        FieldInsnNode field = (FieldInsnNode) insnNode;
        if (field.owner.equals(owner) && field.name.equals(var) && field.desc.equals(desc)) {
          field.owner = newClass;
          field.name = newVar;
          field.desc = newDesc;
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
  private void hookStaticVariableClone(
      MethodNode methodNode,
      String owner,
      String var,
      String desc,
      String newClass,
      String newVar,
      String newDesc) {
    Iterator<AbstractInsnNode> insnNodeList = methodNode.instructions.iterator();
    while (insnNodeList.hasNext()) {
      AbstractInsnNode insnNode = insnNodeList.next();

      int opcode = insnNode.getOpcode();
      if (opcode == Opcodes.PUTSTATIC) {
        FieldInsnNode field = (FieldInsnNode) insnNode;
        if (field.owner.equals(owner) && field.name.equals(var) && field.desc.equals(desc)) {
          methodNode.instructions.insertBefore(field, new InsnNode(Opcodes.DUP));
          methodNode.instructions.insert(
              field, new FieldInsnNode(Opcodes.PUTSTATIC, newClass, newVar, newDesc));
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
        writer.write(
            decodeAccess(fieldNode.access) + fieldNode.desc + " " + fieldNode.name + ";\n");
      }

      writer.write("\n");

      Iterator<MethodNode> methodNodeList = node.methods.iterator();
      while (methodNodeList.hasNext()) {
        MethodNode methodNode = methodNodeList.next();
        writer.write(
            decodeAccess(methodNode.access) + methodNode.name + " " + methodNode.desc + ":\n");

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

    if ((access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) res += "public ";
    if ((access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE) res += "private ";
    if ((access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED) res += "protected ";

    if ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) res += "static ";
    if ((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) res += "final ";
    if ((access & Opcodes.ACC_VOLATILE) == Opcodes.ACC_VOLATILE) res += "protected ";
    if ((access & Opcodes.ACC_SYNCHRONIZED) == Opcodes.ACC_SYNCHRONIZED) res += "synchronized ";
    if ((access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT) res += "abstract ";
    if ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE) res += "interface ";

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

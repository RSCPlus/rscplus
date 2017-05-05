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
import java.nio.file.Files;
import javax.xml.bind.DatatypeConverter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class RSC_Recovery {
	
	public static void main(String[] args) {
		Settings.initDir();
		
		File folder = new File(Settings.Dir.CACHE);
		File[] files = folder.listFiles();
		
		File manifest = new File("recovery/META-INF/MANIFEST.MF");
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(manifest));
			writer.write("Manifest-Version: 1.0\n");
			writer.write("\n");
		} catch (Exception e) {
		}
		
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String filename = file.getName();
			String class_name = "null";
			String sha_digest = "null";
			
			sha_digest = DatatypeConverter.printBase64Binary(Util.hexStringByte(filename));
			
			try {
				byte[] data = Files.readAllBytes(file.toPath());
				ClassReader reader = new ClassReader(data);
				ClassNode node = new ClassNode();
				reader.accept(node, ClassReader.SKIP_DEBUG);
				class_name = node.name + ".class";
				
				File dump = new File("recovery/" + class_name);
				Files.write(dump.toPath(), data);
				
				Logger.Info("Filename: " + filename);
				Logger.Info("Class Name: " + class_name);
				Logger.Info("SHA1-Digest: " + sha_digest);
				
				writer.write("Name: " + class_name + "\n");
				writer.write("SHA1-Digest: " + sha_digest + "\n");
				writer.write("\n");
			} catch (Exception e) {
				Logger.Error("Failed to recover '" + filename + "'");
			}
		}
		
		try {
			writer.close();
		} catch (Exception e) {
		}
	}
}

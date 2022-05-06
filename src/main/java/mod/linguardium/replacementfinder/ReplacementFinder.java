package mod.linguardium.replacementfinder;

import com.google.gson.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagManagerLoader;
import net.minecraft.util.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public class ReplacementFinder implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("replacementfinder");
	private static Gson g = new GsonBuilder().setPrettyPrinting().create();
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		for (ModContainer container: FabricLoader.getInstance().getAllMods()) {
			container.findPath("data").ifPresent(dataPath->{
				try {
					processNameSpaces(container.getMetadata().getName(),dataPath);
				} catch (IOException e) {
					// Errors dont actually matter
				}
			});
		}
		LOGGER.info("Hello Fabric world!");
	}
	private static void processNameSpaces(String modName, Path folder) throws IOException {
		for (Path fName : Files.list(folder).toList()) {
			if (fName.equals(folder))
				continue;
			if(Files.isDirectory(fName)) {
				processFolder(modName, fName);
			}
		}
	}
	private static void processFolder(String modName, Path namespaceFolder) throws IOException {
		Path tagsFolder = namespaceFolder.resolve("tags");
		if (Files.exists(tagsFolder)){
			Files.find(tagsFolder,15,(path,attributes)->path.toString().endsWith(".json")).forEach(path->{
				processJson(modName,namespaceFolder,path);
			});
		}

	}
	private static void processJson(String modName, Path namespaceFolder, Path file) {

		try {
			String jsonString = new String(Files.readAllBytes(file));
			JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
			if (JsonHelper.getBoolean(json, "replace", false)) {
				LOGGER.info("mod [{}] replaces data tag {}:{}",modName,namespaceFolder.getFileName(),namespaceFolder.resolve("tags").relativize(file));
			}
		} catch (IOException e) {
			LOGGER.warn("failed to process {}",file.getFileName());
			throw new RuntimeException(e);
		}
	}
}

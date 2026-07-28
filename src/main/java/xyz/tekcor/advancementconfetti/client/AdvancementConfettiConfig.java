package xyz.tekcor.advancementconfetti.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;

public final class AdvancementConfettiConfig {
	private static boolean normalEnabled = true;
	private static boolean rareEnabled = true;

	private AdvancementConfettiConfig() {
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("advancementconfetti.txt");
	}

	public static boolean normalEnabled() {
		return normalEnabled;
	}

	public static boolean rareEnabled() {
		return rareEnabled;
	}

	public static void setNormalEnabled(boolean value) {
		normalEnabled = value;
		save();
	}

	public static void setRareEnabled(boolean value) {
		rareEnabled = value;
		save();
	}

	public static void load() {
		Path path = file();

		if (!Files.isRegularFile(path)) {
			save();
			return;
		}

		try {
			for (String line : Files.readAllLines(path)) {
				String trimmed = line.trim();

				if (trimmed.isEmpty() || trimmed.startsWith("#")) {
					continue;
				}

				String[] parts = trimmed.split("=", 2);

				if (parts.length != 2) {
					continue;
				}

				String key = parts[0].trim();
				boolean value = Boolean.parseBoolean(parts[1].trim());

				if (key.equals("normal")) {
					normalEnabled = value;
				} else if (key.equals("rare")) {
					rareEnabled = value;
				}
			}
		} catch (IOException e) {
			AdvancementConfettiClient.LOGGER.warn("Could not read {}, using defaults", path, e);
		}
	}

	private static void save() {
		Path path = file();

		try {
			Files.createDirectories(path.getParent());
			Files.write(path, List.of(
					"# Toggle each celebration type on/off. Change in game with /advancementconfetti or via Mod Menu.",
					"normal=" + normalEnabled,
					"rare=" + rareEnabled));
		} catch (IOException e) {
			AdvancementConfettiClient.LOGGER.error("Could not write {}", path, e);
		}
	}
}

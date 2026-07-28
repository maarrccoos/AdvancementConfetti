package xyz.tekcor.advancementconfetti.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

public final class ScreenshotCapture {
	private ScreenshotCapture() {
	}

	public static File galleryDir() {
		return FabricLoader.getInstance().getGameDir().resolve("advancementconfetti").toFile();
	}

	public static File screenshotDir() {
		return new File(galleryDir(), "screenshots");
	}

	public static void capture(boolean rare, String advancementTitle) {
		Path dir = screenshotDir().toPath();

		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			AdvancementConfettiClient.LOGGER.error("Could not create {}", dir, e);
			return;
		}

		Minecraft client = Minecraft.getInstance();
		String fileName = System.currentTimeMillis() + "_" + (rare ? "rare" : "normal")
				+ "_" + sanitize(advancementTitle) + ".png";

		Screenshot.grab(galleryDir(), fileName, client.getMainRenderTarget(), 1, message ->
				AdvancementConfettiClient.LOGGER.info("{}", message.getString()));
	}

	public static String titleOf(String fileName) {
		String name = fileName;
		int dot = name.lastIndexOf('.');

		if (dot > 0) {
			name = name.substring(0, dot);
		}

		String[] parts = name.split("_", 3);
		return parts.length < 3 || parts[2].isBlank() ? name : parts[2];
	}

	public static boolean isRare(String fileName) {
		String[] parts = fileName.split("_", 3);
		return parts.length > 1 && parts[1].equals("rare");
	}

	private static String sanitize(String raw) {
		String cleaned = raw.replaceAll("[\\\\/:*?\"<>|_]", " ").trim();
		cleaned = cleaned.replaceAll("\\s+", " ");

		if (cleaned.length() > 80) {
			cleaned = cleaned.substring(0, 80).trim();
		}

		return cleaned.isEmpty() ? "advancement" : cleaned;
	}
}

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

	public static void capture(boolean rare, String advancementId) {
		Path dir = screenshotDir().toPath();

		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			AdvancementConfettiClient.LOGGER.error("Could not create {}", dir, e);
			return;
		}

		Minecraft client = Minecraft.getInstance();
		String fileName = System.currentTimeMillis() + "_" + (rare ? "rare" : "normal")
				+ "_" + sanitize(advancementId) + ".png";

		Screenshot.grab(galleryDir(), fileName, client.getMainRenderTarget(), 1, message ->
				AdvancementConfettiClient.LOGGER.info("{}", message.getString()));
	}

	private static String sanitize(String raw) {
		String cleaned = raw.replaceAll("[\\\\/:*?\"<>|]", "_");
		return cleaned.isEmpty() ? "advancement" : cleaned;
	}
}

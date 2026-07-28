package xyz.tekcor.advancementconfetti.client.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import xyz.tekcor.advancementconfetti.client.AdvancementConfettiClient;
import xyz.tekcor.advancementconfetti.client.ScreenshotCapture;

public class GalleryScreen extends Screen {
	private static final int PAGE_SIZE = 9;
	private static final int COLUMNS = 3;
	private static final int THUMB_SIZE = 64;
	private static final int GAP = 12;

	private final Screen parent;
	private File[] files = new File[0];
	private int page;
	private final List<Thumbnail> thumbnails = new ArrayList<>();

	public GalleryScreen(Screen parent) {
		super(Component.literal("Advancement Gallery"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		File dir = ScreenshotCapture.screenshotDir();
		File[] found = dir.listFiles((d, name) -> name.toLowerCase(Locale.ROOT).endsWith(".png"));
		this.files = found == null ? new File[0] : found;
		Arrays.sort(this.files, Comparator.comparing(File::getName).reversed());

		this.page = Math.min(this.page, maxPage());

		buildPage();

		this.addRenderableWidget(Button.builder(Component.literal("< Prev"), b -> changePage(-1))
				.bounds(this.width / 2 - 130, this.height - 30, 60, 20)
				.build());

		this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
				.bounds(this.width / 2 - 50, this.height - 30, 100, 20)
				.build());

		this.addRenderableWidget(Button.builder(Component.literal("Next >"), b -> changePage(1))
				.bounds(this.width / 2 + 70, this.height - 30, 60, 20)
				.build());
	}

	private int maxPage() {
		return this.files.length == 0 ? 0 : (this.files.length - 1) / PAGE_SIZE;
	}

	private void changePage(int delta) {
		int next = Math.max(0, Math.min(maxPage(), this.page + delta));

		if (next == this.page) {
			return;
		}

		this.page = next;

		for (Thumbnail thumbnail : this.thumbnails) {
			thumbnail.releaseTexture();
		}

		this.clearWidgets();
		init();
	}

	private void buildPage() {
		this.thumbnails.clear();

		int start = this.page * PAGE_SIZE;
		int end = Math.min(this.files.length, start + PAGE_SIZE);

		int gridWidth = COLUMNS * THUMB_SIZE + (COLUMNS - 1) * GAP;
		int startX = this.width / 2 - gridWidth / 2;
		int startY = 50;

		for (int i = start; i < end; i++) {
			int slot = i - start;
			int col = slot % COLUMNS;
			int row = slot / COLUMNS;

			int x = startX + col * (THUMB_SIZE + GAP);
			int y = startY + row * (THUMB_SIZE + GAP);

			File file = this.files[i];
			Thumbnail thumbnail = new Thumbnail(x, y, THUMB_SIZE, THUMB_SIZE, file, this);
			this.thumbnails.add(thumbnail);
			this.addRenderableWidget(thumbnail);
		}
	}

	void openFullView(File file) {
		Minecraft.getInstance().setScreen(new ImageViewScreen(this, file));
	}

	@Override
	public void onClose() {
		for (Thumbnail thumbnail : this.thumbnails) {
			thumbnail.releaseTexture();
		}

		Minecraft.getInstance().setScreen(this.parent);
	}

	@Override
	public boolean isPauseScreen() {
		return this.parent == null;
	}

	private static final class Thumbnail extends AbstractWidget {
		private final File file;
		private final GalleryScreen gallery;
		private Identifier textureId;
		private boolean loaded;

		Thumbnail(int x, int y, int width, int height, File file, GalleryScreen gallery) {
			super(x, y, width, height, Component.literal(file.getName()));
			this.file = file;
			this.gallery = gallery;
		}

		private void ensureLoaded() {
			if (this.loaded) {
				return;
			}

			this.loaded = true;

			try (InputStream in = new FileInputStream(this.file)) {
				NativeImage image = NativeImage.read(in);
				DynamicTexture texture = new DynamicTexture(() -> "advancementconfetti-thumb", image);

				this.textureId = Identifier.fromNamespaceAndPath(
						"advancementconfetti", "gallery/" + this.file.getName().toLowerCase(Locale.ROOT).hashCode());

				Minecraft.getInstance().getTextureManager().register(this.textureId, texture);
			} catch (IOException e) {
				AdvancementConfettiClient.LOGGER.warn("Could not load thumbnail {}", this.file, e);
			}
		}

		void releaseTexture() {
			if (this.textureId != null) {
				Minecraft.getInstance().getTextureManager().release(this.textureId);
				this.textureId = null;
			}
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			ensureLoaded();

			if (this.textureId != null) {
				graphics.blit(this.textureId,
						this.getX(), this.getY(),
						this.getX() + this.getWidth(), this.getY() + this.getHeight(),
						0.0F, 1.0F, 0.0F, 1.0F);
			} else {
				graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xFF404040);
			}

			graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0xFFAAAAAA);
		}

		@Override
		public void onClick(MouseButtonEvent event, boolean doubleClick) {
			this.gallery.openFullView(this.file);
		}

		@Override
		protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
			this.defaultButtonNarrationText(output);
		}
	}
}

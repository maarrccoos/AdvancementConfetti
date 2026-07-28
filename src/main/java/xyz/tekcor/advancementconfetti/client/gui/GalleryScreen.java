package xyz.tekcor.advancementconfetti.client.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import xyz.tekcor.advancementconfetti.client.AdvancementConfettiClient;
import xyz.tekcor.advancementconfetti.client.ScreenshotCapture;

public class GalleryScreen extends Screen {
	private static final int COLUMNS = 3;
	private static final int ROWS = 3;
	private static final int PAGE_SIZE = COLUMNS * ROWS;

	private static final int THUMB_WIDTH = 84;
	private static final int THUMB_HEIGHT = 48;
	private static final int LABEL_HEIGHT = 11;
	private static final int GAP = 9;

	private static final int SELECTED_BORDER = 0xFF55FF55;
	private static final int NORMAL_BORDER = 0xFF808080;
	private static final int RARE_BORDER = 0xFFD9A521;
	private static final int TEXT_DIM = 0xFFBBBBBB;
	private static final int TEXT_SELECTED = 0xFF55FF55;

	private final Screen parent;
	private File[] files = new File[0];
	private int page;
	private final List<Thumbnail> thumbnails = new ArrayList<>();
	private final Set<String> selected = new HashSet<>();
	private Button deleteButton;

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

		this.page = Math.max(0, Math.min(this.page, maxPage()));

		buildPage();

		int bottom = this.height - 26;
		int centreX = this.width / 2;

		this.addRenderableWidget(Button.builder(Component.literal("<"), b -> changePage(-1))
				.bounds(centreX - 152, bottom, 24, 20)
				.build());

		this.deleteButton = this.addRenderableWidget(
				Button.builder(deleteLabel(), b -> deleteSelected())
						.bounds(centreX - 124, bottom, 100, 20)
						.build());

		this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
				.bounds(centreX - 20, bottom, 60, 20)
				.build());

		this.addRenderableWidget(Button.builder(Component.literal("Open Folder"), b -> openFolder())
				.bounds(centreX + 44, bottom, 84, 20)
				.build());

		this.addRenderableWidget(Button.builder(Component.literal(">"), b -> changePage(1))
				.bounds(centreX + 132, bottom, 24, 20)
				.build());

		refreshDeleteButton();
	}

	private Component deleteLabel() {
		return this.selected.isEmpty()
				? Component.literal("Delete")
				: Component.literal("Delete (" + this.selected.size() + ")");
	}

	private void refreshDeleteButton() {
		if (this.deleteButton != null) {
			this.deleteButton.setMessage(deleteLabel());
			this.deleteButton.active = !this.selected.isEmpty();
		}
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
		rebuild();
	}

	private void rebuild() {
		releaseTextures();
		this.clearWidgets();
		init();
	}

	private void buildPage() {
		this.thumbnails.clear();

		int start = this.page * PAGE_SIZE;
		int end = Math.min(this.files.length, start + PAGE_SIZE);

		int cellHeight = THUMB_HEIGHT + LABEL_HEIGHT;
		int gridWidth = COLUMNS * THUMB_WIDTH + (COLUMNS - 1) * GAP;
		int startX = this.width / 2 - gridWidth / 2;
		int startY = 40;

		for (int i = start; i < end; i++) {
			int slot = i - start;
			int column = slot % COLUMNS;
			int row = slot / COLUMNS;

			int x = startX + column * (THUMB_WIDTH + GAP);
			int y = startY + row * (cellHeight + GAP);

			Thumbnail thumbnail = new Thumbnail(x, y, this.files[i], this);
			this.thumbnails.add(thumbnail);
			this.addRenderableWidget(thumbnail);
		}
	}

	private void toggleSelected(File file) {
		if (!this.selected.remove(file.getName())) {
			this.selected.add(file.getName());
		}

		refreshDeleteButton();
	}

	private boolean isSelected(File file) {
		return this.selected.contains(file.getName());
	}

	private void deleteSelected() {
		if (this.selected.isEmpty()) {
			return;
		}

		releaseTextures();

		for (File file : this.files) {
			if (!this.selected.contains(file.getName())) {
				continue;
			}

			if (!file.delete()) {
				AdvancementConfettiClient.LOGGER.warn("Could not delete {}", file);
			}
		}

		this.selected.clear();
		this.clearWidgets();
		init();
	}

	private void openFolder() {
		Util.getPlatform().openPath(ScreenshotCapture.screenshotDir().toPath());
	}

	void openFullView(File file) {
		releaseTextures();
		Minecraft.getInstance().setScreen(new ImageViewScreen(this, file));
	}

	private void releaseTextures() {
		for (Thumbnail thumbnail : this.thumbnails) {
			thumbnail.releaseTexture();
		}
	}

	@Override
	public void onClose() {
		releaseTextures();
		Minecraft.getInstance().setScreen(this.parent);
	}

	@Override
	public boolean isPauseScreen() {
		return this.parent == null;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		graphics.text(this.font, this.title,
				this.width / 2 - this.font.width(this.title) / 2, 16, 0xFFFFFFFF);

		if (this.files.length == 0) {
			Component empty = Component.literal("Nothing here yet. Go earn an advancement.");
			graphics.text(this.font, empty, this.width / 2 - this.font.width(empty) / 2, 70, TEXT_DIM);
			return;
		}

		Component info = Component.literal(
				"Page " + (this.page + 1) + "/" + (maxPage() + 1)
						+ "   ·   click to select, double click to view");
		graphics.text(this.font, info, this.width / 2 - this.font.width(info) / 2, 28, TEXT_DIM);
	}

	private static final class Thumbnail extends AbstractWidget {
		private final File file;
		private final GalleryScreen gallery;
		private final String label;
		private final boolean rare;
		private Identifier textureId;
		private boolean loaded;

		Thumbnail(int x, int y, File file, GalleryScreen gallery) {
			super(x, y, THUMB_WIDTH, THUMB_HEIGHT, Component.literal(ScreenshotCapture.titleOf(file.getName())));
			this.file = file;
			this.gallery = gallery;
			this.label = ScreenshotCapture.titleOf(file.getName());
			this.rare = ScreenshotCapture.isRare(file.getName());
		}

		private void ensureLoaded() {
			if (this.loaded) {
				return;
			}

			this.loaded = true;

			try (InputStream in = new FileInputStream(this.file)) {
				NativeImage image = NativeImage.read(in);
				DynamicTexture texture = new DynamicTexture(() -> "advancementconfetti-thumb", image);

				this.textureId = Identifier.fromNamespaceAndPath("advancementconfetti",
						"gallery/" + Math.abs(this.file.getName().hashCode()));

				Minecraft.getInstance().getTextureManager().register(this.textureId, texture);
			} catch (IOException e) {
				AdvancementConfettiClient.LOGGER.warn("Could not load thumbnail {}", this.file, e);
			}
		}

		void releaseTexture() {
			if (this.textureId != null) {
				Minecraft.getInstance().getTextureManager().release(this.textureId);
				this.textureId = null;
				this.loaded = false;
			}
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
			ensureLoaded();

			int x = this.getX();
			int y = this.getY();
			boolean selected = this.gallery.isSelected(this.file);

			if (this.textureId != null) {
				graphics.blit(this.textureId, x, y, x + this.getWidth(), y + this.getHeight(),
						0.0F, 1.0F, 0.0F, 1.0F);
			} else {
				graphics.fill(x, y, x + this.getWidth(), y + this.getHeight(), 0xFF303030);
			}

			int border = selected ? SELECTED_BORDER : (this.rare ? RARE_BORDER : NORMAL_BORDER);
			graphics.outline(x, y, this.getWidth(), this.getHeight(), border);

			if (selected) {
				graphics.outline(x - 1, y - 1, this.getWidth() + 2, this.getHeight() + 2, SELECTED_BORDER);
			}

			Minecraft client = Minecraft.getInstance();
			String text = trim(client, this.label, this.getWidth());
			int textX = x + this.getWidth() / 2 - client.font.width(text) / 2;

			graphics.text(client.font, Component.literal(text), textX, y + this.getHeight() + 2,
					selected ? TEXT_SELECTED : TEXT_DIM);
		}

		private static String trim(Minecraft client, String text, int maxWidth) {
			if (client.font.width(text) <= maxWidth) {
				return text;
			}

			String result = text;

			while (result.length() > 1 && client.font.width(result + "..") > maxWidth) {
				result = result.substring(0, result.length() - 1);
			}

			return result + "..";
		}

		@Override
		public void onClick(MouseButtonEvent event, boolean doubleClick) {
			if (doubleClick) {
				this.gallery.openFullView(this.file);
			} else {
				this.gallery.toggleSelected(this.file);
			}
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			this.defaultButtonNarrationText(output);
		}
	}
}

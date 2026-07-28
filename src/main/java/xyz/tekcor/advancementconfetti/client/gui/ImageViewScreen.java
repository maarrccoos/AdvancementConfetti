package xyz.tekcor.advancementconfetti.client.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import xyz.tekcor.advancementconfetti.client.AdvancementConfettiClient;

public class ImageViewScreen extends Screen {
	private final Screen parent;
	private final File file;
	private Identifier textureId;
	private int imageWidth;
	private int imageHeight;

	public ImageViewScreen(Screen parent, File file) {
		super(Component.literal(file.getName()));
		this.parent = parent;
		this.file = file;
	}

	@Override
	protected void init() {
		try (InputStream in = new FileInputStream(this.file)) {
			NativeImage image = NativeImage.read(in);
			this.imageWidth = image.getWidth();
			this.imageHeight = image.getHeight();

			DynamicTexture texture = new DynamicTexture(() -> "advancementconfetti-fullview", image);

			this.textureId = Identifier.fromNamespaceAndPath("advancementconfetti", "fullview");
			Minecraft.getInstance().getTextureManager().register(this.textureId, texture);
		} catch (IOException e) {
			AdvancementConfettiClient.LOGGER.warn("Could not open {}", this.file, e);
		}

		this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
				.bounds(this.width / 2 - 50, this.height - 30, 100, 20)
				.build());
	}

	@Override
	public void onClose() {
		if (this.textureId != null) {
			Minecraft.getInstance().getTextureManager().release(this.textureId);
		}

		Minecraft.getInstance().setScreen(this.parent);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		if (this.textureId == null || this.imageWidth == 0 || this.imageHeight == 0) {
			return;
		}

		float scale = Math.min(
				(this.width - 40) / (float) this.imageWidth,
				(this.height - 80) / (float) this.imageHeight);
		scale = Math.min(scale, 4.0F);

		int drawWidth = (int) (this.imageWidth * scale);
		int drawHeight = (int) (this.imageHeight * scale);
		int x = (this.width - drawWidth) / 2;
		int y = (this.height - drawHeight) / 2 - 10;

		graphics.blit(this.textureId, x, y, x + drawWidth, y + drawHeight, 0.0F, 1.0F, 0.0F, 1.0F);
	}
}

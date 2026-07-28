package xyz.tekcor.advancementconfetti.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xyz.tekcor.advancementconfetti.client.AdvancementConfettiConfig;

public class SettingsScreen extends Screen {
	private final Screen parent;
	private Button normalButton;
	private Button rareButton;

	public SettingsScreen(Screen parent) {
		super(Component.literal("Advancement Confetti Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int centreX = this.width / 2;

		this.normalButton = this.addRenderableWidget(Button.builder(normalLabel(), b -> {
					AdvancementConfettiConfig.setNormalEnabled(!AdvancementConfettiConfig.normalEnabled());
					this.normalButton.setMessage(normalLabel());
				})
				.bounds(centreX - 100, 60, 200, 20)
				.build());

		this.rareButton = this.addRenderableWidget(Button.builder(rareLabel(), b -> {
					AdvancementConfettiConfig.setRareEnabled(!AdvancementConfettiConfig.rareEnabled());
					this.rareButton.setMessage(rareLabel());
				})
				.bounds(centreX - 100, 90, 200, 20)
				.build());

		this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
				.bounds(centreX - 100, 130, 200, 20)
				.build());
	}

	private static Component normalLabel() {
		return Component.literal("Normal confetti: " + (AdvancementConfettiConfig.normalEnabled() ? "ON" : "OFF"));
	}

	private static Component rareLabel() {
		return Component.literal("Rare confetti: " + (AdvancementConfettiConfig.rareEnabled() ? "ON" : "OFF"));
	}

	@Override
	public void onClose() {
		net.minecraft.client.Minecraft.getInstance().setScreen(this.parent);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		graphics.text(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 20, 0xFFFFFFFF);
	}
}

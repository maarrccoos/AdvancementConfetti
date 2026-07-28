package xyz.tekcor.advancementconfetti.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import xyz.tekcor.advancementconfetti.client.gui.GalleryScreen;
import xyz.tekcor.advancementconfetti.client.gui.SettingsScreen;

public class AdvancementConfettiClient implements ClientModInitializer {
	public static final String MOD_ID = "advancementconfetti";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final SoundEvent CONFETTI_SOUND = registerSound("confetti");
	public static final SoundEvent RARE_CONFETTI_SOUND = registerSound("rare_confetti");

	private static int screenshotDelayTicks = -1;
	private static boolean pendingRare;
	private static String pendingAdvancementId;
	private static Screen pendingScreen;

	@Override
	public void onInitializeClient() {
		LOGGER.info("Advancement Confetti initialising");

		AdvancementConfettiConfig.load();

		HudElementRegistry.addLast(
				Identifier.fromNamespaceAndPath(MOD_ID, "confetti_overlay"), ConfettiOverlay.INSTANCE);

		ClientTickEvents.END_CLIENT_TICK.register(AdvancementConfettiClient::onTick);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) ->
				dispatcher.register(ClientCommands.literal("advancementconfetti")
						.executes(context -> {
							context.getSource().sendFeedback(Component.literal(
									"normal=" + AdvancementConfettiConfig.normalEnabled()
											+ ", rare=" + AdvancementConfettiConfig.rareEnabled()));
							return 1;
						})
						.then(ClientCommands.literal("toggle")
								.then(ClientCommands.literal("normal").executes(AdvancementConfettiClient::toggleNormal))
								.then(ClientCommands.literal("rare").executes(AdvancementConfettiClient::toggleRare)))
						.then(ClientCommands.literal("gallery")
								.executes(context -> {
									pendingScreen = new GalleryScreen(null);
									return 1;
								}))
						.then(ClientCommands.literal("settings")
								.executes(context -> {
									pendingScreen = new SettingsScreen(null);
									return 1;
								}))));

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof PauseScreen)) {
				return;
			}

			Button button = Button.builder(Component.literal("Gallery"), b ->
							client.setScreen(new GalleryScreen(screen)))
					.bounds(scaledWidth - 66, 6, 60, 20)
					.build();

			Screens.getWidgets(screen).add(button);
		});
	}

	private static int toggleNormal(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> context) {
		AdvancementConfettiConfig.setNormalEnabled(!AdvancementConfettiConfig.normalEnabled());
		context.getSource().sendFeedback(Component.literal("normal celebration: " + AdvancementConfettiConfig.normalEnabled()));
		return 1;
	}

	private static int toggleRare(com.mojang.brigadier.context.CommandContext<FabricClientCommandSource> context) {
		AdvancementConfettiConfig.setRareEnabled(!AdvancementConfettiConfig.rareEnabled());
		context.getSource().sendFeedback(Component.literal("rare celebration: " + AdvancementConfettiConfig.rareEnabled()));
		return 1;
	}

	public static void celebrate(boolean rare, String advancementId) {
		if (rare && !AdvancementConfettiConfig.rareEnabled()) {
			return;
		}

		if (!rare && !AdvancementConfettiConfig.normalEnabled()) {
			return;
		}

		ConfettiOverlay.INSTANCE.spawn(rare);

		Minecraft client = Minecraft.getInstance();

		if (client.player != null) {
			client.player.playSound(rare ? RARE_CONFETTI_SOUND : CONFETTI_SOUND, 1.0F, 1.0F);
		}

		pendingRare = rare;
		pendingAdvancementId = advancementId;
		screenshotDelayTicks = 20;
	}

	private static void onTick(Minecraft client) {
		ConfettiOverlay.INSTANCE.tick();

		if (pendingScreen != null && client.screen == null) {
			Screen screen = pendingScreen;
			pendingScreen = null;
			client.setScreen(screen);
		}

		if (screenshotDelayTicks < 0) {
			return;
		}

		screenshotDelayTicks--;

		if (screenshotDelayTicks == 0) {
			ScreenshotCapture.capture(pendingRare, pendingAdvancementId);
			screenshotDelayTicks = -1;
		}
	}

	private static SoundEvent registerSound(String name) {
		Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}
}

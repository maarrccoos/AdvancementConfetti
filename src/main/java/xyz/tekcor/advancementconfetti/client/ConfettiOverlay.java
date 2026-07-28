package xyz.tekcor.advancementconfetti.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joml.Matrix3x2fStack;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ConfettiOverlay implements HudElement {
	public static final ConfettiOverlay INSTANCE = new ConfettiOverlay();

	private static final int[] NORMAL_COLORS = {
			0xFFFF5555, 0xFF55FF55, 0xFF5599FF, 0xFFFFEE55, 0xFFFF55CC, 0xFF55FFEE, 0xFFFFAA33
	};

	private static final int[] RARE_COLORS = {
			0xFFFFD700, 0xFFFFF3B0, 0xFFC77DFF, 0xFF9D4EDD, 0xFFFFFFFF, 0xFFFF9E00, 0xFFE0AAFF
	};

	private static final int NORMAL_PER_SIDE = 45;
	private static final int RARE_PER_SIDE = 130;

	private static final float GRAVITY = 0.16F;
	private static final float DRAG = 0.985F;
	private static final float OFF_SCREEN_MARGIN = 30.0F;

	private final Random random = new Random();
	private final List<ConfettiParticle> particles = new ArrayList<>();

	private ConfettiOverlay() {
	}

	public void spawn(boolean rare) {
		int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();

		emit(rare, -OFF_SCREEN_MARGIN, true);
		emit(rare, width + OFF_SCREEN_MARGIN, false);
	}

	private void emit(boolean rare, float originX, boolean toRight) {
		int count = rare ? RARE_PER_SIDE : NORMAL_PER_SIDE;
		int[] palette = rare ? RARE_COLORS : NORMAL_COLORS;

		float minSpeed = rare ? 9.0F : 7.5F;
		float speedRange = rare ? 12.0F : 9.0F;

		int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		float originY = height * 0.34F;

		for (int i = 0; i < count; i++) {
			double angle = Math.toRadians(6.0D + this.random.nextDouble() * 40.0D);
			float speed = minSpeed + this.random.nextFloat() * speedRange;

			float velocityX = (float) (Math.cos(angle) * speed) * (toRight ? 1.0F : -1.0F);
			float velocityY = (float) (-Math.sin(angle) * speed);

			float trail = this.random.nextFloat() * 34.0F;
			float x = originX - (toRight ? trail : -trail);
			float y = originY + (this.random.nextFloat() - 0.5F) * 28.0F;

			float half = rare ? 2.5F : 2.0F;
			float halfWidth = half + this.random.nextFloat();
			float halfHeight = half + this.random.nextFloat() * 2.0F;

			int color = palette[this.random.nextInt(palette.length)];
			int life = 90 + this.random.nextInt(rare ? 70 : 40);
			float rotation = this.random.nextFloat() * 360.0F;
			float rotationSpeed = (this.random.nextFloat() - 0.5F) * 26.0F;

			this.particles.add(new ConfettiParticle(
					x, y, velocityX, velocityY, rotation, rotationSpeed, color, life, halfWidth, halfHeight));
		}
	}

	public void tick() {
		if (this.particles.isEmpty()) {
			return;
		}

		int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

		this.particles.removeIf(particle -> {
			particle.tick(GRAVITY, DRAG);
			return particle.isDead() || particle.y > height + 40.0F;
		});
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
		if (this.particles.isEmpty()) {
			return;
		}

		float partial = tracker.getGameTimeDeltaPartialTick(false);
		Matrix3x2fStack pose = graphics.pose();

		for (ConfettiParticle particle : this.particles) {
			int alpha = (int) (particle.alpha() * 255.0F) << 24;
			int color = (particle.color & 0x00FFFFFF) | alpha;

			pose.pushMatrix();
			pose.translate(particle.lerpX(partial), particle.lerpY(partial));
			pose.rotate((float) Math.toRadians(particle.lerpRotation(partial)));

			graphics.fill(
					(int) -particle.halfWidth, (int) -particle.halfHeight,
					(int) particle.halfWidth, (int) particle.halfHeight, color);

			pose.popMatrix();
		}
	}
}

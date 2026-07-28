package xyz.tekcor.advancementconfetti.client;

final class ConfettiParticle {
	float x;
	float y;
	float prevX;
	float prevY;
	float velocityX;
	float velocityY;
	float rotation;
	float prevRotation;
	float rotationSpeed;
	final float halfWidth;
	final float halfHeight;
	final int color;
	int life;

	ConfettiParticle(float x, float y, float velocityX, float velocityY, float rotation,
			float rotationSpeed, int color, int life, float halfWidth, float halfHeight) {
		this.x = x;
		this.y = y;
		this.prevX = x;
		this.prevY = y;
		this.velocityX = velocityX;
		this.velocityY = velocityY;
		this.rotation = rotation;
		this.prevRotation = rotation;
		this.rotationSpeed = rotationSpeed;
		this.color = color;
		this.life = life;
		this.halfWidth = halfWidth;
		this.halfHeight = halfHeight;
	}

	void tick(float gravity, float horizontalDrag, float terminalFallSpeed) {
		this.prevX = this.x;
		this.prevY = this.y;
		this.prevRotation = this.rotation;

		this.x += this.velocityX;
		this.y += this.velocityY;

		this.velocityX *= horizontalDrag;
		this.velocityY = Math.min(this.velocityY + gravity, terminalFallSpeed);

		this.rotation += this.rotationSpeed;
		this.life--;
	}

	boolean isDead() {
		return this.life <= 0;
	}

	float lerpX(float partial) {
		return this.prevX + (this.x - this.prevX) * partial;
	}

	float lerpY(float partial) {
		return this.prevY + (this.y - this.prevY) * partial;
	}

	float lerpRotation(float partial) {
		return this.prevRotation + (this.rotation - this.prevRotation) * partial;
	}

	float alpha() {
		return this.life < 25 ? Math.max(0.0F, this.life / 25.0F) : 1.0F;
	}
}

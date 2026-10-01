package me.cat.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

public class GlassAnimation {
    private final List<Shard> shards = new ArrayList<>();
    private float time = 0;
    private final int width, height;
    private boolean finished = false;

    public GlassAnimation(int width, int height) {
        this.width = width;
        this.height = height;
        Random random = Random.create();
        int cols = 14;
        int rows = 9;
        int shardW = width / cols;
        int shardH = height / rows;

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                float sx = i * shardW + shardW / 2f + (random.nextFloat() - 0.5f) * 10;
                float sy = j * shardH + shardH / 2f + (random.nextFloat() - 0.5f) * 10;
                shards.add(new Shard(sx, sy, shardW, shardH, random));
            }
        }
    }

    public void render(DrawContext ctx, float delta) {
        time += delta;

        if (time > 35) {
            finished = true;
            return;
        }

        for (Shard shard : shards) {
            shard.update(delta);
            float alphaNorm = MathHelper.clamp(1f - (time / 30f), 0f, 1f);
            int alpha = (int) (alphaNorm * 220);
            if (alpha <= 0) continue;

            int baseColor = (alpha << 24) | 0xCCCCCC;
            int shineColor = (Math.min(alpha + 30, 255) << 24) | 0xFFFFFF;

            ctx.getMatrices().push();
            ctx.getMatrices().translate(shard.x, shard.y, 0);
            ctx.getMatrices().multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(shard.rotation));

            // Основной осколок
            ctx.fill(-shard.w / 2, -shard.h / 2, shard.w / 2, shard.h / 2, baseColor);
            // Блик
            ctx.fill(-shard.w / 2, -shard.h / 2, -shard.w / 4, -shard.h / 4, shineColor);

            ctx.getMatrices().pop();
        }
    }

    public boolean isFinished() {
        return finished;
    }

    private static class Shard {
        float x, y, w, h;
        float vx, vy, rotation, rotSpeed;

        Shard(float x, float y, int w, int h, Random random) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.vx = (random.nextFloat() - 0.5f) * 5f;
            this.vy = (random.nextFloat() - 0.5f) * 5f - 3f;
            this.rotation = random.nextFloat() * 360f;
            this.rotSpeed = (random.nextFloat() - 0.5f) * 14f;
        }

        void update(float delta) {
            x += vx * delta;
            y += vy * delta;
            vy += 0.18f * delta; // гравитация
            rotation += rotSpeed * delta;
        }
    }
}

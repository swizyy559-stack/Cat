package com.cat.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/** Настоящая отрисовка эффектов в мире (кольца и ленты, а не частицы). */
public final class WorldFx {
    private WorldFx() {}

    private static final long RING_LIFE_MS = 900;
    private static final long TRAIL_LIFE_MS = 800;
    private static final int SEGS = 56;

    private static final List<double[]> RINGS = new ArrayList<>(); // x, y, z, startMs
    private static final List<double[]> TRAIL = new ArrayList<>(); // x, y, z, timeMs
    private static boolean wasOnGround = true;
    private static Entity target;
    private static long targetUntil;

    public static void setTarget(Entity e) {
        target = e;
        targetUntil = Util.getMeasuringTimeMs() + 3000;
    }

    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) {
            RINGS.clear();
            TRAIL.clear();
            return;
        }
        final long now = Util.getMeasuringTimeMs();

        boolean onGround = p.isOnGround();
        if (Module.JUMPCIRCLES.enabled && wasOnGround && !onGround && p.getVelocity().y > 0.1) {
            RINGS.add(new double[]{p.getX(), p.getY() + 0.02, p.getZ(), now});
        }
        wasOnGround = onGround;
        RINGS.removeIf(r -> now - (long) r[3] > RING_LIFE_MS);

        double speed = Math.hypot(p.getX() - p.prevX, p.getZ() - p.prevZ);
        if (Module.TRAILS.enabled && speed > 0.03) {
            TRAIL.add(new double[]{p.getX(), p.getY(), p.getZ(), now});
        }
        TRAIL.removeIf(t -> now - (long) t[3] > TRAIL_LIFE_MS);

        if (target != null && (target.isRemoved() || now > targetUntil)) target = null;
    }

    public static void render(WorldRenderContext ctx) {
        MatrixStack ms = ctx.matrixStack();
        if (ms == null) return;
        boolean targetOn = Module.TARGETRING.enabled && target != null;
        if (RINGS.isEmpty() && TRAIL.size() < 2 && !targetOn) return;

        Vec3d cam = ctx.camera().getPos();
        Matrix4f m = ms.peek().getPositionMatrix();
        long now = Util.getMeasuringTimeMs();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        // JumpCircles: расширяющееся радужное кольцо с мягким свечением
        for (double[] r : RINGS) {
            float t = (now - (long) r[3]) / (float) RING_LIFE_MS;
            if (t < 0f || t > 1f) continue;
            float a = 1f - t;
            double radius = 0.3 + t * 1.8;
            float shift = t * 0.6f;
            ring(m, cam, r[0], r[1], r[2], radius, 0.14, a * 0.25f, true, shift, 0);
            ring(m, cam, r[0], r[1], r[2], radius, 0.035, a, true, shift, 0);
        }

        // Trails: светящаяся лента за игроком
        if (TRAIL.size() > 1) {
            BufferBuilder b = Tessellator.getInstance().begin(
                    VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            int n = TRAIL.size();
            for (int i = 0; i < n; i++) {
                double[] s = TRAIL.get(i);
                float age = (now - (long) s[3]) / (float) TRAIL_LIFE_MS;
                float a = Math.max(0f, 1f - age);
                int c = Color.HSBtoRGB((0.06f + i * 0.004f) % 1f, 0.85f, 1f);
                float r = ((c >> 16) & 255) / 255f, g = ((c >> 8) & 255) / 255f, bl = (c & 255) / 255f;
                b.vertex(m, (float) (s[0] - cam.x), (float) (s[1] - cam.y), (float) (s[2] - cam.z))
                        .color(r, g, bl, a * 0.9f);
                b.vertex(m, (float) (s[0] - cam.x), (float) (s[1] + 1.8 - cam.y), (float) (s[2] - cam.z))
                        .color(r, g, bl, a * 0.1f);
            }
            BufferRenderer.drawWithGlobalProgram(b.end());
        }

        // TargetRing: кольцо плавно ходит вверх-вниз по цели
        if (targetOn) {
            MinecraftClient mc = MinecraftClient.getInstance();
            float td = mc.getRenderTickCounter().getTickDelta(false);
            Vec3d p = target.getLerpedPos(td);
            double t = now / 450.0;
            double y = p.y + (Math.sin(t) + 1.0) / 2.0 * target.getHeight();
            double rad = target.getWidth() * 0.75 + 0.2;
            ring(m, cam, p.x, y, p.z, rad, 0.1, 0.25f, false, 0f, 0xFF6A3C);
            ring(m, cam, p.x, y, p.z, rad, 0.03, 0.95f, false, 0f, 0xFF8A4C);
            ring(m, cam, p.x, p.y + 0.02, p.z, rad, 0.03, 0.5f, false, 0f, 0xFF6A3C);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void ring(Matrix4f m, Vec3d cam, double cx, double cy, double cz,
                             double radius, double half, float alpha,
                             boolean rainbow, float shift, int rgb) {
        BufferBuilder b = Tessellator.getInstance().begin(
                VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        double ro = radius + half;
        double ri = Math.max(0.0, radius - half);
        for (int i = 0; i <= SEGS; i++) {
            double a = i * (Math.PI * 2.0 / SEGS);
            double cos = Math.cos(a), sin = Math.sin(a);
            int c = rainbow ? (Color.HSBtoRGB((i / (float) SEGS + shift) % 1f, 0.6f, 1f) & 0xFFFFFF) : rgb;
            float r = ((c >> 16) & 255) / 255f, g = ((c >> 8) & 255) / 255f, bl = (c & 255) / 255f;
            b.vertex(m, (float) (cx + cos * ro - cam.x), (float) (cy - cam.y), (float) (cz + sin * ro - cam.z))
                    .color(r, g, bl, alpha);
            b.vertex(m, (float) (cx + cos * ri - cam.x), (float) (cy - cam.y), (float) (cz + sin * ri - cam.z))
                    .color(r, g, bl, alpha);
        }
        BufferRenderer.drawWithGlobalProgram(b.end());
    }
}

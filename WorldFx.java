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

import java.util.ArrayList;
import java.util.List;

/** Отрисовка эффектов в мире: кольца, лента, нимб. */
public final class WorldFx {
    private WorldFx() {}

    private static final int SEGS = 56;

    private static final List<double[]> RINGS = new ArrayList<>(); // x, y, z, startMs
    private static final List<double[]> TRAIL = new ArrayList<>(); // x, y, z, timeMs
    private static boolean wasOnGround = true;
    private static Entity target;
    private static long targetUntil;

    public static void setTarget(Entity e) {
        target = e;
        targetUntil = Util.getMeasuringTimeMs() + (long) (Module.TARGET_TIME.v * 1000);
    }

    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) {
            RINGS.clear();
            TRAIL.clear();
            return;
        }
        final long now = Util.getMeasuringTimeMs();
        final long ringLife = (long) (Module.JUMP_TIME.v * 1000);
        final long trailLife = (long) (Module.TRAIL_LEN.v * 1000);

        boolean onGround = p.isOnGround();
        if (Module.JUMPCIRCLES.enabled && wasOnGround && !onGround && p.getVelocity().y > 0.1) {
            RINGS.add(new double[]{p.getX(), p.getY() + 0.02, p.getZ(), now});
        }
        wasOnGround = onGround;
        RINGS.removeIf(r -> now - (long) r[3] > ringLife);

        double speed = Math.hypot(p.getX() - p.prevX, p.getZ() - p.prevZ);
        if (Module.TRAILS.enabled && speed > 0.03) {
            TRAIL.add(new double[]{p.getX(), p.getY(), p.getZ(), now});
        }
        TRAIL.removeIf(t -> now - (long) t[3] > trailLife);

        if (target != null && (target.isRemoved() || now > targetUntil)) target = null;
    }

    public static void render(WorldRenderContext ctx) {
        MatrixStack ms = ctx.matrixStack();
        if (ms == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        boolean targetOn = Module.TARGETRING.enabled && target != null;
        boolean haloOn = Module.HALO.enabled && !mc.options.getPerspective().isFirstPerson();
        if (RINGS.isEmpty() && TRAIL.size() < 2 && !targetOn && !haloOn) return;

        Vec3d cam = ctx.camera().getPos();
        Matrix4f m = ms.peek().getPositionMatrix();
        long now = Util.getMeasuringTimeMs();
        float td = mc.getRenderTickCounter().getTickDelta(false);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        // JumpCircles
        long ringLife = (long) (Module.JUMP_TIME.v * 1000);
        boolean jr = Colors.rainbow(Module.JUMP_COLOR);
        int jc = Colors.fixed(Module.JUMP_COLOR);
        for (double[] r : RINGS) {
            float t = (now - (long) r[3]) / (float) ringLife;
            if (t < 0f || t > 1f) continue;
            float a = 1f - t;
            double radius = 0.3 + t * Module.JUMP_SIZE.v;
            float shift = t * 0.6f;
            ring(m, cam, r[0], r[1], r[2], radius, 0.14, a * 0.25f, jr, shift, jc);
            ring(m, cam, r[0], r[1], r[2], radius, 0.035, a, jr, shift, jc);
        }

        // Trails
        if (TRAIL.size() > 1) {
            long trailLife = (long) (Module.TRAIL_LEN.v * 1000);
            double height = Module.TRAIL_HEIGHT.v;
            BufferBuilder b = Tessellator.getInstance().begin(
                    VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            int n = TRAIL.size();
            for (int i = 0; i < n; i++) {
                double[] s = TRAIL.get(i);
                float age = (now - (long) s[3]) / (float) trailLife;
                float a = Math.max(0f, 1f - age);
                int c = Colors.pick(Module.TRAIL_COLOR, now / 3000f + i * 0.02f);
                float r = ((c >> 16) & 255) / 255f, g = ((c >> 8) & 255) / 255f, bl = (c & 255) / 255f;
                b.vertex(m, (float) (s[0] - cam.x), (float) (s[1] - cam.y), (float) (s[2] - cam.z))
                        .color(r, g, bl, a * 0.9f);
                b.vertex(m, (float) (s[0] - cam.x), (float) (s[1] + height - cam.y), (float) (s[2] - cam.z))
                        .color(r, g, bl, a * 0.1f);
            }
            BufferRenderer.drawWithGlobalProgram(b.end());
        }

        // TargetRing
        if (targetOn) {
            Vec3d p = target.getLerpedPos(td);
            double t = now / 450.0 * Module.TARGET_SPEED.v;
            double y = p.y + (Math.sin(t) + 1.0) / 2.0 * target.getHeight();
            double rad = target.getWidth() * 0.75 + 0.2;
            boolean tr = Colors.rainbow(Module.TARGET_COLOR);
            int tc = Colors.fixed(Module.TARGET_COLOR);
            float shift = (now % 3000) / 3000f;
            ring(m, cam, p.x, y, p.z, rad, 0.1, 0.25f, tr, shift, tc);
            ring(m, cam, p.x, y, p.z, rad, 0.03, 0.95f, tr, shift, tc);
            ring(m, cam, p.x, p.y + 0.02, p.z, rad, 0.03, 0.5f, tr, shift, tc);
        }

        // Halo
        if (haloOn) {
            Vec3d pp = mc.player.getLerpedPos(td);
            double hy = pp.y + Module.HALO_HEIGHT.v;
            double rad = Module.HALO_SIZE.v;
            boolean hr = Colors.rainbow(Module.HALO_COLOR);
            int hc = Colors.fixed(Module.HALO_COLOR);
            float shift = (now % 4000) / 4000f;
            ring(m, cam, pp.x, hy, pp.z, rad, 0.06, 0.3f, hr, shift, hc);
            ring(m, cam, pp.x, hy, pp.z, rad, 0.018, 0.95f, hr, shift, hc);
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
            int c = rainbow ? Colors.hsb(i / (float) SEGS + shift) : rgb;
            float r = ((c >> 16) & 255) / 255f, g = ((c >> 8) & 255) / 255f, bl = (c & 255) / 255f;
            b.vertex(m, (float) (cx + cos * ro - cam.x), (float) (cy - cam.y), (float) (cz + sin * ro - cam.z))
                    .color(r, g, bl, alpha);
            b.vertex(m, (float) (cx + cos * ri - cam.x), (float) (cy - cam.y), (float) (cz + sin * ri - cam.z))
                    .color(r, g, bl, alpha);
        }
        BufferRenderer.drawWithGlobalProgram(b.end());
    }
}

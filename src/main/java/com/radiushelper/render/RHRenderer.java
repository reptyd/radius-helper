package com.radiushelper.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.radiushelper.config.RHConfig;
import com.radiushelper.config.RHConfigStorage;
import com.radiushelper.selection.SelectionState;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class RHRenderer {
    private RHRenderer() {
    }

    public static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return;
        }
        SelectionState selection = SelectionState.get();
        if (!selection.hasPoint1()) {
            return;
        }
        RHConfig config = RHConfigStorage.get();
        boolean drawFill = config.renderFill;
        boolean drawOutline = config.renderOutline;
        boolean drawSelectionBox = config.showSelectionBox;
        boolean drawPoints = config.showPointMarkers;
        if (!drawFill && !drawOutline && !drawSelectionBox && !drawPoints) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        Vec3d camPos = context.camera().getPos();
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        boolean depthOff = config.xray || drawSelectionBox || drawPoints || (drawOutline && !drawFill);
        if (depthOff) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        int color = config.mode == RHConfig.Mode.PRIVATE ? config.privateColor : config.tntColor;
        int rr = (color >> 16) & 0xFF;
        int gg = (color >> 8) & 0xFF;
        int bb = color & 0xFF;
        int alpha = clamp255(config.mode == RHConfig.Mode.PRIVATE ? config.privateAlpha : config.tntAlpha);

        List<BlockPos> placements = selection.hasSelection() ? selection.getPlacements(config) : List.of();
        BlockPos point1 = selection.getPoint1();
        BlockPos point2 = selection.getPoint2();

        if (drawFill) {
            BufferBuilder bbFill = Tessellator.getInstance().getBuffer();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            Matrix4f m = matrices.peek().getPositionMatrix();
            bbFill.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            for (BlockPos pos : placements) {
                addCube(bbFill, m, pos.getX(), pos.getY(), pos.getZ(), rr, gg, bb, alpha);
            }
            if (drawSelectionBox && selection.hasSelection()) {
                Box box = selection.getSelectionBox();
                int sr = (config.selectionColor >> 16) & 0xFF;
                int sg = (config.selectionColor >> 8) & 0xFF;
                int sb = config.selectionColor & 0xFF;
                int sa = clamp255(config.selectionAlpha);
                addBox(bbFill, m, (float) box.minX, (float) box.minY, (float) box.minZ,
                    (float) box.maxX, (float) box.maxY, (float) box.maxZ, sr, sg, sb, sa);
            }
            if (drawPoints) {
                int sa = clamp255(config.selectionAlpha);
                addPointMarkerFill(bbFill, m, point1, 0x4CFF6A, sa);
                if (point2 != null) {
                    addPointMarkerFill(bbFill, m, point2, 0xFF4C4C, sa);
                }
            }
            BufferRenderer.drawWithGlobalProgram(bbFill.end());
        }

        if (drawOutline || drawSelectionBox || drawPoints) {
            BufferBuilder bbEdge = Tessellator.getInstance().getBuffer();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            Matrix4f m = matrices.peek().getPositionMatrix();
            bbEdge.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            if (drawOutline) {
                int outlineAlpha = Math.max(180, alpha);
                for (BlockPos pos : placements) {
                    addEdgeBoxes(bbEdge, m, pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1.0f, pos.getY() + 1.0f, pos.getZ() + 1.0f,
                        0.08f, rr, gg, bb, outlineAlpha);
                }
            }
            if (drawSelectionBox && selection.hasSelection()) {
                Box box = selection.getSelectionBox();
                int sr = (config.selectionColor >> 16) & 0xFF;
                int sg = (config.selectionColor >> 8) & 0xFF;
                int sb = config.selectionColor & 0xFF;
                addEdgeBoxes(bbEdge, m, (float) box.minX, (float) box.minY, (float) box.minZ,
                    (float) box.maxX, (float) box.maxY, (float) box.maxZ,
                    0.15f, sr, sg, sb, 255);
            }
            if (drawPoints) {
                int sa = clamp255(config.selectionAlpha);
                addPointMarkerEdges(bbEdge, m, point1, 0x4CFF6A, sa);
                if (point2 != null) {
                    addPointMarkerEdges(bbEdge, m, point2, 0xFF4C4C, sa);
                }
            }
            BufferRenderer.drawWithGlobalProgram(bbEdge.end());
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        if (depthOff) {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
        matrices.pop();
    }

    private static int clamp255(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 255) {
            return 255;
        }
        return value;
    }

    private static void addCube(BufferBuilder bb, Matrix4f m, int x, int y, int z, int r, int g, int b, int a) {
        addBox(bb, m, x, y, z, x + 1, y + 1, z + 1, r, g, b, a);
    }

    private static void addBox(BufferBuilder bb, Matrix4f m, float x1, float y1, float z1,
                               float x2, float y2, float z2, int r, int g, int b, int a) {
        quad(bb, m, x1, y1, z1, x1, y2, z1, x1, y2, z2, x1, y1, z2, r, g, b, a);
        quad(bb, m, x2, y1, z2, x2, y2, z2, x2, y2, z1, x2, y1, z1, r, g, b, a);
        quad(bb, m, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, r, g, b, a);
        quad(bb, m, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, r, g, b, a);
        quad(bb, m, x2, y1, z1, x2, y2, z1, x1, y2, z1, x1, y1, z1, r, g, b, a);
        quad(bb, m, x1, y1, z2, x1, y2, z2, x2, y2, z2, x2, y1, z2, r, g, b, a);
    }

    private static void quad(BufferBuilder bb, Matrix4f m, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3,
                             float x4, float y4, float z4, int r, int g, int b, int a) {
        bb.vertex(m, x1, y1, z1).color(r, g, b, a).next();
        bb.vertex(m, x2, y2, z2).color(r, g, b, a).next();
        bb.vertex(m, x3, y3, z3).color(r, g, b, a).next();
        bb.vertex(m, x4, y4, z4).color(r, g, b, a).next();
    }

    private static void addPointMarkerFill(BufferBuilder bb, Matrix4f m, BlockPos pos, int color, int alpha) {
        if (pos == null) {
            return;
        }
        float inset = 0.1f;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        addBox(bb, m,
            pos.getX() + inset, pos.getY() + inset, pos.getZ() + inset,
            pos.getX() + 1.0f - inset, pos.getY() + 1.0f - inset, pos.getZ() + 1.0f - inset,
            r, g, b, alpha);
    }

    private static void addPointMarkerEdges(BufferBuilder bb, Matrix4f m, BlockPos pos, int color, int alpha) {
        if (pos == null) {
            return;
        }
        float inset = 0.1f;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        addEdgeBoxes(bb, m,
            pos.getX() + inset, pos.getY() + inset, pos.getZ() + inset,
            pos.getX() + 1.0f - inset, pos.getY() + 1.0f - inset, pos.getZ() + 1.0f - inset,
            0.1f, r, g, b, alpha);
    }

    private static void addEdgeBoxes(BufferBuilder bb, Matrix4f m, float x1, float y1, float z1,
                                     float x2, float y2, float z2, float thickness, int r, int g, int b, int a) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float t = Math.min(thickness, Math.min(dx, Math.min(dy, dz)) * 0.45f);
        if (t <= 0.0f) {
            return;
        }

        float x1t = x1 + t;
        float x2t = x2 - t;
        float y1t = y1 + t;
        float y2t = y2 - t;
        float z1t = z1 + t;
        float z2t = z2 - t;

        // X edges
        addBox(bb, m, x1, y1, z1, x2, y1t, z1t, r, g, b, a);
        addBox(bb, m, x1, y1, z2t, x2, y1t, z2, r, g, b, a);
        addBox(bb, m, x1, y2t, z1, x2, y2, z1t, r, g, b, a);
        addBox(bb, m, x1, y2t, z2t, x2, y2, z2, r, g, b, a);

        // Y edges
        addBox(bb, m, x1, y1, z1, x1t, y2, z1t, r, g, b, a);
        addBox(bb, m, x1, y1, z2t, x1t, y2, z2, r, g, b, a);
        addBox(bb, m, x2t, y1, z1, x2, y2, z1t, r, g, b, a);
        addBox(bb, m, x2t, y1, z2t, x2, y2, z2, r, g, b, a);

        // Z edges
        addBox(bb, m, x1, y1, z1, x1t, y1t, z2, r, g, b, a);
        addBox(bb, m, x1, y2t, z1, x1t, y2, z2, r, g, b, a);
        addBox(bb, m, x2t, y1, z1, x2, y1t, z2, r, g, b, a);
        addBox(bb, m, x2t, y2t, z1, x2, y2, z2, r, g, b, a);
    }
}

package com.radiushelper;

import com.radiushelper.config.RHConfigStorage;
import com.radiushelper.render.RHRenderer;
import com.radiushelper.selection.SelectionState;
import com.radiushelper.ui.RHConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class RadiusHelperClient implements ClientModInitializer {
    public static final String MODID = "radius_helper";

    private static KeyBinding kbPoint;
    private static KeyBinding kbMenu;

    @Override
    public void onInitializeClient() {
        RHConfigStorage.load();

        kbPoint = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.radius_helper.set_point",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_1,
            "category.radius_helper"
        ));
        kbMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.radius_helper.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "category.radius_helper"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null) {
                return;
            }
            while (kbPoint.wasPressed()) {
                BlockPos pos = pickPos(client);
                SelectionState selection = SelectionState.get();
                if (!selection.hasPoint1() || selection.hasPoint2()) {
                    selection.setPoint1(pos);
                    selection.clearPoint2();
                    client.player.sendMessage(Text.translatable("message.radius_helper.point1", formatPos(pos)), true);
                } else {
                    selection.setPoint2(pos);
                    client.player.sendMessage(Text.translatable("message.radius_helper.point2", formatPos(pos)), true);
                }
            }
            while (kbMenu.wasPressed()) {
                client.setScreen(new RHConfigScreen(client.currentScreen));
            }
        });

        WorldRenderEvents.LAST.register(RHRenderer::render);
    }

    private static BlockPos pickPos(MinecraftClient client) {
        HitResult hit = client.crosshairTarget;
        if (hit instanceof BlockHitResult blockHit) {
            return blockHit.getBlockPos();
        }
        return client.player.getBlockPos();
    }

    private static String formatPos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}

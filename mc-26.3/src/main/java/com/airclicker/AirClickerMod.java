package com.airclicker;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class AirClickerMod implements ClientModInitializer {
    public static boolean running = false;
    public static KeyMapping toggleKey;
    public static KeyMapping menuKey;
    private static long nextClick = 0;
    private static boolean prevPauseOnLostFocus = true;

    @Override
    public void onInitializeClient() {
        ClickerConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("airclicker", "main"));

        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.airclicker.toggle", InputConstants.Type.KEYBOARD, InputConstants.KEY_R, category));
        menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.airclicker.menu", InputConstants.Type.KEYBOARD, InputConstants.KEY_RSHIFT, category));
        
        ClientTickEvents.END_CLIENT_TICK.register(AirClickerMod::tick);

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("airclicker", "status"),
                (graphics, deltaTracker) -> {
                    if (!running) return;
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.gui.hud.isHidden()) return;
                    long left = Math.max(0, nextClick - System.currentTimeMillis());
                    String txt = "Taro Autoclicker Oficial 26.3 ● " + ClickerConfig.format(ClickerConfig.intervalSeconds)
                            + "  (próximo en " + String.format(java.util.Locale.ROOT, "%.1f", left / 1000.0) + " s)";
                    graphics.text(mc.font, txt, 6, 6, 0xFF22C55E, true);
                });
    }

    public static void start() {
        Minecraft mc = Minecraft.getInstance();
        if (!running) {
            // Evita que el juego se pause al perder el foco (para seguir clicando en segundo plano).
            prevPauseOnLostFocus = mc.options.pauseOnLostFocus;
            mc.options.pauseOnLostFocus = false;
        }
        running = true;
        nextClick = System.currentTimeMillis() + (long) (ClickerConfig.intervalSeconds * 1000);
    }

    public static void stop() {
        if (running) {
            Minecraft.getInstance().options.pauseOnLostFocus = prevPauseOnLostFocus;
        }
        running = false;
    }

    private static void tick(Minecraft mc) {
        while (toggleKey.consumeClick()) {
            if (running) stop(); else start();
            if (mc.player != null) {
                mc.player.sendOverlayMessage(Component.literal(running ? "§aAutoclick iniciado" : "§cAutoclick detenido"));
            }
        }
        while (menuKey.consumeClick()) {
            if (mc.gui.screen() == null) mc.gui.setScreen(new ClickerScreen());
        }

        if (!running) return;
        if (mc.player == null || mc.level == null) {
            stop();
            return;
        }
        long now = System.currentTimeMillis();
        // Con un menú abierto no se hace clic y se reinicia la cuenta.
        if (mc.gui.screen() != null) {
            nextClick = now + (long) (ClickerConfig.intervalSeconds * 1000);
            return;
        }
        if (now >= nextClick) {
            KeyMapping.click(InputConstants.getKey(mc.options.keyAttack.saveString()));
            nextClick = now + (long) (ClickerConfig.intervalSeconds * 1000);
        }
    }
}

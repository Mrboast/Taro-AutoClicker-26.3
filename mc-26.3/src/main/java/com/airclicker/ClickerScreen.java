package com.airclicker;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickerScreen extends Screen {
    private static final String NAME = "Taro Autoclicker Oficial 26.3";

    private static final int W = 340;
    private static final int H = 222;

    private static final int GREEN = 0xFF22C55E;
    private static final int GREEN_BG = 0xFF0F2A1A;
    private static final int AMBER = 0xFFF59E0B;
    private static final int AMBER_BG = 0xFF33260A;
    private static final int GRAY = 0xFFA1A1AA;
    private static final int WHITE = 0xFFFFFFFF;

    private Button startBtn;
    private Button stopBtn;

    public ClickerScreen() {
        super(Component.literal(NAME));
    }

    private int px() { return (width - W) / 2; }
    private int py() { return (height - H) / 2; }

    @Override
    protected void init() {
        int x = px(), y = py();
        addRenderableWidget(new IntervalSlider(x + 16, y + 90, W - 32, 20));

        int bw = (W - 44) / 2;
        startBtn = addRenderableWidget(Button.builder(Component.literal("▶  Iniciar"),
                b -> AirClickerMod.start()).bounds(x + 16, y + H - 40, bw, 24).build());
        stopBtn = addRenderableWidget(Button.builder(Component.literal("■  Detener"),
                b -> AirClickerMod.stop()).bounds(x + 28 + bw, y + H - 40, bw, 24).build());
    }

    private void badge(GuiGraphicsExtractor g, String text, int rightX, int y, int fg, int bg) {
        int tw = font.width(text);
        g.fill(rightX - tw - 12, y - 3, rightX, y + 11, bg);
        g.text(font, text, rightX - tw - 6, y, fg, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        startBtn.active = !AirClickerMod.running;
        stopBtn.active = AirClickerMod.running;

        int x = px(), y = py();
        int left = x + 16, right = x + W - 16;

        // Panel
        g.fill(x - 1, y - 1, x + W + 1, y + H + 1, 0xFF2A2A2A); // borde
        g.fill(x, y, x + W, y + H, 0xF2050505);                 // fondo

        // Widgets (slider y botones)
        super.extractRenderState(g, mouseX, mouseY, delta);

        // Encabezado
        g.text(font, NAME, left, y + 14, WHITE, true);
        badge(g, "Clic al aire", right, y + 12, GREEN, GREEN_BG);

        // Estado
        g.text(font, "Estado", left, y + 38, WHITE, true);
        if (AirClickerMod.running) badge(g, "En marcha", right, y + 36, AMBER, AMBER_BG);
        else badge(g, "Preparado", right, y + 36, GREEN, GREEN_BG);

        // Valor grande (escala 2x)
        var m = g.pose();
        m.pushMatrix();
        m.scale(2f, 2f);
        g.text(font, ClickerConfig.format(ClickerConfig.intervalSeconds),
                left / 2, (y + 54) / 2, GREEN, true);
        m.popMatrix();
        g.text(font, "Intervalo de clics", left, y + 76, GRAY, false);

        // Separador y datos
        g.fill(left, y + 120, right, y + 121, 0xFF1F1F1F);
        row(g, "Tiempo mínimo", "0.5 segundos", y + 130, left, right);
        row(g, "Tiempo máximo", "15 segundos", y + 146, left, right);
        row(g, "Tecla de activación",
                AirClickerMod.toggleKey.getTranslatedKeyMessage().getString(), y + 162, left, right);
    }

    private void row(GuiGraphicsExtractor g, String label, String value, int y, int left, int right) {
        g.text(font, label, left, y, GRAY, false);
        g.text(font, value, right - font.width(value), y, WHITE, true);
    }

    @Override
    public void removed() {
        ClickerConfig.save();
    }

    /** Slider logarítmico: más precisión en tiempos cortos. */
    private static class IntervalSlider extends AbstractSliderButton {
        IntervalSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Component.empty(), toSlider(ClickerConfig.intervalSeconds));
            updateMessage();
        }

        static double toSlider(double s) {
            return Math.log(s / ClickerConfig.MIN) / Math.log(ClickerConfig.MAX / ClickerConfig.MIN);
        }

        static double fromSlider(double v) {
            return ClickerConfig.MIN * Math.pow(ClickerConfig.MAX / ClickerConfig.MIN, v);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("Intervalo: " + ClickerConfig.format(ClickerConfig.intervalSeconds)));
        }

        @Override
        protected void applyValue() {
            ClickerConfig.set(fromSlider(value));
        }
    }
}

package me.cat.client.gui;

import me.cat.client.CatClient;
import me.cat.client.gui.components.CategoryPanel;
import me.cat.client.language.LangManager;
import me.cat.client.module.Category;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGui extends Screen {
    private final List<CategoryPanel> panels = new ArrayList<>();
    private boolean closing = false;
    private GlassAnimation glassAnimation;

    public ClickGui() {
        super(Text.literal("Cat ClickGUI"));
        int x = 30;
        for (Category cat : Category.values()) {
            panels.add(new CategoryPanel(x, 30, 130, 18, cat));
            x += 145;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        if (closing && glassAnimation != null) {
            glassAnimation.render(context, delta);
            if (glassAnimation.isFinished()) {
                closing = false;
                mc.setScreen(null);
                return;
            }
            // Затемнение фона во время анимации
            context.fill(0, 0, width, height, 0xBB000000);
            return;
        }

        // Кнопка переключения языка
        String langBtn = LangManager.getCurrent() == LangManager.Language.RU ? "RU / EN" : "EN / RU";
        int langW = textRenderer.getWidth(langBtn);
        int langX = width - langW - 14;
        int langY = 8;
        context.fill(langX - 6, langY - 4, langX + langW + 6, langY + textRenderer.fontHeight + 6, 0xDD222222);
        context.drawHorizontalLine(langX - 6, langX + langW + 6, langY - 4, 0xFFFFAA00);
        context.drawTextWithShadow(textRenderer, langBtn, langX, langY, 0xFFFFAA00);

        // Панели
        for (CategoryPanel panel : panels) {
            panel.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (closing) return false;

        // Клик по языку
        String langBtn = LangManager.getCurrent() == LangManager.Language.RU ? "RU / EN" : "EN / RU";
        int langW = textRenderer.getWidth(langBtn);
        int langX = width - langW - 14;
        int langY = 8;
        if (mouseX >= langX - 6 && mouseX <= langX + langW + 6 && mouseY >= langY - 4 && mouseY <= langY + textRenderer.fontHeight + 6) {
            LangManager.setLanguage(LangManager.getCurrent() == LangManager.Language.RU ? LangManager.Language.EN : LangManager.Language.RU);
            return true;
        }

        for (CategoryPanel panel : panels) {
            panel.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (closing) return false;
        for (CategoryPanel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (closing) return false;
        for (CategoryPanel panel : panels) {
            panel.mouseDragged(mouseX, mouseY, button);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE && !closing) {
            startCloseAnimation();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (!closing) {
            startCloseAnimation();
        }
    }

    private void startCloseAnimation() {
        closing = true;
        glassAnimation = new GlassAnimation(width, height);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

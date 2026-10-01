package me.cat.client.gui.components;

import me.cat.client.CatClient;
import me.cat.client.language.LangManager;
import me.cat.client.module.Category;
import me.cat.client.module.Module;
import me.cat.client.module.settings.BooleanSetting;
import me.cat.client.module.settings.ColorSetting;
import me.cat.client.module.settings.NumberSetting;
import me.cat.client.module.settings.Setting;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class CategoryPanel {
    private int x, y, width, height;
    private final Category category;
    private boolean open = true;
    private boolean dragging = false;
    private double dragX, dragY;
    private final List<Module> modules;
    private Module expandedModule = null;

    public CategoryPanel(int x, int y, int width, int height, Category category) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.category = category;
        this.modules = CatClient.moduleManager.getModulesByCategory(category);
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Тень/фон заголовка
        ctx.fill(x + 2, y + 2, x + width, y + height, 0x88000000);
        ctx.fill(x, y, x + width, y + height, 0xFF333333);
        // Акцентная полоска сверху
        ctx.fill(x, y, x + width, y + 2, 0xFFFFAA00);

        String title = LangManager.get("gui." + category.name().toLowerCase());
        ctx.drawTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer, title, x + 6, y + 6, 0xFFFFAA00);

        if (!open) return;

        int offset = height;
        for (Module m : modules) {
            int my = y + offset;
            // Фон модуля
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= my && mouseY <= my + 16;
            int bg = m.isEnabled() ? 0xDD2E7D32 : (hovered ? 0xDD444444 : 0xDD333333);
            ctx.fill(x, my, x + width, my + 16, bg);
            ctx.drawTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer, m.getName(), x + 6, my + 4, 0xFFFFFFFF);
            offset += 16;

            // Настройки если раскрыт
            if (expandedModule == m) {
                for (Setting<?> s : m.getSettings()) {
                    int sy = y + offset;
                    ctx.fill(x + 4, sy, x + width - 4, sy + 14, 0xDD222222);
                    String label = s.getName() + ": ";
                    if (s instanceof BooleanSetting) {
                        boolean val = ((BooleanSetting) s).getValue();
                        int toggleColor = val ? 0xFF4CAF50 : 0xFF555555;
                        ctx.fill(x + width - 22, sy + 2, x + width - 6, sy + 12, toggleColor);
                        ctx.drawTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer, label, x + 8, sy + 3, 0xFFCCCCCC);
                    } else if (s instanceof NumberSetting) {
                        double val = ((NumberSetting) s).getValue();
                        label += String.format("%.2f", val);
                        ctx.drawTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer, label, x + 8, sy + 3, 0xFFCCCCCC);
                    } else if (s instanceof ColorSetting) {
                        int val = ((ColorSetting) s).getValue();
                        ctx.fill(x + width - 22, sy + 2, x + width - 6, sy + 12, val | 0xFF000000);
                        ctx.drawTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer, label, x + 8, sy + 3, 0xFFCCCCCC);
                    }
                    offset += 14;
                }
            }
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            if (button == 1) {
                open = !open;
                return;
            }
            dragging = true;
            dragX = mouseX - x;
            dragY = mouseY - y;
            return;
        }

        if (!open) return;
        int offset = height;
        for (Module m : modules) {
            int my = y + offset;
            if (mouseX >= x && mouseX <= x + width && mouseY >= my && mouseY <= my + 16) {
                if (button == 0) {
                    m.toggle();
                } else if (button == 1) {
                    expandedModule = (expandedModule == m) ? null : m;
                }
                return;
            }
            offset += 16;

            if (expandedModule == m) {
                for (Setting<?> s : m.getSettings()) {
                    int sy = y + offset;
                    if (mouseX >= x + width - 22 && mouseX <= x + width - 6 && mouseY >= sy + 2 && mouseY <= sy + 12) {
                        if (s instanceof BooleanSetting) {
                            ((BooleanSetting) s).setValue(!((BooleanSetting) s).getValue());
                            return;
                        }
                    }
                    offset += 14;
                }
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    public void mouseDragged(double mouseX, double mouseY, int button) {
        if (dragging) {
            x = (int) (mouseX - dragX);
            y = (int) (mouseY - dragY);
        }
    }
}

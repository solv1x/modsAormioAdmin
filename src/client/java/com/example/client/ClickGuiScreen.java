package com.example.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClickGuiScreen extends Screen {

    // ===== Модули — как в оригинале. Статические поля сохранены,
    // потому что на них ссылается другой код (CheckCommand и т.д.) =====
    public static final Module FULLBRIGHT    = new Module("Fullbright", "Визуал");
    public static final Module CHECK_COMMAND = new Module("Check Command", "Проверки");
    public static final Module PLAYER_MENU   = new Module("Player Context Menu", "Проверки");

    private final List<Module> allModules = new ArrayList<>();
    private final List<Module> filteredModules = new ArrayList<>();

    // ===== Цвета =====
    private static final int COLOR_OVERLAY    = 0x90000000;
    private static final int COLOR_PANEL_BG   = 0xFF1E1E1E;
    private static final int COLOR_PANEL_HEAD = 0xFF2D2D2D;
    private static final int COLOR_BORDER     = 0xFF555555;
    private static final int COLOR_ROW_BG     = 0xFF2A2A2A;
    private static final int COLOR_ROW_HOVER  = 0xFF3A3A3A;
    private static final int COLOR_TAB_ACTIVE = 0xFF4A6FA5;
    private static final int COLOR_TAB_IDLE   = 0xFF333333;
    private static final int COLOR_ON         = 0xFF4CAF50;
    private static final int COLOR_OFF        = 0xFF606060;
    private static final int COLOR_TEXT       = 0xFFFFFFFF;
    private static final int COLOR_TEXT_DIM   = 0xFFAAAAAA;

    // ===== Геометрия =====
    private static final int PANEL_WIDTH     = 220;
    private static final int TITLE_HEIGHT    = 24;
    private static final int TAB_HEIGHT      = 18;
    private static final int SEARCH_HEIGHT   = 18;
    private static final int ROW_HEIGHT      = 20;
    private static final int ROW_GAP         = 4;
    private static final int PADDING         = 8;
    private static final int MAX_LIST_HEIGHT = 200; // дальше — скролл

    private int panelX = -1;
    private int panelY = -1;
    private int panelHeight;

    private String selectedCategory = "Все";
    private EditBox searchBox;
    private int scrollOffset = 0;

    private boolean dragging = false;
    private double dragX, dragY;

    public ClickGuiScreen() {
        super(Component.literal("Панель управления"));
        allModules.add(FULLBRIGHT);
        allModules.add(CHECK_COMMAND);
        allModules.add(PLAYER_MENU);
    }

    @Override
    protected void init() {
        super.init();

        // Позиция по центру только при первом открытии, дальше сохраняется
        if (panelX < 0 || panelY < 0) {
            panelX = (this.width - PANEL_WIDTH) / 2;
            panelY = (this.height - 220) / 2;
        }

        searchBox = new EditBox(this.font,
                panelX + PADDING, panelY + TITLE_HEIGHT + TAB_HEIGHT + PADDING,
                PANEL_WIDTH - PADDING * 2, SEARCH_HEIGHT, Component.literal("Поиск"));
        searchBox.setHint(Component.literal("Поиск..."));
        searchBox.setMaxLength(32);
        // Пересоздавать виджеты при вводе не нужно — фильтр пересчитывается в render()
        searchBox.setResponder(text -> scrollOffset = 0);
        this.addRenderableWidget(searchBox);
        this.setFocused(searchBox);
    }

    /** Пересчитывает отфильтрованный список. Вызывается каждый кадр перед отрисовкой. */
    private void computeFiltered() {
        filteredModules.clear();
        String query = searchBox != null ? searchBox.getValue().toLowerCase(Locale.ROOT).trim() : "";

        for (Module m : allModules) {
            boolean matchesCategory = selectedCategory.equals("Все")
                    || m.getCategory().equals(selectedCategory);
            boolean matchesSearch = query.isEmpty()
                    || m.getName().toLowerCase(Locale.ROOT).contains(query);
            if (matchesCategory && matchesSearch) {
                filteredModules.add(m);
            }
        }
        clampScroll();
    }

    private void clampScroll() {
        int visibleRows = visibleRowCount();
        int max = Math.max(0, filteredModules.size() - visibleRows);
        scrollOffset = Math.min(Math.max(0, scrollOffset), max);
    }

    private int visibleRowCount() {
        int available = Math.min(MAX_LIST_HEIGHT, this.height - panelY - panelHeaderHeight() - 24);
        return Math.max(1, available / (ROW_HEIGHT + ROW_GAP));
    }

    private int panelHeaderHeight() {
        return TITLE_HEIGHT + TAB_HEIGHT + SEARCH_HEIGHT + PADDING;
    }

    /** Категории собираются из модулей — новый модуль появится во вкладках сам. */
    private List<String> getTabs() {
        List<String> tabs = new ArrayList<>();
        tabs.add("Все");
        for (Module m : allModules) {
            if (!tabs.contains(m.getCategory())) {
                tabs.add(m.getCategory());
            }
        }
        return tabs;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        computeFiltered();

        graphics.fill(0, 0, this.width, this.height, COLOR_OVERLAY);

        int visibleRows = visibleRowCount();
        int listHeight = Math.min(filteredModules.size(), visibleRows) * (ROW_HEIGHT + ROW_GAP) - ROW_GAP;
        panelHeight = panelHeaderHeight() + Math.max(0, listHeight) + PADDING;

        // Панель + рамка
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, COLOR_PANEL_BG);
        drawBorder(graphics, panelX, panelY, PANEL_WIDTH, panelHeight, COLOR_BORDER, 1);

        // Заголовок — зона перетаскивания
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + TITLE_HEIGHT, COLOR_PANEL_HEAD);
        graphics.drawString(this.font, "ClickGUI", panelX + PADDING, panelY + 8, COLOR_TEXT, false);

        renderTabs(graphics, mouseX, mouseY);
        renderModuleList(graphics, mouseX, mouseY);

        super.render(graphics, mouseX, mouseY, delta);

        graphics.drawString(this.font, "Моды от wexTooLikee - бустят.", 10, this.height - 20, COLOR_TEXT_DIM, false);
    }

    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        int tabX = panelX + PADDING;
        int tabY = panelY + TITLE_HEIGHT + 2;

        for (String tab : getTabs()) {
            int tabWidth = this.font.width(tab) + 10;
            boolean hovered = mouseX >= tabX && mouseX <= tabX + tabWidth
                    && mouseY >= tabY && mouseY <= tabY + TAB_HEIGHT - 2;
            boolean active = selectedCategory.equals(tab);
            int bg = active ? COLOR_TAB_ACTIVE : hovered ? COLOR_ROW_HOVER : COLOR_TAB_IDLE;
            graphics.fill(tabX, tabY, tabX + tabWidth, tabY + TAB_HEIGHT - 2, bg);
            graphics.drawString(this.font, tab, tabX + 5, tabY + 5, COLOR_TEXT, false);
            tabX += tabWidth + 4;
        }
    }

    private void renderModuleList(GuiGraphics graphics, int mouseX, int mouseY) {
        int startY = panelY + panelHeaderHeight();
        int btnX = panelX + PADDING + 2;
        int btnWidth = PANEL_WIDTH - (PADDING + 2) * 2;

        for (int i = 0; i < filteredModules.size(); i++) {
            Module module = filteredModules.get(i);

            // Пропускаем строки вне видимой области (скролл)
            if (i < scrollOffset) continue;
            if (i >= scrollOffset + visibleRowCount()) break;

            int y = startY + (i - scrollOffset) * (ROW_HEIGHT + ROW_GAP);
            boolean hovered = mouseX >= btnX && mouseX <= btnX + btnWidth
                    && mouseY >= y && mouseY <= y + ROW_HEIGHT;

            // Фон строки с подсветкой при наведении
            graphics.fill(btnX, y, btnX + btnWidth, y + ROW_HEIGHT,
                    hovered ? COLOR_ROW_HOVER : COLOR_ROW_BG);

            // Индикатор состояния справа
            int indicatorX = btnX + btnWidth - 12;
            graphics.fill(indicatorX, y + 6, indicatorX + 6, y + ROW_HEIGHT - 6,
                    module.isEnabled() ? COLOR_ON : COLOR_OFF);

            // Название модуля
            graphics.drawString(this.font, module.getName(), btnX + 6, y + 6, COLOR_TEXT, false);
        }

        // Скроллбар, если модулей больше, чем влезает
        if (filteredModules.size() > visibleRowCount()) {
            int trackX = panelX + PANEL_WIDTH - 5;
            int trackTop = startY;
            int trackHeight = visibleRowCount() * (ROW_HEIGHT + ROW_GAP) - ROW_GAP;
            graphics.fill(trackX, trackTop, trackX + 3, trackTop + trackHeight, 0xFF333333);

            int thumbHeight = Math.max(12, trackHeight * visibleRowCount() / filteredModules.size());
            int thumbY = trackTop + scrollOffset * (trackHeight - thumbHeight)
                    / (filteredModules.size() - visibleRowCount());
            graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbHeight, 0xFF777777);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        // Перетаскивание за заголовок
        if (mouseY >= panelY && mouseY <= panelY + TITLE_HEIGHT
                && mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH) {
            dragging = true;
            dragX = mouseX - panelX;
            dragY = mouseY - panelY;
            searchBox.setFocused(false);
            return true;
        }

        // Клик по вкладкам
        int tabY = panelY + TITLE_HEIGHT + 2;
        if (mouseY >= tabY && mouseY <= tabY + TAB_HEIGHT - 2
                && mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH) {
            int tabX = panelX + PADDING;
            for (String tab : getTabs()) {
                int tabWidth = this.font.width(tab) + 10;
                if (mouseX >= tabX && mouseX <= tabX + tabWidth) {
                    selectedCategory = tab;
                    scrollOffset = 0;
                    searchBox.setFocused(false);
                    return true;
                }
                tabX += tabWidth + 4;
            }
        }

        // Клик по строке модуля — переключение
        int startY = panelY + panelHeaderHeight();
        int btnX = panelX + PADDING + 2;
        int btnWidth = PANEL_WIDTH - (PADDING + 2) * 2;
        for (int i = 0; i < filteredModules.size(); i++) {
            if (i < scrollOffset) continue;
            if (i >= scrollOffset + visibleRowCount()) break;
            int y = startY + (i - scrollOffset) * (ROW_HEIGHT + ROW_GAP);
            if (mouseX >= btnX && mouseX <= btnX + btnWidth
                    && mouseY >= y && mouseY <= y + ROW_HEIGHT) {
                filteredModules.get(i).toggle();
                ConfigManager.save();
                searchBox.setFocused(false);
                return true;
            }
        }

        // Клик мимо панели — снимаем фокус с поиска
        if (mouseX < panelX || mouseX > panelX + PANEL_WIDTH || mouseY > panelY + panelHeight) {
            searchBox.setFocused(false);
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging) {
            // Клампим в границы экрана — панель не уедет
            panelX = Math.min(Math.max(0, (int) (event.x() - this.dragX)), this.width - PANEL_WIDTH);
            panelY = Math.min(Math.max(0, (int) (event.y() - this.dragY)), this.height - panelHeight);
            // Виджеты пересоздавать не нужно — всё рисуется от panelX/panelY
            searchBox.setPosition(panelX + PADDING, panelY + TITLE_HEIGHT + TAB_HEIGHT + PADDING);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Прокрутка списка, если курсор над панелью
        if (mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH
                && mouseY >= panelY && mouseY <= panelY + panelHeight) {
            scrollOffset -= (int) Math.signum(scrollY);
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void tick() {
        super.tick();
        if (searchBox != null) {
            searchBox.toString(); // мигающий курсор в поле поиска
        }
    }

    @Override
    public void onClose() {
        // Здесь удобно сохранить позицию панели:
        // ConfigManager.savePanelPos(panelX, panelY);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color, int thickness) {
        graphics.fill(x, y, x + width, y + thickness, color);
        graphics.fill(x, y + height - thickness, x + width, y + height, color);
        graphics.fill(x, y, x + thickness, y + height, color);
        graphics.fill(x + width - thickness, y, x + width, y + height, color);
    }
}

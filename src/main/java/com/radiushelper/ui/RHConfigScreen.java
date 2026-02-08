package com.radiushelper.ui;

import com.radiushelper.config.RHConfig;
import com.radiushelper.config.RHConfigStorage;
import com.radiushelper.selection.SelectionState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RHConfigScreen extends Screen {
    private static final int FIELD_WIDTH = 58;
    private static final int FIELD_HEIGHT = 18;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen parent;
    private Tab tab = Tab.GENERAL;

    private final List<ClickableWidget> generalWidgets = new ArrayList<>();
    private final List<ClickableWidget> colorWidgets = new ArrayList<>();

    private TextFieldWidget privateSizeField;
    private TextFieldWidget tntRadiusField;

    private ButtonWidget fillButton;
    private ButtonWidget outlineButton;
    private ButtonWidget xrayButton;
    private ButtonWidget selectionBoxButton;
    private ButtonWidget pointMarkersButton;

    private TextFieldWidget selectionRField;
    private TextFieldWidget selectionGField;
    private TextFieldWidget selectionBField;
    private TextFieldWidget selectionHexField;
    private TextFieldWidget selectionAlphaField;

    private TextFieldWidget privateRField;
    private TextFieldWidget privateGField;
    private TextFieldWidget privateBField;
    private TextFieldWidget privateHexField;
    private TextFieldWidget privateAlphaField;

    private TextFieldWidget tntRField;
    private TextFieldWidget tntGField;
    private TextFieldWidget tntBField;
    private TextFieldWidget tntHexField;
    private TextFieldWidget tntAlphaField;

    private ButtonWidget generalTabButton;
    private ButtonWidget colorTabButton;
    private ButtonWidget doneButton;

    private String errorMessage;
    private boolean syncingFields;

    public RHConfigScreen(Screen parent) {
        super(Text.translatable("screen.radius_helper.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearChildren();
        generalWidgets.clear();
        colorWidgets.clear();

        RHConfig config = RHConfigStorage.get();
        int panelWidth = Math.min(500, width - 40);
        int panelHeight = Math.min(300, height - 40);
        int panelX = (width - panelWidth) / 2;
        int panelY = (height - panelHeight) / 2;
        int headerY = panelY + 10;
        int contentTop = panelY + 44;
        int innerX = panelX + 18;
        int innerW = panelWidth - 36;

        generalTabButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.radius_helper.tab.general"),
            button -> switchTab(Tab.GENERAL))
            .dimensions(panelX + panelWidth - 172, headerY - 2, 78, BUTTON_HEIGHT)
            .build());

        colorTabButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.radius_helper.tab.colors"),
            button -> switchTab(Tab.COLORS))
            .dimensions(panelX + panelWidth - 88, headerY - 2, 78, BUTTON_HEIGHT)
            .build());

        CyclingButtonWidget.Builder<RHConfig.Mode> modeBuilder = CyclingButtonWidget.builder(
            (RHConfig.Mode mode) -> Text.translatable("screen.radius_helper.mode." + mode.name().toLowerCase()));
        CyclingButtonWidget<RHConfig.Mode> modeButton = modeBuilder
            .values(RHConfig.Mode.PRIVATE, RHConfig.Mode.TNT)
            .initially(config.mode)
            .build(innerX, contentTop, innerW, BUTTON_HEIGHT,
                Text.translatable("screen.radius_helper.mode"), (button, value) -> {
                    config.mode = value;
                    SelectionState.get().markDirty();
                    RHConfigStorage.save();
                });
        addGeneral(modeButton);

        int rowY = contentTop + 44;
        int leftX = innerX;
        int rightX = innerX + innerW / 2 + 8;

        privateSizeField = createNumberField(leftX, rowY, config.privateSize, NumberField.PRIVATE_SIZE, InputType.INT);
        tntRadiusField = createNumberField(rightX, rowY, config.tntRadius, NumberField.TNT_RADIUS, InputType.INT);
        addGeneral(privateSizeField);
        addGeneral(tntRadiusField);

        rowY += 36;
        fillButton = addGeneral(ButtonWidget.builder(toggleLabel("screen.radius_helper.render_fill", config.renderFill),
            button -> toggleFlag(Flag.RENDER_FILL))
            .dimensions(leftX, rowY, 110, BUTTON_HEIGHT).build());
        outlineButton = addGeneral(ButtonWidget.builder(toggleLabel("screen.radius_helper.render_outline", config.renderOutline),
            button -> toggleFlag(Flag.RENDER_OUTLINE))
            .dimensions(rightX, rowY, 110, BUTTON_HEIGHT).build());

        rowY += 28;
        xrayButton = addGeneral(ButtonWidget.builder(toggleLabel("screen.radius_helper.xray", config.xray),
            button -> toggleFlag(Flag.XRAY))
            .dimensions(leftX, rowY, 110, BUTTON_HEIGHT).build());
        selectionBoxButton = addGeneral(ButtonWidget.builder(toggleLabel("screen.radius_helper.show_selection_box", config.showSelectionBox),
            button -> toggleFlag(Flag.SHOW_SELECTION_BOX))
            .dimensions(rightX, rowY, 110, BUTTON_HEIGHT).build());

        rowY += 28;
        pointMarkersButton = addGeneral(ButtonWidget.builder(toggleLabel("screen.radius_helper.show_points", config.showPointMarkers),
            button -> toggleFlag(Flag.SHOW_POINTS))
            .dimensions(leftX, rowY, 110, BUTTON_HEIGHT).build());

        int colorTop = contentTop + 36;
        int groupGap = 64;
        int fieldGap = FIELD_WIDTH + 6;

        selectionRField = createNumberField(innerX, colorTop, getR(config.selectionColor), NumberField.SELECTION_R, InputType.BYTE);
        selectionGField = createNumberField(innerX + fieldGap, colorTop, getG(config.selectionColor), NumberField.SELECTION_G, InputType.BYTE);
        selectionBField = createNumberField(innerX + fieldGap * 2, colorTop, getB(config.selectionColor), NumberField.SELECTION_B, InputType.BYTE);
        selectionHexField = createHexField(innerX + fieldGap * 3, colorTop, config.selectionColor, NumberField.SELECTION_HEX);
        selectionAlphaField = createNumberField(innerX + fieldGap * 4, colorTop, config.selectionAlpha, NumberField.SELECTION_ALPHA, InputType.BYTE);
        addColor(selectionRField);
        addColor(selectionGField);
        addColor(selectionBField);
        addColor(selectionHexField);
        addColor(selectionAlphaField);

        int privateTop = colorTop + groupGap;
        privateRField = createNumberField(innerX, privateTop, getR(config.privateColor), NumberField.PRIVATE_R, InputType.BYTE);
        privateGField = createNumberField(innerX + fieldGap, privateTop, getG(config.privateColor), NumberField.PRIVATE_G, InputType.BYTE);
        privateBField = createNumberField(innerX + fieldGap * 2, privateTop, getB(config.privateColor), NumberField.PRIVATE_B, InputType.BYTE);
        privateHexField = createHexField(innerX + fieldGap * 3, privateTop, config.privateColor, NumberField.PRIVATE_HEX);
        privateAlphaField = createNumberField(innerX + fieldGap * 4, privateTop, config.privateAlpha, NumberField.PRIVATE_ALPHA, InputType.BYTE);
        addColor(privateRField);
        addColor(privateGField);
        addColor(privateBField);
        addColor(privateHexField);
        addColor(privateAlphaField);

        int tntTop = privateTop + groupGap;
        tntRField = createNumberField(innerX, tntTop, getR(config.tntColor), NumberField.TNT_R, InputType.BYTE);
        tntGField = createNumberField(innerX + fieldGap, tntTop, getG(config.tntColor), NumberField.TNT_G, InputType.BYTE);
        tntBField = createNumberField(innerX + fieldGap * 2, tntTop, getB(config.tntColor), NumberField.TNT_B, InputType.BYTE);
        tntHexField = createHexField(innerX + fieldGap * 3, tntTop, config.tntColor, NumberField.TNT_HEX);
        tntAlphaField = createNumberField(innerX + fieldGap * 4, tntTop, config.tntAlpha, NumberField.TNT_ALPHA, InputType.BYTE);
        addColor(tntRField);
        addColor(tntGField);
        addColor(tntBField);
        addColor(tntHexField);
        addColor(tntAlphaField);

        doneButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.radius_helper.done"),
            button -> close())
            .dimensions(panelX + panelWidth - 90, panelY + panelHeight - 30, 70, BUTTON_HEIGHT).build());

        applyTabVisibility();
    }

    @Override
    public void close() {
        RHConfigStorage.save();
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = Math.min(500, width - 40);
        int panelHeight = Math.min(300, height - 40);
        int panelX = (width - panelWidth) / 2;
        int panelY = (height - panelHeight) / 2;
        int innerX = panelX + 18;
        int innerW = panelWidth - 36;

        context.fillGradient(0, 0, width, height, 0xFF0E111A, 0xFF1A2233);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC10141F);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 30, 0xFF1A2130);

        context.drawText(textRenderer, title, panelX + 12, panelY + 10, 0xF2F4F8, false);

        if (tab == Tab.GENERAL) {
            int contentTop = panelY + 44;
            int rowY = contentTop + 44;
            int leftX = innerX;
            int rightX = innerX + innerW / 2 + 8;
            context.drawText(textRenderer, Text.translatable("screen.radius_helper.private_size"), leftX, rowY - 12, 0xB7C0D6, false);
            context.drawText(textRenderer, Text.translatable("screen.radius_helper.tnt_radius"), rightX, rowY - 12, 0xB7C0D6, false);
        } else {
            int contentTop = panelY + 44;
            int groupGap = 64;
            int labelY = contentTop + 6;
            int groupY = contentTop + 26;
            int fieldGap = FIELD_WIDTH + 6;

            context.drawText(textRenderer, Text.translatable("screen.radius_helper.color.selection"), innerX, labelY, 0xB7C0D6, false);
            drawRGBLabels(context, innerX, groupY - 12, fieldGap);
            drawColorSwatch(context, innerX + fieldGap * 5 + 8, groupY + 1, RHConfigStorage.get().selectionColor);

            int privateTop = groupY + groupGap;
            context.drawText(textRenderer, Text.translatable("screen.radius_helper.color.private"), innerX, labelY + groupGap, 0xB7C0D6, false);
            drawRGBLabels(context, innerX, privateTop - 12, fieldGap);
            drawColorSwatch(context, innerX + fieldGap * 5 + 8, privateTop + 1, RHConfigStorage.get().privateColor);

            int tntTop = privateTop + groupGap;
            context.drawText(textRenderer, Text.translatable("screen.radius_helper.color.tnt"), innerX, labelY + groupGap * 2, 0xB7C0D6, false);
            drawRGBLabels(context, innerX, tntTop - 12, fieldGap);
            drawColorSwatch(context, innerX + fieldGap * 5 + 8, tntTop + 1, RHConfigStorage.get().tntColor);
        }

        if (errorMessage != null) {
            context.drawText(textRenderer, Text.literal(errorMessage).formatted(Formatting.RED), panelX + 18, panelY + panelHeight - 25, 0xFF4D4D, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawRGBLabels(DrawContext context, int startX, int y, int fieldGap) {
        context.drawText(textRenderer, Text.translatable("screen.radius_helper.color.r"), startX + 2, y, 0x8C95AA, false);
        context.drawText(textRenderer, Text.translatable("screen.radius_helper.color.g"), startX + fieldGap + 2, y, 0x8C95AA, false);
        context.drawText(textRenderer, Text.translatable("screen.radius_helper.color.b"), startX + fieldGap * 2 + 2, y, 0x8C95AA, false);
        context.drawText(textRenderer, Text.translatable("screen.radius_helper.color.hex"), startX + fieldGap * 3 + 2, y, 0x8C95AA, false);
        context.drawText(textRenderer, Text.translatable("screen.radius_helper.color.alpha"), startX + fieldGap * 4 + 2, y, 0x8C95AA, false);
    }

    private void drawColorSwatch(DrawContext context, int x, int y, int rgb) {
        int color = 0xFF000000 | (rgb & 0xFFFFFF);
        context.fill(x, y, x + 18, y + 18, 0xFF10141F);
        context.fill(x + 2, y + 2, x + 16, y + 16, color);
    }

    private void switchTab(Tab newTab) {
        if (tab == newTab) {
            return;
        }
        tab = newTab;
        applyTabVisibility();
    }

    private void applyTabVisibility() {
        boolean general = tab == Tab.GENERAL;
        for (ClickableWidget widget : generalWidgets) {
            widget.visible = general;
            widget.active = general;
        }
        for (ClickableWidget widget : colorWidgets) {
            widget.visible = !general;
            widget.active = !general;
        }
        generalTabButton.active = !general;
        colorTabButton.active = general;
    }

    private <T extends ClickableWidget> T addGeneral(T widget) {
        addDrawableChild(widget);
        generalWidgets.add(widget);
        return widget;
    }

    private <T extends ClickableWidget> T addColor(T widget) {
        addDrawableChild(widget);
        colorWidgets.add(widget);
        return widget;
    }

    private TextFieldWidget createNumberField(int x, int y, int value, NumberField field, InputType type) {
        TextFieldWidget widget = new TextFieldWidget(textRenderer, x, y, FIELD_WIDTH, FIELD_HEIGHT, Text.empty());
        widget.setText(Integer.toString(value));
        widget.setTextPredicate(text -> isNumericInput(text, type));
        widget.setChangedListener(text -> onNumberChanged(text, field));
        return widget;
    }

    private TextFieldWidget createHexField(int x, int y, int value, NumberField field) {
        TextFieldWidget widget = new TextFieldWidget(textRenderer, x, y, FIELD_WIDTH, FIELD_HEIGHT, Text.empty());
        widget.setText(colorToHex(value));
        widget.setTextPredicate(RHConfigScreen::isHexInput);
        widget.setChangedListener(text -> onNumberChanged(text, field));
        return widget;
    }

    private void toggleFlag(Flag flag) {
        RHConfig config = RHConfigStorage.get();
        switch (flag) {
            case RENDER_FILL -> config.renderFill = !config.renderFill;
            case RENDER_OUTLINE -> config.renderOutline = !config.renderOutline;
            case XRAY -> config.xray = !config.xray;
            case SHOW_SELECTION_BOX -> config.showSelectionBox = !config.showSelectionBox;
            case SHOW_POINTS -> config.showPointMarkers = !config.showPointMarkers;
        }
        SelectionState.get().markDirty();
        RHConfigStorage.save();
        refreshToggleLabels();
    }

    private void refreshToggleLabels() {
        RHConfig config = RHConfigStorage.get();
        fillButton.setMessage(toggleLabel("screen.radius_helper.render_fill", config.renderFill));
        outlineButton.setMessage(toggleLabel("screen.radius_helper.render_outline", config.renderOutline));
        xrayButton.setMessage(toggleLabel("screen.radius_helper.xray", config.xray));
        selectionBoxButton.setMessage(toggleLabel("screen.radius_helper.show_selection_box", config.showSelectionBox));
        pointMarkersButton.setMessage(toggleLabel("screen.radius_helper.show_points", config.showPointMarkers));
    }

    private Text toggleLabel(String key, boolean value) {
        String suffix = value ? "ON" : "OFF";
        return Text.literal(Text.translatable(key).getString() + ": " + suffix);
    }

    private void onNumberChanged(String value, NumberField field) {
        if (syncingFields) {
            return;
        }
        RHConfig config = RHConfigStorage.get();
        if (value.isEmpty() || value.equals("#")) {
            errorMessage = null;
            return;
        }
        try {
            switch (field) {
                case PRIVATE_SIZE -> config.privateSize = Integer.parseInt(value);
                case TNT_RADIUS -> config.tntRadius = Integer.parseInt(value);
                case SELECTION_R -> config.selectionColor = updateRgbChannel(config.selectionColor, Channel.R, parseByte(value));
                case SELECTION_G -> config.selectionColor = updateRgbChannel(config.selectionColor, Channel.G, parseByte(value));
                case SELECTION_B -> config.selectionColor = updateRgbChannel(config.selectionColor, Channel.B, parseByte(value));
                case SELECTION_HEX -> config.selectionColor = parseHex(value);
                case SELECTION_ALPHA -> config.selectionAlpha = parseByte(value);
                case PRIVATE_R -> config.privateColor = updateRgbChannel(config.privateColor, Channel.R, parseByte(value));
                case PRIVATE_G -> config.privateColor = updateRgbChannel(config.privateColor, Channel.G, parseByte(value));
                case PRIVATE_B -> config.privateColor = updateRgbChannel(config.privateColor, Channel.B, parseByte(value));
                case PRIVATE_HEX -> config.privateColor = parseHex(value);
                case PRIVATE_ALPHA -> config.privateAlpha = parseByte(value);
                case TNT_R -> config.tntColor = updateRgbChannel(config.tntColor, Channel.R, parseByte(value));
                case TNT_G -> config.tntColor = updateRgbChannel(config.tntColor, Channel.G, parseByte(value));
                case TNT_B -> config.tntColor = updateRgbChannel(config.tntColor, Channel.B, parseByte(value));
                case TNT_HEX -> config.tntColor = parseHex(value);
                case TNT_ALPHA -> config.tntAlpha = parseByte(value);
            }
            config.clamp();
            syncColorFields(field, config);
            SelectionState.get().markDirty();
            RHConfigStorage.save();
            errorMessage = null;
        } catch (NumberFormatException ex) {
            errorMessage = Text.translatable("screen.radius_helper.invalid_number").getString();
        }
    }

    private void syncColorFields(NumberField changed, RHConfig config) {
        if (changed.isSelection()) {
            syncRgbFields(selectionRField, selectionGField, selectionBField, selectionHexField, config.selectionColor);
        } else if (changed.isPrivate()) {
            syncRgbFields(privateRField, privateGField, privateBField, privateHexField, config.privateColor);
        } else if (changed.isTnt()) {
            syncRgbFields(tntRField, tntGField, tntBField, tntHexField, config.tntColor);
        }
    }

    private void syncRgbFields(TextFieldWidget rField, TextFieldWidget gField, TextFieldWidget bField,
                               TextFieldWidget hexField, int color) {
        syncingFields = true;
        rField.setText(Integer.toString(getR(color)));
        gField.setText(Integer.toString(getG(color)));
        bField.setText(Integer.toString(getB(color)));
        hexField.setText(colorToHex(color));
        syncingFields = false;
    }

    private static boolean isNumericInput(String value, InputType type) {
        if (value.isEmpty()) {
            return true;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        try {
            int parsed = Integer.parseInt(value);
            if (type == InputType.BYTE) {
                return parsed >= 0 && parsed <= 255;
            }
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static boolean isHexInput(String value) {
        if (value.isEmpty() || value.equals("#")) {
            return true;
        }
        String v = value.startsWith("#") ? value.substring(1) : value;
        if (v.length() > 6) {
            return false;
        }
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (!isHexChar(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHexChar(char c) {
        return (c >= '0' && c <= '9')
            || (c >= 'a' && c <= 'f')
            || (c >= 'A' && c <= 'F');
    }

    private static int parseByte(String value) {
        int parsed = Integer.parseInt(value);
        if (parsed < 0) {
            return 0;
        }
        if (parsed > 255) {
            return 255;
        }
        return parsed;
    }

    private static int parseHex(String value) {
        String v = value.startsWith("#") ? value.substring(1) : value;
        if (v.isEmpty()) {
            return 0;
        }
        int parsed = Integer.parseInt(v, 16);
        return parsed & 0xFFFFFF;
    }

    private static int updateRgbChannel(int color, Channel channel, int value) {
        int r = getR(color);
        int g = getG(color);
        int b = getB(color);
        switch (channel) {
            case R -> r = value;
            case G -> g = value;
            case B -> b = value;
        }
        return (r << 16) | (g << 8) | b;
    }

    private static int getR(int color) {
        return (color >> 16) & 0xFF;
    }

    private static int getG(int color) {
        return (color >> 8) & 0xFF;
    }

    private static int getB(int color) {
        return color & 0xFF;
    }

    private static String colorToHex(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }

    private enum Flag {
        RENDER_FILL,
        RENDER_OUTLINE,
        XRAY,
        SHOW_SELECTION_BOX,
        SHOW_POINTS
    }

    private enum NumberField {
        PRIVATE_SIZE,
        TNT_RADIUS,
        SELECTION_R,
        SELECTION_G,
        SELECTION_B,
        SELECTION_HEX,
        SELECTION_ALPHA,
        PRIVATE_R,
        PRIVATE_G,
        PRIVATE_B,
        PRIVATE_HEX,
        PRIVATE_ALPHA,
        TNT_R,
        TNT_G,
        TNT_B,
        TNT_HEX,
        TNT_ALPHA;

        boolean isSelection() {
            return name().startsWith("SELECTION_");
        }

        boolean isPrivate() {
            return name().startsWith("PRIVATE_");
        }

        boolean isTnt() {
            return name().startsWith("TNT_");
        }
    }

    private enum InputType {
        INT,
        BYTE
    }

    private enum Tab {
        GENERAL,
        COLORS
    }

    private enum Channel {
        R,
        G,
        B
    }
}

package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.block.LabelerBlockEntity;
import com.github.burgerguy.recordable.shared.menu.LabelerConstants;
import com.github.burgerguy.recordable.shared.menu.LabelerMenu;
import com.github.burgerguy.recordable.shared.menu.Paint;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

public class LabelerScreen extends AbstractContainerScreen<LabelerMenu> {
    private static final ResourceLocation BG_LOCATION = new ResourceLocation(Recordable.MOD_ID, "textures/gui/labeler.png");

    private final ClientCanvas clientCanvas;

    private PaintWidget[] paintWidgets;
    private EditBox authorEditBox;
    private EditBox titleEditBox;
    private Button undoButton;
    private Button resetButton;
    private Button finishButton;

    public LabelerScreen(LabelerMenu labelerMenu, Inventory playerInventory, Component component) {
        super(labelerMenu, playerInventory, component);
        LabelerBlockEntity labelerBlockEntity = this.menu.getLabelerBlockEntity();
        this.clientCanvas = new ClientCanvas(
                labelerBlockEntity.getPixelIndexModel(),
                labelerBlockEntity.getPixelModelWidth(),
                labelerMenu.getPaintPalette(),
                playerInventory
        );

        // image height adjustment
        this.imageHeight = 182;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        for (PaintWidget paintWidget : this.paintWidgets) paintWidget.update();
        this.authorEditBox.tick();
        this.titleEditBox.tick();
        boolean canUndo = this.clientCanvas.canUndo();
        this.undoButton.active = canUndo;
        this.resetButton.active = canUndo;
        this.finishButton.active = !this.menu.getPaperSlot().getItem().isEmpty() &&
                                   !this.menu.getRecordSlot().getItem().isEmpty() &&
                                   !LabelerScreen.this.authorEditBox.getValue().isEmpty() &&
                                   !LabelerScreen.this.titleEditBox.getValue().isEmpty();
    }

    @Override
    protected void init() {
        super.init();

        // gets ScreenRenderUtil ready for render
        this.addRenderableOnly((poseStack, mouseX, mouseY, partialTick) -> {
            ScreenRenderUtil.startFills(ScreenRenderUtil.FILL_BUFFER_1);
            ScreenRenderUtil.startFills(ScreenRenderUtil.FILL_BUFFER_2);
            ScreenRenderUtil.startBlits(ScreenRenderUtil.BLIT_BUFFER_1);
        });

        Collection<Paint> paints = this.menu.getPaintPalette().getPaints();
        this.paintWidgets = new PaintWidget[paints.size()];
        int idx = 0;
        for (Paint paint : paints) {
            int xIdx = idx / LabelerConstants.PALETTE_COLUMNS_WRAP;
            int yIdx = idx % LabelerConstants.PALETTE_COLUMNS_WRAP;
            PaintWidget paintWidget = new PaintWidget(
                    this.leftPos + LabelerConstants.PALETTE_X + xIdx * (LabelerConstants.COLOR_WIDTH + LabelerConstants.COLOR_MARGIN_X),
                    this.topPos + LabelerConstants.PALETTE_Y + yIdx * (LabelerConstants.COLOR_HEIGHT + LabelerConstants.COLOR_MARGIN_Y),
                    LabelerConstants.COLOR_WIDTH,
                    LabelerConstants.COLOR_HEIGHT,
                    paint
            );
            this.paintWidgets[idx] = paintWidget;
            this.addRenderableWidget(paintWidget);
            idx++;
        }

        this.addRenderableWidget(new CanvasWidget(
                this.leftPos + 76,
                this.topPos + 29,
                LabelerConstants.PIXEL_SIZE,
                this.clientCanvas
        ));

        Font font = Minecraft.getInstance().font;

        this.authorEditBox = this.addRenderableWidget(new EditBox(font, this.leftPos + 68, this.topPos + 9, 40, 10, this.authorEditBox, new TranslatableComponent("screen.recordable.labeler.author")) {
            @Override
            public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                super.renderButton(poseStack, mouseX, mouseY, partialTick);

                if (!this.isFocused() && this.getValue().isEmpty()) {
                    // mc does this, we should too
                    //noinspection IntegerDivisionInFloatingPointContext
                    font.draw(poseStack, this.getMessage(), this.x + 4, this.y + (this.height - 8) / 2, 0x707070);
                }
            }
        });

        this.titleEditBox = this.addRenderableWidget(new EditBox(font, this.leftPos + 111, this.topPos + 9, 57, 10, this.titleEditBox, new TranslatableComponent("screen.recordable.labeler.title")) {
            @Override
            public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                super.renderButton(poseStack, mouseX, mouseY, partialTick);

                if (!this.isFocused() && this.getValue().isEmpty()) {
                    // mc does this, we should too
                    //noinspection IntegerDivisionInFloatingPointContext
                    font.draw(poseStack, this.getMessage(), this.x + 4, this.y + (this.height - 8) / 2, 0x707070);
                }
            }
        });

        this.addRenderableWidget(new CustomToggleButtonWidget(
                this.leftPos + 7,
                this.topPos + 16,
                LabelerConstants.SMALL_BUTTON_WIDTH,
                LabelerConstants.SMALL_BUTTON_HEIGHT,
                LabelerConstants.SMALL_BUTTON_U,
                LabelerConstants.SMALL_BUTTON_V,
                176.0f,
                28.0f,
                this.clientCanvas.isErasing(),
                new TranslatableComponent("screen.recordable.labeler.eraser"),
                pressed -> {
                    this.clientCanvas.setErasing(pressed);

                    for (PaintWidget paintColorWidget : this.paintWidgets) {
                        paintColorWidget.setForceInactive(pressed);
                    }
                }
        ));

        this.addRenderableWidget(new CustomToggleButtonWidget(
                this.leftPos + 21,
                this.topPos + 16,
                LabelerConstants.SMALL_BUTTON_WIDTH,
                LabelerConstants.SMALL_BUTTON_HEIGHT,
                LabelerConstants.SMALL_BUTTON_U,
                LabelerConstants.SMALL_BUTTON_V,
                176.0f,
                40.0f,
                this.clientCanvas.isMixing(),
                new TranslatableComponent("screen.recordable.labeler.mix"),
                this.clientCanvas::setMixing
        ));

        this.undoButton = this.addRenderableWidget(new CustomButtonWidget(
                this.leftPos + 35,
                this.topPos + 16,
                LabelerConstants.SMALL_BUTTON_WIDTH,
                LabelerConstants.SMALL_BUTTON_HEIGHT,
                LabelerConstants.SMALL_BUTTON_U,
                LabelerConstants.SMALL_BUTTON_V,
                176.0f,
                52.0f,
                new TranslatableComponent("screen.recordable.labeler.undo"),
                b -> this.clientCanvas.undo()
        ));
        this.undoButton.active = false;

        this.resetButton = this.addRenderableWidget(new CustomButtonWidget(
                this.leftPos + 49,
                this.topPos + 16,
                LabelerConstants.SMALL_BUTTON_WIDTH,
                LabelerConstants.SMALL_BUTTON_HEIGHT,
                LabelerConstants.SMALL_BUTTON_U,
                LabelerConstants.SMALL_BUTTON_V,
                176.0f,
                64.0f,
                new TranslatableComponent("screen.recordable.labeler.reset"),
                b -> this.clientCanvas.reset()
        ));
        this.resetButton.active = false;

        this.finishButton = this.addRenderableWidget(new CustomButtonWidget(
                this.leftPos + 151,
                this.topPos + 67,
                LabelerConstants.LARGE_BUTTON_WIDTH,
                LabelerConstants.LARGE_BUTTON_HEIGHT,
                LabelerConstants.LARGE_BUTTON_U,
                LabelerConstants.LARGE_BUTTON_V,
                176.0f,
                94.0f,
                new TranslatableComponent("screen.recordable.labeler.finish"),
                b -> this.doFinish()
        ));
        this.finishButton.active = false; // this turns true when the paper and record are filled

        // renders everything that widgets drew using ScreenRenderUtil
        this.addRenderableOnly((poseStack, mouseX, mouseY, partialTick) -> {
            ScreenRenderUtil.endAndRenderFills(ScreenRenderUtil.FILL_BUFFER_1);
            ScreenRenderUtil.endAndRenderBlits(ScreenRenderUtil.BLIT_BUFFER_1, BG_LOCATION, 0);
            ScreenRenderUtil.endAndRenderFills(ScreenRenderUtil.FILL_BUFFER_2);
        });
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // from ContainerEventHandler
        boolean superDuperMouseDragged = this.getFocused() != null && this.isDragging() && button == 0 && this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY) || superDuperMouseDragged;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // from ContainerEventHandler
        this.setDragging(false);
        boolean superDuperMouseReleased = this.getChildAt(mouseX, mouseY).filter(guiEventListener -> guiEventListener.mouseReleased(mouseX, mouseY, button)).isPresent();
        return super.mouseReleased(mouseX, mouseY, button) || superDuperMouseReleased;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.player.closeContainer();
        } else if (this.authorEditBox.keyPressed(keyCode, scanCode, modifiers) || this.authorEditBox.canConsumeInput()) {
            return true;
        } else if (this.titleEditBox.keyPressed(keyCode, scanCode, modifiers) || this.titleEditBox.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTick, int mouseX, int mouseY) {
        this.renderBackground(matrixStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BG_LOCATION);
        int i = this.leftPos;
        int j = this.topPos;
        this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        Slot recordSlot = this.menu.getRecordSlot();
        Slot dyeSlot = this.menu.getDyeSlot();
        Slot paperSlot = this.menu.getPaperSlot();
        if (!recordSlot.hasItem()) {
            this.blit(matrixStack, i + recordSlot.x, j + recordSlot.y, this.imageWidth, 0, 16, 16);
        }
        if (!dyeSlot.hasItem()) {
            this.blit(matrixStack, i + dyeSlot.x, j + dyeSlot.y, this.imageWidth + 16, 0, 16, 16);
        }
        if (!paperSlot.hasItem()) {
            this.blit(matrixStack, i + paperSlot.x, j + paperSlot.y, this.imageWidth + 32, 0, 16, 16);
        }
    }

    public void doFinish() {
        if (this.authorEditBox == null || this.titleEditBox == null) throw new IllegalStateException("Screen not initialized");
        String author = this.authorEditBox.getValue();
        String title = this.titleEditBox.getValue();
        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.resetWriterIndex();
        buffer.writeUtf(author);
        buffer.writeUtf(title);
        this.clientCanvas.writeToPacket(buffer);
        ClientPlayNetworking.send(Recordable.FINALIZE_LABEL_ID, buffer);
        this.clientCanvas.clear();
    }

    // workaround for the double-selection bug
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        GuiEventListener lastFocused = this.getFocused();
        boolean parentClicked = super.mouseClicked(mouseX, mouseY, button);
        if (parentClicked) {
            GuiEventListener currentFocused = this.getFocused();
            if (lastFocused != null && !lastFocused.equals(currentFocused)) {
                while (lastFocused.changeFocus(true)); // removes focus from the previous element
            }
        }
        return parentClicked;
    }

}

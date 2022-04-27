package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.block.LabelerBlockEntity;
import com.github.burgerguy.recordable.shared.menu.LabelerConstants;
import com.github.burgerguy.recordable.shared.menu.LabelerMenu;
import com.github.burgerguy.recordable.shared.menu.Paint;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class LabelerScreen extends AbstractContainerScreen<LabelerMenu> {
    private static final ResourceLocation COLOR_ICON_LOCATION = new ResourceLocation(Recordable.MOD_ID, "textures/gui/labeler.png");

    private final ClientCanvas clientCanvas;

    private PaintWidget[] paintWidgets;
    private EditBox authorEditBox;
    private EditBox titleEditBox;
    private Button undoButton;
    private Button resetButton;
    private Button finishButton;

    public LabelerScreen(LabelerMenu labelerMenu, Inventory inventory, Component component) {
        super(labelerMenu, inventory, component);
        labelerMenu.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu menu, int slotId, ItemStack stack) {
                Button finishButton = LabelerScreen.this.finishButton;
                if (finishButton != null) {
                    finishButton.active = !menu.getSlot(LabelerMenu.PAPER_SLOT_ID).getItem().isEmpty() &&
                            !menu.getSlot(LabelerMenu.PAPER_SLOT_ID).getItem().isEmpty();
                }
            }

            @Override
            public void dataChanged(AbstractContainerMenu menu, int dataSlotIndex, int value) {
            }
        });
        LabelerBlockEntity labelerBlockEntity = menu.getLabelerBlockEntity();
        this.clientCanvas = new ClientCanvas(
                labelerBlockEntity.getPixelIndexModel(),
                labelerBlockEntity.getPixelModelWidth(),
                labelerMenu.getPaints()
        );
    }

    @Override
    public void containerTick() {
        for (PaintWidget paintWidget : this.paintWidgets) paintWidget.update();
        this.authorEditBox.tick();
        this.titleEditBox.tick();
        boolean canUndo = this.clientCanvas.canUndo();
        this.undoButton.active = canUndo;
        this.resetButton.active = canUndo;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        // gets ScreenRenderUtil ready for render
        this.addRenderableOnly((poseStack, mouseX, mouseY, partialTick) -> ScreenRenderUtil.startFills());

        Paint[] paints = this.menu.getPaints();
        this.paintWidgets = new PaintWidget[paints.length];
        for (int i = 0; i < paints.length; i++) {
            int xIdx = i / LabelerConstants.PALETTE_COLUMNS_WRAP;
            int yIdx = i % LabelerConstants.PALETTE_COLUMNS_WRAP;
            PaintWidget paintWidget = new PaintWidget(
                    this.leftPos + LabelerConstants.PALETTE_X + xIdx * (LabelerConstants.COLOR_WIDTH + LabelerConstants.COLOR_MARGIN_X),
                    this.topPos + LabelerConstants.PALETTE_Y + yIdx * (LabelerConstants.COLOR_HEIGHT + LabelerConstants.COLOR_MARGIN_Y),
                    LabelerConstants.COLOR_WIDTH,
                    LabelerConstants.COLOR_HEIGHT,
                    this.menu.getPaints()[i]
            );
            this.paintWidgets[i] = paintWidget;
            this.addRenderableWidget(paintWidget);
        }

        CanvasWidget canvasWidget = new CanvasWidget(this.leftPos + 70, this.topPos + 20, 16, this.clientCanvas);
        this.addRenderableWidget(canvasWidget);

        Font font = Minecraft.getInstance().font;

        this.authorEditBox = new EditBox(font, this.leftPos + 80, this.topPos - 30, 80, 12, new TranslatableComponent("screen.recordable.labeler.author"));
        this.addRenderableWidget(this.authorEditBox);

        this.titleEditBox = new EditBox(font, this.leftPos + 80, this.topPos - 15, 80, 12, new TranslatableComponent("screen.recordable.labeler.title"));
        this.addRenderableWidget(this.titleEditBox);

        this.undoButton = new Button(this.leftPos + 160, this.topPos, 16, 16, new TranslatableComponent("screen.recordable.labeler.undo"), b -> this.clientCanvas.undo());
        this.undoButton.active = false;
        this.addRenderableWidget(this.undoButton);

        Button eraseButton = new Button(this.leftPos + 160, this.topPos + 20, 16, 16, new TranslatableComponent("screen.recordable.labeler.eraser"), b -> {
            boolean erasing = this.clientCanvas.toggleErase();

            for (PaintWidget paintColorWidget : paintWidgets) {
                paintColorWidget.setForceInactive(erasing);
            }
        });
        this.addRenderableWidget(eraseButton);

        Button mixButton = new Button(this.leftPos + 160, this.topPos + 40, 16, 16, new TranslatableComponent("screen.recordable.labeler.mix"), b -> this.clientCanvas.toggleMix());
        this.addRenderableWidget(mixButton);

        this.resetButton = new Button(this.leftPos + 160, this.topPos + 60, 16, 16, new TranslatableComponent("screen.recordable.labeler.reset"), b -> this.clientCanvas.reset());
        this.resetButton.active = false;
        this.addRenderableWidget(this.resetButton);

        this.finishButton = new Button(this.leftPos + 160, this.topPos + 80, 16, 16, new TranslatableComponent("screen.recordable.labeler.finish"), b -> this.doFinish());
        this.finishButton.active = false; // this turns true when the paper and record are filled
        this.addRenderableWidget(this.finishButton);

        // renders everything that widgets drew using ScreenRenderUtil
        this.addRenderableOnly((poseStack, mouseX, mouseY, partialTick) -> ScreenRenderUtil.endAndRenderFills());
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String author = this.authorEditBox.getValue();
        String title = this.authorEditBox.getValue();
        this.init(minecraft, width, height);
        this.authorEditBox.setValue(author);
        this.titleEditBox.setValue(title);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // from ContainerEventHandler
        boolean superDuperMouseDragged = this.getFocused() != null && this.isDragging() && button == 0 && this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
        if (superDuperMouseDragged) {
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // from ContainerEventHandler
        this.setDragging(false);
        boolean superDuperMouseReleased = this.getChildAt(mouseX, mouseY).filter(guiEventListener -> guiEventListener.mouseReleased(mouseX, mouseY, button)).isPresent();
        if (superDuperMouseReleased) {
            return true;
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }
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
    protected void renderBg(PoseStack matrixStack, float partialTick, int mouseX, int mouseY) {
        // TODO: placeholder background, make real background
        fill(matrixStack, 0, 0, this.width, this.height, 0xFFCCCCCC);
    }

    public void doFinish() {
        if (this.authorEditBox == null || this.titleEditBox == null) throw new IllegalStateException("Screen not initialized");
        String author = this.authorEditBox.getValue();
        String title = this.titleEditBox.getValue();
        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeUtf(author);
        buffer.writeUtf(title);
        this.clientCanvas.writeToPacket(buffer);
        ClientPlayNetworking.send(Recordable.FINALIZE_LABEL_ID, buffer);
        this.clientCanvas.clear();
    }

}

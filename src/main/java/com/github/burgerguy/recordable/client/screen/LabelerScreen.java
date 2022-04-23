package com.github.burgerguy.recordable.client.screen;

import com.github.burgerguy.recordable.client.render.util.ScreenRenderUtil;
import com.github.burgerguy.recordable.shared.Recordable;
import com.github.burgerguy.recordable.shared.menu.LabelerConstants;
import com.github.burgerguy.recordable.shared.menu.LabelerMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
import org.lwjgl.system.MemoryStack;

public class LabelerScreen extends AbstractContainerScreen<LabelerMenu> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(Recordable.MOD_ID, "labeler");
    private static final ResourceLocation COLOR_ICON_LOCATION = new ResourceLocation("textures/gui/container/loom.png");

    private ClientPainter clientPainter;

    private EditBox authorEditBox;
    private EditBox titleEditBox;
    private Button undoButton;
    private Button resetButton;

    public LabelerScreen(LabelerMenu labelerMenu, Inventory inventory, Component component) {
        super(labelerMenu, inventory, component);
    }

    @Override
    public void containerTick() {
        this.authorEditBox.tick();
        this.titleEditBox.tick();
        boolean canUndo = this.clientPainter.canUndo();
        this.undoButton.active = canUndo;
        this.resetButton.active = canUndo;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        // gets ScreenRenderUtil ready for render
        this.addRenderableOnly((poseStack, mouseX, mouseY, partialTick) -> ScreenRenderUtil.startFills());

        // create and add paint colors
        PaintColorWidget[] paintColorWidgets = new PaintColorWidget[LabelerConstants.COLOR_COUNT];
        for (int i = 0; i < LabelerConstants.COLOR_COUNT; i++) {
            paintColorWidgets[i] = new PaintColorWidget(
                    LabelerConstants.DEFINED_COLORS[i],
                    this.menu.getLabelerBlockEntity().getColorLevels()[i],
                    LabelerConstants.COLOR_MAX_CAPACITY,
                    LabelerConstants.COLOR_LEVEL_PER_ITEM
            );
        }
        for (int i = 0; i < paintColorWidgets.length; i++) {
            int xIdx = i / LabelerConstants.PALETTE_COLUMNS_WRAP;
            int yIdx = i % LabelerConstants.PALETTE_COLUMNS_WRAP;
            PaintColorWidget paintColorWidget = paintColorWidgets[i];
            paintColorWidget.setBounds(
                this.leftPos + LabelerConstants.PALETTE_X + xIdx * (LabelerConstants.COLOR_WIDTH + LabelerConstants.COLOR_MARGIN_X),
                this.topPos + LabelerConstants.PALETTE_Y + yIdx * (LabelerConstants.COLOR_HEIGHT + LabelerConstants.COLOR_MARGIN_Y),
                LabelerConstants.COLOR_WIDTH,
                LabelerConstants.COLOR_HEIGHT
            );
            this.addRenderableWidget(paintColorWidget);
        }

        this.clientPainter = new ClientPainter(
                menu.getLabelerBlockEntity().getPixelIndexModel(),
                menu.getLabelerBlockEntity().getPixelModelWidth(),
                paintColorWidgets
        );

        // TODO: reactivate undo button when paint applied
        PaintWidget paintWidget = new PaintWidget(200, 40, 16, this.clientPainter);
        this.addRenderableWidget(paintWidget);

        Font font = Minecraft.getInstance().font;

        this.authorEditBox = new EditBox(font, 200, 200, 80, 20, new TranslatableComponent("screen.recordable.labeler.author"));
        this.addRenderableWidget(this.authorEditBox);

        this.titleEditBox = new EditBox(font, 200, 225, 80, 20, new TranslatableComponent("screen.recordable.labeler.title"));
        this.addRenderableWidget(this.titleEditBox);

        this.undoButton = new Button(300, 20, 16, 16, new TranslatableComponent("screen.recordable.labeler.undo"), b -> this.clientPainter.undo());
        this.addRenderableWidget(this.undoButton);

        Button eraseButton = new Button(300, 40, 16, 16, new TranslatableComponent("screen.recordable.labeler.eraser"), b -> this.clientPainter.toggleErase());
        this.addRenderableWidget(eraseButton);

        Button mixButton = new Button(300, 60, 16, 16, new TranslatableComponent("screen.recordable.labeler.mix"), b -> this.clientPainter.toggleMix());
        this.addRenderableWidget(mixButton);

        this.resetButton = new Button(300, 80, 16, 16, new TranslatableComponent("screen.recordable.labeler.reset"), b -> this.clientPainter.reset());
        this.addRenderableWidget(this.resetButton);

        // gray this out when no edits have been made
        Button finishButton = new Button(300, 100, 16, 16, new TranslatableComponent("screen.recordable.labeler.finish"), b -> this.doFinish());
        this.addRenderableWidget(finishButton);

        // TODO: deselect paint color widget when using eraser
        // TODO: stop current paint when run out of color
        // TODO: make paint widgets not sound when clicked and not selected

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

    //    @Override
//    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//        if (keyCode == 256) {
//            this.minecraft.player.closeContainer();
//        }
//        if (this.name.keyPressed(keyCode, scanCode, modifiers) || this.name.canConsumeInput()) {
//            return true;
//        }
//        return super.keyPressed(keyCode, scanCode, modifiers);
//    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTick, int mouseX, int mouseY) {
        // TODO: placeholder background, make real background
        fill(matrixStack, 0, 0, this.width, this.height, 0xFFCCCCCC);
    }

    public void doFinish() {
        if (this.authorEditBox == null || this.titleEditBox == null) throw new IllegalStateException("Screen not initialized");
        this.clientPainter.clear();
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            String author = this.authorEditBox.getValue();
            String title = this.titleEditBox.getValue();
            // kind of an inefficient way of figuring it out, but whatever
            int sizeBytes = Integer.BYTES + author.getBytes(StandardCharsets.UTF_8).length
                    + Integer.BYTES + title.getBytes(StandardCharsets.UTF_8).length
                    + this.clientPainter.getSizeBytes();
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(memoryStack.malloc(sizeBytes)));
            buffer.resetWriterIndex();
            buffer.writeUtf(author);
            buffer.writeUtf(title);
            this.clientPainter.writeToPacket(buffer);
            ClientPlayNetworking.send(Recordable.FINALIZE_LABEL_ID, buffer);
        }
    }

}

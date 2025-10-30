package net.fw14.createAddons.accessDenied.screen;
//

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.trains.schedule.DestinationSuggestions;
import net.createmod.catnip.data.IntAttached;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlayerEditBox extends EditBox {
    private final DestinationSuggestions destinationSuggestions;
    private final Consumer<String> mainResponder;
    private String prevValue;

    public PlayerEditBox(Screen screen, Font pFont, int pX, int pY, int pWidth, int pHeight, boolean anchorToBottom) {
        super(pFont, pX, pY, pWidth, pHeight, Component.empty());
        this.prevValue = "=)";


        var intAttachedList = new ArrayList<IntAttached<String>>();
        int idx = 0;
        var server = Minecraft.getInstance().isSingleplayer() ? List.<GameProfile>of() : Minecraft.getInstance().getConnection().getOnlinePlayers()
                .stream().map(PlayerInfo::getProfile).toList();
        for (GameProfile player : server)
            intAttachedList.add(IntAttached.with(idx++, player.getName()));

        this.destinationSuggestions = new DestinationSuggestions(Minecraft.getInstance(),
                screen, this, pFont, intAttachedList, false, -72 + this.getY() + (anchorToBottom ? 0 : this.getHeight()));

        this.destinationSuggestions.setAllowSuggestions(true);
        this.destinationSuggestions.updateCommandInfo();
        this.mainResponder = (t) -> {
            if (!t.equals(this.prevValue)) {
                this.destinationSuggestions.updateCommandInfo();
            }

            this.prevValue = t;
        };
        this.setResponder(this.mainResponder);
        this.setBordered(false);
        this.setFocused(false);
        this.mouseClicked((double)0.0F, (double)0.0F, 0);
        this.setMaxLength(25);
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.destinationSuggestions.keyPressed(pKeyCode, pScanCode, pModifiers)) {
            return true;
        } else if (this.isFocused() && pKeyCode == 257) {
            this.setFocused(false);
            this.moveCursorToEnd();
            this.mouseClicked((double)0.0F, (double)0.0F, 0);
            return true;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        return this.destinationSuggestions.mouseScrolled(Mth.clamp(pDelta, (double)-1.0F, (double)1.0F)) ? true : super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 1 && this.isMouseOver(pMouseX, pMouseY)) {
            this.setValue("");
            return true;
        } else {
            boolean wasFocused = this.isFocused();
            if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
                if (!wasFocused) {
                    this.setHighlightPos(0);
                    this.setCursorPosition(this.getValue().length());
                }

                return true;
            } else {
                return this.destinationSuggestions.mouseClicked((double)((int)pMouseX), (double)((int)pMouseY), pButton);
            }
        }
    }

    public void setValue(String text) {
        this.setHighlightPos(0);
        super.setValue(text);
    }

    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }

    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        PoseStack matrixStack = pGuiGraphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(0.0F, 0.0F, 500.0F);
        this.destinationSuggestions.render(pGuiGraphics, pMouseX, pMouseY);
        matrixStack.popPose();
        if (this.destinationSuggestions.isEmpty()) {
            this.destinationSuggestions.updateCommandInfo();
        }
    }

    public void setResponder(Consumer<String> pResponder) {
        super.setResponder(pResponder == this.mainResponder ? this.mainResponder : this.mainResponder.andThen(pResponder));
    }

    public void tick() {
        super.tick();
        if (!this.isFocused()) {
            this.destinationSuggestions.hide();
        }

        this.destinationSuggestions.tick();
    }

    public void hideSuggestions() {
        this.destinationSuggestions.hide();
    }
}

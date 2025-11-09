package net.fw14.createAddons.accessDenied.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.theme.Color;
import net.fw14.createAddons.accessDenied.AccessDenied;
import net.fw14.createAddons.accessDenied.screen.AccessControlScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.joml.Math;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(StockKeeperRequestScreen.class)
public class StockKeeperRequestScreenMixin extends Screen {
    @Shadow(remap = false) @Final private boolean isAdmin;
    @Shadow(remap = false) private boolean isLocked;
    @Shadow(remap = false) StockTickerBlockEntity blockEntity;
    @Shadow(remap = false) int lockX;
    @Shadow(remap = false) int lockY;
    @Shadow(remap = false) private boolean scrollHandleActive;
    @Shadow(remap = false) public LerpedFloat itemScroll;

    protected StockKeeperRequestScreenMixin(Component p_96550_) { super(p_96550_); }

    @Inject(at= @At(value = "RETURN"), method = "renderForeground", remap = false)
    public void renderFg(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!isAdmin || !isLocked) return;

        var cScroll = itemScroll.getValue(partialTicks);
        if (cScroll >= 0.5f)
            return;

        int posX = lockX + 17;

        float progress = Math.min(cScroll / 0.5f, 1f);

        graphics.pose().pushPose();
        graphics.pose().translate(0f - 4f * progress, 0f, 0f);

        RenderSystem.setShaderTexture(0, AccessDenied.resLoc("textures/gui/configure_button.png"));
        UIRenderHelper.drawColoredTexture(graphics, Color.WHITE.setAlpha(1f - progress), posX, lockY, 0, 0, 0, 16, 16, 16, 16);

        graphics.pose().popPose();

        if (mouseX > posX && mouseX <= posX + 16 && mouseY > lockY && mouseY <= lockY + 16) {
            graphics.renderComponentTooltip(Minecraft.getInstance().font,
                    List.of(Component.translatable("create_access_denied.configure_button")),
                    mouseX, mouseY);
        }
    }

    @Inject(at=@At("RETURN"), method = "mouseClicked", cancellable = true)
    public void click(double mouseX, double mouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
        if (!isAdmin || !isLocked) return;
        if (itemScroll.getValue() > 0.5f) return;
        int posX = lockX + 17;
        if (mouseX > posX && mouseX <= posX + 16 && mouseY > lockY && mouseY <= lockY + 16) {
            this.minecraft.setScreen(new AccessControlScreen(this.blockEntity.behaviour.freqId, this));
            this.scrollHandleActive = false;
            cir.cancel();
        }
    }
}

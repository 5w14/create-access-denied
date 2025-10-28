package net.fw14.createAddons.accessDenied.mixin;

import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.fw14.createAddons.accessDenied.AccessDenied;
import net.fw14.createAddons.accessDenied.screen.AccessControlScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    @Shadow @Final private boolean isAdmin;
    @Shadow private boolean isLocked;
    @Shadow StockTickerBlockEntity blockEntity;
    @Shadow int lockX;
    @Shadow int lockY;

    protected StockKeeperRequestScreenMixin(Component p_96550_) { super(p_96550_); }

    @Inject(at=@At("RETURN"), method = "renderForeground", remap = false)
    public void renderFg(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!isAdmin || !isLocked) return;

        int posX = lockX + 17;
        graphics.blit(ResourceLocation.fromNamespaceAndPath(AccessDenied.MODID, "textures/gui/configure_button.png"),
                posX, lockY, 0, 0, 16, 16, 16, 16);

        if (mouseX > posX && mouseX <= posX + 16 && mouseY > lockY && mouseY <= lockY + 16) {
            graphics.renderComponentTooltip(Minecraft.getInstance().font,
                    List.of(Component.translatable("create_access_denied.configure_button")),
                    mouseX, mouseY);
        }
    }

    @Inject(at=@At("RETURN"), method = "mouseClicked")
    public void click(double mouseX, double mouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
        if (!isAdmin || !isLocked) return;
        int posX = lockX + 17;
        if (mouseX > posX && mouseX <= posX + 16 && mouseY > lockY && mouseY <= lockY + 16) {
            this.minecraft.setScreen(new AccessControlScreen(this.blockEntity.behaviour.freqId, this));
        }
    }
}

package net.fw14.createAddons.accessDenied.mixin;

import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import net.fw14.createAddons.accessDenied.AccessDenied;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StockKeeperRequestScreen.class)
public class StockKeeperRequestScreenMixin extends Screen {
    protected StockKeeperRequestScreenMixin(Component p_96550_) { super(p_96550_); }

    @Inject(at=@At("RETURN"), method = "renderForeground", remap = false)
    public void renderFg(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        AccessDenied.renderScreenForeground(graphics, mouseX, mouseY,
                (StockKeeperRequestScreen) (Object) this);
    }

    @Inject(at=@At("RETURN"),method = "init", remap = false)
    public void init(CallbackInfo ci) {
        AccessDenied.initScreen((StockKeeperRequestScreen) (Object) this);
    }
}

package net.fw14.createAddons.accessDenied.mixin;

import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.fw14.createAddons.accessDenied.AccessDenied;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StockKeeperRequestScreen.class)
public class StockKeeperRequestScreenMixin extends Screen {
    @Shadow @Final private boolean isAdmin;

    @Shadow private boolean isLocked;
    @Shadow private StockTickerBlockEntity blockEntity;

//    @Unique private Button accessDenied$manageButton;

    @Shadow private int lockX;

    @Shadow private int lockY;

    protected StockKeeperRequestScreenMixin(Component p_96550_) { super(p_96550_); }

    @Inject(at=@At("RETURN"), method = "renderForeground", remap = false)
    public void renderFg(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        AccessDenied.renderScreenForeground(graphics, mouseX, mouseY, isAdmin,
                isLocked, lockX, lockY, blockEntity.behaviour.freqId, (StockKeeperRequestScreen) (Object) this);
    }

    @Inject(at=@At("RETURN"), method = "init", remap = false)
    public void init(CallbackInfo ci) {

//        accessDenied$manageButton.setPosition();
//        accessDenied$manageButton.setWidth(16);
//        accessDenied$manageButton.setHeight(16);
//        accessDenied$manageButton.visible = isLocked && isAdmin;

//        addRenderableWidget(accessDenied$manageButton);
    }

}

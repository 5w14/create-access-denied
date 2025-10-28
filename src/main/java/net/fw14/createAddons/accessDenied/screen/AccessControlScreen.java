package net.fw14.createAddons.accessDenied.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class AccessControlScreen extends Screen {
    private final UUID networkId;
    private final Screen parent;

    public AccessControlScreen(UUID networkId, Screen parent) {
        super(Component.translatable("create_access_denied.screen.access_control"));
        this.networkId = networkId;
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics p_281549_, int p_281550_, int p_282878_, float p_282465_) {
        super.render(p_281549_, p_281550_, p_282878_, p_282465_);
    }

    @Override
    public boolean mouseClicked(double p_94695_, double p_94696_, int p_94697_) {
        return super.mouseClicked(p_94695_, p_94696_, p_94697_);
    }
}

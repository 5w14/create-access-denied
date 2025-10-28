package net.fw14.createAddons.accessDenied;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AccessDenied.MODID)
@Mod.EventBusSubscriber
public class AccessDenied {
    public static final String MODID = "create_access_denied";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AccessDenied() {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    public static void renderScreenForeground(GuiGraphics graphics, int mouseX, int mouseY, StockKeeperRequestScreen screen) {
        boolean isAdmin = getfie(screen, "isAdmin");
        if (!isAdmin) return;
        boolean isLocked = getfie(screen, "isLocked");
        graphics.drawString(Minecraft.getInstance().font, Component.literal("locked:"+isLocked), screen.width / 2 + 110, screen.searchBox.getY(), 0xFFFFFF);
    }

    public static boolean getfie(StockKeeperRequestScreen screen, String name) {
        try {
            var cls = screen.getClass();
            var field = cls.getDeclaredField(name);
            field.setAccessible(true);
            return field.getBoolean(screen);
        } catch (Exception e) {
        }
        return false;
    }

    public static void initScreen(StockKeeperRequestScreen screen) {
    }
}

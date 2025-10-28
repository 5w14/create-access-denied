package net.fw14.createAddons.accessDenied;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import net.fw14.createAddons.accessDenied.networking.S2CAllowedPlayersSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AccessDenied.MODID)
@Mod.EventBusSubscriber
public class AccessDenied {
    public static String PROTOCOL_VERSION = "1";
    public static final String MODID = "create_access_denied";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Map<UUID, Set<UUID>> AllowedPlayersClientState = new HashMap<>();

    public static final SimpleChannel NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MODID, "networking"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public AccessDenied() {
        int msgType = 0;
        NETWORK_CHANNEL.registerMessage(msgType++, S2CAllowedPlayersSyncPacket.class,
                S2CAllowedPlayersSyncPacket::write, S2CAllowedPlayersSyncPacket::read, S2CAllowedPlayersSyncPacket::handle);
    }

    public static void renderScreenForeground(GuiGraphics graphics, int mouseX, int mouseY, boolean canAdmin, boolean isLocked,
                                              UUID networkId, StockKeeperRequestScreen screen) {
        graphics.drawString(Minecraft.getInstance().font, Component.literal("locked:"+isLocked + " net:" + networkId), screen.width / 2 + 110, screen.searchBox.getY(), 0xFFFFFF);

        var network = AccessDenied.AllowedPlayersClientState.getOrDefault(networkId, Set.of());
        int startPos = screen.searchBox.getY() + 15;
        for (UUID player : network) {
            graphics.drawString(Minecraft.getInstance().font, Component.literal(player.toString()),
                    screen.width / 2 + 110, startPos+=10, 0xFFFFFF);
        }
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

package net.fw14.createAddons.accessDenied;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import net.fw14.createAddons.accessDenied.networking.C2SModifyAllowedPlayersPacket;
import net.fw14.createAddons.accessDenied.networking.S2CAllowedPlayersSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

import java.util.*;

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
        NETWORK_CHANNEL.registerMessage(msgType++, C2SModifyAllowedPlayersPacket.class,
                C2SModifyAllowedPlayersPacket::write, C2SModifyAllowedPlayersPacket::read, C2SModifyAllowedPlayersPacket::handle);
    }

    public static void renderScreenForeground(GuiGraphics graphics, int mouseX, int mouseY,
                                              boolean isAdmin, boolean isLocked, int lockX, int lockY,
                                              UUID networkId, StockKeeperRequestScreen screen) {

        if (!isAdmin || !isLocked) return;

        int posX = lockX + 17;
        graphics.blit(ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/configure_button.png"),
                posX, lockY, 0, 0, 16, 16, 16, 16);

        if (mouseX > posX && mouseX <= posX + 16 && mouseY > lockY && mouseY <= lockY + 16) {
            graphics.renderComponentTooltip(Minecraft.getInstance().font,
                    List.of(
                            Component.translatable("create_access_denied.configure_button")),
                    mouseX, mouseY);
        }
    }

    public static void initScreen(StockKeeperRequestScreen screen) {
    }
}

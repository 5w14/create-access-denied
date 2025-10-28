package net.fw14.createAddons.accessDenied.screen;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import net.fw14.createAddons.accessDenied.AccessDenied;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AccessControlScreen extends Screen {
    private final UUID networkId;
    private final Screen parent;

    private static final ResourceLocation UI_RL = ResourceLocation.fromNamespaceAndPath(AccessDenied.MODID, "textures/gui/ui_background.png");
    private static final int UI_WIDTH = 192;
    private static final int UI_HEIGHT = 119;

    private static final int ADD_BUTTON_START_X = 170;
    private static final int ADD_BUTTON_START_Y = 98;
    private static final int ADD_BUTTON_SIZE = 18;
    public static final int WRAP_COUNT = 9;

    static Font font;

    private static final Map<UUID, ProfileCache> profileCache = new ConcurrentHashMap<>();

    public record ProfileCache(GameProfile profile, MinecraftProfileTexture profileTexture, ResourceLocation skinLocation) {
        public static ProfileCache get(UUID uuid, Minecraft minecraft) {
            if (profileCache.containsKey(uuid))
                return profileCache.get(uuid);

            var profile = new GameProfile(uuid, null);
            profile = minecraft.getMinecraftSessionService().fillProfileProperties(profile, false);
            var tex = minecraft.getMinecraftSessionService().getTextures(profile, false).get(MinecraftProfileTexture.Type.SKIN);
            var resloc = minecraft.getSkinManager().registerTexture(tex, MinecraftProfileTexture.Type.SKIN);
            var toCache = new ProfileCache(profile, tex, resloc);
            profileCache.put(uuid, toCache);
            return toCache;
        }

        public static void preCache(UUID playerUUID, Minecraft minecraft) {
            CompletableFuture.runAsync(() -> {
                var profile = new GameProfile(playerUUID, null);
                profile = minecraft.getMinecraftSessionService().fillProfileProperties(profile, false);
                var tex = minecraft.getMinecraftSessionService().getTextures(profile, false).get(MinecraftProfileTexture.Type.SKIN);
                var resloc = minecraft.getSkinManager().registerTexture(tex, MinecraftProfileTexture.Type.SKIN);
                var toCache = new ProfileCache(profile, tex, resloc);
                profileCache.put(playerUUID, toCache);
            });
        }

        public void renderSkin(GuiGraphics guiGraphics, int x, int y, int k) {
            guiGraphics.blit(this.skinLocation, x, y, k, k, 8, 8, 8, 8, 64, 64);
            guiGraphics.blit(this.skinLocation, x-1, y-1, k+2, k+2, 40, 8, 8, 8, 64, 64);
        }
    }

    public AccessControlScreen(UUID networkId, Screen parent) {
        super(Component.translatable("create_access_denied.screen.access_control"));
        this.networkId = networkId;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int uiStartX = this.width / 2 - UI_WIDTH / 2 ;
        int uiStartY = this.height / 2 - UI_HEIGHT / 2;

        font = new NoShadowFontWrapper(minecraft.font);

        var box = addRenderableWidget(
                new EditBox(font, uiStartX + 10, uiStartY + 103,
                157, 16, Component.literal("test")));

        box.setBordered(false);
        box.setMaxLength(16);
        box.setTextColor(0xFFFFFF);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float f) {
        this.renderBackground(graphics);

        int uiStartX = this.width / 2 - UI_WIDTH / 2 ;
        int uiStartY = this.height / 2 - UI_HEIGHT / 2;

        graphics.blit(UI_RL, uiStartX, uiStartY, 0, 0, UI_WIDTH, UI_HEIGHT);
        graphics.blit(UI_RL, uiStartX - 16, uiStartY - 16, 208, 0, 48, 48);

        graphics.drawCenteredString(font, Component.translatable("create_access_denied.screen.access_control"), this.width / 2, uiStartY + 4, 0x3d473b);

        if (isHoveringButton(mouseX, mouseY))
            graphics.blit(UI_RL, uiStartX + 170, uiStartY + 98, 208, 48, 18, 18);


        graphics.drawString(font, Component.translatable("create_access_denied.screen.players"), uiStartX + 12, uiStartY + 22, 0xedcdbd);

        var playerRenderX = uiStartX - 6;
        var playerRenderY = uiStartY + 36;
        var networkAllowedPlayers = AccessDenied.AllowedPlayersClientState.get(networkId);
        int playerIdx = 0;
        for (int i = 0; i < 3; i++)
        for (var player : networkAllowedPlayers) {
            var profile = ProfileCache.get(player, minecraft);

            var headSize = 13;

            var x = playerRenderX += headSize + 6;

            boolean isHovering = mouseX > playerRenderX - 2 && mouseX <= playerRenderX + headSize + 3
                    && mouseY > playerRenderY - 2 && mouseY < playerRenderY + headSize + 3;

            if (isHovering) {
                graphics.fill(x - 3, playerRenderY - 3, x + headSize + 3,
                        playerRenderY + headSize + 3, 0x22000000);
                profile.renderSkin(graphics, x, playerRenderY, headSize);
                graphics.renderComponentTooltip(minecraft.font,
                        List.of(
                                Component.literal(profile.profile.getName()),
                                Component.translatable("create_access_denied.screen.remove_hint")
                                        .withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            } else {
                profile.renderSkin(graphics, x, playerRenderY, headSize);
            }

            playerIdx++;
            if (playerIdx % WRAP_COUNT == 0) {
                playerRenderY += headSize + 4;
                playerRenderX = uiStartX - 6;
            }
        }

        super.render(graphics, mouseX, mouseY, f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isHoveringButton(mouseX, mouseY)) {
            Minecraft.getInstance().getSoundManager().
                    play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.get(), 1.0f, 0.8f * 0.25F));
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    boolean isHoveringButton(double mouseX, double mouseY) {
        int uiStartX = this.width / 2 - UI_WIDTH / 2 ;
        int uiStartY = this.height / 2 - UI_HEIGHT / 2;
        return mouseX > uiStartX + ADD_BUTTON_START_X && mouseX <= uiStartX + ADD_BUTTON_START_X + ADD_BUTTON_SIZE &&
                mouseY > uiStartY + ADD_BUTTON_START_Y && mouseY <= uiStartY + ADD_BUTTON_START_Y + ADD_BUTTON_SIZE;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(this.parent);
    }
}

package net.fw14.createAddons.accessDenied.screen;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import net.fw14.createAddons.accessDenied.AccessDenied;
import net.fw14.createAddons.accessDenied.networking.C2SModifyAllowedPlayersPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AccessControlScreen extends Screen {
    private final UUID networkId;
    private final Screen parent;

    private static final ResourceLocation UI_RL = AccessDenied.resLoc("textures/gui/ui_background.png");
    private static final int UI_WIDTH = 192;
    private static final int UI_HEIGHT = 119;

    private static final int ADD_BUTTON_START_X = 170;
    private static final int ADD_BUTTON_START_Y = 98;
    private static final int ADD_BUTTON_SIZE = 18;

    public static final int HEAD_SIZE = 13;
    public static final int WRAP_COUNT = 9;

    private PlayerEditBox playerUsernameBox;
    static Font font;

    private static final Map<UUID, ProfileCache> profileCache = new ConcurrentHashMap<>();

    public record ProfileCache(GameProfile profile, MinecraftProfileTexture profileTexture, ResourceLocation skinLocation) {
        public static ProfileCache get(UUID uuid, Minecraft minecraft) {
            if (profileCache.containsKey(uuid))
                return profileCache.get(uuid);

            var profile = new GameProfile(uuid, null);
            profile = minecraft.getMinecraftSessionService().fetchProfile(profile.getId(), false).profile();
            var skin = minecraft.getSkinManager().getOrLoad(profile).join();
            var toCache = new ProfileCache(profile, null, skin.texture());
            profileCache.put(uuid, toCache);
            return toCache;
        }

        public static void preCache(UUID playerUUID, Minecraft minecraft) {
            CompletableFuture.runAsync(() -> {
                get(playerUUID, minecraft);
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
        GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);

        int uiStartX = this.width / 2 - UI_WIDTH / 2 ;
        int uiStartY = this.height / 2 - UI_HEIGHT / 2;

        font = new NoShadowFontWrapper(minecraft.font);

        var box = addRenderableWidget( new PlayerEditBox(this, font, uiStartX + 10, uiStartY + 103, 157, 16, false) );
        box.setBordered(false);
        box.setMaxLength(16);
        box.setTextColor(0xFFFFFF);
        this.playerUsernameBox = box;
    }

    @Override
    public void tick() {
        if (this.playerUsernameBox != null) {
            if (this.playerUsernameBox.isFocused()) this.playerUsernameBox.tick();
            else this.playerUsernameBox.hideSuggestions();
        }

        super.tick();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float f) {
        this.renderBackground(graphics, mouseX, mouseY, f);

        int uiStartX = this.width / 2 - UI_WIDTH / 2 ;
        int uiStartY = this.height / 2 - UI_HEIGHT / 2;

        graphics.blit(UI_RL, uiStartX, uiStartY, 0, 0, UI_WIDTH, UI_HEIGHT);
        graphics.blit(UI_RL, uiStartX - 16, uiStartY - 16, 208, 0, 48, 48);

        graphics.drawCenteredString(font, Component.translatable("create_access_denied.screen.access_control"), this.width / 2, uiStartY + 4, 0x3d473b);

        if (atLimit()) {
            graphics.blit(UI_RL, uiStartX + 170, uiStartY + 98, 208, 80, 18, 18);

            if (isHoveringButton(mouseX, mouseY)) {
                graphics.renderComponentTooltip(minecraft.font,
                        List.of(Component.translatable("create_access_denied.screen.at_limit", AccessDenied.PLAYER_LIMIT)), mouseX, mouseY);
            }
        } else if (isHoveringButton(mouseX, mouseY)) {
            graphics.blit(UI_RL, uiStartX + 170, uiStartY + 98, 208, 48, 18, 18);
        }


        graphics.drawString(font, Component.translatable("create_access_denied.screen.players"), uiStartX + 12, uiStartY + 23, 0xedcdbd);

        var playerRenderX = uiStartX - 6;
        var playerRenderY = uiStartY + 36;
        var networkAllowedPlayers = AccessDenied.AllowedPlayersClientState.get(networkId);
        int playerIdx = 0;
        for (var player : networkAllowedPlayers) {
            var profile = ProfileCache.get(player, minecraft);

            var x = playerRenderX += HEAD_SIZE + 6;

            boolean isHovering = mouseX > playerRenderX - 2 && mouseX <= playerRenderX + HEAD_SIZE + 3
                    && mouseY > playerRenderY - 2 && mouseY < playerRenderY + HEAD_SIZE + 3;

            if (isHovering) {
                graphics.fill(x - 3, playerRenderY - 3, x + HEAD_SIZE + 3,
                        playerRenderY + HEAD_SIZE + 3, 0x22000000);
                profile.renderSkin(graphics, x, playerRenderY, HEAD_SIZE);
                graphics.renderComponentTooltip(minecraft.font,
                        List.of(
                                Component.literal(profile.profile.getName()),
                                Component.translatable("create_access_denied.screen.remove_hint")
                                        .withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            } else {
                profile.renderSkin(graphics, x, playerRenderY, HEAD_SIZE);
            }

            playerIdx++;
            if (playerIdx % WRAP_COUNT == 0) {
                playerRenderY += HEAD_SIZE + 4;
                playerRenderX = uiStartX - 6;
            }
        }

        if (networkAllowedPlayers.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("create_access_denied.screen.no_players").withStyle(ChatFormatting.ITALIC), this.width / 2, this.height / 2 - 4, 0x88edcdbd);
        }

        // Back button
        if (isHovering(mouseX, mouseY, uiStartX - 7, uiStartY + UI_HEIGHT + 4, 55, 15)) {
            graphics.blit(UI_RL, uiStartX - 7, uiStartY + UI_HEIGHT + 4, 0, 160, 55, 15);
            graphics.drawCenteredString(font, Component.translatable("gui.done"), uiStartX + 22, uiStartY + UI_HEIGHT + 8, 0x3d473b);
        } else {
            graphics.blit(UI_RL, uiStartX - 7, uiStartY + UI_HEIGHT + 4, 0, 144, 55, 15);
            graphics.drawCenteredString(font, Component.translatable("gui.done"), uiStartX + 22, uiStartY + UI_HEIGHT + 8, 0x323232);
        }

        playerUsernameBox.active = !atLimit();
        super.render(graphics, mouseX, mouseY, f);
    }

    boolean isHovering(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (super.mouseClicked(mouseX, mouseY, mouseButton))
            return true;

        int uiStartX = this.width / 2 - UI_WIDTH / 2 ;
        int uiStartY = this.height / 2 - UI_HEIGHT / 2;

        if (isHovering(mouseX, mouseY, uiStartX - 7, uiStartY + UI_HEIGHT + 4, 55, 15) && mouseButton == 0) {
            minecraft.setScreen(parent);
            Minecraft.getInstance().getSoundManager().
                    play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 0.8f * 0.25F));
            return true;
        }

        if (isHoveringButton(mouseX, mouseY) && mouseButton == 0) {
            submitToAdd();
            Minecraft.getInstance().getSoundManager().
                    play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 0.8f * 0.25F));
            return true;
        }

        if (!hasShiftDown())
            return super.mouseClicked(mouseX, mouseY, mouseButton);

        var playerRenderX = uiStartX - 6;
        var playerRenderY = uiStartY + 36;
        var networkAllowedPlayers = AccessDenied.AllowedPlayersClientState.get(networkId);
        int playerIdx = 0;
        for (var player : networkAllowedPlayers) {
            var profile = ProfileCache.get(player, minecraft);

            playerRenderX += HEAD_SIZE + 6;
            boolean isHovering = mouseX > playerRenderX - 2 && mouseX <= playerRenderX + HEAD_SIZE + 3
                    && mouseY > playerRenderY - 2 && mouseY < playerRenderY + HEAD_SIZE + 3;

            if (isHovering) {
                submitToRemove(profile);
                Minecraft.getInstance().getSoundManager().
                        play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 0.8f * 0.25F));
                return true;
            }

            playerIdx++;
            if (playerIdx % WRAP_COUNT == 0) {
                playerRenderY += HEAD_SIZE + 4;
                playerRenderX = uiStartX - 6;
            }
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int p_96553_, int p_96554_) {
        if (keyCode == GLFW.GLFW_KEY_ENTER
                && this.playerUsernameBox.isFocused()) {
            this.submitToAdd();
            return true;
        }

        return super.keyPressed(keyCode, p_96553_, p_96554_);
    }

    boolean isHoveringButton(double mouseX, double mouseY) {
        int uiStartX = this.width / 2 - UI_WIDTH / 2 ;
        int uiStartY = this.height / 2 - UI_HEIGHT / 2;
        return isHovering(mouseX, mouseY, uiStartX + ADD_BUTTON_START_X, uiStartY + ADD_BUTTON_START_Y,
                ADD_BUTTON_SIZE, ADD_BUTTON_SIZE);
    }

    boolean atLimit() {
        var networkAllowedPlayers = AccessDenied.AllowedPlayersClientState.get(networkId);
        return networkAllowedPlayers.size() >= AccessDenied.PLAYER_LIMIT;
    }

    private void submitToAdd() {
        if (atLimit())
            return;

        var value = this.playerUsernameBox.getValue();
        if (value.length() < 3)
            return;

        this.playerUsernameBox.setValue("");

        // Skip UUID lookup for known players
        var cachedProfile = profileCache.values().stream().filter(v ->
                v.profile.getName().equalsIgnoreCase(value)).findFirst();
        if (cachedProfile.isPresent()) {
            PacketDistributor.sendToServer(C2SModifyAllowedPlayersPacket.add(this.networkId, cachedProfile.get().profile().getId()));
            return;
        }

        CompletableFuture.runAsync(() -> {
            AccessDenied.fetchProfileByUsername(value).whenComplete((profile, exception) -> {
                if (exception != null || profile == null)
                    return;

                ProfileCache.preCache(profile.getId(), minecraft);
                PacketDistributor.sendToServer(C2SModifyAllowedPlayersPacket.add(this.networkId, profile.getId()));
            });
        });
    }

    private void submitToRemove(ProfileCache cached) {
        PacketDistributor.sendToServer(C2SModifyAllowedPlayersPacket.remove(this.networkId, cached.profile().getId()));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

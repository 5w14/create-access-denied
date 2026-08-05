package net.fw14.createAddons.accessDenied.screen;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import net.fw14.createAddons.accessDenied.AccessDenied;
import net.fw14.createAddons.accessDenied.client.AccessDeniedClient;
import net.fw14.createAddons.accessDenied.config.Config;
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
import net.minecraft.util.Mth;
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
    public static final int ROW_HEIGHT = HEAD_SIZE + 4;
    public static final int VISIBLE_ROWS = 3;
    private static final int GRID_TOP_OFFSET = 36;
    private double scrollOffset;

    private PlayerEditBox playerUsernameBox;
    static Font font;

    private static final Map<UUID, ProfileCache> profileCache = new ConcurrentHashMap<>();

    public record ProfileCache(GameProfile profile, ResourceLocation skinLocation) {
        public static ProfileCache get(UUID uuid, Minecraft minecraft) {
            if (profileCache.containsKey(uuid))
                return profileCache.get(uuid);

            var profileFetched = minecraft.getMinecraftSessionService().fetchProfile(uuid, false);
            if (profileFetched == null)
                return new ProfileCache(new GameProfile(uuid, uuid.toString()),
                        minecraft.getSkinManager().getInsecureSkin(new GameProfile(uuid, uuid.toString())).texture());

            return addFetched(profileFetched.profile(), minecraft);
        }

        public static void preCache(UUID playerUUID, Minecraft minecraft) {
            CompletableFuture.runAsync(() -> {
                get(playerUUID, minecraft);
            });
        }

        public static ProfileCache addFetched(GameProfile profile, Minecraft minecraft) {
            var skin = minecraft.getSkinManager().getInsecureSkin(profile);
            var toCache = new ProfileCache(profile, skin.texture());
            profileCache.put(profile.getId(), toCache);

            minecraft.getSkinManager().getOrLoad(profile).whenComplete((fetchedSkin, throwable) -> {
                profileCache.put(profile.getId(), new ProfileCache(profile, fetchedSkin.texture()));
            });

            return toCache;
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
        box.setFocused(true);
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
        super.renderBackground(graphics, mouseX, mouseY, f);

        int uiStartX = this.width / 2 - UI_WIDTH / 2 ;
        int uiStartY = this.height / 2 - UI_HEIGHT / 2;

        graphics.blit(UI_RL, uiStartX, uiStartY, 0, 0, UI_WIDTH, UI_HEIGHT);
        graphics.blit(UI_RL, uiStartX - 16, uiStartY - 16, 208, 0, 48, 48);

        graphics.drawCenteredString(font, Component.translatable("create_access_denied.screen.access_control"), this.width / 2, uiStartY + 4, 0x3d473b);

        if (atLimit()) {
            graphics.blit(UI_RL, uiStartX + 170, uiStartY + 98, 208, 80, 18, 18);

            if (isHoveringButton(mouseX, mouseY)) {
                graphics.renderComponentTooltip(minecraft.font,
                        List.of(Component.translatable("create_access_denied.screen.at_limit", Config.PLAYER_LIMIT.get())), mouseX, mouseY);
            }
        } else if (isHoveringButton(mouseX, mouseY)) {
            graphics.blit(UI_RL, uiStartX + 170, uiStartY + 98, 208, 48, 18, 18);
        }


        graphics.drawString(font, Component.translatable("create_access_denied.screen.players"), uiStartX + 12, uiStartY + 23, 0xedcdbd);

        var networkAllowedPlayers = AccessDenied.AllowedPlayersClientState.get(networkId);

        int viewportY1 = uiStartY + GRID_TOP_OFFSET;
        int viewportHeight = VISIBLE_ROWS * ROW_HEIGHT;
        int viewportY2 = viewportY1 + viewportHeight;
        int maxScroll = maxScroll(networkAllowedPlayers.size());
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);
        var playerRenderX = uiStartX - 6;
        var playerRenderY = viewportY1 - (int) Math.round(scrollOffset);
        int playerIdx = 0;
        ProfileCache hoveredProfile = null;


        graphics.enableScissor(0, viewportY1, this.width, viewportY2);
        for (var player : networkAllowedPlayers) {
            var profile = ProfileCache.get(player, minecraft);

            var x = playerRenderX += HEAD_SIZE + 6;

            if (playerRenderY + HEAD_SIZE >= viewportY1 && playerRenderY <= viewportY2) {
                boolean isHovering = mouseX > playerRenderX - 2 && mouseX <= playerRenderX + HEAD_SIZE + 3
                        && mouseY > playerRenderY - 2 && mouseY < playerRenderY + HEAD_SIZE + 3
                        && mouseY >= viewportY1 && mouseY < viewportY2;
                if (isHovering) {
                    graphics.fill(x - 3, playerRenderY - 3, x + HEAD_SIZE + 3, playerRenderY + HEAD_SIZE + 3, 0x22000000);
                    hoveredProfile = profile;
                }
                profile.renderSkin(graphics, x, playerRenderY, HEAD_SIZE);
            }

            playerIdx++;
            if (playerIdx % WRAP_COUNT == 0) {
                playerRenderY += ROW_HEIGHT;
                playerRenderX = uiStartX - 6;
            }
        }
        graphics.disableScissor();

        if (maxScroll > 0) {
            int barX = uiStartX + UI_WIDTH - 5;
            int thumbHeight = Math.max(6, viewportHeight * viewportHeight / (viewportHeight + maxScroll));
            int thumbY = viewportY1 + (int) ((viewportHeight - thumbHeight) * (scrollOffset / maxScroll));
            graphics.fill(barX, viewportY1, barX + 1, viewportY2, 0x33000000);
            graphics.fill(barX, thumbY, barX + 1, thumbY + thumbHeight, 0x99edcdbd);
        }

        if (hoveredProfile != null) {
            graphics.renderComponentTooltip(
                minecraft.font,
                List.of(
                    Component.literal(hoveredProfile.profile.getName()),
                    Component.translatable("create_access_denied.screen.remove_hint").withStyle(ChatFormatting.GRAY)
                ), mouseX, mouseY
            );
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

        playerUsernameBox.render(graphics, mouseX, mouseY, f);
    }

    boolean isHovering(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h;
    }

    private int maxScroll(int playerCount) {
        int totalRows = (playerCount + WRAP_COUNT - 1) / WRAP_COUNT;
        int viewportHeight = VISIBLE_ROWS * ROW_HEIGHT;
        return Math.max(0, totalRows * ROW_HEIGHT - viewportHeight);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int uiStartX = this.width / 2 - UI_WIDTH / 2;
        int uiStartY = this.height / 2 - UI_HEIGHT / 2;
        int viewportY1 = uiStartY + GRID_TOP_OFFSET;
        int viewportY2 = viewportY1 + VISIBLE_ROWS * ROW_HEIGHT;

        if (mouseX >= uiStartX && mouseX < uiStartX + UI_WIDTH && mouseY >= viewportY1 && mouseY < viewportY2) {
            var networkAllowedPlayers = AccessDenied.AllowedPlayersClientState.get(networkId);
            scrollOffset = Mth.clamp(scrollOffset - scrollY * ROW_HEIGHT, 0, maxScroll(networkAllowedPlayers.size()));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
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

        var networkAllowedPlayers = AccessDenied.AllowedPlayersClientState.get(networkId);
        int viewportY1 = uiStartY + GRID_TOP_OFFSET;
        int viewportY2 = viewportY1 + VISIBLE_ROWS * ROW_HEIGHT;
        var playerRenderX = uiStartX - 6;
        var playerRenderY = viewportY1 - (int) Math.round(scrollOffset);
        int playerIdx = 0;
        for (var player : networkAllowedPlayers) {
            var profile = ProfileCache.get(player, minecraft);

            playerRenderX += HEAD_SIZE + 6;
            boolean isHovering = playerRenderY + HEAD_SIZE >= viewportY1 && playerRenderY <= viewportY2
                    && mouseX > playerRenderX - 2 && mouseX <= playerRenderX + HEAD_SIZE + 3
                    && mouseY > playerRenderY - 2 && mouseY < playerRenderY + HEAD_SIZE + 3
                    && mouseY >= viewportY1 && mouseY < viewportY2;

            if (isHovering) {
                submitToRemove(profile);
                Minecraft.getInstance().getSoundManager().
                        play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 0.8f * 0.25F));
                return true;
            }

            playerIdx++;
            if (playerIdx % WRAP_COUNT == 0) {
                playerRenderY += ROW_HEIGHT;
                playerRenderX = uiStartX - 6;
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
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
        return networkAllowedPlayers.size() >= Config.PLAYER_LIMIT.get();
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
            AccessDeniedClient.fetchProfileByUsername(value).whenComplete((profile, exception) -> {
                if (exception != null || profile == null)
                    return;

                ProfileCache.preCache(profile.getId(), this.minecraft);
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

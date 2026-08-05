package net.fw14.createAddons.accessDenied.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.IntValue PLAYER_LIMIT = BUILDER
            .comment("Player limit of access list.")
            .defineInRange("playerLimit", 18, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec SPEC = BUILDER.build();
    
    private Config() {}
}

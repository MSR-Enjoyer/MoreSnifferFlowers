package net.abraxator.moresnifferflowers.init;


import net.minecraftforge.common.ForgeConfigSpec;

;

public class ModServerConfig {
    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final ForgeConfigSpec.DoubleValue CORRUPTION_SPREAD_SPEED;
    public static final ForgeConfigSpec.BooleanValue CORRUPTED_TREE_GROW_THROUGH;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("corruption");

        CORRUPTION_SPREAD_SPEED = builder
                .comment("Spread speed of corrupted grass blocks, 1 = Default, 0 = Disabled")
                .translation("moresnifferflowers.configuration.corruption_spread_speed")
                .defineInRange("Corruption Spread Speed", 1D, 0D, 5D);

        CORRUPTED_TREE_GROW_THROUGH = builder
                .comment("Should the corrupted tree be able to grow through and destroy blocks? Default = true")
                .translation("moresnifferflowers.configuration.corrupted_tree_grow_through")
                .define("Corrupted Tree Grow Trough", true);

        builder.pop();

        SERVER_CONFIG = builder.build();
    }
}

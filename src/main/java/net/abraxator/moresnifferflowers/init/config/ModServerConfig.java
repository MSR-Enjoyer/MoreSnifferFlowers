package net.abraxator.moresnifferflowers.init.config;


import net.abraxator.moresnifferflowers.MoreSnifferFlowers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

;

public class ModServerConfig {
    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final ForgeConfigSpec.DoubleValue CORRUPTION_SPREAD_SPEED;
    public static final ForgeConfigSpec.BooleanValue CORRUPTED_TREE_GROW_THROUGH;
    public static final ForgeConfigSpec.BooleanValue CORRUPTED_TREE_BONE_MEAL;
    public static final ForgeConfigSpec.BooleanValue CORRUPTED_BOBLING_GRIEFING;
    public static final ForgeConfigSpec.BooleanValue CORRUPTED_SLUDGE_GRIEFING;

    public static final ForgeConfigSpec.ConfigValue<String> REBREWING_LENGTH;
    public static final ForgeConfigSpec.ConfigValue<String> REBREWING_AMPLIFIER;
    public static final ForgeConfigSpec.ConfigValue<String> REBREWING_SPLASH;
    public static final ForgeConfigSpec.ConfigValue<String> REBREWING_LINGERING;



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

        CORRUPTED_TREE_BONE_MEAL = builder
                .comment("Should corrupted sapling require bone meal to grow at all? Default = false")
                .translation("moresnifferflowers.configuration.corrupted_tree_bone_meal")
                .define("Corrupted Tree Bone Meal", false);

        CORRUPTED_BOBLING_GRIEFING = builder
                .comment("Should boblings spawn projectiles when hit and replace blocks when planting? Default = true")
                .translation("moresnifferflowers.configuration.corrupted_bobling_griefing")
                .define("Corrupted Bobling Griefing", true);

        CORRUPTED_SLUDGE_GRIEFING = builder
                .comment("Should sludges shoot projectiles when blocks get destroyed? Default = true")
                .translation("moresnifferflowers.configuration.corrupted_sludge_griefing")
                .define("Corrupted Sludge Griefing", true);


        builder.pop();

        builder.push("rebrewing");

        builder.comment("Change items for rebrewing recipe, JEI needs rejoin");
        builder.translation("moresnifferflowers.configuration.rebrew_jei");

        REBREWING_LENGTH = builder
                .translation("moresnifferflowers.configuration.rebrew_length")
                .define("Rebrewing Length", itemToString(Items.REDSTONE));

        REBREWING_AMPLIFIER = builder
                .translation("moresnifferflowers.configuration.rebrew_amplifier")
                .define("Rebrewing Amplifier", itemToString(Items.GLOWSTONE_DUST));

        REBREWING_SPLASH = builder
                .translation("moresnifferflowers.configuration.rebrew_splash")
                .define("Rebrewing Splash", itemToString(Items.GUNPOWDER));

        REBREWING_LINGERING = builder
                .translation("moresnifferflowers.configuration.rebrew_lingering")
                .define("Rebrewing Lingering", itemToString(Items.DRAGON_BREATH));


        builder.pop();


        SERVER_CONFIG = builder.build();
    }

    private static @NotNull String itemToString(Item glowstoneDust) {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(glowstoneDust)).toString();
    }

    public static Item itemFromLoc(String loc) {
        return ForgeRegistries.ITEMS.getValue(MoreSnifferFlowers.ofLoc(loc));
    }
}

package me.desht.modularrouters.recipe;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.ModuleType;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.recipe.enhancement.*;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ModRecipes {
    public static void init() {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.itemRouter, 4),
                "ibi", "brb", "ibi",
                'b', Blocks.IRON_BARS, 'r', ModItems.blankModule, 'i', Items.IRON_INGOT);

        GameRegistry.addRecipe(new ItemStack(ModItems.blankModule, 6),
                " r ", "ppp", "nnn",
                'r', Items.REDSTONE, 'p', Items.PAPER, 'n', Items.GOLD_NUGGET);
        for (ModuleType type : ModuleType.values()) {
            IRecipe recipe = ItemModule.getModule(type).getRecipe();
            if (recipe != null) GameRegistry.addRecipe(recipe);
        }

        ItemStack lapis = new ItemStack(Items.DYE, 1, 4);
        GameRegistry.addRecipe(new ItemStack(ModItems.blankUpgrade, 6),
                "ppn", "pdn", " pn",
                'p', Items.PAPER, 'd', lapis, 'n', Items.GOLD_NUGGET);
        for (ItemUpgrade.UpgradeType type : ItemUpgrade.UpgradeType.values()) {
            GameRegistry.addRecipe(ItemUpgrade.getUpgrade(type).getRecipe());
        }
        GameRegistry.addRecipe(new ShapelessOreRecipe(
                ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.RANGE),
                ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.RANGEDOWN)));

        for (ItemSmartFilter.FilterType type : ItemSmartFilter.FilterType.values()) {
            GameRegistry.addRecipe(ItemSmartFilter.getFilter(type).getRecipe());
        }
        // special case for deprecated sorter & mod sorter modules
        GameRegistry.addShapelessRecipe(
                ItemSmartFilter.makeItemStack(ItemSmartFilter.FilterType.BULKITEM),
                ModuleHelper.makeItemStack(ModuleType.SORTER));
        GameRegistry.addShapelessRecipe(
                ItemSmartFilter.makeItemStack(ItemSmartFilter.FilterType.MOD),
                ModuleHelper.makeItemStack(ModuleType.MODSORTER));

        RecipeSorter.register(ModularRouters.modId + ":enchantModule", EnchantModuleRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
        for (ModuleType type : EnchantModuleRecipe.validEnchantments.keySet()) {
            for (Enchantment ench : EnchantModuleRecipe.validEnchantments.get(type)) {
                for (int level = ench.getMinLevel(); level <= ench.getMaxLevel(); level++) {
                    ItemStack resStack = ModuleHelper.makeItemStack(type);
                    ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                    resStack.addEnchantment(ench, level);
                    book.addEnchantment(ench, level);
                    GameRegistry.addRecipe(new EnchantModuleRecipe(resStack, ModuleHelper.makeItemStack(type), book));
                }
            }
        }

        addSelfCraftRecipes();
        addRedstoneUpgradeRecipes();
        addRegulatorUpgradeRecipes();
        addPickupDelayRecipes();
        addFastPickupRecipe();

        MinecraftForge.EVENT_BUS.register(ItemCraftedListener.class);
    }

    private static void addSelfCraftRecipes() {
        // crafting a module into itself resets all NBT on the module
        RecipeSorter.register(ModularRouters.modId + ":reset", ModuleResetRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        for (ModuleType type : ModuleType.values()) {
            if (type == ModuleType.SORTER || type == ModuleType.MODSORTER)
                continue;
            ItemStack stack = ModuleHelper.makeItemStack(type);
            ItemStack output = ModuleHelper.makeItemStack(type);
            GameRegistry.addRecipe(new ModuleResetRecipe(output,
                    "M",
                    'M', stack));
        }
    }

    private static void addFastPickupRecipe() {
        RecipeSorter.register(ModularRouters.modId + ":fastPickup", FastPickupEnhancementRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        GameRegistry.addRecipe(new FastPickupEnhancementRecipe(ModuleType.VACUUM));
    }

    private static void addRedstoneUpgradeRecipes() {
        RecipeSorter.register(ModularRouters.modId + ":redstoneUpgrade", RedstoneEnhancementRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        for (ModuleType type : ModuleType.values()) {
            GameRegistry.addRecipe(new RedstoneEnhancementRecipe(type));
        }
    }

    private static void addRegulatorUpgradeRecipes() {
        RecipeSorter.register(ModularRouters.modId + ":regulatorUpgrade", RegulatorEnhancementRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        for (ModuleType type : ModuleType.values()) {
            if (RegulatorEnhancementRecipe.appliesTo(type)) {
                GameRegistry.addRecipe(new RegulatorEnhancementRecipe(type));
            }
        }
    }

    private static void addPickupDelayRecipes() {
        RecipeSorter.register(ModularRouters.modId + ":pickupDelayUpgrade", PickupDelayEnhancementRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        for (ModuleType type : new ItemModule.ModuleType[] { ModuleType.DROPPER, ModuleType.FLINGER} ) {
            GameRegistry.addRecipe(new PickupDelayEnhancementRecipe(type));
        }
    }
}

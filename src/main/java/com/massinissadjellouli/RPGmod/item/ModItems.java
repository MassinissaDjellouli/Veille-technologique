package com.massinissadjellouli.RPGmod.item;

import com.massinissadjellouli.RPGmod.RPGMod;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEM_DEFERRED_REGISTER =
            DeferredRegister.create(ForgeRegistries.ITEMS, RPGMod.MODID);


    //COPPER TOOLS
    //Properties: Tier,AttackDamageModifer,AttackSpeed,Tab
    public static final RegistryObject<PickaxeItem> COPPER_PICKAXE =
        createPickaxe("copper_pickaxe",ToolTiers.COPPER);
    public static final RegistryObject<AxeItem> COPPER_AXE =
        createAxe("copper_axe", ToolTiers.COPPER);
    public static final RegistryObject<ShovelItem> COPPER_SHOVEL =
        createShovel("copper_shovel", ToolTiers.COPPER);
    public static final RegistryObject<HoeItem> COPPER_HOE =
        createHoe("copper_hoe", ToolTiers.COPPER);
    public static final RegistryObject<SwordItem> COPPER_SWORD =
        createSword("copper_sword", ToolTiers.COPPER);

    //Steel
    public static final RegistryObject<Item> STEEL_INGOT = createMiscItem("steel_ingot");

    //Crystals
    public static final RegistryObject<Item> FIRE_CRYSTAL = createMiscItem("fire_crystal");
    public static final RegistryObject<Item> ICE_CRYSTAL = createMiscItem("ice_crystal");
    public static final RegistryObject<Item> POISON_CRYSTAL = createMiscItem("poison_crystal");

    //Titanium
    public static final RegistryObject<Item> TITANIUM_INGOT = createMiscItem("titanium_ingot");
    public static final RegistryObject<Item> REINFORCED_TITANIUM_INGOT = createMiscItem("reinforced_titanium_ingot");

    //Steel tools
    public static final RegistryObject<PickaxeItem> STEEL_PICKAXE =
        createPickaxe("steel_pickaxe",ToolTiers.STEEL);
    public static final RegistryObject<AxeItem> STEEL_AXE =
       createAxe("steel_axe", ToolTiers.STEEL);
    public static final RegistryObject<HoeItem> STEEL_HOE =
       createHoe("steel_hoe", ToolTiers.STEEL);
    public static final RegistryObject<ShovelItem> STEEL_SHOVEL =
       createShovel("steel_shovel", ToolTiers.STEEL);
    public static final RegistryObject<SwordItem> STEEL_SWORD =
       createSword("steel_sword", ToolTiers.STEEL);

    //Titanium tools
    public static final RegistryObject<PickaxeItem> TITANIUM_PICKAXE =
            createPickaxe("titanium_pickaxe",ToolTiers.TITANIUM);
    public static final RegistryObject<AxeItem> TITANIUM_AXE =
            createAxe("titanium_axe", ToolTiers.TITANIUM);
    public static final RegistryObject<HoeItem> TITANIUM_HOE =
            createHoe("titanium_hoe", ToolTiers.TITANIUM);
    public static final RegistryObject<ShovelItem> TITANIUM_SHOVEL =
            createShovel("titanium_shovel", ToolTiers.TITANIUM);
    public static final RegistryObject<SwordItem> TITANIUM_SWORD =
            createSword("titanium_sword", ToolTiers.TITANIUM);

    // Reinforced titanium tools
    public static final RegistryObject<PickaxeItem> REINFORCED_TITANIUM_PICKAXE =
            createPickaxe("reinforced_titanium_pickaxe",ToolTiers.REINFORCED_TITANIUM);

    public static final RegistryObject<AxeItem> REINFORCED_TITANIUM_AXE =
            createAxe("reinforced_titanium_axe", ToolTiers.REINFORCED_TITANIUM);

    public static final RegistryObject<HoeItem> REINFORCED_TITANIUM_HOE =
            createHoe("reinforced_titanium_hoe", ToolTiers.REINFORCED_TITANIUM);

    public static final RegistryObject<ShovelItem> REINFORCED_TITANIUM_SHOVEL =
            createShovel("reinforced_titanium_shovel", ToolTiers.REINFORCED_TITANIUM);

    public static final RegistryObject<SwordItem> REINFORCED_TITANIUM_SWORD =
            createSword("reinforced_titanium_sword", ToolTiers.REINFORCED_TITANIUM);

    //Create objects functions
    private static RegistryObject<SwordItem> createSword(String name,Tier tier){
        return ITEM_DEFERRED_REGISTER.register(name,
                ()-> new SwordItem(
                        tier,4,-2.4f,
                        new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
    }
    private static RegistryObject<PickaxeItem> createPickaxe(String name,Tier tier){
        return ITEM_DEFERRED_REGISTER.register(name,
                ()-> new PickaxeItem(tier,1,-2.8f,
                        new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)));
    }
    private static RegistryObject<AxeItem> createAxe(String name,Tier tier){
        return ITEM_DEFERRED_REGISTER.register(name,
                ()-> new AxeItem(tier,5,-3f,
                        new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)));
    }
    private static RegistryObject<HoeItem> createHoe(String name,Tier tier){
        return ITEM_DEFERRED_REGISTER.register(name,
                ()-> new HoeItem(tier,-2,0,
                        new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)));
    }
    private static RegistryObject<ShovelItem> createShovel(String name,Tier tier){
        return ITEM_DEFERRED_REGISTER.register(name,
                ()-> new ShovelItem(tier,2,-3f,
                        new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)));
    }
    private static RegistryObject<Item> createMiscItem(String name){
        return ITEM_DEFERRED_REGISTER.register(name,
                ()-> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    }


    public static void registerItems(IEventBus eventBus){
        ITEM_DEFERRED_REGISTER.register(eventBus);
    }
}

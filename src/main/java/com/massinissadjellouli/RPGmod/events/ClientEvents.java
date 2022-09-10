package com.massinissadjellouli.RPGmod.events;

import com.massinissadjellouli.RPGmod.RPGMod;
import com.massinissadjellouli.RPGmod.client.ClientGamemodeData;
import com.massinissadjellouli.RPGmod.client.ThirstHudOverlay;
import com.massinissadjellouli.RPGmod.networking.ModPackets;
import com.massinissadjellouli.RPGmod.networking.packet.*;
import com.massinissadjellouli.RPGmod.tags.ModTags;
import com.massinissadjellouli.RPGmod.tags.ModTags.Items.RarityTags;
import com.massinissadjellouli.RPGmod.thirst.PlayerThirst;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = RPGMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    private static final int DEFENSE_INCREASE_PERCENT = 10;
    private static final int PLAYER_SPEED_INCREASE_PERCENT = 2;
    private static final int ATTACK_DAMAGE_INCREASE_PERCENT = 10;
    private static final int ATTACK_SPEED_INCREASE_PERCENT = 10;
    private static final DecimalFormat DECIMAL_FORMATER = getDecimalFormater();
    private static final String TOOLTIPS_TAB = " ";

    @SubscribeEvent
    public static void onLivingEntityUseItem(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level.isClientSide && event.getItem().is(Items.POTION) && event.getDuration() == 0) {
            ModPackets.sendToServer(new DrankC2SPacket());
        }
    }

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
            event.getEntity().setCustomName(Component.literal(
                            DECIMAL_FORMATER.format(
                                    Math.max(event.getEntity().getHealth() - event.getAmount(),0)) + "/" +
                            DECIMAL_FORMATER.format(event.getEntity().getMaxHealth()))
                    .withStyle(ChatFormatting.RED));
     }

    @SubscribeEvent
    public static void onGMChange(PlayerEvent.PlayerChangeGameModeEvent event) {
        ClientGamemodeData.setIsSurvival(event.getCurrentGameMode().isSurvival());
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (event.getEntity().is(player) && event.getEntity().level.isClientSide) {
            ModPackets.sendToServer(new JumpReduceThirstC2SPacket());

        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient()) {
            if (event.player.isSprinting()) {
                PlayerThirst.setReduceByTick(PlayerThirst.getReduceByTick() + 0.4f);
            }
            ModPackets.sendToServer(new ReduceThirstByTickC2SPacket());
            ModPackets.sendToServer(new GamemodeDataSyncC2SPacket());
            ModPackets.sendToServer(new ThirstEffectC2SPacket());
        }
    }

    private static void setWeaponAttributes(ItemTooltipEvent event) {
        ItemStack weapon = event.getItemStack();
        ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        for (RarityTags tagKey : RarityTags.values()) {

            if (tagManager.getTag(tagKey.tagKey).contains(weapon.getItem())
                    && tagManager.getTag(Tags.Items.TOOLS_SWORDS).contains(weapon.getItem())) {
                float damage = ((SwordItem) weapon.getItem()).getDamage();
                float speed = ((SwordItem) weapon.getItem()).getTier().getSpeed();
                float damageIncrease = damage * (1f + ((float) tagKey.level * ATTACK_DAMAGE_INCREASE_PERCENT) / 100);
                float speedIncrease = speed * (1f + ((float) tagKey.level * ATTACK_SPEED_INCREASE_PERCENT) / 100) - speed;
                final AttributeModifier ATTACK_MODIFIER =
                        new AttributeModifier(UUID.fromString("f83fdd4d-7c5f-4433-92b3-0cc5592ef67c"), "ATTACK_MODIFIER",
                                damageIncrease, AttributeModifier.Operation.ADDITION);
                final AttributeModifier SPEED_MODIFIER =
                        new AttributeModifier(UUID.fromString("bfa50c88-ca13-4224-b73f-0a500d6889e3"), "SPEED_MODIFIER",
                                speedIncrease, AttributeModifier.Operation.ADDITION);
                if (attributeNotPresent(weapon.getItem(), ATTACK_MODIFIER) && attributeNotPresent(weapon.getItem(), SPEED_MODIFIER) && damageIncrease > 0) {
                    weapon.addAttributeModifier(Attributes.ATTACK_DAMAGE, ATTACK_MODIFIER, EquipmentSlot.MAINHAND);
                    weapon.addAttributeModifier(Attributes.ATTACK_SPEED, SPEED_MODIFIER, EquipmentSlot.MAINHAND);
                    weapon.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);
                }
                break;
            }
        }
    }

    private static void setArmorAttributes(ItemTooltipEvent event, RarityTags tagKey) {
        ItemStack armor = event.getItemStack();
        ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        if (tagManager.getTag(tagKey.tagKey).contains(armor.getItem())
                && tagManager.getTag(Tags.Items.ARMORS).contains(armor.getItem())) {
            ArmorItem armorItem = (ArmorItem) armor.getItem();
            float toughness = armorItem.getToughness();
            float toughnessIncrease = (1 + toughness) * (1f + ((float) tagKey.level * TOUGHNESS_INCREASE_PERCENT) / 100);
            float speedIncrease = (((float) tagKey.level * PLAYER_SPEED_INCREASE_PERCENT) / 100);
            final AttributeModifier DEFENSE_MODIFIER =
                    new AttributeModifier(UUID.fromString("f83fdd4d-7c5f-4433-92b3-0cc5592ef67c"), "DEFENSE_MODIFIER",
                            armorItem.getDefense(), AttributeModifier.Operation.ADDITION);
            final AttributeModifier TOUGHNESS_MODIFIER =
                    new AttributeModifier(UUID.fromString("f83fdd4d-7c5f-4433-92b3-0cc5592ef67c"), "TOUGHNESS_MODIFIER",
                            toughnessIncrease, AttributeModifier.Operation.ADDITION);
            final AttributeModifier SPEED_MODIFIER =
                    new AttributeModifier(UUID.fromString("bfa50c88-ca13-4224-b73f-0a500d6889e3"), "SPEED_MODIFIER",
                            speedIncrease, AttributeModifier.Operation.MULTIPLY_TOTAL);
            if (attributeNotPresent(armor.getItem(), TOUGHNESS_MODIFIER)
                    && attributeNotPresent(armor.getItem(), SPEED_MODIFIER)
                    && attributeNotPresent(armor.getItem(), DEFENSE_MODIFIER)
                    && toughnessIncrease > 0) {
                armor.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, TOUGHNESS_MODIFIER, armorItem.getSlot());
                armor.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER, armorItem.getSlot());
                armor.addAttributeModifier(Attributes.ARMOR, DEFENSE_MODIFIER, armorItem.getSlot());
            }
        }
    }

    private static DecimalFormat getDecimalFormater() {
        DecimalFormat decimalFormat = new DecimalFormat("##.#");
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        return decimalFormat;
    }

    @SubscribeEvent
    public static void itemTooltip(ItemTooltipEvent event) {
        ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        Item item = event.getItemStack().getItem();
        for (RarityTags tagKey : RarityTags.values()) {
            TagKey<Item> tag = tagKey.tagKey;
            if (tagManager.getTag(tag).contains(item)) {
                setTooltip(event,tagKey);
                break;
            }
        }

        setWeaponAttributes(event);
        //setArmorAttributes(event);

    }

    private static void setAttributes(ItemTooltipEvent event, RarityTags tagKey) {
        Item item = event.getItemStack().getItem();
        if (item instanceof SwordItem) {
            setWeaponAttributes(event, tagKey);
        } else if (item instanceof ArmorItem) {
            setArmorAttributes(event, tagKey);
        }
    }

    private static void setTooltip(ItemTooltipEvent event, RarityTags tagKey) {
        ChatFormatting style = tagKey.style;
        List<Component> oldToolTips = List.copyOf(event.getToolTip());
        resetTooltips(event,oldToolTips,style);
        setTooltipBody(tagKey,event);
        addTooltipFooter(tagKey,oldToolTips,event);

    }

    private static void setTooltipBody(RarityTags tagKey, ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        if(item instanceof SwordItem){
            setSwordTooltips(tagKey, event);
        }else if (item instanceof ArmorItem){
            setArmorTooltips(tagKey, event);
        }
    }

    private static void addTooltipFooter(RarityTags tagKey,List<Component> oldToolTips, ItemTooltipEvent event) {
        ChatFormatting style = tagKey.style;
        String rarity = tagKey.name;
        String nbt = getNBTTooltip(oldToolTips);
        String id = getIdTooltip(oldToolTips);
        event.getToolTip().add(Component.literal(rarity).withStyle(style));
        if (nbt.length() > 0) {
            event.getToolTip().add(Component.literal(id).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (id.length() > 0) {
            event.getToolTip().add(Component.literal(nbt).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static void resetTooltips(ItemTooltipEvent event, List<Component> oldToolTips, ChatFormatting style) {
        event.getToolTip().clear();
        String title = oldToolTips.get(0).getString();
        event.getToolTip().add(0, Component.literal(title).withStyle(style));
    }

    private static void setArmorTooltips(RarityTags tagKey, ItemTooltipEvent event) {
        ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        Item item = event.getItemStack().getItem();
        ArmorItem armorItem = (ArmorItem) event.getItemStack().getItem();
        String bodypart = "";
        switch (armorItem.getSlot()) {
            case HEAD -> bodypart = "Sur la tête";
            case CHEST -> bodypart = "Sur le corps";
            case LEGS -> bodypart = "Aux jambes";
            case FEET -> bodypart = "Aux pieds";

        }


            event.getToolTip().add(Component.literal(""));
        event.getToolTip().add(Component.literal(bodypart + " :").withStyle(ChatFormatting.GRAY));
        if (tagManager.getTag(Tags.Items.ARMORS).contains(item)) {
            event.getToolTip().add(Component.literal("+" +
                    armorItem.getDefense() +
                    " de points d'armure"
            ).withStyle(ChatFormatting.BLUE));

            float toughness = armorItem.getToughness();
            float toughnessIncrease = (1 + toughness) * (1f + ((float) tagKey.level * TOUGHNESS_INCREASE_PERCENT) / 100);
            if (toughnessIncrease > 0) {

                event.getToolTip().add(Component.literal("+" +
                        DECIMAL_FORMATER.format(toughnessIncrease) +
                        " de robustesse"
                ).withStyle(ChatFormatting.BLUE));
            }

            float speedIncrease = (((float) tagKey.level * PLAYER_SPEED_INCREASE_PERCENT) / 100);
            if (speedIncrease > 0) {

                event.getToolTip().add(Component.literal("+" +
                        DECIMAL_FORMATER.format(speedIncrease * 100) +
                        " % de vitesse"
                ).withStyle(ChatFormatting.BLUE));
            }
            if (TOUGHNESS_INCREASE_PERCENT * tagKey.level > 0) {
                event.getToolTip().add(
                        Component.literal(
                                TOUGHNESS_INCREASE_PERCENT * tagKey.level
                                        + "% de robustesse en plus"
                        ).withStyle(ChatFormatting.RED)
                );
            }
            if (speedIncrease > 0) {
                event.getToolTip().add(
                        Component.literal(
                                speedIncrease * 100
                                        + "% de vitesse en plus"
                        ).withStyle(ChatFormatting.RED)
                );
            }
        }
    }



    private static void setSwordTooltips(RarityTags tagKey, ItemTooltipEvent event) {
        ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        Item item = event.getItemStack().getItem();
        if (tagManager.getTag(Tags.Items.TOOLS_SWORDS).contains(item)) {
            float dmgIncrease = tagKey.level * ATTACK_DAMAGE_INCREASE_PERCENT;
            float dmg = ((SwordItem) item).getDamage();


            event.getToolTip().add(Component.literal(""));
            event.getToolTip().add(Component.literal("Dans la main principale :").withStyle(ChatFormatting.GRAY));
            event.getToolTip().add(Component.literal(TOOLTIPS_TAB +
                    DECIMAL_FORMATER.format(dmg) + " de dégâts d'attaque").withStyle(ChatFormatting.DARK_GREEN));
            String bonusDmg = getBonusDmg(dmg, dmgIncrease, DECIMAL_FORMATER);
            if (dmgIncrease > 0) {
                event.getToolTip().add(Component.literal(bonusDmg)
                        .withStyle(ChatFormatting.DARK_GREEN));
                if (event.getItemStack().getDamageValue() > 0) {

                    event.getToolTip().add(Component.literal(
                            "Durabilité: " +
                                    (event.getItemStack().getMaxDamage() -
                                            event.getItemStack().getDamageValue()) + " / " +
                                    event.getItemStack().getMaxDamage()
                    ));
                }
                String damageIncreasePercent = DECIMAL_FORMATER.format(dmgIncrease) + "% de dégâts en plus";
                event.getToolTip().add(Component.literal(damageIncreasePercent)
                        .withStyle(ChatFormatting.RED));
                String attackSpeedIncreasePercent = DECIMAL_FORMATER.format(
                        ATTACK_SPEED_INCREASE_PERCENT * tagKey.level) + "% plus rapide";
                event.getToolTip().add(Component.literal(attackSpeedIncreasePercent)
                        .withStyle(ChatFormatting.RED));
            }
        }
    }

    private static String getNBTTooltip(List<Component> oldToolTips) {
        for (Component tooltip : oldToolTips) {
            String toolitpString = tooltip.getString();
            if (toolitpString.contains("NBT")) {
                return toolitpString;
            }
        }
        return "";
    }

    private static String getIdTooltip(List<Component> oldToolTips) {
        for (Component tooltip : oldToolTips) {
            String toolitpString = tooltip.getString();
            if (toolitpString.contains("minecraft:") || toolitpString.contains(RPGMod.MODID + ":")) {
                return toolitpString;
            }
        }
        return "";
    }
    private static String getBonusDmg(float dmg, float dmgIncrease, DecimalFormat decimalFormat) {
        String bonusDmg = "";
        if (dmgIncrease > 0) {
            float dmgBonus = dmg * (1 + dmgIncrease / 100) - dmg;
            String isPlural = (dmg * (1 + dmgIncrease / 100) - dmg) > 1 ? "s" : "";
            bonusDmg = TOOLTIPS_TAB + "+" +
                    decimalFormat.format(dmgBonus) +
                    " dégât" + isPlural + " en plus";
        }
        return bonusDmg;
    }

    private static boolean attributeNotPresent(Item weapon, AttributeModifier attribute) {
        return !weapon.getDefaultInstance().getAttributeModifiers(EquipmentSlot.MAINHAND).containsValue(attribute);
    }



    @Mod.EventBusSubscriber(modid = RPGMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("thirst", ThirstHudOverlay.HUD_THIRST);
        }
    }

}
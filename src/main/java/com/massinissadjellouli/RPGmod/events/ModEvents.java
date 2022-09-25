package com.massinissadjellouli.RPGmod.events;

import com.massinissadjellouli.RPGmod.RPGMod;
import com.massinissadjellouli.RPGmod.skills.PlayerSkillProvider;
import com.massinissadjellouli.RPGmod.skills.PlayerSkills;
import com.massinissadjellouli.RPGmod.skills.PlayerSkillsData.SkillData;
import com.massinissadjellouli.RPGmod.thirst.PlayerThirst;
import com.massinissadjellouli.RPGmod.thirst.PlayerThirstProvider;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.checkerframework.checker.units.qual.A;

import static com.massinissadjellouli.RPGmod.skills.PlayerSkillData.PlayerSkillEnum.*;

@Mod.EventBusSubscriber(modid = RPGMod.MODID,value = Dist.CLIENT)
public class ModEvents {
 @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent event){
     if(event.getObject() instanceof Player){
         if(!((Player) event.getObject()).getCapability(PlayerThirstProvider.PLAYER_THIRST).isPresent()){
             event.addCapability(new ResourceLocation(RPGMod.MODID,"thirst"), new PlayerThirstProvider());
         }
         if(!((Player) event.getObject()).getCapability(PlayerSkillProvider.PLAYER_SKILLS).isPresent()){
             event.addCapability(new ResourceLocation(RPGMod.MODID,"skills"), new PlayerSkillProvider());
         }
     }
 }


 @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event){
     event.getOriginal().reviveCaps();
     if(event.isWasDeath()) {
         event.getOriginal().getCapability(PlayerSkillProvider.PLAYER_SKILLS).ifPresent(oldStore ->{
             event.getEntity().getCapability(PlayerSkillProvider.PLAYER_SKILLS).ifPresent(newStore -> {
                 newStore.copyFrom(oldStore);
             });
         });
     }
     event.getEntity().getAttributes().assignValues(event.getOriginal().getAttributes());
     resetBonusesEffects(event.getEntity());
 }

    private static void resetBonusesEffects(Player player) {
     player.getCapability(PlayerSkillProvider.PLAYER_SKILLS).ifPresent((capability)->{
            if(capability.getSkill(MINING).isEmpty()) return;
            if(capability.getSkill(ATTACKING).isEmpty()) return;
            if(capability.getSkill(FORAGING).isEmpty()) return;
            SkillData mining =  capability.getSkill(MINING).get();
            SkillData foraging =  capability.getSkill(FORAGING).get();
            SkillData attacking =  capability.getSkill(ATTACKING).get();
            if(!player.level.isClientSide){
                int hasteLevel = mining.level/10;
                int foragingLevel = foraging.level/10;
                MobEffect foragingEffect = foragingLevel < 4 ? MobEffects.DAMAGE_RESISTANCE : MobEffects.DAMAGE_BOOST;
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,100000, hasteLevel));
                player.addEffect(new MobEffectInstance(foragingEffect,100000, foragingLevel));
            }
     });
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event){
    event.register(PlayerThirst.class);
    event.register(PlayerSkills.class);
 }



}

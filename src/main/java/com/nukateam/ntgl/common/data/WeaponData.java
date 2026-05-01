package com.nukateam.ntgl.common.data;

import com.nukateam.ntgl.common.data.holders.WeaponMode;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;

import javax.annotation.Nullable;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class WeaponData{
    @Nullable public final ItemStack weapon;
    @Nullable public ItemStack attachment;
    @Nullable public final LivingEntity wielder;
    public WeaponMode weaponMode = WeaponMode.PRIMARY;

    public WeaponData(ItemStack weapon, LivingEntity wielder) {
        this.weapon = weapon;
        this.wielder = wielder;
    }

    public WeaponData setAttachment(@Nullable ItemStack attachment) {
        this.attachment = attachment;
        return this;
    }

    public WeaponData setWeaponMode(WeaponMode weaponMode) {
        this.weaponMode = weaponMode;
        return this;
    }

    @Override
    public WeaponData clone(){
        return new WeaponData(weapon, wielder).setWeaponMode(weaponMode).setAttachment(attachment);
    }

    public RegistryAccess registryAccess() {
        if (wielder != null) {
            return wielder.level().registryAccess();
        }
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess();
        }
        if (FMLEnvironment.dist.isClient()) {
            var mc = Minecraft.getInstance();
            if (mc.level != null) {
                return mc.level.registryAccess();
            }
            if (mc.getConnection() != null) {
                return mc.getConnection().registryAccess();
            }
        }
        return RegistryAccess.EMPTY;
    }

}

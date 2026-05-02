package com.nukateam.ntgl.client.compat;

import com.nukateam.ntgl.client.util.handler.ClientMeleeHandler;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * When Better Combat performs a melee swing, forwards eligible NTGL weapons to the mod's own melee
 * pipeline so explosion / cone damage stay authoritative. Weapon attributes zero out BC damage on hits.
 */
@OnlyIn(Dist.CLIENT)
public final class BetterCombatClientHooks {

    private BetterCombatClientHooks() {}

    public static void register() {
        BetterCombatClientEvents.ATTACK_HIT.register(BetterCombatClientHooks::onAttackHit);
    }

    @SuppressWarnings("unused")
    private static void onAttackHit(AbstractClientPlayer player, AttackHand attackHand,
                                    List<?> targets, Entity cursorTarget) {
        if (attackHand.isOffHand()) return;

        ItemStack stack = attackHand.itemStack();
        if (!(stack.getItem() instanceof IWeapon)) return;

        var data = new WeaponData(stack, player);
        if (!WeaponModifierHelper.canMelee(data)) return;

        ClientMeleeHandler.addTracker(data, InteractionHand.MAIN_HAND);
    }
}

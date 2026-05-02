package com.nukateam.ntgl.common.util.helpers;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class PlayerHelper {
    public static HumanoidArm convertHand(InteractionHand hand){
        return hand == InteractionHand.MAIN_HAND ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
    }

    /**
     * Whether {@code hand} is the entity's main hand, using the entity's dominant arm (third-person safe).
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean isRight(LivingEntity entity, InteractionHand hand) {
        var mainArm = entity.getMainArm();
        return mainArm == HumanoidArm.RIGHT
                ? hand == InteractionHand.MAIN_HAND
                : hand == InteractionHand.OFF_HAND;
    }

    public static InteractionHand convertHand(HumanoidArm arm){
        return arm == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static InteractionHand getOpposite(InteractionHand arm) {
        return arm == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }
}

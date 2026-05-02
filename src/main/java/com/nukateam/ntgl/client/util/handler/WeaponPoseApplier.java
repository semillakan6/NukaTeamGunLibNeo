package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.IdentityHashMap;

/**
 * Applies NTGL weapon poses to humanoid model parts. Used from {@code setupAnim} and again from
 * {@code renderToBuffer} so poses survive mods (e.g. EMF) that animate after {@code setupAnim}.
 */
@OnlyIn(Dist.CLIENT)
public final class WeaponPoseApplier {
    /**
     * Which entity last ran {@code setupAnim} on each model instance (render thread).
     * Model-keyed avoids wrong entity when several humanoids set up on the same thread.
     */
    private static final IdentityHashMap<HumanoidModel<?>, LivingEntity> POSE_ENTITY = new IdentityHashMap<>();

    private WeaponPoseApplier() {}

    public static void bindPoseEntity(HumanoidModel<?> model, LivingEntity entity) {
        POSE_ENTITY.put(model, entity);
    }

    public static void clearPoseEntity(HumanoidModel<?> model) {
        POSE_ENTITY.remove(model);
    }

    public static LivingEntity getPoseEntity(HumanoidModel<?> model) {
        return POSE_ENTITY.get(model);
    }

    public static <T extends LivingEntity> void applyWeaponPose(HumanoidModel<T> model, T entity) {
        setupForArm(entity, model, InteractionHand.MAIN_HAND);
        setupForArm(entity, model, InteractionHand.OFF_HAND);
    }

    private static <T extends LivingEntity> void setupForArm(
            T entity, HumanoidModel<T> model, InteractionHand interactionHand) {
        var heldItem = entity.getItemInHand(interactionHand);

        if (heldItem.getItem() instanceof IWeapon) {
            var aimProgress =
                    AimingHandler.get().getAimProgress(entity, Minecraft.getInstance().getTimer().getRealtimeDeltaTicks());
            var gripType = WeaponModifierHelper.getGripType(new WeaponData(heldItem, entity));

            gripType.getHeldAnimation().applyHumanoidModelRotation(
                    entity, model.rightArm, model.leftArm, model.head, interactionHand, aimProgress);

            if (model instanceof PlayerModel<T> playerModel) {
                copyModelAngles(playerModel.rightArm, playerModel.rightSleeve);
                copyModelAngles(playerModel.leftArm, playerModel.leftSleeve);
            }
            copyModelAngles(model.head, model.hat);
        }
    }

    private static void copyModelAngles(ModelPart source, ModelPart target) {
        target.xRot = source.xRot;
        target.yRot = source.yRot;
        target.zRot = source.zRot;
    }
}

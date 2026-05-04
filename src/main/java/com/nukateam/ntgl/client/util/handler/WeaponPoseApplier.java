package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.client.compat.EmfAnimationCompat;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.lang.reflect.Method;
import java.util.IdentityHashMap;

/**
 * Applies NTGL weapon poses to humanoid model parts. Used from {@code setupAnim} and again from
 * {@link HumanoidModel#renderToBuffer} so poses survive mods (e.g. EMF) that animate per-part during the mesh pass.
 * When EMF is installed, {@link com.nukateam.ntgl.client.compat.EmfAnimationCompat} asks EMF to skip custom math on
 * the arm {@link net.minecraft.client.model.geom.ModelPart} instances we drive, so NTGL rotations are not overwritten
 * every frame.
 *
 * <p>All arm/head access goes through {@link HumanoidModel#getArm} and {@link HumanoidModel#getHead}
 * rather than the raw fields ({@code model.rightArm}, {@code model.head}). Some mods (e.g. EMF) subclass
 * {@link HumanoidModel} and override {@code getArm}/{@code getHead} to return their own animated
 * {@link ModelPart} objects, while the vanilla fields stay at idle. Writing to the overridden parts
 * ensures {@link net.minecraft.client.model.HumanoidModel#translateToHand} — which also calls
 * {@code getArm} internally — reads the same parts we just rotated.
 */
@OnlyIn(Dist.CLIENT)
public final class WeaponPoseApplier {
    /**
     * Which entity last ran {@code setupAnim} on each model instance (render thread).
     * Model-keyed avoids wrong entity when several humanoids set up on the same thread.
     */
    private static final IdentityHashMap<HumanoidModel<?>, LivingEntity> POSE_ENTITY = new IdentityHashMap<>();

    /** Cached reflection handle for {@code HumanoidModel.getArm(HumanoidArm)} — resolved once. */
    private static final Method GET_ARM_METHOD;

    static {
        Method m = null;
        try {
            m = HumanoidModel.class.getDeclaredMethod("getArm", HumanoidArm.class);
            m.setAccessible(true);
        } catch (Exception ignored) {}
        GET_ARM_METHOD = m;
    }

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
        // EMF re-applies CEM on each ModelPart render; pause arm animations on our grip parts only.
        EmfAnimationCompat.syncWeaponArmAnimationPause(model, entity);
    }

    private static <T extends LivingEntity> void setupForArm(
            T entity, HumanoidModel<T> model, InteractionHand interactionHand) {
        var heldItem = entity.getItemInHand(interactionHand);

        if (heldItem.getItem() instanceof IWeapon) {
            var aimProgress =
                    AimingHandler.get().getAimProgress(entity, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));
            var gripType = WeaponModifierHelper.getGripType(new WeaponData(heldItem, entity));

            // Call getArm() / getHead() via reflection so the call is virtual: if EMF (or any other mod)
            // overrides these protected methods to return its own animated ModelPart objects, we rotate
            // those same parts — the same objects translateToHand() will later read.
            // HumanoidModel.getArm is protected so we can't call it directly from outside the hierarchy;
            // HeadedModel.getHead() is public and always returns the right part for the head.
            var rightArm = getArmPart(model, HumanoidArm.RIGHT);
            var leftArm  = getArmPart(model, HumanoidArm.LEFT);
            var head     = ((HeadedModel) model).getHead();

            gripType.getHeldAnimation().applyHumanoidModelRotation(
                    entity, rightArm, leftArm, head, interactionHand, aimProgress);

            if (model instanceof PlayerModel<T> playerModel) {
                copyModelAngles(rightArm, playerModel.rightSleeve);
                copyModelAngles(leftArm,  playerModel.leftSleeve);
            }
            copyModelAngles(head, model.hat);
        }
    }

    /**
     * Returns the arm {@link ModelPart} via the cached reflection handle for
     * {@code HumanoidModel.getArm(HumanoidArm)}, so the call dispatches polymorphically to any
     * subclass override (e.g. EMF's). Falls back to the vanilla fields if reflection fails.
     */
    public static ModelPart getArmPart(HumanoidModel<?> model, HumanoidArm arm) {
        if (GET_ARM_METHOD != null) {
            try {
                return (ModelPart) GET_ARM_METHOD.invoke(model, arm);
            } catch (Exception ignored) {}
        }
        return arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
    }

    private static void copyModelAngles(ModelPart source, ModelPart target) {
        target.xRot = source.xRot;
        target.yRot = source.yRot;
        target.zRot = source.zRot;
    }
}

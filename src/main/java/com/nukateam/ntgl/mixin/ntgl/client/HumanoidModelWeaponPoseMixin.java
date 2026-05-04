package com.nukateam.ntgl.mixin.ntgl.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.client.util.handler.WeaponPoseApplier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Re-applies NTGL weapon poses at {@link HumanoidModel} mesh time. On 1.21+ the player model is a
 * {@link HumanoidModel} / {@link net.minecraft.client.model.HierarchicalModel}, not an
 * {@link net.minecraft.client.model.AgeableListModel}, so {@code AgeableListModel} mixins never run for players.
 * <p>
 * EMF runs per-part animation after {@code setupAnim}; a HEAD/TAIL pass here keeps arm fields consistent
 * for the body draw and for layers (held item, etc.).
 */
@Mixin(value = HumanoidModel.class, priority = 100)
public class HumanoidModelWeaponPoseMixin {
    @Inject(
            method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            at = @At("HEAD"),
            order = 2100)
    private void ntgl$reapplyWeaponPoseBeforeMesh(
            PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
        var entity = WeaponPoseApplier.getPoseEntity((HumanoidModel<?>) (Object) this);
        if (entity == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        var model = (HumanoidModel<LivingEntity>) (Object) this;
        WeaponPoseApplier.applyWeaponPose(model, entity);
    }

    @Inject(
            method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            at = @At("TAIL"),
            order = 2100)
    private void ntgl$reapplyWeaponPoseAfterMesh(
            PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
        var entity = WeaponPoseApplier.getPoseEntity((HumanoidModel<?>) (Object) this);
        if (entity != null) {
            @SuppressWarnings("unchecked")
            var model = (HumanoidModel<LivingEntity>) (Object) this;
            WeaponPoseApplier.applyWeaponPose(model, entity);
        }
    }
}

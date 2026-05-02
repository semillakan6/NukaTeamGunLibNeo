package com.nukateam.ntgl.mixin.ntgl.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.client.util.handler.WeaponPoseApplier;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Re-applies NTGL weapon poses at {@link AgeableListModel#renderToBuffer} time so they run after
 * other mods (e.g. EMF) that animate between {@code setupAnim} and drawing the model.
 */
@Mixin(AgeableListModel.class)
public class AgeableListModelMixin {
    @Inject(
            method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            at = @At("HEAD"))
    private void ntgl$reapplyWeaponPoseBeforeMesh(
            PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
        if (!((Object) this instanceof HumanoidModel<?> humanoid)) {
            return;
        }
        var entity = WeaponPoseApplier.getPoseEntity(humanoid);
        if (entity == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        var model = (HumanoidModel<LivingEntity>) (Object) humanoid;
        WeaponPoseApplier.applyWeaponPose(model, entity);
    }

    /**
     * EMF (and similar) may overwrite arm {@link net.minecraft.client.model.geom.ModelPart} fields while each
     * part renders. Re-apply after the mesh pass so {@link net.minecraft.client.renderer.entity.layers.ItemInHandLayer}
     * sees the same rotations for {@code translateToHand}.
     */
    @Inject(
            method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            at = @At("TAIL"))
    private void ntgl$reapplyWeaponPoseAfterMesh(
            PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
        if (!((Object) this instanceof HumanoidModel<?> humanoid)) {
            return;
        }
        var entity = WeaponPoseApplier.getPoseEntity(humanoid);
        if (entity != null) {
            @SuppressWarnings("unchecked")
            var model = (HumanoidModel<LivingEntity>) (Object) humanoid;
            WeaponPoseApplier.applyWeaponPose(model, entity);
        }
        WeaponPoseApplier.clearPoseEntity(humanoid);
    }
}

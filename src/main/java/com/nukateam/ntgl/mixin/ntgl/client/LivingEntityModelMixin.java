package com.nukateam.ntgl.mixin.ntgl.client;

import com.nukateam.ntgl.client.util.handler.WeaponPoseApplier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * I eventually want to get rid of this.
 * <p>
 * Author: MrCrayfish
 */
@Mixin(HumanoidModel.class)
public class LivingEntityModelMixin<T extends LivingEntity> {
    @SuppressWarnings({"ConstantConditions"})
    @Inject(method = "setupAnim*", at = @At(value = "HEAD"))
    private void setupAnimHead(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        WeaponPoseApplier.bindPoseEntity((HumanoidModel<?>)(Object)this, entity);
    }

    @SuppressWarnings({"ConstantConditions"})
    @Inject(method = "setupAnim*", at = @At(value = "TAIL"))
    private void setupAnimTail(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        var model = (HumanoidModel<T>)(Object)this;
        WeaponPoseApplier.applyWeaponPose(model, entity);
    }
}

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
/**
 * Low mixin priority so this class merges after most mods (e.g. EMF). High {@code @Inject(order=...)} on
 * {@code setupAnim} tail so the pose apply runs after other injectors at that return.
 */
@Mixin(value = HumanoidModel.class, priority = 100)
public class LivingEntityModelMixin<T extends LivingEntity> {
    @SuppressWarnings({"ConstantConditions"})
    @Inject(method = "setupAnim*", at = @At(value = "HEAD"), order = 900)
    private void setupAnimHead(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        WeaponPoseApplier.bindPoseEntity((HumanoidModel<?>)(Object)this, entity);
    }

    @SuppressWarnings({"ConstantConditions"})
    @Inject(method = "setupAnim*", at = @At(value = "TAIL"), order = 2100)
    private void setupAnimTail(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        var model = (HumanoidModel<T>)(Object)this;
        WeaponPoseApplier.applyWeaponPose(model, entity);
    }
}

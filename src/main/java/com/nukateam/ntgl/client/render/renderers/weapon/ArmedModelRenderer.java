package com.nukateam.ntgl.client.render.renderers.weapon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.handlers.ClientTickHandler;
import com.nukateam.ntgl.client.render.layers.GlowingLayer;
import com.nukateam.ntgl.client.util.helpers.TransformUtils;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.helpers.compatibility.ChassisHelper;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.ClientUtil;
import software.bernie.geckolib.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static com.nukateam.ntgl.client.render.GeoRenderUtils.*;
import static com.nukateam.ntgl.client.util.ClientDebug.*;

public class ArmedModelRenderer<Animator extends ItemAnimator> extends DynamicGeoItemRenderer<Animator> {
    /** FP offsets tuned for one-handed pistols; geo left_arm bone sits farther out than two-handed rigs. */
    private static final double FP_LEFT_ARM_X_ONE = -65 / 10d / 16d;
    private static final double FP_LEFT_ARM_Y_ONE = 0 / 10d / 16d;
    private static final double FP_RIGHT_ARM_X_ONE = 50 / 10d / 16d;
    private static final double FP_RIGHT_ARM_Y_ONE = -20 / 10d / 16d;
    /** Supporting hand pulled inward/down slightly so extended two-handed kits meet the fore grip. */
    private static final double FP_LEFT_ARM_X_TWO = -38 / 10d / 16d;
    private static final double FP_LEFT_ARM_Y_TWO = -8 / 10d / 16d;
    private static final double FP_RIGHT_ARM_X_TWO = 44 / 10d / 16d;
    private static final double FP_RIGHT_ARM_Y_TWO = -22 / 10d / 16d;

    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_ARM = "left_arm";
    public static final String RIGHT_ARM_ANIM = "right_arm_anim";
    public static final String LEFT_ARM_ANIM = "left_arm_anim";
    protected MultiBufferSource bufferSource;
    protected boolean firstRightRender = true;
    protected boolean firstLeftRender = true;
    private ItemDisplayContext transformType;

    public ArmedModelRenderer(GeoModel<Animator> model) {
        super(model);
        addRenderLayer(new GlowingLayer<>(this));
        ClientTickHandler.addTicker(this, this::tick);
    }

    protected void tick(){}

    @Override
    public void render(LivingEntity entity, ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
                       @Nullable MultiBufferSource bufferSource,
                       @Nullable RenderType renderType, @Nullable VertexConsumer buffer, int packedLight) {
        this.bufferSource = bufferSource;
        this.transformType = transformType;
        this.firstRightRender = true;
        this.firstLeftRender  = true;
        this.currentEntity = entity;

        poseStack.pushPose();
        {
            poseStack.translate(0, 0, -50 / 10d / 16d);
            super.render(entity, stack, transformType, poseStack, bufferSource, renderType, buffer, packedLight);
        }
        poseStack.popPose();
    }

    @Override
    public void renderRecursively(PoseStack poseStack, Animator animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer,
                                  boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                  int colour) {
        poseStack.pushPose();

        switch (bone.getName()) {
            case LEFT_ARM, RIGHT_ARM -> {
                bone.setHidden(true);
                bone.setChildrenHidden(false);
                renderArms(poseStack, bone, packedLight, packedOverlay, bufferSource);
            }
            case LEFT_ARM_ANIM, RIGHT_ARM_ANIM ->{
                if(!TransformUtils.isFirstPerson(transformType)){
                    bone.setHidden(true);
                }
                else bone.setHidden(false);
            }
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource,
                this.bufferSource.getBuffer(renderType), isReRender, partialTick, packedLight,
                packedOverlay, colour);
        poseStack.popPose();
    }


    protected void renderArms(PoseStack poseStack, GeoBone bone, int packedLight, int packedOverlay, MultiBufferSource bufferSource) {
        var client = Minecraft.getInstance();
        if(client.player == null) return;

        var isRightHand = this.transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        var isLeftHand = this.transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        if (bone.getName().equals(RIGHT_ARM)){
            if(!firstRightRender)
                return;
            firstRightRender = false;
        }
        if (bone.getName().equals(LEFT_ARM)){
            if(!firstLeftRender)
                return;
            firstLeftRender = false;
        }

        if (isRightHand || isLeftHand) {
            poseStack.pushPose();
            {
                RenderUtil.prepMatrixForBone(poseStack, bone);
                poseStack.translate(0.01, -0.27, 0.05);
                poseStack.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());

                var entity = this.currentEntity;
                var mainStack = entity != null ? entity.getMainHandItem() : ItemStack.EMPTY;
                boolean twoHandedFp = entity != null
                        && mainStack.getItem() instanceof IWeapon
                        && !WeaponModifierHelper.isOneHanded(new WeaponData(mainStack, entity));
                double supportingX = twoHandedFp ? FP_LEFT_ARM_X_TWO : FP_LEFT_ARM_X_ONE;
                double supportingY = twoHandedFp ? FP_LEFT_ARM_Y_TWO : FP_LEFT_ARM_Y_ONE;
                double mainX = twoHandedFp ? FP_RIGHT_ARM_X_TWO : FP_RIGHT_ARM_X_ONE;
                double mainY = twoHandedFp ? FP_RIGHT_ARM_Y_TWO : FP_RIGHT_ARM_Y_ONE;
                double dbgZ = Z / 10d / 16d;

                if(ChassisHelper.isPlayerInChassis()){
                    if(isRightHand) {
                        if (bone.getName().equals(LEFT_ARM)) {
                            ChassisHelper.renderChassisHand(poseStack, isRightHand, HumanoidArm.LEFT, packedLight);
                        } else if (bone.getName().equals(RIGHT_ARM)) {
                            ChassisHelper.renderChassisHand(poseStack, isRightHand, HumanoidArm.RIGHT, packedLight);
                        }
                    } else {
                        if (bone.getName().equals(LEFT_ARM)) {
                            ChassisHelper.renderChassisHand(poseStack, isRightHand, HumanoidArm.RIGHT, packedLight);
                        } else if (bone.getName().equals(RIGHT_ARM)) {
                            ChassisHelper.renderChassisHand(poseStack, isRightHand, HumanoidArm.LEFT, packedLight);
                        }
                    }
                }
                else {
                    if (isRightHand) {
                        if (bone.getName().equals(LEFT_ARM)) {
                            poseStack.translate(supportingX, supportingY, dbgZ);
                            renderRightArm(poseStack, bone, packedLight, bufferSource, false);
                        } else if (bone.getName().equals(RIGHT_ARM)) {
                            poseStack.translate(mainX, mainY, dbgZ);
                            renderRightArm(poseStack, bone, packedLight, bufferSource, true);
                        }
                    } else {
                        if (bone.getName().equals(LEFT_ARM)) {
                            poseStack.translate(mainX, mainY, dbgZ);
                            renderRightArm(poseStack, bone, packedLight, bufferSource, true);
                        } else if (bone.getName().equals(RIGHT_ARM)) {
                            poseStack.translate(supportingX, supportingY, dbgZ);
                            renderRightArm(poseStack, bone, packedLight, bufferSource, false);
                        }
                    }
                }
            }
            poseStack.popPose();
        }
    }
}

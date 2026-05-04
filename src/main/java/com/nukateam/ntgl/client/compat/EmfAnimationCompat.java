package com.nukateam.ntgl.client.compat;

import com.nukateam.ntgl.client.util.handler.WeaponPoseApplier;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optional integration with Entity Model Features (EMF): EMF re-applies CEM animation on each
 * {@link net.minecraft.client.model.geom.ModelPart} render via {@code EMFModelPartWithState}, which
 * overwrites NTGL weapon poses set in {@code setupAnim}. EMF exposes
 * {@code EMFAnimationApi.pauseCustomAnimationsForThesePartsOfEntity} so animations skip the arm
 * parts we control — we resolve the API reflectively so NTGL does not hard-depend on EMF at compile time.
 * <p>
 * When clearing the pause, we only remove our entry from EMF's {@code entitiesPausedParts} map. We avoid
 * {@code resumeAllCustomAnimationsForEntity} because that also clears {@code entitiesPaused} and can break
 * unrelated features (e.g. other mods or client effects that rely on EMF's full-entity pause state).
 *
 * @see <a href="https://github.com/Traben-0/Entity_Model_Features">Entity Model Features</a>
 */
@OnlyIn(Dist.CLIENT)
public final class EmfAnimationCompat {
    /** UUIDs for which NTGL currently has EMF arm part animation paused (so we only resume what we paused). */
    private static final Set<UUID> NTGL_EMF_ARM_PAUSE = ConcurrentHashMap.newKeySet();

    private static boolean initAttempted;
    private static boolean apiUsable;
    private static Method emfEntityOfEntity;
    private static Method pauseArmParts;
    /** Fallback if reflective access to {@code entitiesPausedParts} fails across EMF versions. */
    private static Method resumeAll;
    /** EMF's per-UUID arm-part pause map — cleared surgically so we do not wipe {@code entitiesPaused}. */
    private static Field entitiesPausedPartsField;

    private EmfAnimationCompat() {}

    private static void tryInit() {
        if (initAttempted) {
            return;
        }
        initAttempted = true;
        try {
            Class<?> api = Class.forName("traben.entity_model_features.EMFAnimationApi");
            Class<?> emfEntity = Class.forName("traben.entity_model_features.utils.EMFEntity");
            emfEntityOfEntity = api.getMethod("emfEntityOf", net.minecraft.world.entity.Entity.class);
            pauseArmParts = api.getMethod("pauseCustomAnimationsForThesePartsOfEntity", emfEntity, ModelPart[].class);
            resumeAll = api.getMethod("resumeAllCustomAnimationsForEntity", emfEntity);
            try {
                Class<?> ctx = Class.forName("traben.entity_model_features.models.animation.EMFAnimationEntityContext");
                entitiesPausedPartsField = ctx.getDeclaredField("entitiesPausedParts");
                entitiesPausedPartsField.setAccessible(true);
            } catch (Throwable ignored) {
                entitiesPausedPartsField = null;
            }
            apiUsable = true;
        } catch (Throwable ignored) {
            apiUsable = false;
        }
    }

    /**
     * Drops only NTGL's part-specific pause entry. If reflective access fails, falls back to
     * {@code resumeAllCustomAnimationsForEntity(emfEntity)} (requires caller's {@code emfEntity}).
     */
    private static void clearArmPartPauseOnly(UUID id, @Nullable Object emfEntityForFallback) {
        if (entitiesPausedPartsField != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<UUID, ?> map = (Map<UUID, ?>) entitiesPausedPartsField.get(null);
                if (map != null) {
                    map.remove(id);
                }
                return;
            } catch (Throwable ignored) {
                // fall through
            }
        }
        if (emfEntityForFallback != null) {
            try {
                resumeAll.invoke(null, emfEntityForFallback);
            } catch (Throwable ignored) {
                // give up
            }
        }
    }

    /**
     * When the entity holds a weapon in either hand, pauses EMF-driven math on the arm {@link ModelPart}
     * instances (same references as {@link WeaponPoseApplier#getArmPart}) so NTGL rotations survive each
     * part render. When neither hand holds a weapon, clears that pause if NTGL had set it.
     */
    public static void syncWeaponArmAnimationPause(HumanoidModel<?> model, LivingEntity entity) {
        tryInit();
        if (!apiUsable) {
            return;
        }
        boolean armed = isWeapon(entity.getMainHandItem()) || isWeapon(entity.getOffhandItem());
        UUID id = entity.getUUID();
        try {
            Object emfEntity = emfEntityOfEntity.invoke(null, entity);
            if (armed) {
                ModelPart right = WeaponPoseApplier.getArmPart(model, HumanoidArm.RIGHT);
                ModelPart left = WeaponPoseApplier.getArmPart(model, HumanoidArm.LEFT);
                pauseArmParts.invoke(null, emfEntity, (Object) new ModelPart[] {right, left});
                NTGL_EMF_ARM_PAUSE.add(id);
            } else if (NTGL_EMF_ARM_PAUSE.remove(id)) {
                clearArmPartPauseOnly(id, emfEntity);
            }
        } catch (Throwable ignored) {
            // EMF/ETF not fully wired for this entity; skip
        }
    }

    private static boolean isWeapon(ItemStack stack) {
        return stack.getItem() instanceof IWeapon;
    }
}

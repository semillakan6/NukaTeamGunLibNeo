package com.nukateam.ntgl.common.util.helpers.compatibility;

import einstein.subtle_effects.init.ModConfigs;
import einstein.subtle_effects.particle.emitter.SplashEmitter;
import einstein.subtle_effects.particle.option.SplashEmitterParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SubtleEffectsHelper {
    private static final ResourceLocation WATER_FLUID_DEF = ResourceLocation.withDefaultNamespace("water");
    private static final ResourceLocation LAVA_FLUID_DEF = ResourceLocation.withDefaultNamespace("lava");

    public static boolean doSplashEffect(Entity entity, Vec3 pos, boolean inLava) {
        var fluidId = inLava ? LAVA_FLUID_DEF : WATER_FLUID_DEF;

        if (!ModConfigs.ENTITIES.splashes.splashEffects.get()) {
            return false;
        }
        var splashEmitter = SplashEmitter.createForEntity(entity, fluidId, entity.getDeltaMovement().y);
        entity.level().addAlwaysVisibleParticle(splashEmitter, true,
                pos.x(), pos.y() + 0.01, pos.z(),
                0.0F, 0.0F, 0.0F);
        return true;
    }

    public static boolean doSplashEffect(Vec3 pos, float size, float speed, boolean isInLava) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) {
            return false;
        }
        var fluidId = isInLava ? LAVA_FLUID_DEF : WATER_FLUID_DEF;

        var ratio = isInLava ? 2f : 1f;

        if (!ModConfigs.ENTITIES.splashes.splashEffects.get()) {
            return false;
        }
        var splashEmitter = new SplashEmitterParticleOptions(fluidId, size, size * speed / ratio, -1.0F, -1);
        mc.level.addAlwaysVisibleParticle(splashEmitter, true,
                pos.x(), pos.y() + 0.01, pos.z(),
                0.0F, 0.0F, 0.0F);
        return true;
    }

    public static void doExplosionSplash(Level level, float radius, Vec3 position) {
        if (level.isClientSide && ModConfigs.ENTITIES.splashes.explosionsCauseSplashes.get()) {
            var pos = BlockPos.containing(position);
            var fluidState = level.getFluidState(pos);

            if (!fluidState.isEmpty()) {
                int blockY = pos.getY();

                for (int y = blockY; y < blockY + (radius) + 1; y++) {
                    var currentPos = pos.atY(y);
                    var currentFluidState = level.getFluidState(currentPos);

                    if (fluidState.getType().isSame(currentFluidState.getType())) {
                        continue;
                    }

                    if (level.getBlockState(currentPos).isSolidRender(level, currentPos)) {
                        return;
                    }

                    var fluidId = fluidState.is(FluidTags.WATER) ? WATER_FLUID_DEF
                            : fluidState.is(FluidTags.LAVA) ? LAVA_FLUID_DEF : null;

                    if (fluidId != null) {
                        var surfacePos = currentPos.below();
                        var surfaceFluidState = level.getFluidState(surfacePos);
                        var scale = radius - ((y - blockY) / radius);

                        level.addAlwaysVisibleParticle(new SplashEmitterParticleOptions(fluidId, scale, scale * (scale * 0.1F), -1.0F, -1),
                                true, position.x, surfacePos.getY() + surfaceFluidState.getHeight(level, surfacePos) + 0.01, position.z,
                                0, 0, 0
                        );
                    }
                    return;
                }
            }
        }
    }
}

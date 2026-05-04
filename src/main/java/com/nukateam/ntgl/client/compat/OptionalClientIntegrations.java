package com.nukateam.ntgl.client.compat;

import com.nukateam.ntgl.Ntgl;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Loads Controllable and Better Combat hooks via reflection so core client bootstrap
 * ({@link com.nukateam.ntgl.client.handlers.ClientHandler}) does not reference optional mods at class-load time.
 */
public final class OptionalClientIntegrations {
    private static final String CONTROLLER_HANDLER = "com.nukateam.ntgl.client.util.handler.ControllerHandler";
    private static final String GUN_BUTTON_BINDINGS = "com.nukateam.ntgl.client.input.GunButtonBindings";
    private static final String BETTER_COMBAT_HOOKS = "com.nukateam.ntgl.client.compat.BetterCombatClientHooks";

    private OptionalClientIntegrations() {}

    public static void register() {
        if (Ntgl.controllableLoaded) {
            registerControllable();
        }
        if (Ntgl.betterCombatLoaded) {
            registerBetterCombat();
        }
    }

    private static void registerControllable() {
        try {
            Class<?> handlerClass = Class.forName(CONTROLLER_HANDLER);
            Object handler = handlerClass.getDeclaredConstructor().newInstance();
            NeoForge.EVENT_BUS.register(handler);

            Class<?> bindingsClass = Class.forName(GUN_BUTTON_BINDINGS);
            bindingsClass.getMethod("register").invoke(null);
        } catch (ReflectiveOperationException e) {
            Ntgl.LOGGER.error("Failed to register Controllable integration", e);
        }
    }

    private static void registerBetterCombat() {
        try {
            Class<?> hooksClass = Class.forName(BETTER_COMBAT_HOOKS);
            hooksClass.getMethod("register").invoke(null);
        } catch (ReflectiveOperationException e) {
            Ntgl.LOGGER.error("Failed to register Better Combat integration", e);
        }
    }
}

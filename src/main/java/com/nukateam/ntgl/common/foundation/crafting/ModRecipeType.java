package com.nukateam.ntgl.common.foundation.crafting;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeType {
    public static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, Ntgl.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<WorkbenchRecipe>> WORKBENCH =
            REGISTER.register("workbench", () -> RecipeType.simple(Ntgl.ntglResource("workbench")));

    private ModRecipeType() {
    }
}

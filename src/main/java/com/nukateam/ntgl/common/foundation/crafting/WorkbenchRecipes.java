package com.nukateam.ntgl.common.foundation.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public final class WorkbenchRecipes {
    private WorkbenchRecipes() {
    }

    public static boolean isEmpty(Level world) {
        return world.getRecipeManager().getAllRecipesFor(ModRecipeType.WORKBENCH.get()).isEmpty();
    }

    public static List<RecipeHolder<WorkbenchRecipe>> getAll(Level world) {
        return world.getRecipeManager().getAllRecipesFor(ModRecipeType.WORKBENCH.get());
    }

    @Nullable
    public static WorkbenchRecipe getRecipeById(Level world, ResourceLocation id) {
        return world.getRecipeManager().getAllRecipesFor(ModRecipeType.WORKBENCH.get()).stream()
                .filter(h -> h.id().equals(id))
                .map(RecipeHolder::value)
                .findFirst()
                .orElse(null);
    }
}

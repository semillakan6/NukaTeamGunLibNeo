package com.nukateam.ntgl.common.foundation.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/**
 * Ingredient plus stack count for workbench assembly recipes.
 * JSON: {@code { "ingredient": { "item": "..." }, "count": 4 }}.
 */
public record WorkbenchMaterial(Ingredient ingredient, int count) {
    public static final Codec<WorkbenchMaterial> CODEC = RecordCodecBuilder.create(i -> i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(WorkbenchMaterial::ingredient),
            Codec.INT.optionalFieldOf("count", 1).forGetter(WorkbenchMaterial::count)
    ).apply(i, WorkbenchMaterial::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchMaterial> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            WorkbenchMaterial::ingredient,
            ByteBufCodecs.VAR_INT,
            WorkbenchMaterial::count,
            WorkbenchMaterial::new
    );

    public boolean has(Player player) {
        return com.nukateam.ntgl.common.util.util.InventoryUtil.hasWorkstationIngredient(player, this);
    }

    public void consume(Player player) {
        com.nukateam.ntgl.common.util.util.InventoryUtil.removeWorkstationIngredient(player, this);
    }

    public List<ItemStack> itemsForDisplay() {
        return List.of(ingredient.getItems());
    }
}

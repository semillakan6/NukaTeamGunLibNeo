package com.nukateam.ntgl.common.foundation.crafting;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;

public class WorkbenchRecipeSerializer implements RecipeSerializer<WorkbenchRecipe> {
    public static final MapCodec<WorkbenchRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            WorkbenchMaterial.CODEC.listOf().fieldOf("materials").forGetter(r -> r.materials().stream().toList()),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(WorkbenchRecipe::result)
    ).apply(inst, (mats, stack) -> new WorkbenchRecipe(stack, ImmutableList.copyOf(mats))));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, WorkbenchMaterial.STREAM_CODEC),
            r -> new ArrayList<>(r.materials()),
            ItemStack.STREAM_CODEC,
            WorkbenchRecipe::result,
            (mats, stack) -> new WorkbenchRecipe(stack, ImmutableList.copyOf(mats))
    );

    @Override
    public MapCodec<WorkbenchRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, WorkbenchRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}

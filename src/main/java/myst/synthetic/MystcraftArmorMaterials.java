package myst.synthetic;

import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.core.registries.Registries;

public interface MystcraftArmorMaterials {

    ResourceKey<EquipmentAsset> GLASSES_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "glasses")
    );

    TagKey<Item> REPAIRS_GLASSES = TagKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath("mystcraft-sc", "repairs_glasses")
    );

    ArmorMaterial GLASSES = new ArmorMaterial(
            7,
            Map.of(
                    ArmorType.BOOTS, 1,
                    ArmorType.LEGGINGS, 3,
                    ArmorType.CHESTPLATE, 5,
                    ArmorType.HELMET, 2,
                    ArmorType.BODY, 7
            ),
            25,
            SoundEvents.ARMOR_EQUIP_GOLD,
            0.0F,
            0.0F,
            REPAIRS_GLASSES,
            GLASSES_ASSET
    );
}
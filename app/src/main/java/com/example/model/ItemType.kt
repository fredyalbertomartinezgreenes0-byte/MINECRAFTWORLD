package com.example.model

enum class ItemCategory {
    BLOCK,
    TOOL,
    WEAPON,
    ARMOR,
    FOOD,
    MATERIAL,
    SPECIAL
}

enum class ToolType {
    NONE,
    PICKAXE,
    AXE,
    SHOVEL,
    SWORD
}

enum class ArmorSlot {
    NONE,
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS
}

data class ItemDef(
    val id: String,
    val name: String,
    val category: ItemCategory,
    val associatedBlockId: Int? = null,
    val maxStack: Int = 64,
    val attackDamage: Float = 1.0f,
    val miningSpeedMultiplier: Float = 1.0f,
    val toolType: ToolType = ToolType.NONE,
    val armorSlot: ArmorSlot = ArmorSlot.NONE,
    val armorDefense: Int = 0,
    val foodValue: Int = 0,
    val maxDurability: Int = 0,
    val iconColor: Long = 0xFFCCCCCC,
    val description: String = ""
)

object ItemRegistry {
    private val items = mutableMapOf<String, ItemDef>()

    val AIR = register(
        ItemDef(
            id = "air",
            name = "Vacío",
            category = ItemCategory.MATERIAL,
            maxStack = 0
        )
    )

    // Blocks as items
    val DIRT = register(ItemDef("dirt", "Tierra", ItemCategory.BLOCK, associatedBlockId = BlockType.DIRT.id, iconColor = 0xFF6D4C33))
    val COBBLESTONE = register(ItemDef("cobblestone", "Adoquín", ItemCategory.BLOCK, associatedBlockId = BlockType.COBBLESTONE.id, iconColor = 0xFF63676B))
    val OAK_LOG = register(ItemDef("oak_log", "Tronco de Roble", ItemCategory.BLOCK, associatedBlockId = BlockType.OAK_LOG.id, iconColor = 0xFF5E4225))
    val OAK_PLANKS = register(ItemDef("oak_planks", "Tablones de Madera", ItemCategory.BLOCK, associatedBlockId = BlockType.OAK_PLANKS.id, iconColor = 0xFFA57D4D))
    val SAND = register(ItemDef("sand", "Arena", ItemCategory.BLOCK, associatedBlockId = BlockType.SAND.id, iconColor = 0xFFD8CB94))
    val GLASS = register(ItemDef("glass", "Vidrio", ItemCategory.BLOCK, associatedBlockId = BlockType.GLASS.id, iconColor = 0xAAAFCBDF))
    val BRICK = register(ItemDef("brick", "Ladrillos", ItemCategory.BLOCK, associatedBlockId = BlockType.BRICK.id, iconColor = 0xFF8A4233))
    val TORCH = register(ItemDef("torch", "Antorcha", ItemCategory.BLOCK, associatedBlockId = BlockType.TORCH.id, iconColor = 0xFFFFD54F, description = "Ilumina áreas oscuras"))
    val CRAFTING_TABLE = register(ItemDef("crafting_table", "Mesa de Fabricación", ItemCategory.BLOCK, associatedBlockId = BlockType.CRAFTING_TABLE.id, iconColor = 0xFF7B532E))
    val FURNACE = register(ItemDef("furnace", "Horno", ItemCategory.BLOCK, associatedBlockId = BlockType.FURNACE.id, iconColor = 0xFF525557))
    val CHEST = register(ItemDef("chest", "Cofre", ItemCategory.BLOCK, associatedBlockId = BlockType.CHEST.id, iconColor = 0xFF8F6732))
    val CACTUS = register(ItemDef("cactus", "Cactus", ItemCategory.BLOCK, associatedBlockId = BlockType.CACTUS.id, iconColor = 0xFF356B23))
    val SNOWBALL = register(ItemDef("snowball", "Bola de Nieve", ItemCategory.MATERIAL, iconColor = 0xFFE8EEF5))

    // End Update / Voidlands Blocks as items
    val VOIDSTONE = register(ItemDef("voidstone", "Piedra del Vacío", ItemCategory.BLOCK, associatedBlockId = BlockType.VOIDSTONE.id, iconColor = 0xFF2D1F47))
    val VOID_CRYSTAL = register(ItemDef("void_crystal_block", "Cristal del Vacío", ItemCategory.BLOCK, associatedBlockId = BlockType.VOID_CRYSTAL.id, iconColor = 0xFFD44BFF))
    val VOID_OBSIDIAN = register(ItemDef("void_obsidian", "Obsidiana Cósmica", ItemCategory.BLOCK, associatedBlockId = BlockType.VOID_OBSIDIAN.id, iconColor = 0xFF1B142E, description = "Marco para el Portal del Vacío"))
    val VOID_BRICK = register(ItemDef("void_brick", "Ladrillo del Vacío", ItemCategory.BLOCK, associatedBlockId = BlockType.VOID_BRICK.id, iconColor = 0xFF3E285C))

    // Basic Materials
    val STICK = register(ItemDef("stick", "Palo", ItemCategory.MATERIAL, iconColor = 0xFF8D6E63))
    val COAL = register(ItemDef("coal", "Carbón", ItemCategory.MATERIAL, iconColor = 0xFF212121, description = "Combustible para el horno"))
    val RAW_IRON = register(ItemDef("raw_iron", "Hierro Bruto", ItemCategory.MATERIAL, iconColor = 0xFF8B776D))
    val IRON_INGOT = register(ItemDef("iron_ingot", "Lingote de Hierro", ItemCategory.MATERIAL, iconColor = 0xFFD0D7DE))
    val RAW_GOLD = register(ItemDef("raw_gold", "Oro Bruto", ItemCategory.MATERIAL, iconColor = 0xFFA9974B))
    val GOLD_INGOT = register(ItemDef("gold_ingot", "Lingote de Oro", ItemCategory.MATERIAL, iconColor = 0xFFFFD700))
    val DIAMOND = register(ItemDef("diamond", "Diamante", ItemCategory.MATERIAL, iconColor = 0xFF4DD0E1, description = "Gema brillante y resistente"))

    // Voidlands Materials & Special Items
    val VOID_SHARD = register(ItemDef("void_shard", "Fragmento del Vacío", ItemCategory.MATERIAL, iconColor = 0xFFE040FB, description = "Poder arcano del espacio"))
    val VOID_ORE_RAW = register(ItemDef("void_ore_raw", "Mineral del Vacío Bruto", ItemCategory.MATERIAL, iconColor = 0xFF5B3482))
    val VOID_INGOT = register(ItemDef("void_ingot", "Lingote del Vacío", ItemCategory.MATERIAL, iconColor = 0xFF7C4DFF, description = "Metal supremo dimensional"))
    val VOID_CORE_SPARK = register(ItemDef("void_core_spark", "Chispa del Vacío", ItemCategory.SPECIAL, iconColor = 0xFFB388FF, maxStack = 1, description = "Activa el Portal a The Voidlands"))
    val VOID_CROWN = register(ItemDef("void_crown", "Corona del Vacío", ItemCategory.ARMOR, armorSlot = ArmorSlot.HELMET, armorDefense = 5, maxDurability = 800, iconColor = 0xFFFFD700, description = "Trofeo del Void King"))
    val VOID_ELYTRA = register(ItemDef("void_elytra", "Alas del Vacío", ItemCategory.ARMOR, armorSlot = ArmorSlot.CHESTPLATE, armorDefense = 4, maxDurability = 900, iconColor = 0xFF7C4DFF, description = "Planea por los abismos"))

    // Tools - Wood
    val WOODEN_PICKAXE = register(ItemDef("wooden_pickaxe", "Pico de Madera", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.PICKAXE, miningSpeedMultiplier = 2f, maxDurability = 60, iconColor = 0xFF8D6E63))
    val WOODEN_AXE = register(ItemDef("wooden_axe", "Hacha de Madera", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.AXE, miningSpeedMultiplier = 2f, maxDurability = 60, iconColor = 0xFF8D6E63))
    val WOODEN_SHOVEL = register(ItemDef("wooden_shovel", "Pala de Madera", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.SHOVEL, miningSpeedMultiplier = 2f, maxDurability = 60, iconColor = 0xFF8D6E63))
    val WOODEN_SWORD = register(ItemDef("wooden_sword", "Espada de Madera", ItemCategory.WEAPON, maxStack = 1, toolType = ToolType.SWORD, attackDamage = 4f, maxDurability = 60, iconColor = 0xFF8D6E63))

    // Tools - Stone
    val STONE_PICKAXE = register(ItemDef("stone_pickaxe", "Pico de Piedra", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.PICKAXE, miningSpeedMultiplier = 4f, maxDurability = 132, iconColor = 0xFF78909C))
    val STONE_AXE = register(ItemDef("stone_axe", "Hacha de Piedra", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.AXE, miningSpeedMultiplier = 4f, maxDurability = 132, iconColor = 0xFF78909C))
    val STONE_SHOVEL = register(ItemDef("stone_shovel", "Pala de Piedra", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.SHOVEL, miningSpeedMultiplier = 4f, maxDurability = 132, iconColor = 0xFF78909C))
    val STONE_SWORD = register(ItemDef("stone_sword", "Espada de Piedra", ItemCategory.WEAPON, maxStack = 1, toolType = ToolType.SWORD, attackDamage = 5f, maxDurability = 132, iconColor = 0xFF78909C))

    // Tools - Iron
    val IRON_PICKAXE = register(ItemDef("iron_pickaxe", "Pico de Hierro", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.PICKAXE, miningSpeedMultiplier = 6f, maxDurability = 250, iconColor = 0xFFECEFF1))
    val IRON_AXE = register(ItemDef("iron_axe", "Hacha de Hierro", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.AXE, miningSpeedMultiplier = 6f, maxDurability = 250, iconColor = 0xFFECEFF1))
    val IRON_SHOVEL = register(ItemDef("iron_shovel", "Pala de Hierro", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.SHOVEL, miningSpeedMultiplier = 6f, maxDurability = 250, iconColor = 0xFFECEFF1))
    val IRON_SWORD = register(ItemDef("iron_sword", "Espada de Hierro", ItemCategory.WEAPON, maxStack = 1, toolType = ToolType.SWORD, attackDamage = 6f, maxDurability = 250, iconColor = 0xFFECEFF1))

    // Tools - Diamond
    val DIAMOND_PICKAXE = register(ItemDef("diamond_pickaxe", "Pico de Diamante", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.PICKAXE, miningSpeedMultiplier = 8f, maxDurability = 1561, iconColor = 0xFF4DD0E1))
    val DIAMOND_AXE = register(ItemDef("diamond_axe", "Hacha de Diamante", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.AXE, miningSpeedMultiplier = 8f, maxDurability = 1561, iconColor = 0xFF4DD0E1))
    val DIAMOND_SHOVEL = register(ItemDef("diamond_shovel", "Pala de Diamante", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.SHOVEL, miningSpeedMultiplier = 8f, maxDurability = 1561, iconColor = 0xFF4DD0E1))
    val DIAMOND_SWORD = register(ItemDef("diamond_sword", "Espada de Diamante", ItemCategory.WEAPON, maxStack = 1, toolType = ToolType.SWORD, attackDamage = 7f, maxDurability = 1561, iconColor = 0xFF4DD0E1))

    // Tools - Void (End Update Tier)
    val VOID_BLADE = register(ItemDef("void_blade", "Espada del Vacío", ItemCategory.WEAPON, maxStack = 1, toolType = ToolType.SWORD, attackDamage = 10f, maxDurability = 2500, iconColor = 0xFFB388FF, description = "Corta a través de la realidad"))
    val VOID_PICKAXE = register(ItemDef("void_pickaxe", "Pico del Vacío", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.PICKAXE, miningSpeedMultiplier = 12f, maxDurability = 2500, iconColor = 0xFFB388FF, description = "Desintegra cualquier mineral"))
    val VOID_AXE = register(ItemDef("void_axe", "Hacha del Vacío", ItemCategory.TOOL, maxStack = 1, toolType = ToolType.AXE, miningSpeedMultiplier = 12f, maxDurability = 2500, iconColor = 0xFFB388FF))

    // Armor
    val IRON_HELMET = register(ItemDef("iron_helmet", "Casco de Hierro", ItemCategory.ARMOR, armorSlot = ArmorSlot.HELMET, armorDefense = 2, maxDurability = 165, iconColor = 0xFFCFD8DC))
    val IRON_CHESTPLATE = register(ItemDef("iron_chestplate", "Peto de Hierro", ItemCategory.ARMOR, armorSlot = ArmorSlot.CHESTPLATE, armorDefense = 6, maxDurability = 240, iconColor = 0xFFCFD8DC))
    val IRON_LEGGINGS = register(ItemDef("iron_leggings", "Grebas de Hierro", ItemCategory.ARMOR, armorSlot = ArmorSlot.LEGGINGS, armorDefense = 5, maxDurability = 225, iconColor = 0xFFCFD8DC))
    val IRON_BOOTS = register(ItemDef("iron_boots", "Botas de Hierro", ItemCategory.ARMOR, armorSlot = ArmorSlot.BOOTS, armorDefense = 2, maxDurability = 195, iconColor = 0xFFCFD8DC))

    val DIAMOND_HELMET = register(ItemDef("diamond_helmet", "Casco de Diamante", ItemCategory.ARMOR, armorSlot = ArmorSlot.HELMET, armorDefense = 3, maxDurability = 363, iconColor = 0xFF80DEEA))
    val DIAMOND_CHESTPLATE = register(ItemDef("diamond_chestplate", "Peto de Diamante", ItemCategory.ARMOR, armorSlot = ArmorSlot.CHESTPLATE, armorDefense = 8, maxDurability = 528, iconColor = 0xFF80DEEA))
    val DIAMOND_LEGGINGS = register(ItemDef("diamond_leggings", "Grebas de Diamante", ItemCategory.ARMOR, armorSlot = ArmorSlot.LEGGINGS, armorDefense = 6, maxDurability = 495, iconColor = 0xFF80DEEA))
    val DIAMOND_BOOTS = register(ItemDef("diamond_boots", "Botas de Diamante", ItemCategory.ARMOR, armorSlot = ArmorSlot.BOOTS, armorDefense = 3, maxDurability = 429, iconColor = 0xFF80DEEA))

    // Food
    val APPLE = register(ItemDef("apple", "Manzana", ItemCategory.FOOD, foodValue = 4, iconColor = 0xFFE53935))
    val RAW_PORK = register(ItemDef("raw_pork", "Carne Cruda", ItemCategory.FOOD, foodValue = 3, iconColor = 0xFFF48FB1))
    val COOKED_PORK = register(ItemDef("cooked_pork", "Carne Cocinada", ItemCategory.FOOD, foodValue = 8, iconColor = 0xFF8D6E63))
    val BREAD = register(ItemDef("bread", "Pan", ItemCategory.FOOD, foodValue = 5, iconColor = 0xFFFFB74D))

    private fun register(item: ItemDef): ItemDef {
        items[item.id] = item
        return item
    }

    fun get(id: String): ItemDef = items[id] ?: AIR
    fun getAll(): List<ItemDef> = items.values.toList()
}

data class ItemStack(
    val itemId: String,
    val count: Int = 1,
    val durability: Int = 0
) {
    val def: ItemDef get() = ItemRegistry.get(itemId)
    val isEmpty: Boolean get() = itemId == "air" || count <= 0

    companion object {
        val EMPTY = ItemStack("air", 0, 0)
    }
}

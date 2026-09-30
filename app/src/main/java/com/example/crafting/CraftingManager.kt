package com.example.crafting

import com.example.model.ItemStack

data class CraftingRecipe(
    val id: String,
    val name: String,
    val output: ItemStack,
    val gridWidth: Int, // 1, 2, or 3
    val gridHeight: Int,
    val pattern: List<String>, // itemIds (length = width * height, "air" for empty)
    val isShapeless: Boolean = false,
    val requiredItems: Map<String, Int> = emptyMap() // For shapeless
)

data class SmeltingRecipe(
    val inputItemId: String,
    val output: ItemStack,
    val cookTimeSeconds: Float = 5.0f
)

object CraftingManager {
    val recipes = mutableListOf<CraftingRecipe>()
    val smeltingRecipes = mutableListOf<SmeltingRecipe>()

    init {
        // --- BASIC SHAPELESS RECIPES ---
        // Wood log -> 4 Planks
        recipes.add(
            CraftingRecipe(
                id = "planks_from_log",
                name = "Tablones de Roble",
                output = ItemStack("oak_planks", 4),
                gridWidth = 1,
                gridHeight = 1,
                pattern = listOf("oak_log")
            )
        )

        // 2 Planks -> 4 Sticks
        recipes.add(
            CraftingRecipe(
                id = "sticks",
                name = "Palos",
                output = ItemStack("stick", 4),
                gridWidth = 1,
                gridHeight = 2,
                pattern = listOf("oak_planks", "oak_planks")
            )
        )

        // 1 Coal + 1 Stick -> 4 Torches
        recipes.add(
            CraftingRecipe(
                id = "torch",
                name = "Antorchas",
                output = ItemStack("torch", 4),
                gridWidth = 1,
                gridHeight = 2,
                pattern = listOf("coal", "stick")
            )
        )

        // 4 Planks -> Crafting Table
        recipes.add(
            CraftingRecipe(
                id = "crafting_table",
                name = "Mesa de Fabricación",
                output = ItemStack("crafting_table", 1),
                gridWidth = 2,
                gridHeight = 2,
                pattern = listOf(
                    "oak_planks", "oak_planks",
                    "oak_planks", "oak_planks"
                )
            )
        )

        // 8 Planks -> Chest
        recipes.add(
            CraftingRecipe(
                id = "chest",
                name = "Cofre",
                output = ItemStack("chest", 1),
                gridWidth = 3,
                gridHeight = 3,
                pattern = listOf(
                    "oak_planks", "oak_planks", "oak_planks",
                    "oak_planks", "air", "oak_planks",
                    "oak_planks", "oak_planks", "oak_planks"
                )
            )
        )

        // 8 Cobblestone -> Furnace
        recipes.add(
            CraftingRecipe(
                id = "furnace",
                name = "Horno",
                output = ItemStack("furnace", 1),
                gridWidth = 3,
                gridHeight = 3,
                pattern = listOf(
                    "cobblestone", "cobblestone", "cobblestone",
                    "cobblestone", "air", "cobblestone",
                    "cobblestone", "cobblestone", "cobblestone"
                )
            )
        )

        // --- WOODEN TOOLS ---
        recipes.add(
            CraftingRecipe(
                id = "wooden_pickaxe",
                name = "Pico de Madera",
                output = ItemStack("wooden_pickaxe", 1),
                gridWidth = 3,
                gridHeight = 3,
                pattern = listOf(
                    "oak_planks", "oak_planks", "oak_planks",
                    "air", "stick", "air",
                    "air", "stick", "air"
                )
            )
        )
        recipes.add(
            CraftingRecipe(
                id = "wooden_sword",
                name = "Espada de Madera",
                output = ItemStack("wooden_sword", 1),
                gridWidth = 1,
                gridHeight = 3,
                pattern = listOf("oak_planks", "oak_planks", "stick")
            )
        )
        recipes.add(
            CraftingRecipe(
                id = "wooden_axe",
                name = "Hacha de Madera",
                output = ItemStack("wooden_axe", 1),
                gridWidth = 2,
                gridHeight = 3,
                pattern = listOf(
                    "oak_planks", "oak_planks",
                    "oak_planks", "stick",
                    "air", "stick"
                )
            )
        )

        // --- STONE TOOLS ---
        recipes.add(
            CraftingRecipe(
                id = "stone_pickaxe",
                name = "Pico de Piedra",
                output = ItemStack("stone_pickaxe", 1),
                gridWidth = 3,
                gridHeight = 3,
                pattern = listOf(
                    "cobblestone", "cobblestone", "cobblestone",
                    "air", "stick", "air",
                    "air", "stick", "air"
                )
            )
        )
        recipes.add(
            CraftingRecipe(
                id = "stone_sword",
                name = "Espada de Piedra",
                output = ItemStack("stone_sword", 1),
                gridWidth = 1,
                gridHeight = 3,
                pattern = listOf("cobblestone", "cobblestone", "stick")
            )
        )

        // --- IRON TOOLS ---
        recipes.add(
            CraftingRecipe(
                id = "iron_pickaxe",
                name = "Pico de Hierro",
                output = ItemStack("iron_pickaxe", 1),
                gridWidth = 3,
                gridHeight = 3,
                pattern = listOf(
                    "iron_ingot", "iron_ingot", "iron_ingot",
                    "air", "stick", "air",
                    "air", "stick", "air"
                )
            )
        )
        recipes.add(
            CraftingRecipe(
                id = "iron_sword",
                name = "Espada de Hierro",
                output = ItemStack("iron_sword", 1),
                gridWidth = 1,
                gridHeight = 3,
                pattern = listOf("iron_ingot", "iron_ingot", "stick")
            )
        )
        recipes.add(
            CraftingRecipe(
                id = "iron_chestplate",
                name = "Peto de Hierro",
                output = ItemStack("iron_chestplate", 1),
                gridWidth = 3,
                gridHeight = 3,
                pattern = listOf(
                    "iron_ingot", "air", "iron_ingot",
                    "iron_ingot", "iron_ingot", "iron_ingot",
                    "iron_ingot", "iron_ingot", "iron_ingot"
                )
            )
        )

        // --- DIAMOND TOOLS ---
        recipes.add(
            CraftingRecipe(
                id = "diamond_pickaxe",
                name = "Pico de Diamante",
                output = ItemStack("diamond_pickaxe", 1),
                gridWidth = 3,
                gridHeight = 3,
                pattern = listOf(
                    "diamond", "diamond", "diamond",
                    "air", "stick", "air",
                    "air", "stick", "air"
                )
            )
        )
        recipes.add(
            CraftingRecipe(
                id = "diamond_sword",
                name = "Espada de Diamante",
                output = ItemStack("diamond_sword", 1),
                gridWidth = 1,
                gridHeight = 3,
                pattern = listOf("diamond", "diamond", "stick")
            )
        )

        // --- THE VOIDLANDS (END UPDATE) RECIPES ---
        // 4 Voidstone + 4 Void Shards -> 4 Void Obsidian
        recipes.add(
            CraftingRecipe(
                id = "void_obsidian",
                name = "Obsidiana Cósmica",
                output = ItemStack("void_obsidian", 4),
                gridWidth = 3,
                gridHeight = 3,
                pattern = listOf(
                    "voidstone", "void_shard", "voidstone",
                    "void_shard", "diamond", "void_shard",
                    "voidstone", "void_shard", "voidstone"
                )
            )
        )

        // Void Spark (activator for Void Portal)
        recipes.add(
            CraftingRecipe(
                id = "void_core_spark",
                name = "Chispa del Vacío",
                output = ItemStack("void_core_spark", 1),
                gridWidth = 2,
                gridHeight = 2,
                pattern = listOf(
                    "void_shard", "gold_ingot",
                    "iron_ingot", "coal"
                )
            )
        )

        // Void Blade
        recipes.add(
            CraftingRecipe(
                id = "void_blade",
                name = "Espada del Vacío",
                output = ItemStack("void_blade", 1),
                gridWidth = 1,
                gridHeight = 3,
                pattern = listOf("void_ingot", "void_ingot", "stick")
            )
        )

        // Void Pickaxe
        recipes.add(
            CraftingRecipe(
                id = "void_pickaxe",
                name = "Pico del Vacío",
                output = ItemStack("void_pickaxe", 1),
                gridWidth = 3,
                gridHeight = 3,
                pattern = listOf(
                    "void_ingot", "void_ingot", "void_ingot",
                    "air", "stick", "air",
                    "air", "stick", "air"
                )
            )
        )

        // --- SMELTING RECIPES ---
        smeltingRecipes.add(SmeltingRecipe("raw_iron", ItemStack("iron_ingot", 1)))
        smeltingRecipes.add(SmeltingRecipe("raw_gold", ItemStack("gold_ingot", 1)))
        smeltingRecipes.add(SmeltingRecipe("void_ore_raw", ItemStack("void_ingot", 1)))
        smeltingRecipes.add(SmeltingRecipe("raw_pork", ItemStack("cooked_pork", 1)))
        smeltingRecipes.add(SmeltingRecipe("sand", ItemStack("glass", 1)))
        smeltingRecipes.add(SmeltingRecipe("cobblestone", ItemStack("stone", 1)))
    }

    fun findRecipe(grid: List<String>, width: Int, height: Int): ItemStack? {
        for (recipe in recipes) {
            if (matches(grid, width, height, recipe)) {
                return recipe.output.copy()
            }
        }
        return null
    }

    private fun matches(grid: List<String>, gWidth: Int, gHeight: Int, recipe: CraftingRecipe): Boolean {
        if (recipe.gridWidth > gWidth || recipe.gridHeight > gHeight) return false

        // Check every possible top-left offset in grid
        for (offsetX in 0..(gWidth - recipe.gridWidth)) {
            for (offsetY in 0..(gHeight - recipe.gridHeight)) {
                var match = true
                for (gy in 0 until gHeight) {
                    for (gx in 0 until gWidth) {
                        val gridItem = grid[gy * gWidth + gx]
                        val rx = gx - offsetX
                        val ry = gy - offsetY
                        val expectedItem = if (rx in 0 until recipe.gridWidth && ry in 0 until recipe.gridHeight) {
                            recipe.pattern[ry * recipe.gridWidth + rx]
                        } else {
                            "air"
                        }
                        if (gridItem != expectedItem) {
                            match = false
                            break
                        }
                    }
                    if (!match) break
                }
                if (match) return true
            }
        }
        return false
    }

    fun getSmeltResult(inputItemId: String): ItemStack? {
        return smeltingRecipes.find { it.inputItemId == inputItemId }?.output?.copy()
    }
}

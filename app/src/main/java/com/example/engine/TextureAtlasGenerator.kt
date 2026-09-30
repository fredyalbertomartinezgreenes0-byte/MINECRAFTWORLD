package com.example.engine

import android.graphics.Bitmap
import android.graphics.Color
import java.util.Random

enum class TexturePack(val displayName: String, val description: String) {
    CLASSIC("Minecraft Clásico", "Texturas pixeladas 16x16 auténticas y fieles."),
    VOIDLANDS_COSMIC("The Voidlands Cósmico", "Estilo oscuro espacial con cristales de neón y estrellas."),
    BARE_BONES("Minimalista / Bare Bones", "Colores planos vibrantes y estética limpia."),
    MEDIEVAL_RPG("Fantasía Medieval", "Piedras rústicas, maderas oscuras y detalles antiguos.")
}

object TextureAtlasGenerator {
    const val ATLAS_SIZE = 256
    const val TILE_SIZE = 16
    const val TILES_PER_ROW = ATLAS_SIZE / TILE_SIZE // 16 tiles per row

    // Tile indices in atlas
    const val TILE_GRASS_TOP = 0
    const val TILE_GRASS_SIDE = 1
    const val TILE_DIRT = 2
    const val TILE_STONE = 3
    const val TILE_COBBLESTONE = 4
    const val TILE_OAK_LOG_SIDE = 5
    const val TILE_OAK_LOG_TOP = 6
    const val TILE_OAK_PLANKS = 7
    const val TILE_OAK_LEAVES = 8
    const val TILE_SAND = 9
    const val TILE_WATER = 10
    const val TILE_GLASS = 11
    const val TILE_COAL_ORE = 12
    const val TILE_IRON_ORE = 13
    const val TILE_GOLD_ORE = 14
    const val TILE_DIAMOND_ORE = 15

    const val TILE_BRICK = 16
    const val TILE_TORCH = 17
    const val TILE_CRAFTING_TOP = 18
    const val TILE_CRAFTING_SIDE = 19
    const val TILE_FURNACE_FRONT = 20
    const val TILE_FURNACE_SIDE = 21
    const val TILE_CHEST_TOP = 22
    const val TILE_CHEST_SIDE = 23
    const val TILE_CACTUS_TOP = 24
    const val TILE_CACTUS_SIDE = 25
    const val TILE_BEDROCK = 26
    const val TILE_SNOW = 27

    // The Voidlands Exclusive Tiles
    const val TILE_VOIDSTONE = 28
    const val TILE_VOID_CRYSTAL = 29
    const val TILE_VOID_ORE = 30
    const val TILE_VOID_OBSIDIAN = 31
    const val TILE_VOID_PORTAL = 32
    const val TILE_VOID_BRICK = 33

    var currentPack: TexturePack = TexturePack.CLASSIC

    fun generateAtlas(pack: TexturePack = currentPack): Bitmap {
        currentPack = pack
        val bitmap = Bitmap.createBitmap(ATLAS_SIZE, ATLAS_SIZE, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(ATLAS_SIZE * ATLAS_SIZE)

        // Generate each tile
        drawGrassTop(pixels, TILE_GRASS_TOP, pack)
        drawGrassSide(pixels, TILE_GRASS_SIDE, pack)
        drawDirt(pixels, TILE_DIRT, pack)
        drawStone(pixels, TILE_STONE, pack)
        drawCobblestone(pixels, TILE_COBBLESTONE, pack)
        drawLogSide(pixels, TILE_OAK_LOG_SIDE, pack)
        drawLogTop(pixels, TILE_OAK_LOG_TOP, pack)
        drawPlanks(pixels, TILE_OAK_PLANKS, pack)
        drawLeaves(pixels, TILE_OAK_LEAVES, pack)
        drawSand(pixels, TILE_SAND, pack)
        drawWater(pixels, TILE_WATER, pack)
        drawGlass(pixels, TILE_GLASS, pack)
        drawOre(pixels, TILE_COAL_ORE, Color.rgb(35, 35, 35), pack)
        drawOre(pixels, TILE_IRON_ORE, Color.rgb(215, 180, 155), pack)
        drawOre(pixels, TILE_GOLD_ORE, Color.rgb(255, 215, 0), pack)
        drawOre(pixels, TILE_DIAMOND_ORE, Color.rgb(77, 208, 225), pack)

        drawBrick(pixels, TILE_BRICK, pack)
        drawTorch(pixels, TILE_TORCH, pack)
        drawCraftingTop(pixels, TILE_CRAFTING_TOP, pack)
        drawCraftingSide(pixels, TILE_CRAFTING_SIDE, pack)
        drawFurnaceFront(pixels, TILE_FURNACE_FRONT, pack)
        drawFurnaceSide(pixels, TILE_FURNACE_SIDE, pack)
        drawChestTop(pixels, TILE_CHEST_TOP, pack)
        drawChestSide(pixels, TILE_CHEST_SIDE, pack)
        drawCactus(pixels, TILE_CACTUS_TOP, TILE_CACTUS_SIDE, pack)
        drawBedrock(pixels, TILE_BEDROCK, pack)
        drawSnow(pixels, TILE_SNOW, pack)

        // Voidlands
        drawVoidstone(pixels, TILE_VOIDSTONE, pack)
        drawVoidCrystal(pixels, TILE_VOID_CRYSTAL, pack)
        drawVoidOre(pixels, TILE_VOID_ORE, pack)
        drawVoidObsidian(pixels, TILE_VOID_OBSIDIAN, pack)
        drawVoidPortal(pixels, TILE_VOID_PORTAL, pack)
        drawVoidBrick(pixels, TILE_VOID_BRICK, pack)

        bitmap.setPixels(pixels, 0, ATLAS_SIZE, 0, 0, ATLAS_SIZE, ATLAS_SIZE)
        return bitmap
    }

    private fun setTilePixel(pixels: IntArray, tileId: Int, localX: Int, localY: Int, color: Int) {
        val tileCol = tileId % TILES_PER_ROW
        val tileRow = tileId / TILES_PER_ROW
        val globalX = tileCol * TILE_SIZE + localX
        val globalY = tileRow * TILE_SIZE + localY
        if (globalX in 0 until ATLAS_SIZE && globalY in 0 until ATLAS_SIZE) {
            pixels[globalY * ATLAS_SIZE + globalX] = color
        }
    }

    // --- PROCEDURAL 16x16 TEXTURE GENERATION ---

    private fun drawGrassTop(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(42)
        val baseR = if (pack == TexturePack.VOIDLANDS_COSMIC) 70 else 88
        val baseG = if (pack == TexturePack.VOIDLANDS_COSMIC) 35 else 160
        val baseB = if (pack == TexturePack.VOIDLANDS_COSMIC) 140 else 45

        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val variance = if (pack == TexturePack.BARE_BONES) 0 else rand.nextInt(26) - 13
                val r = (baseR + variance).coerceIn(0, 255)
                val g = (baseG + variance).coerceIn(0, 255)
                val b = (baseB + variance).coerceIn(0, 255)
                setTilePixel(pixels, tileId, x, y, Color.rgb(r, g, b))
            }
        }
    }

    private fun drawGrassSide(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(43)
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val isGrassFringe = (y < 3) || (y == 3 && (x % 3 != 0)) || (y == 4 && (x % 5 == 0))
                if (isGrassFringe) {
                    val variance = if (pack == TexturePack.BARE_BONES) 0 else rand.nextInt(20) - 10
                    setTilePixel(pixels, tileId, x, y, Color.rgb(85 + variance, 155 + variance, 42 + variance))
                } else {
                    val variance = if (pack == TexturePack.BARE_BONES) 0 else rand.nextInt(20) - 10
                    setTilePixel(pixels, tileId, x, y, Color.rgb(108 + variance, 75 + variance, 50 + variance))
                }
            }
        }
    }

    private fun drawDirt(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(44)
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val variance = if (pack == TexturePack.BARE_BONES) 0 else rand.nextInt(28) - 14
                val isPebble = !isBareBones(pack) && (rand.nextInt(18) == 0)
                if (isPebble) {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(135, 105, 78))
                } else {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(108 + variance, 75 + variance, 50 + variance))
                }
            }
        }
    }

    private fun drawStone(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(45)
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val variance = if (pack == TexturePack.BARE_BONES) 0 else rand.nextInt(24) - 12
                val c = (125 + variance).coerceIn(0, 255)
                setTilePixel(pixels, tileId, x, y, Color.rgb(c, c, c))
            }
        }
    }

    private fun drawCobblestone(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(46)
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                // Rock mortar outline
                val isCrack = (x % 5 == 0 && y % 4 == 0) || (x == 3 && y in 2..6) || (x == 11 && y in 8..13)
                if (isCrack && !isBareBones(pack)) {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(65, 65, 65))
                } else {
                    val variance = rand.nextInt(30) - 15
                    val c = (110 + variance).coerceIn(0, 255)
                    setTilePixel(pixels, tileId, x, y, Color.rgb(c, c, c))
                }
            }
        }
    }

    private fun drawLogSide(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(47)
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val isBarkLine = (x % 4 == 0)
                if (isBarkLine) {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(75, 52, 30))
                } else {
                    val variance = if (pack == TexturePack.BARE_BONES) 0 else rand.nextInt(16) - 8
                    setTilePixel(pixels, tileId, x, y, Color.rgb(102 + variance, 72 + variance, 42 + variance))
                }
            }
        }
    }

    private fun drawLogTop(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(48)
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val dx = x - 7.5
                val dy = y - 7.5
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                val isRing = dist > 5.5 || (dist > 3.0 && dist < 4.2)
                if (isRing) {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(140, 110, 72))
                } else {
                    val variance = rand.nextInt(12) - 6
                    setTilePixel(pixels, tileId, x, y, Color.rgb(168 + variance, 138 + variance, 92 + variance))
                }
            }
        }
    }

    private fun drawPlanks(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(49)
        for (y in 0 until 16) {
            val isSeam = (y % 4 == 0)
            for (x in 0 until 16) {
                if (isSeam) {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(130, 95, 55))
                } else {
                    val variance = if (pack == TexturePack.BARE_BONES) 0 else rand.nextInt(14) - 7
                    setTilePixel(pixels, tileId, x, y, Color.rgb(168 + variance, 128 + variance, 75 + variance))
                }
            }
        }
    }

    private fun drawLeaves(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(50)
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val isHole = (rand.nextInt(5) == 0)
                if (isHole && !isBareBones(pack)) {
                    setTilePixel(pixels, tileId, x, y, Color.argb(80, 20, 60, 15))
                } else {
                    val variance = rand.nextInt(30) - 15
                    setTilePixel(pixels, tileId, x, y, Color.argb(230, 52 + variance, 115 + variance, 32 + variance))
                }
            }
        }
    }

    private fun drawSand(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(51)
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val variance = if (pack == TexturePack.BARE_BONES) 0 else rand.nextInt(18) - 9
                setTilePixel(pixels, tileId, x, y, Color.rgb(218 + variance, 205 + variance, 150 + variance))
            }
        }
    }

    private fun drawWater(pixels: IntArray, tileId: Int, pack: TexturePack) {
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val wave = ((x + y) % 4 == 0)
                val alpha = if (wave) 190 else 165
                val blue = if (wave) 230 else 205
                setTilePixel(pixels, tileId, x, y, Color.argb(alpha, 35, 110, blue))
            }
        }
    }

    private fun drawGlass(pixels: IntArray, tileId: Int, pack: TexturePack) {
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val isBorder = (x == 0 || x == 15 || y == 0 || y == 15)
                val isGlint = (x in 3..6 && y == x) || (x in 10..13 && y == x)
                if (isBorder) {
                    setTilePixel(pixels, tileId, x, y, Color.argb(240, 200, 220, 240))
                } else if (isGlint) {
                    setTilePixel(pixels, tileId, x, y, Color.argb(180, 240, 250, 255))
                } else {
                    setTilePixel(pixels, tileId, x, y, Color.argb(40, 180, 210, 235))
                }
            }
        }
    }

    private fun drawOre(pixels: IntArray, tileId: Int, gemColor: Int, pack: TexturePack) {
        drawStone(pixels, tileId, pack)
        // Overlay ore flecks
        val oreCoords = listOf(
            Pair(4, 4), Pair(5, 4), Pair(4, 5),
            Pair(10, 3), Pair(11, 3), Pair(11, 4),
            Pair(7, 10), Pair(8, 10), Pair(8, 11), Pair(7, 11),
            Pair(12, 11), Pair(13, 11), Pair(3, 12), Pair(3, 13)
        )
        for ((ox, oy) in oreCoords) {
            setTilePixel(pixels, tileId, ox, oy, gemColor)
        }
    }

    private fun drawBrick(pixels: IntArray, tileId: Int, pack: TexturePack) {
        for (y in 0 until 16) {
            val isMortarH = (y % 4 == 0)
            for (x in 0 until 16) {
                val rowIdx = y / 4
                val isMortarV = (x % 8 == (rowIdx % 2) * 4)
                if (isMortarH || isMortarV) {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(180, 175, 170))
                } else {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(155, 68, 52))
                }
            }
        }
    }

    private fun drawTorch(pixels: IntArray, tileId: Int, pack: TexturePack) {
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                if (x in 7..8 && y in 5..14) {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(115, 80, 45)) // stick
                } else if (x in 6..9 && y in 2..4) {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(255, 215, 0)) // flame
                } else {
                    setTilePixel(pixels, tileId, x, y, Color.TRANSPARENT)
                }
            }
        }
    }

    private fun drawCraftingTop(pixels: IntArray, tileId: Int, pack: TexturePack) {
        drawPlanks(pixels, tileId, pack)
        // 3x3 grid etched into top
        for (y in 2..13) {
            for (x in 2..13) {
                if (x == 5 || x == 9 || y == 5 || y == 9) {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(85, 55, 25))
                }
            }
        }
    }

    private fun drawCraftingSide(pixels: IntArray, tileId: Int, pack: TexturePack) {
        drawPlanks(pixels, tileId, pack)
        // Draw tools hanging on side
        for (y in 4..11) {
            setTilePixel(pixels, tileId, 5, y, Color.rgb(180, 180, 180)) // saw/tool
            setTilePixel(pixels, tileId, 10, y, Color.rgb(90, 60, 30))
        }
    }

    private fun drawFurnaceFront(pixels: IntArray, tileId: Int, pack: TexturePack) {
        drawCobblestone(pixels, tileId, pack)
        // Dark opening
        for (y in 6..12) {
            for (x in 4..11) {
                setTilePixel(pixels, tileId, x, y, Color.rgb(35, 35, 35))
            }
        }
    }

    private fun drawFurnaceSide(pixels: IntArray, tileId: Int, pack: TexturePack) {
        drawCobblestone(pixels, tileId, pack)
    }

    private fun drawChestTop(pixels: IntArray, tileId: Int, pack: TexturePack) {
        drawPlanks(pixels, tileId, pack)
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                if (x == 0 || x == 15 || y == 0 || y == 15) {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(65, 45, 20))
                }
            }
        }
    }

    private fun drawChestSide(pixels: IntArray, tileId: Int, pack: TexturePack) {
        drawPlanks(pixels, tileId, pack)
        // Latch in center
        for (y in 6..8) {
            for (x in 7..8) {
                setTilePixel(pixels, tileId, x, y, Color.rgb(205, 205, 205))
            }
        }
    }

    private fun drawCactus(pixels: IntArray, topId: Int, sideId: Int, pack: TexturePack) {
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                setTilePixel(pixels, topId, x, y, Color.rgb(55, 115, 35))
                val isThorn = (x % 4 == 0 && y % 3 == 0)
                if (isThorn) {
                    setTilePixel(pixels, sideId, x, y, Color.BLACK)
                } else {
                    setTilePixel(pixels, sideId, x, y, Color.rgb(45, 102, 28))
                }
            }
        }
    }

    private fun drawBedrock(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(52)
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val c = rand.nextInt(55) + 15
                setTilePixel(pixels, tileId, x, y, Color.rgb(c, c, c))
            }
        }
    }

    private fun drawSnow(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(53)
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val variance = rand.nextInt(12) - 6
                val c = (245 + variance).coerceIn(0, 255)
                setTilePixel(pixels, tileId, x, y, Color.rgb(c, c, c + 5))
            }
        }
    }

    // --- THE VOIDLANDS (END UPDATE) TEXTURES ---

    private fun drawVoidstone(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(54)
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val isStar = (rand.nextInt(24) == 0)
                if (isStar) {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(200, 100, 255))
                } else {
                    val variance = rand.nextInt(20) - 10
                    setTilePixel(pixels, tileId, x, y, Color.rgb(38 + variance, 24 + variance, 58 + variance))
                }
            }
        }
    }

    private fun drawVoidCrystal(pixels: IntArray, tileId: Int, pack: TexturePack) {
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val facet = ((x * 3 + y * 5) % 7 == 0)
                if (facet) {
                    setTilePixel(pixels, tileId, x, y, Color.argb(220, 255, 120, 255))
                } else {
                    setTilePixel(pixels, tileId, x, y, Color.argb(190, 180, 40, 245))
                }
            }
        }
    }

    private fun drawVoidOre(pixels: IntArray, tileId: Int, pack: TexturePack) {
        drawVoidstone(pixels, tileId, pack)
        val crystals = listOf(
            Pair(3, 4), Pair(4, 4), Pair(4, 5),
            Pair(10, 3), Pair(11, 4),
            Pair(7, 10), Pair(8, 10), Pair(8, 11),
            Pair(12, 12), Pair(3, 11)
        )
        for ((cx, cy) in crystals) {
            setTilePixel(pixels, tileId, cx, cy, Color.rgb(230, 60, 255))
        }
    }

    private fun drawVoidObsidian(pixels: IntArray, tileId: Int, pack: TexturePack) {
        val rand = Random(55)
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val variance = rand.nextInt(18) - 9
                val purpleVein = (x + y) % 7 == 0
                if (purpleVein) {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(95, 30, 150))
                } else {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(22 + variance, 15 + variance, 32 + variance))
                }
            }
        }
    }

    private fun drawVoidPortal(pixels: IntArray, tileId: Int, pack: TexturePack) {
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val swirl = ((x * x + y * y) % 8)
                val alpha = 210
                val r = 110 + swirl * 15
                val g = 25 + swirl * 8
                val b = 220 + swirl * 4
                setTilePixel(pixels, tileId, x, y, Color.argb(alpha, r, g, b))
            }
        }
    }

    private fun drawVoidBrick(pixels: IntArray, tileId: Int, pack: TexturePack) {
        for (y in 0 until 16) {
            val isMortarH = (y % 4 == 0)
            for (x in 0 until 16) {
                val rowIdx = y / 4
                val isMortarV = (x % 8 == (rowIdx % 2) * 4)
                if (isMortarH || isMortarV) {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(30, 15, 45))
                } else {
                    setTilePixel(pixels, tileId, x, y, Color.rgb(65, 40, 95))
                }
            }
        }
    }

    private fun isBareBones(pack: TexturePack): Boolean = pack == TexturePack.BARE_BONES
}

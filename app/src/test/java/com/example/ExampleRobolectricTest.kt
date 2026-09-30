package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.crafting.CraftingManager
import com.example.engine.TextureAtlasGenerator
import com.example.engine.TexturePack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MineCraftWorld", appName)
  }

  @Test
  fun `verify crafting recipes work`() {
    val result = CraftingManager.findRecipe(listOf("oak_log"), 1, 1)
    assertNotNull(result)
    assertEquals("oak_planks", result?.itemId)
    assertEquals(4, result?.count)
  }

  @Test
  fun `verify texture atlas generation for all packs`() {
    for (pack in TexturePack.entries) {
      val atlas = TextureAtlasGenerator.generateAtlas(pack)
      assertNotNull(atlas)
      assertEquals(TextureAtlasGenerator.ATLAS_SIZE, atlas.width)
      assertEquals(TextureAtlasGenerator.ATLAS_SIZE, atlas.height)
    }
  }
}

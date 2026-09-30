package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import com.example.model.BlockType
import com.example.model.CreatureEntity
import com.example.model.Dimension
import com.example.model.Projectile
import com.example.model.RaycastHit
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

class VoxelRenderer(
    private val context: Context,
    var voxelWorld: VoxelWorld? = null
) : GLSurfaceView.Renderer {

    // Camera
    var playerX: Float = 0f
    var playerY: Float = 22f
    var playerZ: Float = 0f
    var playerPitch: Float = 0f
    var playerYaw: Float = 0f
    var isThirdPerson: Boolean = false

    // World state
    var timeOfDay: Float = 0.2f
    var targetedBlock: RaycastHit? = null
    var breakProgress: Float = 0f
    var heldItemId: String = "air"
    var swingAnimation: Float = 0f

    // Entities & Projectiles
    var creatures: List<CreatureEntity> = emptyList()
    var projectiles: List<Projectile> = emptyList()

    // Texture Atlas
    private var atlasTextureId = 0
    private var pendingPackReload: TexturePack? = null

    // Matrices
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    // Shaders
    private var shaderProgram = 0
    private var uMVPMatrixLoc = 0
    private var uTextureLoc = 0
    private var uUseTextureLoc = 0
    private var uFogColorLoc = 0
    private var uFogDensityLoc = 0
    private var uLightDirLoc = 0
    private var aPositionLoc = 0
    private var aTexCoordLoc = 0
    private var aNormalLoc = 0
    private var aColorLoc = 0

    // VBO mesh for world (vertex stride: 3 pos + 2 uv + 3 normal + 4 color = 12 floats = 48 bytes)
    private var meshFloatBuffer: FloatBuffer? = null
    private var vertexCount = 0
    private var lastRebuildX = -999
    private var lastRebuildY = -999
    private var lastRebuildZ = -999
    private var needsMeshRebuild = true

    // Unit cube buffer for entities/items
    private lateinit var unitCubeBuffer: FloatBuffer

    private val vertexShaderSource = """
        uniform mat4 uMVPMatrix;
        uniform vec3 uLightDir;
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        attribute vec3 aNormal;
        attribute vec4 aColor;
        varying vec2 vTexCoord;
        varying vec4 vColor;
        varying float vLighting;
        varying float vDistance;

        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vDistance = gl_Position.z;
            vTexCoord = aTexCoord;
            vColor = aColor;
            float diffuse = max(dot(aNormal, uLightDir), 0.35);
            vLighting = diffuse;
        }
    """.trimIndent()

    private val fragmentShaderSource = """
        precision mediump float;
        uniform sampler2D uTexture;
        uniform int uUseTexture;
        uniform vec4 uFogColor;
        uniform float uFogDensity;
        varying vec2 vTexCoord;
        varying vec4 vColor;
        varying float vLighting;
        varying float vDistance;

        void main() {
            vec4 texColor = (uUseTexture == 1) ? texture2D(uTexture, vTexCoord) : vec4(1.0);
            if (texColor.a < 0.1) discard;

            vec4 finalColor = texColor * vColor * vec4(vec3(vLighting), 1.0);
            float fogFactor = clamp(exp(-pow(vDistance * uFogDensity, 2.0)), 0.0, 1.0);
            gl_FragColor = mix(uFogColor, finalColor, fogFactor);
        }
    """.trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        shaderProgram = createProgram(vertexShaderSource, fragmentShaderSource)
        uMVPMatrixLoc = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        uTextureLoc = GLES20.glGetUniformLocation(shaderProgram, "uTexture")
        uUseTextureLoc = GLES20.glGetUniformLocation(shaderProgram, "uUseTexture")
        uFogColorLoc = GLES20.glGetUniformLocation(shaderProgram, "uFogColor")
        uFogDensityLoc = GLES20.glGetUniformLocation(shaderProgram, "uFogDensity")
        uLightDirLoc = GLES20.glGetUniformLocation(shaderProgram, "uLightDir")

        aPositionLoc = GLES20.glGetAttribLocation(shaderProgram, "aPosition")
        aTexCoordLoc = GLES20.glGetAttribLocation(shaderProgram, "aTexCoord")
        aNormalLoc = GLES20.glGetAttribLocation(shaderProgram, "aNormal")
        aColorLoc = GLES20.glGetAttribLocation(shaderProgram, "aColor")

        loadTextureAtlas(TextureAtlasGenerator.currentPack)
        initUnitCube()
    }

    fun setTexturePack(pack: TexturePack) {
        pendingPackReload = pack
        needsMeshRebuild = true
    }

    private fun loadTextureAtlas(pack: TexturePack) {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        atlasTextureId = textures[0]

        val bitmap = TextureAtlasGenerator.generateAtlas(pack)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, atlasTextureId)
        // Authentic crisp Minecraft pixel aesthetics (GL_NEAREST, never blurred!)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 70f, ratio, 0.1f, 120f)
    }

    override fun onDrawFrame(gl: GL10?) {
        val world = voxelWorld ?: return

        // Reload texture pack if requested
        pendingPackReload?.let {
            loadTextureAtlas(it)
            pendingPackReload = null
        }

        // Sky and fog colors
        val (skyR, skyG, skyB) = calculateSkyColor(world.currentDimension, timeOfDay)
        GLES20.glClearColor(skyR, skyG, skyB, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Camera calculations
        val radPitch = Math.toRadians(playerPitch.toDouble())
        val radYaw = Math.toRadians(playerYaw.toDouble())

        val dirX = (-sin(radYaw) * cos(radPitch)).toFloat()
        val dirY = (-sin(radPitch)).toFloat()
        val dirZ = (-cos(radYaw) * cos(radPitch)).toFloat()

        var eyeX = playerX
        var eyeY = playerY + 1.62f
        var eyeZ = playerZ

        if (isThirdPerson) {
            eyeX -= dirX * 3.5f
            eyeY -= dirY * 3.5f
            eyeZ -= dirZ * 3.5f
        }

        Matrix.setLookAtM(
            viewMatrix, 0,
            eyeX, eyeY, eyeZ,
            eyeX + dirX, eyeY + dirY, eyeZ + dirZ,
            0f, 1f, 0f
        )

        GLES20.glUseProgram(shaderProgram)

        // Bind Texture Atlas
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, atlasTextureId)
        GLES20.glUniform1i(uTextureLoc, 0)
        GLES20.glUniform1i(uUseTextureLoc, 1)

        // Fog settings
        GLES20.glUniform4f(uFogColorLoc, skyR, skyG, skyB, 1.0f)
        val fogDensity = if (world.currentDimension == Dimension.VOIDLANDS) 0.024f else 0.016f
        GLES20.glUniform1f(uFogDensityLoc, fogDensity)

        // Sunlight direction
        val sunAngle = timeOfDay * Math.PI * 2.0
        val lx = cos(sunAngle).toFloat()
        val ly = sin(sunAngle).toFloat().coerceAtLeast(0.2f)
        val lz = 0.3f
        GLES20.glUniform3f(uLightDirLoc, lx, ly, lz)

        // Rebuild mesh if player moved
        val pBx = floor(playerX).toInt()
        val pBy = floor(playerY).toInt()
        val pBz = floor(playerZ).toInt()
        if (needsMeshRebuild || Math.abs(pBx - lastRebuildX) > 4 || Math.abs(pBz - lastRebuildZ) > 4) {
            rebuildWorldMesh(world, pBx, pBy, pBz)
            lastRebuildX = pBx
            lastRebuildY = pBy
            lastRebuildZ = pBz
            needsMeshRebuild = false
        }

        // Draw Textured Voxel Mesh
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0)

        meshFloatBuffer?.let { buf ->
            val stride = 48 // 12 floats * 4 bytes

            buf.position(0)
            GLES20.glVertexAttribPointer(aPositionLoc, 3, GLES20.GL_FLOAT, false, stride, buf)
            GLES20.glEnableVertexAttribArray(aPositionLoc)

            buf.position(3)
            GLES20.glVertexAttribPointer(aTexCoordLoc, 2, GLES20.GL_FLOAT, false, stride, buf)
            GLES20.glEnableVertexAttribArray(aTexCoordLoc)

            buf.position(5)
            GLES20.glVertexAttribPointer(aNormalLoc, 3, GLES20.GL_FLOAT, false, stride, buf)
            GLES20.glEnableVertexAttribArray(aNormalLoc)

            buf.position(8)
            GLES20.glVertexAttribPointer(aColorLoc, 4, GLES20.GL_FLOAT, false, stride, buf)
            GLES20.glEnableVertexAttribArray(aColorLoc)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

            GLES20.glDisableVertexAttribArray(aPositionLoc)
            GLES20.glDisableVertexAttribArray(aTexCoordLoc)
            GLES20.glDisableVertexAttribArray(aNormalLoc)
            GLES20.glDisableVertexAttribArray(aColorLoc)
        }

        // Render Entities
        renderCreatures()

        // Render Projectiles
        renderProjectiles()

        // Render Targeted Block Highlight
        renderBlockHighlight()

        // Render Held Item (1st person hand)
        if (!isThirdPerson) {
            renderHeldItem(eyeX, eyeY, eyeZ, dirX, dirY, dirZ)
        }
    }

    fun requestMeshRebuild() {
        needsMeshRebuild = true
    }

    private fun rebuildWorldMesh(world: VoxelWorld, cx: Int, cy: Int, cz: Int) {
        val radiusH = 26
        val radiusV = 16
        val minX = (cx - radiusH).coerceAtLeast(world.minX)
        val maxX = (cx + radiusH).coerceAtMost(world.maxX)
        val minZ = (cz - radiusH).coerceAtLeast(world.minZ)
        val maxZ = (cz + radiusH).coerceAtMost(world.maxZ)
        val minY = (cy - radiusV).coerceAtLeast(world.minY)
        val maxY = (cy + radiusV).coerceAtMost(world.maxY)

        val vertexList = ArrayList<Float>(60000)

        for (y in minY..maxY) {
            for (x in minX..maxX) {
                for (z in minZ..maxZ) {
                    val block = world.getBlock(x, y, z)
                    if (block == BlockType.AIR) continue

                    val fx = x.toFloat()
                    val fy = y.toFloat()
                    val fz = z.toFloat()

                    // Top (+Y)
                    val topB = world.getBlock(x, y + 1, z)
                    if (topB.isTransparent || !topB.isSolid) {
                        addTexturedFace(vertexList, fx, fy, fz, Face.TOP, block.topTile, 0f, 1f, 0f, block.topColor)
                    }

                    // Bottom (-Y)
                    val botB = world.getBlock(x, y - 1, z)
                    if (botB.isTransparent || !botB.isSolid) {
                        addTexturedFace(vertexList, fx, fy, fz, Face.BOTTOM, block.bottomTile, 0f, -1f, 0f, block.bottomColor)
                    }

                    // North (-Z)
                    val northB = world.getBlock(x, y, z - 1)
                    if (northB.isTransparent || !northB.isSolid) {
                        addTexturedFace(vertexList, fx, fy, fz, Face.NORTH, block.sideTile, 0f, 0f, -1f, block.sideColor)
                    }

                    // South (+Z)
                    val southB = world.getBlock(x, y, z + 1)
                    if (southB.isTransparent || !southB.isSolid) {
                        addTexturedFace(vertexList, fx, fy, fz, Face.SOUTH, block.sideTile, 0f, 0f, 1f, block.sideColor)
                    }

                    // West (-X)
                    val westB = world.getBlock(x - 1, y, z)
                    if (westB.isTransparent || !westB.isSolid) {
                        addTexturedFace(vertexList, fx, fy, fz, Face.WEST, block.sideTile, -1f, 0f, 0f, block.sideColor)
                    }

                    // East (+X)
                    val eastB = world.getBlock(x + 1, y, z)
                    if (eastB.isTransparent || !eastB.isSolid) {
                        addTexturedFace(vertexList, fx, fy, fz, Face.EAST, block.sideTile, 1f, 0f, 0f, block.sideColor)
                    }
                }
            }
        }

        vertexCount = vertexList.size / 12
        val bb = ByteBuffer.allocateDirect(vertexList.size * 4)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        for (f in vertexList) fb.put(f)
        fb.position(0)
        meshFloatBuffer = fb
    }

    private enum class Face { TOP, BOTTOM, NORTH, SOUTH, EAST, WEST }

    private fun addTexturedFace(
        list: ArrayList<Float>,
        x: Float, y: Float, z: Float,
        face: Face,
        tileId: Int,
        nx: Float, ny: Float, nz: Float,
        colorTintLong: Long
    ) {
        val col = tileId % TextureAtlasGenerator.TILES_PER_ROW
        val row = tileId / TextureAtlasGenerator.TILES_PER_ROW
        val u0 = (col * TextureAtlasGenerator.TILE_SIZE).toFloat() / TextureAtlasGenerator.ATLAS_SIZE
        val u1 = ((col + 1) * TextureAtlasGenerator.TILE_SIZE).toFloat() / TextureAtlasGenerator.ATLAS_SIZE
        val v0 = (row * TextureAtlasGenerator.TILE_SIZE).toFloat() / TextureAtlasGenerator.ATLAS_SIZE
        val v1 = ((row + 1) * TextureAtlasGenerator.TILE_SIZE).toFloat() / TextureAtlasGenerator.ATLAS_SIZE

        val a = ((colorTintLong shr 24) and 0xFF) / 255.0f
        val r = 1.0f
        val g = 1.0f
        val b = 1.0f

        val (v1p, v2p, v3p, v4p) = when (face) {
            Face.TOP -> arrayOf(
                floatArrayOf(x, y + 1f, z + 1f),
                floatArrayOf(x + 1f, y + 1f, z + 1f),
                floatArrayOf(x + 1f, y + 1f, z),
                floatArrayOf(x, y + 1f, z)
            )
            Face.BOTTOM -> arrayOf(
                floatArrayOf(x, y, z),
                floatArrayOf(x + 1f, y, z),
                floatArrayOf(x + 1f, y, z + 1f),
                floatArrayOf(x, y, z + 1f)
            )
            Face.NORTH -> arrayOf(
                floatArrayOf(x + 1f, y + 1f, z),
                floatArrayOf(x + 1f, y, z),
                floatArrayOf(x, y, z),
                floatArrayOf(x, y + 1f, z)
            )
            Face.SOUTH -> arrayOf(
                floatArrayOf(x, y + 1f, z + 1f),
                floatArrayOf(x, y, z + 1f),
                floatArrayOf(x + 1f, y, z + 1f),
                floatArrayOf(x + 1f, y + 1f, z + 1f)
            )
            Face.WEST -> arrayOf(
                floatArrayOf(x, y + 1f, z),
                floatArrayOf(x, y, z),
                floatArrayOf(x, y, z + 1f),
                floatArrayOf(x, y + 1f, z + 1f)
            )
            Face.EAST -> arrayOf(
                floatArrayOf(x + 1f, y + 1f, z + 1f),
                floatArrayOf(x + 1f, y, z + 1f),
                floatArrayOf(x + 1f, y, z),
                floatArrayOf(x + 1f, y + 1f, z)
            )
        }

        fun putVert(vp: FloatArray, u: Float, v: Float) {
            // Pos (3)
            list.add(vp[0]); list.add(vp[1]); list.add(vp[2])
            // UV (2)
            list.add(u); list.add(v)
            // Normal (3)
            list.add(nx); list.add(ny); list.add(nz)
            // Color (4)
            list.add(r); list.add(g); list.add(b); list.add(a)
        }

        // Tri 1: v1, v2, v3
        putVert(v1p, u0, v1)
        putVert(v2p, u1, v1)
        putVert(v3p, u1, v0)

        // Tri 2: v1, v3, v4
        putVert(v1p, u0, v1)
        putVert(v3p, u1, v0)
        putVert(v4p, u0, v0)
    }

    private fun renderCreatures() {
        GLES20.glUniform1i(uUseTextureLoc, 0)
        for (creature in creatures) {
            if (creature.isDead) continue

            Matrix.setIdentityM(modelMatrix, 0)
            val scale = if (creature.type.isBoss) 2.5f else 0.8f
            Matrix.translateM(modelMatrix, 0, creature.x, creature.y, creature.z)
            Matrix.rotateM(modelMatrix, 0, creature.yaw, 0f, 1f, 0f)
            Matrix.scaleM(modelMatrix, 0, scale, scale, scale)

            val mvMatrix = FloatArray(16)
            Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            val finalMvp = FloatArray(16)
            Matrix.multiplyMM(finalMvp, 0, projectionMatrix, 0, mvMatrix, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, finalMvp, 0)

            val color = if (creature.isHurt) 0xFFFF1744 else creature.type.primaryColor
            drawSolidCube(color)
        }
        GLES20.glUniform1i(uUseTextureLoc, 1)
    }

    private fun renderProjectiles() {
        GLES20.glUniform1i(uUseTextureLoc, 0)
        for (p in projectiles) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, p.x, p.y, p.z)
            Matrix.scaleM(modelMatrix, 0, 0.25f, 0.25f, 0.25f)

            val mvMatrix = FloatArray(16)
            Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            val finalMvp = FloatArray(16)
            Matrix.multiplyMM(finalMvp, 0, projectionMatrix, 0, mvMatrix, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, finalMvp, 0)
            drawSolidCube(p.color)
        }
        GLES20.glUniform1i(uUseTextureLoc, 1)
    }

    private fun renderBlockHighlight() {
        val hit = targetedBlock ?: return
        GLES20.glUniform1i(uUseTextureLoc, 0)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, hit.blockPos.x.toFloat() - 0.005f, hit.blockPos.y.toFloat() - 0.005f, hit.blockPos.z.toFloat() - 0.005f)
        Matrix.scaleM(modelMatrix, 0, 1.01f, 1.01f, 1.01f)

        val mvMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        val finalMvp = FloatArray(16)
        Matrix.multiplyMM(finalMvp, 0, projectionMatrix, 0, mvMatrix, 0)

        GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, finalMvp, 0)

        val alpha = if (breakProgress > 0f) 0.5f + breakProgress * 0.4f else 0.2f
        val color = if (breakProgress > 0f) 0xFFFF5722 else 0xFFFFFFFF
        drawSolidCube(color, alpha)
        GLES20.glUniform1i(uUseTextureLoc, 1)
    }

    private fun renderHeldItem(eyeX: Float, eyeY: Float, eyeZ: Float, dirX: Float, dirY: Float, dirZ: Float) {
        if (heldItemId == "air") return
        GLES20.glUniform1i(uUseTextureLoc, 0)

        val rx = -dirZ
        val rz = dirX
        val len = Math.sqrt((rx * rx + rz * rz).toDouble()).toFloat().coerceAtLeast(0.001f)
        val nrx = rx / len
        val nrz = rz / len

        val swing = sin(swingAnimation * Math.PI).toFloat()
        val itemX = eyeX + dirX * 0.6f + nrx * 0.28f
        val itemY = eyeY + dirY * 0.6f - 0.25f - swing * 0.15f
        val itemZ = eyeZ + dirZ * 0.6f + nrz * 0.28f

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, itemX, itemY, itemZ)
        Matrix.rotateM(modelMatrix, 0, -playerYaw, 0f, 1f, 0f)
        Matrix.rotateM(modelMatrix, 0, 25f + swing * 40f, 1f, 0f, 0f)
        Matrix.scaleM(modelMatrix, 0, 0.2f, 0.2f, 0.2f)

        val mvMatrix = FloatArray(16)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        val finalMvp = FloatArray(16)
        Matrix.multiplyMM(finalMvp, 0, projectionMatrix, 0, mvMatrix, 0)

        GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, finalMvp, 0)
        drawSolidCube(0xFF4DD0E1)
        GLES20.glUniform1i(uUseTextureLoc, 1)
    }

    private fun initUnitCube() {
        val cubeVerts = ArrayList<Float>()
        fun addC(x: Float, y: Float, z: Float, nx: Float, ny: Float, nz: Float) {
            cubeVerts.add(x); cubeVerts.add(y); cubeVerts.add(z)
            cubeVerts.add(0f); cubeVerts.add(0f)
            cubeVerts.add(nx); cubeVerts.add(ny); cubeVerts.add(nz)
            cubeVerts.add(1f); cubeVerts.add(1f); cubeVerts.add(1f); cubeVerts.add(1f)
        }

        fun quad(v1: FloatArray, v2: FloatArray, v3: FloatArray, v4: FloatArray, n: FloatArray) {
            addC(v1[0], v1[1], v1[2], n[0], n[1], n[2])
            addC(v2[0], v2[1], v2[2], n[0], n[1], n[2])
            addC(v3[0], v3[1], v3[2], n[0], n[1], n[2])
            addC(v1[0], v1[1], v1[2], n[0], n[1], n[2])
            addC(v3[0], v3[1], v3[2], n[0], n[1], n[2])
            addC(v4[0], v4[1], v4[2], n[0], n[1], n[2])
        }

        val topN = floatArrayOf(0f, 1f, 0f)
        quad(floatArrayOf(0f, 1f, 1f), floatArrayOf(1f, 1f, 1f), floatArrayOf(1f, 1f, 0f), floatArrayOf(0f, 1f, 0f), topN)
        val botN = floatArrayOf(0f, -1f, 0f)
        quad(floatArrayOf(0f, 0f, 0f), floatArrayOf(1f, 0f, 0f), floatArrayOf(1f, 0f, 1f), floatArrayOf(0f, 0f, 1f), botN)
        val northN = floatArrayOf(0f, 0f, -1f)
        quad(floatArrayOf(1f, 1f, 0f), floatArrayOf(1f, 0f, 0f), floatArrayOf(0f, 0f, 0f), floatArrayOf(0f, 1f, 0f), northN)
        val southN = floatArrayOf(0f, 0f, 1f)
        quad(floatArrayOf(0f, 1f, 1f), floatArrayOf(0f, 0f, 1f), floatArrayOf(1f, 0f, 1f), floatArrayOf(1f, 1f, 1f), southN)
        val westN = floatArrayOf(-1f, 0f, 0f)
        quad(floatArrayOf(0f, 1f, 0f), floatArrayOf(0f, 0f, 0f), floatArrayOf(0f, 0f, 1f), floatArrayOf(0f, 1f, 1f), westN)
        val eastN = floatArrayOf(1f, 0f, 0f)
        quad(floatArrayOf(1f, 1f, 1f), floatArrayOf(1f, 0f, 1f), floatArrayOf(1f, 0f, 0f), floatArrayOf(1f, 1f, 0f), eastN)

        val bb = ByteBuffer.allocateDirect(cubeVerts.size * 4)
        bb.order(ByteOrder.nativeOrder())
        unitCubeBuffer = bb.asFloatBuffer()
        for (f in cubeVerts) unitCubeBuffer.put(f)
        unitCubeBuffer.position(0)
    }

    private fun drawSolidCube(colorLong: Long, overrideAlpha: Float? = null) {
        val a = overrideAlpha ?: (((colorLong shr 24) and 0xFF) / 255.0f)
        val r = ((colorLong shr 16) and 0xFF) / 255.0f
        val g = ((colorLong shr 8) and 0xFF) / 255.0f
        val b = (colorLong and 0xFF) / 255.0f

        val stride = 48
        unitCubeBuffer.position(0)
        GLES20.glVertexAttribPointer(aPositionLoc, 3, GLES20.GL_FLOAT, false, stride, unitCubeBuffer)
        GLES20.glEnableVertexAttribArray(aPositionLoc)

        GLES20.glVertexAttrib4f(aColorLoc, r, g, b, a)
        GLES20.glDisableVertexAttribArray(aColorLoc)

        unitCubeBuffer.position(5)
        GLES20.glVertexAttribPointer(aNormalLoc, 3, GLES20.GL_FLOAT, false, stride, unitCubeBuffer)
        GLES20.glEnableVertexAttribArray(aNormalLoc)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)

        GLES20.glDisableVertexAttribArray(aPositionLoc)
        GLES20.glDisableVertexAttribArray(aNormalLoc)
    }

    private fun calculateSkyColor(dimension: Dimension, timeOfDay: Float): Triple<Float, Float, Float> {
        if (dimension == Dimension.VOIDLANDS) {
            return Triple(0.06f, 0.03f, 0.12f)
        }

        val day = sin(timeOfDay * Math.PI * 2.0).toFloat()
        return when {
            day > 0.3f -> Triple(0.45f, 0.65f, 0.98f)
            day > -0.1f -> {
                val t = (day + 0.1f) / 0.4f
                Triple(0.2f + t * 0.65f, 0.1f + t * 0.45f, 0.3f + t * 0.6f)
            }
            else -> Triple(0.03f, 0.05f, 0.12f)
        }
    }

    private fun createProgram(vSource: String, fSource: String): Int {
        val vShader = loadShader(GLES20.GL_VERTEX_SHADER, vSource)
        val fShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fSource)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vShader)
        GLES20.glAttachShader(program, fShader)
        GLES20.glLinkProgram(program)
        return program
    }

    private fun loadShader(type: Int, code: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
        return shader
    }
}

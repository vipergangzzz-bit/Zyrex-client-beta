package com.zyrex.utils.render

import com.zyrex.util.MinecraftInstance
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

object RenderUtils : MinecraftInstance() {

    var deltaTime: Int = 0

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        Gui.drawRect(x.toInt(), y.toInt(), x2.toInt(), y2.toInt(), color)
    }

    fun drawRect(x: Int, y: Int, x2: Int, y2: Int, color: Int) {
        Gui.drawRect(x, y, x2, y2, color)
    }

    fun drawRect(x: Double, y: Double, x2: Double, y2: Double, color: Int) {
        Gui.drawRect(x.toInt(), y.toInt(), x2.toInt(), y2.toInt(), color)
    }

    fun drawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, borderColor: Int, fillColor: Int) {
        drawRect(x, y, x2, y2, fillColor)
        drawRect(x, y, x2, y + width, borderColor)
        drawRect(x, y2 - width, x2, y2, borderColor)
        drawRect(x, y, x + width, y2, borderColor)
        drawRect(x2 - width, y, x2, y2, borderColor)
    }

    fun drawBorderedRect(x: Int, y: Int, x2: Int, y2: Int, width: Int, borderColor: Int, fillColor: Int) {
        drawRect(x, y, x2, y2, fillColor)
        drawRect(x, y, x2, y + width, borderColor)
        drawRect(x, y2 - width, x2, y2, borderColor)
        drawRect(x, y, x + width, y2, borderColor)
        drawRect(x2 - width, y, x2, y2, borderColor)
    }

    fun drawBorderedRect(x: Int, y: Int, x2: Int, y2: Int, width: Float, borderColor: Int, fillColor: Int) {
        val fx = x.toFloat()
        val fy = y.toFloat()
        val fx2 = x2.toFloat()
        val fy2 = y2.toFloat()
        drawRect(fx, fy, fx2, fy2, fillColor)
        drawRect(fx, fy, fx2, fy + width, borderColor)
        drawRect(fx, fy2 - width, fx2, fy2, borderColor)
        drawRect(fx, fy, fx + width, fy2, borderColor)
        drawRect(fx2 - width, fy, fx2, fy2, borderColor)
    }

    fun drawFilledCircle(x: Int, y: Int, radius: Float, color: Color) {
        drawFilledCircle(x.toFloat(), y.toFloat(), radius, color.rgb)
    }

    fun drawFilledCircle(x: Float, y: Float, radius: Float, color: Int) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_POINT_SMOOTH)
        GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_NICEST)
        GL11.glPointSize(radius * 2)
        GL11.glBegin(GL11.GL_POINTS)
        GL11.glColor4f(
            ((color shr 16) and 0xFF) / 255f,
            ((color shr 8) and 0xFF) / 255f,
            (color and 0xFF) / 255f,
            ((color shr 24) and 0xFF) / 255f
        )
        GL11.glVertex2f(x, y)
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glPopAttrib()
    }

    fun drawFilledCircle(x: Float, y: Float, radius: Float, color: Color) {
        drawFilledCircle(x, y, radius, color.rgb)
    }

    fun drawBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color: Int) {
        drawRect(x, y, x2, y + width, color)
        drawRect(x, y2 - width, x2, y2, color)
        drawRect(x, y, x + width, y2, color)
        drawRect(x2 - width, y, x2, y2, color)
    }

    fun drawImage(resourceLocation: ResourceLocation, x: Int, y: Int, width: Int, height: Int) {
        GlStateManager.color(1f, 1f, 1f, 1f)
        mc.renderEngine.bindTexture(resourceLocation)
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())
    }

    fun drawTexture(id: Int, x: Int, y: Int, width: Int, height: Int) {
        GlStateManager.bindTexture(id)
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())
    }

    fun updateTextureCache(
        id: Int,
        hue: Float,
        width: Int,
        height: Int,
        generateImage: (java.awt.image.BufferedImage, Int) -> Unit,
        drawAt: (Int) -> Unit
    ) {
        val image = java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB)
        generateImage(image, id)
        val textureId = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        val pixels = IntArray(width * height)
        image.getRGB(0, 0, width, height, pixels, 0, width)
        val buffer = java.nio.ByteBuffer.allocateDirect(width * height * 4)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = pixels[y * width + x]
                buffer.put(((pixel shr 16) and 0xFF).toByte())
                buffer.put(((pixel shr 8) and 0xFF).toByte())
                buffer.put((pixel and 0xFF).toByte())
                buffer.put(0xFF.toByte())
            }
        }
        buffer.flip()
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer)
        drawAt(textureId)
    }
}

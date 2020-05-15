package info.laht.aco.render.jme

import com.jme3.asset.AssetManager
import com.jme3.input.RawInputListener
import com.jme3.input.event.*
import com.jme3.material.Material
import com.jme3.material.RenderState
import com.jme3.math.ColorRGBA
import com.jme3.math.Quaternion
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import info.laht.aco.math.Color
import org.joml.Quaterniondc
import org.joml.Vector3dc


internal fun Vector3f.set(v: Vector3dc) = apply {
    set(v.x().toFloat(), v.y().toFloat(), v.z().toFloat())
}

internal fun Quaternion.set(q: Quaterniondc) = apply {
    set(q.x().toFloat(), q.y().toFloat(), q.z().toFloat(), q.w().toFloat())
}

internal fun AssetManager.getLightingMaterial(color: Color? = null): Material {
    return Material(this, "Common/MatDefs/Light/Lighting.j3md").apply {
        additionalRenderState.blendMode = RenderState.BlendMode.Alpha
        if (color != null) {
            ColorRGBA().set(color).also { colorRGBA ->
                setBoolean("UseMaterialColors", true)
                setColor("Ambient", colorRGBA)
                setColor("Diffuse", colorRGBA)
                setColor("Specular", colorRGBA)
                setColor("GlowColor", colorRGBA)
            }
        }
    }
}

/*
internal fun Node.setLocalTranslation(v: Vector3dc) {
    setLocalTranslation(v.x().toFloat(), v.y().toFloat(), v.z().toFloat())
}
*/

internal fun ColorRGBA.set(c: Color, alpha: Float = 1f) = apply {
    set(c.r, c.g, c.b, alpha)
}

internal fun AssetManager.getUnshadedMaterial(): Material {
    return Material(this, "Common/MatDefs/Misc/Unshaded.j3md")
}

internal fun AssetManager.getWireFrameMaterial(color: Color? = null): Material {
    return getUnshadedMaterial().apply {
        additionalRenderState.isWireframe = true
        if (color != null) {
            setColor("Color", ColorRGBA().set(color))
        }
    }
}

internal open class RawInputAdapter : RawInputListener {
    override fun beginInput() {}
    override fun endInput() {}
    override fun onJoyAxisEvent(evt: JoyAxisEvent) {}
    override fun onJoyButtonEvent(evt: JoyButtonEvent) {}
    override fun onMouseMotionEvent(evt: MouseMotionEvent) {}
    override fun onMouseButtonEvent(evt: MouseButtonEvent) {}
    override fun onKeyEvent(evt: KeyInputEvent) {}
    override fun onTouchEvent(evt: TouchEvent) {}
}

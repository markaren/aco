package info.laht.aco.render.jme

import com.jme3.material.RenderState
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.shape.Box
import info.laht.aco.core.ComponentMapper
import info.laht.aco.core.Entity
import info.laht.aco.core.Family
import info.laht.aco.math.Color
import info.laht.aco.render.TransformComponent
import info.laht.aco.render.geometry.BoxShape
import info.laht.aco.render.geometry.GeometryComponent
import info.laht.aco.systems.IteratingSystem
import org.joml.Quaterniond
import org.joml.Vector3d

class JmeRenderSystem : IteratingSystem(
    Family.all(TransformComponent::class.java, GeometryComponent::class.java).get()
) {

    private val renderer = JmeRenderer()
    private val tm = ComponentMapper.getFor(TransformComponent::class.java)
    private val gm = ComponentMapper.getFor(GeometryComponent::class.java)

    private val map = mutableMapOf<Entity, Node>()

    private val tmpVec = Vector3d()
    private val tmpQuat = Quaterniond()

    private val root: Node
        get() = renderer.root

    val cameraPos: Vector3f
        get() = renderer.camera.location
    var moveSpeed: Float
        get() = renderer.flyByCamera.moveSpeed
        set(value) {
            renderer.flyByCamera.moveSpeed = value
        }
    val backgroundColor: ColorRGBA
        get() = renderer.viewPort.backgroundColor

    init {
        renderer.start()
        backgroundColor.set(0.6f, 0.7f, 1f, 1f)
    }

    private fun createBox(shape: BoxShape, color: Color): Geometry {
        return Geometry(
            "BoxGeometry",
            Box(shape.width * 0.5f, shape.height * 0.5f, shape.depth * 0.5f)
        ).apply {
            material = renderer.assetManager.getLightingMaterial(color)
            material.additionalRenderState.faceCullMode = RenderState.FaceCullMode.Off
        }
    }

    private fun createNode(entity: Entity): Node {
        return Node().also { node ->
            renderer.invokeLater {
                val geometry = gm.get(entity)
                when (val shape = geometry.shape) {
                    is BoxShape -> {
                        node.attachChild(createBox(shape, geometry.color))
                    }
                }
                root.attachChild(node)
            }
        }
    }

    override fun postInit() {
        entities.forEach { entity ->
            map.computeIfAbsent(entity) {
                createNode(entity)
            }
        }
    }

    override fun processEntity(entity: Entity, currentTime: Double, deltaTime: Double) {

        val node = map.computeIfAbsent(entity) {
            createNode(entity)
        }

        val transform = tm.get(entity)

        renderer.invokeLater {
            node.localTranslation.set(transform.getWorldTranslation(tmpVec))
            node.localRotation.set(transform.getWorldQuaternion(tmpQuat))
            node.forceRefresh(true, true, true)
        }

    }

    override fun terminate() {
        renderer.stop()
    }

}

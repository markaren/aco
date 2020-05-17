package info.laht.aco.render.jme

import com.jme3.app.SimpleApplication
import com.jme3.app.state.AbstractAppState
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.material.RenderState
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.shape.Box
import com.jme3.scene.shape.Sphere
import info.laht.aco.core.*
import info.laht.aco.math.Color
import info.laht.aco.render.TransformComponent
import info.laht.aco.render.geometry.BoxShape
import info.laht.aco.render.geometry.GeometryComponent
import info.laht.aco.render.geometry.SphereShape
import info.laht.aco.systems.IteratingSystem
import org.joml.Quaterniond
import org.joml.Vector3d
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class JmeEngineRunner(
    private val engine: Engine
) : EngineRunner {

    private val app = App()
    private var running = false

    private val lock = ReentrantLock()
    private var initialized = lock.newCondition()

    init {
        app.start()
        engine.addSystem(app.renderSystem)
        lock.withLock {
            initialized.await()
        }

    }

    override fun start() {
        running = true
    }

    fun pause(flag: Boolean) {
        running = flag
    }

    override fun stop() {
        app.stop()
    }

    private inner class App : SimpleApplication() {

        private val root = Node()
        val renderSystem: JmeInternalRenderSystem by lazy {
            JmeInternalRenderSystem()
        }

        override fun simpleInitApp() {
            super.setPauseOnLostFocus(false)
            super.flyCam.isDragToRotate = true
            super.rootNode.attachChild(root)
            super.flyCam.moveSpeed = 10f

            super.viewPort.backgroundColor.set(0.6f, 0.7f, 1f, 1f)


            setupLights()


            super.stateManager.attach(object : AbstractAppState() {
                override fun cleanup() {
                    engine.close()
                }
            })

            engine.init()

            lock.withLock {
                initialized.signalAll()
            }
        }

        override fun simpleUpdate(tpf: Float) {
            if (running) {
                engine.step(tpf.toDouble())
            }
        }

        private fun setupLights() {

            DirectionalLight().apply {
                color = ColorRGBA.White.mult(0.8f)
                direction = Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal()
                rootNode.addLight(this)
            }

            DirectionalLight().apply {
                color = ColorRGBA.White.mult(0.8f)
                direction = Vector3f(0.5f, 0.5f, 0.5f).normalizeLocal()
                rootNode.addLight(this)
            }

            AmbientLight().apply {
                color = ColorRGBA.White.mult(0.3f)
                rootNode.addLight(this)
            }

        }

        private fun createBox(shape: BoxShape, color: Color): Geometry {
            return Geometry(
                "BoxGeometry",
                Box(shape.width * 0.5f, shape.height * 0.5f, shape.depth * 0.5f)
            ).apply {
                material = assetManager.getLightingMaterial(color)
                material.additionalRenderState.faceCullMode = RenderState.FaceCullMode.Off
            }
        }

        private fun createSphere(shape: SphereShape, color: Color): Geometry {
            return Geometry(
                "SphereGeometry",
                Sphere(32, 32, shape.radius)
            ).apply {
                material = assetManager.getLightingMaterial(color)
                material.additionalRenderState.faceCullMode = RenderState.FaceCullMode.Off
            }
        }

        private inner class JmeInternalRenderSystem : IteratingSystem(
            Family.all(TransformComponent::class.java, GeometryComponent::class.java).get()
        ) {

            private val tmpVec = Vector3d()
            private val tmpQuat = Quaterniond()
            private val map = mutableMapOf<Entity, Node>()

            private val tm = ComponentMapper.getFor(TransformComponent::class.java)
            private val gm = ComponentMapper.getFor(GeometryComponent::class.java)

            override fun processEntity(entity: Entity, deltaTime: Double) {
                val node = map.computeIfAbsent(entity) {
                    Node().apply {
                        val geometry = gm.get(entity)
                        when (val shape = geometry.shape) {
                            is BoxShape -> {
                                attachChild(createBox(shape, geometry.color))
                            }
                            is SphereShape -> {
                                attachChild(createSphere(shape, geometry.color))
                            }
                        }
                        root.attachChild(this)
                    }
                }
                val transform = tm.get(entity)
                node.localTranslation.set(transform.getWorldTranslation(tmpVec))
                node.localRotation.set(transform.getWorldQuaternion(tmpQuat))
                node.forceRefresh(true, true, true)
            }
        }

    }

}

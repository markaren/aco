package info.laht.aco.render.jme

import com.jme3.app.SimpleApplication
import com.jme3.asset.AssetManager
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.math.ColorRGBA
import com.jme3.math.FastMath
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.properties.Delegates

internal class JmeRenderer: SimpleApplication() {

    private val lock = ReentrantLock()
    private var initialized = lock.newCondition()
    private var tasks: Queue<() -> Unit> = ArrayDeque()

    val root = Node("root").apply {
        rotate((-FastMath.PI / 2), 0f, 0f)
    }

    fun invokeLater(task: () -> Unit) {
        tasks.add(task)
    }

    private fun invokePendingTasks() {
        while (!tasks.isEmpty()) {
            tasks.poll()?.invoke()
        }
    }

    override fun start() {
        super.start()
        lock.withLock {
            initialized.await()
        }
    }

    override fun simpleInitApp() {
        super.setPauseOnLostFocus(false)

        super.flyCam.isDragToRotate = true

        super.rootNode.attachChild(root)

        super.inputManager.isCursorVisible = true

        setupLights()

        invokePendingTasks()

        lock.withLock {
            initialized.signalAll()
        }

    }

    override fun simpleUpdate(tpf: Float) {
        invokePendingTasks()
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


}

package info.laht.aco.render

import info.laht.aco.core.Component
import org.joml.*
import java.lang.Math

class TransformComponent: Component {

    var autoUpdate = true
    var parent: TransformComponent? = null
        private set

    private val local = Matrix4d()
    private val world = Matrix4d()
    private val children = mutableListOf<TransformComponent>()
    private var worldNeedsUpdate = false

    fun getLocal(): Matrix4dc {
        return local
    }

    fun getWorld(force: Boolean = true): Matrix4dc {
        if (autoUpdate || force) {
            updateMatrixWorld()
        }
        return world
    }

    fun updateMatrixWorld() {
        if (!hasParent()) {
            world.set(local)
        } else {
            world.set(parent!!.getWorld(true)).mul(local)
        }
        worldNeedsUpdate = false
    }

    fun setWorldNeedsUpdate() {
        worldNeedsUpdate = true
        children.forEach { obj: TransformComponent -> obj.setWorldNeedsUpdate() }
    }

    fun hasParent(): Boolean {
        return parent != null
    }

    fun setParent(parent: TransformComponent) {
        require(!(parent === this)) { "Setting self as parent!" }
        if (hasParent()) {
            detatchFromParent()
        }
        this.parent = parent
        setWorldNeedsUpdate()
    }

    fun addChild(child: TransformComponent) {
        child.setParent(this)
    }

    fun detatchFromParent() {
        val w = getWorld()
        parent!!.children.remove(this)
        parent = null
        setLocal(w)
    }

    fun getChildren(): List<TransformComponent> {
        return children
    }

    var localTranslation: Vector3dc
        get() = getLocalTranslation(Vector3d())
        set(v) {
            setLocalTranslation(v.x(), v.y(), v.z())
        }

    fun getLocalTranslation(store: Vector3d): Vector3d {
        return local.getTranslation(store)
    }

    val worldTranslation: Vector3dc
        get() = getWorldTranslation(Vector3d())

    fun getWorldTranslation(store: Vector3d): Vector3dc {
        return getWorld().getTranslation(store)
    }

    var localQuaternion: Quaterniondc
        get() = getLocalQuaternion(Quaterniond())
        set(q) {
            val m30 = local.m30()
            val m31 = local.m31()
            val m32 = local.m32()
            local.rotation(q)
            local.m30(m30)
            local.m31(m31)
            local.m32(m32)
            setWorldNeedsUpdate()
        }

    fun getLocalQuaternion(store: Quaterniond): Quaterniond {
        return local.getNormalizedRotation(store)
    }

    val worldQuaternion: Quaterniond
        get() = getWorldQuaternion(Quaterniond())

    fun getWorldQuaternion(store: Quaterniond): Quaterniond {
        return localToWorld(store)
    }

    fun setLocalTranslation(x: Double, y: Double, z: Double) {
        local.setTranslation(x, y, z)
        setWorldNeedsUpdate()
    }

    fun setLocal(m: Matrix4dc) {
        local.set(m)
        setWorldNeedsUpdate()
    }

    fun setWorld(m: Matrix4dc) {
        setLocal(worldToLocal(Matrix4d(m)))
    }

    fun localToWorld(v: Vector3d): Vector3d {
        return v.mulPosition(getWorld())
    }

    fun localToWorld(v: Vector3dc, store: Vector3d): Vector3d {
        throwOnSame(v, store)
        return store.set(v).mulPosition(getWorld())
    }

    fun localDirectionToWorld(v: Vector3d): Vector3d {
        return v.mulDirection(getWorld())
    }

    fun localDirectionToWorld(v: Vector3dc, store: Vector3d): Vector3d {
        throwOnSame(v, store)
        return store.set(v).mulDirection(getWorld())
    }

    fun localToWorld(m: Matrix4d): Matrix4d {
        return m.mulLocalAffine(getWorld())
    }

    fun localToWorld(m: Matrix4dc, store: Matrix4d): Matrix4d {
        throwOnSame(m, store)
        return getWorld().mulAffine(m, store)
    }

    fun worldToLocal(v: Vector3d): Vector3d {
        return v.mulPosition(Matrix4d(parent!!.getWorld()).invertAffine())
    }

    fun worldToLocal(v: Vector3dc, store: Vector3d): Vector3d {
        throwOnSame(v, store)
        return store.set(v).mulPosition(Matrix4d(parent!!.getWorld()).invertAffine())
    }

    fun worldDirectionToLocal(v: Vector3d): Vector3d {
        return v.mulDirection(Matrix4d(parent!!.getWorld()).invertAffine())
    }

    fun worldDirectionToLocal(v: Vector3dc, store: Vector3d): Vector3d {
        throwOnSame(v, store)
        return store.set(v).mulDirection(Matrix4d(parent!!.getWorld()).invertAffine())
    }

    fun worldToLocal(m: Matrix4d): Matrix4d {
        return m.mulLocalAffine(Matrix4d(parent!!.getWorld()).invertAffine())
    }

    fun worldToLocal(m: Matrix4dc, store: Matrix4d): Matrix4d {
        throwOnSame(m, store)
        return store.set(parent!!.getWorld()).invertAffine().mulAffine(m)
    }

    fun localToWorld(q: Quaterniond?): Quaterniond {
        return localToWorld(Matrix4d().set(q)).getNormalizedRotation(q)
    }

    fun worldToLocal(q: Quaterniond?): Quaterniond {
        return worldToLocal(Matrix4d().set(q)).getNormalizedRotation(q)
    }

    fun forward(): Vector3d {
        return worldQuaternion.transform(Vector3d(1.0, 0.0, 0.0))
    }

    fun left(): Vector3d {
        return worldQuaternion.transform(Vector3d(0.0, 1.0, 0.0))
    }

    fun up(): Vector3d {
        return worldQuaternion.transform(Vector3d(0.0, 0.0, 1.0))
    }

    fun mul(m: Matrix4dc?) {
        local.mulAffine(m)
        setWorldNeedsUpdate()
    }

    fun preMul(m: Matrix4dc?) {
        local.mulLocalAffine(m)
        setWorldNeedsUpdate()
    }

    @JvmOverloads
    fun lookAt(lookAtWorld: Vector3dc, up: Vector3dc = Vector3d(0.0, 0.0, 1.0)) {
        val eye = worldTranslation
        val dir = lookAtWorld.sub(eye, Vector3d()).normalize()
        setWorld(Matrix4d().translationRotateTowards(eye, dir, up))
    }

    fun localTranslateX(x: Double) {
        mul(Matrix4d().setTranslation(x, 0.0, 0.0))
    }

    fun localTranslateY(y: Double) {
        mul(Matrix4d().setTranslation(0.0, y, 0.0))
    }

    fun localTranslateZ(z: Double) {
        mul(Matrix4d().setTranslation(0.0, 0.0, z))
    }

    fun localTranslate(x: Double, y: Double, z: Double) {
        mul(Matrix4d().setTranslation(x, y, z))
    }

    fun localRotateX(angle: Double) {
        mul(Matrix4d().rotate(angle, 1.0, 0.0, 0.0))
    }

    fun localRotateY(angle: Double) {
        mul(Matrix4d().rotate(angle, 0.0, 1.0, 0.0))
    }

    fun localRotateZ(angle: Double) {
        mul(Matrix4d().rotate(angle, 0.0, 0.0, 1.0))
    }

    fun localRotate(angle: Double, x: Double, y: Double, z: Double) {
        mul(Matrix4d().rotate(angle, x, y, z))
    }

    private fun throwOnSame(o1: Any, o2: Any) {
        require(!(o1 === o2)) { "Storage object must be different than input!" }
    }

    override fun toString(): String {
        val w = getWorld()
        val t = w.getTranslation(Vector3d())
        val e = w.getEulerAnglesZYX(Vector3d())
        val DEGREE = "\u00b0"
        return "Transform{" + "translation=" + t + ", rotation=[x=" + Math.toDegrees(e.x) + DEGREE + ", y=" + Math.toDegrees(
            e.y
        ) + DEGREE + ", z=" + Math.toDegrees(e.z) + DEGREE + "]}"
    }
    
}

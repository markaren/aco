package info.laht.aco.components

import info.laht.aco.core.Engine
import info.laht.aco.core.Entity
import info.laht.aco.core.Family
import org.junit.Assert
import org.junit.Test

internal class NameComponentTest {

    @Test
    fun testNameComponent() {

        val name = "myNamedEntity"
        val entity = Entity()
        entity.add(NameComponent(name))

        val engine = Engine()
        engine.addEntity(entity)
        engine.addEntity(Entity())

        val namedEntities = engine.getEntitiesFor(Family.all(NameComponent::class.java).get())
        assert(namedEntities.size() == 1)
        Assert.assertEquals(name, namedEntities[0].getComponent(NameComponent::class.java).name)

    }

}

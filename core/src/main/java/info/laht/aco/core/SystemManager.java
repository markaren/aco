package info.laht.aco.core;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import info.laht.aco.utils.ImmutableArray;

import java.util.Comparator;

class SystemManager {

    private final SystemComparator systemComparator = new SystemComparator();
    private final Array<EntitySystem> systems = new Array<>(true, 16);
    private final ImmutableArray<EntitySystem> immutableSystems = new ImmutableArray<>(systems);
    private final ObjectMap<Class<?>, EntitySystem> systemsByClass = new ObjectMap<>();
    private final SystemListener listener;

    public SystemManager(SystemListener listener) {
        this.listener = listener;
    }

    public void addSystem(EntitySystem system) {
        Class<? extends EntitySystem> systemType = system.getClass();
        EntitySystem oldSystem = getSystem(systemType);

        if (oldSystem != null) {
            removeSystem(oldSystem);
        }

        systems.add(system);
        systemsByClass.put(systemType, system);
        systems.sort(systemComparator);
        listener.systemAdded(system);
    }

    public void removeSystem(EntitySystem system) {
        if (systems.removeValue(system, true)) {
            systemsByClass.remove(system.getClass());
            listener.systemRemoved(system);
        }
    }

    public void removeAllSystems() {
        while (systems.size > 0) {
            removeSystem(systems.first());
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends EntitySystem> T getSystem(Class<T> systemType) {
        return (T) systemsByClass.get(systemType);
    }

    public ImmutableArray<EntitySystem> getSystems() {
        return immutableSystems;
    }

    private static class SystemComparator implements Comparator<EntitySystem> {
        @Override
        public int compare(EntitySystem a, EntitySystem b) {
            return Integer.compare(a.priority, b.priority);
        }
    }

    interface SystemListener {
        void systemAdded(EntitySystem system);

        void systemRemoved(EntitySystem system);
    }

}

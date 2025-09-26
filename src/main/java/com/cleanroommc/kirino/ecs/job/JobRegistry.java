package com.cleanroommc.kirino.ecs.job;

import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.storage.INativeArray;
import com.cleanroommc.kirino.utils.reflection.ReflectionUtils;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class JobRegistry {
    private final Map<Class<? extends IParallelJob>, Map<JobDataQuery, IJobDataInjector>> parallelJobDataQueryMap = new HashMap<>();
    private final Map<Class<? extends IParallelJob>, Map<String, IJobDataInjector>> parallelJobExternalDataQueryMap = new HashMap<>();
    private final Map<Class<? extends IParallelJob>, IJobInstantiator> parallelJobInstantiatorMap = new HashMap<>();

    private final ComponentRegistry componentRegistry;

    public JobRegistry(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    @SuppressWarnings({"unchecked"})
    private <T extends IParallelJob, U> IJobDataInjector genParallelJobDataInjector(Class<T> clazz, String fieldName, Class<U> fieldClass) {
        BiConsumer<T, U> setter = (BiConsumer<T, U>) ReflectionUtils.getFieldSetter(clazz, fieldName);
        Objects.requireNonNull(setter);
        return (owner, value) -> {
            setter.accept((T) owner, (U) value);
        };
    }

    private IJobInstantiator genParallelJobInstantiator(Class<? extends IParallelJob> clazz) {
        MethodHandle ctor = ReflectionUtils.getConstructor(clazz);
        Objects.requireNonNull(ctor);
        return () -> {
            try {
                return ctor.invoke();
            } catch (Throwable e) {
                return null;
            }
        };
    }

    public void registerParallelJob(Class<? extends IParallelJob> clazz) {
        try {
            clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Parallel job class " + clazz.getName() + " is missing a default constructor with no parameters.", e);
        }

        parallelJobInstantiatorMap.computeIfAbsent(clazz, this::genParallelJobInstantiator);

        Map<JobDataQuery, IJobDataInjector> dataQueryMap = parallelJobDataQueryMap.computeIfAbsent(clazz, k -> new HashMap<>());
        Map<String, IJobDataInjector> externalDataQueryMap = parallelJobExternalDataQueryMap.computeIfAbsent(clazz, k -> new HashMap<>());

        String exceptionText = "Parallel job class " + clazz.getName() + " contains invalid annotation entries.";

        for (Field field : clazz.getDeclaredFields()) {
            // scan JobDataQuery
            if (field.isAnnotationPresent(JobDataQuery.class) && !Modifier.isStatic(field.getModifiers())) {
                if (!INativeArray.class.isAssignableFrom(field.getType())) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("INativeArray must be assignable from the JobDataQuery-annotated field " + field.getName() + "."));
                }

                JobDataQuery jobDataQuery = field.getAnnotation(JobDataQuery.class);

                if (!ICleanComponent.class.isAssignableFrom(jobDataQuery.componentClass())) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("ICleanComponent must be assignable from JobDataQuery#componentClass() " + jobDataQuery.componentClass().getName() + "."));
                }
                if (jobDataQuery.componentClass() == ICleanComponent.class) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#componentClass() " + jobDataQuery.componentClass().getName() + " must not be ICleanComponent itself."));
                }
                if (!componentRegistry.componentExists(jobDataQuery.componentClass().asSubclass(ICleanComponent.class))) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#componentClass() " + jobDataQuery.componentClass().getName() + " isn't registered in the component registry."));
                }
                String componentName = componentRegistry.getComponentName(jobDataQuery.componentClass().asSubclass(ICleanComponent.class));
                try {
                    componentRegistry.getFieldOrdinal(componentName, jobDataQuery.fieldAccessChain());
                } catch (Throwable e) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#fieldAccessChain() is invalid.", e));
                }

                IJobDataInjector jobDataInjector = genParallelJobDataInjector(clazz, field.getName(), field.getType());
                dataQueryMap.put(jobDataQuery, jobDataInjector);
            }
            // scan JobExternalDataQuery
            if (field.isAnnotationPresent(JobExternalDataQuery.class) && !Modifier.isStatic(field.getModifiers())) {
                IJobDataInjector jobDataInjector = genParallelJobDataInjector(clazz, field.getName(), field.getType());
                externalDataQueryMap.put(field.getName(), jobDataInjector);
            }
        }
    }

    @Nullable
    public Map<JobDataQuery, IJobDataInjector> getParallelJobDataQueries(Class<? extends IParallelJob> clazz) {
        return parallelJobDataQueryMap.get(clazz);
    }

    @Nullable
    public Map<String, IJobDataInjector> getParallelJobExternalDataQueries(Class<? extends IParallelJob> clazz) {
        return parallelJobExternalDataQueryMap.get(clazz);
    }

    @Nullable
    public IJobInstantiator getParallelJobInstantiator(Class<? extends IParallelJob> clazz) {
        return parallelJobInstantiatorMap.get(clazz);
    }
}

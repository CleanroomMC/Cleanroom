package com.cleanroommc.kirino.ecs.job;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.storage.ArchetypeDataPool;
import com.cleanroommc.kirino.ecs.storage.ArrayRange;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JobScheduler {
    private final JobRegistry jobRegistry;

    public JobScheduler(JobRegistry jobRegistry) {
        this.jobRegistry = jobRegistry;
    }

    public void executeParallel(EntityManager entityManager, Class<? extends IParallelJob> clazz, @Nullable Map<String, Object> externalData, Executor executor) {
        Map<JobDataQuery, IJobDataInjector> parallelJobDataQueries = jobRegistry.getParallelJobDataQueries(clazz);
        Map<String, IJobDataInjector> parallelJobExternalDataQueries = jobRegistry.getParallelJobExternalDataQueries(clazz);
        IJobInstantiator instantiator = jobRegistry.getParallelJobInstantiator(clazz);
        if (parallelJobDataQueries == null || parallelJobExternalDataQueries == null || instantiator == null) {
            throw new IllegalStateException("Parallel job class " + clazz.getName() + " isn't registered.");
        }

        if (!parallelJobExternalDataQueries.isEmpty()) {
            Preconditions.checkArgument(externalData != null,
                    "Argument \"externalData\" must not be null since there are %d external data queries.", parallelJobExternalDataQueries.size());

            for (String key : parallelJobExternalDataQueries.keySet()) {
                Preconditions.checkArgument(externalData.containsKey(key),
                        "Missing the entry \"%s\" from \"externalData\".", key);
            }
        }

        EntityQuery query = entityManager.newQuery();
        ((IParallelJob) instantiator.instantiate()).query(query);
        List<ArchetypeDataPool> archetypes = entityManager.startQuery(query);

        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (ArchetypeDataPool archetype : archetypes) {
            IParallelJob job = (IParallelJob) instantiator.instantiate();

            // data injection
            for (Map.Entry<JobDataQuery, IJobDataInjector> entry : parallelJobDataQueries.entrySet()) {
                IPrimitiveArray array = archetype.getArray(entry.getKey().componentClass().asSubclass(ICleanComponent.class), entry.getKey().fieldAccessChain());
                entry.getValue().inject(job, array);
            }
            if (externalData != null) {
                for (Map.Entry<String, IJobDataInjector> entry : parallelJobExternalDataQueries.entrySet()) {
                    entry.getValue().inject(job, externalData.get(entry.getKey()));
                }
            }

            ArrayRange arrayRange = archetype.getArrayRange();

            // todo: configurable & dynamic
            int futureCount = 16;
            int step = (arrayRange.end - arrayRange.start) / futureCount;

            for (int i = 0; i < futureCount; i++) {
                int finalI = i;
                futures.add(CompletableFuture.runAsync(() -> {
                    int start = arrayRange.start + finalI * step;
                    int end = (finalI == futureCount - 1) ? arrayRange.end : arrayRange.start + (finalI + 1) * step;
                    for (int j = start; j < end; j++) {
                        if (arrayRange.deprecatedIndexes.contains(j)) {
                            continue;
                        }
                        job.execute(entityManager, j);
                    }
                }, executor));
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}

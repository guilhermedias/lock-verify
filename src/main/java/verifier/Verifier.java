package verifier;

import system.System;
import system.process.Process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Verifier {
    public static boolean causesDeadlock(List<UUID> executionOrder, System system) {
        List<UUID> systemProcesses =
                system.getProcesses().stream()
                        .map(process -> process.getProcessId())
                        .collect(Collectors.toList());

        if(!executionOrderListMatchesSystemProcesses(executionOrder, systemProcesses)) {
            throw new IllegalArgumentException();
        }

        Map<UUID, Set<UUID>> orderRestriction = generateOrderRestriction(system);

        final boolean[] causesDeadlock = {false};
        orderRestriction.forEach((process, dependencies) -> {
            dependencies.forEach(dependency -> {
                   if(!compliesWithOrderRestriction(process, dependency, executionOrder)) {
                       causesDeadlock[0] = true;
                   }
            });
        });

        return causesDeadlock[0];
    }

    private static boolean executionOrderListMatchesSystemProcesses(List<UUID> executionOrder, List<UUID> systemProcesses) {
        return systemProcesses.containsAll(executionOrder) && executionOrder.containsAll(systemProcesses);
    }

    private static Map<UUID, Set<UUID>> generateOrderRestriction(System system) {
        return system.getProcesses().stream()
                .map(process -> {
                    HashMap<UUID, Set<UUID>> dependenciesMap = new HashMap<>();
                    dependenciesMap.put(process.getProcessId(), getAllDependencies(process));
                    return dependenciesMap;
                })
                .reduce(new HashMap<>(), (finalMap, processMap) -> {
                    HashMap<UUID, Set<UUID>> mergeMap= new HashMap<>(finalMap);
                    mergeMap.putAll(processMap);
                    return mergeMap;
                });
    }

    private static Set<UUID> getAllDependencies(Process<?> process) {
        Set<UUID> allDependencies =
                process.getDependencies().stream()
                        .map(p -> p.getProcessId())
                        .collect(Collectors.toSet());

        process.getDependencies().forEach(p -> allDependencies.addAll(getAllDependencies(p)));

        return allDependencies;
    }

    private static boolean compliesWithOrderRestriction(UUID process, UUID dependency, List<UUID> executionOrder) {
        return executionOrder.indexOf(dependency) < executionOrder.indexOf(process);
    }
}

package verifier;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import system.System;
import system.process.Process;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static verifier.Verifier.causesDeadlock;

public class VerifierTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private UUID processId1;
    private UUID processId2;
    private System process2DependsOnProcess1System;

    @Before
    public void setUp() throws Exception {
        processId1 = UUID.randomUUID();
        processId2 = UUID.randomUUID();
        process2DependsOnProcess1System = systemWhereProcess2DependsOnProcess1(processId1, processId2);
    }

    @Test
    public void shouldThrowExceptionWhenExecutionOrderListDoesNotMatchSystemProcesses() {
        List<UUID> executionOrder = emptyList();

        exception.expect(IllegalArgumentException.class);

        causesDeadlock(executionOrder, process2DependsOnProcess1System);
    }

    @Test
    public void shouldReturnFalseWhenExecutionOrderDoesNotCauseDeadlock() {
        List<UUID> executionOrder = asList(processId1, processId2);

        boolean causesDeadlock = causesDeadlock(executionOrder, process2DependsOnProcess1System);

        assertThat(causesDeadlock, is(false));
    }

    @Test
    public void shouldReturnTrueWhenExecutionOrderCausesDeadlock() {
        List<UUID> executionOrder = asList(processId2, processId1);

        boolean causesDeadlock = causesDeadlock(executionOrder, process2DependsOnProcess1System);

        assertThat(causesDeadlock, is(true));
    }

    private Process processWithIdAndDependency(UUID processId, List<Process<?>> dependencies) {
        Process<?> mockedProcess = mock(Process.class);
        when(mockedProcess.getProcessId()).thenReturn(processId);
        when(mockedProcess.getDependencies()).thenReturn(dependencies);
        return mockedProcess;
    }

    private System systemWithProcesses(Process<?>... processes) {
        System mockedSystem = mock(System.class);
        when(mockedSystem.getProcesses()).thenReturn(asList(processes));
        return mockedSystem;
    }

    private System systemWhereProcess2DependsOnProcess1(UUID processId1, UUID processId2) {
        Process<?> process1 = processWithIdAndDependency(processId1, Collections.<Process<?>>emptyList());
        Process<?> process2 = processWithIdAndDependency(processId2, Arrays.<Process<?>>asList(process1));
        return systemWithProcesses(process1, process2);
    }
}
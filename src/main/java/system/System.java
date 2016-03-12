package system;

import system.process.Process;

import java.util.List;

public interface System {
    List<Process<?>> getProcesses();
}

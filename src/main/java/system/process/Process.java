package system.process;

import java.util.List;
import java.util.UUID;

public interface Process<T> {
    UUID getProcessId();
    List<Process<?>> getDependencies();
}

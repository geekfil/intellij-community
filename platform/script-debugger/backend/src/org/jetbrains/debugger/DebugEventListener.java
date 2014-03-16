package org.jetbrains.debugger;

import com.intellij.util.Url;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EventListener;

/**
 * This interface is used by the SDK to report debug events for a certain {@link Vm} to
 * the clients.
 */
public interface DebugEventListener extends EventListener {
  /**
   * Reports the virtual machine has suspended (on hitting
   * breakpoints or a step end). The {@code context} can be used to access the
   * current backtrace.
   */
  void suspended(@NotNull SuspendContext context);

  /**
   * Reports the virtual machine has resumed. This can happen
   * asynchronously, due to a user action in the browser (without explicitly
   * resuming the VM through
   */
  void resumed();

  /**
   * Reports the debug connection has terminated and {@link Vm} has stopped operating.
   * This event is not reported if connection was closed explicitly on our side
   */
  void disconnected();

  /**
   * Reports that a new script has been loaded into a tab.
   *
   * @param script loaded into the tab
   */
  void scriptLoaded(@NotNull Script script, @Nullable String sourceMapData);

  void sourceMapFound(@NotNull Script script, @Nullable Url sourceMapUrl, @NotNull String sourceMapData);

  /**
   * Reports that the script has been collected and is no longer used in VM.
   */
  void scriptCollected(Script script);

  void scriptsCleared();

  /**
   * Reports that script source has been altered in remote VM.
   */
  void scriptContentChanged(Script newScript);
}
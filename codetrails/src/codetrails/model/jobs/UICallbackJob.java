package codetrails.model.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

import codetrails.model.ViewContentProvider;

public class UICallbackJob extends UIJob
{
  private final ViewContentProvider contentProvider;

  public UICallbackJob(ViewContentProvider contentProvider)
  {
    super("Code Trails UI Updater");
    this.contentProvider = contentProvider;
  }

  @Override
  public IStatus runInUIThread(IProgressMonitor monitor)
  {
    contentProvider.notifyContentChange();
    return Status.OK_STATUS;
  }
}
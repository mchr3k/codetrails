package codetrails.model.jobs;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import codetrails.data.ProjectData;
import codetrails.data.Trail;
import codetrails.model.MarkerUpdate;

public class IncrementalUpdateJob extends Job
{
  private final BlockingQueue<MarkerUpdate> updates = new LinkedBlockingQueue<MarkerUpdate>();

  private final Map<IProject,ProjectData> workingData;

  private static final ISchedulingRule SINGLETON_RULE = new ISchedulingRule()
  {
    @Override
    public boolean isConflicting(ISchedulingRule rule)
    {
      return (SINGLETON_RULE == rule);
    }

    @Override
    public boolean contains(ISchedulingRule rule)
    {
      return (SINGLETON_RULE == rule);
    }
  };

  private UICallbackJob uiCallbackJob;

  public IncrementalUpdateJob(Map<IProject, ProjectData> workingData, UICallbackJob uiCallbackJob)
  {
    super("Code Trails Incremental Background Updater");
    this.workingData = workingData;
    setSystem(true);
    setRule(SINGLETON_RULE);
    this.uiCallbackJob = uiCallbackJob;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor)
  {
    while (updates.size() > 0)
    {
      if (monitor.isCanceled())
        return Status.CANCEL_STATUS;

      MarkerUpdate markerUpdate = updates.poll();
      if (markerUpdate != null)
      {
        processUpdate(markerUpdate);
      }
    }

    uiCallbackJob.schedule();

    return Status.OK_STATUS;
  }

  private void processUpdate(MarkerUpdate markerUpdate)
  {
    // System.out.println("Incremental Update...");
    for (IMarkerDelta markerDelta : markerUpdate.removed)
    {
      IResource resource = markerDelta.getResource();

      if (resource != null)
      {
        IProject project = resource.getProject();
        ProjectData working = workingData.get(project);

        if (working != null)
        {
          IMarker marker = markerDelta.getMarker();
          String message = (String)markerDelta.getAttribute("message");

          if (message.startsWith(Trail.TRAIL_TASK_TAG))
          {
            // System.out.println("Removing: " + message);
            working.remove(marker, message);
          }
        }
      }
    }

    for (IMarkerDelta markerDelta : markerUpdate.added)
    {
      IResource resource = markerDelta.getResource();

      if (resource != null)
      {
        IProject project = resource.getProject();
        ProjectData working = workingData.get(project);

        if (working == null)
        {
          working = new ProjectData(project);
          workingData.put(project, working);
        }

        IMarker marker = markerDelta.getMarker();
        String message = (String)markerDelta.getAttribute("message");

        if (message.startsWith(Trail.TRAIL_TASK_TAG))
        {
          // System.out.println("Adding: " + message);
          working.put(marker, message);
        }
      }
    }
  }

  public void add(MarkerUpdate update)
  {
    // System.out.println("Schedule incremental update...");
    updates.add(update);
    schedule();
  }
}

package codetrails.model.jobs;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.statushandlers.StatusManager;

import codetrails.Activator;
import codetrails.data.ProjectData;
import codetrails.data.Trail;

public class FullUpdateJob extends Job
{
  private final BlockingQueue<IProject> updates = new LinkedBlockingQueue<IProject>();
  private final UICallbackJob uiCallbackJob;
  private final Map<IProject, ProjectData> workingData;

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

  public FullUpdateJob(Map<IProject, ProjectData> workingData, UICallbackJob uiCallbackJob)
  {
    super("Code Trails Full Scan Background Updater");
    this.workingData = workingData;
    setSystem(true);
    setRule(SINGLETON_RULE);
    this.uiCallbackJob = uiCallbackJob;
  }

  public void add(IProject project)
  {
    // System.out.println("Schedule full update: " + project + " ...");
    updates.add(project);
    schedule(250);
  }

  @Override
  protected IStatus run(IProgressMonitor monitor)
  {
    while (updates.size() > 0)
    {
      if (monitor.isCanceled())
        return Status.CANCEL_STATUS;

      IProject project = updates.poll();
      if (project != null)
      {
        processProject(project);
      }
    }

    uiCallbackJob.schedule();

    return Status.OK_STATUS;
  }

  private void processProject(IProject project)
  {
    // System.out.println("Full scan: " + project);
    try
    {
      IMarker[] markers = project.findMarkers(IMarker.TASK, true, IResource.DEPTH_INFINITE);

      for (IMarker marker : markers)
      {
        IResource resource = marker.getResource();

        if (resource != null)
        {
          ProjectData working = workingData.get(project);

          if (working == null)
          {
            working = new ProjectData(project);
            workingData.put(project, working);
          }

          String message = (String)marker.getAttribute("message");
          // System.out.println("Found: " + message);

          if (message.startsWith(Trail.TRAIL_TASK_TAG))
          {
            working.put(marker, message);
          }
        }
      }
    }
    catch (CoreException e)
    {
      StatusManager.getManager().handle(e, Activator.PLUGIN_ID);
    }
  }
}
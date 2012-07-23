package codetrails.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;

import codetrails.model.jobs.IncrementalUpdateJob;


public class UpdateListener implements IResourceChangeListener
{
  private final IncrementalUpdateJob job;

  public UpdateListener(IncrementalUpdateJob updateJob)
  {
    job = updateJob;
  }

  @Override
  public void resourceChanged(IResourceChangeEvent event)
  {
    IMarkerDelta[] markerDeltas = event.findMarkerDeltas(IMarker.TASK, true);

    /*
     * Identify added/changed markers
     */
    List<IMarkerDelta> removed = new ArrayList<IMarkerDelta>();
    List<IMarkerDelta> added = new ArrayList<IMarkerDelta>();
    for (IMarkerDelta markerDelta : markerDeltas)
    {
      if (markerDelta.getKind() == IResourceDelta.ADDED)
      {
        added.add(markerDelta);
      }
      else if (markerDelta.getKind() == IResourceDelta.REMOVED)
      {
        removed.add(markerDelta);
      }
    }

    /*
     * Add update to job
     */
    MarkerUpdate update = new MarkerUpdate(added, removed);
    job.add(update);
  }

  /**
   * Start listening for changes.
   */
  synchronized void start()
  {
    ResourcesPlugin.getWorkspace().addResourceChangeListener(
        this,
        IResourceChangeEvent.POST_CHANGE);
    job.schedule();
  }

  /**
   * Stop listening for changes.
   */
  synchronized void stop()
  {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
  }
}

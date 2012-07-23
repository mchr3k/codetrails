package codetrails.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;

public class ProjectData
{
  private final Map<String, Trail> trails = new ConcurrentHashMap<String, Trail>();
  private final IProject project;

  public ProjectData(IProject project)
  {
    this.project = project;
  }

  public void remove(IMarker marker, String message)
  {
    try
    {
      TrailEntry entry = new TrailEntry(message, marker);

      Trail trail = trails.get(entry.trailtag);
      if (trail != null)
      {
        trail.remove(marker);

        if (trail.size() == 0)
        {
          trails.remove(entry.trailtag);
        }
      }
    }
    catch (IllegalArgumentException ex)
    {
      // Ignore
    }
  }

  public void put(IMarker marker, String message)
  {
    try
    {
      TrailEntry entry = new TrailEntry(message, marker);

      Trail trail = trails.get(entry.trailtag);
      if (trail == null)
      {
        trail = new Trail(this, entry.trailtag);
        trails.put(entry.trailtag, trail);
      }

      trail.put(marker, entry);
    }
    catch (IllegalArgumentException ex)
    {
      // Ignore
    }
  }

  @Override
  public String toString()
  {
    return trails.toString();
  }

  public Trail getTrail(String tag)
  {
    return trails.get(tag);
  }

  public Trail[] getTrails()
  {
    Collection<Trail> values = trails.values();
    List<Trail> sorted = new ArrayList<Trail>(values);

    Collections.sort(sorted, new Comparator<Trail>()
    {
      @Override
      public int compare(Trail o1, Trail o2)
      {
        return o1.getTag().compareTo(o2.getTag());
      }
    });

    return sorted.toArray(new Trail[sorted.size()]);
  }

  public boolean hasTrails()
  {
    return trails.size() > 0;
  }

  public String getName()
  {
    return project.getName();
  }

  @Override
  public int hashCode()
  {
    return project.getName().hashCode();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof ProjectData)
    {
      ProjectData projectData = (ProjectData) obj;
      return projectData.project.getName().equals(projectData.project.getName());
    }
    return (obj == this);
  }

  public ProjectData copy()
  {
    return copy(null);
  }

  public ProjectData copy(ProjectData olddata)
  {
    ProjectData copy = new ProjectData(project);

    for (Entry<String,Trail> entry : trails.entrySet())
    {
      Trail trail = entry.getValue();
      Trail oldtrail = null;
      if (olddata != null)
      {
        oldtrail = olddata.trails.get(trail.getTag());
      }
      Trail trailCopy = trail.copy(copy, oldtrail);
      copy.trails.put(entry.getKey(), trailCopy);
    }

    return copy;
  }
}

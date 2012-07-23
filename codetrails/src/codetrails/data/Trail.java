package codetrails.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IMarker;

public class Trail
{
  public static final String TRAIL_TASK_TAG = "TRAIL";

  private final String tag;
  private final Map<IMarker, TrailEntry> markers = new ConcurrentHashMap<IMarker, TrailEntry>();

  private final ProjectData parent;

  public Trail(ProjectData parent, String tag)
  {
    this.parent = parent;
    this.tag = tag;
  }

  public Trail(ProjectData parent, String tag, Map<IMarker, TrailEntry> markers, Trail oldtrail)
  {
    this.parent = parent;
    this.tag = tag;
    if (oldtrail == null)
    {
      // System.out.println("Copying up to date Trail: " + tag);
      this.markers.putAll(markers);
    }
    else
    {
     // System.out.println("Copying Trail without index updates: " + tag);

      Map<String, TrailEntry> newentries = new HashMap<String, TrailEntry>();
      for (TrailEntry newentry : markers.values())
      {
        String key = newentry.marker.getResource().toString() + ":" + newentry.tagoffset;
        newentries.put(key, newentry);
      }
      Map<String, TrailEntry> oldentries = new HashMap<String, TrailEntry>();
      for (TrailEntry oldentry : oldtrail.markers.values())
      {
        String key = oldentry.marker.getResource().toString() + ":" + oldentry.tagoffset;
        oldentries.put(key, oldentry);
      }

      for (Entry<String, TrailEntry> entry : oldentries.entrySet())
      {
        String key = entry.getKey();
        TrailEntry oldentry = entry.getValue();
        TrailEntry newentry = newentries.remove(key);
        if (newentry != null)
        {
          TrailEntry combined = new TrailEntry(newentry, oldentry.index);
          this.markers.put(combined.marker, combined);
        }
      }

      for (TrailEntry entry : newentries.values())
      {
        this.markers.put(entry.marker, entry);
      }
    }
  }

  public void put(IMarker marker, TrailEntry entry)
  {
    entry.setParent(this);
    markers.put(marker, entry);
  }

  public void remove(IMarker marker)
  {
    markers.remove(marker);
  }

  @Override
  public String toString()
  {
    return Arrays.toString(getMarkers());
  }

  public Trail copy(ProjectData parent, Trail trail)
  {
    return new Trail(parent, tag, markers, trail);
  }

  public int size()
  {
    return markers.size();
  }

  public TrailEntry[] getMarkers()
  {
    Collection<TrailEntry> values = markers.values();
    List<TrailEntry> sorted = new ArrayList<TrailEntry>(values);
    Collections.sort(sorted, new Comparator<TrailEntry>()
    {
      @Override
      public int compare(TrailEntry o1, TrailEntry o2)
      {
        int index1 = o1.index;
        int index2 = o2.index;

        if ((index1 > -1) && (index2 == -1))
          return -1;

        if ((index1 == -1) && (index2 > -1))
          return 1;

        if ((index1 == -1) && (index2 == -1))
        {
          if (!o1.text.equals(o2.text))
            return o1.text.compareTo(o2.text);
          else
            return (System.identityHashCode(o1.text) < System.identityHashCode(o2.text) ? -1 : 1);
        }

        if (o1.index != o2.index)
          return (o1.index < o2.index ? -1 : 1);
        else if (!o1.text.equals(o2.text))
          return o1.text.compareTo(o2.text);
        else
          return (System.identityHashCode(o1.text) < System.identityHashCode(o2.text) ? -1 : 1);
      }
    });
    return sorted.toArray(new TrailEntry[sorted.size()]);
  }

  public boolean hasMarkers()
  {
    return (markers.size() > 0);
  }

  public String getTag()
  {
    return tag;
  }

  @Override
  public int hashCode()
  {
    return tag.hashCode();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof Trail)
    {
      Trail trail = (Trail) obj;
      return trail.tag.equals(tag);
    }

    return super.equals(obj);
  }

  public ProjectData getParent()
  {
    return parent;
  }
}

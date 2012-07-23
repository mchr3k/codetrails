package codetrails.data;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

public class TrailEntry
{
  public final String trailtag;
  public final String text;
  public final int index;
  public final IMarker marker;
  public final int tagoffset;
  public final int taglength;
  public final int indexlength;
  private Trail parent;

  public TrailEntry(TrailEntry entry, int newindex)
  {
    this.trailtag = entry.trailtag;
    this.text = entry.text;
    this.marker = entry.marker;
    this.tagoffset = entry.tagoffset;
    this.taglength = entry.taglength;
    this.index = newindex;
    this.indexlength = Integer.toString(newindex).length();
    this.parent = entry.parent;
  }

  public TrailEntry(String markertext, IMarker marker)
  {
    this.marker = marker;

    // markertext is in the following format
    // <tasktag> [trailtag(:index)?] text
    int firstSpace = markertext.indexOf(' ');
    if (firstSpace == -1)
      throw new IllegalArgumentException("Need first space");

    int firstCloseBracket = markertext.indexOf(']', firstSpace + 1);
    if (firstCloseBracket == -1)
      throw new IllegalArgumentException("Need close bracket");

    String fulltrailtext = markertext.substring(firstSpace + 1, firstCloseBracket + 1);
    if (marker.exists())
    {
      try
      {
        Integer charStart = (Integer) marker.getAttribute(IMarker.CHAR_START);
        if (charStart == null)
          throw new IllegalArgumentException("no charStart");

        tagoffset = charStart + firstSpace + 2;
      }
      catch (CoreException e)
      {
        throw new IllegalArgumentException(e);
      }
    }
    else
    {
      tagoffset = -1;
    }

    int openBracket = fulltrailtext.indexOf('[');
    int closeBracket = fulltrailtext.indexOf(']');
    if ((openBracket != 0) && (closeBracket != fulltrailtext.length() - 1))
      throw new IllegalArgumentException("Improper bracketing");

    String trailtextwithpossibleindex = fulltrailtext.substring(1, fulltrailtext.length() - 1);

    int colon = trailtextwithpossibleindex.indexOf(':');
    if (colon != -1)
    {
      String[] parts = trailtextwithpossibleindex.split(":");
      trailtag = parts[0];
      index = Integer.parseInt(parts[1]);
      indexlength = parts[1].length();
    }
    else
    {
      trailtag = trailtextwithpossibleindex;
      index = -1;
      indexlength = -1;
    }
    taglength = trailtag.length();

    text = markertext.substring(firstCloseBracket + 2);
  }

  @Override
  public String toString()
  {
    return ((index > -1) ? index + ":" : "") + text;
  }

  public IMarker getMarker()
  {
    return marker;
  }

  public void setParent(Trail parent)
  {
    this.parent = parent;
  }

  public Trail getParent()
  {
    return parent;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof TrailEntry)
    {
      TrailEntry entry = (TrailEntry) obj;
      return ((tagoffset == entry.tagoffset) &&
              (marker.getResource().equals(entry.marker.getResource())));
    }

    return false;
  }

  @Override
  public int hashCode()
  {
    return marker.hashCode();
  }
}
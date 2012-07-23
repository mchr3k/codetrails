package codetrails.model;

import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;

public class MarkerUpdate
{
  public final List<IMarkerDelta> removed;
  public final List<IMarkerDelta> added;

  public MarkerUpdate(List<IMarkerDelta> added, List<IMarkerDelta> removed)
  {
    this.added = added;
    this.removed = removed;
  }
}
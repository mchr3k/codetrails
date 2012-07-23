package codetrails.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import codetrails.data.Trail;
import codetrails.data.TrailEntry;

public class RenameTrailProcessor extends Refactoring
{
  public static class Info
  {
    public final Trail trail;
    public String newname;

    public Info(Trail trail)
    {
      this.trail = trail;
    }

    public String getOldName()
    {
      return trail.getTag();
    }
  }

  private final Info info;

  public RenameTrailProcessor(Info info)
  {
    this.info = info;
  }

  @Override
  public String getName()
  {
    return "Rename Trail";
  }

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
      throws CoreException, OperationCanceledException
  {
    // Nothing to check
    return new RefactoringStatus();
  }

  @Override
  public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
      throws CoreException, OperationCanceledException
  {
    // Nothing to check
    return new RefactoringStatus();
  }

  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException,
      OperationCanceledException
  {
    Map<IFile, List<ReplaceEdit>> changesPerFile = new HashMap<IFile, List<ReplaceEdit>>();

    // Compute changes which need to be made
    for (TrailEntry entry : info.trail.getMarkers())
    {
      IResource resource = entry.getMarker().getResource();

      if (resource instanceof IFile)
      {
        IFile file = (IFile) resource;
        List<ReplaceEdit> list = changesPerFile.get(file);
        if (list == null)
        {
          list = new ArrayList<ReplaceEdit>();
          changesPerFile.put(file, list);
        }
        ReplaceEdit edit = new ReplaceEdit(entry.tagoffset, entry.taglength, info.newname);
        list.add(edit);
      }
    }

    CompositeChange change = new CompositeChange("Rename Trail: " + info.getOldName() + " -> " + info.newname);

    for (Entry<IFile, List<ReplaceEdit>> entry : changesPerFile.entrySet())
    {
      IFile file = entry.getKey();
      List<ReplaceEdit> edits = entry.getValue();

      TextFileChange filechange = new TextFileChange("Rename Trail", file);
      filechange.setEdit(new MultiTextEdit());

      for (ReplaceEdit edit : edits)
      {
        filechange.addEdit(edit);
      }

      change.add(filechange);
    }

    return change;
  }

}

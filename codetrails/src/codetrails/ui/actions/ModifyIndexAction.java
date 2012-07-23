package codetrails.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.statushandlers.StatusManager;

import codetrails.Activator;
import codetrails.data.TrailEntry;
import codetrails.model.ViewContentProvider;
import codetrails.ui.View.Direction;

public class ModifyIndexAction extends Action
{
  private final ISelectionProvider selectionProvider;
  private final Direction direction;
  private final ViewContentProvider contentProvider;

  public ModifyIndexAction(ISelectionProvider selectionProvider, ViewContentProvider contentProvider, Direction direction)
  {
    this.selectionProvider = selectionProvider;
    this.contentProvider = contentProvider;
    this.direction = direction;
  }

  @Override
  public String getText()
  {
    if (direction == Direction.UP)
      return "Up";
    else
      return "Down";
  }

  @Override
  public void run()
  {
    ISelection selection = selectionProvider.getSelection();
    if (selection instanceof IStructuredSelection)
    {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      Object element = structuredSelection.getFirstElement();

      if (element instanceof TrailEntry)
      {
        TrailEntry entry = (TrailEntry) element;
        IMarker marker = entry.getMarker();

        if ((direction == Direction.DOWN) || (entry.index > 0))
        {
          IResource resource = marker.getResource();

          if (resource instanceof IFile)
          {
            IFile file = (IFile) resource;
            modifyFile(file, entry);
          }
        }
      }
    }
  }

  private void modifyFile(IFile file, TrailEntry entry)
  {
    int newindex = entry.index;
    if (direction == Direction.UP)
    {
      newindex--;
    }
    else
    {
      newindex++;
    }

    ReplaceEdit edit = new ReplaceEdit(entry.tagoffset + entry.taglength + 1,
                                       entry.indexlength, Integer.toString(newindex));

    TextFileChange change = new TextFileChange("Change Trail Index", file);
    change.setEdit(edit);
    try
    {
      TrailEntry newEntry = new TrailEntry(entry, newindex);
      contentProvider.replace(entry, newEntry);

      change.perform(new NullProgressMonitor());
    }
    catch (CoreException e)
    {
      StatusManager.getManager().handle(e, Activator.PLUGIN_ID);
    }
  }
}

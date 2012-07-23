package codetrails.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

import codetrails.data.ProjectData;
import codetrails.data.Trail;
import codetrails.data.TrailEntry;
import codetrails.model.jobs.FullUpdateJob;
import codetrails.model.jobs.IncrementalUpdateJob;
import codetrails.model.jobs.UICallbackJob;

public class ViewContentProvider implements ITreeContentProvider, ILabelProvider
{
  public static final Object LOADING_INPUT = new Object();

  private final Map<IProject,ProjectData> workingData = new ConcurrentHashMap<IProject, ProjectData>();
  private final Set<IProject> fullUpdates = new HashSet<IProject>();
  private ProjectData data;

  public final IncrementalUpdateJob incrementalupdateJob;
  private final UpdateListener updateListener;
  private final FullUpdateJob fullupdateJob;

  private Viewer viewer;

  private boolean showNumbers = false;
  private final AtomicBoolean acceptIndexChanges = new AtomicBoolean(true);

  public ViewContentProvider()
  {
    UICallbackJob callbackJob = new UICallbackJob(this);
    incrementalupdateJob = new IncrementalUpdateJob(workingData, callbackJob);
    fullupdateJob = new FullUpdateJob(workingData, callbackJob);
    updateListener = new UpdateListener(incrementalupdateJob);
    updateListener.start();
  }

  @Override
  public void dispose()
  {
    incrementalupdateJob.cancel();
    fullupdateJob.cancel();
    updateListener.stop();
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    this.viewer = viewer;
  }

  @Override
  public Object[] getElements(Object inputElement)
  {
    if (inputElement == null)
      return new Object[0];

    if (inputElement instanceof IProject)
    {
      IProject project = (IProject) inputElement;

      ProjectData olddata = data;
      data = workingData.get(project);
      if (data != null)
      {
        if (acceptIndexChanges.get())
          data = data.copy();
        else
          data = data.copy(olddata);
      }

      // If we haven't yet fetched all of the markers
      // for this project schedule this now
      if (!fullUpdates.contains(project))
      {
        fullUpdates.add(project);
        fullupdateJob.add(project);
        fullupdateJob.schedule();
      }
    }

    if (data != null)
      return data.getTrails();

    return new Object[0];
  }

  @Override
  public Object[] getChildren(Object parentElement)
  {
    if (data == null)
      return new Object[0];

    if (parentElement instanceof Trail)
      return ((Trail)parentElement).getMarkers();

    return new Object[0];
  }

  @Override
  public Object getParent(Object element)
  {
    return null;
  }

  @Override
  public boolean hasChildren(Object element)
  {
    if (element == null)
      return false;

    if (element instanceof ProjectData)
      return ((ProjectData) element).hasTrails();

    if (element instanceof Trail)
      return ((Trail)element).hasMarkers();

    return false;
  }

  @Override
  public void addListener(ILabelProviderListener listener)
  {
  }

  @Override
  public boolean isLabelProperty(Object element, String property)
  {
    return false;
  }

  @Override
  public void removeListener(ILabelProviderListener listener)
  {
  }

  @Override
  public Image getImage(Object element)
  {
    if (element instanceof Trail)
      return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OPEN_MARKER);

    if (element instanceof TrailEntry)
      return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);

    return null;
  }

  @Override
  public String getText(Object element)
  {
    if (element instanceof ProjectData)
      return ((ProjectData)element).getName();

    if (element instanceof Trail)
      return ((Trail)element).getTag();

    if (element instanceof TrailEntry)
    {
      TrailEntry entry = (TrailEntry) element;
      if (showNumbers)
      {
        StringBuilder str = new StringBuilder();
        if (entry.index > -1)
        {
          str.append(Integer.toString(entry.index));
          str.append(": ");
        }
        str.append(entry.text);
        return str.toString();
      }
      else
        return entry.text;
    }

    return "unknown";
  }

  public void notifyContentChange()
  {
    if (!viewer.getControl().isDisposed())
    {
      if (data != null)
      {
        ISelection selection = viewer.getSelection();
        viewer.refresh();
        viewer.setSelection(selection);
      }
      else
      {
        viewer.setInput(viewer.getInput());
      }
    }
  }

  public void refresh()
  {
    Object input = viewer.getInput();
    if (input instanceof IProject)
    {
      IProject project = (IProject) input;
      fullupdateJob.add(project);
      fullupdateJob.schedule();
    }
  }

  public void setShowNumbers(boolean checked)
  {
    showNumbers = checked;
    viewer.refresh();
  }

  public void replace(TrailEntry entry, TrailEntry newEntry)
  {
    acceptIndexChanges.set(false);

    // Update Trail in main data
    Trail parent = entry.getParent();
    parent.remove(entry.marker);
    parent.put(newEntry.marker, newEntry);

    // Update Trail in UI
    Trail uitrail = data.getTrail(parent.getTag());
    uitrail.remove(entry.marker);
    uitrail.put(newEntry.marker, newEntry);

    viewer.refresh();
  }

  public void acceptIndexChanges()
  {
    acceptIndexChanges.set(true);
  }
}

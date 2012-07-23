package codetrails.ui.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.ide.IDE;

import codetrails.data.Trail;
import codetrails.ui.actions.RenameTrailProcessor.Info;

public class RenameTrailAction extends Action
{
  private final Trail trail;
  private final IShellProvider shellProvider;

  public RenameTrailAction(IShellProvider shellProvider, Trail trail)
  {
    super("Rename Trail");
    this.shellProvider = shellProvider;
    this.trail = trail;
  }

  @Override
  public void run()
  {
    if(saveAll())
    {
      openWizard();
    }
  }

  private static boolean saveAll()
  {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    return IDE.saveAllEditors(new IResource[] { workspaceRoot }, false);
  }

  private void openWizard()
  {
    Info info = new Info(trail);
    Refactoring processor = new RenameTrailProcessor(info);
    RenameTrailWizard wizard = new RenameTrailWizard(processor, info);
    RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
        wizard);
    try
    {
      String titleForFailedChecks = ""; //$NON-NLS-1$
      op.run(shellProvider.getShell(), titleForFailedChecks);
    }
    catch (final InterruptedException irex)
    {
      // operation was cancelled
    }
  }

}

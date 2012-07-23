package codetrails.ui.actions;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import codetrails.data.Trail;
import codetrails.ui.actions.RenameTrailProcessor.Info;

public class RenameTrailWizard extends RefactoringWizard
{
  private final Info info;

  public RenameTrailWizard(Refactoring processor, Info info)
  {
    super(processor, DIALOG_BASED_USER_INTERFACE);
    this.info = info;
  }

  @Override
  protected void addUserInputPages()
  {
    addPage(new RenameTrailInputPage());
  }

  private class RenameTrailInputPage extends UserInputWizardPage
  {

    private Text txtNewName;



    public RenameTrailInputPage()
    {
      super(RenameTrailInputPage.class.getName());
    }

    // interface methods of UserInputWizardPage
    // /////////////////////////////////////////

    @Override
    public void createControl(final Composite parent)
    {
      Composite composite = createRootComposite(parent);
      setControl(composite);

      createLblNewName(composite);
      createTxtNewName(composite);

      validate();
    }

    // UI creation methods
    // ////////////////////

    private Composite createRootComposite(final Composite parent)
    {
      Composite result = new Composite(parent, SWT.NONE);
      GridLayout gridLayout = new GridLayout(2, false);
      gridLayout.marginWidth = 10;
      gridLayout.marginHeight = 10;
      result.setLayout(gridLayout);
      initializeDialogUnits(result);
      Dialog.applyDialogFont(result);
      return result;
    }

    private void createLblNewName(final Composite composite)
    {
      Label lblNewName = new Label(composite, SWT.NONE);
      lblNewName.setText("Enter new Trail name");
    }

    private void createTxtNewName(Composite composite)
    {
      txtNewName = new Text(composite, SWT.BORDER);
      txtNewName.setText(info.getOldName());
      txtNewName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      txtNewName.selectAll();
      txtNewName.addKeyListener(new KeyAdapter()
      {
        @Override
        public void keyReleased(final KeyEvent e)
        {
          info.newname = txtNewName.getText();
          validate();
        }
      });
    }

    // helping methods
    // ////////////////
    private void validate()
    {
      String txt = txtNewName.getText();

      boolean valid = true;
      if (txt.length() == 0)
      {
        valid = false;
        setErrorMessage(null);
      }
      else if (txt.equals(info.getOldName()))
      {
        valid = false;
        setErrorMessage("Enter a new Trail name");
      }
      else
      {
        for (Trail trail : info.trail.getParent().getTrails())
        {
          if (txt.equals(trail.getTag()))
          {
            valid = false;
            setErrorMessage("Another Trail is already using this name");
            break;
          }
        }
      }

      if (valid)
        setErrorMessage(null);

      setPageComplete(valid);
    }
  }
}

package codetrails;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * The images provided by the debug plugin.
 */
public class PluginImages
{
  public static final String REFRESH_ICON = "REFRESH_ICON";
  public static final String EXPAND_ALL_ICON = "EXPAND_ALL_ICON";
  public static final String SHOW_NUMBERS_ICON = "SHOW_NUMBERS_ICON";

  /**
   * The image registry containing <code>Image</code>s and the
   * <code>ImageDescriptor</code>s.
   */
  private static ImageRegistry imageRegistry;

  /* Declare Common paths */
  private static URL ICON_BASE_URL = null;

  static
  {
    String pathSuffix = "icons/"; //$NON-NLS-1$
    ICON_BASE_URL = Activator.getDefault().getBundle().getEntry(pathSuffix);
  }

  /**
   * Declare all images
   */
  private static void declareImages()
  {
    declareRegistryImage(REFRESH_ICON, "refresh.gif"); //$NON-NLS-1$
    declareRegistryImage(EXPAND_ALL_ICON, "expandall.gif"); //$NON-NLS-1$
    declareRegistryImage(SHOW_NUMBERS_ICON, "action1.gif"); //$NON-NLS-1$
  }

  /**
   * Declare an Image in the registry table.
   *
   * @param key
   *          The key to use when registering the image
   * @param path
   *          The path where the image can be found. This path is relative to
   *          where this plugin class is found (i.e. typically the packages
   *          directory)
   */
  private final static void declareRegistryImage(String key, String path)
  {
    ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
    try
    {
      desc = ImageDescriptor.createFromURL(makeIconFileURL(path));
    }
    catch (MalformedURLException me)
    {
      IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                  "Failed to load icon: " + path, me);
      StatusManager.getManager().handle(status);
    }
    imageRegistry.put(key, desc);
  }

  /**
   * Returns the ImageRegistry.
   */
  public static ImageRegistry getImageRegistry()
  {
    if (imageRegistry == null)
    {
      initializeImageRegistry();
    }
    return imageRegistry;
  }

  /**
   * Initialize the image registry by declaring all of the required graphics.
   * This involves creating JFace image descriptors describing how to
   * create/find the image should it be needed. The image is not actually
   * allocated until requested.
   *
   * Where are the images? The images (typically gifs) are found in the same
   * location as this plugin class. This may mean the same package directory as
   * the package holding this class. The images are declared using
   * this.getClass() to ensure they are looked up via this plugin class.
   *
   * @see org.eclipse.jface.resource.ImageRegistry
   */
  public static ImageRegistry initializeImageRegistry()
  {
    imageRegistry = new ImageRegistry(PlatformUI.getWorkbench().getDisplay());
    declareImages();
    return imageRegistry;
  }

  /**
   * Returns the <code>Image<code> identified by the given key,
   * or <code>null</code> if it does not exist.
   */
  public static Image getImage(String key)
  {
    return getImageRegistry().get(key);
  }

  /**
   * Returns the <code>ImageDescriptor<code> identified by the given key,
   * or <code>null</code> if it does not exist.
   */
  public static ImageDescriptor getImageDescriptor(String key)
  {
    return getImageRegistry().getDescriptor(key);
  }

  private static URL makeIconFileURL(String iconPath)
      throws MalformedURLException
  {
    if (ICON_BASE_URL == null)
    {
      throw new MalformedURLException();
    }

    return new URL(ICON_BASE_URL, iconPath);
  }
}

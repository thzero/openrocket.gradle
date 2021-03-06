package net.sf.openrocket.gui.configdialog;


import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JDialog;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Reflection;

/**
 * A dialog that contains the configuration elements of one component.
 * The contents of the dialog are instantiated from CONFIGDIALOGPACKAGE according
 * to the current component.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class ComponentConfigDialog extends JDialog implements ComponentChangeListener {
	private static final long serialVersionUID = 1L;
	private static final String CONFIGDIALOGPACKAGE = "net.sf.openrocket.gui.configdialog";
	private static final String CONFIGDIALOGPOSTFIX = "Config";
	
	// Static Value -- This is a singleton value, and we should only have zero or one active at any time
	private static ComponentConfigDialog dialog = null;
	
	private OpenRocketDocument document = null;
	private RocketComponent component = null;
	private RocketComponentConfig configurator = null;
	
	private final Window parent;
	private static final Translator trans = Application.getTranslator();
	
	private ComponentConfigDialog(Window parent, OpenRocketDocument document, RocketComponent component,
								  List<RocketComponent> listeners) {
		super(parent);
		this.parent = parent;
		
		setComponent(document, component);
		
		GUIUtil.setDisposableDialogOptions(this, null);
		GUIUtil.rememberWindowPosition(this);

		// overrides common defaults in 'GUIUTIL.setDisposableDialogOptions', above
		addWindowListener(new WindowAdapter() {
			/**
			 *  Triggered by the 'Close' Button on the ConfigDialogs.  AND Esc. AND the [x] button (on Windows)
			 *  In fact, it should trigger for any method of closing the dialog.
			 */
			public void windowClosed(WindowEvent e){
				configurator.invalidate();
				document.getRocket().removeComponentChangeListener(ComponentConfigDialog.this);
				ComponentConfigDialog.this.dispose();
				component.clearConfigListeners();
			}
			
			public void windowClosing(WindowEvent e){}

			@Override
			public void windowOpened(WindowEvent e) {
				super.windowOpened(e);
				// Add config listeners
				component.clearConfigListeners();
				if (listeners != null) {
					for (RocketComponent listener : listeners) {
						component.addConfigListener(listener);
					}
				}
			}
		});
	}
	
	
	/**
	 * Set the component being configured.  The listening connections of the old configurator
	 * will be removed and the new ones created.
	 * 
	 * @param component  Component to configure.
	 */
	private void setComponent(OpenRocketDocument document, RocketComponent component) {
		if (configurator != null) {
			// Remove listeners by setting all applicable models to null
			GUIUtil.setNullModels(configurator); // null-safe
		}
		
		this.document = document;
		this.component = component;
		this.document.getRocket().addComponentChangeListener(this);
		
		configurator = getDialogContents();
		this.setContentPane(configurator);
		configurator.updateFields();
		
		//// configuration
		setTitle(trans.get("ComponentCfgDlg.configuration1") + " " + component.getComponentName() + " " + trans.get("ComponentCfgDlg.configuration"));
		
		this.pack();
	}

	public static ComponentConfigDialog getDialog() {
		return dialog;
	}

	/**
	/**
	 * Return the configurator panel of the current component.
	 */
	private RocketComponentConfig getDialogContents() {
		Constructor<? extends RocketComponentConfig> c =
				findDialogContentsConstructor(component);
		if (c != null) {
			try {
				return c.newInstance(document, component);
			} catch (InstantiationException e) {
				throw new BugException("BUG in constructor reflection", e);
			} catch (IllegalAccessException e) {
				throw new BugException("BUG in constructor reflection", e);
			} catch (InvocationTargetException e) {
				throw Reflection.handleWrappedException(e);
			}
		}
		
		// Should never be reached, since RocketComponentConfig should catch all
		// components without their own configurator.
		throw new BugException("Unable to find any configurator for " + component);
	}
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		if (e.isTreeChange() || e.isUndoChange()) {
			
			// Hide dialog in case of tree or undo change
			disposeDialog();
			
		} else {
			/*
			 * TODO: HIGH:  The line below has caused a NullPointerException (without null check)
			 * How is this possible?  The null check was added to avoid this, but the
			 * root cause should be analyzed.
			 * [Openrocket-bugs] 2009-12-12 19:23:22 Automatic bug report for OpenRocket 0.9.5
			 */
			if (configurator != null)
				configurator.updateFields();
		}
	}
	
	
	/**
	 * Finds the Constructor of the given component's config dialog panel in 
	 * CONFIGDIALOGPACKAGE.
	 */
	@SuppressWarnings("unchecked")
	private static Constructor<? extends RocketComponentConfig> findDialogContentsConstructor(RocketComponent component) {
		Class<?> currentclass;
		String currentclassname;
		String configclassname;
		
		Class<?> configclass;
		Constructor<? extends RocketComponentConfig> c;
		
		currentclass = component.getClass();
		while ((currentclass != null) && (currentclass != Object.class)) {
			currentclassname = currentclass.getCanonicalName();
			int index = currentclassname.lastIndexOf('.');
			if (index >= 0)
				currentclassname = currentclassname.substring(index + 1);
			configclassname = CONFIGDIALOGPACKAGE + "." + currentclassname +
					CONFIGDIALOGPOSTFIX;
			
			try {
				configclass = Class.forName(configclassname);
				c = (Constructor<? extends RocketComponentConfig>)
						configclass.getConstructor(OpenRocketDocument.class, RocketComponent.class);
				return c;
			} catch (Exception ignore) {
			}
			
			currentclass = currentclass.getSuperclass();
		}
		return null;
	}
	
	


	//////////  Static dialog  /////////
	
	/**
	 * A singleton configuration dialog.  Will create and show a new dialog if one has not 
	 * previously been used, or update the dialog and show it if a previous one exists.
	 * 
	 * @param document		the document to configure.
	 * @param component		the component to configure.
	 * @param listeners		config listeners for the component
	 */
	public static void showDialog(Window parent, OpenRocketDocument document,
			RocketComponent component, List<RocketComponent> listeners) {
		if (dialog != null)
			dialog.dispose();

		dialog = new ComponentConfigDialog(parent, document, component, listeners);
		dialog.setVisible(true);
		
		////Modify
		document.addUndoPosition(trans.get("ComponentCfgDlg.Modify") + " " + component.getComponentName());
	}

	/**
	 * A singleton configuration dialog.  Will create and show a new dialog if one has not
	 * previously been used, or update the dialog and show it if a previous one exists.
	 *
	 * @param document		the document to configure.
	 * @param component		the component to configure.
	 */
	public static void showDialog(Window parent, OpenRocketDocument document,
								  RocketComponent component) {
		ComponentConfigDialog.showDialog(parent, document, component, null);
	}
	
	
	/* package */
	static void showDialog(RocketComponent component, List<RocketComponent> listeners) {
		showDialog(dialog.parent, dialog.document, component, listeners);
	}

	/* package */
	static void showDialog(RocketComponent component) {
		ComponentConfigDialog.showDialog(component, null);
	}
	
	/**
	 * Disposes the configuration dialog.  May be used even if not currently visible.
	 */
	public static void disposeDialog() {
		if (dialog != null) {
			dialog.dispose();
		}
	}
	
	
	/**
	 * Returns whether the singleton configuration dialog is currently visible or not.
	 */
	public static boolean isDialogVisible() {
		return (dialog != null) && (dialog.isVisible());
	}
	
}

package net.sf.openrocket.gui.util;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFileChooser;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

public abstract class PreferencesImporter {
    private static final Translator trans = Application.getTranslator();
    private static final Logger log = LoggerFactory.getLogger(PreferencesImporter.class);

    public static boolean importPreferences(Window parent) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(trans.get("PreferencesImporter.chooser.title"));
        chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
        chooser.setFileFilter(FileHelper.XML_FILTER);
        chooser.setAcceptAllFileFilterUsed(false);

        int returnVal = chooser.showOpenDialog(parent);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            log.info("Cancelled import of preferences.");
            return false;
        }

        ((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());

        File importFile = chooser.getSelectedFile();
        try (FileInputStream fis = new FileInputStream(importFile)) {
            Preferences.importPreferences(fis);
            log.info("Preferences imported successfully.");
        } catch (IOException | InvalidPreferencesFormatException e) {
            log.warn("Error while importing preferences: " + e.getMessage());
        }

        return true;
    }
}

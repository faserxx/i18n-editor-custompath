package com.jvms.i18neditor.editor;

import com.jvms.i18neditor.FileStructure;
import com.jvms.i18neditor.ResourceType;
import com.jvms.i18neditor.editor.menu.*;
import com.jvms.i18neditor.swing.util.Dialogs;
import com.jvms.i18neditor.util.GithubRepoUtil;
import com.jvms.i18neditor.util.MessageBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static javax.swing.JOptionPane.DEFAULT_OPTION;

/**
 * This class represents the top bar menu of the editor.
 *
 * @author Jacob van Mourik
 */
public class EditorMenuBar extends JMenuBar {
    private final static long serialVersionUID = -101788804096708514L;

    private final Editor editor;
    private final TranslationTree tree;
    private JMenuItem saveMenuItem;
    private JMenuItem reloadMenuItem;
    private JMenuItem addTranslationMenuItem;
    private JMenuItem findTranslationMenuItem;
    private JMenuItem findByLanguageMenuItem;
    private JMenuItem renameTranslationMenuItem;
    private JMenuItem copyTranslationKeyMenuItem;
    private JMenuItem removeTranslationMenuItem;
    private JMenuItem clearSearchMenuItem;
    private JMenuItem changePathFolderProjectMenuItem;
    private JMenuItem closeProjectMenuItem;
    private JMenuItem restoreToDefaultMenuItem;
    private JMenuItem openContainingFolderMenuItem;
    private JMenuItem projectSettingsMenuItem;
    private JMenuItem editorSettingsMenuItem;
    private JMenu openRecentMenuItem;
    private JMenu editMenu;
    private JMenu viewMenu;
    private JMenu settingsMenu;

    public EditorMenuBar(Editor editor, TranslationTree tree) {
        super();
        this.editor = editor;
        this.tree = tree;
        setupUI();
        setEnabled(false);
        setSaveable(false);
        setEditable(false);
    }
    //enable clear search
    public void setEnableClearSearch(boolean enable)
    {
        clearSearchMenuItem.setEnabled(enable);
    }
    @Override
    public void setEnabled(boolean enabled) {
       reloadMenuItem.setEnabled(enabled);
        openContainingFolderMenuItem.setEnabled(enabled);
        closeProjectMenuItem.setEnabled(enabled);
        editMenu.setEnabled(enabled);
        viewMenu.setEnabled(enabled);
        settingsMenu.removeAll();

        if (enabled) {
            settingsMenu.add(projectSettingsMenuItem);
            settingsMenu.addSeparator();
            settingsMenu.add(editorSettingsMenuItem);
        } else {
            settingsMenu.add(editorSettingsMenuItem);
        }
        settingsMenu.add(restoreToDefaultMenuItem);
        SwingUtilities.updateComponentTreeUI(this);
    }

    public void setSaveable(boolean saveable) {
        saveMenuItem.setEnabled(saveable);
    }

    public void setEditable(boolean editable) {
        addTranslationMenuItem.setEnabled(editable);
        findTranslationMenuItem.setEnabled(editable);
        changePathFolderProjectMenuItem.setEnabled(editable);
        restoreToDefaultMenuItem.setEnabled(true);

        findByLanguageMenuItem.setEnabled(editable);
    }

    public void setRecentItems(List<String> items) {
        openRecentMenuItem.removeAll();
        if (items.isEmpty()) {
            openRecentMenuItem.setEnabled(false);
        } else {
            openRecentMenuItem.setEnabled(true);
            for (int i = 0; i < items.size(); i++) {
                Integer n = i + 1;
                JMenuItem menuItem = new JMenuItem(n + ": " + items.get(i), Character.forDigit(i, 10));
                Path path = Paths.get(menuItem.getText().replaceFirst("[0-9]+: ", ""));
                menuItem.addActionListener(e -> editor.importProject(path,null));
                openRecentMenuItem.add(menuItem);
            }
            JMenuItem clearMenuItem = new JMenuItem(MessageBundle.get("menu.file.recent.clear.title"));
            clearMenuItem.addActionListener(e -> editor.clearHistory());
            openRecentMenuItem.addSeparator();
            openRecentMenuItem.add(clearMenuItem);
        }
    }

    private void setupUI() {
        int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        // File menu
        JMenu fileMenu = new JMenu(MessageBundle.get("menu.file.title"));
        fileMenu.setMnemonic(MessageBundle.getMnemonic("menu.file.vk"));



        JMenuItem importMenuItem = new JMenuItem(MessageBundle.get("menu.file.project.import.title"));
        importMenuItem.setMnemonic(MessageBundle.getMnemonic("menu.file.project.import.vk"));
        importMenuItem.addActionListener(e -> editor.showImportProjectDialog());

        openContainingFolderMenuItem = new JMenuItem(MessageBundle.get("menu.file.folder.title"));
        openContainingFolderMenuItem.addActionListener(e -> editor.openProjectDirectory());

        openRecentMenuItem = new JMenu(MessageBundle.get("menu.file.recent.title"));
        openRecentMenuItem.setMnemonic(MessageBundle.getMnemonic("menu.file.recent.vk"));

        saveMenuItem = new JMenuItem(MessageBundle.get("menu.file.save.title"), MessageBundle.getMnemonic("menu.file.save.vk"));
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, keyMask));
        saveMenuItem.addActionListener(e -> editor.saveProject());

        reloadMenuItem = new JMenuItem(MessageBundle.get("menu.file.reload.title"), MessageBundle.getMnemonic("menu.file.reload.vk"));
        reloadMenuItem.addActionListener(e -> editor.reloadProject());

        clearSearchMenuItem= new JMenuItem(MessageBundle.get("menu.file.clear.search.title"),MessageBundle.getMnemonic("menu.file.clear.search.vk"));
        clearSearchMenuItem.addActionListener(e->editor.reloadProject());
        clearSearchMenuItem.setEnabled(false);

        closeProjectMenuItem = new JMenuItem(MessageBundle.get("menu.file.close.title"), MessageBundle.getMnemonic("menu.file.reload.vk"));
        closeProjectMenuItem.addActionListener(e -> editor.closeProject());

        JMenuItem exitMenuItem = new JMenuItem(MessageBundle.get("menu.file.exit.title"), MessageBundle.getMnemonic("menu.file.exit.vk"));
        exitMenuItem.addActionListener(e -> editor.dispatchEvent(new WindowEvent(editor, WindowEvent.WINDOW_CLOSING)));


        fileMenu.add(importMenuItem);
        if (Desktop.isDesktopSupported()) {
            fileMenu.add(openContainingFolderMenuItem);
        }
        fileMenu.add(openRecentMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(saveMenuItem);
        fileMenu.add(reloadMenuItem);
        fileMenu.add(clearSearchMenuItem);
        fileMenu.add(closeProjectMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        // Edit menu
        editMenu = new JMenu(MessageBundle.get("menu.edit.title"));
        editMenu.setMnemonic(MessageBundle.getMnemonic("menu.edit.vk"));

        addTranslationMenuItem = new AddTranslationMenuItem(editor, tree, false);
        findTranslationMenuItem = new FindTranslationMenuItem(editor, false);
        findByLanguageMenuItem = new FindByLanguageMenuItem(editor, false);
        removeTranslationMenuItem = new RemoveTranslationMenuItem(editor, false);
        renameTranslationMenuItem = new RenameTranslationMenuItem(editor, false);
        copyTranslationKeyMenuItem = new CopyTranslationKeyToClipboardMenuItem(editor, false);
        changePathFolderProjectMenuItem = new ChangePathFolderProjectMenuItem(editor, false);
        restoreToDefaultMenuItem = new RestoreToDefaultMenuItem(editor, false);
        findByLanguageMenuItem = new FindByLanguageMenuItem(editor, false);

        editMenu.add(new AddLocaleMenuItem(editor, true));
        editMenu.addSeparator();
        editMenu.add(addTranslationMenuItem);
        editMenu.add(findTranslationMenuItem);
        editMenu.add(findByLanguageMenuItem);
        editMenu.addSeparator();
        editMenu.add(renameTranslationMenuItem);
        editMenu.add(removeTranslationMenuItem);
        editMenu.add(copyTranslationKeyMenuItem);
        editMenu.addSeparator();
        editMenu.add(changePathFolderProjectMenuItem);

        // View menu
        viewMenu = new JMenu(MessageBundle.get("menu.view.title"));
        viewMenu.setMnemonic(MessageBundle.getMnemonic("menu.view.vk"));
        viewMenu.add(new ExpandTranslationsMenuItem(tree));
        viewMenu.add(new CollapseTranslationsMenuItem(tree));

        // Settings menu
        settingsMenu = new JMenu(MessageBundle.get("menu.settings.title"));
        settingsMenu.setMnemonic(MessageBundle.getMnemonic("menu.settings.vk"));

        editorSettingsMenuItem = new JMenuItem(MessageBundle.get("menu.settings.preferences.editor.title"));
        editorSettingsMenuItem.addActionListener(e -> {
            Dialogs.showComponentDialog(editor,
                    MessageBundle.get("dialogs.preferences.editor.title"),
                    new EditorSettingsPane(editor));
        });

        projectSettingsMenuItem = new JMenuItem(MessageBundle.get("menu.settings.preferences.project.title"));


        projectSettingsMenuItem.addActionListener(e -> {
           /* Dialogs.showComponentDialog(editor,
                    MessageBundle.get("dialogs.preferences.project.title"),
                    new EditorProjectSettingsPane(editor));*/
            FileStructure before = editor.getProject().getResourceFileStructure();
            int result = JOptionPane.showOptionDialog(editor,  new EditorProjectSettingsPane(editor), MessageBundle.get("dialogs.preferences.project.title"), DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null, null);
            if(result == 0 && editor.getProject().getResourceFileStructure() != before){
                editor.saveProject();
                editor.importProject(editor.getProject().getPath(),null);
            }
        });

        settingsMenu.add(editorSettingsMenuItem);

        // Help menu
        JMenu helpMenu = new JMenu(MessageBundle.get("menu.help.title"));
        helpMenu.setMnemonic(MessageBundle.getMnemonic("menu.help.vk"));

        JMenuItem versionMenuItem = new JMenuItem(MessageBundle.get("menu.help.version.title"));
        versionMenuItem.addActionListener(e -> editor.showVersionDialog(false));

        JMenuItem homeMenuItem = new JMenuItem(MessageBundle.get("menu.help.home.title", Editor.TITLE));
        homeMenuItem.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create(GithubRepoUtil.getURL(Editor.GITHUB_USER, Editor.GITHUB_PROJECT)));
            } catch (IOException e1) {
                //
            }
        });

        JMenuItem aboutMenuItem = new JMenuItem(MessageBundle.get("menu.help.about.title", Editor.TITLE));
        aboutMenuItem.addActionListener(e -> editor.showAboutDialog());

        helpMenu.add(versionMenuItem);
        helpMenu.addSeparator();
        helpMenu.add(homeMenuItem);
        helpMenu.add(aboutMenuItem);

        add(fileMenu);
        add(editMenu);
        add(viewMenu);
        add(settingsMenu);
        add(helpMenu);

        tree.addTreeSelectionListener(e -> {
            TranslationTreeNode node = tree.getSelectionNode();
            boolean enabled = node != null && !node.isRoot();
            renameTranslationMenuItem.setEnabled(enabled);
            copyTranslationKeyMenuItem.setEnabled(enabled);
            removeTranslationMenuItem.setEnabled(enabled);
        });
    }
}

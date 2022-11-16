package com.jvms.i18neditor.editor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.jvms.i18neditor.FileStructure;
import com.jvms.i18neditor.Resource;
import com.jvms.i18neditor.ResourceType;
import com.jvms.i18neditor.io.ChecksumException;
import com.jvms.i18neditor.swing.JFileDrop;
import com.jvms.i18neditor.swing.JScrollablePanel;
import com.jvms.i18neditor.swing.util.Dialogs;
import com.jvms.i18neditor.util.*;
import com.jvms.i18neditor.util.GithubRepoUtil.GithubRepoReleaseData;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * This class represents the main class of the editor.
 *
 * @author Jacob van Mourik
 */
public class Editor extends JFrame {
    public static final String TITLE = "i18n-editor";
    public static final String VERSION = "2.0.0-beta.1";
    public static final String GITHUB_USER = "jcbvm";
    public static final String GITHUB_PROJECT = "i18n-editor";
    public static final String PROJECT_FILE = ".i18n-editor-metadata";
    public static final String SETTINGS_FILE = ".i18n-editor";
    public static final String SETTINGS_DIR = System.getProperty("user.home");
    public static final Locale DEFAULT_LANGUAGE = Locale.ENGLISH;
    protected static final List<Locale> SUPPORTED_LANGUAGES = Lists.newArrayList(new Locale("en"), new Locale("nl"), new Locale("pt", "BR"), new Locale("es", "ES"));
    private static final long serialVersionUID = 1113029729495390082L;
    private static final Logger log = LoggerFactory.getLogger(Editor.class);
    private EditorSettings settings = new EditorSettings();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private EditorProject project;
    private boolean dirty;

    private EditorMenuBar editorMenu;
    private JSplitPane contentPane;
    private JLabel introText;
    private JPanel translationsPanel;
    private JScrollPane resourcesScrollPane;
    private TranslationTree translationTree;
    private TranslationKeyField translationField;
    private JPanel resourcesPanel;
    private Set<ResourceField> resourceFields = new HashSet<>();
    private Map<ResourceField, JLabel> jLabels = new HashMap<>();


//    public void createProject(Path dir, ResourceType type) {
//        File folder = new File(dir.toString(),"i18n");
//        folder.mkdirs();
//        importProject(dir, null);
//        showAddLocaleDialog();
//       }

    public void closeProject() {
        if (project != null) {
            closeCurrentProject();
            project = null;
            clearUI();
        }
    }

    public void changePathFolder() {
        int confirm = JOptionPane.showConfirmDialog(this, MessageBundle.get("dialogs.change.path.text"), MessageBundle.get("dialogs.change.path.title"), JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle(MessageBundle.get("dialogs.project.change.path.title"));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fc.showOpenDialog(this);
            String pathOld = project.getPath().toString();
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    FileUtils.cleanDirectory(new File(fc.getSelectedFile().getPath()));
                    FileUtils.copyDirectory(new File(pathOld), new File(fc.getSelectedFile().getPath()));
                    importProject(Paths.get(fc.getSelectedFile().getPath()), null);
                    FileUtils.forceDelete(new File(pathOld));
                    JOptionPane.showMessageDialog(this, MessageBundle.get("dialogs.change.success"), MessageBundle.get("dialogs.change.title"), JOptionPane.INFORMATION_MESSAGE);
                    //eliminando pathOld del history
                    List<String> recentList = settings.getHistory();
                    recentList.remove(pathOld);
                    settings.setHistory(recentList);
                    editorMenu.setRecentItems(recentList);
                } catch (IOException e) {
                    log.error("Error copy file to another directory", e);
                    showError(MessageBundle.get("resources.change.path.error"));
                }
            }
        }
    }


    public void restoreToDefaultProject() {
        int confirm = JOptionPane.showConfirmDialog(this, MessageBundle.get("dialogs.restore.text"), MessageBundle.get("dialogs.restore.confirmation.title"), JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            File fileToDeleted = new File(SETTINGS_DIR, SETTINGS_FILE);
            if (fileToDeleted.delete()) {
                JOptionPane.showMessageDialog(this, MessageBundle.get("dialogs.restore.success"), MessageBundle.get("dialogs.restore.title"), JOptionPane.INFORMATION_MESSAGE);
                EditorSettings settingsa = new EditorSettings();
                settingsa.setResourceFileDefinition(settings.getResourceFileDefinition());
                settingsa.setResourceFileStructure(settings.getResourceFileStructure());
                settingsa.setHistory(new ArrayList<>());
                settings = settingsa;
                storeEditorState();
                closeProject();
                clearUI();
                launch();
            } else {
                log.error("Error to deleted file: " + Paths.get(SETTINGS_DIR, SETTINGS_FILE));
                showError(MessageBundle.get("resources.delete.file.i18n-editor"));
            }
        }
    }


    public void showFindByLanguageDialog() {
        String key = "";
        while (key != null) {
            key = Dialogs.showInputDialog(this, MessageBundle.get("dialogs.translation.language.find.title"), MessageBundle.get("dialogs.translation.language.find.text"), null, JOptionPane.QUESTION_MESSAGE, key, new TranslationKeyCaret());
                       /* if(key.equals(""))
                                   {
                                       Dialogs.showWarningDialog(this,
							MessageBundle.get("dialogs.translation.language.find.title"),
							MessageBundle.get("dialogs.translation.language.find.text"));
                                   }
                        else
                                   {*/
            if (!key.equals("")) {
                List<Resource> resources = project.getResources();  //Getting resource list
                Boolean flag = false;
                for (int i = 0; i < resources.size(); i++) //looping through list of resources to select the one that belongs to the selected language
                {
                    Locale l = resources.get(i).getLocale(); //getting the Locale of each resource
                    if (l != null) {
                        if (l.getLanguage().contains(key)) //checking the language of each locale to add the searched resource to the list
                        {
                            flag = true;
                            Path testPath = resources.get(i).getPath();
                            Path pathProject = project.getPath();
                            importProject(pathProject, testPath);

//                            if (testPath != null) {
//                                Resource resource = resources.get(i);
//                                try {
//                                    Resources.load(resource);
//                                } catch (Exception e) {
//                                }
//                                setupResource(resources.get(i));
//                                project.addResource(resources.get(i));
//                            }
                        }
                    }
                }
                editorMenu.setEnableClearSearch(true);
                if (Boolean.FALSE.equals(flag)) {
                    Dialogs.showWarningDialog(this, MessageBundle.get("dialogs.translation.language.find.title"), MessageBundle.get("dialogs.translation.language.find.error"));
                }
                key = null;
            }
            //break;
        }
    }


    public void importProject(Path dir, Path dirLanguage) {
        if (!dir.toFile().exists()) {
            Utils.showError(MessageBundle.get("dialogs.dir.notexist"));
        }
        //Check if exist the directory
        //Preconditions.checkArgument(Files.isDirectory(dir));

        if (!closeCurrentProject()) {
            return;
        }

        clearUI();

        project = new EditorProject(dir);
        restoreProjectState(project);

        // Recreate Tree by Type of File Structure
        if (project.getResourceFileStructure() == FileStructure.Flat)
            FlatViewUtils.analizeAndCreateTree(this, project, dir, dirLanguage, translationTree, settings);
        else {
            FolderViewUtils.analizeAndCreateTree(this, project, dir, dirLanguage, translationTree, settings);
        }

        updateTreeNodeStatuses();
        updateHistory();
        updateUI();
        requestFocusInFirstResourceField();


    }


    public boolean saveProject() {
        boolean error = false;
        if (project != null) {
            for (Resource resource : project.getResources()) {
                if (!saveResource(resource)) {
                    error = true;
                }
            }
        }
        if (dirty) {
            setDirty(error);
        }
        return !error;
    }

    public void reloadProject() {
        if (project != null) {
            importProject(project.getPath(), null);
        }
    }

    public void removeSelectedTranslation() {
        TranslationTreeNode node = translationTree.getSelectionNode();
        if (node != null && !node.isRoot()) {
            TranslationTreeNode parent = (TranslationTreeNode) node.getParent();

            //Obtain all paths that containt the key
            List<String> paths = Utils.getAllPathFromKeys(project, Utils.restoreStringTrunk(node, node.getKey()));
            removeTranslation(node.getKey(), node);
            Utils.logsPathWithMessage(paths, 'D', MessageBundle.get("log.remove.selected.translation") + " " + node.getKey());
            translationTree.setSelectionNode(parent);


        }
    }

    public void renameSelectedTranslation() {
        TranslationTreeNode node = translationTree.getSelectionNode();
        if (node != null && !node.isRoot()) {
            String nameOld = node.getKey();
            showRenameTranslationDialog(node);
            String nameNew = node.getKey();

            if (!nameNew.equals(nameOld)) {
                StringBuilder sb = new StringBuilder();
                sb.append(MessageBundle.get("log.rename.selected.translation")).append(" ").append(nameOld).append(MessageBundle.get("log.to")).append(" ").append(nameNew);
                Utils.writeLogsByNameKey(project, Utils.restoreStringTrunk(node, node.getKey()), 'A', sb.toString());
            }
        }
    }

    public void copySelectedTranslationKey() {
        TranslationTreeNode node = translationTree.getSelectionNode();
        if (node != null && !node.isRoot()) {
            String key = node.getKey();
            StringSelection selection = new StringSelection(key);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            Utils.writeLogsByNameKey(project, node.getKey(), 'A', MessageBundle.get("log.copy.selected.translation") + " " + key);

        }
    }


    public boolean addLocale(Path path, String localeString) {

        if (project == null) {
            return false;
        }

        Locale locale = Locales.parseLocale(localeString);

        try {
            Resource resource = Resources.create(project.getResourceType(), path, project.getResourceFileDefinition(), project.getResourceFileStructure(), locale);
            addResource(resource);

            LogParameters params = new LogParameters("", project.getPath().toAbsolutePath().toString(), 'C', MessageBundle.get("log.add.locale") + localeString);
            LogManager.logMessage(params);
            requestFocusInFirstResourceField();
            return true;
        } catch (IOException e) {
            log.error("Error creating new locale", e);
            showError(MessageBundle.get("dialogs.locale.add.error.create"));
            return false;
        }
    }

    public boolean addTranslation(String key) {

        if (!ResourceKeys.isValid(key)) {
            showError(MessageBundle.get("dialogs.translation.key.error"));
            return false;
        }

        TranslationTreeNode node = translationTree.getNodeByKey(key);

        if (node != null) {
            //translationTree.setSelectionNode(node);
            JOptionPane.showMessageDialog(this, MessageBundle.get("dialogs.create.branch"), MessageBundle.get("dialogs.branch.title"), JOptionPane.YES_OPTION);
            return false;
        } else if (!confirmNewTranslation(key)) {
            return false;
        }
        if (project != null) {
            String path = Utils.getPathOfNode(translationTree.getSelectionNode(), project).toString();
            project.getResources().stream().filter(x -> x.getPath().toAbsolutePath().toString().contains(path)).forEach(resource -> resource.storeTranslation(key, ""));
        }
        translationTree.addNodeByKey(key);
        //   Utils.writeLogsByNameKey(project, key, 'A', MessageBundle.get("log.add.translation") + key);
        requestFocusInFirstResourceField();
        return true;
    }

    public void removeTranslation(String key, TranslationTreeNode node) {
        if (project != null) {
            String path = Utils.getPathOfNode(node, project).toString();

            project.getResources().stream().filter(x -> x.getPath().toAbsolutePath().toString().contains(path)).forEach(resource -> resource.removeTranslation(Utils.restoreStringTrunk(node, key)));
        }
        translationTree.removeNodeByKey(key);
        requestFocusInFirstResourceField();
    }

    public boolean renameTranslation(String key, String newKey, TranslationTreeNode node) {
        String finalKey = Utils.restoreStringTrunk(node, key);
        String finalNewKey = Utils.restoreStringTrunk(node, newKey);

        if (!ResourceKeys.isValid(newKey)) {
            showError(MessageBundle.get("dialogs.translation.key.error"));
            return false;
        }
        if (key.equals(newKey)) {
            return true;
        }
        if (!confirmNewTranslation(key, newKey)) {
            return false;
        }
        if (project != null) {
            project.getResources().forEach(resource -> resource.renameTranslation(finalKey, finalNewKey));
        }
        translationTree.renameNodeByKey(key, newKey);
        requestFocusInFirstResourceField();
        return true;
    }

    public boolean duplicateTranslation(String key, String newKey) {
        if (!ResourceKeys.isValid(newKey)) {
            showError(MessageBundle.get("dialogs.translation.key.error"));
            return false;
        }
        if (key.equals(newKey)) {
            return true;
        }
        if (!confirmNewTranslation(key, newKey)) {
            return false;
        }
        if (project != null) {
            project.getResources().forEach(resource -> resource.duplicateTranslation(key, newKey));
        }
        translationTree.duplicateNodeByKey(key, newKey);
        requestFocusInFirstResourceField();
        return true;
    }

    public void addResource(Resource resource) {
        setupResource(resource);
        updateUI();
        if (project != null) {
            project.addResource(resource);
            updateTreeNodeStatuses();
        }
    }

    public EditorProject getProject() {
        return project;
    }

    public EditorSettings getSettings() {
        return settings;
    }

    public Locale getCurrentLocale() {
        Locale locale = settings.getEditorLanguage();
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        updateTitle();
        editorMenu.setSaveable(dirty);
    }

    public void clearHistory() {
        settings.setHistory(Lists.newArrayList());
        editorMenu.setRecentItems(Lists.newArrayList());
    }

//    public void showCreateProjectDialog(ResourceType type) {
//        JFileChooser fc = new JFileChooser();
//        fc.setDialogTitle(MessageBundle.get("dialogs.project.new.title"));
//        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//        int result = fc.showOpenDialog(this);
//        if (result == JFileChooser.APPROVE_OPTION) {
//            createProject(Paths.get(fc.getSelectedFile().getPath()), type);
//        } else {
//            updateHistory();
//            updateUI();
//        }
//    }


    public void showImportProjectDialog() {
        String path = null;
        if (project != null) {
            path = project.getPath().toString();
        }
        JFileChooser fc = new JFileChooser(path);
        fc.setDialogTitle(MessageBundle.get("dialogs.project.import.title"));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            importProject(Paths.get(fc.getSelectedFile().getPath()), null);
        }
    }

    public void showAddLocaleDialog() {

        String localeString = "";
        while (localeString != null) {
            localeString = Dialogs.showInputDialog(this, MessageBundle.get("dialogs.locale.add.title"), MessageBundle.get("dialogs.locale.add.text"), null, JOptionPane.QUESTION_MESSAGE).trim();

            if (!Utils.isLocaleAvailable(localeString)) {
                Utils.showError(MessageBundle.get("dialogs.locale.add.error.invalid"));

            } else {

                TranslationTreeNode node = translationTree.getSelectionNode();
                Path path = project.getPath();

                if (project.getResourceFileStructure() == FileStructure.Flat) {
                    path = FlatViewUtils.getPathofLocale(project, this);

                } else if (!node.isRoot()) {
                    path = Utils.getPathOfNode(node, project);

                }


                addLocale(path, localeString);

                localeString = null;
                /*  Path path = ;

                storeProjectState();
                importProject(project.getPath(), null);
                localeString = null;*/
            }
        }
    }

    public void showRenameTranslationDialog(TranslationTreeNode node) {
        String key = node.getKey();
        String newKey = "";
        while (newKey != null) {
            newKey = Dialogs.showInputDialog(this, MessageBundle.get("dialogs.translation.rename.title"), MessageBundle.get("dialogs.translation.rename.text"), MessageBundle.get("dialogs.translation.add.help"), JOptionPane.QUESTION_MESSAGE, key, new TranslationKeyCaret());
            if (newKey != null) {
                boolean result = renameTranslation(key, newKey.trim(), node);
                if (result) {
                    break;
                }
            }
        }
    }


    public void showAddTranslationDialog(TranslationTreeNode node) {
        if (node.typeFile == TypeFile.FOLDER) {
            Path path = Utils.getPathOfNode(node, project);
            if (node.isRoot()) {
                path = project.getPath();

            }
            if (path.toFile().listFiles().length == 0) {
                showAddLocaleDialog();
            }

        }
        String key = "";
        String newKey = "";
        if (node != null && !node.isRoot()) {
            key = node.getKey() + ".";
        }

        while (newKey != null) {
            newKey = Dialogs.showInputDialog(this, MessageBundle.get("dialogs.translation.add.title"), MessageBundle.get("dialogs.translation.add.text"), MessageBundle.get("dialogs.translation.add.help"), JOptionPane.QUESTION_MESSAGE, key, new TranslationKeyCaret());
            if (newKey != null) {
                boolean result = addTranslation(newKey.trim());
                if (result) {
                    editorMenu.setSaveable(true);
                    break;
                }
            }
        }
    }

    public void showFindTranslationDialog() {
        String key = "";
        while (key != null) {
            key = Dialogs.showInputDialog(this, MessageBundle.get("dialogs.translation.find.title"), MessageBundle.get("dialogs.translation.find.text"), null, JOptionPane.QUESTION_MESSAGE, key, new TranslationKeyCaret());
            if (key != null) {
                TranslationTreeNode node = translationTree.getNodeByKey(key.trim());
                if (node == null) {
                    Dialogs.showWarningDialog(this, MessageBundle.get("dialogs.translation.find.title"), MessageBundle.get("dialogs.translation.find.error"));
                } else {
                    translationTree.setSelectionNode(node);
                    break;
                }
            }
        }
    }

    public void showAboutDialog() {
        Dialogs.showHtmlDialog(this, MessageBundle.get("dialogs.about.title", TITLE), "<img src=\"" + Images.getClasspathURL("images/icon-48.png") + "\"><br>" + "<span style=\"font-size:1.3em;\"><strong>" + TITLE + "</strong></span><br>" + VERSION + "<br><br>" + "Copyright (c) 2015 - 2018<br>" + "Jacob van Mourik<br>" + "MIT Licensed");
    }

    public void showVersionDialog(boolean newVersionOnly) {
        executor.execute(() -> {
            GithubRepoReleaseData data;
            String content;
            try {
                data = GithubRepoUtil.getLatestRelease(Editor.GITHUB_USER, Editor.GITHUB_PROJECT).get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                data = null;
            }
            if (data != null && VERSION.compareToIgnoreCase(data.getTagName()) < 0) {
                content = MessageBundle.get("dialogs.version.new") + " " + "<strong>" + data.getTagName() + "</strong><br>" + "<a href=\"" + data.getHtmlUrl() + "\">" + MessageBundle.get("dialogs.version.link") + "</a>";
            } else if (!newVersionOnly) {
                content = MessageBundle.get("dialogs.version.uptodate");
            } else {
                return;
            }
            Dialogs.showHtmlDialog(this, MessageBundle.get("dialogs.version.title"), content);
        });
    }

    public boolean closeCurrentProject() {
        boolean result = true;
        if (dirty) {
            int confirm = JOptionPane.showConfirmDialog(this, MessageBundle.get("dialogs.save.text"), MessageBundle.get("dialogs.save.title"), JOptionPane.YES_NO_CANCEL_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                result = saveProject();
            } else {
                result = confirm != JOptionPane.CANCEL_OPTION;
            }
        }
        if (result && project != null) {
            storeProjectState();
        }
        if (result && dirty) {
            setDirty(false);
        }

        return result;
    }

    public void openProjectDirectory() {
        if (project == null) return;
        try {
            Desktop.getDesktop().open(project.getPath().toFile());
        } catch (IOException ex) {
            log.error("Unable to open project directory " + project.getPath(), ex);
        }
    }

    public void launch() {
        restoreEditorState();

        if (settings.getEditorLanguage() != null) {
            Locale.setDefault(settings.getEditorLanguage());
        } else {
            Locale.setDefault(DEFAULT_LANGUAGE);
        }


        MessageBundle.loadResources();

        setupUI();
        setupFileDrop();
        setupGlobalKeyEventDispatcher();

        setPreferredSize(new Dimension(settings.getWindowWidth(), settings.getWindowHeight()));
        setLocation(settings.getWindowPositionX(), settings.getWindowPositionY());
        contentPane.setDividerLocation(settings.getWindowDeviderPosition());

        pack();
        setVisible(true);
        if (!settings.closeInError) {
            List<String> dirs = settings.getHistory();
            if (!dirs.isEmpty()) {
                String lastDir = dirs.get(dirs.size() - 1);
                Path path = Paths.get(lastDir);
                if (Files.exists(path)) {
                    importProject(path, null);
                }
            }
        }
        settings.closeInError = true;

        if (project == null) {
            updateHistory();
        }
        if (project != null && project.hasResources()) {
            // Restore last expanded nodes
            List<String> expandedKeys = settings.getLastExpandedNodes();
            List<TranslationTreeNode> expandedNodes = expandedKeys.stream().map(translationTree::getNodeByKey).filter(n -> n != null).collect(Collectors.toList());
            translationTree.expand(expandedNodes);
            // Restore last selected node
            String selectedKey = settings.getLastSelectedNode();
            TranslationTreeNode selectedNode = translationTree.getNodeByKey(selectedKey);
            if (selectedNode != null) {
                translationTree.setSelectionNode(selectedNode);
            }
        }

        if (settings.isCheckVersionOnStartup()) {
            showVersionDialog(true);
        }
    }

    public void updateUI() {
        TranslationTreeNode selectedNode = translationTree.getSelectionNode();

        resourcesPanel.removeAll();
        resourceFields = resourceFields.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
        resourceFields.forEach(field -> {

            Locale locale = field.getResource().getLocale();
            String label = locale != null ? locale.getDisplayName() : MessageBundle.get("resources.locale.default");
            field.setEnabled(selectedNode != null && selectedNode.isEditable() && (selectedNode.isLeaf() || field.getResource().supportsParentValues()));
            field.setRows(settings.getDefaultInputHeight());
            resourcesPanel.add(Box.createVerticalStrut(5));
            JLabel jLabel = new JLabel(label);
            jLabel.setVisible(false);
            jLabels.put(field, jLabel);
            resourcesPanel.add(jLabel);
            // resourcesPanel.add(Box.createVerticalStrut(5));
            resourcesPanel.add(field);

            //resourcesPanel.add(Box.createVerticalStrut(10));
        });

        Container container = getContentPane();
        if (project != null) {
            container.add(contentPane);
            container.remove(introText);
            List<Resource> resources = project.getResources();
            editorMenu.setEnabled(true);
            editorMenu.setEditable(!resources.isEmpty());
            translationField.setEditable(!resources.isEmpty());
        } else {
            container.add(introText);
            container.remove(contentPane);
            editorMenu.setEnabled(false);
            editorMenu.setEditable(false);
            translationField.setEditable(false);
        }

        translationField.setVisible(settings.isKeyFieldEnabled());
        translationTree.setToggleClickCount(settings.isDoubleClickTreeToggling() ? 2 : 1);

        updateTitle();
        validate();
        repaint();
    }

    private boolean confirmNewTranslation(String oldKey, String newKey) {
        TranslationTreeNode newNode = translationTree.getNodeByKey(newKey);
        TranslationTreeNode oldNode = translationTree.getNodeByKey(oldKey);
        if (newNode != null) {
            boolean isReplace = newNode.isLeaf() || oldNode.isLeaf();
            boolean confirm = Dialogs.showConfirmDialog(this, MessageBundle.get("dialogs.translation.conflict.title"), MessageBundle.get("dialogs.translation.conflict.text." + (isReplace ? "replace" : "merge")), JOptionPane.WARNING_MESSAGE);
            if (!confirm) {
                return false;
            }
        }
        return confirmNewTranslation(newKey);
    }

    private boolean confirmNewTranslation(String key) {
        if (project == null || project.supportsResourceParentValues()) {
            return true;
        }
        // Check if there is an existing leaf node in the key path with one or more values
        key = ResourceKeys.withoutLastPart(key);
        while (!Strings.isNullOrEmpty(key)) {
            TranslationTreeNode node = translationTree.getNodeByKey(key);
            if (node != null && !node.isRoot() && node.isLeaf()) {
                boolean hasValue = project.getResources().stream().anyMatch(r -> {
                    return !Strings.isNullOrEmpty(r.getTranslation(node.getKey()));
                });
                if (hasValue) {
                    return Dialogs.showConfirmDialog(this, MessageBundle.get("dialogs.translation.overwrite.title"), MessageBundle.get("dialogs.translation.overwrite.text", node.getKey()), JOptionPane.WARNING_MESSAGE);
                }
            }
            key = ResourceKeys.withoutLastPart(key);
        }
        return true;
    }

    private void requestFocusInFirstResourceField() {
        resourceFields.stream().findFirst().ifPresent(JComponent::requestFocusInWindow);
    }

    private void clearUI() {
        translationField.clear();
        translationTree.clear();
        resourceFields.clear();
        updateUI();
    }

    private void setupUI() {
        Color borderColor = Colors.scale(UIManager.getColor("Panel.background"), .8f);

        setTitle(TITLE);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new EditorWindowListener());

        setIconImages(Lists.newArrayList("512", "256", "128", "64", "48", "32", "24", "20", "16").stream().map(size -> Images.loadFromClasspath("images/icon-" + size + ".png").getImage()).collect(Collectors.toList()));

        translationTree = new TranslationTree();
        translationTree.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        translationTree.addTreeSelectionListener(new TranslationTreeNodeSelectionListener());
        translationTree.addMouseListener(new TranslationTreeMouseListener());

        translationField = new TranslationKeyField();
        translationField.addKeyListener(new TranslationFieldKeyListener());
        translationField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 1, borderColor), ((CompoundBorder) translationField.getBorder()).getInsideBorder()));

        JScrollPane translationsScrollPane = new JScrollPane(translationTree);
        translationsScrollPane.getViewport().setOpaque(false);
        translationsScrollPane.setOpaque(false);
        translationsScrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, borderColor));

        translationsPanel = new JPanel(new BorderLayout());
        translationsPanel.add(translationsScrollPane);
        translationsPanel.add(translationField, BorderLayout.SOUTH);

        resourcesPanel = new JScrollablePanel(true, false);
        resourcesPanel.setLayout(new BoxLayout(resourcesPanel, BoxLayout.Y_AXIS));
        resourcesPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        resourcesPanel.setOpaque(false);
        resourcesPanel.addMouseListener(new ResourcesPaneMouseListener());

        resourcesScrollPane = new JScrollPane(resourcesPanel);
        resourcesScrollPane.getViewport().setOpaque(false);
        resourcesScrollPane.setOpaque(false);
        resourcesScrollPane.setBorder(null);
        resourcesScrollPane.addMouseListener(new ResourcesPaneMouseListener());

        contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, translationsPanel, resourcesScrollPane);
        contentPane.setBorder(null);
        contentPane.setDividerSize(10);

        // Style the split pane divider if possible
        SplitPaneUI splitPaneUI = contentPane.getUI();
        if (splitPaneUI instanceof BasicSplitPaneUI) {
            BasicSplitPaneDivider divider = ((BasicSplitPaneUI) splitPaneUI).getDivider();
            divider.setBorder(null);
            resourcesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));
        }

        introText = new JLabel("<html><body style=\"text-align:center; padding:30px;\">" + MessageBundle.get("core.intro.text") + "</body></html>");
        introText.setOpaque(true);
        introText.setFont(introText.getFont().deriveFont(28f));
        introText.setHorizontalTextPosition(SwingConstants.CENTER);
        introText.setVerticalTextPosition(SwingConstants.BOTTOM);
        introText.setHorizontalAlignment(SwingConstants.CENTER);
        introText.setVerticalAlignment(SwingConstants.CENTER);
        introText.setForeground(getBackground().darker());
        introText.setIcon(Images.loadFromClasspath("images/icon-intro.png"));

        Container container = getContentPane();
        container.add(introText);

        editorMenu = new EditorMenuBar(this, translationTree);
        setJMenuBar(editorMenu);
    }

    private void setupFileDrop() {
        new JFileDrop(getContentPane(), null, new JFileDrop.Listener() {
            @Override
            public void filesDropped(java.io.File[] files) {
                try {
                    Path path = Paths.get(files[0].getCanonicalPath());
                    importProject(path, null);
                } catch (IOException e) {
                    log.error("Error importing resources via file drop", e);
                    showError(MessageBundle.get("resources.open.error.multiple"));
                }
            }
        });
    }


    private void setupGlobalKeyEventDispatcher() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED || !e.isAltDown() || (SystemUtils.IS_OS_MAC && !e.isMetaDown()) || (!SystemUtils.IS_OS_MAC && !e.isShiftDown())) {
                return false;
            }
            TreePath selected = translationTree.getSelectionPath();
            if (selected == null) {
                return false;
            }
            boolean result = false;
            int row = translationTree.getRowForPath(selected);
            switch (e.getKeyCode()) {
                case KeyEvent.VK_RIGHT:
                    if (!translationTree.isExpanded(row)) {
                        translationTree.expandRow(row);
                    }
                    result = true;
                    break;
                case KeyEvent.VK_LEFT:
                    if (translationTree.isCollapsed(row)) {
                        translationTree.setSelectionPath(selected.getParentPath());
                    } else {
                        translationTree.collapseRow(row);
                    }
                    result = true;
                    break;
                case KeyEvent.VK_UP:
                    TreePath prev = translationTree.getPathForRow(Math.max(0, row - 1));
                    if (prev != null) {
                        translationTree.setSelectionPath(prev);
                    }
                    result = true;
                    break;
                case KeyEvent.VK_DOWN:
                    TreePath next = translationTree.getPathForRow(row + 1);
                    if (next != null) {
                        translationTree.setSelectionPath(next);
                    }
                    result = true;
                    break;
            }
            if (result && !resourceFields.isEmpty()) {
                Component comp = getFocusOwner();
                if (comp != null && (comp instanceof ResourceField || comp.equals(this))) {
                    TranslationTreeNode current = translationTree.getSelectionNode();
                    if (!current.isLeaf() || current.isRoot()) {
                        requestFocusInWindow();
                    } else if (comp.equals(this)) {
                        requestFocusInFirstResourceField();
                    }
                }
            }
            return result;
        });
    }

    public void setupResource(Resource resource) {
        resource.addListener(e -> setDirty(true));
        ResourceField field = new ResourceField(resource);
        field.addKeyListener(new ResourceFieldKeyListener());
        field.setVisible(false);
        resourceFields.add(field);
    }

    private void updateHistory() {
        List<String> recentDirs = settings.getHistory();
        if (project != null) {
            String path = project.getPath().toString();
            recentDirs.remove(path);
            recentDirs.add(path);
            if (recentDirs.size() > 10) {
                recentDirs.remove(0);
            }
            settings.setHistory(recentDirs);
        }
        editorMenu.setRecentItems(Lists.reverse(recentDirs));
    }

    private void updateTitle() {
        String dirtyPart = dirty ? "*" : "";
        String projectPart = "";
        if (project != null) {
            projectPart = project.getPath().toString() + " [" + project.getResourceType() + "] - ";
        }
        setTitle(dirtyPart + projectPart + TITLE);
    }

    private void showError(String message) {
        Dialogs.showErrorDialog(this, MessageBundle.get("dialogs.error.title"), message);
    }

    private void updateTreeNodeStatuses() {
        if (project == null) return;
        Set<String> keys = project.getResources().stream().flatMap(r -> r.getTranslations().keySet().stream()).filter(key -> project.getResources().stream().anyMatch(r -> !r.hasTranslation(key))).collect(Collectors.toSet());
        translationTree.updateNodes(keys);
    }

    private void updateTreeNodeStatus(String key) {
        if (project == null) return;
        boolean hasError = project.getResources().stream().anyMatch(r -> !r.hasTranslation(key));
        translationTree.updateNode(key, hasError);
    }

    private boolean saveResource(Resource resource) {
        if (project != null) {
            try {
                Resources.write(resource, !project.isMinifyResources(), project.isFlattenJSON());
            } catch (ChecksumException e) {
                boolean confirm = Dialogs.showConfirmDialog(this, MessageBundle.get("dialogs.save.checksum.title"), MessageBundle.get("dialogs.save.checksum.text", resource.getPath()), JOptionPane.WARNING_MESSAGE);
                if (confirm) {
                    resource.setChecksum(null);
                    saveResource(resource);
                } else {
                    return false;
                }
            } catch (IOException e) {
                log.error("Error saving resource file " + resource.getPath(), e);
                showError(MessageBundle.get("resources.write.error.single", resource.getPath().toString()));
                return false;
            }
        }
        return true;
    }

    private void storeProjectState() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("minify_resources", project.isMinifyResources());
        props.setProperty("flatten_json", project.isFlattenJSON());
        props.setProperty("resource_type", ResourceType.JSON);
        props.setProperty("resource_definition", EditorSettings.DEFAULT_RESOURCE_FILE_DEFINITION);
        props.setProperty("resource_structure", project.getResourceFileStructure());
        props.store(Paths.get(project.getPath().toString(), PROJECT_FILE));

    }

    private void restoreProjectState(EditorProject project) {
        ExtendedProperties props = new ExtendedProperties();
        Path path = Paths.get(project.getPath().toString(), PROJECT_FILE);
        if (Files.exists(path)) {
            props.load(Paths.get(project.getPath().toString(), PROJECT_FILE));
            project.setMinifyResources(props.getBooleanProperty("minify_resources", settings.isMinifyResources()));
            project.setFlattenJSON(props.getBooleanProperty("flatten_json", settings.isFlattenJSON()));
            project.setResourceType(props.getEnumProperty("resource_type", ResourceType.class));
            String resourceName = props.getProperty("resource_name"); // for backwards compatibility
            if (Strings.isNullOrEmpty(resourceName)) {
                project.setResourceFileDefinition(props.getProperty("resource_definition", EditorSettings.DEFAULT_RESOURCE_FILE_DEFINITION));
                project.setResourceFileStructure(props.getEnumProperty("resource_structure", FileStructure.class, settings.getResourceFileStructure()));
            } else {
                // for backwards compatibility
                project.setResourceFileDefinition(resourceName);
                project.setResourceFileStructure(project.getResourceType() == ResourceType.Properties ? FileStructure.Flat : FileStructure.Nested);
            }
        } else {
            project.setMinifyResources(settings.isMinifyResources());
            project.setFlattenJSON(settings.isFlattenJSON());
            project.setResourceFileDefinition(settings.getResourceFileDefinition());
            project.setResourceFileStructure(settings.getResourceFileStructure());
        }
    }

    private void storeEditorState() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("window_width", getWidth());
        props.setProperty("window_height", getHeight());
        props.setProperty("window_pos_x", getX());
        props.setProperty("window_pos_y", getY());
        props.setProperty("window_div_pos", contentPane.getDividerLocation());
        props.setProperty("minify_resources", settings.isMinifyResources());
        props.setProperty("flatten_json", settings.isFlattenJSON());
        props.setProperty("resource_definition", EditorSettings.DEFAULT_RESOURCE_FILE_DEFINITION);
        props.setProperty("resource_structure", settings.getResourceFileStructure());
        props.setProperty("check_version", settings.isCheckVersionOnStartup());
        props.setProperty("default_input_height", settings.getDefaultInputHeight());
        props.setProperty("key_field_enabled", settings.isKeyFieldEnabled());
        props.setProperty("double_click_tree_toggling", settings.isDoubleClickTreeToggling());
        props.setProperty("close_in_error", settings.closeInError);
        if (settings.getEditorLanguage() != null) {
            props.setProperty("editor_language", settings.getEditorLanguage());
        }
        if (!settings.getHistory().isEmpty()) {
            props.setProperty("history", settings.getHistory());
        }
        if (project != null) {
            // Store keys of expanded nodes
            List<String> expandedNodeKeys = translationTree.getExpandedNodes().stream().map(TranslationTreeNode::getKey).collect(Collectors.toList());
            props.setProperty("last_expanded", expandedNodeKeys);
            // Store key of selected node
            TranslationTreeNode selectedNode = translationTree.getSelectionNode();
            props.setProperty("last_selected", selectedNode == null ? "" : selectedNode.getKey());
        }
        props.store(Paths.get(SETTINGS_DIR, SETTINGS_FILE));
    }

    private void restoreEditorState() {
        ExtendedProperties props = new ExtendedProperties();
        props.load(Paths.get(SETTINGS_DIR, SETTINGS_FILE));
        settings.setWindowWidth(props.getIntegerProperty("window_width", 1024));
        settings.setWindowHeight(props.getIntegerProperty("window_height", 768));
        settings.setWindowPositionX(props.getIntegerProperty("window_pos_x", 0));
        settings.setWindowPositionY(props.getIntegerProperty("window_pos_y", 0));
        settings.setWindowDeviderPosition(props.getIntegerProperty("window_div_pos", 250));
        settings.setCheckVersionOnStartup(props.getBooleanProperty("check_version", true));
        settings.setDefaultInputHeight(props.getIntegerProperty("default_input_height", 5));
        settings.setKeyFieldEnabled(props.getBooleanProperty("key_field_enabled", true));
        settings.setDoubleClickTreeToggling(props.getBooleanProperty("double_click_tree_toggling", false));
        settings.setMinifyResources(props.getBooleanProperty("minify_resources", false));
        settings.setFlattenJSON(props.getBooleanProperty("flatten_json", false));
        settings.setHistory(props.getListProperty("history"));
        settings.setLastExpandedNodes(props.getListProperty("last_expanded"));
        settings.setLastSelectedNode(props.getProperty("last_selected"));
        settings.setResourceFileStructure(props.getEnumProperty("resource_structure", FileStructure.class, FileStructure.Flat));
        settings.setEditorLanguage(props.getLocaleProperty("editor_language"));
        settings.setCloseInError(props.getBooleanProperty("close_in_error", false));
    }

    private class TranslationTreeMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            showPopupMenu(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            showPopupMenu(e);
        }

        private void showPopupMenu(MouseEvent e) {

            if (!e.isPopupTrigger() || project == null) {
                return;
            }
            TreePath path = translationTree.getPathForLocation(e.getX(), e.getY());
            if (path == null) {
                TranslationTreeMenu menu = new TranslationTreeMenu(Editor.this, translationTree);
                menu.show(e.getComponent(), e.getX(), e.getY());
            } else {
                translationTree.setSelectionPath(path);
                TranslationTreeNode node = translationTree.getSelectionNode();
                TranslationTreeNodeMenu menu = new TranslationTreeNodeMenu(Editor.this, node);
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private class TranslationTreeNodeSelectionListener implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            TranslationTreeNode node = translationTree.getSelectionNode();

            if (node != null) {
                // Store scroll position
                int scrollValue = resourcesScrollPane.getVerticalScrollBar().getValue();

                // Update UI values
                String key = node.getKey();


                translationField.setValue(key);
                resourceFields.forEach(x -> {

                    if (node.typeFile != TypeFile.ELEMENT) {
                        resourcesPanel.setVisible(false);
                    } else if (x.getResource().getPath().getParent().getParent().toString().equals(Utils.getPathOfNode(node, project).toString())

                    ) {
                        resourcesPanel.setVisible(true);
                        x.setVisible(true);
                        jLabels.get(x).setVisible(true);
                        x.setValue(key);
                        if (project.getResourceFileStructure() == FileStructure.Nested) {
                            String trunkateKey = key.replace(Utils.getNameTrunk(node) + ".", "");
                            x.setValue(trunkateKey);
                        }
                        x.setEnabled(node.isEditable() && (node.isLeaf() || x.getResource().supportsParentValues()));
                    } else {

                        x.setVisible(false);
                        jLabels.get(x).setVisible(false);
                    }
                });


                // Restore scroll position and foc
                SwingUtilities.invokeLater(() -> resourcesScrollPane.getVerticalScrollBar().setValue(scrollValue));
            }
        }
    }

    private class TranslationFieldKeyListener extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                TranslationKeyField field = (TranslationKeyField) e.getSource();
                String key = field.getValue();

                addTranslation(key);
            }
        }
    }

    private class ResourcesPaneMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            showPopupMenu(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            showPopupMenu(e);
        }

        private void showPopupMenu(MouseEvent e) {
            if (!e.isPopupTrigger() || project == null) {
                return;
            }
            ResourcesPaneMenu menu = new ResourcesPaneMenu(Editor.this);
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private class ResourceFieldKeyListener extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            TranslationTreeNode node = translationTree.getSelectionNode();
            ResourceField field = (ResourceField) e.getSource();
            String key = node.getKey();
            String value = field.getValue();
            field.getResource().storeTranslation(key, value);
            updateTreeNodeStatus(key);
        }
    }

    private class EditorWindowListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            if (closeCurrentProject()) {
                storeEditorState();
                System.exit(0);
            }
        }
    }
}

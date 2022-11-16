package com.jvms.i18neditor.editor;

import com.jvms.i18neditor.ResourceType;
import com.jvms.i18neditor.swing.JHelpLabel;
import com.jvms.i18neditor.swing.JTextField;
import com.jvms.i18neditor.util.MessageBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * This class represents the project settings pane.
 *
 * @author Jacob van Mourik
 */
public class EditorProjectSettingsPane extends AbstractSettingsPane {
    private final static long serialVersionUID = 5665963334924596315L;
    private final Editor editor;

    public EditorProjectSettingsPane(Editor editor) {
        super();
        this.editor = editor;
        this.setupUI();
    }


    private void setupUI() {
        EditorProject project = editor.getProject();

        // General settings
        JPanel fieldset1 = createFieldset(MessageBundle.get("settings.fieldset.general"));

        ComboBoxFileStructure currentFileStructureItem = null;
        for (ComboBoxFileStructure item : fileStructureComboBoxItems) {
            if (item.getStructure().equals(project.getResourceFileStructure())) {
                currentFileStructureItem = item;
                break;
            }
        }
        JPanel fileStructurePanel = new JPanel(new GridLayout(0, 1));
        JLabel fileStructureLabel = new JLabel(MessageBundle.get("settings.filestructure.title"));
        JComboBox fileStructureField = new JComboBox(fileStructureComboBoxItems.toArray());
        fileStructureField.setSelectedItem(currentFileStructureItem);
        fileStructureField.addActionListener(e -> {
            project.setResourceFileStructure(((ComboBoxFileStructure) fileStructureField.getSelectedItem()).getStructure());

        });
        fileStructurePanel.add(fileStructureLabel);
        fileStructurePanel.add(fileStructureField);
        fieldset1.add(fileStructurePanel, createVerticalGridBagConstraints());



        ResourceType type = project.getResourceType();
        if (type == ResourceType.JSON || type == ResourceType.ES6) {
            JCheckBox minifyBox = new JCheckBox(MessageBundle.get("settings.minify.title"));
            minifyBox.setSelected(project.isMinifyResources());
            minifyBox.addChangeListener(e -> project.setMinifyResources(minifyBox.isSelected()));
            fieldset1.add(minifyBox, createVerticalGridBagConstraints());

            JCheckBox flattenJSONBox = new JCheckBox(MessageBundle.get("settings.flattenjson.title"));
            flattenJSONBox.setSelected(project.isFlattenJSON());
            flattenJSONBox.addChangeListener(e -> project.setFlattenJSON(flattenJSONBox.isSelected()));
            fieldset1.add(flattenJSONBox, createVerticalGridBagConstraints());
        }

        setLayout(new GridBagLayout());
        add(fieldset1, createVerticalGridBagConstraints());
    }
}

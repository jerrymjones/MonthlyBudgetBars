/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2023, Jerry Jones
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 
package com.moneydance.modules.features.budgetbars;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.moneydance.awt.AwtUtil;
import com.moneydance.awt.GridC;

/**
 * This class implements the budget bar edit dialog
 *
 * @author  Jerry Jones
 */
public class BudgetBarEdit extends JDialog implements ChangeListener
{
    // The parent widget
    BudgetBars parent;

    // List of available monthly budgets that can be edited
    private MyBudgetList budgetList;

    // Controls needing access from outside the constructor
    JComboBox<String> budgetSelector;
    JCheckBox showFullNames;
    JCheckBox showAllAncestors;
    private JSlider warning;
    private JLabel warningValLabel;
    private JSlider over;
    private JLabel overValLabel;

    // The available category list
    private DefaultListModel<CategoryListItem> availableModel;
    private JList<CategoryListItem> availableList;

    // The selected category list
    private DefaultListModel<CategoryListItem> selectedModel;
    private JList<CategoryListItem> selectedList;

    // Flag indicating selection list changes
    private boolean selChanged = false;

    // Flag indicating warning or over budget state changed
    private boolean stateChanged = false;

    // Settings instance
    Settings settings;

    /**
     * @param parent - The parent widget this editor is for.
     */
    public BudgetBarEdit(final BudgetBars parent) 
    {
        super();

        // Save the parent for later
        this.parent = parent;

        // Title for the edit window
        this.setTitle("Monthly Budget Bar Settings");

        // Get the settings for the widget
        this.settings = Settings.getInstance();

        // Get the budgetList
        this.budgetList = parent.getBudgetList();

        // We can't continue if no monthly budgets are defined
        if (this.budgetList.getBudgetCount() == 0)
            {
            // Display an error message - No budgets exist!
            JOptionPane.showMessageDialog( null,
            "No monthly style budgets have been created.  Use 'Tools:Budget Manager' to create a monthly budget before using this extension.",
            "Error (Monthly Budget Bars)",
            JOptionPane.ERROR_MESSAGE);
                
            // Exit from the constructor
            return;
            }

        /*
        * Configure the dialog 
        */
        // Set the window non-resizable and make it application modal
        this.setResizable(false);
        this.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);

        // Set what to do on close
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.enableEvents(WindowEvent.WINDOW_CLOSING);

        /*
        * Add the Top Panel
        */  
        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        this.add( topPanel, BorderLayout.NORTH );   

        /*
        * Create a budget selector
        */
        // Label
        final JLabel budgetLabel = new JLabel("Budget:");
        budgetLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        topPanel.add(budgetLabel, GridC.getc(0, 1).insets(20, 0, 10, 0).east());

        // Create the selector
        final String strNames[] = this.budgetList.getBudgetNames();
        this.budgetSelector = new JComboBox<String>(strNames);
        this.budgetSelector.setSelectedItem(this.settings.getBudgetName());    
        this.budgetSelector.setToolTipText("Select the budget to report");  
        topPanel.add(this.budgetSelector, GridC.getc(1, 1).insets(20, 10, 10, 0).fillx());
        
        /*
        ** Show full category names checkbox
        */
        this.showFullNames = new JCheckBox("Show full category names");
        this.showFullNames.setSelected(this.settings.getUseFullNames());
        this.showFullNames.setToolTipText("Select to show full category names on the budget bars");
        topPanel.add(this.showFullNames,GridC.getc(1, 2).insets(10, 10, 10, 0).fillx());

        /*
        ** Show all ancestors checkbox
        */
        this.showAllAncestors = new JCheckBox("Show all ancestors on the budget bar tooltips");
        this.showAllAncestors.setSelected(this.settings.getAllAncestors());
        this.showAllAncestors.setToolTipText("Select to show all ancestors not just direct children on the budget bar tooltips");
        topPanel.add(this.showAllAncestors,GridC.getc(1, 3).insets(10, 10, 10, 0).fillx());

        /*
        * Field to set the warning level
        */
        // Label
        final JLabel warningLabel = new JLabel("Warning Level (%):");
        warningLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        topPanel.add(warningLabel, GridC.getc(0, 4).insets(10, 0, 0, 0).east());

        // Add a slider to set the level from 50.0% to 150.0%
        this.warning = new JSlider(500, 1500);
        this.warning.setValue(Math.round(this.settings.getWarningLevel() * 10));
        topPanel.add(this.warning,GridC.getc(1, 4).insets(10, 10, 0, 0).fillx());

        // Value label
        this.warningValLabel = new JLabel();
        this.warningValLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.warningValLabel.setText(String.valueOf(this.warning.getValue() / 10.0f));
        topPanel.add(this.warningValLabel, GridC.getc(1, 5).insets(0, 0, 10, 0).center());

        // Add a change listener so we can update the warningValLabel
        this.warning.addChangeListener(this);

        /*
        * Field to set the over budget level
        */
        // Label
        final JLabel overLabel = new JLabel("Over Budget level (%):");
        overLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        topPanel.add(overLabel, GridC.getc(0, 6).insets(10, 0, 0, 0).east());

        // Add a slider to set the level from 50.0% to 150.0%
        this.over = new JSlider(Math.round(this.settings.getWarningLevel() * 10), Math.round(this.settings.getWarningLevel() * 10) + 500);
        this.over.setValue(Math.round(this.settings.getOverBudgetLevel() * 10));
        topPanel.add(this.over,GridC.getc(1, 6).insets(10, 10, 0, 0).fillx());

        // Value label
        this.overValLabel = new JLabel();
        this.overValLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.overValLabel.setText(String.valueOf(this.over.getValue() / 10.0f));
        topPanel.add(this.overValLabel, GridC.getc(1, 7).center());

        // Add a change listener so we can update the warningValLabel
        this.over.addChangeListener(this);

        /*
        * Add the middle left Panel
        */  
        final JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        this.add( leftPanel, BorderLayout.LINE_START );  

        // Add the Available Categories label
        leftPanel.add(new JLabel("Available Categories"), GridC.getc(0, 0).insets(10, 0, 0, 0));

        // Create the list model for the available list
        this.availableModel = new DefaultListModel<CategoryListItem>();

        // Load the available categories list
        this.loadAvailableList(this.parent.getSelectedCats().toArray());

        // Create the "Available" list component
        this.availableList = new JList<CategoryListItem>(this.availableModel);
        this.availableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
        this.availableList.setVisibleRowCount(10);
        final JScrollPane available = new JScrollPane( this.availableList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        available.getViewport().setPreferredSize(new Dimension(300, available.getViewport().getPreferredSize().height)); // Set size and allow scrollbars to work
        leftPanel.add(available, GridC.getc(0, 1).insets(5, 15, 36, 15));

        /*
        * Add the middle center Panel
        */  
        final JPanel midPanel = new JPanel();
        midPanel.setLayout(new GridBagLayout());
        this.add( midPanel, BorderLayout.CENTER );  

        // Remove button
        final JButton remButton = new JButton("< Remove");
        remButton.setToolTipText("Remove item(s) from the selected categories");
        midPanel.add(remButton,GridC.getc(0,1).insets(10,0,10,0));

        // Create an action listener to dispatch the action when this button is clicked
        remButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                BudgetBarEdit.this.removeAction();
                }
            });

        // Add button
        final JButton addButton = new JButton("Add >");
        addButton.setToolTipText("Add item(s) to the selected categories");
        addButton.setPreferredSize(remButton.getPreferredSize()); // Make buttons the same size
        midPanel.add(addButton,GridC.getc(0,0).insets(10,0,10,0));

        // Create an action listener to dispatch the action when this button is clicked
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                BudgetBarEdit.this.addAction();
                }
            });

        /*
        * Add the middle right Panel
        */  
        final JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        this.add( rightPanel, BorderLayout.LINE_END );  

        // Add the Selected Categories label
        rightPanel.add(new JLabel("Selected Categories"), GridC.getc(0, 0).insets(10, 0, 0, 0));

        // Create and fill the list model for the selected list
        this.selectedModel = new DefaultListModel<CategoryListItem>();
        for (final CategoryListItem item: parent.getSelectedCats()) 
            this.selectedModel.addElement(item);

        // Create the "Selected" list
        this.selectedList = new JList<CategoryListItem>(this.selectedModel);
        this.selectedList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
        this.selectedList.setVisibleRowCount(10);
        final JScrollPane selected = new JScrollPane( this.selectedList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        selected.getViewport().setPreferredSize(new Dimension(300, available.getViewport().getPreferredSize().height)); // Set size and allow scrollbars to work
        rightPanel.add(selected, GridC.getc(0, 1).insets(5, 15, 0, 15));

        /*
        * Add the sub Panel for the up/down buttons
        */  
        final JPanel subPanel = new JPanel();
        subPanel.setLayout(new GridBagLayout());
        rightPanel.add( subPanel, GridC.getc(0, 2));  

        // Up button
        final JButton upButton = new JButton("Up");
        upButton.setToolTipText("Move selected item(s) up");
        subPanel.add(upButton, GridC.getc(0,0).insets(10,10,10,10));

        // Create an action listener to dispatch the action when this button is clicked
        upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                BudgetBarEdit.this.upAction();
                }
            });

        // Down button
        final JButton downButton = new JButton("Down");
        downButton.setToolTipText("Move selected item(s) down");
        subPanel.add(downButton, GridC.getc(1,0).insets(10,10,10,10));

        // Create an action listener to dispatch the action when this button is clicked
        downButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                BudgetBarEdit.this.downAction();
                }
            });

        // Make buttons the same size
        upButton.setPreferredSize(downButton.getPreferredSize());  

        /*
        * Add the Bottom Panel - Action Buttons
        */  
        final JPanel bottomPanel = new JPanel(new GridBagLayout());
        this.add( bottomPanel, BorderLayout.SOUTH ); 
        
        /*
        ** Save Button
        */
        final JButton saveButton = new JButton("Save");
        saveButton.setToolTipText("Save the current settings and exit");
        bottomPanel.add(saveButton,GridC.getc(0,0).insets(10,10,20,10));

        // Create an action listener to dispatch the action when this button is clicked
        saveButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            BudgetBarEdit.this.save();
            }
        });

        /*
        ** Cancel Button
        */
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Cancel budget editing and exit");
        bottomPanel.add(cancelButton,GridC.getc(1,0).insets(10,10,20,10));
        
        // Create an action listener to dispatch the action when this button is clicked
        cancelButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            BudgetBarEdit.this.cancel();
            }
        });

        // Cause the window to be sized according to the contents
        this.pack();

        // Center the frame on the screen
        AwtUtil.centerWindow(this);

        // Set the window visible
        this.setVisible(true);
    }

    /** 
     * Processes events on this window.
     * 
     * @param e - The event that sent us here.
     */
    public final void processEvent(final AWTEvent e) 
    {
        if(e.getID() == WindowEvent.WINDOW_CLOSING) 
           this.cancel(); 

        super.processEvent(e);
    }

    /**
     * Method called when a state change event occurs on one of the sliders used
     * to configure the warning and over budget limits. This displays the current
     * value of the bars as well as keeping the controls within bounds.
     * 
     * @param e - The event that sent us here. 
     */
    @Override
    public void stateChanged(final ChangeEvent e) {
        // Flag state change
        this.stateChanged = true;

        // Process the slider change
        if (e.getSource() == this.warning)
            {
            // Update the warning level value display
            BudgetBarEdit.this.warningValLabel.setText(String.valueOf(BudgetBarEdit.this.warning.getValue() / 10.0f));

            // Update the over budget slider minimum and maximum based on the warning level
            BudgetBarEdit.this.over.setMaximum(BudgetBarEdit.this.warning.getValue() + 500);
            BudgetBarEdit.this.over.setMinimum(BudgetBarEdit.this.warning.getValue());

            // Make sure the value of the over budget slider is within the new range
            if (BudgetBarEdit.this.over.getValue() < BudgetBarEdit.this.over.getMinimum())
                BudgetBarEdit.this.over.setValue(BudgetBarEdit.this.over.getMinimum());
            if (BudgetBarEdit.this.over.getValue() > BudgetBarEdit.this.over.getMaximum())
                BudgetBarEdit.this.over.setValue(BudgetBarEdit.this.over.getMaximum());
            }
        else if (e.getSource() == this.over)
            {
            // Update the over budget level value display
            BudgetBarEdit.this.overValLabel.setText(String.valueOf(BudgetBarEdit.this.over.getValue() / 10.0f));

            // Make sure the value of the warning slider is in range
            if (BudgetBarEdit.this.warning.getValue() > BudgetBarEdit.this.over.getValue())
                BudgetBarEdit.this.warning.setValue(BudgetBarEdit.this.over.getValue() - 1);
            }
    }
    
    /**
     * Method to load the available categories list.
     */
    void loadAvailableList(final Object[] selectedList)
    {
        // Get the available categories
        final DataModel dataModel = this.parent.getDataModel();
        for (final String UUID: dataModel.getBudgetCategoriesList().getKeySet())
            {
            // Flag to indicate if this UUID is found on the selected list
            boolean found = false;
            
            // Search the selected categories list and see if this UUID
            // is on it.
            for (final Object item: selectedList) 
                {
                if (item instanceof CategoryListItem)
                    {
                    // If we find it, flag it and stop searching further
                    if (((CategoryListItem)item).getUUID().equals(UUID))
                        {
                        found = true;
                        break;
                        }
                    }
                }
            
            // If not on the selected list, add the category to the available model
            if (!found)
                this.availableModel.addElement(new CategoryListItem(UUID, dataModel.getCategoryItem(UUID).getFullName()));
            }
    }
    
    /**
     * Remove item(s) from the selected items list.
     */
    private void removeAction()
    {
        // Get the selected items
        final int[] selIndices = this.selectedList.getSelectedIndices();

        // Inform user if nothing has been selected
        if (selIndices.length == 0)
            {
            // Display a warning message - Duplicate UUID!
            JOptionPane.showMessageDialog( this,
            "You must select one or more items in the SelectedCategories list first!",
            "Select Item(s)",
            JOptionPane.INFORMATION_MESSAGE);
            return;
            }

        // For each selected item in the array, we remove them from the selected items list.
        // We have to go backwards so the indices remain valid.
        for (int i = selIndices.length - 1; i >= 0 ; i--)
            {
            // Remove from the selected items
            this.selectedModel.remove(selIndices[i]);
            }

        /*
         * We don't just add the category back to the available categories list
         * because we want the item(s) to be in their proper locations in the 
         * list.
         */

        // Clear the available categories list
        this.availableModel.clear();

        // Re-load the available categories list
        this.loadAvailableList(this.selectedModel.toArray());

        // Flag that selection data has changed
        this.selChanged = true;
    }
    
    /**
     * Add item(s) to the selected items list.
     */
    private void addAction()
    {
        // Get the selected items
        final int[] selIndices = this.availableList.getSelectedIndices();

        // Inform user if nothing has been selected
        if (selIndices.length == 0)
            {
            // Display a warning message - Duplicate UUID!
            JOptionPane.showMessageDialog( this,
            "You must select one or more items in the Available Categories list first!",
            "Select Item(s)",
            JOptionPane.INFORMATION_MESSAGE);
            return;
            }

        // For each selected item in the array, we move them over to the selected items list
        // and remove them from the available items list
        for (int i = 0; i < selIndices.length; i++)
            {
            // Add to the selected items
            this.selectedModel.addElement(this.availableModel.get(selIndices[i]));

            // Remove from the available items
            this.availableModel.remove(selIndices[i]);
            }

        // Flag that selection data has changed
        this.selChanged = true;
    }

    /**
     * Move item(s) up in the selected items list.
     */
    private void upAction()
    {
        final int[] selectedIndices = this.selectedList.getSelectedIndices();
        final int[] resultingIndices = this.selectedList.getSelectedIndices();
         
        // Inform user if nothing has been selected
        if (selectedIndices.length == 0)
            {
            // Display a warning message - Duplicate UUID!
            JOptionPane.showMessageDialog( this,
            "You must select one or more items in the Selected Categories list first!",
            "Select Item(s)",
            JOptionPane.INFORMATION_MESSAGE);
            return;
            }

        // For each selected item in the array, we move them up one level
        for (int i = 0; i < selectedIndices.length; i++)
            {
            // Top item in the list cannot be moved up
            if (selectedIndices[i] != 0)
                {
                // Add the item one step higher in the list
                this.selectedModel.add(selectedIndices[i] - 1, this.selectedModel.get(selectedIndices[i]));

                // Remove the original item on the list
                this.selectedModel.remove(selectedIndices[i] + 1);

                // Set the new selection index
                resultingIndices[i] -= 1;
                }
            else
                {
                // This item was already at the top so we'll just set it to a value that will be ignored.
                // Indices greater than or equal to the model size are ignored in the call to setSelectedIndices.
                resultingIndices[i] = this.selectedModel.getSize();
                }
            }

        // Re-select the now moved items
        this.selectedList.setSelectedIndices(resultingIndices);

        // Flag that selection data has changed
        this.selChanged = true;
    }

     /**
     * Move item(s) down in the selected items list.
     */
    private void downAction()
    {
        final int[] selectedIndices = this.selectedList.getSelectedIndices();
        final int[] resultingIndices = this.selectedList.getSelectedIndices();
        
        // Inform user if nothing has been selected
        if (selectedIndices.length == 0)
            {
            // Display a warning message - Duplicate UUID!
            JOptionPane.showMessageDialog( this,
            "You must select one or more items in the Selected Categories list first!",
            "Select Item(s)",
            JOptionPane.INFORMATION_MESSAGE);
            return;
            }

        // For each selected item in the array, we move them down one level
        for(int i = 0; i < selectedIndices.length; i++)
            {
            // Bottom item in the list cannot be moved down
            if (selectedIndices[i] < this.selectedModel.getSize() - 1)
                {
                // Add it 1 step lower in the list
                this.selectedModel.add(selectedIndices[i] + 2, this.selectedModel.get(selectedIndices[i]));

                // Remove the original item
                this.selectedModel.remove(selectedIndices[i]);

                // Set the new selection index
                resultingIndices[i] += 1;
                }
            else
                {
                // This item was already at the bottom so we'll just set it to a value that will be ignored.
                // Indices greater than or equal to the model size are ignored in the call to setSelectedIndices.
                resultingIndices[i] = this.selectedModel.getSize();
                }
            }

        // Re-select the now moved items
        this.selectedList.setSelectedIndices(resultingIndices);

        // Flag that selection data has changed
        this.selChanged = true;
    }

    /**
     * Method to determine if any changes have been made to the configuration
     * other than the selected categories.
     *  
     * @return - true if changes were made, false otherwise.
     */
    private boolean isDataChanged()
    {
        // Did the budget change
        if (!(this.budgetSelector.getSelectedItem().toString()).equals(this.settings.getBudgetName()))
            return true;

        if (this.showFullNames.isSelected() != this.settings.getUseFullNames() )
            return true;

        if (this.showAllAncestors.isSelected() != this.settings.getAllAncestors() )
        return true;

        // Did the sliders change?
        if (this.stateChanged)
            return true;
 
        // No changes were made
        return false;
    }

    /**
     * Method to save changes made in the edit dialog.
     */
    private void save()
    {
        // Has anything changed?
        if ((this.selChanged) || (this.isDataChanged()))
            {  
            // Save the other parameters if they changed
            if (this.isDataChanged())
                {
                    // Gather the values from the dialog controls
                    this.settings.setBudgetName(this.budgetSelector.getSelectedItem().toString());
                    this.settings.setUseFullNames(this.showFullNames.isSelected());
                    this.settings.setAllAncestors(this.showAllAncestors.isSelected());
                    this.settings.setWarningLevel(this.warning.getValue() / 10.0f);
                    this.settings.setOverBudgetLevel(this.over.getValue() / 10.0f);

                    // Save the settings
                    this.settings.saveSettings();
                }

            // Save the selected categories if they changed
            if (this.selChanged)
                {
                // String to hold all the selected category UUID's
                String selCatList = "";

                // Get all of the selected categories into a comma separated string
                for (int i = 0; i < this.selectedModel.size(); i++)
                    {
                    selCatList += this.selectedModel.get(i).getUUID();
                    if (i < this.selectedModel.size() - 1)
                        selCatList += ",";
                    }

                // Save the settings
                this.parent.getRootAccount().setPreference(Constants.CATEGORIES_SELECTED, selCatList);
                }

            // Tell the parent that our configuration changed
            this.parent.configurationChanged();
            }

        // Hide the edit dialog
        this.setVisible(false);
    }

    /**
     * Method to cancel any changes made in the edit dialog.
     */
    private void cancel()
    {
        // Prompt with a warning if the data has changed else just close the console
        if ((this.selChanged) || (this.isDataChanged()))
            {
            final int response = JOptionPane.showConfirmDialog( this,
            "The configuration has been edited. Would you like to save the changes before leaving?",
            "Configuration Edited",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response == 0)      // Yes, go save the changes
                this.save();
            else if (response == 2) // Cancel
                return;
            }

        // Hide the edit dialog
        this.setVisible(false);
    }
}

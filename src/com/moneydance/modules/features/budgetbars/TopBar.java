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


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * This class creates the top bar for the Budget Bars.
 *
 * @author  Jerry Jones
 */
public class TopBar extends JPanel
{
    // The parent Budget Bars extension
    private final BudgetBars parent;

    private final JLabel periodSelector;

    /**
     * Constructor method used to create the top bar for the Monthly Budget Bars.
     * 
     * @param BudgetBars parent - The parent of this bar
     */
    public TopBar(final BudgetBars parent) 
    {
        // Call the default JPanel constructor
        super();

        // Save the parent that instantiated the top bar
        this.parent = parent;

        // Set the layout manager for this bar
        this.setLayout(new BorderLayout(10,10));

        // Allow the underlying panel to show through
        this.setOpaque(false);

        // Create and initialize the periodSelector
        this.periodSelector = new JLabel("\u23F7 "+Constants.periods[Settings.getInstance().getPeriod()]);
        this.add(this.periodSelector, BorderLayout.LINE_START);

        // Create an action listener to dispatch the action when this label is clicked
        this.periodSelector.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
            TopBar.this.selectPeriod();
            }
        });

        /*
        ** Edit Button
        */
        final JButton editButton = new JButton("Edit");
        editButton.setToolTipText("Change the Budget Bar settings.");
        this.add(editButton, BorderLayout.LINE_END);

        // Create an action listener to dispatch the action when this button is clicked
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
            TopBar.this.doEdit();
            }
        });

        // Add a gap at the bottom of the budget bar
        this.add(Box.createRigidArea(new Dimension(0, 5)), BorderLayout.PAGE_END);
    }


    void doEdit()
    {
        new BudgetBarEdit(this.parent);
    }

    /**
     * This class extends JMenuItem adding an ID 
     *
     * @author  Jerry Jones
     */
    public class TopBarMenuItem extends JMenuItem
    {
        // Identifier for this menu item
        int id;

        /**
         * Constructor method used to create the top bar menu items.
         * 
         * @param menu - The pop-up menu to add the item to.
         * @param id - The identifier that will be used to determine what item was selected.
         * @param text - The text of the pop-up menu item.
         * @param listener - The action listener for this item.
         */
        public TopBarMenuItem(final JPopupMenu menu, final int id, final String text, final ActionListener listener) 
        {
            // Call the super class constructor
            super();

            // Save the identifier for this menu item
            this.id = id;

            // Set the text to display for this menu item
            this.setText(text);

            // Add an action listener for the menu item
            this.addActionListener(listener);

            // Add the popup menu item to the menu
            menu.add(this);
        }

        /**
         * Method to return the ID of a menu item
         * 
         * @return the id
         */
        public int getId() {
            return this.id;
        }
    }

    /**
     * Action method called when the User Guide label is clicked. This method
     * displays a brief help message for the extension.
     */
    private void selectPeriod() 
    {
       // Create new popup menu
        final JPopupMenu popMenu = new JPopupMenu();

        // Add the popup menu items
        for (int i = 0; i < Constants.periods.length; i++)
            new TopBarMenuItem(popMenu, i, Constants.periods[i], this.popListener);            
       
        // Show the popup menu
        popMenu.show(this, 0, this.periodSelector.getHeight());
    }

    /**
	 * Create the action listener to receive menu item events.
     */
    ActionListener popListener = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent event) 
        {
            // Retrieve the popup menu item selected
            final int id = ((TopBarMenuItem) event.getSource()).getId();

            // Update the selector 
            TopBar.this.periodSelector.setText("\u23F7 "+Constants.periods[id]);

            // Tell our parent that the period changed
            TopBar.this.parent.periodChanged(id);
        }
    };
}


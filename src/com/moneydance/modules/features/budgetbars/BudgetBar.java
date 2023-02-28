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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ToolTipManager;

import com.moneydance.apps.md.view.gui.MoneydanceGUI;

/**
 * This class creates the individual budget bars for each selected category
 *
 * @author  Jerry Jones
 */
public class BudgetBar extends JPanel 
{
    // The controls on this panel needing access outside the constructor
    private JLabel categoryLabel = null;
    private JLabel budgetLabel = null;
    private JProgressBar progressBar = null;
    private JLabel spentLabel = null;

    // Storage for the passed in parameters
    private final DataModel dataModel;
    private final String UUID;

     /**
     * Constructor to create a JPanel to display a single budget bar
     * 
     * @param mdGUI - The Moneydance GUI
     * @param dataModel - The data model used for the data to display
     * @param UUID - The UUID of the account to display on this budget bar
     */
    public BudgetBar(final MoneydanceGUI mdGUI, final DataModel dataModel, final String UUID) 
    {
        // Call the JPanel constructor
        super();

        // Save parameters for later
        this.dataModel = dataModel;
        this.UUID = UUID;

        // Set the layout of the panel
        this.setLayout(new BorderLayout(10,2));

        // Allow the underlying panel to show through so the background color 
        // is correct
        this.setOpaque(false);

        // Display the category name
        this.categoryLabel = new JLabel("", JLabel.CENTER);
        this.add(this.categoryLabel, BorderLayout.PAGE_START);

        // Display the budget amount left to spend at the left end of the progress bar
        this.spentLabel = new JLabel("", JLabel.RIGHT);
        this.spentLabel.setPreferredSize(new Dimension(100, this.spentLabel.getHeight()));
        this.add(this.spentLabel, BorderLayout.LINE_START);

        // Create and configure the progress bar
        this.progressBar = new JProgressBar();
        this.progressBar.setStringPainted(false);
        this.progressBar.setBackground(mdGUI.getColors().sidebarBackground);
        this.add(this.progressBar, BorderLayout.CENTER);

        // Display the budget value at the right end of the budget bar
        this.budgetLabel = new JLabel("", JLabel.RIGHT);
        this.budgetLabel.setPreferredSize(new Dimension(100, this.budgetLabel.getHeight()));
        this.add(this.budgetLabel, BorderLayout.LINE_END);
        
        // Add a gap at the bottom of the budget bar
        this.add(Box.createRigidArea(new Dimension(0, 15)), BorderLayout.PAGE_END);
    }

    /**
     * Method to refresh the data displayed on this bar
     */
    public void refresh()
    {

        // The data model must be valid
        if (this.dataModel == null)
            return;

        // Get the settings for the widget
        final Settings settings = Settings.getInstance();

        // Get the budget category item
        final BudgetCategoryItem item = this.dataModel.getCategoryItem(this.UUID);

        // Retrieve the values for this category
        final Double budget = (Double)(item.getBudgetTotal() / 100.0d); 
        final Double actual = (Double)(item.getActualTotal() / 100.0d);
        
        // Update the category label
        if (this.categoryLabel != null)
            {
            if (settings.getUseFullNames())
                this.categoryLabel.setText(item.getFullName());
            else
                this.categoryLabel.setText(item.getShortName());
            }

        // Update the amount spent
        if (this.spentLabel != null)
            this.spentLabel.setText(NumberFormat.getCurrencyInstance().format(actual));        

        // Update the progress bar
        if (this.progressBar != null)
            {
            // Set the color of the progress bar
            if (actual <= (settings.getWarningLevel() / 100.0f) * budget)
                {
                this.progressBar.setBorder(BorderFactory.createLineBorder(Constants.GREEN));
                this.progressBar.setForeground(Constants.GREEN); 
                }
            else if (actual <= (settings.getOverBudgetLevel() / 100.0f) * budget)
                {
                // I would prefer these to be Yellow but the text color on JProgressBars is white
                // and that cannot be easily changed. 
                this.progressBar.setBorder(BorderFactory.createLineBorder(Constants.ORANGE));
                this.progressBar.setForeground(Constants.ORANGE); 
                }
            else
                {
                this.progressBar.setBorder(BorderFactory.createLineBorder(Constants.RED));
                this.progressBar.setForeground(Constants.RED); 
                }

            // Set the progress
            if (actual >= budget)
                this.progressBar.setValue(100);
            else
                this.progressBar.setValue((int)((100.0d * actual / budget) ));
            this.progressBar.setStringPainted(true);

            // Update the text for the amount remaining
            this.progressBar.setString(NumberFormat.getCurrencyInstance().format(budget-actual)); 

            /*
             * Create the tooltip text
             */
            final StringBuilder tipText = new StringBuilder();
            final StringBuilder childTipText = new StringBuilder();
            final DecimalFormat percentFormat = new DecimalFormat("0.00");

            // Category name
            tipText.append("<html><center><b>"+item.getShortName()+"</b></center>");

            // % Spent
            if (budget == 0)
                tipText.append("<center><b>N/A</b></center>"); // Prevents NaN
            else
                tipText.append("<center><b>"+(percentFormat.format(100.0d * actual / budget))+"%</b></center>");

// TODO Add parent's contribution to the total if the sum of the direct children (not ancestors) is less than the total
// amount spent. Parent_contribution = actual - sum_of_direct_children. We'll have to split the top part of the tip text
// after the header was added from the child contributions then add the two together when setting the tip text. If the 
// Parent_contribution is non-zero we'll append that to tipText then combine. This is for the case where a user assigns
// a parent category to a transaction when children exist to better classify the expense.
// This worked for a normal parent category but not for a special category or likely even a normal category with parent
// categories below that. In those cases the child parent categories still show their totals not their contributions.

            // It this item has children then get that information
            if (item.hasChildren())
                {
                // Total child spending
                Double childSpent = 0d;

                // First child displayed flag
                boolean firstChild = true;

                // Get the children of this item
                final List<BudgetCategoryItem> children = this.dataModel.getBudgetCategoriesList().getChildren(this.UUID, settings.getAllAncestors());

                // Iterator
                final Iterator<BudgetCategoryItem> it = children.iterator();

                while (it.hasNext()) 
                    {
                    // Get a child
                    final BudgetCategoryItem child = it.next();

                    // Retrieve the values for this category
                    final Double childBudget = (Double)(child.getBudgetTotal() / 100.0d); 
                    final Double childActual = (Double)(child.getActualTotal() / 100.0d);

                    // Don't display if budget and actual are both 0
                    if ((childBudget == 0) && (childActual == 0))
                        continue;

                    // If this is the first child to be displayed, we need to display the header
                    if (firstChild)
                        {
                        // Add child header
                        tipText.append("<table><tr><th>Category</th><th>Spent</th><th>%</th><th>Remaining</th><th>Budget</th></tr>");

                        // Clear the flag so we only display the header once
                        firstChild = false;
                        }

                    // Add category name indented
                    int indent = (child.getIndentLevel() - item.getIndentLevel() - 1);
                    String indentStr = "";
                    for (int i = 0; i < indent; i++) {
                        indentStr += "&nbsp;&nbsp;&nbsp;";
                        }
                    childTipText.append("<tr><td>"+indentStr+child.getShortName()+"&nbsp;&nbsp;</td>");

                    // Add spent amount
                    childTipText.append("<td align='right'>"+NumberFormat.getCurrencyInstance().format(childActual)+"&nbsp;&nbsp;</td>");

                    // Keep track of direct children total spent
                    if (child.getIndentLevel() == item.getIndentLevel() + 1)
                        childSpent += childActual;

                    // Add spent %
                    if (childBudget == 0)
                        childTipText.append("<td align='center'>N/A</td>");   // Prevents NaN
                    else
                        childTipText.append("<td align='right'>"+(percentFormat.format(100.0d * childActual / childBudget))+"%&nbsp;&nbsp;</td>");
                   
                    // Add Remaining amount
                    childTipText.append("<td align='right'>"+NumberFormat.getCurrencyInstance().format(childBudget-childActual)+"&nbsp;&nbsp;</td>");

                    // Add budget amount
                    childTipText.append("<td align='right'>"+NumberFormat.getCurrencyInstance().format(childBudget)+"&nbsp;&nbsp;</td>");

                    // End of row
                    childTipText.append("</tr>");
                    }

                // See if there is a parent contribution to the total spent
                Double parentContribution = actual - childSpent;
                if (parentContribution > 0)
                    {
                    // Category name
                    tipText.append("<tr><td>"+item.getShortName()+"&nbsp;&nbsp;</td>");

                    // Parent contribution amount
                    tipText.append("<td align='right'>"+NumberFormat.getCurrencyInstance().format(parentContribution)+"&nbsp;&nbsp;</td>");

                    // % of budget
                    tipText.append("<td align='center'>N/A</td>");

                    // Add Remaining amount
                    tipText.append("<td align='right'>"+NumberFormat.getCurrencyInstance().format(0d)+"&nbsp;&nbsp;</td>");

                    // Add budget amount
                    tipText.append("<td align='right'>"+NumberFormat.getCurrencyInstance().format(0d)+"&nbsp;&nbsp;</td>");
                    }

                // End the table if child items were displayed
                if (!firstChild)
                    childTipText.append("</table>");
                }

            // Set the tooltip text
            this.progressBar.setToolTipText(tipText.toString()+childTipText.toString());

            // Hack to change the hover dismiss delay without affecting everyone else
            this.progressBar.addMouseListener(new MouseAdapter() {
                final int defaultInitialDelay = ToolTipManager.sharedInstance().getInitialDelay();
                final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
                final int dismissDelay = Integer.MAX_VALUE; // Effectively forever

                @Override
                public void mouseEntered(MouseEvent me) {
                ToolTipManager.sharedInstance().setInitialDelay(100);
                ToolTipManager.sharedInstance().setDismissDelay(this.dismissDelay);
                }
                
                @Override
                public void mouseExited(MouseEvent me) {
                ToolTipManager.sharedInstance().setDismissDelay(this.defaultDismissTimeout);
                ToolTipManager.sharedInstance().setInitialDelay(this.defaultInitialDelay);
                }
            });

            // Update the amount budgeted
            if (this.budgetLabel != null)
                this.budgetLabel.setText(NumberFormat.getCurrencyInstance().format(budget));
            }
    }
}

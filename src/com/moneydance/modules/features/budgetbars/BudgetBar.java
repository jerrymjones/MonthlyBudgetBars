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
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ToolTipManager;

import com.infinitekind.moneydance.model.CurrencyType;
import com.infinitekind.moneydance.model.CurrencyUtil;
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

    // Retrieved parameters
    private char separator;
    private Settings settings;

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

        // Get the decimal separator for this locale
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        this.separator = symbols.getDecimalSeparator();

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
        this.spentLabel.setPreferredSize(new Dimension(110, this.spentLabel.getHeight()));
        this.add(this.spentLabel, BorderLayout.LINE_START);

        // Create and configure the progress bar
        this.progressBar = new JProgressBar();
        this.progressBar.setStringPainted(false);
        this.progressBar.setBackground(mdGUI.getColors().sidebarBackground);
        this.add(this.progressBar, BorderLayout.CENTER);

        // Display the budget value at the right end of the budget bar
        this.budgetLabel = new JLabel("", JLabel.LEFT);
        this.budgetLabel.setPreferredSize(new Dimension(110, this.budgetLabel.getHeight()));
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
        this.settings = Settings.getInstance();

        // Get the budget category item
        final BudgetCategoryItem item = this.dataModel.getCategoryItem(this.UUID);

        // Retrieve the values for this category
        final long budget = item.getBudgetTotal(); 
        final long actual = item.getActualTotal();

        // Update the category label
        if (this.categoryLabel != null)
            {
            if (this.settings.getUseFullNames())
                this.categoryLabel.setText(item.getFullName());
            else
                this.categoryLabel.setText(item.getShortName());
            }
            
        // Update the amount spent
        if (this.spentLabel != null)
            this.spentLabel.setText(this.formatValue(item, actual));      

        // Update the progress bar
        if (this.progressBar != null)
            {
            // Set the color of the progress bar
            if (actual <= (this.settings.getWarningLevel() / 100.0f) * budget)
                {
                this.progressBar.setBorder(BorderFactory.createLineBorder(Constants.GREEN));
                this.progressBar.setForeground(Constants.GREEN); 
                }
            else if (actual <= (this.settings.getOverBudgetLevel() / 100.0f) * budget)
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
                this.progressBar.setValue((int)((100 * actual / budget) ));
            this.progressBar.setStringPainted(true);

            // Update the text for the amount remaining
            this.progressBar.setString(this.formatValue(item, budget-actual));            

            /*
             * Create the tooltip text
             */
            final StringBuilder tipText = new StringBuilder();
            
            // Category name
            tipText.append("<html><center><b>"+item.getShortName()+"</b></center>");

            // % Spent
            if (budget == 0)
                tipText.append("<center><b>N/A</b></center>"); // Prevents NaN
            else
                tipText.append("<center><b>"+(Constants.PERCENT_FORMAT.format(100 * actual / budget))+"%</b></center>");

            // Go process the root category
            ProcessCategory root = new ProcessCategory(this.UUID, this.dataModel, item.getIndentLevel(), true, this.settings.getAllAncestors()); 

            // Do we have child information to add
            if (root.getTipText().length() != 0)
                {
                // Yes, so add the header
                tipText.append("<table><tr><th>Category</th><th>Spent</th><th>%</th><th>Remaining</th><th>Budget</th></tr>");

                // Append the tip text from this child
                tipText.append(root.getTipText());

                // End the table
                tipText.append("</table>");
                }

            // Set the tooltip text
            this.progressBar.setToolTipText(tipText.toString());

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
                this.budgetLabel.setText(this.formatValue(item, budget));
            }
    }

    
    /** 
     * Method to convert currency format as needed and create a string formatted
     * for the target currency.
     * 
     * @param item - The budget category item we're formatting.
     * @param value - The numeric value to format.
     * @return String - The converted nd formatted value.
     */
    String formatValue(BudgetCategoryItem item, long value) {
        if (this.settings.getUseCategoryCurrency())
            return (item.getCurrencyType().formatFancy(value, this.separator));
        else
            {
            CurrencyType toType = this.dataModel.getBook().getCurrencies().getBaseType();
            return (toType.formatFancy(CurrencyUtil.convertValue(value, item.getCurrencyType(), toType), this.separator));
            }
    }

    /**
     * This class Gathers tool tip information for each parent category requested
     *
     * @author  Jerry Jones
     */
    public class ProcessCategory
    {
        // The tool tip text for this category
        final StringBuilder tipText = new StringBuilder();

        // Total child spending for this category
        long childSpent = 0;

        /**
         * Constructor to create a parent category processor
         * 
         * @param UUID - The UUID of the parent category to process
         * @param dataModel - The data model in use
         * @param rootIndent - The indent level of the root category
         * @param isRoot - True when processing the root category
         * @param showAllAncestors - True when showing all ancestor categories
         */
        public ProcessCategory(String UUID, DataModel dataModel, int rootIndent, boolean isRoot, boolean showAllAncestors) 
        {
            // Get the budget category item
            final BudgetCategoryItem item = dataModel.getCategoryItem(UUID);

            // It this item has children then get a list of them
            if (item.hasChildren())
                {
                // Get the direct children of this parent category
                final List<BudgetCategoryItem> children = dataModel.getBudgetCategoriesList().getChildren(UUID, false);

                // Iterator
                final Iterator<BudgetCategoryItem> it = children.iterator();

                while (it.hasNext()) 
                    {
                    // Get a child
                    final BudgetCategoryItem child = it.next();

                    // Retrieve the values for this category
                    final long childBudget = child.getBudgetTotal(); 
                    final long childActual = child.getActualTotal();

                    // Don't display if actual and budget are both 0
                    if ((childActual == 0) && (childBudget == 0))
                        continue;

                    // Is this child a parent?
                    if (child.hasChildren())
                        {
                        // Go process this child as a parent
                        ProcessCategory processChild = new ProcessCategory(child.getAccount().getUUID(), dataModel, rootIndent, false, showAllAncestors);
                        
                        // Append the tip text from this child
                        this.tipText.append(processChild.getTipText());

                        // Convert the currency as needed then add the child spent to our total
                        if (child.getCurrencyType() != item.getCurrencyType())
                            this.childSpent += CurrencyUtil.convertValue(childActual, child.getCurrencyType(), item.getCurrencyType());
                        else
                            this.childSpent += childActual;
                        }
                    else
                        {
                        // Convert the currency as needed then add the child spent to our total
                        if (child.getCurrencyType() != item.getCurrencyType())
                            this.childSpent += CurrencyUtil.convertValue(childActual, child.getCurrencyType(), item.getCurrencyType());
                        else
                            this.childSpent += childActual;

                        // Only show children if we are processing the root category (ie. direct child of root)
                        // or if we are showing all ancestors 
                        if ((isRoot) || (showAllAncestors))
                            {
                            // Add category name indented
                            int indent = (child.getIndentLevel() - rootIndent - 1);
                            String indentStr = "";
                            for (int i = 0; i < indent; i++) {
                                indentStr += "&nbsp;&nbsp;&nbsp;";
                                }
                            this.tipText.append("<tr><td>"+indentStr+child.getShortName()+"&nbsp;&nbsp;</td>");

                            // Add spent amount
                            this.tipText.append("<td align='right'>"+BudgetBar.this.formatValue(child, childActual)+"&nbsp;&nbsp;</td>");

                            // Add spent %
                            if (childBudget == 0)
                                this.tipText.append("<td align='center'>N/A</td>");   // Prevents NaN
                            else
                                this.tipText.append("<td align='right'>"+(Constants.PERCENT_FORMAT.format(100.0d * childActual / childBudget))+"%&nbsp;&nbsp;</td>");
                        
                            // Add Remaining amount
                            this.tipText.append("<td align='right'>"+BudgetBar.this.formatValue(child, childBudget - childActual)+"&nbsp;&nbsp;</td>");

                            // Add budget amount
                            this.tipText.append("<td align='right'>"+BudgetBar.this.formatValue(child, childBudget)+"&nbsp;&nbsp;</td>");
                            BudgetBar.this.formatValue(child, childBudget);
                            // End of row
                            this.tipText.append("</tr>");
                            }
                        }
                    } // For each child

                // Calculate the parent contribution to the total spent
                long parentContribution = item.getActualTotal() - this.childSpent;

                // Show parent contribution as required
                if (((isRoot) && (parentContribution > 0)) || ((!isRoot) && (showAllAncestors)))
                    this.prependCategory(item, 0, parentContribution, rootIndent);
                    
                // If this category is a direct child of the root category then prepend as normal entry
                else if (item.getIndentLevel() == rootIndent + 1)
                    this.prependCategory(item, item.getBudgetTotal(), item.getActualTotal(), rootIndent);
                }
            }

        private void prependCategory(BudgetCategoryItem item, long budget, long actual, int rootIndent)
        {
            // The tool tip text to prepend for this category
            final StringBuilder preTipText = new StringBuilder();

            // Add category name indented
            int indent = (item.getIndentLevel() - rootIndent - 1);
            String indentStr = "";
            for (int i = 0; i < indent; i++) {
                indentStr += "&nbsp;&nbsp;&nbsp;";
                }
            preTipText.append("<tr><td>"+indentStr+item.getShortName()+"&nbsp;&nbsp;</td>");

            // Only display the following if there is data to display
            if ((actual > 0) || (budget > 0))
                {
                // Add spent amount
                preTipText.append("<td align='right'>"+BudgetBar.this.formatValue(item, actual)+"&nbsp;&nbsp;</td>");

                // Add spent %
                if (budget == 0)
                    preTipText.append("<td align='center'>N/A</td>");   // Prevents NaN
                else
                    preTipText.append("<td align='right'>"+(Constants.PERCENT_FORMAT.format(100.0d * actual / budget))+"%&nbsp;&nbsp;</td>");
            
                // Add Remaining amount
                preTipText.append("<td align='right'>"+BudgetBar.this.formatValue(item, budget-actual)+"&nbsp;&nbsp;</td>");

                // Add budget amount
                preTipText.append("<td align='right'>"+BudgetBar.this.formatValue(item, budget)+"&nbsp;&nbsp;</td>");
                }

            // End of row
            preTipText.append("</tr>");

            // Prepend the tooltip text to tipText
            this.tipText.insert(0, preTipText);
        }

        /**
         * @return the tipText
         */
        public StringBuilder getTipText() {
            return this.tipText;
        }

        /**
         * @return the childSpent
         */
        public long getChildSpent() {
            return this.childSpent;
        }
    }
}

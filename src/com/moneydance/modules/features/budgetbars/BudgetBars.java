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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.infinitekind.moneydance.model.AbstractTxn;
import com.infinitekind.moneydance.model.Account;
import com.infinitekind.moneydance.model.AccountBook;
import com.infinitekind.moneydance.model.AccountListener;
import com.infinitekind.moneydance.model.Budget;
import com.infinitekind.moneydance.model.BudgetList;
import com.infinitekind.moneydance.model.BudgetListener;
import com.infinitekind.moneydance.model.TransactionListener;
import com.moneydance.apps.md.view.HomePageView;
import com.moneydance.apps.md.view.gui.MoneydanceGUI;
import com.moneydance.apps.md.view.gui.MoneydanceLAF;
import com.moneydance.awt.CollapsibleRefresher;

/**
 * This class implements the Monthly Budget Bars widget.
 *
 * @author  Jerry Jones
 */
class BudgetBars implements HomePageView, AccountListener, TransactionListener, BudgetListener {
    // Storage for the passed in parameters
    private MoneydanceGUI mdGUI = null;
    private AccountBook book = null;

    // The current data model which may change as configuration parameters change
    private DataModel dataModel;

    // List of monthly style budgets
    private MyBudgetList budgetList;

    // The current budget being used
    private Budget budget = null;

    // The panel for the Budget Bars widget
    private JPanel monthlyBarsPanel;

    // The list of budget bars being displayed
    private ArrayList<BudgetBar> barList = null;

    // Ser true when configuration changes are made, false otherwise
    private boolean configurationChanged = false;

    // The settings for this widget
    private Settings settings = null;

    // The selected categories list
    private List<CategoryListItem> selectedCats = null;

    // Do not allow re-entry to getGUIView
    boolean noReentry = false;

    // Delayed refresher to prevent multiple refresh cycles in a short period of time when changes are made
    private final CollapsibleRefresher refresher = new CollapsibleRefresher(BudgetBars.this::doRefresh);

    /**
     * Constructor method used to create the Monthly Budget Bar widget.
     * 
     * @param mdGUI - The Moneydance GUI
     */
    BudgetBars(final MoneydanceGUI mdGUI) {
        this.mdGUI = mdGUI;
    }

    /**
     * Returns a GUI component that provides a view of the info panel for the given data file
     * 
     * @param book - The account book to use
     */
    @Override
    public synchronized JComponent getGUIView(final AccountBook book) 
    {
        // Don't allow re-entry. Otherwise we get prompted twice if the selected 
        // budget doesn't exist.Not sure why Moneydance calls this twice. Also, 
        // not sure why synchronized doesn't fix this.
        if (this.noReentry)
            // The panel could be null but Moneydance seems to be ok with that
            return this.monthlyBarsPanel;

        // Prevent Moneydance from calling us twice.
        this.noReentry = true; 

        // We have to reload if the configuration was edited
        if (this.configurationChanged)
            {
            // Reload the data model and setup the widget
            this.dataModel = null;
            }

        // We have to reload if the book changes or if the configuration was edited
        if (!book.equals(this.book))
            {   
            // Reload the data model and setup the widget
            this.dataModel = null;
            this.monthlyBarsPanel = null;

            // Save the account book
            this.book = book;
            }

        if (this.dataModel == null)
            {
            // Get the settings for the widget
            this.settings = Settings.getInstance(book);

            // Get the current list of monthly budgets
            this.budgetList = new MyBudgetList(book);

            // Get the selected budget
            this.budget = this.budgetList.getBudget(this.settings.getBudgetName());
            
            // If the selected budget was not found we have to get a new one
            if (this.budget == null)
                {
                // Get the number of monthly budgets found
                int budgetCount = this.budgetList.getBudgetCount();

                // Get the returned budget names
                String[] strNames =  this.budgetList.getBudgetNames();

                // The selected budget to use
                String budgetName;

                if (budgetCount == 0)
                    {
                    // Display an error message - No budgets exist!
                    JOptionPane.showMessageDialog( null,
                    "No monthly style budgets have been created.  Use 'Tools:Budget Manager' to create a monthly budget before using this extension.",
                    "Error (Monthly Budget Bars)",
                    JOptionPane.ERROR_MESSAGE);

                    // We can't continue, so exit
                    return null;
                    }
                else if (budgetCount == 1) // If there is only one monthly budget available, select that one
                    {
                    // Get the single budget name returned
                    budgetName = strNames[0];

                    // Display an error message - No budgets exist!
                    JOptionPane.showMessageDialog( null,
                    "The budget '"+this.settings.getBudgetName()+"' does not exist. Using the budget named '"+budgetName+"' instead.",
                    "Information (Monthly Budget Bars)",
                    JOptionPane.INFORMATION_MESSAGE);
                    }
                else // Multiple budgets found so we have to ask the user which one to use
                    {
                    // Show a dialog to enable selection of the budget to copy from.
                    budgetName = (String) JOptionPane.showInputDialog(null, 
                    "Select the monthly budget to use for Monthly Budget Bars.",
                    "Select Budget (Monthly Budget Bars)", 
                    JOptionPane.QUESTION_MESSAGE, null,
                    strNames,
                    strNames[0]); 	// Initial choice
                    }

                // Set the selected budget name
                this.settings.setBudgetName(budgetName);
                
                // Save the new setting
                this.settings.saveSettings();
                
                // Get the selected budget
                this.budget = this.budgetList.getBudget(budgetName);
                if (this.budget == null)
                    return null;    // Still no budget, let's bail.
                }

            // Create the data model for the budget bars
            this.dataModel = new DataModel(book, this);
            }

        // Build the widget to display
        if ((this.monthlyBarsPanel == null) || (this.configurationChanged))
            {
            // Get the selected categories to make budget bars for
            this.selectedCats = new ArrayList<CategoryListItem>();
            String selCatList = this.book.getRootAccount().getPreference(Constants.CATEGORIES_SELECTED, null);
            if (selCatList != null)
                {
                for (final String UUID: Arrays.asList(selCatList.split("\\s*,\\s*")))
                    {
                    // Get the item from the UUID
                    BudgetCategoryItem item = this.dataModel.getCategoryItem(UUID);

                    // Only add it if it's still valid
                    if (item != null)
                        this.selectedCats.add(new CategoryListItem(UUID, item.getFullName()));
                    }
                }

            // If the configuration changed clear the old configuration and re-add the components
            if (this.configurationChanged)
                {
                // Iterate the list of budget bars and remove the components
                if (this.barList != null)
                    {
                    for (final BudgetBar bar:this.barList)  
                        bar.removeAll();
                    }
                
                // Empty the bar list
                this.barList.clear();

                // Remove everything from the main panel
                this.monthlyBarsPanel.removeAll();
                }
            else
                {
                // Create a new panel for the widget
                this.monthlyBarsPanel = new JPanel();
                this.monthlyBarsPanel.setLayout(new BoxLayout(this.monthlyBarsPanel, BoxLayout.PAGE_AXIS));
                this.monthlyBarsPanel.setForeground(this.mdGUI.getColors().homePageFG);
                this.monthlyBarsPanel.setBorder(BorderFactory.createCompoundBorder(MoneydanceLAF.homePageBorder, BorderFactory.createEmptyBorder(0, 10, 0, 10)));
                }
                
            // Add the top bar
            this.monthlyBarsPanel.add(new TopBar(this));

            // Now add bars for all the selected budget categories
            this.barList = new ArrayList<BudgetBar>(); 
            for (final CategoryListItem category: this.selectedCats) 
                {   
                final BudgetCategoryItem item = this.dataModel.getCategoryItem(category.getUUID());
                    {
                    if (item != null)
                        {
                        // Add a budget category from the list
                        final BudgetBar bar = new BudgetBar(this.mdGUI, this.dataModel, category.getUUID());
                        this.monthlyBarsPanel.add(bar);
                        this.barList.add(bar);
                        }
                    else
                        System.err.println("ERROR: Category does not exist. No budget bar created.");
                    }
                }

            // Add the footer bar
            this.monthlyBarsPanel.add(new FooterBar(this.mdGUI));
            }
        
        // Clear the configuration changed flag
        this.configurationChanged = false;

        // Clear the re-entry flag
        this.noReentry = false; 

        // Return the GUI component for Monthly Budget Bars
        return this.monthlyBarsPanel;
    }

    /** 
     * Sets the view as active or inactive. When not active, a view should not have any registered listeners
     * with other parts of the program. This will be called when an view is added to the home page
     * or when the home page is refreshed after not being visible for a while.
     */
    @Override
    public void setActive(final boolean active) 
    {
        if (this.book != null)
            {
            if (active)
                {
                // Setup the listeners. Note that the API documentation says these
                // should be setup in setActive and removed when it is called with
                // the active parameter set to false. The problem with this is that 
                // when you go to enter new transactions, etc. setActive will be 
                // called with false and any changes will be missed forcing a full
                // reload the next time we go active.       
                this.book.addAccountListener(this);  
                this.book.getTransactionSet().addTransactionListener(this);
                this.book.getBudgets().addListener(this);

                // Add a listener on our budget
                if (this.budget !=null)
                    this.budget.addBudgetListener(this);
                }
            else
                {
                // Remove the listeners
                this.book.removeAccountListener(this);
                this.book.getTransactionSet().removeTransactionListener(this);
                this.book.getBudgets().removeListener(this);

                // Remove the listener on our budget
                if (this.budget !=null)
                    this.budget.removeBudgetListener(this);
                }
            }
    }

    /**
     * Forces a refresh of the information in the Budget Bar widget. For example, this is called after
     * the budgets or actuals are updated. Like the other home page controls, we do this lazily to 
     * avoid repeated refreshes after updates.
     */
    @Override
    public void refresh() 
    {
        this.refresher.enqueueRefresh();
    }

    /**
     * Actually do the refresh
     */
    public void doRefresh() 
    {
        if (this.dataModel != null)
            {
            // Load the data if so
            this.dataModel.loadData();

            // Iterate the list of budget bars and refresh all of them
            for (final BudgetBar bar:this.barList)  
                bar.refresh();
            }
    }

    /**
     * Called when the view should clean up everything. For example, this is called when a file is closed and the GUI
     * is reset. The view should disconnect from any resources that are associated with the currently opened data file.
     */
    @Override
    public void reset() 
    {

        // Set the view as inactive
        this.setActive(false);

        // Iterate the list of budget bars and remove the components
        if (this.barList != null)
            {
            for (final BudgetBar bar:this.barList)  
                bar.removeAll();
            }

        // Remove all the components from the main display panel too
        if (this.monthlyBarsPanel != null) 
            this.monthlyBarsPanel.removeAll();

        // Reset the panel
        this.monthlyBarsPanel = null;
    }

    /**
     * Method called when the user changes the display period for the budget bars
     * 
     * @param newID - The new period ID
     */
    public void periodChanged(final int newID)
    {
        // Save the new period
        this.settings.setPeriod(newID);
        this.settings.saveSettings();

        // Refresh the data
        this.doRefresh();
    }
    
    /**
     * @return the budget
     */
    public Budget getBudget() {
        return this.budget;
    }

    /**
     * Returns a unique identifier for this view.
     *  
     * @return String - The unique identifier
     */
    @Override
    public String getID() {
        return "MonthlyBudgetBars";
    }
    
    /** 
     * Returns a short descriptive name of this view.
     * 
     * @return String - The short name
     */
    @Override
    public String toString() {
        return "Monthly Budget Bars";
    }
    
    /**
     * @return the dataModel
     */
    public DataModel getDataModel() {
        return this.dataModel;
    }

    /**
     * @return the budgetList
     */
    public MyBudgetList getBudgetList() {
        return this.budgetList;
    }

    /**
     * @return the selected categories list
     */
    public List<CategoryListItem> getSelectedCats() {
        return this.selectedCats;
    }

    /**
     * @return the book
     */
    public AccountBook getBook() {
        return this.book;
    }

    /**
     * @return the root account
     */
    public Account getRootAccount() {
        return this.book.getRootAccount();
    }
    
    /**
     * Method to set the configuration changed flag and then reload the widget
     * after configuration changes.
     */
    public void configurationChanged() {
        // Set the configuration changed flag
        this.configurationChanged = true;

        // Re-create the widget with the new data
        this.getGUIView(this.book);

        // Refresh the view
        this.refresh();
    }

    /*
    * Listener callbacks
    */
    @Override
    public void accountAdded(final Account parentAccount, final Account newAccount) {
        this.refresh();
    }

    @Override
    public void accountBalanceChanged(final Account account) {
        this.refresh();
    }

    @Override
    public void accountDeleted(final Account parentAccount, final Account oldAccount) {
        this.refresh();
    }

    @Override
    public void accountModified(final Account modifiedAccount) {
        this.refresh();
    }

    @Override
    public void transactionAdded(final AbstractTxn newTxn) {
        this.refresh();
    }

    @Override
    public void transactionModified(final AbstractTxn modTxn) {
        this.refresh();
    }

    @Override
    public void transactionRemoved(final AbstractTxn remTxn) {
        this.refresh();
    }

    @Override
    public void budgetAdded(final Budget newBudget) {
    }

    @Override
    public void budgetListModified(final BudgetList newBudgetList) {
    }

    @Override
    public void budgetModified(final Budget modifiedBudget) {
        this.refresh();
    }

    @Override
    public void budgetRemoved(final Budget removedBudget) {
        // Is the budget that was removed the one we're using?
        if (this.budget.equals(removedBudget))
            {
            // Set the configuration changed flag
            this.configurationChanged = true;

            // Re-create the widget with the new data
            this.getGUIView(this.book);

            // Refresh the view
            this.refresh();
            }
    }
}




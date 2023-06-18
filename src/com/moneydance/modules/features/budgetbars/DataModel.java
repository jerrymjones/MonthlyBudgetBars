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

import java.util.Calendar;
import java.util.Iterator;

import com.infinitekind.moneydance.model.Account;
import com.infinitekind.moneydance.model.Account.AccountType;
import com.infinitekind.moneydance.model.AccountBook;
import com.infinitekind.moneydance.model.AccountUtil;
import com.infinitekind.moneydance.model.BudgetItem;
import com.infinitekind.moneydance.model.BudgetItemList;
import com.infinitekind.moneydance.model.BudgetPeriod;
import com.infinitekind.moneydance.model.PeriodType;
import com.infinitekind.util.DateUtil;

/**
* This class implements the data model for the Monthly Budget Bars.
*
* @author  Jerry Jones
*/
public class DataModel {
    // The current data file
    private final AccountBook book;

    private final BudgetBars parent;

    // Budget item list
    private BudgetItemList budgetItemList;

    // Budget Categories List
    private BudgetCategoriesList budgetCategoriesList = null;
    
    /**
     * Constructor for the data model.
     * 
     * @param book - The account book to use for the data model
     * @param parent - The budgetBars parent object
     */
    public DataModel(final AccountBook book, final BudgetBars parent) 
    {
        // Save the account book for later
        this.book = book;

        // Save the parent for later
        this.parent = parent;

        // Load the data from the specified budget and account book
        this.loadData();
    }

    /**
     * Method to load the data for the budget bars.
     * 
     * @param budget - The budget object to use
     */
    public void loadData() {
        // Get current time information
        final Calendar now = Calendar.getInstance();
        int thisYear = now.get(Calendar.YEAR);
        final int thisMonth = now.get(Calendar.MONTH) + 1;  // Calendar months are 0...11 and we want 1...12

        // Now set up for the selected period
        int startMonth; // The starting month for the period
        int months;     // The number of months to display
        switch(Settings.getInstance().getPeriod())
            {
            case Constants.PERIOD_THIS_MONTH:
                // Use This month
                startMonth = thisMonth;
                months = 1;
                break;

            case Constants.PERIOD_LAST_MONTH:
                // Use January through the end of the current month
                startMonth = thisMonth - 1;
                if (startMonth == 0)
                    {
                    startMonth = 12;
                    thisYear -= 1;
                    }
                months = 1;
                break;

            case Constants.PERIOD_THIS_YEAR:
                // Use January through the end of the year
                startMonth = 1;
                months = 12;
                break;

            case Constants.PERIOD_AUTOMATIC:
            default:
                // Use January through the end of the current month
                startMonth = 1;
                months = thisMonth -  startMonth + 1;
                break;
            }

        // Get the budget item list for the passed in budget
        if (this.parent.getBudget() == null)
            return;
        else
            this.budgetItemList = this.parent.getBudget().getItemList();

        // Create a new Budget Categories list
        this.budgetCategoriesList = new BudgetCategoriesList(this.book);

        // Create a special category for the Income - Expenses total row
        this.budgetCategoriesList.add(Constants.UUID_OVERALL, "Income-Expenses", Account.AccountType.ROOT, 0);

        // Add a special category to the data model for "Income"
        this.budgetCategoriesList.add(Constants.UUID_INCOME, "Income", Account.AccountType.INCOME, 1);

        // Iterate through the accounts to find all active Income categories
        // Note that accounts and categories are the same, they are all Accounts. 
        for (final Iterator<Account> iter = AccountUtil.getAccountIterator(this.book); iter.hasNext(); ) 
            {
            // Get the account 
            final Account acct = iter.next();

            // Go add category if it's the right type and if it's an income category
            this.addIf(acct, Account.AccountType.INCOME, thisYear, startMonth, months);
            }

        // Add a special category to the data model for "Expenses"
        this.budgetCategoriesList.add(Constants.UUID_EXPENSE, "Expenses", Account.AccountType.EXPENSE, 1);

        // Iterate through the accounts to find all active Expense categories
        for (final Iterator<Account> iter = AccountUtil.getAccountIterator(this.book); iter.hasNext(); ) 
            {
            // Get the account 
            final Account acct = iter.next();

            // Go add category if it's the right type and if it's an expense category
            this.addIf(acct, Account.AccountType.EXPENSE, thisYear, startMonth, months);
            }
    }


    /** 
     * Get a budget category item given the UUID
     * 
     * @param UUID - The UUID of the account.
     * @return BudgetCategoryItem - The BudgetCategoryItem object corresponding to
     * the UUID. A null return value indicates the item does not exist.
     */
    public BudgetCategoryItem getCategoryItem(final String UUID)
    {
        if (this.budgetCategoriesList == null)
            return null;
        else
            return this.budgetCategoriesList.getCategoryItem(UUID);
    }


    /** 
     * Get the short name of this category.
     * 
     * @param item - The budget category item to retrieve the short name from
     * @return String - The short name of this category.
     */
    public String getShortName(final BudgetCategoryItem item) 
    {
        return item.getShortName();
    }

    /** 
     * Get the total budget for this category.
     * 
     * @param item - The budget category item to retrieve the short name from
     * @return Long - The budget total (* 100L)
     */
    public Long getBudgetTotal(final BudgetCategoryItem item)
    {
        return item.getBudgetTotal();
    }

    /**
     * @return the book
     */
    public AccountBook getBook() {
        return this.book;
    }

    /**
     * @return the budgetCategoriesList
     */
    public BudgetCategoriesList getBudgetCategoriesList() {
        return this.budgetCategoriesList;
    }

    
    /**
     * This method adds an account (category) to the budget category list if
     * it meets the right criteria - it must be active and not hidden as well
     * as being the proper type.
     * 
     * @param acct - The account to add 
     * @param type - The account type we're looking for
     * @param thisYear - The current year
     * @param startMonth - The starting month to retrieve
     * @param months - The number of months to retrieve
     */
    private void addIf(final Account acct, final AccountType type, final int thisYear, final int startMonth, final int months) 
    {
    // Get the type of this account
    final AccountType acctType = acct.getAccountType();

    // Is the account type that we're looking for?    
    if (acctType == type)
        {
        // Is the account active
        if ((!acct.getAccountOrParentIsInactive()) && (!acct.getHideOnHomePage()))
            {
            // Add this category
            final BudgetCategoryItem item = this.budgetCategoriesList.add(acct);
            if (item == null)
                return;

            // If this is not a roll-up category then we need to get the current budget values for this category
            if (!item.hasChildren())
                {
                for (int month = startMonth; month < (startMonth + months); month++)
                    {
                    // Find existing budget values for each month
                    final BudgetItem i = this.budgetItemList.getBudgetItemForCategory(acct, new BudgetPeriod(DateUtil.getDate(thisYear, month, 1), PeriodType.MONTH));
                    if (i != null)
                        {
                        // Set the budget value for the current month
                        item.setBudgetValueForMonth(this, this.budgetCategoriesList, month, i.getAmount(), acctType);
                        }
                    }
                } 

            // Only add transaction totals if the category is budgeted or if  we are not ignoring unbudgeted categories
            if ((item.getBudgetTotal() != 0) || (Settings.getInstance().getIgnoreUnbudgeted() == false))
                {
                // Retrieve the actual totals for this account
                new TransactionTotals(item, this.book, acct, thisYear, startMonth, months);

                // Update the parent actual totals
                item.updateParentActualTotals(this.budgetCategoriesList, item);
                }
            }
        }
    }
}


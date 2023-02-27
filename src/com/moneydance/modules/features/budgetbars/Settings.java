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

import com.infinitekind.moneydance.model.AccountBook;

/**
 * This class implements the settings for the Monthly Budget Bars widget.
 *
 * @author  Jerry Jones
 */
public class Settings {
    /**
     * The instance of Settings that this Class is storing
     */
    private static Settings instance = null;

    // The account book in use
    private static AccountBook book;

    /*
     * The settings we support
     */
    // Version number of the settings
    private static int version = Constants.SETTINGS_VERSION_1;

    // The name of the budget to use
    private static String budgetName = "Budget";

    // Use full names when true
    private static Boolean useFullNames = false;

    // Warning level for actuals (% of budget)
    private static float warningLevel = 100.0f;

    // Over budget level for actuals (% of budget)
    private static float overBudgetLevel = 105.0f;

    // The display period for the budget bars 
    private static int period = Constants.PERIOD_AUTOMATIC;

    // The display period for the budget bars 
    private static Boolean allAncestors = false;

    /**
     * Default constructor for the settings class.
     * 
     * @param  book - The account book in use
     */
    private Settings(final AccountBook book) 
    {
        Settings.loadSettings(book);
    }

    private static void loadSettings(AccountBook book)
    {
        // Get the default settings. These are stored in a comma separated string.
        final String rawSettings = book.getRootAccount().getPreference(Constants.MBB_SETTINGS, "");
        final String[] rawSplit = rawSettings.split("\\s*,\\s*");

        // Did we get valid data?
        if (rawSplit.length == Constants.V1_NUM_MBR_SETTINGS)
            {
            try
                {
                Settings.version            = Integer.parseInt(rawSplit[0]);
                Settings.budgetName         = rawSplit[1];
                Settings.useFullNames       = rawSplit[2].equalsIgnoreCase("true");
                Settings.warningLevel       = Float.parseFloat(rawSplit[3]);
                Settings.overBudgetLevel    = Float.parseFloat(rawSplit[4]);
                Settings.period             = Integer.parseInt(rawSplit[5]);
                Settings.allAncestors       = rawSplit[6].equalsIgnoreCase("true");
                return;
                }
            catch (final Exception e)
                {
                e.printStackTrace();
                System.err.println("ERROR: Cannot parse the configuration settings '"+rawSettings+"'.");
                }
            }

        // Otherwise, we'll use the defaults just to get going
        Settings.version            = Constants.SETTINGS_VERSION_1;
        Settings.budgetName         = "Budget";
        Settings.useFullNames       = false;
        Settings.warningLevel       = 100.0f;
        Settings.overBudgetLevel    = 105.0f;
        Settings.period             = Constants.PERIOD_AUTOMATIC;
        Settings.allAncestors       = false;
    }


    /**
     * Get the Instance of this class There should only ever be one instance of
     * this class and other classes can use this static method to retrieve the
     * instance
     *
     * @return Configuration the stored Instance of this class
     */
    public static synchronized Settings getInstance(final AccountBook book) {
        // If there is no instance yet then we'll create one here
        if (Settings.instance == null)
            { 
            // Create a new instance
            Settings.instance = new Settings(book);
            }
        // Otherwise, if the book changed we need to reload and save the new book
        else if (!book.equals(Settings.book))
            {
            // Reload the settings for the new book
            Settings.loadSettings(book);
            }
        
        // Save the account book
        Settings.book = book;

        return Settings.instance;
    }

    /**
     * Get the Instance of this class There should only ever be one instance of
     * this class and other classes can use this static method to retrieve the
     * instance
     *
     * @return Configuration the stored Instance of this class or null if it hasn't
     * been initialized with an account book yet. 
     */
    public static synchronized Settings getInstance() {
        return Settings.instance;
    }

    /**
     * Save the settings 
     */
    public void saveSettings() {
        final String settings = Settings.version+","+Settings.budgetName+","+Settings.useFullNames.toString()+","+Settings.warningLevel
            +","+Settings.overBudgetLevel+","+Settings.period+","+Settings.allAncestors.toString();
        Settings.book.getRootAccount().setPreference(Constants.MBB_SETTINGS, settings);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Settings [version=" + Settings.version + ", budgetName=" + Settings.budgetName + ", useFullNames=" + Settings.useFullNames + ", warningLevel="
                + Settings.warningLevel + ", overBudgetLevel=" + Settings.overBudgetLevel + ", period=" + Settings.period  + ", allAncestors=" 
                + Settings.allAncestors+ "]";
    }

    /**
     * @return the budgetName
     */
    public String getBudgetName() {
        return Settings.budgetName;
    }

    /**
     * @param budgetName the budgetName to set
     */
    public void setBudgetName(final String budgetName) {
        Settings.budgetName = budgetName;
    }

    /**
     * @return the useFullNames
     */
    public Boolean getUseFullNames() {
        return Settings.useFullNames;
    }

    /**
     * @param useFullNames the useFullNames to set
     */
    public void setUseFullNames(final Boolean useFullNames) {
        Settings.useFullNames = useFullNames;
    }

    /**
     * @return the warningLevel
     */
    public float getWarningLevel() {
        return Settings.warningLevel;
    }

    /**
     * @param warningLevel the warningLevel to set
     */
    public void setWarningLevel(final float warningLevel) {
        Settings.warningLevel = warningLevel;
    }

    /**
     * @return the overBudgetLevel
     */
    public float getOverBudgetLevel() {
        return Settings.overBudgetLevel;
    }

    /**
     * @param overBudgetLevel the overBudgetLevel to set
     */
    public void setOverBudgetLevel(final float overBudgetLevel) {
        Settings.overBudgetLevel = overBudgetLevel;
    }

    /**
     * @return the period
     */
    public int getPeriod() {
        return Settings.period;
    }

    /**
     * @param period the period to set
     */
    public void setPeriod(final int period) {
        Settings.period = period;
    }

    /**
     * @return the allAncestors flag
     */
    public Boolean getAllAncestors() {
        return Settings.allAncestors;
    }

    /**
     * @param allAncestors the allAncestors to set
     */
    public void setAllAncestors(Boolean allAncestors) {
        Settings.allAncestors = allAncestors;
    }

    /**
     * @return the settings version
     */
    public int getVersion() {
        return Settings.version;
    }
}

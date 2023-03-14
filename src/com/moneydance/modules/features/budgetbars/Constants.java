/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2022-2023, Jerry Jones
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

import java.awt.Color;
import java.text.DecimalFormat;

public class Constants 
{
    /*
    * Special RGB Color codes
    */
    public static final Color GREEN                 = new Color(40, 90, 40);
    public static final Color ORANGE                = new Color(205, 89, 0);
    public static final Color RED                   = new Color(130, 0, 0);  
    public static final Color MEDIUM_BLUE           = new Color(33, 144, 255);

    /*
     * Budget Bar Period constants
    */
    public static final String[] periods            = { "Automatic", "This Month", "Last Month", "This Year" };
    public static final int PERIOD_AUTOMATIC        = 0;
    public static final int PERIOD_THIS_MONTH       = 1;
    public static final int PERIOD_LAST_MONTH       = 2;
    public static final int PERIOD_THIS_YEAR        = 3;

    /*
     * UUIDs for special categories
     */
    public static final String UUID_OVERALL         = "00000000-0000-0000-0000-000000000001";
    public static final String UUID_INCOME          = "00000000-0000-0000-0000-000000000002";
    public static final String UUID_EXPENSE         = "00000000-0000-0000-0000-000000000003";

    /*
     * Configuration parameter constants
     */
    public static String CATEGORIES_SELECTED        = "MonthlyBudgetBars_cats";     // Categories selected to display
    public static String MBB_SETTINGS               = "MonthlyBudgetBars_settings"; // Settings 
    public static final int SETTINGS_VERSION_1      = 1;                            // Version 1 of the settings
    public static final int V1_NUM_MBR_SETTINGS     = 7;                            // Number of MBB_Settings in version 1 settings: (Version, Budget name, Use_full_names, warning level, over level, allAncestors)
    public static final int SETTINGS_VERSION_2      = 2;                            // Version 2 of the settings
    public static final int V2_NUM_MBR_SETTINGS     = 8;                            // Number of MBB_Settings in version 1 settings: (Version, Budget name, Use_full_names, warning level, over level, allAncestors,useCategoryCurrency)

    /*
     * Decimal format when printing percentages
     */
    public static DecimalFormat PERCENT_FORMAT      = new DecimalFormat("0.00");
}

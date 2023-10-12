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

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.moneydance.apps.md.view.gui.MDColors;
import com.moneydance.apps.md.view.gui.MoneydanceGUI;

/**
 * Class to create the footer bar of the Monthly Budget Bars
 *
 * @author  Jerry Jones
 */
public class FooterBar extends JPanel 
{
    /**
     * Constructor method used to create the footer bar for the Monthly Budget Bars.
     * 
     * @param mdGUI - The Moneydance GUI
     */
    public FooterBar(final MoneydanceGUI mdGUI) 
    {
        // Call the default JPanel constructor
        super();

        MDColors colors = com.moneydance.apps.md.view.gui.MDColors.getSingleton();

        // Set the layout manager for this bar
        this.setLayout(new BorderLayout(10,10));

        // Allow the underlying panel to show through
        this.setOpaque(false);

        // Left legend
        final JLabel leftLabel = new JLabel("Spent", JLabel.RIGHT);
        leftLabel.setPreferredSize(new Dimension(110, leftLabel.getHeight()));
        leftLabel.setForeground(colors.isDarkTheme() ? Constants.MEDIUM_BLUE : colors.reportBlueFG);   
        this.add(leftLabel, BorderLayout.LINE_START);

        // Center legend
        final JLabel centerLabel = new JLabel("Remaining", JLabel.CENTER);
        centerLabel.setForeground(colors.isDarkTheme() ? Constants.MEDIUM_BLUE : colors.reportBlueFG);
        this.add(centerLabel, BorderLayout.CENTER);

        // Right legend
        final JLabel rightLabel = new JLabel("Budget", JLabel.LEFT);
        rightLabel.setPreferredSize(new Dimension(110, rightLabel.getHeight()));
        rightLabel.setForeground(colors.isDarkTheme() ? Constants.MEDIUM_BLUE : colors.reportBlueFG);
        this.add(rightLabel, BorderLayout.LINE_END);
    }
}


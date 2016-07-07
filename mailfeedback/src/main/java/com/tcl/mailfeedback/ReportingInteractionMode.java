/*
 * Copyright 2010 Emmanuel Astier & Kevin Gaudin
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 */
package com.tcl.mailfeedback;

/**
 * Defines the different user interaction modes for ACRA.
 * <ul>
 * <li>SILENT: No interaction, reports are sent silently and a "Force close" dialog terminates the
 * app.</li>
 * <li>TOAST: A simple Toast is triggered when the application crashes, the Force close dialog is
 * not displayed.</li>
 * <li>NOTIFICATION: A status bar notification is triggered when the application crashes, the Force
 * close dialog is not displayed. When the user selects the notification, a dialog is displayed
 * asking him if he is ok to send a report</li>
 * </ul>
 */
enum ReportingInteractionMode {
    SILENT, NOTIFICATION, TOAST
}

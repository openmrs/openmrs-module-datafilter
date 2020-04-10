/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.datafilter.extension.html;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ContentTest {

    @Test
    public void addLocationNameLine() {
        Content content = new Content(Arrays.asList("L1","L2"),new ArrayList<String>());

        String expected = "<style>.listItemBoxCustom {width: 460px;padding: 2px;border: 1px solid lightgray;float: left;background-color: #EFEFEF;overflow-x: scroll;height: 200px;}</style>" +
                "<td valign='top'>Location</td>" +
                "<p>Selected Locations:</p>" +
                "<p id='selectedLocations'></p>" +
                "<td valign='top'><div id='locationStrings' class='listItemBoxCustom'>" +
                "<span class='listItem'>" +
                "<input type='checkbox' name='locationStrings' id='locationStrings.L1' value='L1'><label for='locationStrings.L1'>L1</label>" +
                "</span>" +
                "<span class='listItem'>" +
                "<input type='checkbox' name='locationStrings' id='locationStrings.L2' value='L2'><label for='locationStrings.L2'>L2</label>" +
                "</span>" +
                "</div></td>";
        assertEquals(expected, content.generate());

    }

    @Test
    public void addLocationNameLineWithMappedLocationsChecked() {
        Content content = new Content(Arrays.asList("L1","L2"),Arrays.asList("L2"));

        String expected = "<style>.listItemBoxCustom {width: 460px;padding: 2px;border: 1px solid lightgray;float: left;background-color: #EFEFEF;overflow-x: scroll;height: 200px;}</style>" +
                "<td valign='top'>Location</td>" +
                "<p>Selected Locations:</p>" +
                "<p id='selectedLocations'></p>" +
                "<td valign='top'><div id='locationStrings' class='listItemBoxCustom'>" +
                "<span class='listItem'>" +
                "<input type='checkbox' name='locationStrings' id='locationStrings.L1' value='L1'><label for='locationStrings.L1'>L1</label>" +
                "</span>" +
                "<span class='listItem'>" +
                "<input type='checkbox' name='locationStrings' id='locationStrings.L2' value='L2' checked><label for='locationStrings.L2'>L2</label>" +
                "</span>" +
                "</div></td>";
        assertEquals(expected, content.generate());

    }

    @Test
    public void getFullContent() {
        String expected = "<style>.listItemBoxCustom {width: 460px;padding: 2px;border: 1px solid lightgray;float: left;background-color: #EFEFEF;overflow-x: scroll;height: 200px;}</style>" +
                "<td valign='top'>Location</td>" +
                "<p>Selected Locations:</p>" +
                "<p id='selectedLocations'></p>" +
                "<td valign='top'><div id='locationStrings' class='listItemBoxCustom'></div></td>";
        assertEquals(expected, new Content(new ArrayList<>(), new ArrayList<>()).generate());

    }
}

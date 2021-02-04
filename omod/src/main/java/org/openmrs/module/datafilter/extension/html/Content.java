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

import org.springframework.web.util.HtmlUtils;

import java.util.List;

public class Content {
	
	private final List<String> locationNames;
	
	private final List<String> selectedLocations;
	
	private String styles;
	
	private String scripts;
	
	private String title;
	
	private String contentStart;
	
	private String contentEnd;
	
	private static final String CHECKED = " checked";
	
	public Content(List<String> locationNames, List<String> selectedLocations) {
		this.locationNames = locationNames;
		this.selectedLocations = selectedLocations;
		this.styles = "<style>.listItemBoxCustom {width: 440px;" + "padding: 2px;" + "border: 1px solid lightgray;"
		        + "float: left;" + "background-color: #EFEFEF;" + "overflow-x: scroll;" + "height: 200px;}" + "</style>";
		this.scripts = "";
		this.title = "Location";
		this.contentStart = styles + scripts + "<td valign='top'>" + title + "</td>" + "<td valign='top'>"
		        + "<div id='locationStrings' class='listItemBoxCustom'>";
		this.contentEnd = "</div></td>";
	}
	
	public String generate() {
		return contentStart + addHTMLForLocationNames() + contentEnd;
	}
	
	private String addHTMLForLocationNames() {
		return locationNames.stream().reduce("", (appendedContent, locationName) -> {
			String checkedValue = "";
			if (selectedLocations.contains(locationName)) {
				checkedValue = CHECKED;
			}
			locationName = HtmlUtils.htmlEscape(locationName);
			appendedContent += "<span class='listItem'>";
			appendedContent += "<input type='checkbox' name='locationStrings' id='locationStrings." + locationName + "'"
			        + " value='" + locationName + "'" + checkedValue + ">";
			appendedContent += "<label for='locationStrings." + locationName + "'>" + locationName + "</label>";
			appendedContent += "</span>";
			return appendedContent;
		});
	}
	
}

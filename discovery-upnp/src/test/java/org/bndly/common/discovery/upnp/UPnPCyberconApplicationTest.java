package org.bndly.common.discovery.upnp;

/*-
 * #%L
 * org.bndly.ebx.discovery-upnp
 * %%
 * Copyright (C) 2013 - 2020 Cybercon GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.regex.Matcher;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author bndly &lt;bndly@cybercon.de&gt;
 */
public class UPnPCyberconApplicationTest {

	@Test
	public void testURLRegEx() {
		String input = "v6[/fe80:0:0:0:a21b:1d3d:f62c:2643%utun2]v6[/fe80:0:0:0:99ab:6a82:7194:2065%utun0]v4[/192.168.1.207]:9998";
		Matcher matcher = UPnPCyberconApplication.URL_PATTERN.matcher(input);
		Assert.assertTrue(matcher.matches());
	}
}

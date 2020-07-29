/*-
 * #%L
 * org.bndly.ebx.app-common
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
var page = require('webpage').create();

page.open("http://localhost:3000/test/index.html", function (status) {
  if (status != "success") {
    console.log("page couldn't be loaded successfully");
    phantom.exit(1);
  }
  waitFor(function () {
    return page.evaluate(function () {
      var output = document.getElementById('status');
      if (!output) { return false; }
      return (/^(\d+ failures?|all passed)/i).test(output.innerText);
    });
  }, function () {
    var failed = page.evaluate(function () { return window.failed; });
    var output = page.evaluate(function () {
      return document.getElementById('output').innerText + "\n" +
        document.getElementById('status').innerText;
    });
    console.log(output);
    phantom.exit(failed > 0 ? 1 : 0);
  });
});

function waitFor (test, cb) {
  if (test()) {
    cb();
  } else {
    setTimeout(function () { waitFor(test, cb); }, 250);
  }
}

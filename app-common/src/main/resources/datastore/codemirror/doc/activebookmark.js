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
// Kludge in HTML5 tag recognition in IE8
document.createElement("section");
document.createElement("article");

(function() {
  if (!window.addEventListener) return;
  var pending = false, prevVal = null;

  function updateSoon() {
    if (!pending) {
      pending = true;
      setTimeout(update, 250);
    }
  }

  function update() {
    pending = false;
    var marks = document.getElementById("nav").getElementsByTagName("a"), found;
    for (var i = 0; i < marks.length; ++i) {
      var mark = marks[i], m;
      if (mark.getAttribute("data-default")) {
        if (found == null) found = i;
      } else if (m = mark.href.match(/#(.*)/)) {
        var ref = document.getElementById(m[1]);
        if (ref && ref.getBoundingClientRect().top < 50)
          found = i;
      }
    }
    if (found != null && found != prevVal) {
      prevVal = found;
      var lis = document.getElementById("nav").getElementsByTagName("li");
      for (var i = 0; i < lis.length; ++i) lis[i].className = "";
      for (var i = 0; i < marks.length; ++i) {
        if (found == i) {
          marks[i].className = "active";
          for (var n = marks[i]; n; n = n.parentNode)
            if (n.nodeName == "LI") n.className = "active";
        } else {
          marks[i].className = "";
        }
      }
    }
  }

  window.addEventListener("scroll", updateSoon);
  window.addEventListener("load", updateSoon);
  window.addEventListener("hashchange", function() {
    setTimeout(function() {
      var hash = document.location.hash, found = null, m;
      var marks = document.getElementById("nav").getElementsByTagName("a");
      for (var i = 0; i < marks.length; i++)
        if ((m = marks[i].href.match(/(#.*)/)) && m[1] == hash) { found = i; break; }
      if (found != null) for (var i = 0; i < marks.length; i++)
        marks[i].className = i == found ? "active" : "";
    }, 300);
  });
})();

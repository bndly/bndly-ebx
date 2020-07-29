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
// CodeMirror, copyright (c) by Marijn Haverbeke and others
// Distributed under an MIT license: http://codemirror.net/LICENSE

(function(mod) {
  if (typeof exports == "object" && typeof module == "object") // CommonJS
    mod(require("../../lib/codemirror"), require("../../mode/css/css"));
  else if (typeof define == "function" && define.amd) // AMD
    define(["../../lib/codemirror", "../../mode/css/css"], mod);
  else // Plain browser env
    mod(CodeMirror);
})(function(CodeMirror) {
  "use strict";

  var pseudoClasses = {link: 1, visited: 1, active: 1, hover: 1, focus: 1,
                       "first-letter": 1, "first-line": 1, "first-child": 1,
                       before: 1, after: 1, lang: 1};

  CodeMirror.registerHelper("hint", "css", function(cm) {
    var cur = cm.getCursor(), token = cm.getTokenAt(cur);
    var inner = CodeMirror.innerMode(cm.getMode(), token.state);
    if (inner.mode.name != "css") return;

    if (token.type == "keyword" && "!important".indexOf(token.string) == 0)
      return {list: ["!important"], from: CodeMirror.Pos(cur.line, token.start),
              to: CodeMirror.Pos(cur.line, token.end)};

    var start = token.start, end = cur.ch, word = token.string.slice(0, end - start);
    if (/[^\w$_-]/.test(word)) {
      word = ""; start = end = cur.ch;
    }

    var spec = CodeMirror.resolveMode("text/css");

    var result = [];
    function add(keywords) {
      for (var name in keywords)
        if (!word || name.lastIndexOf(word, 0) == 0)
          result.push(name);
    }

    var st = inner.state.state;
    if (st == "pseudo" || token.type == "variable-3") {
      add(pseudoClasses);
    } else if (st == "block" || st == "maybeprop") {
      add(spec.propertyKeywords);
    } else if (st == "prop" || st == "parens" || st == "at" || st == "params") {
      add(spec.valueKeywords);
      add(spec.colorKeywords);
    } else if (st == "media" || st == "media_parens") {
      add(spec.mediaTypes);
      add(spec.mediaFeatures);
    }

    if (result.length) return {
      list: result,
      from: CodeMirror.Pos(cur.line, start),
      to: CodeMirror.Pos(cur.line, end)
    };
  });
});

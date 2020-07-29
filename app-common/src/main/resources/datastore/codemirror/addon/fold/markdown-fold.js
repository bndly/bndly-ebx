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
    mod(require("../../lib/codemirror"));
  else if (typeof define == "function" && define.amd) // AMD
    define(["../../lib/codemirror"], mod);
  else // Plain browser env
    mod(CodeMirror);
})(function(CodeMirror) {
"use strict";

CodeMirror.registerHelper("fold", "markdown", function(cm, start) {
  var maxDepth = 100;

  function isHeader(lineNo) {
    var tokentype = cm.getTokenTypeAt(CodeMirror.Pos(lineNo, 0));
    return tokentype && /\bheader\b/.test(tokentype);
  }

  function headerLevel(lineNo, line, nextLine) {
    var match = line && line.match(/^#+/);
    if (match && isHeader(lineNo)) return match[0].length;
    match = nextLine && nextLine.match(/^[=\-]+\s*$/);
    if (match && isHeader(lineNo + 1)) return nextLine[0] == "=" ? 1 : 2;
    return maxDepth;
  }

  var firstLine = cm.getLine(start.line), nextLine = cm.getLine(start.line + 1);
  var level = headerLevel(start.line, firstLine, nextLine);
  if (level === maxDepth) return undefined;

  var lastLineNo = cm.lastLine();
  var end = start.line, nextNextLine = cm.getLine(end + 2);
  while (end < lastLineNo) {
    if (headerLevel(end + 1, nextLine, nextNextLine) <= level) break;
    ++end;
    nextLine = nextNextLine;
    nextNextLine = cm.getLine(end + 2);
  }

  return {
    from: CodeMirror.Pos(start.line, firstLine.length),
    to: CodeMirror.Pos(end, cm.getLine(end).length)
  };
});

});

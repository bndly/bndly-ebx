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
    mod(require("../../lib/codemirror"), require("../htmlmixed/htmlmixed"),
        require("../../addon/mode/multiplex"));
  else if (typeof define == "function" && define.amd) // AMD
    define(["../../lib/codemirror", "../htmlmixed/htmlmixed",
            "../../addon/mode/multiplex"], mod);
  else // Plain browser env
    mod(CodeMirror);
})(function(CodeMirror) {
  "use strict";

  CodeMirror.defineMode("htmlembedded", function(config, parserConfig) {
    return CodeMirror.multiplexingMode(CodeMirror.getMode(config, "htmlmixed"), {
      open: parserConfig.open || parserConfig.scriptStartRegex || "<%",
      close: parserConfig.close || parserConfig.scriptEndRegex || "%>",
      mode: CodeMirror.getMode(config, parserConfig.scriptingModeSpec)
    });
  }, "htmlmixed");

  CodeMirror.defineMIME("application/x-ejs", {name: "htmlembedded", scriptingModeSpec:"javascript"});
  CodeMirror.defineMIME("application/x-aspx", {name: "htmlembedded", scriptingModeSpec:"text/x-csharp"});
  CodeMirror.defineMIME("application/x-jsp", {name: "htmlembedded", scriptingModeSpec:"text/x-java"});
  CodeMirror.defineMIME("application/x-erb", {name: "htmlembedded", scriptingModeSpec:"ruby"});
});

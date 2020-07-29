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

(function() {
  CodeMirror.defineMode("markdown_with_stex", function(){
    var inner = CodeMirror.getMode({}, "stex");
    var outer = CodeMirror.getMode({}, "markdown");

    var innerOptions = {
      open: '$',
      close: '$',
      mode: inner,
      delimStyle: 'delim',
      innerStyle: 'inner'
    };

    return CodeMirror.multiplexingMode(outer, innerOptions);
  });

  var mode = CodeMirror.getMode({}, "markdown_with_stex");

  function MT(name) {
    test.mode(
      name,
      mode,
      Array.prototype.slice.call(arguments, 1),
      'multiplexing');
  }

  MT(
    "stexInsideMarkdown",
    "[strong **Equation:**] [delim&delim-open $][inner&tag \\pi][delim&delim-close $]");
})();

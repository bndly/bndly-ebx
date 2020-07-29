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

CodeMirror.defineMode("eiffel", function() {
  function wordObj(words) {
    var o = {};
    for (var i = 0, e = words.length; i < e; ++i) o[words[i]] = true;
    return o;
  }
  var keywords = wordObj([
    'note',
    'across',
    'when',
    'variant',
    'until',
    'unique',
    'undefine',
    'then',
    'strip',
    'select',
    'retry',
    'rescue',
    'require',
    'rename',
    'reference',
    'redefine',
    'prefix',
    'once',
    'old',
    'obsolete',
    'loop',
    'local',
    'like',
    'is',
    'inspect',
    'infix',
    'include',
    'if',
    'frozen',
    'from',
    'external',
    'export',
    'ensure',
    'end',
    'elseif',
    'else',
    'do',
    'creation',
    'create',
    'check',
    'alias',
    'agent',
    'separate',
    'invariant',
    'inherit',
    'indexing',
    'feature',
    'expanded',
    'deferred',
    'class',
    'Void',
    'True',
    'Result',
    'Precursor',
    'False',
    'Current',
    'create',
    'attached',
    'detachable',
    'as',
    'and',
    'implies',
    'not',
    'or'
  ]);
  var operators = wordObj([":=", "and then","and", "or","<<",">>"]);

  function chain(newtok, stream, state) {
    state.tokenize.push(newtok);
    return newtok(stream, state);
  }

  function tokenBase(stream, state) {
    if (stream.eatSpace()) return null;
    var ch = stream.next();
    if (ch == '"'||ch == "'") {
      return chain(readQuoted(ch, "string"), stream, state);
    } else if (ch == "-"&&stream.eat("-")) {
      stream.skipToEnd();
      return "comment";
    } else if (ch == ":"&&stream.eat("=")) {
      return "operator";
    } else if (/[0-9]/.test(ch)) {
      stream.eatWhile(/[xXbBCc0-9\.]/);
      stream.eat(/[\?\!]/);
      return "ident";
    } else if (/[a-zA-Z_0-9]/.test(ch)) {
      stream.eatWhile(/[a-zA-Z_0-9]/);
      stream.eat(/[\?\!]/);
      return "ident";
    } else if (/[=+\-\/*^%<>~]/.test(ch)) {
      stream.eatWhile(/[=+\-\/*^%<>~]/);
      return "operator";
    } else {
      return null;
    }
  }

  function readQuoted(quote, style,  unescaped) {
    return function(stream, state) {
      var escaped = false, ch;
      while ((ch = stream.next()) != null) {
        if (ch == quote && (unescaped || !escaped)) {
          state.tokenize.pop();
          break;
        }
        escaped = !escaped && ch == "%";
      }
      return style;
    };
  }

  return {
    startState: function() {
      return {tokenize: [tokenBase]};
    },

    token: function(stream, state) {
      var style = state.tokenize[state.tokenize.length-1](stream, state);
      if (style == "ident") {
        var word = stream.current();
        style = keywords.propertyIsEnumerable(stream.current()) ? "keyword"
          : operators.propertyIsEnumerable(stream.current()) ? "operator"
          : /^[A-Z][A-Z_0-9]*$/g.test(word) ? "tag"
          : /^0[bB][0-1]+$/g.test(word) ? "number"
          : /^0[cC][0-7]+$/g.test(word) ? "number"
          : /^0[xX][a-fA-F0-9]+$/g.test(word) ? "number"
          : /^([0-9]+\.[0-9]*)|([0-9]*\.[0-9]+)$/g.test(word) ? "number"
          : /^[0-9]+$/g.test(word) ? "number"
          : "variable";
      }
      return style;
    },
    lineComment: "--"
  };
});

CodeMirror.defineMIME("text/x-eiffel", "eiffel");

});
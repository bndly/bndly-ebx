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
  if (typeof exports == "object" && typeof module == "object")
    mod(require("../../lib/codemirror"));
  else if (typeof define == "function" && define.amd)
    define(["../../lib/codemirror"], mod);
  else
    mod(CodeMirror);
})(function(CodeMirror) {
"use strict";

CodeMirror.defineMode('troff', function() {

  var words = {};

  function tokenBase(stream) {
    if (stream.eatSpace()) return null;

    var sol = stream.sol();
    var ch = stream.next();

    if (ch === '\\') {
      if (stream.match('fB') || stream.match('fR') || stream.match('fI') ||
          stream.match('u')  || stream.match('d')  ||
          stream.match('%')  || stream.match('&')) {
        return 'string';
      }
      if (stream.match('m[')) {
        stream.skipTo(']');
        stream.next();
        return 'string';
      }
      if (stream.match('s+') || stream.match('s-')) {
        stream.eatWhile(/[\d-]/);
        return 'string';
      }
      if (stream.match('\(') || stream.match('*\(')) {
        stream.eatWhile(/[\w-]/);
        return 'string';
      }
      return 'string';
    }
    if (sol && (ch === '.' || ch === '\'')) {
      if (stream.eat('\\') && stream.eat('\"')) {
        stream.skipToEnd();
        return 'comment';
      }
    }
    if (sol && ch === '.') {
      if (stream.match('B ') || stream.match('I ') || stream.match('R ')) {
        return 'attribute';
      }
      if (stream.match('TH ') || stream.match('SH ') || stream.match('SS ') || stream.match('HP ')) {
        stream.skipToEnd();
        return 'quote';
      }
      if ((stream.match(/[A-Z]/) && stream.match(/[A-Z]/)) || (stream.match(/[a-z]/) && stream.match(/[a-z]/))) {
        return 'attribute';
      }
    }
    stream.eatWhile(/[\w-]/);
    var cur = stream.current();
    return words.hasOwnProperty(cur) ? words[cur] : null;
  }

  function tokenize(stream, state) {
    return (state.tokens[0] || tokenBase) (stream, state);
  };

  return {
    startState: function() {return {tokens:[]};},
    token: function(stream, state) {
      return tokenize(stream, state);
    }
  };
});

CodeMirror.defineMIME('troff', 'troff');

});

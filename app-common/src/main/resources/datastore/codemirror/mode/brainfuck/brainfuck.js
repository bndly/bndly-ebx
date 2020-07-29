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

// Brainfuck mode created by Michael Kaminsky https://github.com/mkaminsky11

(function(mod) {
  if (typeof exports == "object" && typeof module == "object")
    mod(require("../../lib/codemirror"))
  else if (typeof define == "function" && define.amd)
    define(["../../lib/codemirror"], mod)
  else
    mod(CodeMirror)
})(function(CodeMirror) {
  "use strict"
  var reserve = "><+-.,[]".split("");
  /*
  comments can be either:
  placed behind lines

        +++    this is a comment

  where reserved characters cannot be used
  or in a loop
  [
    this is ok to use [ ] and stuff
  ]
  or preceded by #
  */
  CodeMirror.defineMode("brainfuck", function() {
    return {
      startState: function() {
        return {
          commentLine: false,
          left: 0,
          right: 0,
          commentLoop: false
        }
      },
      token: function(stream, state) {
        if (stream.eatSpace()) return null
        if(stream.sol()){
          state.commentLine = false;
        }
        var ch = stream.next().toString();
        if(reserve.indexOf(ch) !== -1){
          if(state.commentLine === true){
            if(stream.eol()){
              state.commentLine = false;
            }
            return "comment";
          }
          if(ch === "]" || ch === "["){
            if(ch === "["){
              state.left++;
            }
            else{
              state.right++;
            }
            return "bracket";
          }
          else if(ch === "+" || ch === "-"){
            return "keyword";
          }
          else if(ch === "<" || ch === ">"){
            return "atom";
          }
          else if(ch === "." || ch === ","){
            return "def";
          }
        }
        else{
          state.commentLine = true;
          if(stream.eol()){
            state.commentLine = false;
          }
          return "comment";
        }
        if(stream.eol()){
          state.commentLine = false;
        }
      }
    };
  });
CodeMirror.defineMIME("text/x-brainfuck","brainfuck")
});

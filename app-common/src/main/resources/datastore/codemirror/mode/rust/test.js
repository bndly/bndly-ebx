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
  var mode = CodeMirror.getMode({indentUnit: 4}, "rust");
  function MT(name) {test.mode(name, mode, Array.prototype.slice.call(arguments, 1));}

  MT('integer_test',
     '[number 123i32]',
     '[number 123u32]',
     '[number 123_u32]',
     '[number 0xff_u8]',
     '[number 0o70_i16]',
     '[number 0b1111_1111_1001_0000_i32]',
     '[number 0usize]');

  MT('float_test',
     '[number 123.0f64]',
     '[number 0.1f64]',
     '[number 0.1f32]',
     '[number 12E+99_f64]');

  MT('string-literals-test',
     '[string "foo"]',
     '[string r"foo"]',
     '[string "\\"foo\\""]',
     '[string r#""foo""#]',
     '[string "foo #\\"# bar"]',

     '[string b"foo"]',
     '[string br"foo"]',
     '[string b"\\"foo\\""]',
     '[string br#""foo""#]',
     '[string br##"foo #" bar"##]',

     "[string-2 'h']",
     "[string-2 b'h']");

})();

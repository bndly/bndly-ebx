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
  var mode = CodeMirror.getMode({indentUnit: 2}, "mscgen");
  function MT(name) { test.mode(name, mode, Array.prototype.slice.call(arguments, 1)); }

  MT("empty chart",
     "[keyword msc][bracket {]",
     "[base   ]",
     "[bracket }]"
   );

  MT("comments",
    "[comment // a single line comment]",
    "[comment # another  single line comment /* and */ ignored here]",
    "[comment /* A multi-line comment even though it contains]",
    "[comment msc keywords and \"quoted text\"*/]");

  MT("strings",
    "[string \"// a string\"]",
    "[string \"a string running over]",
    "[string two lines\"]",
    "[string \"with \\\"escaped quote\"]"
  );

  MT("xù/ msgenny keywords classify as 'base'",
    "[base watermark]",
    "[base alt loop opt ref else break par seq assert]"
  );

  MT("mscgen options classify as keyword",
    "[keyword hscale]", "[keyword width]", "[keyword arcgradient]", "[keyword wordwraparcs]"
  );

  MT("mscgen arcs classify as keyword",
    "[keyword note]","[keyword abox]","[keyword rbox]","[keyword box]",
    "[keyword |||...---]", "[keyword ..--==::]",
    "[keyword ->]", "[keyword <-]", "[keyword <->]",
    "[keyword =>]", "[keyword <=]", "[keyword <=>]",
    "[keyword =>>]", "[keyword <<=]", "[keyword <<=>>]",
    "[keyword >>]", "[keyword <<]", "[keyword <<>>]",
    "[keyword -x]", "[keyword x-]", "[keyword -X]", "[keyword X-]",
    "[keyword :>]", "[keyword <:]", "[keyword <:>]"
  );

  MT("within an attribute list, attributes classify as attribute",
    "[bracket [[][attribute label]",
    "[attribute id]","[attribute url]","[attribute idurl]",
    "[attribute linecolor]","[attribute linecolour]","[attribute textcolor]","[attribute textcolour]","[attribute textbgcolor]","[attribute textbgcolour]",
    "[attribute arclinecolor]","[attribute arclinecolour]","[attribute arctextcolor]","[attribute arctextcolour]","[attribute arctextbgcolor]","[attribute arctextbgcolour]",
    "[attribute arcskip][bracket ]]]"
  );

  MT("outside an attribute list, attributes classify as base",
    "[base label]",
    "[base id]","[base url]","[base idurl]",
    "[base linecolor]","[base linecolour]","[base textcolor]","[base textcolour]","[base textbgcolor]","[base textbgcolour]",
    "[base arclinecolor]","[base arclinecolour]","[base arctextcolor]","[base arctextcolour]","[base arctextbgcolor]","[base arctextbgcolour]",
    "[base arcskip]"
  );

  MT("a typical program",
    "[comment # typical mscgen program]",
    "[keyword msc][base  ][bracket {]",
    "[keyword wordwraparcs][operator =][string \"true\"][base , ][keyword hscale][operator =][string \"0.8\"][keyword arcgradient][operator =][base 30;]",
    "[base   a][bracket [[][attribute label][operator =][string \"Entity A\"][bracket ]]][base ,]",
    "[base   b][bracket [[][attribute label][operator =][string \"Entity B\"][bracket ]]][base ,]",
    "[base   c][bracket [[][attribute label][operator =][string \"Entity C\"][bracket ]]][base ;]",
    "[base   a ][keyword =>>][base  b][bracket [[][attribute label][operator =][string \"Hello entity B\"][bracket ]]][base ;]",
    "[base   a ][keyword <<][base  b][bracket [[][attribute label][operator =][string \"Here's an answer dude!\"][bracket ]]][base ;]",
    "[base   c ][keyword :>][base  *][bracket [[][attribute label][operator =][string \"What about me?\"][base , ][attribute textcolor][operator =][base red][bracket ]]][base ;]",
    "[bracket }]"
  );
})();

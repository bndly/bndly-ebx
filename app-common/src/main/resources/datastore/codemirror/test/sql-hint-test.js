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
  var Pos = CodeMirror.Pos;

  var simpleTables = {
    "users": ["name", "score", "birthDate"],
    "xcountries": ["name", "population", "size"]
  };

  var schemaTables = {
    "schema.users": ["name", "score", "birthDate"],
    "schema.countries": ["name", "population", "size"]
  };

  var displayTextTables = [{
    text: "mytable",
    displayText: "mytable | The main table",
    columns: [{text: "id", displayText: "id | Unique ID"},
              {text: "name", displayText: "name | The name"}]
  }];

  namespace = "sql-hint_";

  function test(name, spec) {
    testCM(name, function(cm) {
      cm.setValue(spec.value);
      cm.setCursor(spec.cursor);
      var completion = CodeMirror.hint.sql(cm, {tables: spec.tables});
      if (!deepCompare(completion.list, spec.list))
        throw new Failure("Wrong completion results " + JSON.stringify(completion.list) + " vs " + JSON.stringify(spec.list));
      eqPos(completion.from, spec.from);
      eqPos(completion.to, spec.to);
    }, {
      value: spec.value,
      mode: "text/x-mysql"
    });
  }

  test("keywords", {
    value: "SEL",
    cursor: Pos(0, 3),
    list: ["SELECT"],
    from: Pos(0, 0),
    to: Pos(0, 3)
  });

  test("from", {
    value: "SELECT * fr",
    cursor: Pos(0, 11),
    list: ["FROM"],
    from: Pos(0, 9),
    to: Pos(0, 11)
  });

  test("table", {
    value: "SELECT xc",
    cursor: Pos(0, 9),
    tables: simpleTables,
    list: ["xcountries"],
    from: Pos(0, 7),
    to: Pos(0, 9)
  });

  test("columns", {
    value: "SELECT users.",
    cursor: Pos(0, 13),
    tables: simpleTables,
    list: ["users.name", "users.score", "users.birthDate"],
    from: Pos(0, 7),
    to: Pos(0, 13)
  });

  test("singlecolumn", {
    value: "SELECT users.na",
    cursor: Pos(0, 15),
    tables: simpleTables,
    list: ["users.name"],
    from: Pos(0, 7),
    to: Pos(0, 15)
  });

  test("quoted", {
    value: "SELECT `users`.`na",
    cursor: Pos(0, 18),
    tables: simpleTables,
    list: ["`users`.`name`"],
    from: Pos(0, 7),
    to: Pos(0, 18)
  });

  test("quotedcolumn", {
    value: "SELECT users.`na",
    cursor: Pos(0, 16),
    tables: simpleTables,
    list: ["`users`.`name`"],
    from: Pos(0, 7),
    to: Pos(0, 16)
  });

  test("schema", {
    value: "SELECT schem",
    cursor: Pos(0, 12),
    tables: schemaTables,
    list: ["schema.users", "schema.countries",
           "SCHEMA", "SCHEMA_NAME", "SCHEMAS"],
    from: Pos(0, 7),
    to: Pos(0, 12)
  });

  test("schemaquoted", {
    value: "SELECT `sch",
    cursor: Pos(0, 11),
    tables: schemaTables,
    list: ["`schema`.`users`", "`schema`.`countries`"],
    from: Pos(0, 7),
    to: Pos(0, 11)
  });

  test("schemacolumn", {
    value: "SELECT schema.users.",
    cursor: Pos(0, 20),
    tables: schemaTables,
    list: ["schema.users.name",
           "schema.users.score",
           "schema.users.birthDate"],
    from: Pos(0, 7),
    to: Pos(0, 20)
  });

  test("schemacolumnquoted", {
    value: "SELECT `schema`.`users`.",
    cursor: Pos(0, 24),
    tables: schemaTables,
    list: ["`schema`.`users`.`name`",
           "`schema`.`users`.`score`",
           "`schema`.`users`.`birthDate`"],
    from: Pos(0, 7),
    to: Pos(0, 24)
  });

  test("displayText_table", {
    value: "SELECT myt",
    cursor: Pos(0, 10),
    tables: displayTextTables,
    list: displayTextTables,
    from: Pos(0, 7),
    to: Pos(0, 10)
  });

  test("displayText_column", {
    value: "SELECT mytable.",
    cursor: Pos(0, 15),
    tables: displayTextTables,
    list: [{text: "mytable.id", displayText: "id | Unique ID"},
           {text: "mytable.name", displayText: "name | The name"}],
    from: Pos(0, 7),
    to: Pos(0, 15)
  });

  test("alias_complete", {
    value: "SELECT t. FROM users t",
    cursor: Pos(0, 9),
    tables: simpleTables,
    list: ["t.name", "t.score", "t.birthDate"],
    from: Pos(0, 7),
    to: Pos(0, 9)
  });

  test("alias_complete_with_displayText", {
    value: "SELECT t. FROM mytable t",
    cursor: Pos(0, 9),
    tables: displayTextTables,
    list: [{text: "t.id", displayText: "id | Unique ID"},
           {text: "t.name", displayText: "name | The name"}],
    from: Pos(0, 7),
    to: Pos(0, 9)
  })

  function deepCompare(a, b) {
    if (!a || typeof a != "object")
      return a === b;
    if (!b || typeof b != "object")
      return false;
    for (var prop in a) if (!deepCompare(a[prop], b[prop])) return false;
    return true;
  }
})();

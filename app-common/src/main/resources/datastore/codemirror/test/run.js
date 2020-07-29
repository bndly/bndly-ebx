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
#!/usr/bin/env node

var ok = require("./lint").ok;

var files = new (require('node-static').Server)();

var server = require('http').createServer(function (req, res) {
  req.addListener('end', function () {
    files.serve(req, res, function (err/*, result */) {
      if (err) {
        console.error(err);
        process.exit(1);
      }
    });
  }).resume();
}).addListener('error', function (err) {
  throw err;
}).listen(3000, function () {
  var childProcess = require('child_process');
  var phantomjs = require("phantomjs");
  var childArgs = [
    require("path").join(__dirname, 'phantom_driver.js')
  ];
  childProcess.execFile(phantomjs.path, childArgs, function (err, stdout, stderr) {
    server.close();
    console.log(stdout);
    if (err) console.error(err);
    if (stderr) console.error(stderr);
    process.exit(err || stderr || !ok ? 1 : 0);
  });
});

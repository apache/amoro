const fs = require('fs');
const path = require('path');

const filePath = path.resolve(__dirname, './src');

const xmlLicenses = `
<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
/-->
`

const tsLicenses = `
/*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
`

const lessLicenses = `
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
`


function fileDisplay(filePath) {
  fs.readdir(filePath, function (err, files) {
    if (err) {
      console.warn('get file list error: ' + err);
    } else {
      for (let index = 0; index < files.length; index++) {
        const filename = files[index];
        const filedir = path.join(filePath, filename);
        fs.stat(filedir, function (eror, stats) {
          if (eror) {
            console.warn('get file stat error: ' + eror);
          } else {
            const isFile = stats.isFile();
            const isDir = stats.isDirectory();
            if (isFile) {
              const res = read(filedir);
              const fileName = path.basename(filedir);
              res && console.log('success file: ' + fileName);
            }
            if (isDir) {
              fileDisplay(filedir)
            }
          }
        })
      }
    }
  });
}

function read(fPath) {
  var data = fs.readFileSync(fPath, "utf-8");
  return write(data, fPath)
}

function write(data, fPath) {
  if (data.includes('Apache') && data.includes('License')) {
    return true
  }

  const fileName = path.basename(fPath);
  let license;
  if (fileName.endsWith('.ts') || fileName.endsWith('.js') || fileName.endsWith('.css')) {
    license = tsLicenses
  }
  if (fileName.endsWith('.vue') || fileName.endsWith('.html') || fileName.endsWith('.svg')) {
    license = xmlLicenses
  }
  if (fileName.endsWith('.less')) {
    license = lessLicenses
  }

  if (license) {
    const newFile = `${license}\n${data}`

    fs.writeFileSync(`${fPath}`, newFile, { encoding: 'utf-8' });
    return true
  }

  else {
    console.error(`not found license in file: ${fPath}. Please add more license manually!`)
    return false
  }
}

fileDisplay(filePath);

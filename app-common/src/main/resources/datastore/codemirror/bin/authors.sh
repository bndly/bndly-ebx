###
# #%L
# org.bndly.ebx.app-common
# %%
# Copyright (C) 2013 - 2020 Cybercon GmbH
# %%
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# #L%
###
# Combine existing list of authors with everyone known in git, sort, add header.
tail --lines=+3 AUTHORS > AUTHORS.tmp
git log --format='%aN' >> AUTHORS.tmp
echo -e "List of CodeMirror contributors. Updated before every release.\n" > AUTHORS
sort -u AUTHORS.tmp >> AUTHORS
rm -f AUTHORS.tmp

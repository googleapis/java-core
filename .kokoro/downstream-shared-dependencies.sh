#!/bin/bash
# Copyright 2020 Google LLC
#
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

# Presubmit to ensure the dependencies of the Google Libraries BOM, with the modification of change
# in the PR, pick up the highest versions among transitive dependencies.
# https://maven.apache.org/enforcer/enforcer-rules/requireUpperBoundDeps.html

set -eo pipefail
# Display commands being run.
set -x

if [[ $# -lt 1 ]];
then
  echo "Usage: $0 <repo-name>"
  exit 1
fi
REPO=$1

## Get the directory of the build script
scriptDir=$(realpath $(dirname "${BASH_SOURCE[0]}"))
## cd to the parent directory, i.e. the root of the git repo
cd ${scriptDir}/..

# Make java-core artifacts available for 'mvn validate' at the bottom
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgcloud.download.skip=true -B -V -q

# Read the current version of this java-core in the POM. Example version: '0.116.1-alpha-SNAPSHOT'
CORE_VERSION_POM=pom.xml
# Namespace (xmlns) prevents xmllint from specifying tag names in XPath
CORE_VERSION=`sed -e 's/xmlns=".*"//' ${CORE_VERSION_POM} | xmllint --xpath '/project/version/text()' -`

if [ -z "${CORE_VERSION}" ]; then
  echo "Version is not found in ${CORE_VERSION_POM}"
  exit 1
fi
echo "Version: ${CORE_VERSION}"

# Check this java-core against HEAD of java-shared dependencies

git clone "https://github.com/googleapis/java-${REPO}.git" --depth=1
pushd java-${REPO}/first-party-dependencies

# replace version
xmllint --shell <(cat pom.xml) << EOF
setns x=http://maven.apache.org/POM/4.0.0
cd .//x:artifactId[text()="google-cloud-core-bom"]
cd ../x:version
set ${CORE_VERSION}
save pom.xml
EOF

# run dependencies script
cd ..
mvn -Denforcer.skip=true clean install

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
SHARED_DEPS_VERSION=$2
echo $SHARED_DEPS_VERSION
FILE_PATH="$HOME/.m2/repository/com/google/cloud/google-cloud-shared-dependencies/${SHARED_DEPS_VERSION}/google-cloud-shared-dependencies-${SHARED_DEPS_VERSION}-tests.jar"

if [ ! -f "$FILE_PATH" ]; then
    echo "$FILE_PATH does not exist."
    exit 1
fi
## Get the directory of the build script
scriptDir=$(realpath $(dirname "${BASH_SOURCE[0]}"))
## cd to the parent directory, i.e. the root of the git repo
cd ${scriptDir}/..

# Check this java client library against the packaged version of java-shared-dependencies

#git clone "https://github.com/googleapis/java-${REPO}.git" --depth=1
#pushd java-${REPO}

mvn install:install-file -Dfile=${FILE_PATH} -DgroupId=com.google.cloud -DartifactId=google-cloud-shared-dependencies -Dversion=${SHARED_DEPS_VERSION} -Dpackaging=jar

# replace version
xmllint --shell <(cat pom.xml) << EOF
setns x=http://maven.apache.org/POM/4.0.0
cd .//x:artifactId[text()="google-cloud-shared-dependencies"]
cd ../x:version
set ${SHARED_DEPS_VERSION}
save pom.xml
EOF

# run dependencies script
mvn -Denforcer.skip=true clean install

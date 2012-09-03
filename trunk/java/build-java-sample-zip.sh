#!/bin/sh
#
# Copyright 2012 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# Usage: build-java-sample-zip.sh [datestr]
#
# Copies the Java sample files to a temporary directory and zips them up.
# The resulting ZIP file is left in /tmp.
# If datestr is not specified, it defaults to the current date in YYYYMMDD
# format; for example, "20120904". This would result in the zipfile
# /tmp/oauth2-java-sample-20120904.zip.
#
# The script is intended to be run from the root of the Java code hierarchy.
# It makes sure there are no local modifications to the files that are
# being zipped up.


top_level_files="README-java-sample.txt build.xml build.properties ../python/oauth2.py"
relative_files="com/google/code/samples/oauth2/*.java"
all_files="$top_level_files $relative_files"

if [[ "$1" ]] ; then
  date="$1"
else
  date=$(date "+%Y%m%d")
fi

relative_tmpdir="oauth2-java-sample-$date"
full_tmpdir="/tmp/$relative_tmpdir"
outfile="/tmp/oauth2-java-sample-$date.zip"

if [[ -e $full_tmpdir ]]; then
  echo "ERROR: directory $full_tmpdir already exists"
  exit -1
fi

if [[ -e $outfile ]]; then
  echo "ERROR: $outfile already exists"
  exit -1
fi

status=$(svn status $all_files)
if [[ "$status" ]] ; then
  echo "ERROR: One or more files has uncommitted changes:"
  echo "$status"
  exit -1
fi

mkdir -p $full_tmpdir
cp $top_level_files $full_tmpdir
cp --parents $relative_files $full_tmpdir

cd /tmp
zip -r $outfile $relative_tmpdir
rm -r $full_tmpdir

echo "Created $outfile"

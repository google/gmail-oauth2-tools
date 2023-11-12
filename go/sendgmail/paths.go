// Copyright 2023 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package main

import (
	"log"
	"os"
	"path/filepath"
)

var useXDG bool = true

func userHomeDir() string {
	dir, err := os.UserHomeDir()
	if err != nil {
		log.Fatalf("Failed to get user home directory: %v.", err)
	}
	return dir
}

func userConfigDir() string {
	if dir := os.Getenv("XDG_CONFIG_HOME"); dir != "" {
		return dir
	}
	dir, err := os.UserHomeDir()
	if err != nil {
		log.Fatalf("Neither $XDG_CONFIG_HOME nor the user home directory is defined: %v.", err)
	}
	return filepath.Join(dir, ".config")
}

func dirExists(path string) bool {
	st, err := os.Stat(path)
	return err == nil && st.IsDir()
}

func fileExists(path string) bool {
	st, err := os.Stat(path)
	return err == nil && st.Mode().IsRegular()
}

func init() {
	switch {
	case dirExists(filepath.Join(userConfigDir(), "sendgmail")):
		useXDG = true
	case fileExists(filepath.Join(userHomeDir(), ".sendgmail.json")):
		useXDG = false
	default:
		useXDG = true
	}
}

func configPath() string {
	if useXDG {
		return filepath.Join(userConfigDir(), "sendgmail", "config.json")
	} else {
		return filepath.Join(userHomeDir(), ".sendgmail.json")
	}
}

func tokenPath() string {
	if useXDG {
		return filepath.Join(userConfigDir(), "sendgmail", "token."+sender+".json")
	} else {
		return filepath.Join(userHomeDir(), ".sendgmail."+sender+".json")
	}
}

// Copyright 2019 Google Inc. All Rights Reserved.
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

// sendgmail is a tool that uses Gmail in order to mimic `sendmail` for `git send-email`.
//
// USAGE:
//
// $ /PATH/TO/sendgmail -sender=USERNAME@gmail.com -setup
//
// $ git send-email --smtp-server=/PATH/TO/sendgmail --smtp-server-option=-sender=USERNAME@gmail.com ...
package main

import (
	"context"
	"encoding/base64"
	"encoding/json"
	"flag"
	"io"
	"log"
	"os"
	"path/filepath"
	"strings"
	"sync"

	"golang.org/x/oauth2"
	googleOAuth2 "golang.org/x/oauth2/google"

	"google.golang.org/api/gmail/v1"
	"google.golang.org/api/option"
)

var (
	sender string
	setUp  bool
	dummyF string
	dummyI bool
	dryRun bool
	scopes []string = []string{"https://www.googleapis.com/auth/gmail.send"}
)

const MIME_LINE = "\r\n"

func init() {
	flag.StringVar(&sender, "sender", "", "Specifies the sender's email address.")
	flag.BoolVar(&setUp, "setup", false, "If true, sendgmail sets up the sender's OAuth2 token and then exits.")
	flag.StringVar(&dummyF, "f", "", "Dummy flag for compatibility with sendmail.")
	flag.BoolVar(&dummyI, "i", true, "Dummy flag for compatibility with sendmail.")
	flag.BoolVar(&dryRun, "dry-run", false, "only print, do not send")
}

func main() {
	flag.Parse()
	// Originally, this checked for "@gmail.com" as a suffix,
	// but any Google Workspace domain can also be supported.
	// Checking only for '@' gives rudimentary assurance that
	// the user specified an email address; the complexity of
	// performing deeper checks is unwarranted at this point.
	if !strings.ContainsRune(sender, '@') {
		log.Fatalf("-sender must specify an email address.")
	}
	config := getConfig()
	if setUp {
		setUpToken()
		return
	}
	sendMessage(config)
}

func configJSON() []byte {
	configJSON, err := os.ReadFile(configPath())
	if err != nil {
		log.Fatalf("Failed to read config: %v.", err)
	}
	return configJSON
}

func getConfig() *oauth2.Config {
	config, err := googleOAuth2.ConfigFromJSON(configJSON(), "https://mail.google.com/")
	if err != nil {
		log.Fatalf("Failed to parse config: %v.", err)
	}
	return config
}

func setUpToken() {
	aHandler := newCallbackHandler()
	credentials, err := aHandler.getCredentials()
	if err != nil {
		log.Fatalf("Failed to exchange authorisation code for token: %v.", err)
	}
	token, err := credentials.TokenSource.Token()
	if err != nil {
		log.Fatalf("Failed to exchange authorisation code for token: %v.", err)
	}
	tokenFile, err := os.OpenFile(tokenPath(), os.O_WRONLY|os.O_CREATE|os.O_TRUNC, 0600)
	if err != nil {
		log.Fatalf("Failed to open token file for writing: %v.", err)
	}
	defer tokenFile.Close()
	if err := json.NewEncoder(tokenFile).Encode(token); err != nil {
		log.Fatalf("Failed to write token: %v.", err)
	}
}

type messageHeader struct {
	to, cc, bcc []string
}

func parseArgs(args []string) (mh messageHeader) {
	mh.to, mh.cc, mh.bcc = make([]string, 0), make([]string, 0), make([]string, 0)
	for _, arg := range args {
		parts := strings.Split(arg, ":")
		switch parts[0] {
		case "bcc":
			mh.bcc = append(mh.bcc, parts[1])
		case "cc":
			mh.cc = append(mh.cc, parts[1])
		default:
			mh.to = append(mh.to, parts[0])
		}
	}
	return
}

func (mh messageHeader) mimeHeader() string {
	var header strings.Builder
	header.WriteString("To: " + strings.Join(mh.to, ",") + MIME_LINE)
	if len(mh.bcc) > 0 {
		header.WriteString("Bcc: " + strings.Join(mh.bcc, ",") + MIME_LINE)
	}
	if len(mh.cc) > 0 {
		header.WriteString("Cc: " + strings.Join(mh.cc, ",") + MIME_LINE)
	}
	return header.String()
}

func sendMessage(config *oauth2.Config) {
	tokenFile, err := os.Open(tokenPath())
	if err != nil {
		log.Fatalf("Failed to open token file for reading: %v.", err)
	}
	defer tokenFile.Close()
	var token oauth2.Token
	if err := json.NewDecoder(tokenFile).Decode(&token); err != nil {
		log.Fatalf("Failed to read token: %v.", err)
	}
	ctx := context.Background()
	tokenSource := config.TokenSource(ctx, &token)
	srv, err := gmail.NewService(ctx, option.WithTokenSource(tokenSource))
	if err != nil {
		log.Fatalf("Unable to retrieve Gmail client: %v", err)
	}
	message, err := io.ReadAll(os.Stdin)
	if err != nil {
		log.Fatalf("unable to read stdin")
	}
	header := parseArgs(flag.Args())
	headerByte := []byte(header.mimeHeader())
	fullMessage := append(headerByte[:], message[:]...)

	gmsg := &gmail.Message{
		Raw: base64.URLEncoding.EncodeToString(fullMessage),
	}
	if dryRun {
		return
	}
	_, err = srv.Users.Messages.Send("me", gmsg).Do()
	if err != nil {
		log.Fatalf("Unable to send email: %v", err)
	}
}

func userConfigDir() string {
	if dir := os.Getenv("XDG_CONFIG_HOME"); dir != "" {
		return dir
	}
	if dir := os.Getenv("HOME"); dir != "" {
		return filepath.Join(dir, ".config")
	}
	panic("Neither $XDG_CONFIG_HOME nor $HOME is defined.")
}

func userHomeDir() string {
	dir, err := os.UserHomeDir()
	if err != nil {
		log.Fatalf("Failed to get user home directory: %v.", err)
	}
	return dir
}

var isXDG = sync.OnceValue(func() bool {
	if _, err := os.Stat(filepath.Join(userConfigDir(), "sendgmail", "config.json")); err == nil {
		return true
	}
	if _, err := os.Stat(filepath.Join(userHomeDir(), ".sendgmail.json")); err == nil {
		return false
	}
	return true
})

func configPath() string {
	if isXDG() {
		return filepath.Join(userConfigDir(), "sendgmail", "config.json")
	} else {
		return filepath.Join(userHomeDir(), ".sendgmail.json")
	}
}

func tokenPath() string {
	if isXDG() {
		return filepath.Join(userConfigDir(), "sendgmail", "token."+sender+".json")
	} else {
		return filepath.Join(userHomeDir(), ".sendgmail."+sender+".json")
	}
}

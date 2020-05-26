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
	"encoding/json"
	"flag"
	"fmt"
	"io/ioutil"
	"log"
	"net/smtp"
	"os"

	"golang.org/x/oauth2"
	googleOAuth2 "golang.org/x/oauth2/google"
)

var (
	sender string
	setUp  bool
	dummyF string
	dummyI bool
)

func init() {
	flag.StringVar(&sender, "sender", "", "Specifies the sender's email address.")
	flag.BoolVar(&setUp, "setup", false, "If true, sendgmail sets up the sender's OAuth2 token and then exits.")
	flag.StringVar(&dummyF, "f", "", "Dummy flag for compatibility with sendmail.")
	flag.BoolVar(&dummyI, "i", true, "Dummy flag for compatibility with sendmail.")
}

func main() {
	flag.Parse()
	config := getConfig()
	tokenPath := fmt.Sprintf("%v/.sendgmail.%v.json", os.Getenv("HOME"), sender)
	if setUp {
		setUpToken(config, tokenPath)
		return
	}
	sendMessage(config, tokenPath)
}

func getConfig() *oauth2.Config {
	configPath := fmt.Sprintf("%v/.sendgmail.json", os.Getenv("HOME"))
	configJSON, err := ioutil.ReadFile(configPath)
	if err != nil {
		log.Fatalf("Failed to read config: %v.", err)
	}
	config, err := googleOAuth2.ConfigFromJSON(configJSON, "https://mail.google.com/")
	if err != nil {
		log.Fatalf("Failed to parse config: %v.", err)
	}
	return config
}

func setUpToken(config *oauth2.Config, tokenPath string) {
	fmt.Println()
	fmt.Println("1. Ensure that you are logged in as", sender, "in your browser.")
	fmt.Println()
	fmt.Println("2. Open the following link and authorise sendgmail:")
	fmt.Println(config.AuthCodeURL("state", oauth2.AccessTypeOffline))
	fmt.Println()
	fmt.Println("3. Enter the authorisation code:")
	var code string
	if _, err := fmt.Scan(&code); err != nil {
		log.Fatalf("Failed to read authorisation code: %v.", err)
	}
	fmt.Println()
	token, err := config.Exchange(context.Background(), code)
	if err != nil {
		log.Fatalf("Failed to exchange authorisation code for token: %v.", err)
	}
	tokenFile, err := os.OpenFile(tokenPath, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, 0600)
	if err != nil {
		log.Fatalf("Failed to open token file for writing: %v.", err)
	}
	defer tokenFile.Close()
	if err := json.NewEncoder(tokenFile).Encode(token); err != nil {
		log.Fatalf("Failed to write token: %v.", err)
	}
}

func sendMessage(config *oauth2.Config, tokenPath string) {
	tokenFile, err := os.Open(tokenPath)
	if err != nil {
		log.Fatalf("Failed to open token file for reading: %v.", err)
	}
	defer tokenFile.Close()
	var token oauth2.Token
	if err := json.NewDecoder(tokenFile).Decode(&token); err != nil {
		log.Fatalf("Failed to read token: %v.", err)
	}
	tokenSource := config.TokenSource(context.Background(), &token)
	message, err := ioutil.ReadAll(os.Stdin)
	if err != nil {
		log.Fatalf("Failed to read message: %v.", err)
	}
	if err := smtp.SendMail("smtp.gmail.com:587", authWith(tokenSource), sender, flag.Args(), message); err != nil {
		log.Fatalf("Failed to send message: %v.", err)
	}
}

func authWith(tokenSource oauth2.TokenSource) *auth {
	return &auth{ts: tokenSource}
}

type auth struct {
	ts oauth2.TokenSource
}

func (a *auth) Start(serverInfo *smtp.ServerInfo) (string, []byte, error) {
	if !serverInfo.TLS {
		return "", nil, fmt.Errorf("unencrypted connection: %v", serverInfo)
	}
	token, err := a.ts.Token()
	if err != nil {
		return "", nil, fmt.Errorf("failed to get token: %v", err)
	}
	toServer := fmt.Sprintf("user=%v\001auth=%v %v\001\001", sender, token.Type(), token.AccessToken)
	return "XOAUTH2", []byte(toServer), nil
}

func (a *auth) Next(fromServer []byte, more bool) ([]byte, error) {
	if more {
		return nil, fmt.Errorf("unexpected challenge: %v", string(fromServer))
	}
	return nil, nil
}

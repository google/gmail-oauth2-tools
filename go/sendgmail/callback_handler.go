package main

import (
	"context"
	"encoding/base64"
	"fmt"
	"log"
	"net/http"

	"crypto/sha256"
	"github.com/google/uuid"
	"golang.org/x/oauth2/authhandler"
	googleOAuth2 "golang.org/x/oauth2/google"
)

// http server handling oauth2 callback 3-legged-flow
type callbackHandler struct {
	codeChan chan string
	state    string
}

// basic handler on / for receiving code
func (h *callbackHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	query := r.URL.Query()
	if query.Has("error") {
		log.Printf("Oauth error: %s\n", query.Get("error"))
		return
	}
	if !(query.Has("code")) {
		w.Write([]byte("expecting query param: code"))
		return
	}
	code := query.Get("code")
	w.Write([]byte(fmt.Sprintf("code: %s\n", code)))
	h.codeChan <- code
	close(h.codeChan)
}

// prompt user on CLI for oauth consent screen, wait on channel for code from http
func (h *callbackHandler) authHandler(authCodeURL string) (string, string, error) {
	fmt.Println()
	fmt.Println("1. Ensure that you are logged in as", sender, "in your browser.")
	fmt.Println()
	fmt.Println("2. Open the following link and authorise sendgmail:")
	fmt.Println(authCodeURL + "&access_type=offline&prompt=consent") // hack to obtain a refresh token
	fmt.Println()
	fmt.Println("3. Waiting for authorization code:")
	return <-h.codeChan, h.state, nil
}

// oauth signing / challenge / etc
func (h *callbackHandler) getCredentials() (*googleOAuth2.Credentials, error) {
	verifier := uuid.NewString()
	s256 := sha256.Sum256([]byte(verifier))
	challenge := base64.RawURLEncoding.EncodeToString(s256[:])
	pkceParams := authhandler.PKCEParams{Challenge: challenge, ChallengeMethod: "S256", Verifier: verifier}
	credentialsParams := googleOAuth2.CredentialsParams{Scopes: scopes, AuthHandler: h.authHandler, PKCE: &pkceParams, State: h.state}
	return googleOAuth2.CredentialsFromJSONWithParams(context.Background(), configJSON(), credentialsParams)
}

// set up the server and listen on localhost:5000
func newCallbackHandler() *callbackHandler {
	var h callbackHandler
	h.codeChan = make(chan string)
	h.state = uuid.NewString()
	go func() {
		http.Handle("/", &h)
		http.ListenAndServe("127.0.0.1:5000", nil)
	}()
	return &h
}

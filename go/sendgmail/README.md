# sendgmail

sendgmail is a tool that uses Gmail in order to mimic `sendmail` for `git
send-email`.

## Obtaining OAuth2 credentials for sendgmail

1.  Go to the
    [Go Quickstart](https://developers.google.com/gmail/api/quickstart/go) page
    in the Gmail API documentation.

2.  Click the **ENABLE THE GMAIL API** button.

3.  Click the **DOWNLOAD CLIENT CONFIGURATION** button.

4.  Save the file to your home directory as `.sendgmail.json`.

A project named *Quickstart* has been created for you, but you probably want to
go to the [Google Cloud console](https://console.cloud.google.com/) and rename
the project something like *sendgmail* in **IAM & Admin > Settings** and also in
**APIs & Services > Credentials > OAuth consent screen**.

## Installing sendgmail

Run the following command to build and install sendgmail to
`GOPATH/bin/sendgmail`:

```shell
go get -u github.com/google/gmail-oauth2-tools/go/sendgmail
```

## Obtaining OAuth2 credentials for yourself

Run the following command to perform the OAuth2 dance:

```shell
GOPATH/bin/sendgmail -sender=USERNAME@gmail.com -setup
```

## Using sendgmail

Add the following section to `.gitconfig` in your home directory:

```
[sendemail]
    smtpServer = GOPATH/bin/sendgmail
    smtpServerOption = -sender=USERNAME@gmail.com
```

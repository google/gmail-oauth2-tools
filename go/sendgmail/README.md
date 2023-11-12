# sendgmail

sendgmail is a tool that uses Gmail in order to mimic `sendmail` for `git
send-email`.

## Obtaining OAuth2 credentials for sendgmail

1.  Go to the [Google Cloud console](https://console.cloud.google.com/).

    *   Create a new project. You probably want to name it something like
        *sendgmail* in **IAM & Admin > Settings** and also in **APIs &
        Services > OAuth consent screen**.

2.  Go to the
    [Go quickstart](https://developers.google.com/gmail/api/quickstart/go) page
    of the Gmail API documentation.

    *   Click the **Enable the API** button. It will open another page in your
        browser. Follow the steps on that page to enable the Gmail API for the
        project that you created.

    *   Follow the steps in the **Authorize credentials for a desktop
        application** section. However, set the application type to *Web
        application* (i.e. instead of *Desktop app*) and then add
        `https://oauth2.dance/` as an authorised redirect URI. This is necessary
        for seeing the authorisation code on a page in your browser.

    *   When you download the credentials as JSON, create the directory
        `${XDG_CONFIG_HOME:-${HOME}/.config}/sendgmail` with file mode `0700`
        and save the file to that directory as `config.json` with file mode `0600`.

        Note: the legacy configuration file at `${HOME}/.sendgmail.json` will be
        searched if the directory `${XDG_CONFIG_HOME:-${HOME}/.config}/sendgmail`
        does not exist.

3.  Go back to **APIs & Services > OAuth consent screen** in the Google Cloud
    console.

    *   Add `USERNAME@gmail.com` as a test user. This is necessary for using the
        project that you created.

    *   Add `https://mail.google.com/` as a scope. This is necessary for using
        Gmail via SMTP.

## Installing sendgmail

Run the following command to build and install sendgmail to
`GOPATH/bin/sendgmail`:

```shell
go install github.com/google/gmail-oauth2-tools/go/sendgmail@latest
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

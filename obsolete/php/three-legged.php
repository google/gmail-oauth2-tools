<?php
/**
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
      * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

session_start();

require_once 'common.php';
require_once 'Zend/Oauth/Consumer.php';
require_once 'Zend/Crypt/Rsa/Key/Private.php'; 
require_once 'Zend/Mail/Protocol/Imap.php';
require_once 'Zend/Mail/Storage/Imap.php';

function getCurrentUrl($includeQuery = true) {
  if ($_SERVER['https'] == 'on') {
    $scheme = 'https';
  } else {
    $scheme = 'http';
  }
  $hostname = $_SERVER['SERVER_NAME'];
  $port = $_SERVER['SERVER_PORT'];

  if ($includeQuery) {
    $uri = $_SERVER['REQUEST_URI'];
  } else {
    $uri = $_SERVER['SCRIPT_NAME'];
  }
  if (($port == '80' && $scheme == 'http') ||
      ($port == '443' && $scheme == 'https')) {
      $url = $scheme . '://' . $hostname . $uri;
  } else {
      $url = $scheme . '://' . $hostname . ':' . $port . $uri;
  }
  return $url;
}

/**
 * If the e-mail address was just submitted via a
 * form POST, set it in the session.  Else if we
 * don't yet have an email address, prompt the user
 * for their address.
 */
if (array_key_exists('email_address', $_POST)) {
  $_SESSION['email_address'] = $_POST['email_address'];
  $email_address = $_SESSION['email_address'];
} else if (array_key_exists('email_address', $_SESSION)) {
  $email_address = $_SESSION['email_address'];
} else {
  include 'header.php';
?>
  <h1>Please enter your e-mail address</h1>
  <form method="POST">
    <input type="text" name="email_address" />
    <input type="submit" />
  </form>
<?php
  include 'footer.php';
  exit;
}

/**
 * Setup OAuth
 */
$options = array(
    'requestScheme' => Zend_Oauth::REQUEST_SCHEME_HEADER,
    'version' => '1.0',
    'consumerKey' => $THREE_LEGGED_CONSUMER_KEY,
    'callbackUrl' => getCurrentUrl(),
    'requestTokenUrl' => 'https://www.google.com/accounts/OAuthGetRequestToken',
    'userAuthorizationUrl' => 'https://www.google.com/accounts/OAuthAuthorizeToken',
    'accessTokenUrl' => 'https://www.google.com/accounts/OAuthGetAccessToken'
);

if ($THREE_LEGGED_SIGNATURE_METHOD == 'RSA-SHA1') {
    $options['signatureMethod'] = 'RSA-SHA1';
    $options['consumerSecret'] = new Zend_Crypt_Rsa_Key_Private(
        file_get_contents(realpath($THREE_LEGGED_RSA_PRIVATE_KEY)));
} else {
    $options['signatureMethod'] = 'HMAC-SHA1';
    $options['consumerSecret'] = $THREE_LEGGED_CONSUMER_SECRET_HMAC;
}

$consumer = new Zend_Oauth_Consumer($options);

/**
 * When using HMAC-SHA1, you need to persist the request token in some way.
 * This is because you'll need the request token's token secret when upgrading
 * to an access token later on. The example below saves the token object 
 * as a session variable.
 */
if (!isset($_SESSION['ACCESS_TOKEN'])) {
  if (!isset($_SESSION['REQUEST_TOKEN'])) {
    // Get Request Token and redirect to Google
    $_SESSION['REQUEST_TOKEN'] = serialize($consumer->getRequestToken(array('scope' => implode(' ', $THREE_LEGGED_SCOPES))));
    $consumer->redirect();
  } else {
    // Have Request Token already, Get Access Token
    $_SESSION['ACCESS_TOKEN'] = serialize($consumer->getAccessToken($_GET, unserialize($_SESSION['REQUEST_TOKEN'])));
    header('Location: ' . getCurrentUrl(false));
    exit;
  } 
} else {
  // Retrieve mail using Access Token
  $accessToken = unserialize($_SESSION['ACCESS_TOKEN']);
  $config = new Zend_Oauth_Config();
  $config->setOptions($options);
  $config->setToken($accessToken);
  $config->setRequestMethod('GET');
  $url = 'https://mail.google.com/mail/b/' .
       $email_address . 
       '/imap/';

  $httpUtility = new Zend_Oauth_Http_Utility();
  
  /**
   * Get an unsorted array of oauth params,
   * including the signature based off those params.
   */
  $params = $httpUtility->assembleParams(
      $url, 
      $config);
  
  /**
   * Sort parameters based on their names, as required
   * by OAuth.
   */
  ksort($params);
  
  /**
   * Construct a comma-deliminated,ordered,quoted list of 
   * OAuth params as required by XOAUTH.
   * 
   * Example: oauth_param1="foo",oauth_param2="bar"
   */
  $first = true;
  $oauthParams = '';
  foreach ($params as $key => $value) {
    // only include standard oauth params
    if (strpos($key, 'oauth_') === 0) {
      if (!$first) {
        $oauthParams .= ',';
      }
      $oauthParams .= $key . '="' . urlencode($value) . '"';
      $first = false;
    }
  }
  
  /**
   * Generate SASL client request, using base64 encoded 
   * OAuth params
   */
  $initClientRequest = 'GET ' . $url . ' ' . $oauthParams;
  $initClientRequestEncoded = base64_encode($initClientRequest);
  
  /**
   * Make the IMAP connection and send the auth request
   */
  $imap = new Zend_Mail_Protocol_Imap('imap.gmail.com', '993', true);
  $authenticateParams = array('XOAUTH', $initClientRequestEncoded);
  $imap->requestAndResponse('AUTHENTICATE', $authenticateParams);
  
  /**
   * Print the INBOX message count and the subject of all messages
   * in the INBOX
   */
  $storage = new Zend_Mail_Storage_Imap($imap);
 
  include 'header.php'; 
  echo '<h1>Total messages: ' . $storage->countMessages() . "</h1>\n";

  /**
   * Retrieve first 5 messages.  If retrieving more, you'll want
   * to directly use Zend_Mail_Protocol_Imap and do a batch retrieval,
   * plus retrieve only the headers
   */
  echo 'First five messages: <ul>';
  for ($i = 1; $i <= $storage->countMessages() && $i <= 5; $i++ ){ 
    echo '<li>' . htmlentities($storage->getMessage($i)->subject) . "</li>\n";
  }
  echo '</ul>';
  include 'footer.php'; 
}
?>

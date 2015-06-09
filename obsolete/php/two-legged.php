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

require_once 'common.php';
require_once 'Zend/Oauth.php';
require_once 'Zend/Oauth/Config.php';
require_once 'Zend/Oauth/Token/Access.php';
require_once 'Zend/Mail/Protocol/Imap.php';
require_once 'Zend/Mail/Storage/Imap.php';

/**
 * Setup OAuth
 */
$options = array(
    'requestScheme' => Zend_Oauth::REQUEST_SCHEME_HEADER,
    'version' => '1.0',
    'signatureMethod' => 'HMAC-SHA1',
    'consumerKey' => $TWO_LEGGED_CONSUMER_KEY,
    'consumerSecret' => $TWO_LEGGED_CONSUMER_SECRET_HMAC
);

$config = new Zend_Oauth_Config();
$config->setOptions($options);
$config->setToken(new Zend_Oauth_Token_Access());
$config->setRequestMethod('GET');
$url = 'https://mail.google.com/mail/b/' .
       $TWO_LEGGED_EMAIL_ADDRESS . 
       '/imap/';
$urlWithXoauth = $url . 
                 '?xoauth_requestor_id=' .
                 urlencode($TWO_LEGGED_EMAIL_ADDRESS);

$httpUtility = new Zend_Oauth_Http_Utility();

/**
 * Get an unsorted array of oauth params,
 * including the signature based off those params.
 */
$params = $httpUtility->assembleParams(
    $url, 
    $config, 
    array('xoauth_requestor_id' => $TWO_LEGGED_EMAIL_ADDRESS));

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
$initClientRequest = 'GET ' . $urlWithXoauth . ' ' . $oauthParams;
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
echo '<h1>Total messages: ' . $storage->countMessages() . ' for ' .
    $TWO_LEGGED_EMAIL_ADDRESS .  "</h1>\n";

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

?>

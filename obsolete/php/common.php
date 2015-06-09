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

$TWO_LEGGED_CONSUMER_KEY = 'YOUR_CONSUMER_KEY';
$TWO_LEGGED_CONSUMER_SECRET_HMAC = 'YOUR_CONSUMER_SECRET';
$TWO_LEGGED_EMAIL_ADDRESS = 'YOUR_EMAIL_ADDRESS';

/**
 * Use the following for HMAC, where the Consumer
 * Secret looks like 'xABIch2+jddUraCSlrWmKe'
 */
$THREE_LEGGED_CONSUMER_KEY = 'YOUR_CONSUMER_KEY';
$THREE_LEGGED_SIGNATURE_METHOD = 'HMAC-SHA1';
$THREE_LEGGED_CONSUMER_SECRET_HMAC = 'YOUR_CONSUMER_SECRET';

/** 
 * Alternatively, use the following for RSA, where the
 * RSA public key is uploaded to Google as a X.509 Cert
 */
// $THREE_LEGGED_CONSUMER_KEY = 'YOUR_CONSUMER_KEY';
// $THREE_LEGGED_SIGNATURE_METHOD = 'RSA-SHA1';
// $THREE_LEGGED_RSA_PRIVATE_KEY = 'LOCATION_OF_FILE_WITH_YOUR_RSA_PRIVATE_KEY';

/* No need to modify usually */
$THREE_LEGGED_SCOPES = array('https://mail.google.com/');

/**
 * Add the current directory to the include path
 */
$path = dirname(__FILE__);
set_include_path(get_include_path() . PATH_SEPARATOR . $path);

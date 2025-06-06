/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


/**
 * The localization service that resolves message bundles by directly loading `.properties` files.
 * The locale is simply ignored.
 *
 * @name defaultService
 * @memberOf pentaho.i18n
 * @type {pentaho.i18n.IService}
 * @amd pentaho/i18n
 */
define([
  "./MessageBundle"
], function(MessageBundle) {

  /* globals window */

  return {
    load: function(bundlePath, require, onLoad, config) {

      if(config.isBuild) {
        // Indicate that the optimizer should not wait for this resource and complete optimization.
        // This resource will be resolved dynamically during run time in the web browser.
        onLoad();
      } else {
        var bundleUrl = require.toUrl(bundlePath) + ".properties";
        if(bundleUrl.charAt(0) === ".") {
          // Still a relative path. This causes problems with the text! plugin normalizing it again
          // as a module...
          var baseUrl = window.location.href;
          var index = baseUrl.lastIndexOf("/");
          if(index !== baseUrl.length - 1) {
            // Leave the /
            baseUrl = baseUrl.substring(0, index + 1);
          }

          bundleUrl = baseUrl + bundleUrl;
        }

        require(["text!" + bundleUrl], function(bundleText) {
          onLoad(new MessageBundle(__parseProperties(bundleText)));
        });
      }
    },
    normalize: function(name, normalize) {
      return normalize(__getBundleID(name));
    }
  };

  function __getBundleID(bundlePath) {
    var bundleMid;
    if(!bundlePath) {
      // "pentaho/i18n!"
      // Use the default location and bundle.
      bundleMid = "./i18n/messages";
    } else if(bundlePath[0] === "/") {
      // "pentaho/i18n!/pentaho/common/nls/messages"
      // The path is, directly, an absolute module id of a message bundle (without the /).
      bundleMid = bundlePath.substr(1);
      if(!bundleMid) throw new Error("[pentaho/i18n!] Bundle path argument cannot be a single '/'.");
    } else if(bundlePath[0] !== "." && bundlePath.indexOf("/") < 0) {

      // the name of a bundle in the default "./i18n" sub-module
      bundleMid = "./i18n/" + bundlePath;
    } else {
      // "pentaho/i18n!./nls/information"
      // The path is, directly, a relative module id of a message bundle
      // Or the path has already been resolved by RequireJS.
      bundleMid = bundlePath;
    }

    return bundleMid;
  }

  function __parseProperties(text) {
    // "Brute" parsing.
    var lines = text.split(/[\n\r]+/);
    var props = {};
    lines.forEach(function(line) {
      if(!/^\s*#/.test(line)) {
        var m = /^\s*(.+?)\s*=(.*?)\s*$/.exec(line);
        if(m) {
          props[m[1]] = m[2];
        }
      }
    });
    return props;
  }
});

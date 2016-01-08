
////////////////////////////////////////////////////////////////////////////////
// DCC Dictionary viewer
// Browse and compare ICGC data dictionaries
//
// Note:
// - The viewer does not support dictionary versions before 0.6c, this also means
//   the viewer assumes a fixed column order
// - Comparing A=>B will not yield the same result as B=>A due to how new/remove
//   items are calculated
//
// Dependencies:
// - Core: D3, Underscore
// - Wrapper: angularJS
// - Styles: HighlightJS, JS-Beautify, regex-colorizer JS, Bootstrap
//
////////////////////////////////////////////////////////////////////////////////
'use strict';

var dictionaryApp = dictionaryApp || {};

(function() {

  angular.module('DictionaryViewerApp', [])
    .controller('DictionaryViewerController', function () {


  });
})();
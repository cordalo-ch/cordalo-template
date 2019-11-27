/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

var cordaloEnv = (function () {
    function _GETvar(parameterName) {
        var result = null,
            tmp = [];
        location.search
            .substr(1)
            .split("&")
            .forEach(function (item) {
                tmp = item.split("=");
                if (tmp[0] === parameterName) result = decodeURIComponent(tmp[1]);
            });
        return result;
    };

    function _GetMainUrl() {
        if (port != null) {
            if (local != null) {
                return "http://" + local + ":" + port
            } else {
                return document.location.protocol + "//" + document.location.hostname + ":" + port
            }
        } else {
            return document.location.protocol + "//" + document.location.hostname + ":" + document.location.port
        }
    }

    function init() {
        jQuery.fn.fail = function (f) {
            var o = $(this[0]) // This is the element
            f("missing mock for " + o);
            return this; // This is needed so other functions can keep chaining off of this
        };
        if (local) {
            if (local == "true") {
                local = "localhost"
            }
        }
    }

    var port = _GETvar("port");
    var local = _GETvar("local");
    var mock = _GETvar("mock");
    var MAIN_URL = _GetMainUrl();
    var MOCK_DATA = [];
    var ME = [];
    init();

    return {
        API_URL: function (file) {
            return (file != null) ? MAIN_URL + file : MAIN_URL
        },
        addMock: function (url, data) {
            MOCK_DATA[url] = data;
        },
        setME: function (key, value) {
            ME[key] = value;
        },
        ME: function (key) {
            return ME[key];
        },
        findAPI: function (url) {
            const regex = /http[s]?\:\/\/[^\/]*\:?[0-9]*(\/.*)/gs;
            let m;
            var resultMatch = url;

            while ((m = regex.exec(url)) !== null) {
                // This is necessary to avoid infinite loops with zero-width matches
                if (m.index === regex.lastIndex) {
                    regex.lastIndex++;
                }

                // The result can be accessed through the `m`-variable.
                m.forEach(function(match, groupIndex) {
                    if(match != url) {
                        resultMatch = match;
                        return match;
                    }
                });
            }
            return resultMatch;
        },
        jQuery_get: function (object) {
            if (mock) {
                var api = cordaloEnv.findAPI(object.url);
                if (api && MOCK_DATA[api]) {
                    console.log("successful MOCK_DATA for url " + object.url);
                    object.success(MOCK_DATA[api]);
                } else {
                    console.log("missing MOCK_DATA for url " + object.url);
                }
                return $(API_Failed());
            }
            return $.get(object);
        }
    };
})();


function API_Failed() {
    this.f;
}

API_Failed.prototype.fail = function (f) {
    this.f = f;
}

$get = cordaloEnv.jQuery_get;

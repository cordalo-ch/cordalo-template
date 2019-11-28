/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

if (cordaloEnv) {
    console.log("cordalo connected to " + cordaloEnv.API_URL())
} else {
    console.log("load cordalo.js in your html page");
}

function strongS(i) {
    return (i < 10 ? "<strong>" : "");
}

function strongE(i) {
    return (i < 10 ? "</strong><br>" : "");
}

function makeOptions(id, list, actionText = "Action", onSelectionMethod = "onSelectionChanged") {
    var keys = Object.keys(list);
    if (keys.length > 1) {
        var s = "<select id='" + id + "' onChange='" + onSelectionMethod + "(this)'><option>" + actionText + "</option>";
        Object.entries(list).forEach(function([key, value]) {
            s = s + (key === "self" ? "" : "<br><option value=\"" + value + "\">" + key + "</option>")
            });
        s = s + "</select>";
        return s;
    }
    return "";
}

function X500toOL(x500) {
    if (x500 == null || x500 === "") return "";
    var DNs = x500.split(/[,=]/);
    return DNs[1] + ", " + DNs[3]
}

function X500toO(x500) {
    if (x500 == null || x500 === "") return "";
    var DNs = x500.split(/[,=]/);
    return DNs[1];
}

function price(price) {
    if (price == null) return "";
    return "CHF " + price;
}


function getRandomInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
}

function participantsWithoutMe(list) {
    return list.filter( function(x) { return x != cordaloEnv.ME("X500") });
}

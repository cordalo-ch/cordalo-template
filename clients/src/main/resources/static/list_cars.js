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

function randInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
}

function createNewCar(self, stammNr) {
    animationOn();
    if (!stammNr) {
        stammNr = (100 + randInt(899))+"."+(100+randInt(899))+"."+(100+randInt(899));
    }
    $.ajax(
        {
            url: cordaloEnv.API_URL("/api/v1/cordalo/template/cars/"),
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            data: "make=" + encodeURI("Renault") + "&model=" + encodeURI("Grand Scienic") + "&type=" + encodeURI("PW") + "&stammNr=" + encodeURI(stammNr)
        }
    ).done(function (result) {
    }).fail(function (jqXHR, textStatus) {
        display_error(jqXHR);
    });
}


function searchCar(self, stammNr, from) {
    animationOn();
    if (!stammNr) {
        stammNr = prompt("Please enter a valid stamm number xxx.xxx.xxx?", "");
        if (stammNr != null) {
            alert("no valid stamm Nr available")
            return
        }
    }
    if (!from) {
        display_error("from party cannot be empty");
    }
    $.ajax(
        {
            url: cordaloEnv.API_URL("/api/v1/cordalo/template/cars/search"),
            method: "GET",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            data: "stammNr=" + encodeURI(stammNr) + "&from=" + encodeURI(from)
        }
    ).done(function (result) {
        alert(JSON.parse(result));
    }).fail(function (jqXHR, textStatus) {
        display_error(jqXHR);
    });
}

function show_cars(tagName, result) {
    var i = 0;
    $(tagName).jsGrid({
        height: "auto",
        width: "100%",

        sorting: true,
        paging: false,
        selecting: false,
        filtering: false,
        autoload: true,

        data: result.reverse(),

        fields: [
            {
                title: "StammNr", name: "state.stammNr", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    return strongS(i) + value + strongE(i);
                }
            },
            {
                title: "Model", name: "state", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    return strongS(i) + item.model +" "+item.make + strongE(i);
                }
            },
            {
                title: "Type", name: "state.type", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    return strongS(i) + value + strongE(i);
                }
            },
            {
                title: "Link",
                name: "state.linearId",
                type: "text",
                align: "center",
                width: 25,
                itemTemplate: function (value, item) {
                    var res = "<a target='_blank' href='" + cordaloEnv.API_URL("/api/v1/cordalo/template/cars/" + value.id) + "'>o</a>&nbsp;";
                    i = i + 10;
                    return strongS(i) + res + strongE(i);
                }
            }
        ]
    });
}

function get_cars() {
    $get({
        url: cordaloEnv.API_URL("/api/v1/cordalo/template/messages"),
        data: {},
        success: function (result) {
            show_messages("#cars-template", result);
        }
    });
}



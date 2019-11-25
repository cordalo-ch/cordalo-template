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

function createNewE178(self, stammNr, state) {
    animationOn();
    if (!state) {
        state = "AG";
    }
    var stammNr = prompt("Please enter a valid stamm number xxx.xxx.xxx?", stammNr);
    if (stammNr != null) {
        $.ajax(
            {
                url: cordaloEnv.API_URL("/api/v1/cordalo/template/e178/"),
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                data: "stammNr=" + encodeURI(stammNr) + "&leasing=" + encodeURI("O=Company-B,L=Winterthur,ST=ZH,C=CH") + "&state=" + encodeURI(state)
            }
        ).done(function (result) {
        }).fail(function (jqXHR, textStatus) {
            display_error(jqXHR);
        });
    } else {
        animationOff();
    }
}

function deleteE178(e178) {
    var id = $(e178).attr("value");
    if (confirm('Are you sure to delete this E178 ' + id + '?')) {
        animationOn();
        $.ajax(
            {
                url: cordaloEnv.API_URL("/api/v1/cordalo/template/e178/" + id),
                method: "DELETE",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                data: ""
            }
        ).done(function (result) {
            animationOff();
        }).fail(function (jqXHR, textStatus) {
            display_error(jqXHR);
        });
    }
}


function show_e178(tagName, result) {
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
                title: "Car", name: "state", type: "text", width: 70, itemTemplate: function (value, item) {
                    i = i + 1;
                    return strongS(i) + item.state.stammNr + "<br>" + item.state.state + " ..." + strongE(i);
                }
            },
            {
                title: "Partners", name: "state", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    var x500_O = participantsWithoutMe(item.state.participantsX500).map(x = > X500toO(mapX500(x))
                )
                    ;
                    return strongS(i) + x500_O.join(",<br>") + strongE(i);
                }
            },
            {
                title: "Status", name: "state.status", type: "text", width: 80, itemTemplate: function (value, item) {
                    i = i + 1;
                    return strongS(i) + value.replace("_", " ") + "<br>" + makeOptions(item.state.linearId.id, item.links, "Action", "onE178StatusChange") + strongE(i);
                }
            },
            {
                title: "Link",
                name: "state.linearId",
                type: "text",
                align: "center",
                width: 25,
                itemTemplate: function (value, item) {
                    var res = "<a target='_blank' href='" + cordaloEnv.API_URL("/api/v1/cordalo/template/e178/" + value.id) + "'>o</a>&nbsp;"
                        + "<a value=" + value.id + " href=\"#\" onClick=\"deleteE178(this)\"'>X</a>";
                    i = i + 10;
                    return strongS(i) + res + strongE(i);
                }
            }
        ]
    });
}

function onE178StatusChange(select) {
    if ($(select).val() !== '') {
        var url = $(select).val();
        var action = url.split("/").reverse()[0];
        animationOn();

        var data = "";
        if (action == "REQUEST") {
            data = "retail=" + encodeURI("O=Company-A,L=Zurich,ST=ZH,C=CH") + "&leasing=" + encodeURI("O=Company-B,L=Winterthur,ST=ZH,C=CH") + "&state=" + encodeURI("ZH");
        } else if (action == "ISSUE") {
            data = "regulator=" + encodeURI("O=Company-C,L=Zug,ST=ZG,C=CH");
        } else if (action == "REQUEST_INSURANCE") {
            data = "insurer=" + encodeURI(this.getRandomInsurer());
        } else if (action == "INSURE") {
            data = "";
        } else if (action == "REGISTER") {
            data = "";
        } else if (action == "CANCEL") {
            data = "";
        }

        $.ajax(
            {
                url: url,
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                data: data
            }
        ).done(function (result) {

        }).fail(function (jqXHR, textStatus) {
            display_error(jqXHR);
        });
    }
};

function getRandomInsurer() {
    var peers = ["O=Company-D,L=Geneva,ST=ZH,C=CH", "O=Company-E,L=Uster,ST=ZH,C=CH"];
    return peers[getRandomInt(peers.length)];
}

function get_e178() {
    $get({
        url: cordaloEnv.API_URL("/api/v1/cordalo/template/e178"),
        data: {},
        success: function (result) {
            show_e178("#e178-template", result);
        }
    });
}



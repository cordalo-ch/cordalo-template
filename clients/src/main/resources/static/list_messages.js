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

function createNewMessage(self, message) {
    animationOn();
    if (!message) {
        message = "";
    }
    $.ajax(
        {
            url: cordaloEnv.API_URL("/api/v1/cordalo/template/messages/"),
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            data: "to=" + encodeURI(get_random_peer()) + "&message=" + encodeURI(message)
        }
    ).done(function (result) {
    }).fail(function (jqXHR, textStatus) {
        display_error(jqXHR);
    });
}

function deleteMessage(message) {
    var id = $(message).attr("value");
    if (confirm('Are you sure to delete this message ' + id + '?')) {
        animationOn();
        $.ajax(
            {
                url: cordaloEnv.API_URL("/api/v1/cordalo/template/messages/" + id),
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

function show_messages(tagName, result) {
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
                title: "Chat", name: "state", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    var chat = "";
                    var align = "";
                    if (value.senderX500 == cordaloEnv.ME("X500")) {
                        chat = "-> " + X500toO(value.receiverX500);
                        align = "right";
                    } else {
                        chat = "<- " + X500toO(value.senderX500);
                        align = "left";
                    }
                    var msg = value.message.replace("\n", "<br>");
                    chat = msg + "<br>(" + chat + ")";
                    return "<span style=\"width:100%;text-align:" + align + "\">" + strongS(i) + chat + strongE(i) + "</span>";
                }
            },
            {
                title: "Link",
                name: "state.linearId",
                type: "text",
                align: "center",
                width: 25,
                itemTemplate: function (value, item) {
                    var res = "<a target='_blank' href='" + cordaloEnv.API_URL("/api/v1/cordalo/template/messages/" + value.id) + "'>o</a>&nbsp;"
                        + "<a value=" + value.id + " href=\"#\" onClick=\"deleteMessage(this)\"'>X</a>";
                    i = i + 10;
                    return strongS(i) + res + strongE(i);
                }
            }
        ]
    });
}

function get_messages() {
    $get({
        url: cordaloEnv.API_URL("/api/v1/cordalo/template/messages"),
        data: {},
        success: function (result) {
            show_messages("#messages-template", result);
        }
    });
}



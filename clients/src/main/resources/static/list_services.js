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

function createNewService(self, service, spShort, price) {
    animationOn();
    $.ajax(
        {
            url: cordaloEnv.API_URL("/api/v1/cordalo/template/services/"),
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            data: "service-name=" + encodeURI(service) + "&data=" + encodeURI(getServiceData()) + "&price=" + encodeURI(price)
        }
    ).done(function (result) {
    }).fail(function (jqXHR, textStatus) {
        display_error(jqXHR);
    });
}

function deleteService(service) {
    var id = $(service).attr("value");
    if (confirm('Are you sure to delete service ' + id + '?')) {
        animationOn();
        $.ajax(
            {
                url: cordaloEnv.API_URL("/api/v1/cordalo/template/services/" + id),
                method: "DELETE",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                data: ""
            }
        ).done(function (result) {
            get_services();
            animationOff();
        }).fail(function (jqXHR, textStatus) {
            display_error(jqXHR);
        });
    }
}

function getServiceData() {
    return "{\"test\": \"42\"}";
}


function onServiceSelectionChanged(select) {
    if ($(select).val() !== '') {
        var url = $(select).val();
        var action = url.split("/").reverse()[0];

        var data = action !== "SHARE" ? "" : "service-provider=" + encodeURI(this.get_random_peer());
        animationOn();
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


function show_services(tagName, result) {
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

            /*
              */
            {
                title: "Service", name: "state.serviceName", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    return strongS(i) + item.state.serviceName + "<br>" + price(item.state.price) + strongE(i);
                }
            },
            {
                title: "Partners", name: "state", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    var x500_O = participantsWithoutMe(item.state.participantsX500).map(x = > X500toO(x)
                )
                    ;
                    return strongS(i) + x500_O.join(",") + strongE(i);
                }
            },
            {
                title: "State", name: "state.state", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    return strongS(i) + value + "<br>" + makeOptions(item.state.linearId.id, item.links, "Action", "onServiceSelectionChanged") + strongE(i);
                }
            },
            {
                title: "Link",
                name: "state.linearId",
                type: "text",
                align: "center",
                width: 25,
                itemTemplate: function (value) {
                    var res = "<a target='_blank' href='" + cordaloEnv.API_URL("/api/v1/cordalo/template/services/" + value.id) + "'>o</a>&nbsp;"
                        + "<a value=" + value.id + " href=\"#\" onClick=\"deleteService(this)\"'>X</a>";
                    i = i + 10;
                    return strongS(i) + res + strongE(i);
                }
            }
        ]
    });
}

function get_services() {
    $get({
        url: cordaloEnv.API_URL("/api/v1/cordalo/template/services"),
        data: {},
        success: function (result) {
            show_services("#services-template", result);
        }
    });
}



if (cordaloEnv) {
    console.log("cordalo connected to " + cordaloEnv.API_URL())
} else {
    console.log("load cordalo.js in your html page");
}

function createNewMessage(self, to, message) {
    animationOn();
    $.ajax(
        {
            url: cordaloEnv.API_URL("/api/v1/cordalo/template/messages/"),
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            data: "to=" + encodeURI(receiver) + "&message=" + encodeURI(message)
        }
    ).done(function (result) {
    }).fail(function (jqXHR, textStatus) {
        alert(jqXHR.responseText);
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
            alert(jqXHR.responseText);
        });
    }
}


function onMessageSelectionChanged(select) {
    if ($(select).val() !== '') {
        var url = $(select).val();
        var action = url.split("/").reverse()[0];
        if (action !== "SHARE") {
            animationOn();
            $.ajax(
                {
                    url: url,
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    data: ""
                }
            ).done(function (result) {

            }).fail(function (jqXHR, textStatus) {
                alert(jqXHR.responseText);
            });
        } else {
            animationOn();
            $.ajax(
                {
                    url: url,
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    data: "service-provider=" + encodeURI(this.get_random_peer())
                }
            ).done(function (result) {

            }).fail(function (jqXHR, textStatus) {
                alert(jqXHR.responseText);
            });
        }
    }
};


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

            /*
              */
            {
                title: "Message", name: "state.message", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    return strongS(i) + value + strongE(i);
                }
            },
            {
                title: "From", name: "state", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    var x500_O = X500toO(item.state.senderX500);
                    return strongS(i) + x500_O + strongE(i);
                }
            },
            {
                title: "To", name: "state", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    var x500_O = X500toO(item.state.receiverX500);
                    return strongS(i) + x500_O + strongE(i);
                }
            },
            {
                title: "Link",
                name: "state.id",
                type: "text",
                align: "center",
                width: 30,
                itemTemplate: function (value) {
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



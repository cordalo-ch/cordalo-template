if (cordaloEnv) {
    console.log("cordalo connected to " + cordaloEnv.API_URL())
} else {
    console.log("load cordalo.js in your html page");
}

function createNewE178(self, stammNr, state) {
    animationOn();
    if (!state) {
        state = "";
    }
    $.ajax(
        {
            url: cordaloEnv.API_URL("/api/v1/cordalo/template/e178/"),
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            data: "leasing=" + encodeURI(get_random_peer()) + "&state=" + encodeURI(state)
        }
    ).done(function (result) {
    }).fail(function (jqXHR, textStatus) {
        alert(jqXHR.responseText);
    });
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
            alert(jqXHR.responseText);
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
                title: "Chat", name: "state", type: "text", itemTemplate: function (value, item) {
                    i = i + 1;
                    var chat = "";
                    var align="";
                    if (value.senderX500 == cordaloEnv.ME("X500")) {
                        chat = "-> "+X500toO(value.receiverX500);
                        align="right";
                    } else {
                        chat = "<- " + X500toO(value.senderX500);
                        align="left";
                    }
                    var msg = value.message.replace("\n", "<br>");
                    chat = msg +"<br>("+chat+")";
                    return "<span style=\"width:100%;text-align:"+align+"\">"+strongS(i) + chat + strongE(i)+"</span>";
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

function get_e178() {
    $get({
        url: cordaloEnv.API_URL("/api/v1/cordalo/template/e178"),
        data: {},
        success: function (result) {
            show_e178("#e178-template", result);
        }
    });
}



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

cordaloEnv.addMock(
    "/api/v1/cordalo/template/services",
    [{
        "state": {
            "id": {"externalId": null, "id": "3a3fe715-ef9e-4683-a932-7f67b2afbac0"},
            "state": "SHARED",
            "serviceName": "Tele Medicine",
            "serviceData": {"test": "42"},
            "price": 25,
            "initiatorX500": "O=Company-B,L=Winterthur,ST=ZH,C=CH",
            "serviceProviderX500": "O=Company-D,L=Zurich,ST=ZH,C=CH",
            "linearId": {"externalId": null, "id": "3a3fe715-ef9e-4683-a932-7f67b2afbac0"}
        },
        "links": {
            "UPDATE": "http://localhost:10804/api/v1/cordalo/template/services/3a3fe715-ef9e-4683-a932-7f67b2afbac0/UPDATE",
            "WITHDRAW": "http://localhost:10804/api/v1/cordalo/template/services/3a3fe715-ef9e-4683-a932-7f67b2afbac0/WITHDRAW",
            "SEND_PAYMENT": "http://localhost:10804/api/v1/cordalo/template/services/3a3fe715-ef9e-4683-a932-7f67b2afbac0/SEND_PAYMENT",
            "ACCEPT": "http://localhost:10804/api/v1/cordalo/template/services/3a3fe715-ef9e-4683-a932-7f67b2afbac0/ACCEPT",
            "DECLINE": "http://localhost:10804/api/v1/cordalo/template/services/3a3fe715-ef9e-4683-a932-7f67b2afbac0/DECLINE",
            "self": "http://localhost:10804/api/v1/cordalo/template/services/3a3fe715-ef9e-4683-a932-7f67b2afbac0"
        },
        "error": null
    }, {
        "state": {
            "id": {"externalId": null, "id": "fc6f8a5f-dee3-48ad-a245-594e1b631cc9"},
            "state": "SHARED",
            "serviceName": "New Service",
            "serviceData": {},
            "price": 34,
            "initiatorX500": "O=Company-A,L=Zurich,ST=ZH,C=CH",
            "serviceProviderX500": "O=Company-D,L=Zurich,ST=ZH,C=CH",
            "linearId": {"externalId": null, "id": "fc6f8a5f-dee3-48ad-a245-594e1b631cc9"}
        },
        "links": {
            "UPDATE": "http://localhost:10804/api/v1/cordalo/template/services/fc6f8a5f-dee3-48ad-a245-594e1b631cc9/UPDATE",
            "WITHDRAW": "http://localhost:10804/api/v1/cordalo/template/services/fc6f8a5f-dee3-48ad-a245-594e1b631cc9/WITHDRAW",
            "SEND_PAYMENT": "http://localhost:10804/api/v1/cordalo/template/services/fc6f8a5f-dee3-48ad-a245-594e1b631cc9/SEND_PAYMENT",
            "ACCEPT": "http://localhost:10804/api/v1/cordalo/template/services/fc6f8a5f-dee3-48ad-a245-594e1b631cc9/ACCEPT",
            "DECLINE": "http://localhost:10804/api/v1/cordalo/template/services/fc6f8a5f-dee3-48ad-a245-594e1b631cc9/DECLINE",
            "self": "http://localhost:10804/api/v1/cordalo/template/services/fc6f8a5f-dee3-48ad-a245-594e1b631cc9"
        },
        "error": null
    }]
);

cordaloEnv.addMock(
    "/api/v1/network/me",
    {
        "me": {
            "commonName": null,
            "organisationUnit": null,
            "organisation": "Company-D",
            "locality": "Zurich",
            "state": "ZH",
            "country": "CH",
            "x500Principal": {
                "name": "O=Company-D,L=Zurich,ST=ZH,C=CH",
                "encoded": "ME4xCzAJBgNVBAYTAkNIMQswCQYDVQQIDAJaSDEPMA0GA1UEBwwGWnVyaWNoMSEwHwYDVQQKDBhTd2lzc2NhbnRvIFBlbnNpb25zIEx0ZC4="
            }
        }
    }
);
cordaloEnv.addMock(
    "/api/v1/network/peers",
    {
        "peers": [{
            "commonName": null,
            "organisationUnit": null,
            "organisation": "Company-A",
            "locality": "Zurich",
            "state": "ZH",
            "country": "CH",
            "x500Principal": {
                "name": "O=Company-A,L=Zurich,ST=ZH,C=CH",
                "encoded": "MEUxCzAJBgNVBAYTAkNIMQswCQYDVQQIDAJaSDEPMA0GA1UEBwwGWnVyaWNoMRgwFgYDVQQKDA9Td2lzcyBMaWZlIEx0ZC4="
            }
        }, {
            "commonName": null,
            "organisationUnit": null,
            "organisation": "Company-B",
            "locality": "Winterthur",
            "state": "ZH",
            "country": "CH",
            "x500Principal": {
                "name": "O=Company-B,L=Winterthur,ST=ZH,C=CH",
                "encoded": "MEYxCzAJBgNVBAYTAkNIMQswCQYDVQQIDAJaSDETMBEGA1UEBwwKV2ludGVydGh1cjEVMBMGA1UECgwMQVhBIExlYmVuIEFH"
            }
        }, {
            "commonName": null,
            "organisationUnit": null,
            "organisation": "Company-D",
            "locality": "Zug",
            "state": "ZG",
            "country": "CH",
            "x500Principal": {
                "name": "O=Company-C,L=Zug,ST=ZG,C=CH",
                "encoded": "MDYxCzAJBgNVBAYTAkNIMQswCQYDVQQIDAJaRzEMMAoGA1UEBwwDWnVnMQwwCgYDVQQKDANGWkw="
            }
        }]
    }
);

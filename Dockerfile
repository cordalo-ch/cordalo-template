# Copyright (c) 2019 by cordalo.ch - MIT License
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
# documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
# to whom the Software is furnished to do so, subject to the following conditions:
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
# Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
# WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
# COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
# OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


# Zulu is a fully tested, compatibility verified, and trusted binary distribution of the OpenJDK 8, 7, and 6 platforms.
# used by Corda official docker image
FROM azul/zulu-openjdk:8u232

# net-tools our script require netstat
RUN apt-get update && \
    apt-get -y upgrade && \
    apt-get -y install bash curl unzip git net-tools && \
    git clone https://github.com/cordalo-ch/cordalo-template.git && \
    chmod +x /cordalo-template/scripts/*.sh

ENV WEBSERVER_COMPANY_A=10801 \
    WEBSERVER_COMPANY_B=10802 \
    WEBSERVER_COMPANY_C=10803 \
    WEBSERVER_COMPANY_D=10804 \
    WEBSERVER_COMPANY_E=10805 \
    RPC_NOTARY=10003 \
    RPC_COMPANY_A=10006 \
    RPC_COMPANY_B=10009 \
    RPC_COMPANY_C=10012 \
    RPC_COMPANY_D=10015 \
    RPC_COMPANY_E=10018
    NODE_DEBUG_COMPANY_A=5005 \
    NODE_DEBUG_COMPANY_B=5006 \
    NODE_DEBUG_COMPANY_C=5007 \
    NODE_DEBUG_COMPANY_D=5008 \
    NODE_DEBUG_COMPANY_E=5009 \
    NODE_JOLOKIA_COMPANY_A=7005 \
    NODE_JOLOKIA_COMPANY_B=7006 \
    NODE_JOLOKIA_COMPANY_C=7007 \
    NODE_JOLOKIA_COMPANY_D=7008 \
    NODE_JOLOKIA_COMPANY_E=7009


EXPOSE ${WEBSERVER_COMPANY_A}
EXPOSE ${WEBSERVER_COMPANY_B}
EXPOSE ${WEBSERVER_COMPANY_C}
EXPOSE ${WEBSERVER_COMPANY_D}
EXPOSE ${WEBSERVER_COMPANY_D}

EXPOSE ${RPC_NOTARY}
EXPOSE ${RPC_COMPANY_A}
EXPOSE ${RPC_COMPANY_B}
EXPOSE ${RPC_COMPANY_C}
EXPOSE ${RPC_COMPANY_D}
EXPOSE ${RPC_COMPANY_E}

EXPOSE ${NODE_DEBUG_COMPANY_A}
EXPOSE ${NODE_DEBUG_COMPANY_B}
EXPOSE ${NODE_DEBUG_COMPANY_C}
EXPOSE ${NODE_DEBUG_COMPANY_D}
EXPOSE ${NODE_DEBUG_COMPANY_E}

EXPOSE ${NODE_JOLOKIA_COMPANY_A}
EXPOSE ${NODE_JOLOKIA_COMPANY_B}
EXPOSE ${NODE_JOLOKIA_COMPANY_C}
EXPOSE ${NODE_JOLOKIA_COMPANY_D}
EXPOSE ${NODE_JOLOKIA_COMPANY_E}

WORKDIR /cordalo-template
CMD ["/cordalo-template/scripts/cleanAll.sh"]
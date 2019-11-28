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
FROM ubuntu:latest

# net-tools our script require netstat
RUN apt-get update && \
    apt-get -y upgrade && \
    apt-get -y install bash curl unzip git net-tools openjdk-8-jdk-headless sshpass && \
    git clone https://github.com/cordalo-ch/cordalo-template.git && \
    chmod +x /cordalo-template/scripts/*.sh

#- RPC   servers starts with 10003, increment by 3
#- SSH   servers starts with 10103, increment by 3
#- P2P   servers starts with 10002, increment by 3
#- Admin servers starts with 10043, increment by 3 (not needed in the future by corda)
#- Web   servers starts with 10801, increment by 1
#
# As ENV can be ovverriden at run time: docker run -e WEBSERVER_COMPANY_A=20801
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
    RPC_COMPANY_E=10018 \
    NODE_DEBUG_COMPANY_A=5005 \
    NODE_DEBUG_COMPANY_B=5006 \
    NODE_DEBUG_COMPANY_C=5007 \
    NODE_DEBUG_COMPANY_D=5008 \
    NODE_DEBUG_COMPANY_E=5009 \
    NODE_JOLOKIA_COMPANY_A=7005 \
    NODE_JOLOKIA_COMPANY_B=7006 \
    NODE_JOLOKIA_COMPANY_C=7007 \
    NODE_JOLOKIA_COMPANY_D=7008 \
    NODE_JOLOKIA_COMPANY_E=7009 \
    NODE_H2_COMPANY_A=10050 \
    NODE_H2_COMPANY_B=10054 \
    NODE_H2_COMPANY_C=10058 \
    NODE_H2_COMPANY_D=10062 \
    NODE_H2_COMPANY_E=10066

EXPOSE ${WEBSERVER_COMPANY_A} ${WEBSERVER_COMPANY_B} ${WEBSERVER_COMPANY_C} ${WEBSERVER_COMPANY_D} ${WEBSERVER_COMPANY_E}

EXPOSE ${RPC_NOTARY} ${RPC_COMPANY_A} ${RPC_COMPANY_B} ${RPC_COMPANY_C} ${RPC_COMPANY_D} ${RPC_COMPANY_E}

EXPOSE ${NODE_DEBUG_COMPANY_A} ${NODE_DEBUG_COMPANY_B} ${NODE_DEBUG_COMPANY_C} ${NODE_DEBUG_COMPANY_D} ${NODE_DEBUG_COMPANY_E}

EXPOSE ${NODE_JOLOKIA_COMPANY_A} ${NODE_JOLOKIA_COMPANY_B} ${NODE_JOLOKIA_COMPANY_C} ${NODE_JOLOKIA_COMPANY_D} ${NODE_JOLOKIA_COMPANY_E}

EXPOSE ${NODE_H2_COMPANY_A} ${NODE_H2_COMPANY_B} ${NODE_H2_COMPANY_C} ${NODE_H2_COMPANY_D} ${NODE_H2_COMPANY_E}

WORKDIR /cordalo-template
CMD ["/cordalo-template/scripts/entrypoint.sh"]
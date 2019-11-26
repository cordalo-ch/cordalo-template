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
# used by Corda officcial docker image
FROM azul/zulu-openjdk:8u232

# net-tools our script require netstat
RUN apt-get update && \
    apt-get -y upgrade && \
    apt-get -y install bash curl unzip git net-tools && \
    git clone https://github.com/cordalo-ch/cordalo-template.git && \
    chmod +x /cordalo-template/scripts/*.sh

ENV WEBSERVER1=10801 \
    WEBSERVER2=10802 \
    WEBSERVER3=10803 \
    WEBSERVER4=10804 \
    WEBSERVER5=10805 \
    RPC1=10006 \
    RPC2=10009 \
    RPC3=10012 \
    RPC4=10015 \
    RPC5=10018

EXPOSE ${WEBSERVER1}
EXPOSE ${WEBSERVER2}
EXPOSE ${WEBSERVER3}
EXPOSE ${WEBSERVER4}
EXPOSE ${WEBSERVER5}

EXPOSE ${RPC1}
EXPOSE ${RPC2}
EXPOSE ${RPC3}
EXPOSE ${RPC4}
EXPOSE ${RPC5}

WORKDIR /cordalo-template
CMD ["/cordalo-template/scripts/cleanAll.sh"]
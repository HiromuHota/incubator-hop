# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

FROM tomcat:jdk8
MAINTAINER Hiromu Hota <hiromu.hota@gmail.com>

RUN rm -rf webapps/* \
 && wget -q https://github.com/HiromuHota/incubator-hop/releases/download/hopweb%2Fnightly/hop.war -O webapps/ROOT.war \
 && wget -q https://github.com/HiromuHota/incubator-hop/releases/download/hopweb%2Fnightly/hop-assemblies-plugins-dist-0.60-SNAPSHOT.zip -O hop-assemblies-plugins-dist.zip \
 && unzip -q hop-assemblies-plugins-dist.zip \
 && rm hop-assemblies-plugins-dist.zip \
 && mkdir config \
 && wget -q https://raw.githubusercontent.com/project-hop/hop/master/assemblies/static/src/main/resources/config/hop-config.json -O config/hop-config.json

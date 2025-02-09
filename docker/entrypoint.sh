#!/bin/sh

exec \
  "${JAVA_HOME}/bin/java" \
  ${JAVA_OPTS} \
  -Xshare:off \
  -Dlog4j.configurationFile="${FUSEKI_HOME}/log4j2.properties" \
  -cp "${FUSEKI_HOME}/fuseki-server.jar:${FUSEKI_HOME}/compoundnaming.jar" \
  org.apache.jena.fuseki.main.cmds.FusekiServerCmd
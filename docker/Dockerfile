FROM ghcr.io/kurrawong/fuseki:5.5.0-3

ARG FUSEKI_HOME="/opt/fuseki"
ARG FUSEKI_BASE="/fuseki"

COPY shiro.ini ${FUSEKI_BASE}/shiro.ini
RUN mkdir -p ${FUSEKI_BASE}/configuration
COPY config.ttl /fuseki/configuration/test.ttl
COPY compoundnaming.jar ${FUSEKI_HOME}/lib/compoundnaming.jar

COPY entrypoint.sh ${FUSEKI_HOME}/entrypoint.sh
CMD ["/opt/fuseki/entrypoint.sh"]
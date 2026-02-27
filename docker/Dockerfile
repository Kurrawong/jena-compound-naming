# Temporary local base image built from fuseki-container-image PR #11.
# Switch to a released ghcr.io/kurrawong/fuseki:6.0.0-* tag once available.
FROM kurrawong/fuseki:6.0.0-pr11-local

ARG FUSEKI_HOME=/opt/fuseki
ARG FUSEKI_BASE=/fuseki

COPY shiro.ini ${FUSEKI_BASE}/shiro.ini
RUN mkdir -p ${FUSEKI_BASE}/configuration
COPY config.ttl ${FUSEKI_BASE}/configuration/test.ttl
COPY compoundnaming.jar ${FUSEKI_HOME}/lib/compoundnaming.jar

COPY --chmod=0755 entrypoint.sh ${FUSEKI_HOME}/entrypoint.sh
CMD ["/opt/fuseki/entrypoint.sh"]

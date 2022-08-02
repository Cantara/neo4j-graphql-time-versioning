FROM neo4j:4.4.9

ENV APOC_VERSION 4.4.0.7
ENV APOC_URI https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/download/${APOC_VERSION}/apoc-${APOC_VERSION}-all.jar

RUN wget $APOC_URI && mv apoc-${APOC_VERSION}-all.jar plugins/apoc-${APOC_VERSION}-all.jar

COPY xorcery-tbv-neo4j-plugin/target/xorcery-tbv-neo4j-plugin-0.1-SNAPSHOT.jar plugins/xorcery-tbv-neo4j-plugin-0.1-SNAPSHOT.jar

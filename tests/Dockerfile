ARG MAVEN_VERSION=3-amazoncorretto-8
ARG NEXTFLOW_VERSION=23.10.0

# Download system-rules and groovy-test using Maven
FROM maven:${MAVEN_VERSION} AS builder
COPY config/pom.xml /pom.xml
RUN mvn --batch-mode dependency:copy-dependencies -DoutputDirectory=/bljars

FROM nextflow/nextflow:${NEXTFLOW_VERSION}

COPY --from=builder /bljars /bljars

ARG NEXTFLOW_VERSION
# This should be fixed for a given version
ARG NEXTFLOW_MD5=acbb51bf66024671292c890f7d60ca8b
ENV NXF_LAUNCHER=/.nextflow/tmp/launcher/nextflow-one_${NEXTFLOW_VERSION}/buildkitsandbox

# Replace the Nextflow entrypoint with Groovy
RUN sed \
        -i \
        -e 's/"nextflow.cli.Launcher"/"groovy.ui.GroovyMain"/' \
        -e "s|\"-classpath\" \"|\"-classpath\" \"$(find /bljars/ -not -name 'groovy-3*' -type f -printf "%p:"):|" \
        ${NXF_LAUNCHER}/classpath-${NEXTFLOW_MD5}

COPY validator /usr/local/validator

WORKDIR /mnt/pipeline

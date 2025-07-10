FROM openjdk:17-jdk-slim

RUN apt-get update && \
    apt-get install -y wget unzip git && \
    rm -rf /var/lib/apt/lists/*

ENV GRADLE_VERSION=8.10
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    ln -s /opt/gradle/gradle-${GRADLE_VERSION} /opt/gradle/latest

ENV PATH=$PATH:/opt/gradle/latest/bin

WORKDIR /app
COPY . .

RUN gradle --no-daemon build

CMD ["gradle", "run"]
FROM java:8

RUN mkdir /brn

WORKDIR brn

RUN wget https://services.gradle.org/distributions/gradle-5.6.1-bin.zip -P /tmp

RUN unzip -d /opt/gradle /tmp/gradle-*.zip

ADD . /brn

RUN /opt/gradle/gradle-5.6.1/bin/gradle clean bootJar

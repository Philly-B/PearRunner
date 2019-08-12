FROM openjdk:8

WORKDIR /

RUN mkdir -p pearInstall pearDesc \
	&& apt-get update \
	&& apt-get install curl

COPY *.pear pearDesc/pearToExecute.pear

ARG JAR_FILE
ADD target/${JAR_FILE} executor.jar

EXPOSE 8080

CMD java -jar executor.jar --pearPath=./pearDesc/pearToExecute.pear --pearInstallDir=./pearInstall

FROM java:8

WORKDIR /

RUN mkdir -p pearInstall pearDesc

COPY *.pear pearDesc/pearToExecute.pear

ADD *.jar executor.jar

EXPOSE 8080

CMD java - jar executor.jar --pearPath=./pearDesc/pearToExecute.pear --pearInstallPath=./pearInstall

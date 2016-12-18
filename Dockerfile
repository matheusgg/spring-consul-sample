FROM openjdk:8-jre-alpine
ADD consul-server/consul-linux /consul
RUN sh -c 'chmod 777 /consul'
ADD target/*jar /spring-consul-sample.jar
RUN sh -c 'chmod 777 /spring-consul-sample.jar'
ADD docker-entrypoint.sh /docker-entrypoint.sh
RUN sh -c 'chmod 777 /docker-entrypoint.sh'
EXPOSE 8080 8600 8500 8400 8301 8300
ENV JOIN ''
ENV DC ''
ENV NODE_NAME ${HOSTNAME}
ENV DELAY '0'
ENTRYPOINT ["/docker-entrypoint.sh"]
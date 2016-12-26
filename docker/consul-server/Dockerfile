FROM alpine
RUN apk update
RUN apk -q add curl && \
	apk -q add git && \
	apk -q add nodejs && \
	npm install -g git2consul
ADD docker-entrypoint.sh /docker-entrypoint.sh
RUN sh -c 'chmod 777 /docker-entrypoint.sh'
ADD consul-linux /consul
RUN sh -c 'chmod 777 /consul'
ENV DC ${HOSTNAME}
ENV LOG_LEVEL 'INFO'
ENV NODE_NAME ${HOSTNAME}
ENV BOOTSTRAP_EXPECT 1
ENV ADVERTISE ''
ENV JOIN '[]'
ENV DNS_PORT 8600
ENV HTTP_PORT 8500
ENV RPC_PORT 8400
ENV SERF_LAN_PORT 8301
ENV SERF_WAN_PORT 8302
ENV SERVER_PORT 8300
ENV GIT_INITIAL_REPO ''
ENV GIT_INITIAL_REPO_NAME ''
ENV GIT_INITIAL_REPO_BRANCHES '"master"'
ENV GIT_WEB_HOOK_PORT 5151
EXPOSE ${SERVER_PORT} ${SERF_LAN_PORT} ${SERF_LAN_PORT}/udp ${SERF_WAN_PORT} ${SERF_WAN_PORT}/udp ${RPC_PORT} ${HTTP_PORT} ${DNS_PORT} ${DNS_PORT}/udp ${GIT_WEB_HOOK_PORT}
ENTRYPOINT ["/docker-entrypoint.sh"]
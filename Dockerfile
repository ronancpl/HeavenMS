# Docker support, thanks to xinyifly

FROM openjdk:8u171-jdk-alpine
RUN apk -U add tini
WORKDIR /mnt
COPY ./ ./
RUN sh ./posix-compile.sh
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.6.0/wait /wait
RUN chmod +x /wait

EXPOSE 8484 7575 7576 7577
ENTRYPOINT ["tini", "--"]
CMD /wait && sh ./posix-launch.sh

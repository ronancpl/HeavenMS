# Docker support, thanks to xinyifly

FROM openjdk:7-alpine
RUN apk -U add tini
WORKDIR /mnt
COPY ./ ./
RUN sh ./posix-compile.sh
EXPOSE 8484 7575 7576 7577
CMD exec tini -- sh ./docker-launch.sh
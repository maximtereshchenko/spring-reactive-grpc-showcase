FROM eclipse-temurin:11-jdk AS compile
ARG module
WORKDIR /usr/src/app/
COPY ./ ./
RUN ./gradlew -x test --no-daemon ${module}:installDist

FROM eclipse-temurin:11-jre
ARG module
COPY --from=compile /usr/src/app/${module}/build/install/${module} /usr/app/
WORKDIR usr/app/bin
ENV ENTRYPOINT_SCRIPT=${module}
EXPOSE 8080
ENTRYPOINT ./${ENTRYPOINT_SCRIPT}
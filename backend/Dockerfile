# Build em dois estágios: o primeiro tem JDK + Maven para compilar;
# o segundo leva só o JRE e o .jar, gerando uma imagem final menor.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Copiar primeiro só o que define dependências: enquanto o pom.xml não mudar,
# o Docker reaproveita esta camada e não baixa tudo de novo.
COPY .mvn .mvn
COPY mvnw pom.xml ./
# O sed remove CRLF caso o arquivo tenha vindo de um checkout no Windows.
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q dependency:go-offline

COPY src src
# Testes ficam fora do build da imagem porque precisam do banco;
# eles rodam antes, no "mvnw test" (localmente ou em CI).
RUN ./mvnw -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Não rodar como root dentro do container.
RUN useradd --system --uid 1001 app && mkdir -p /app/uploads && chown app /app/uploads
USER app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

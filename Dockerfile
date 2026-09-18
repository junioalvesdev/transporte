FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre
# O container roda em UTC por padrao, mas o usuario digita data/hora no
# fuso da OVG (Brasilia) e o servidor precisa validar "esta no futuro?"
# no MESMO fuso, senao horarios entre a hora BRT atual e a hora BRT+3
# ficam invalidos por engano (o servidor acha que ja passou).
ENV TZ=America/Sao_Paulo
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# migrations com dados reais da OVG, fora do git (ver .gitignore) — o
# diretorio sempre existe (tem um .gitkeep versionado), entao esse COPY nunca
# falha; num clone novo do repo ele so vem vazio e o Flyway aplica so o
# schema/seed generico de db/migration
COPY db-legado-privado ./db-legado-privado
# copia de seguranca das migrations originais (antes de serem consolidadas em
# V1+V2), usada so pelo docker-compose.local.yml pra nao quebrar o checksum
# do Flyway num banco que ja tinha essas migrations antigas aplicadas
COPY db-migration-historico-completo ./db-migration-historico-completo
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

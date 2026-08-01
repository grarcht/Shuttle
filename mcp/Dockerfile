FROM eclipse-temurin:21-jre-alpine
LABEL io.modelcontextprotocol.server.name="io.github.grarcht/shuttle"
WORKDIR /app
COPY build/libs/shuttle-mcp.jar shuttle-mcp.jar
ENTRYPOINT ["java", "-jar", "shuttle-mcp.jar"]

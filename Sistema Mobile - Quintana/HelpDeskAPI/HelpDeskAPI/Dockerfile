# Build stage
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src

# Copiar csproj e restaurar
COPY ["HelpDeskAPI.csproj", "./"]
RUN dotnet restore "HelpDeskAPI.csproj"

# Copiar código
COPY . .

# Build
RUN dotnet build "HelpDeskAPI.csproj" -c Release -o /app/build

# Publish
FROM build AS publish
RUN dotnet publish "HelpDeskAPI.csproj" -c Release -o /app/publish /p:UseAppHost=false

# Runtime stage
FROM mcr.microsoft.com/dotnet/aspnet:8.0
WORKDIR /app
COPY --from=publish /app/publish .

EXPOSE 5000
ENV ASPNETCORE_URLS=http://+:5000

ENTRYPOINT ["dotnet", "HelpDeskAPI.dll"]
<p align="center">
    <h1 align="center">🌎 Ollin - Seismic Monitoring System</h1>
</p>
<p align="center">
    <em>Real-time seismic monitoring application for Mexico</em>
</p>
<p align="center">
	<img src="https://img.shields.io/github/license/JohannTF/Ollin?style=flat&color=0080ff" alt="license">
	<img src="https://img.shields.io/github/last-commit/JohannTF/Ollin?style=flat&logo=git&logoColor=white&color=0080ff" alt="last-commit">
	<img src="https://img.shields.io/github/languages/top/JohannTF/Ollin?style=flat&color=0080ff" alt="repo-top-language">
	<img src="https://img.shields.io/github/languages/count/JohannTF/Ollin?style=flat&color=0080ff" alt="repo-language-count">
</p>

<p align="center">
	<em>Developed with the software and tools below.</em>
</p>
<p align="center">
	<img src="https://img.shields.io/badge/Spring%20Boot-6DB33F.svg?style=flat&logo=SpringBoot&logoColor=white" alt="Spring Boot">
	<img src="https://img.shields.io/badge/Kotlin-7F52FF.svg?style=flat&logo=Kotlin&logoColor=white" alt="Kotlin">
	<img src="https://img.shields.io/badge/Java-ED8B00.svg?style=flat&logo=openjdk&logoColor=white" alt="Java">
	<img src="https://img.shields.io/badge/PostgreSQL-4169E1.svg?style=flat&logo=PostgreSQL&logoColor=white" alt="PostgreSQL">
	<img src="https://img.shields.io/badge/Redis-DC382D.svg?style=flat&logo=Redis&logoColor=white" alt="Redis">
	<img src="https://img.shields.io/badge/Android-3DDC84.svg?style=flat&logo=Android&logoColor=white" alt="Android">
	<img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4.svg?style=flat&logo=Jetpack-Compose&logoColor=white" alt="Jetpack Compose">
	<img src="https://img.shields.io/badge/Docker-2496ED.svg?style=flat&logo=Docker&logoColor=white" alt="Docker">
	<img src="https://img.shields.io/badge/Google%20Maps-4285F4.svg?style=flat&logo=Google-Maps&logoColor=white" alt="Google Maps">
	<img src="https://img.shields.io/badge/Gradle-02303A.svg?style=flat&logo=Gradle&logoColor=white" alt="Gradle">
</p>
<hr>

## 🚀 Getting Started

### Prerequisites

**Backend:**
- Java 21+
- Docker & Docker Compose
- PostgreSQL 15+

**Frontend:**
- Android Studio
- JDK 17+
- Android SDK 34+

### Backend Setup

1. Clone the repository:
```bash
git clone https://github.com/JohannTF/Ollin.git
cd Ollin/backend
```

2. Create `.env` file:
```env
DB_URL=jdbc:postgresql://localhost:5432/sismosdb
DB_USERNAME=postgres
DB_PASSWORD=your_password
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

3. Start services:
```bash
docker-compose up -d

# Verificar que los contenedores estén corriendo
docker ps
```

4. Create database:
```bash
psql -U postgres
CREATE DATABASE sismosdb;
\q
```

5. Run the backend:
```bash
./gradlew bootRun
```

API will be available at `http://localhost:8080`

### Frontend Setup

1. Open `frontend/` in Android Studio

2. Configure `local.properties`:
```properties
sdk.dir=C\:\\Users\\YourUser\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
BACKEND_URL=http://10.0.2.2:8080/
```

> **Note**: Use `http://10.0.2.2:8080/` for emulator or `http://YOUR_LOCAL_IP:8080/` for physical device

3. Get Google Maps API Key from [Google Cloud Console](https://console.cloud.google.com/)

4. Sync dependencies: **File → Sync Project with Gradle Files**

5. Run the app:
```bash
./gradlew installDebug
```

## 📖 Documentation

- API Docs: http://localhost:8080/swagger-ui/index.html
- Data source: [Servicio Sismológico Nacional](http://www.ssn.unam.mx)

## 📝 License

This project is licensed under the MIT License.

## 🤟 Contributors

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/JohannTF">
        <img src="https://github.com/JohannTF.png" width="100px;" alt="Johann Trejo Flores"/><br />
        <sub><b>Johann Trejo Flores</b></sub>
      </a><br />
    </td>
    <td align="center">
      <a href="https://github.com/LuisGerH">
        <img src="https://github.com/LuisGerH.png" width="100px;" alt="Luis Gerardo"/><br />
        <sub><b>Luis Gerardo</b></sub>
      </a><br />
    </td>
  </tr>
</table>
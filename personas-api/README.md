# personas-api

API REST en NestJS + PostgreSQL (TypeORM) para el formulario de personas de la app Android.

## Ejecutar

```bash
# 1. Base de datos
cd ../personas-db && docker compose up -d

# 2. API
cd ../personas-api
cp .env.example .env   # ajusta credenciales si hace falta
npm install
npm run start:dev      # escucha en http://0.0.0.0:8080
```

## Endpoints

| Método | Ruta                   | Descripción                                   |
|--------|------------------------|-----------------------------------------------|
| GET    | `/personas/:dni`       | Busca una persona (404 si no existe)          |
| POST   | `/personas`            | Crea una persona (409 si el DNI ya existe)    |
| PUT    | `/personas/:dni`       | Actualiza los datos (404 si no existe)        |
| POST   | `/personas/:dni/foto`  | Sube la foto (multipart, campo `foto`)        |
| GET    | `/uploads/:archivo`    | Devuelve una foto subida                      |

Las fotos se guardan en `uploads/` y la tabla `persona` guarda su ruta en `foto_ruta`.

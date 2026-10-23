CREATE TABLE IF NOT EXITS persona (
    dni VARCHAR(8) PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    direccion VARCHAR(200),
    profesion VARCHAR(80),
    celular VARCHAR(15),
    correo VARCHAR(120),
    foto_ruta TEXT
);
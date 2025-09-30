-- =========================================
-- MODELO DE DATOS LÓGICO - Proyecto Grupo 6
-- =========================================

CREATE TABLE Rol (
    id_rol INT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE Usuario (
    id_usuario INT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    contrasena_hash VARCHAR(255) NOT NULL,
    fecha_registro DATE NOT NULL,
    estado ENUM('Activo','Inactivo') DEFAULT 'Activo',
    id_rol INT NOT NULL,
    FOREIGN KEY (id_rol) REFERENCES Rol(id_rol)
);

CREATE TABLE Documento (
    id_documento INT PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    descripcion TEXT,
    fecha_subida DATE NOT NULL,
    ruta_archivo VARCHAR(255) NOT NULL,
    formato VARCHAR(10),
    tamano BIGINT,
    id_usuario_subidor INT NOT NULL,
    FOREIGN KEY (id_usuario_subidor) REFERENCES Usuario(id_usuario)
);

CREATE TABLE CategoriaDocumento (
    id_categoria INT PRIMARY KEY,
    nombre_categoria VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE DocumentoCategoria (
    id_documento INT NOT NULL,
    id_categoria INT NOT NULL,
    PRIMARY KEY (id_documento, id_categoria),
    FOREIGN KEY (id_documento) REFERENCES Documento(id_documento),
    FOREIGN KEY (id_categoria) REFERENCES CategoriaDocumento(id_categoria)
);

CREATE TABLE Comprobante (
    id_comprobante INT PRIMARY KEY,
    tipo_comprobante VARCHAR(50) NOT NULL,
    fecha_emision DATE NOT NULL,
    monto_total DECIMAL(12,2) NOT NULL,
    id_documento INT NOT NULL,
    FOREIGN KEY (id_documento) REFERENCES Documento(id_documento)
);

CREATE TABLE AnalisisComprobante (
    id_analisis INT PRIMARY KEY,
    fecha_analisis DATE NOT NULL,
    resultado TEXT,
    observaciones TEXT,
    id_comprobante INT NOT NULL,
    id_usuario_analista INT NOT NULL,
    FOREIGN KEY (id_comprobante) REFERENCES Comprobante(id_comprobante),
    FOREIGN KEY (id_usuario_analista) REFERENCES Usuario(id_usuario)
);

CREATE TABLE HistorialBusqueda (
    id_historial INT PRIMARY KEY,
    termino_busqueda VARCHAR(255) NOT NULL,
    fecha_hora DATETIME NOT NULL,
    id_usuario INT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);

CREATE TABLE HistorialAccion (
    id_accion INT PRIMARY KEY,
    tipo_accion VARCHAR(50) NOT NULL,
    fecha_hora DATETIME NOT NULL,
    id_usuario INT NOT NULL,
    id_documento INT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_documento) REFERENCES Documento(id_documento)
);
-- SCRIPT DE CREACIÓN DE ESQUEMA Y USUARIOS
CREATE TABLESPACE ts_gestion_salud
DATAFILE 'C:\APP\ORACLE\ORADATA\XE\gestion_salud.dbf'
SIZE 100M
AUTOEXTEND ON NEXT 100M MAXSIZE UNLIMITED;

CREATE USER gestion_salud IDENTIFIED BY "GestionSalud2024!"
DEFAULT TABLESPACE ts_gestion_salud
TEMPORARY TABLESPACE temp;

GRANT CONNECT, RESOURCE TO gestion_salud;

-- Tabla de Roles
CREATE TABLE Rol (
    id_rol NUMBER PRIMARY KEY,
    nombre_rol VARCHAR2(50) NOT NULL UNIQUE,
    descripcion VARCHAR2(255),
    fecha_creacion DATE DEFAULT SYSDATE
);

-- Tabla de Áreas Clínicas
CREATE TABLE area_clinica (
    id_area_clinica NUMBER PRIMARY KEY,
    nombre VARCHAR2(100) NOT NULL,
    fecha_creacion DATE DEFAULT SYSDATE,
    cant_trabajadores NUMBER DEFAULT 0
);

-- Tabla de Usuarios
CREATE TABLE Usuario (
    id_usuario NUMBER PRIMARY KEY,
    id_rol NUMBER NOT NULL,
    id_area_clinica NUMBER,
    dni VARCHAR2(20) UNIQUE NOT NULL,
    nombre VARCHAR2(50) NOT NULL,
    apellido VARCHAR2(50) NOT NULL,
    email VARCHAR2(100) UNIQUE NOT NULL,
    contrasena_hash VARCHAR2(255) NOT NULL,
    telefono VARCHAR2(20),
    fecha_registro DATE DEFAULT SYSDATE,
    estado VARCHAR2(20) DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO', 'BLOQUEADO')),
    FOREIGN KEY (id_rol) REFERENCES Rol(id_rol),
    FOREIGN KEY (id_area_clinica) REFERENCES area_clinica(id_area_clinica)
);

-- Tabla de Pacientes
CREATE TABLE Paciente (
    id_paciente NUMBER PRIMARY KEY,
    id_usuario NUMBER NOT NULL,
    dni VARCHAR2(20) UNIQUE NOT NULL,
    nombre VARCHAR2(50) NOT NULL,
    apellido VARCHAR2(50) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    genero VARCHAR2(10) CHECK (genero IN ('MASCULINO', 'FEMENINO', 'OTRO')),
    direccion VARCHAR2(200),
    telefono VARCHAR2(20),
    email VARCHAR2(100),
    fecha_registro DATE DEFAULT SYSDATE,
    estado VARCHAR2(20) DEFAULT 'ACTIVO',
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);

-- Tabla de Historiales Clínicos
CREATE TABLE HistorialClinico (
    id_historial NUMBER PRIMARY KEY,
    id_paciente NUMBER NOT NULL UNIQUE,
    fecha_creacion DATE DEFAULT SYSDATE,
    alergias VARCHAR2(4000),
    condiciones_cronicas VARCHAR2(4000),
    medicamentos_actuales VARCHAR2(4000),
    observaciones VARCHAR2(4000),
    FOREIGN KEY (id_paciente) REFERENCES Paciente(id_paciente)
);

-- Tabla de Medicamentos
CREATE TABLE Medicamento (
    id_medicamento NUMBER PRIMARY KEY,
    nombre VARCHAR2(100) NOT NULL,
    descripcion VARCHAR2(4000),
    principio_activo VARCHAR2(100),
    concentracion VARCHAR2(50),
    forma_farmaceutica VARCHAR2(50),
    stock_actual NUMBER DEFAULT 0,
    stock_minimo NUMBER DEFAULT 10,
    precio_unitario NUMBER(10,2),
    estado VARCHAR2(20) DEFAULT 'ACTIVO',
    fecha_registro DATE DEFAULT SYSDATE
);

-- Tabla de Lotes de Medicamentos
CREATE TABLE LoteMedicamento (
    id_lote NUMBER PRIMARY KEY,
    id_medicamento NUMBER NOT NULL,
    numero_lote VARCHAR2(100) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    cantidad NUMBER NOT NULL,
    fecha_ingreso DATE DEFAULT SYSDATE,
    tipo_ingreso VARCHAR2(20) CHECK (tipo_ingreso IN ('COMPRA', 'DONACION')),
    estado VARCHAR2(20) DEFAULT 'DISPONIBLE',
    FOREIGN KEY (id_medicamento) REFERENCES Medicamento(id_medicamento)
);

-- Tabla de Consultas Médicas
CREATE TABLE ConsultaMedica (
    id_consulta NUMBER PRIMARY KEY,
    id_paciente NUMBER NOT NULL,
    id_medico NUMBER NOT NULL,
    fecha_consulta DATE DEFAULT SYSDATE,
    motivo_consulta VARCHAR2(4000) NOT NULL,
    sintomas VARCHAR2(4000),
    diagnostico VARCHAR2(4000) NOT NULL,
    tratamiento_prescrito VARCHAR2(4000),
    observaciones VARCHAR2(4000),
    estado VARCHAR2(20) DEFAULT 'COMPLETADA',
    FOREIGN KEY (id_paciente) REFERENCES Paciente(id_paciente),
    FOREIGN KEY (id_medico) REFERENCES Usuario(id_usuario)
);

-- Tabla de Prescripciones
CREATE TABLE Prescripcion (
    id_prescripcion NUMBER PRIMARY KEY,
    id_consulta NUMBER NOT NULL,
    fecha_prescripcion DATE DEFAULT SYSDATE,
    estado VARCHAR2(20) DEFAULT 'PENDIENTE',
    FOREIGN KEY (id_consulta) REFERENCES ConsultaMedica(id_consulta)
);

-- Tabla de Prescripción - Medicamento (Tabla intermedia)
CREATE TABLE prescripcion_has_medicamento (
    id_prescripcion_medicamento NUMBER PRIMARY KEY,
    id_prescripcion NUMBER NOT NULL,
    id_medicamento NUMBER NOT NULL,
    cantidad_prescrita NUMBER NOT NULL,
    dosis VARCHAR2(4000),
    frecuencia VARCHAR2(4000),
    FOREIGN KEY (id_prescripcion) REFERENCES Prescripcion(id_prescripcion),
    FOREIGN KEY (id_medicamento) REFERENCES Medicamento(id_medicamento)
);

-- Tabla de Movimientos de Inventario
CREATE TABLE MovimientoInventario (
    id_movimiento NUMBER PRIMARY KEY,
    id_prescripcion_medicamento NUMBER,
    id_medicamento NUMBER NOT NULL,
    id_lote NUMBER,
    id_usuario NUMBER NOT NULL,
    tipo_movimiento VARCHAR2(20) CHECK (tipo_movimiento IN ('INGRESO', 'SALIDA', 'AJUSTE')),
    cantidad NUMBER NOT NULL,
    fecha_movimiento DATE DEFAULT SYSDATE,
    motivo VARCHAR2(4000),
    referencia VARCHAR2(100),
    FOREIGN KEY (id_prescripcion_medicamento) REFERENCES prescripcion_has_medicamento(id_prescripcion_medicamento),
    FOREIGN KEY (id_medicamento) REFERENCES Medicamento(id_medicamento),
    FOREIGN KEY (id_lote) REFERENCES LoteMedicamento(id_lote),
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);

-- Tabla de Donaciones
CREATE TABLE Donacion (
    id_donacion NUMBER PRIMARY KEY,
    id_usuario NUMBER NOT NULL,
    id_area_destino NUMBER,
    dni_donante VARCHAR2(20),
    tipo_donacion VARCHAR2(20) CHECK (tipo_donacion IN ('MONETARIA', 'MEDICAMENTOS')),
    fecha_donacion DATE DEFAULT SYSDATE,
    monto NUMBER(12,2),
    moneda VARCHAR2(3) CHECK (moneda IN ('PEN', 'USD', 'EUR')) DEFAULT 'PEN',
    descripcion VARCHAR2(4000),
    estado VARCHAR2(20) DEFAULT 'RECIBIDA',
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_area_destino) REFERENCES area_clinica(id_area_clinica)
);

/*-- Tabla de Tipos de Cambio, por modificar
CREATE TABLE TipoCambio (
    id_tipo_cambio NUMBER PRIMARY KEY,
    moneda_origen VARCHAR2(3) CHECK (moneda_origen IN ('PEN', 'USD', 'EUR')) NOT NULL,
    moneda_destino VARCHAR2(3) CHECK (moneda_destino IN ('PEN', 'USD', 'EUR')) NOT NULL,
    tasa_cambio NUMBER(10,4) NOT NULL,
    fecha_vigencia DATE DEFAULT SYSDATE NOT NULL,
    estado VARCHAR2(20) DEFAULT 'ACTIVO',
    CONSTRAINT uk_tipo_cambio_fecha UNIQUE (moneda_origen, moneda_destino, fecha_vigencia),
    CONSTRAINT chk_diferentes_monedas CHECK (moneda_origen != moneda_destino)
);*/

-- Tabla de Gastos
CREATE TABLE Gasto (
    id_gasto NUMBER PRIMARY KEY,
    id_usuario NUMBER NOT NULL,
    id_area_destino NUMBER NOT NULL,
    descripcion VARCHAR2(4000) NOT NULL,
    monto NUMBER(12,2) NOT NULL,
    fecha_gasto DATE NOT NULL,
    estado VARCHAR2(20) DEFAULT 'REGISTRADO',
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_area_destino) REFERENCES area_clinica(id_area_clinica)
);

-- Tabla de Historial de Acciones
CREATE TABLE HistorialAccion (
    id_accion NUMBER PRIMARY KEY,
    tipo_accion VARCHAR2(50) NOT NULL,
    fecha_hora DATE DEFAULT SYSDATE,
    id_usuario NUMBER NOT NULL,
    descripcion VARCHAR2(4000),
    modulo VARCHAR2(50),
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);

-- CREACIÓN DE ÍNDICES
-- Índices para Rol
CREATE INDEX idx_rol_nombre ON Rol(nombre_rol);

-- Índices para Usuario
CREATE INDEX idx_usuario_email ON Usuario(email);
CREATE INDEX idx_usuario_dni ON Usuario(dni);
CREATE INDEX idx_usuario_rol ON Usuario(id_rol);
CREATE INDEX idx_usuario_area ON Usuario(id_area_clinica);
CREATE INDEX idx_usuario_estado ON Usuario(estado);

-- Índices para Paciente
CREATE UNIQUE INDEX idx_paciente_dni ON Paciente(dni);
CREATE INDEX idx_paciente_nombre_apellido ON Paciente(nombre, apellido);
CREATE INDEX idx_paciente_usuario ON Paciente(id_usuario);
CREATE INDEX idx_paciente_estado ON Paciente(estado);

-- Índices para HistorialClinico
CREATE INDEX idx_historial_paciente ON HistorialClinico(id_paciente);

-- Índices para Medicamento
CREATE INDEX idx_medicamento_nombre ON Medicamento(nombre);
CREATE INDEX idx_medicamento_stock ON Medicamento(stock_actual);
CREATE INDEX idx_medicamento_estado ON Medicamento(estado);

-- Índices para LoteMedicamento
CREATE INDEX idx_lote_medicamento ON LoteMedicamento(id_medicamento);
CREATE INDEX idx_lote_vencimiento ON LoteMedicamento(fecha_vencimiento);
CREATE INDEX idx_lote_numero ON LoteMedicamento(numero_lote);
CREATE INDEX idx_lote_estado ON LoteMedicamento(estado);

-- Índices para ConsultaMedica
CREATE INDEX idx_consulta_paciente ON ConsultaMedica(id_paciente);
CREATE INDEX idx_consulta_medico ON ConsultaMedica(id_medico);
CREATE INDEX idx_consulta_fecha ON ConsultaMedica(fecha_consulta);

-- Índices para Prescripcion
CREATE INDEX idx_prescripcion_consulta ON Prescripcion(id_consulta);

-- Índices para prescripcion_has_medicamento
CREATE INDEX idx_phm_prescripcion ON prescripcion_has_medicamento(id_prescripcion);
CREATE INDEX idx_phm_medicamento ON prescripcion_has_medicamento(id_medicamento);

-- Índices para MovimientoInventario
CREATE INDEX idx_movimiento_medicamento ON MovimientoInventario(id_medicamento);
CREATE INDEX idx_movimiento_fecha ON MovimientoInventario(fecha_movimiento);
CREATE INDEX idx_movimiento_tipo ON MovimientoInventario(tipo_movimiento);
CREATE INDEX idx_movimiento_usuario ON MovimientoInventario(id_usuario);

-- Índices para Donacion
CREATE INDEX idx_donacion_usuario ON Donacion(id_usuario);
CREATE INDEX idx_donacion_area ON Donacion(id_area_destino);
CREATE INDEX idx_donacion_fecha ON Donacion(fecha_donacion);

-- Índices para Gasto
CREATE INDEX idx_gasto_usuario ON Gasto(id_usuario);
CREATE INDEX idx_gasto_area ON Gasto(id_area_destino);
CREATE INDEX idx_gasto_fecha ON Gasto(fecha_gasto);

-- Índices para HistorialAccion
CREATE INDEX idx_historial_accion_usuario ON HistorialAccion(id_usuario);
CREATE INDEX idx_historial_accion_fecha ON HistorialAccion(fecha_hora);
CREATE INDEX idx_historial_accion_tipo ON HistorialAccion(tipo_accion);

/*-- Crear índices para la tabla TipoCambio
CREATE INDEX idx_tipo_cambio_monedas ON TipoCambio(moneda_origen, moneda_destino);
CREATE INDEX idx_tipo_cambio_fecha ON TipoCambio(fecha_vigencia);*/

-- CREACIÓN DE SECUENCIAS

CREATE SEQUENCE seq_rol START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_area_clinica START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_usuario START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_paciente START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_historial_clinico START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_medicamento START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_lote_medicamento START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_consulta_medica START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_prescripcion START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_prescripcion_medicamento START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_movimiento_inventario START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_donacion START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_gasto START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_historial_accion START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_tipo_cambio START WITH 1 INCREMENT BY 1;

-- SCRIPT DE CARGA DE DATOS INICIALES
-- Insertar Áreas Clínicas
INSERT INTO area_clinica (id_area_clinica, nombre, cant_trabajadores) VALUES
(seq_area_clinica.NEXTVAL, 'Medicina General', 15);
INSERT INTO area_clinica (id_area_clinica, nombre, cant_trabajadores) VALUES
(seq_area_clinica.NEXTVAL, 'Pediatría', 8);
INSERT INTO area_clinica (id_area_clinica, nombre, cant_trabajadores) VALUES
(seq_area_clinica.NEXTVAL, 'Ginecología', 6);
INSERT INTO area_clinica (id_area_clinica, nombre, cant_trabajadores) VALUES
(seq_area_clinica.NEXTVAL, 'Farmacia', 10);
INSERT INTO area_clinica (id_area_clinica, nombre, cant_trabajadores) VALUES
(seq_area_clinica.NEXTVAL, 'Administración', 12);
INSERT INTO area_clinica (id_area_clinica, nombre, cant_trabajadores) VALUES
(seq_area_clinica.NEXTVAL, 'Logística', 8);

-- Insertar Roles del Sistema
INSERT INTO Rol (id_rol, nombre_rol, descripcion) VALUES
(seq_rol.NEXTVAL, 'ADMINISTRADOR', 'Acceso total al sistema');
INSERT INTO Rol (id_rol, nombre_rol, descripcion) VALUES
(seq_rol.NEXTVAL, 'MEDICO', 'Personal médico autorizado');
INSERT INTO Rol (id_rol, nombre_rol, descripcion) VALUES
(seq_rol.NEXTVAL, 'ENFERMERO', 'Personal de enfermería');
INSERT INTO Rol (id_rol, nombre_rol, descripcion) VALUES
(seq_rol.NEXTVAL, 'FARMACEUTICO', 'Gestión de medicamentos e inventario');
INSERT INTO Rol (id_rol, nombre_rol, descripcion) VALUES
(seq_rol.NEXTVAL, 'ADMINISTRATIVO', 'Personal administrativo');
INSERT INTO Rol (id_rol, nombre_rol, descripcion) VALUES
(seq_rol.NEXTVAL, 'LOGISTICA', 'Gestión de inventario y donaciones');

-- Insertar Usuarios Iniciales
INSERT INTO Usuario (id_usuario, id_rol, id_area_clinica, dni, nombre, apellido, email, contrasena_hash, telefono) VALUES
(seq_usuario.NEXTVAL, 1, 5, '00000001', 'Admin', 'Sistema', 'admin@clinica.com', 'hashed_password_123', '555-0001');
INSERT INTO Usuario (id_usuario, id_rol, id_area_clinica, dni, nombre, apellido, email, contrasena_hash, telefono) VALUES
(seq_usuario.NEXTVAL, 2, 1, '00000002', 'Carlos', 'Mendoza', 'carlos.mendoza@clinica.com', 'hashed_password_456', '555-0002');
INSERT INTO Usuario (id_usuario, id_rol, id_area_clinica, dni, nombre, apellido, email, contrasena_hash, telefono) VALUES
(seq_usuario.NEXTVAL, 2, 2, '00000003', 'Ana', 'Gutierrez', 'ana.gutierrez@clinica.com', 'hashed_password_789', '555-0003');
INSERT INTO Usuario (id_usuario, id_rol, id_area_clinica, dni, nombre, apellido, email, contrasena_hash, telefono) VALUES
(seq_usuario.NEXTVAL, 4, 4, '00000004', 'Pedro', 'Lopez', 'pedro.lopez@clinica.com', 'hashed_password_101', '555-0004');
INSERT INTO Usuario (id_usuario, id_rol, id_area_clinica, dni, nombre, apellido, email, contrasena_hash, telefono) VALUES
(seq_usuario.NEXTVAL, 6, 6, '00000005', 'Maria', 'Rodriguez', 'maria.rodriguez@clinica.com', 'hashed_password_102', '555-0005');

-- Insertar Pacientes de Ejemplo
INSERT INTO Paciente (id_paciente, id_usuario, dni, nombre, apellido, fecha_nacimiento, genero, direccion, telefono) VALUES
(seq_paciente.NEXTVAL, 1, '12345678', 'Juan', 'Perez', TO_DATE('1980-05-15', 'YYYY-MM-DD'), 'MASCULINO', 'Av. Principal 123', '555-0101');
INSERT INTO Paciente (id_paciente, id_usuario, dni, nombre, apellido, fecha_nacimiento, genero, direccion, telefono) VALUES
(seq_paciente.NEXTVAL, 1, '87654321', 'Laura', 'Garcia', TO_DATE('1990-08-22', 'YYYY-MM-DD'), 'FEMENINO', 'Calle Secundaria 456', '555-0102');
INSERT INTO Paciente (id_paciente, id_usuario, dni, nombre, apellido, fecha_nacimiento, genero, direccion, telefono) VALUES
(seq_paciente.NEXTVAL, 1, '11223344', 'Miguel', 'Torres', TO_DATE('1975-12-10', 'YYYY-MM-DD'), 'MASCULINO', 'Jr. Los Olivos 789', '555-0103');
INSERT INTO Paciente (id_paciente, id_usuario, dni, nombre, apellido, fecha_nacimiento, genero, direccion, telefono) VALUES
(seq_paciente.NEXTVAL, 1, '44332211', 'Sofia', 'Martinez', TO_DATE('1988-03-30', 'YYYY-MM-DD'), 'FEMENINO', 'Av. Libertad 321', '555-0104');

-- Insertar Medicamentos Básicos
INSERT INTO Medicamento (id_medicamento, nombre, descripcion, principio_activo, concentracion, stock_actual, stock_minimo, precio_unitario) VALUES
(seq_medicamento.NEXTVAL, 'Paracetamol 500mg', 'Analgésico y antipirético', 'Paracetamol', '500mg', 100, 20, 0.50);
INSERT INTO Medicamento (id_medicamento, nombre, descripcion, principio_activo, concentracion, stock_actual, stock_minimo, precio_unitario) VALUES
(seq_medicamento.NEXTVAL, 'Amoxicilina 500mg', 'Antibiótico de amplio espectro', 'Amoxicilina', '500mg', 50, 15, 1.20);
INSERT INTO Medicamento (id_medicamento, nombre, descripcion, principio_activo, concentracion, stock_actual, stock_minimo, precio_unitario) VALUES
(seq_medicamento.NEXTVAL, 'Ibuprofeno 400mg', 'Antiinflamatorio no esteroideo', 'Ibuprofeno', '400mg', 75, 25, 0.75);
INSERT INTO Medicamento (id_medicamento, nombre, descripcion, principio_activo, concentracion, stock_actual, stock_minimo, precio_unitario) VALUES
(seq_medicamento.NEXTVAL, 'Loratadina 10mg', 'Antihistamínico para alergias', 'Loratadina', '10mg', 60, 10, 0.90);

-- Insertar Lotes de Medicamentos
INSERT INTO LoteMedicamento (id_lote, id_medicamento, numero_lote, fecha_vencimiento, cantidad, tipo_ingreso) VALUES
(seq_lote_medicamento.NEXTVAL, 1, 'LOTE-001-2024', TO_DATE('2025-12-31', 'YYYY-MM-DD'), 50, 'COMPRA');
INSERT INTO LoteMedicamento (id_lote, id_medicamento, numero_lote, fecha_vencimiento, cantidad, tipo_ingreso) VALUES
(seq_lote_medicamento.NEXTVAL, 1, 'LOTE-002-2024', TO_DATE('2025-11-30', 'YYYY-MM-DD'), 50, 'DONACION');
INSERT INTO LoteMedicamento (id_lote, id_medicamento, numero_lote, fecha_vencimiento, cantidad, tipo_ingreso) VALUES
(seq_lote_medicamento.NEXTVAL, 2, 'LOTE-003-2024', TO_DATE('2025-10-31', 'YYYY-MM-DD'), 30, 'COMPRA');
INSERT INTO LoteMedicamento (id_lote, id_medicamento, numero_lote, fecha_vencimiento, cantidad, tipo_ingreso) VALUES
(seq_lote_medicamento.NEXTVAL, 3, 'LOTE-004-2024', TO_DATE('2025-09-30', 'YYYY-MM-DD'), 75, 'COMPRA');

-- Insertar tipos de cambio iniciales
INSERT INTO TipoCambio (id_tipo_cambio, moneda_origen, moneda_destino, tasa_cambio, fecha_vigencia) VALUES
(seq_tipo_cambio.NEXTVAL, 'USD', 'PEN', 3.70, SYSDATE);
INSERT INTO TipoCambio (id_tipo_cambio, moneda_origen, moneda_destino, tasa_cambio, fecha_vigencia) VALUES
(seq_tipo_cambio.NEXTVAL, 'EUR', 'PEN', 4.00, SYSDATE);

COMMIT;

DBMS_OUTPUT.PUT_LINE('Datos iniciales cargados exitosamente');

-- PROCEDIMIENTOS ALMACENADOS Y FUNCIONES

-- Función para verificar existencia de paciente por DNI
CREATE OR REPLACE FUNCTION fn_verificar_paciente_existente(
    p_dni IN VARCHAR2
) RETURN NUMBER
IS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM Paciente
    WHERE dni = p_dni AND estado = 'ACTIVO';
    
    RETURN v_count;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END fn_verificar_paciente_existente;
/

-- Procedimiento para crear historial clínico automáticamente
CREATE OR REPLACE PROCEDURE sp_crear_historial_clinico(
    p_id_paciente IN NUMBER,
    p_id_usuario IN NUMBER
)
IS
    v_historial_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_historial_count
    FROM HistorialClinico
    WHERE id_paciente = p_id_paciente;
    
    IF v_historial_count = 0 THEN
        INSERT INTO HistorialClinico (id_historial, id_paciente)
        VALUES (seq_historial_clinico.NEXTVAL, p_id_paciente);
        
        INSERT INTO HistorialAccion (id_accion, tipo_accion, id_usuario, descripcion, modulo)
        VALUES (seq_historial_accion.NEXTVAL, 'CREACION_HISTORIAL', p_id_usuario, 
                'Creación automática de historial clínico para paciente ID: ' || p_id_paciente, 'HISTORIAL_CLINICO');
        COMMIT;
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_crear_historial_clinico;
/

-- Función para verificar stock mínimo
CREATE OR REPLACE FUNCTION fn_obtener_alertas_stock
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
    SELECT 
        m.id_medicamento,
        m.nombre,
        m.stock_actual,
        m.stock_minimo,
        CASE 
            WHEN m.stock_actual <= m.stock_minimo THEN 'CRITICO'
            WHEN m.stock_actual <= (m.stock_minimo * 1.5) THEN 'BAJO'
            ELSE 'NORMAL'
        END as nivel_alerta
    FROM Medicamento m
    WHERE m.estado = 'ACTIVO'
    ORDER BY nivel_alerta, m.stock_actual ASC;
    
    RETURN v_cursor;
END fn_obtener_alertas_stock;
/

-- Procedimiento para registrar movimiento de inventario
CREATE OR REPLACE PROCEDURE sp_registrar_movimiento_inventario(
    p_id_medicamento IN NUMBER,
    p_id_lote IN NUMBER,
    p_tipo_movimiento IN VARCHAR2,
    p_cantidad IN NUMBER,
    p_motivo IN VARCHAR2,
    p_id_usuario IN NUMBER,
    p_id_prescripcion_medicamento IN NUMBER DEFAULT NULL
)
IS
    v_stock_actual NUMBER;
    v_nuevo_stock NUMBER;
    v_medicamento_nombre VARCHAR2(100);
BEGIN
    SELECT stock_actual, nombre INTO v_stock_actual, v_medicamento_nombre
    FROM Medicamento
    WHERE id_medicamento = p_id_medicamento;
    
    IF p_tipo_movimiento = 'INGRESO' THEN
        v_nuevo_stock := v_stock_actual + p_cantidad;
    ELSIF p_tipo_movimiento = 'SALIDA' THEN
        IF v_stock_actual < p_cantidad THEN
            RAISE_APPLICATION_ERROR(-20001, 'Stock insuficiente para el medicamento: ' || v_medicamento_nombre);
        END IF;
        v_nuevo_stock := v_stock_actual - p_cantidad;
    ELSE
        v_nuevo_stock := p_cantidad;
    END IF;

    INSERT INTO MovimientoInventario (
        id_movimiento, id_prescripcion_medicamento, id_medicamento, id_lote,
        id_usuario, tipo_movimiento, cantidad, motivo
    ) VALUES (
        seq_movimiento_inventario.NEXTVAL, p_id_prescripcion_medicamento, p_id_medicamento, p_id_lote,
        p_id_usuario, p_tipo_movimiento, p_cantidad, p_motivo
    );

    UPDATE Medicamento 
    SET stock_actual = v_nuevo_stock
    WHERE id_medicamento = p_id_medicamento;
    
    COMMIT;
    
    INSERT INTO HistorialAccion (id_accion, tipo_accion, id_usuario, descripcion, modulo)
    VALUES (seq_historial_accion.NEXTVAL, 'MOVIMIENTO_INVENTARIO', p_id_usuario, 
            'Movimiento: ' || p_tipo_movimiento || ' - ' || v_medicamento_nombre || ' - Cant: ' || p_cantidad, 'INVENTARIO');
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_registrar_movimiento_inventario;
/

-- Función para verificar medicamentos vencidos
CREATE OR REPLACE FUNCTION fn_obtener_medicamentos_vencidos
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
    SELECT 
        lm.id_lote,
        m.nombre as medicamento,
        lm.numero_lote,
        lm.fecha_vencimiento,
        lm.cantidad,
        CASE 
            WHEN lm.fecha_vencimiento < SYSDATE THEN 'VENCIDO'
            WHEN lm.fecha_vencimiento <= SYSDATE + 30 THEN 'POR_VENCER'
            ELSE 'VIGENTE'
        END as estado_vencimiento
    FROM LoteMedicamento lm
    JOIN Medicamento m ON lm.id_medicamento = m.id_medicamento
    WHERE lm.estado = 'DISPONIBLE'
    AND (lm.fecha_vencimiento < SYSDATE OR lm.fecha_vencimiento <= SYSDATE + 30)
    ORDER BY lm.fecha_vencimiento ASC;
    
    RETURN v_cursor;
END fn_obtener_medicamentos_vencidos;
/

-- Procedimiento para registrar consulta médica
CREATE OR REPLACE PROCEDURE sp_registrar_consulta_medica(
    p_id_paciente IN NUMBER,
    p_id_medico IN NUMBER,
    p_motivo_consulta IN VARCHAR2,
    p_sintomas IN VARCHAR2,
    p_diagnostico IN VARCHAR2,
    p_tratamiento_prescrito IN VARCHAR2,
    p_observaciones IN VARCHAR2 DEFAULT NULL
)
IS
    v_id_consulta NUMBER;
BEGIN
    sp_crear_historial_clinico(p_id_paciente, p_id_medico);

    IF p_diagnostico IS NULL OR LENGTH(TRIM(p_diagnostico)) = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Es obligatorio registrar un diagnóstico para la consulta médica');
    END IF;
    
    INSERT INTO ConsultaMedica (
        id_consulta, id_paciente, id_medico, motivo_consulta,
        sintomas, diagnostico, tratamiento_prescrito, observaciones
    ) VALUES (
        seq_consulta_medica.NEXTVAL, p_id_paciente, p_id_medico, p_motivo_consulta,
        p_sintomas, p_diagnostico, p_tratamiento_prescrito, p_observaciones
    )
    RETURNING id_consulta INTO v_id_consulta;
    
    COMMIT;
    INSERT INTO HistorialAccion (id_accion, tipo_accion, id_usuario, descripcion, modulo)
    VALUES (seq_historial_accion.NEXTVAL, 'CONSULTA_MEDICA', p_id_medico, 
            'Consulta médica registrada - ID: ' || v_id_consulta, 'CONSULTA');
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_registrar_consulta_medica;
/

-- Procedimiento para procesar prescripción completa
CREATE OR REPLACE PROCEDURE sp_procesar_prescripcion(
    p_id_consulta IN NUMBER,
    p_id_medicamento IN NUMBER,
    p_id_lote IN NUMBER,
    p_cantidad_prescrita IN NUMBER,
    p_dosis IN VARCHAR2,
    p_frecuencia IN VARCHAR2,
    p_id_usuario IN NUMBER
)
IS
    v_id_prescripcion NUMBER;
    v_id_prescripcion_medicamento NUMBER;
    v_fecha_vencimiento DATE;
BEGIN
    IF p_id_lote IS NOT NULL THEN
        SELECT fecha_vencimiento INTO v_fecha_vencimiento
        FROM LoteMedicamento 
        WHERE id_lote = p_id_lote;
        
        IF v_fecha_vencimiento < SYSDATE THEN
            RAISE_APPLICATION_ERROR(-20003, 'No se puede prescribir medicamento vencido');
        END IF;
    END IF;

    INSERT INTO Prescripcion (id_prescripcion, id_consulta)
    VALUES (seq_prescripcion.NEXTVAL, p_id_consulta)
    RETURNING id_prescripcion INTO v_id_prescripcion;

    INSERT INTO prescripcion_has_medicamento (
        id_prescripcion_medicamento, id_prescripcion, id_medicamento, id_lote,
        cantidad_prescrita, dosis, frecuencia, estado
    ) VALUES (
        seq_prescripcion_medicamento.NEXTVAL, v_id_prescripcion, p_id_medicamento, p_id_lote,
        p_cantidad_prescrita, p_dosis, p_frecuencia, 'APROBADA'
    )
    RETURNING id_prescripcion_medicamento INTO v_id_prescripcion_medicamento;

    sp_registrar_movimiento_inventario(
        p_id_medicamento, p_id_lote, 'SALIDA', p_cantidad_prescrita,
        'Prescripción médica ID: ' || v_id_prescripcion, p_id_usuario, v_id_prescripcion_medicamento
    );

    INSERT INTO HistorialAccion (id_accion, tipo_accion, id_usuario, descripcion, modulo)
    VALUES (seq_historial_accion.NEXTVAL, 'PRESCRIPCION', p_id_usuario, 
            'Prescripción procesada - ID: ' || v_id_prescripcion, 'FARMACIA');
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_procesar_prescripcion;
/

-- Procedimiento para registrar donación
CREATE OR REPLACE PROCEDURE sp_registrar_donacion(
    p_id_usuario IN NUMBER,
    p_id_area_destino IN NUMBER,
    p_dni_donante IN VARCHAR2,
    p_tipo_donacion IN VARCHAR2,
    p_monto IN NUMBER DEFAULT NULL,
    p_moneda IN VARCHAR2 DEFAULT 'USD',
    p_descripcion IN VARCHAR2
)
IS
    v_id_donacion NUMBER;
BEGIN
    INSERT INTO Donacion (
        id_donacion, id_usuario, id_area_destino, dni_donante,
        tipo_donacion, monto, moneda, descripcion
    ) VALUES (
        seq_donacion.NEXTVAL, p_id_usuario, p_id_area_destino, p_dni_donante,
        p_tipo_donacion, p_monto, p_moneda, p_descripcion
    )
    RETURNING id_donacion INTO v_id_donacion;

    INSERT INTO HistorialAccion (id_accion, tipo_accion, id_usuario, descripcion, modulo)
    VALUES (seq_historial_accion.NEXTVAL, 'DONACION', p_id_usuario, 
            'Donación registrada - ID: ' || v_id_donacion, 'DONACIONES');
    
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_registrar_donacion;
/

-- Función para reporte de donantes
CREATE OR REPLACE FUNCTION fn_generar_reporte_donante(
    p_dni_donante IN VARCHAR2
) RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
    SELECT 
        d.id_donacion,
        d.tipo_donacion,
        d.fecha_donacion,
        d.monto,
        d.moneda,
        d.descripcion,
        d.estado,
        ac.nombre as area_destino,
        u.nombre || ' ' || u.apellido as registrado_por
    FROM Donacion d
    JOIN area_clinica ac ON d.id_area_destino = ac.id_area_clinica
    JOIN Usuario u ON d.id_usuario = u.id_usuario
    WHERE d.dni_donante = p_dni_donante
    ORDER BY d.fecha_donacion DESC;
    
    RETURN v_cursor;
END fn_generar_reporte_donante;
/

-- Procedimiento para auditoría de cambios críticos
CREATE OR REPLACE PROCEDURE sp_registrar_auditoria(
    p_tipo_accion IN VARCHAR2,
    p_id_usuario IN NUMBER,
    p_descripcion IN VARCHAR2,
    p_modulo IN VARCHAR2
)
IS
BEGIN
    INSERT INTO HistorialAccion (
        id_accion, tipo_accion, id_usuario, descripcion, modulo
    ) VALUES (
        seq_historial_accion.NEXTVAL, p_tipo_accion, p_id_usuario, p_descripcion, p_modulo
    );
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        NULL;
END sp_registrar_auditoria;
/


-- Procedimiento para registrar gastos
CREATE OR REPLACE PROCEDURE sp_registrar_gasto(
    p_id_usuario IN NUMBER,
    p_id_area_destino IN NUMBER,
    p_descripcion IN VARCHAR2,
    p_monto IN NUMBER,
    p_fecha_gasto IN DATE DEFAULT SYSDATE
)
IS
    v_id_gasto NUMBER;
BEGIN
    INSERT INTO Gasto (id_gasto, id_usuario, id_area_destino, descripcion, monto, fecha_gasto)
    VALUES (seq_gasto.NEXTVAL, p_id_usuario, p_id_area_destino, p_descripcion, p_monto, p_fecha_gasto)
    RETURNING id_gasto INTO v_id_gasto;

    sp_registrar_auditoria(
        'GASTO_REGISTRADO', p_id_usuario,
        'Gasto registrado - ID: ' || v_id_gasto || ' - Monto: ' || p_monto || ' - Área: ' || p_id_area_destino,
        'FINANZAS'
    );
    
    COMMIT;
END sp_registrar_gasto;
/

-- Función para reporte de gastos por área
CREATE OR REPLACE FUNCTION fn_obtener_gastos_por_area(
    p_id_area IN NUMBER DEFAULT NULL
) RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
    SELECT 
        g.id_gasto,
        g.descripcion,
        g.monto,
        g.fecha_gasto,
        g.estado,
        ac.nombre as area_destino,
        u.nombre || ' ' || u.apellido as registrado_por
    FROM Gasto g
    JOIN area_clinica ac ON g.id_area_destino = ac.id_area_clinica
    JOIN Usuario u ON g.id_usuario = u.id_usuario
    WHERE (p_id_area IS NULL OR g.id_area_destino = p_id_area)
    ORDER BY g.fecha_gasto DESC;
    
    RETURN v_cursor;
END fn_obtener_gastos_por_area;
/

-- VISTAS PARA REPORTES
-- Vista para inventario completo
CREATE OR REPLACE VIEW vw_inventario_completo AS
SELECT 
    m.id_medicamento,
    m.nombre,
    m.descripcion,
    m.principio_activo,
    m.concentracion,
    m.stock_actual,
    m.stock_minimo,
    CASE 
        WHEN m.stock_actual <= m.stock_minimo THEN 'CRITICO'
        WHEN m.stock_actual <= (m.stock_minimo * 1.5) THEN 'BAJO'
        ELSE 'NORMAL'
    END as nivel_stock,
    m.precio_unitario,
    (SELECT COUNT(*) FROM LoteMedicamento lm 
     WHERE lm.id_medicamento = m.id_medicamento AND lm.estado = 'DISPONIBLE') as lotes_activos,
    (SELECT MIN(fecha_vencimiento) FROM LoteMedicamento lm 
     WHERE lm.id_medicamento = m.id_medicamento AND lm.estado = 'DISPONIBLE') as proximo_vencimiento
FROM Medicamento m
WHERE m.estado = 'ACTIVO';

-- Vista para consultas médicas
CREATE OR REPLACE VIEW vw_consultas_medicas AS
SELECT 
    cm.id_consulta,
    p.dni as paciente_dni,
    p.nombre || ' ' || p.apellido as paciente_nombre,
    u.nombre || ' ' || u.apellido as medico_nombre,
    ac.nombre as area_medica,
    cm.fecha_consulta,
    cm.motivo_consulta,
    cm.diagnostico,
    cm.tratamiento_prescrito,
    cm.estado
FROM ConsultaMedica cm
JOIN Paciente p ON cm.id_paciente = p.id_paciente
JOIN Usuario u ON cm.id_medico = u.id_usuario
JOIN area_clinica ac ON u.id_area_clinica = ac.id_area_clinica
WHERE p.estado = 'ACTIVO'
AND u.estado = 'ACTIVO';

-- Vista para prescripciones activas
CREATE OR REPLACE VIEW vw_prescripciones_activas AS
SELECT 
    p.id_prescripcion,
    cm.id_consulta,
    pa.nombre || ' ' || pa.apellido as paciente_nombre,
    me.nombre || ' ' || me.apellido as medico_nombre,
    m.nombre as medicamento,
    phm.cantidad_prescrita,
    phm.dosis,
    phm.frecuencia,
    phm.estado,
    p.fecha_prescripcion
FROM Prescripcion p
JOIN ConsultaMedica cm ON p.id_consulta = cm.id_consulta
JOIN Paciente pa ON cm.id_paciente = pa.id_paciente
JOIN Usuario me ON cm.id_medico = me.id_usuario
JOIN prescripcion_has_medicamento phm ON p.id_prescripcion = phm.id_prescripcion
JOIN Medicamento m ON phm.id_medicamento = m.id_medicamento
WHERE phm.estado = 'APROBADA';

/*-- Función para obtener tipo de cambio actual
CREATE OR REPLACE FUNCTION fn_obtener_tipo_cambio(
    p_moneda_origen IN VARCHAR2,
    p_moneda_destino IN VARCHAR2,
    p_fecha IN DATE DEFAULT SYSDATE
) RETURN NUMBER
IS
    v_tasa_cambio NUMBER;
BEGIN
    BEGIN
        SELECT tasa_cambio INTO v_tasa_cambio
        FROM TipoCambio
        WHERE moneda_origen = p_moneda_origen
        AND moneda_destino = p_moneda_destino
        AND fecha_vigencia = (
            SELECT MAX(fecha_vigencia)
            FROM TipoCambio
            WHERE moneda_origen = p_moneda_origen
            AND moneda_destino = p_moneda_destino
            AND fecha_vigencia <= p_fecha
            AND estado = 'ACTIVO'
        );
        
        RETURN v_tasa_cambio;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            BEGIN
                SELECT 1/tasa_cambio INTO v_tasa_cambio
                FROM TipoCambio
                WHERE moneda_origen = p_moneda_destino
                AND moneda_destino = p_moneda_origen
                AND fecha_vigencia = (
                    SELECT MAX(fecha_vigencia)
                    FROM TipoCambio
                    WHERE moneda_origen = p_moneda_destino
                    AND moneda_destino = p_moneda_origen
                    AND fecha_vigencia <= p_fecha
                    AND estado = 'ACTIVO'
                );
                
                RETURN v_tasa_cambio;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    RAISE_APPLICATION_ERROR(-20100, 'No se encontró tipo de cambio para la conversión solicitada');
            END;
    END;
END fn_obtener_tipo_cambio;
/

-- Función para convertir monto entre monedas
CREATE OR REPLACE FUNCTION fn_convertir_monto(
    p_monto IN NUMBER,
    p_moneda_origen IN VARCHAR2,
    p_moneda_destino IN VARCHAR2,
    p_fecha IN DATE DEFAULT SYSDATE
) RETURN NUMBER
IS
    v_tasa_cambio NUMBER;
BEGIN
    IF p_moneda_origen = p_moneda_destino THEN
        RETURN p_monto;
    END IF;
    
    v_tasa_cambio := fn_obtener_tipo_cambio(p_moneda_origen, p_moneda_destino, p_fecha);
    RETURN ROUND(p_monto * v_tasa_cambio, 2);
EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20101, 'Error al convertir monto: ' || SQLERRM);
END fn_convertir_monto;
/

-- Procedimiento para registrar tipo de cambio
CREATE OR REPLACE PROCEDURE sp_registrar_tipo_cambio(
    p_moneda_origen IN VARCHAR2,
    p_moneda_destino IN VARCHAR2,
    p_tasa_cambio IN NUMBER,
    p_fecha_vigencia IN DATE DEFAULT SYSDATE
)
IS
BEGIN
    IF p_moneda_origen = p_moneda_destino THEN
        RAISE_APPLICATION_ERROR(-20102, 'La moneda origen y destino no pueden ser iguales');
    END IF;

    IF p_tasa_cambio <= 0 THEN
        RAISE_APPLICATION_ERROR(-20103, 'La tasa de cambio debe ser mayor que cero');
    END IF;

    INSERT INTO TipoCambio (
        id_tipo_cambio,
        moneda_origen,
        moneda_destino,
        tasa_cambio,
        fecha_vigencia
    ) VALUES (
        seq_tipo_cambio.NEXTVAL,
        p_moneda_origen,
        p_moneda_destino,
        p_tasa_cambio,
        p_fecha_vigencia
    );
    
    COMMIT;
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        RAISE_APPLICATION_ERROR(-20104, 'Ya existe un tipo de cambio para estas monedas en la fecha especificada');
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sp_registrar_tipo_cambio;
/*/
-- TRIGGERS PARA REGLAS DE NEGOCIO
-- Trigger para verificar medicamentos vencidos en prescripciones
CREATE OR REPLACE TRIGGER trg_verificar_medicamento_vencido
    BEFORE INSERT OR UPDATE ON prescripcion_has_medicamento
    FOR EACH ROW
DECLARE
    v_fecha_vencimiento DATE;
BEGIN
    IF :NEW.id_lote IS NOT NULL THEN
        SELECT fecha_vencimiento INTO v_fecha_vencimiento
        FROM LoteMedicamento
        WHERE id_lote = :NEW.id_lote;
        
        IF v_fecha_vencimiento < SYSDATE THEN
            RAISE_APPLICATION_ERROR(-20002, 'No se puede prescribir un medicamento vencido. Lote ID: ' || :NEW.id_lote);
        END IF;
    END IF;
END;
/

-- Trigger para auditoría de cambios en historiales clínicos
CREATE OR REPLACE TRIGGER trg_auditoria_historial_clinico
    AFTER UPDATE OR DELETE ON HistorialClinico
    FOR EACH ROW
DECLARE
    v_operacion VARCHAR2(10);
BEGIN
    IF UPDATING THEN
        v_operacion := 'UPDATE';
        sp_registrar_auditoria(
            'AUDITORIA_UPDATE', 
            :NEW.id_usuario_creador,
            'Historial clínico actualizado - ID: ' || :OLD.id_historial ||
            ' | Paciente ID: ' || :OLD.id_paciente,
            'HISTORIAL_CLINICO'
        );
    ELSIF DELETING THEN
        v_operacion := 'DELETE';
        sp_registrar_auditoria(
            'AUDITORIA_DELETE', 
            USER,
            'Historial clínico eliminado - ID: ' || :OLD.id_historial ||
            ' | Paciente ID: ' || :OLD.id_paciente,
            'HISTORIAL_CLINICO'
        );
    END IF;
END;
/

-- Trigger para actualizar stock automáticamente
CREATE OR REPLACE TRIGGER trg_actualizar_stock_medicamento
    AFTER INSERT ON MovimientoInventario
    FOR EACH ROW
DECLARE
    v_stock_actual NUMBER;
BEGIN
    IF :NEW.tipo_movimiento = 'INGRESO' THEN
        UPDATE Medicamento 
        SET stock_actual = stock_actual + :NEW.cantidad
        WHERE id_medicamento = :NEW.id_medicamento;
    ELSIF :NEW.tipo_movimiento = 'SALIDA' THEN
        UPDATE Medicamento 
        SET stock_actual = stock_actual - :NEW.cantidad
        WHERE id_medicamento = :NEW.id_medicamento;
    END IF;
END;
/

COMMIT;

DBMS_OUTPUT.PUT_LINE('Objetos de programación creados exitosamente');
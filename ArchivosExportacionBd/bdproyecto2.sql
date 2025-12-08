-- ================= TABLAS =================
CREATE TABLE rol (
    id_rol NUMBER(10) PRIMARY KEY,
    nombre_rol VARCHAR2(50) NOT NULL UNIQUE,
    descripcion VARCHAR2(255)
);

CREATE TABLE usuario (
    id_usuario NUMBER(10) PRIMARY KEY,
    nombre VARCHAR2(50) NOT NULL,
    apellido VARCHAR2(50) NOT NULL,
    email VARCHAR2(100) NOT NULL UNIQUE,
    contrasena_hash VARCHAR2(255) NOT NULL,
    fecha_registro DATE DEFAULT SYSDATE,
    estado VARCHAR2(20) DEFAULT 'ACTIVO',
    id_rol NUMBER(10) NOT NULL,
    dni VARCHAR2(20) UNIQUE,
    telefono VARCHAR2(20),
    direccion VARCHAR2(200),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

CREATE TABLE paciente (
    id_paciente NUMBER(10) PRIMARY KEY,
    dni VARCHAR2(20) NOT NULL UNIQUE,
    nombre VARCHAR2(50) NOT NULL,
    apellido VARCHAR2(50) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    genero VARCHAR2(20),
    telefono VARCHAR2(20),
    direccion VARCHAR2(200),
    email VARCHAR2(100),
    fecha_registro DATE DEFAULT SYSDATE,
    estado VARCHAR2(20) DEFAULT 'ACTIVO',
    id_usuario_registro NUMBER(10),
    CONSTRAINT fk_paciente_usuario FOREIGN KEY (id_usuario_registro) REFERENCES usuario(id_usuario)
);

CREATE TABLE historial_clinico (
    id_historial NUMBER(10) PRIMARY KEY,
    id_paciente NUMBER(10) NOT NULL UNIQUE,
    fecha_creacion DATE DEFAULT SYSDATE,
    alergias CLOB,
    condiciones_cronicas CLOB,
    medicamentos_actuales CLOB,
    observaciones_generales CLOB,
    CONSTRAINT fk_historial_paciente FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente)
);

CREATE TABLE consulta_medica (
    id_consulta NUMBER(10) PRIMARY KEY,
    id_paciente NUMBER(10) NOT NULL,
    id_medico NUMBER(10) NOT NULL,
    fecha_consulta TIMESTAMP DEFAULT SYSTIMESTAMP,
    motivo_consulta CLOB NOT NULL,
    sintomas CLOB,
    diagnostico CLOB NOT NULL,
    tratamiento_prescrito CLOB,
    observaciones CLOB,
    estado VARCHAR2(20) DEFAULT 'EN_PROCESO',
    CONSTRAINT fk_consulta_paciente FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente),
    CONSTRAINT fk_consulta_medico FOREIGN KEY (id_medico) REFERENCES usuario(id_usuario)
);

CREATE TABLE medicamento (
    id_medicamento NUMBER(10) PRIMARY KEY,
    nombre VARCHAR2(100) NOT NULL,
    descripcion CLOB,
    principio_activo VARCHAR2(100),
    concentracion VARCHAR2(50),
    forma_farmaceutica VARCHAR2(50),
    stock_actual NUMBER(10) DEFAULT 0,
    stock_minimo NUMBER(10) DEFAULT 10,
    precio_unitario NUMBER(10,2),
    estado VARCHAR2(20) DEFAULT 'ACTIVO',
    fecha_registro DATE DEFAULT SYSDATE
);


CREATE TABLE area_clinica (
    id_area_clinica NUMBER(10) PRIMARY KEY,
    nombre VARCHAR2(100) NOT NULL,
    fecha_creacion DATE DEFAULT SYSDATE,
    cant_trabajadores NUMBER(5),
    descripcion VARCHAR2(255)
);

CREATE TABLE donacion (
    id_donacion NUMBER(10) PRIMARY KEY,
    dni_donante VARCHAR2(45),
    nombre_donante VARCHAR2(100),
    tipo_donacion VARCHAR2(20),
    fecha_donacion DATE DEFAULT SYSDATE,
    monto NUMBER(12,2),
    moneda VARCHAR2(10) DEFAULT 'USD',
    descripcion CLOB,
    estado VARCHAR2(20) DEFAULT 'RECIBIDA',
    id_usuario_registro NUMBER(10) NOT NULL,
    id_area_destino NUMBER(10) NOT NULL,
    CONSTRAINT fk_don_usu FOREIGN KEY (id_usuario_registro) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_don_area FOREIGN KEY (id_area_destino) REFERENCES area_clinica(id_area_clinica)
);



CREATE TABLE historial_accion (
    id_accion NUMBER(10) PRIMARY KEY,
    tipo_accion VARCHAR2(50) NOT NULL,
    fecha_hora TIMESTAMP DEFAULT SYSTIMESTAMP,
    id_usuario NUMBER(10) NOT NULL,
    descripcion CLOB,
    modulo VARCHAR2(50),
    ip_address VARCHAR2(45),
    CONSTRAINT fk_hist_usu FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- ================= SECUENCIAS =================
CREATE SEQUENCE seq_rol START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_usuario START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_paciente START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_historial START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_consulta START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_medicamento START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_area START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_donacion START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_accion START WITH 1 INCREMENT BY 1 NOCACHE;

-- ================= PAQUETES PL/SQL =================
-- Nota: Ejecutar cada bloque CREATE PACKAGE / BODY por separado en su IDE si es necesario.

-- 1. AUTENTICACION
-- Utilidad: Centraliza la logica de inicio de sesion, protegiendo la tabla de usuarios de consultas directas y asegurando que las reglas de acceso (como verificar si el usuario esta activo) se cumplan siempre.
-- Funcionamiento:
-- sp_validar_login: Recibe un correo y el hash de la contrasena. Busca en la tabla de usuarios si existe esa combinacion exacta. Si la encuentra y el usuario tiene estado 'ACTIVO', devuelve un resultado exitoso (1) junto con el ID, nombre y rol del usuario. Si no, devuelve error.
-- sp_registrar_login: Se llama tras un intento de acceso para insertar un registro en historial_accion, documentando quien intento entrar, cuando y desde que IP, cumpliendo con requisitos de auditoria.

CREATE OR REPLACE PACKAGE PKG_AUTENTICACION AS
    PROCEDURE sp_validar_login(p_email IN VARCHAR2, p_password_hash IN VARCHAR2, p_resultado OUT NUMBER, p_id_usuario OUT NUMBER, p_nombre_completo OUT VARCHAR2, p_rol OUT VARCHAR2);
    PROCEDURE sp_registrar_login(p_email IN VARCHAR2, p_exitoso IN NUMBER, p_ip_address IN VARCHAR2);
END PKG_AUTENTICACION;
/
CREATE OR REPLACE PACKAGE BODY PKG_AUTENTICACION AS
    PROCEDURE sp_validar_login(p_email IN VARCHAR2, p_password_hash IN VARCHAR2, p_resultado OUT NUMBER, p_id_usuario OUT NUMBER, p_nombre_completo OUT VARCHAR2, p_rol OUT VARCHAR2) IS
        v_estado VARCHAR2(20);
    BEGIN
        SELECT id_usuario, nombre || ' ' || apellido, r.nombre_rol, u.estado
        INTO p_id_usuario, p_nombre_completo, p_rol, v_estado
        FROM usuario u JOIN rol r ON u.id_rol = r.id_rol
        WHERE u.email = p_email AND u.contrasena_hash = p_password_hash;
        IF v_estado != 'ACTIVO' THEN p_resultado := -1; ELSE p_resultado := 1; END IF;
    EXCEPTION WHEN NO_DATA_FOUND THEN p_resultado := 0;
    END sp_validar_login;

    PROCEDURE sp_registrar_login(p_email IN VARCHAR2, p_exitoso IN NUMBER, p_ip_address IN VARCHAR2) IS
        v_id NUMBER;
    BEGIN
        SELECT id_usuario INTO v_id FROM usuario WHERE email = p_email;
        INSERT INTO historial_accion VALUES (seq_accion.NEXTVAL, 'LOGIN', SYSTIMESTAMP, v_id, 'Intento login: ' || p_exitoso, 'AUTH', p_ip_address);
        COMMIT;
    EXCEPTION WHEN OTHERS THEN NULL;
    END sp_registrar_login;
END PKG_AUTENTICACION;
/

-- 2. PACIENTES
-- Utilidad: Permite al personal de Caja y Medicos registrar nuevos pacientes y consultar el padron existente sin exponer la estructura interna de las tablas.
-- Funcionamiento:
-- sp_registrar_paciente: Recibe todos los datos personales (DNI, nombre, etc.), inserta el registro en la tabla paciente y devuelve el ID generado. Nota: Aunque el trigger crea el historial clinico, este procedimiento orquesta la transaccion principal.
-- sp_listar_pacientes: Devuelve un cursor (una lista) con todos los pacientes que tienen estado 'ACTIVO', ordenados alfabeticamente, optimizando la carga de la tabla en Java.
-- sp_buscar_paciente_dni: Realiza una busqueda exacta por DNI para recuperar los datos de un paciente especifico rapidamente.

CREATE OR REPLACE PACKAGE PKG_PACIENTES AS
    PROCEDURE sp_registrar_paciente(p_dni IN VARCHAR2, p_nombre IN VARCHAR2, p_apellido IN VARCHAR2, p_fecha_nac IN DATE, p_genero IN VARCHAR2, p_tel IN VARCHAR2, p_dir IN VARCHAR2, p_email IN VARCHAR2, p_id_usu IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2);
    PROCEDURE sp_listar_pacientes(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE sp_buscar_paciente_dni(p_dni IN VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE sp_eliminar_paciente(p_id_paciente IN NUMBER, p_id_usuario IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2);
    PROCEDURE sp_listar_inactivos(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE sp_reactivar_paciente(p_id_paciente IN NUMBER, p_id_usuario IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2);
    PROCEDURE sp_actualizar_historial(p_id_historial IN NUMBER,p_alergias IN CLOB,p_condiciones IN CLOB,p_medicamentos IN CLOB,p_observaciones IN CLOB,p_res OUT NUMBER,p_msg OUT VARCHAR2);
END PKG_PACIENTES;
/

-- 2. ACTUALIZAR PAQUETE PACIENTES (Body)
CREATE OR REPLACE PACKAGE BODY PKG_PACIENTES AS
    -- Procedimiento REGISTRAR mejorado
     PROCEDURE sp_registrar_paciente(p_dni IN VARCHAR2, p_nombre IN VARCHAR2, p_apellido IN VARCHAR2, p_fecha_nac IN DATE, p_genero IN VARCHAR2, p_tel IN VARCHAR2, p_dir IN VARCHAR2, p_email IN VARCHAR2, p_id_usu IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2) IS
        v_id NUMBER;
        v_count NUMBER;
    BEGIN
        -- 1. Validar DNI
        SELECT COUNT(*) INTO v_count FROM paciente WHERE dni = p_dni;
        IF v_count > 0 THEN
            p_res := 0;
            p_msg := 'El DNI ya se encuentra registrado.';
            RETURN;
        END IF;

        -- 2. Insertar Paciente
        -- Al hacer este insert, el TRIGGER 'trg_crear_historial_automatico' se dispara automáticamente
        INSERT INTO paciente (id_paciente, dni, nombre, apellido, fecha_nacimiento, genero, telefono, direccion, email, id_usuario_registro)
        VALUES (seq_paciente.NEXTVAL, p_dni, p_nombre, p_apellido, p_fecha_nac, p_genero, p_tel, p_dir, p_email, p_id_usu)
        RETURNING id_paciente INTO v_id;

        COMMIT; 
        p_res := 1; 
        p_msg := 'Paciente registrado con ID: ' || v_id;

    EXCEPTION 
        WHEN DUP_VAL_ON_INDEX THEN 
            ROLLBACK;
            p_res := 0; 
            p_msg := 'Error: Datos duplicados (DNI o ID).';
        WHEN OTHERS THEN 
            ROLLBACK; 
            p_res := -1; 
            p_msg := 'Error desconocido: ' || SQLERRM;
    END sp_registrar_paciente;


    PROCEDURE sp_listar_pacientes(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN OPEN p_cursor FOR SELECT * FROM paciente WHERE estado = 'ACTIVO' ORDER BY apellido; END;

    PROCEDURE sp_buscar_paciente_dni(p_dni IN VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN OPEN p_cursor FOR SELECT * FROM paciente WHERE dni = p_dni; END;

    -- NUEVO: Eliminar Paciente (Lógico)
    PROCEDURE sp_eliminar_paciente(p_id_paciente IN NUMBER, p_id_usuario IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2) IS
    BEGIN
        UPDATE paciente SET estado = 'INACTIVO' WHERE id_paciente = p_id_paciente;
        
        -- Auditoría
        INSERT INTO historial_accion (id_accion, tipo_accion, id_usuario, descripcion, modulo)
        VALUES (seq_accion.NEXTVAL, 'ELIMINAR_PACIENTE', p_id_usuario, 'Paciente ID ' || p_id_paciente || ' marcado como INACTIVO', 'PACIENTES');
        
        COMMIT;
        p_res := 1;
        p_msg := 'Paciente eliminado exitosamente.';
    EXCEPTION WHEN OTHERS THEN
        ROLLBACK;
        p_res := -1;
        p_msg := SQLERRM;
    END sp_eliminar_paciente;

    PROCEDURE sp_listar_inactivos(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT * FROM paciente WHERE estado = 'INACTIVO' ORDER BY apellido;
    END;

    PROCEDURE sp_reactivar_paciente(p_id_paciente IN NUMBER, p_id_usuario IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2) IS
    BEGIN
        UPDATE paciente SET estado = 'ACTIVO' WHERE id_paciente = p_id_paciente;
        
        INSERT INTO historial_accion (id_accion, tipo_accion, id_usuario, descripcion, modulo)
        VALUES (seq_accion.NEXTVAL, 'REACTIVAR_PACIENTE', p_id_usuario, 'Paciente ID ' || p_id_paciente || ' reactivado.', 'PACIENTES');
        
        COMMIT;
        p_res := 1;
        p_msg := 'Paciente reactivado exitosamente.';
    EXCEPTION WHEN OTHERS THEN
        ROLLBACK;
        p_res := -1;
        p_msg := SQLERRM;
    END;
    
    PROCEDURE sp_actualizar_historial(
        p_id_historial IN NUMBER,
        p_alergias IN CLOB,
        p_condiciones IN CLOB,
        p_medicamentos IN CLOB,
        p_observaciones IN CLOB,
        p_res OUT NUMBER,
        p_msg OUT VARCHAR2
    ) IS
    BEGIN
        UPDATE historial_clinico 
        SET alergias = p_alergias,
            condiciones_cronicas = p_condiciones,
            medicamentos_actuales = p_medicamentos,
            observaciones_generales = p_observaciones
        WHERE id_historial = p_id_historial;
        
        -- Nota: El trigger 'trg_versionar_historial_clinico' se disparará automáticamente 
        -- para guardar la auditoría de los datos anteriores.
        
        COMMIT;
        p_res := 1;
        p_msg := 'Historial clínico actualizado correctamente.';
    EXCEPTION WHEN OTHERS THEN
        ROLLBACK;
        p_res := -1;
        p_msg := 'Error al actualizar: ' || SQLERRM;
    END sp_actualizar_historial;
    
END PKG_PACIENTES;
/

-- 3. MEDICAMENTOS
-- Utilidad: Proporciona a los roles de Farmacia, Logistica y Medicos las herramientas para mantener el catalogo actualizado y detectar necesidades de reabastecimiento.
-- Funcionamiento:
-- sp_registrar_medicamento: Inserta un nuevo producto en el catalogo maestro con sus caracteristicas (principio activo, concentracion) y establece sus parametros de stock minimo y precio.
-- sp_buscar_medicamentos: Permite buscar productos por nombre, filtrando coincidencias parciales (LIKE) y asegurando que solo se muestren medicamentos 'ACTIVOS'.
-- sp_stock_bajo: Ejecuta una consulta critica que filtra y devuelve todos los medicamentos cuyo stock_actual es menor o igual al stock_minimo, ordenados por urgencia, facilitando la toma de decisiones de compra.
-- CABECERA
CREATE OR REPLACE PACKAGE PKG_MEDICAMENTOS AS
    PROCEDURE sp_registrar_medicamento(p_nom IN VARCHAR2, p_desc IN CLOB, p_pa IN VARCHAR2, p_conc IN VARCHAR2, p_forma IN VARCHAR2, p_min IN NUMBER, p_prec IN NUMBER, p_id_usu IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2);
    PROCEDURE sp_buscar_medicamentos(p_crit IN VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE sp_stock_bajo(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE sp_eliminar_medicamento(p_id_med IN NUMBER, p_id_usu IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2);
    PROCEDURE sp_listar_inactivos(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE sp_reactivar_medicamento(p_id_med IN NUMBER, p_id_usu IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2);
END PKG_MEDICAMENTOS;
/

-- CUERPO
CREATE OR REPLACE PACKAGE BODY PKG_MEDICAMENTOS AS
    PROCEDURE sp_registrar_medicamento(p_nom IN VARCHAR2, p_desc IN CLOB, p_pa IN VARCHAR2, p_conc IN VARCHAR2, p_forma IN VARCHAR2, p_min IN NUMBER, p_prec IN NUMBER, p_id_usu IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2) IS
    BEGIN
        INSERT INTO medicamento (id_medicamento, nombre, descripcion, principio_activo, concentracion, forma_farmaceutica, stock_minimo, precio_unitario)
        VALUES (seq_medicamento.NEXTVAL, p_nom, p_desc, p_pa, p_conc, p_forma, p_min, p_prec);
        COMMIT; p_res := 1; p_msg := 'Medicamento registrado';
    EXCEPTION WHEN OTHERS THEN p_res := -1; p_msg := SQLERRM;
    END;

    PROCEDURE sp_buscar_medicamentos(p_crit IN VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT * FROM medicamento WHERE UPPER(nombre) LIKE '%'||UPPER(p_crit)||'%' AND estado='ACTIVO';
    END;

    PROCEDURE sp_stock_bajo(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT * FROM medicamento WHERE stock_actual <= stock_minimo AND estado='ACTIVO' ORDER BY stock_actual ASC;
    END;

    -- NUEVO: Eliminar Medicamento
    PROCEDURE sp_eliminar_medicamento(p_id_med IN NUMBER, p_id_usu IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2) IS
    BEGIN
        UPDATE medicamento SET estado = 'INACTIVO' WHERE id_medicamento = p_id_med;
        INSERT INTO historial_accion VALUES (seq_accion.NEXTVAL, 'ELIMINAR_MEDICAMENTO', SYSTIMESTAMP, p_id_usu, 'Medicamento ID ' || p_id_med || ' desactivado', 'FARMACIA', NULL);
        COMMIT;
        p_res := 1;
        p_msg := 'Medicamento eliminado.';
    EXCEPTION WHEN OTHERS THEN
        ROLLBACK;
        p_res := -1;
        p_msg := SQLERRM;
    END sp_eliminar_medicamento;
    
    PROCEDURE sp_listar_inactivos(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT * FROM medicamento WHERE estado = 'INACTIVO' ORDER BY nombre;
    END sp_listar_inactivos;

    PROCEDURE sp_reactivar_medicamento(p_id_med IN NUMBER, p_id_usu IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2) IS
    BEGIN
        UPDATE medicamento SET estado = 'ACTIVO' WHERE id_medicamento = p_id_med;
        
        INSERT INTO historial_accion (id_accion, tipo_accion, id_usuario, descripcion, modulo)
        VALUES (seq_accion.NEXTVAL, 'REACTIVAR_MEDICAMENTO', p_id_usu, 'Medicamento ID ' || p_id_med || ' reactivado.', 'FARMACIA');
        
        COMMIT;
        p_res := 1;
        p_msg := 'Medicamento reactivado.';
    EXCEPTION WHEN OTHERS THEN
        ROLLBACK;
        p_res := -1;
        p_msg := SQLERRM;
    END sp_reactivar_medicamento;
END PKG_MEDICAMENTOS;
/

-- 4. CONSULTAS
-- Utilidad: Estandariza como se registran los diagnosticos y tratamientos, asegurando que cada atencion quede vinculada correctamente al historial del paciente y al medico responsable.
-- Funcionamiento:
-- sp_registrar_consulta: Recibe los datos de la atencion (motivo, sintomas, diagnostico, receta). Inserta el registro en consulta_medica asignandole automaticamente el estado 'EN_PROCESO' y la fecha actual.
-- sp_listar_consultas_paciente: Recupera el historial completo de atenciones de un paciente especifico, uniendo la tabla de consultas con la de usuarios para devolver tambien el nombre del medico que lo atendio, ordenado cronologicamente.

CREATE OR REPLACE PACKAGE PKG_CONSULTAS AS
    PROCEDURE sp_registrar_consulta(p_id_pac IN NUMBER, p_id_med IN NUMBER, p_motivo IN CLOB, p_sint IN CLOB, p_diag IN CLOB, p_trat IN CLOB, p_obs IN CLOB, p_res OUT NUMBER, p_id_cons OUT NUMBER, p_msg OUT VARCHAR2);
    PROCEDURE sp_listar_consultas_paciente(p_id_pac IN NUMBER, p_cursor OUT SYS_REFCURSOR);
    -- NUEVO
    PROCEDURE sp_eliminar_consulta(p_id_cons IN NUMBER, p_id_usu IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2);
END PKG_CONSULTAS;
/

-- CUERPO
CREATE OR REPLACE PACKAGE BODY PKG_CONSULTAS AS
    PROCEDURE sp_registrar_consulta(p_id_pac IN NUMBER, p_id_med IN NUMBER, p_motivo IN CLOB, p_sint IN CLOB, p_diag IN CLOB, p_trat IN CLOB, p_obs IN CLOB, p_res OUT NUMBER, p_id_cons OUT NUMBER, p_msg OUT VARCHAR2) IS
    BEGIN
        INSERT INTO consulta_medica (id_consulta, id_paciente, id_medico, motivo_consulta, sintomas, diagnostico, tratamiento_prescrito, observaciones, estado)
        VALUES (seq_consulta.NEXTVAL, p_id_pac, p_id_med, p_motivo, p_sint, p_diag, p_trat, p_obs, 'EN_PROCESO')
        RETURNING id_consulta INTO p_id_cons;
        COMMIT; p_res := 1; p_msg := 'Consulta registrada';
    EXCEPTION WHEN OTHERS THEN ROLLBACK; p_res := -1; p_msg := SQLERRM;
    END;

    PROCEDURE sp_listar_consultas_paciente(p_id_pac IN NUMBER, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR 
        SELECT c.*, u.nombre || ' ' || u.apellido as nombre_medico 
        FROM consulta_medica c JOIN usuario u ON c.id_medico = u.id_usuario 
        WHERE c.id_paciente = p_id_pac AND c.estado != 'ELIMINADA' ORDER BY fecha_consulta DESC;
    END;

    -- NUEVO: Eliminar Consulta (Cambiar estado a ELIMINADA o CANCELADA)
    PROCEDURE sp_eliminar_consulta(p_id_cons IN NUMBER, p_id_usu IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2) IS
    BEGIN
        UPDATE consulta_medica SET estado = 'CANCELADA' WHERE id_consulta = p_id_cons;
        INSERT INTO historial_accion VALUES (seq_accion.NEXTVAL, 'CANCELAR_CONSULTA', SYSTIMESTAMP, p_id_usu, 'Consulta ID ' || p_id_cons || ' cancelada', 'CONSULTAS', NULL);
        COMMIT;
        p_res := 1;
        p_msg := 'Consulta cancelada exitosamente.';
    EXCEPTION WHEN OTHERS THEN
        ROLLBACK;
        p_res := -1;
        p_msg := SQLERRM;
    END sp_eliminar_consulta;
END PKG_CONSULTAS;
/

-- 5. DONACIONES
-- Utilidad: Permite al rol de Finanzas documentar el origen de los fondos y recursos, facilitando la transparencia y la generacion de reportes economicos.
-- Funcionamiento:
-- sp_registrar_donacion: Inserta un registro detallado de la donacion, incluyendo el donante, el tipo (monetaria/insumos), el monto, la moneda y el area de destino. Garantiza que la fecha ingresada sea respetada.
-- sp_listar_donaciones: Genera un reporte dinamico basado en un rango de fechas (inicio y fin), devolviendo todas las donaciones registradas en ese periodo, incluyendo el nombre del area beneficiada mediante un JOIN.

CREATE OR REPLACE PACKAGE PKG_DONACIONES AS
    PROCEDURE sp_registrar_donacion(p_dni IN VARCHAR2, p_nom IN VARCHAR2, p_tipo IN VARCHAR2, p_monto IN NUMBER, p_moneda IN VARCHAR2, p_desc IN CLOB, p_id_area IN NUMBER, p_id_usu IN NUMBER, p_res OUT NUMBER, p_msg   OUT VARCHAR2);
    PROCEDURE sp_listar_donaciones(p_inicio IN DATE, p_fin IN DATE, p_cursor OUT SYS_REFCURSOR);
END PKG_DONACIONES;
/
CREATE OR REPLACE PACKAGE BODY PKG_DONACIONES AS
    PROCEDURE sp_registrar_donacion(p_dni IN VARCHAR2, p_nom IN VARCHAR2, p_tipo IN VARCHAR2, p_monto IN NUMBER, p_moneda IN VARCHAR2, p_desc IN CLOB, p_id_area IN NUMBER, p_id_usu IN NUMBER, p_res OUT NUMBER, p_msg OUT VARCHAR2) IS
    BEGIN
        INSERT INTO donacion (id_donacion, dni_donante, nombre_donante, tipo_donacion, monto, moneda, descripcion, id_area_destino, id_usuario_registro)
        VALUES (seq_donacion.NEXTVAL, p_dni, p_nom, p_tipo, p_monto, p_moneda, p_desc, p_id_area, p_id_usu);
        COMMIT; p_res := 1; p_msg := 'Donación registrada';
    EXCEPTION WHEN OTHERS THEN p_res := -1; p_msg := SQLERRM;
    END;

    PROCEDURE sp_listar_donaciones(p_inicio IN DATE, p_fin IN DATE, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR 
        SELECT d.*, a.nombre as area_destino FROM donacion d JOIN area_clinica a ON d.id_area_destino = a.id_area_clinica 
        WHERE fecha_donacion BETWEEN p_inicio AND p_fin ORDER BY fecha_donacion DESC;
    END;
END PKG_DONACIONES;
/

-- ================= TRIGGERS DE INTEGRIDAD Y AUDITORÍA =================
-- TRIGGER PARA EVITAR DUPLICADOS DE PACIENTES
-- Utilidad y Funcionamiento: Su objetivo es garantizar la regla de Paciente único (RN-001). Antes de guardar un nuevo paciente, verifica si ya existe un registro con el estado       'ACTIVO' y el mismo DNI. Si encuentra duplicidad, bloquea la transacción lanzando un error, asegurando que cada persona tenga una identidad única en el sistema.
CREATE OR REPLACE TRIGGER trg_evitar_duplicado_paciente
    BEFORE INSERT ON paciente
    FOR EACH ROW
DECLARE
    v_count NUMBER;
BEGIN
    -- Verificar si ya existe un paciente activo con el mismo DNI
    SELECT COUNT(*) INTO v_count
    FROM paciente
    WHERE dni = :NEW.dni AND estado = 'ACTIVO';
    
    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 
            'Ya existe un paciente activo con el DNI: ' || :NEW.dni || 
            '. Regla de negocio RN-001: Paciente único.');
    END IF;
END trg_evitar_duplicado_paciente;
/

--TRIGGER PARA CREACIÓN AUTOMÁTICA DE HISTORIAL CLÍNICO
-- Utilidad y Funcionamiento: Cumple con la regla de Historial clínico obligatorio (RN-002). Inmediatamente después de registrar un paciente, este trigger crea automáticamente su     registro correspondiente en la tabla historial_clinico. Esto evita errores humanos de olvidar crear la ficha médica y garantiza que el sistema esté listo para la primera      atención.
CREATE OR REPLACE TRIGGER trg_crear_historial_automatico
    AFTER INSERT ON paciente
    FOR EACH ROW
BEGIN
    INSERT INTO historial_clinico (id_historial, id_paciente, fecha_creacion)
    VALUES (seq_historial.NEXTVAL, :NEW.id_paciente, SYSDATE);
    
    -- Registrar la acción en el historial
    INSERT INTO historial_accion (id_accion, tipo_accion, id_usuario, descripcion, modulo)
    VALUES (seq_accion.NEXTVAL, 'CREACION_HISTORIAL', :NEW.id_usuario_registro, 
            'Creación automática de historial clínico para paciente: ' || :NEW.nombre || ' ' || :NEW.apellido, 
            'PACIENTES');
    
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        NULL; -- No bloquear la inserción del paciente si falla el historial
END trg_crear_historial_automatico;
/

-- TRIGGER PARA VALIDAR DIAGNÓSTICO EN CONSULTAS
-- Utilidad y Funcionamiento: Hace cumplir la regla de Registro de diagnóstico (RN-003). Inspecciona que el campo de diagnóstico no sea nulo ni esté vacío antes de guardar la         consulta. Si no hay diagnóstico, rechaza la operación, asegurando que toda atención médica registrada sea válida y útil para el historial.
CREATE OR REPLACE TRIGGER trg_validar_diagnostico_consulta
    BEFORE INSERT OR UPDATE ON consulta_medica
    FOR EACH ROW
BEGIN
    IF :NEW.diagnostico IS NULL OR TRIM(:NEW.diagnostico) IS NULL THEN
        RAISE_APPLICATION_ERROR(-20002, 
            'Es obligatorio registrar un diagnóstico para la consulta médica. ' ||
            'Regla de negocio RN-003: Registro de diagnóstico obligatorio.');
    END IF;
END trg_validar_diagnostico_consulta;
/

-- TRIGGER PARA ALERTAS DE STOCK MÍNIMO
-- Utilidad y Funcionamiento: Implementa la regla de Stock Mínimo (RN-006). Cuando el stock de un medicamento baja a su nivel crítico tras una actualización, el trigger detecta       este cambio y genera automáticamente una alerta en la tabla historial_accion. Esto permite al área de logística reaccionar proactivamente ante el desabastecimiento.
CREATE OR REPLACE TRIGGER trg_alerta_stock_minimo
    AFTER UPDATE OF stock_actual ON medicamento
    FOR EACH ROW
    WHEN (NEW.stock_actual <= NEW.stock_minimo AND NEW.estado = 'ACTIVO')
DECLARE
    v_admin_id NUMBER;
BEGIN
    -- Buscar un usuario administrador para notificar
    BEGIN
        SELECT id_usuario INTO v_admin_id 
        FROM usuario 
        WHERE id_rol = (SELECT id_rol FROM rol WHERE nombre_rol = 'ADMINISTRADOR') 
        AND ROWNUM = 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_admin_id := 1; -- Default al primer usuario
    END;
    
    -- Registrar alerta en historial de acciones
    INSERT INTO historial_accion (id_accion, tipo_accion, id_usuario, descripcion, modulo)
    VALUES (seq_accion.NEXTVAL, 'ALERTA_STOCK', v_admin_id,
            'ALERTA: Medicamento "' || :NEW.nombre || 
            '" alcanzó stock mínimo. Stock actual: ' || :NEW.stock_actual || 
            ', Mínimo requerido: ' || :NEW.stock_minimo,
            'INVENTARIO');
    
    COMMIT;
END trg_alerta_stock_minimo;
/


-- TRIGGER PARA VALIDAR ESTADO DE USUARIOS
-- Utilidad y Funcionamiento: Refuerza las políticas de seguridad del sistema (RN-016). Verifica que el usuario que está intentando realizar una acción tenga el estado 'ACTIVO'.      Si un usuario inactivo o bloqueado intenta operar, el trigger rechaza la acción, cerrando brechas de seguridad por credenciales antiguas.
CREATE OR REPLACE TRIGGER trg_validar_usuario_activo
    BEFORE INSERT OR UPDATE ON historial_accion
    FOR EACH ROW
DECLARE
    v_estado_usuario VARCHAR2(20);
BEGIN
    -- Verificar estado del usuario que realiza la acción
    SELECT estado INTO v_estado_usuario
    FROM usuario
    WHERE id_usuario = :NEW.id_usuario;
    
    IF v_estado_usuario != 'ACTIVO' THEN
        RAISE_APPLICATION_ERROR(-20008,
            'Usuario ID ' || :NEW.id_usuario || ' no está activo. ' ||
            'Estado actual: ' || v_estado_usuario);
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20009, 'Usuario no encontrado: ID ' || :NEW.id_usuario);
END trg_validar_usuario_activo;
/

-- TRIGGER PARA VALIDAR DONACIONES
-- Utilidad y Funcionamiento: Asegura la calidad de la información financiera (RN-011, RN-012). Impone que las donaciones monetarias tengan un monto mayor a cero y que todas las      donaciones tengan un área de destino asignada. Esto es vital para que los reportes financieros y de asignación de recursos sean precisos.
CREATE OR REPLACE TRIGGER trg_validar_donacion
    BEFORE INSERT ON donacion
    FOR EACH ROW
BEGIN
    -- Validar que las donaciones monetarias tengan monto
    IF :NEW.tipo_donacion = 'MONETARIA' AND (:NEW.monto IS NULL OR :NEW.monto <= 0) THEN
        RAISE_APPLICATION_ERROR(-20010,
            'Las donaciones monetarias deben especificar un monto válido. ' ||
            'Regla de negocio RN-011: Registro de toda donación.');
    END IF;
    
    -- Validar que las donaciones tengan área destino
    IF :NEW.id_area_destino IS NULL THEN
        RAISE_APPLICATION_ERROR(-20011,
            'Toda donación debe tener un área destino asignada. ' ||
            'Regla de negocio RN-012: Clasificación de donaciones.');
    END IF;
END trg_validar_donacion;
/

-- TRIGGER PARA LOG DE CONSULTAS MÉDICAS
-- Utilidad y Funcionamiento: Facilita la Trazabilidad del Tratamiento (RN-004). Al crearse una consulta, registra un resumen legible en la auditoría (quién atendió a quién). Esto    permite a los administradores visualizar el flujo de atención rápidamente sin consultas complejas SQL.
CREATE OR REPLACE TRIGGER trg_log_nueva_consulta
    AFTER INSERT ON consulta_medica
    FOR EACH ROW
DECLARE
    v_nombre_paciente VARCHAR2(100);
    v_nombre_medico VARCHAR2(100);
BEGIN
    -- Obtener nombres
    SELECT nombre || ' ' || apellido INTO v_nombre_paciente
    FROM paciente WHERE id_paciente = :NEW.id_paciente;
    
    SELECT nombre || ' ' || apellido INTO v_nombre_medico
    FROM usuario WHERE id_usuario = :NEW.id_medico;
    
    -- Registrar en historial
    INSERT INTO historial_accion (id_accion, tipo_accion, id_usuario, descripcion, modulo)
    VALUES (seq_accion.NEXTVAL, 'NUEVA_CONSULTA', :NEW.id_medico,
            'Nueva consulta registrada - ID: ' || :NEW.id_consulta ||
            ' - Paciente: ' || v_nombre_paciente ||
            ' - Médico: ' || v_nombre_medico ||
            ' - Diagnóstico: ' || SUBSTR(:NEW.diagnostico, 1, 100),
            'CONSULTAS');
    
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        NULL; -- No fallar si el log falla
END trg_log_nueva_consulta;
/

--TRIGGER PARA IMPEDIR ELIMINACIÓN DE REGISTROS ACTIVOS
-- Utilidad y Funcionamiento: Evita la pérdida accidental de información histórica. Impide eliminar físicamente a un paciente que tiene estado 'ACTIVO', forzando el uso del           borrado lógico (cambiar estado a 'INACTIVO'). Esto preserva la integridad referencial de todas las consultas y recetas asociadas a ese paciente en el pasado.
CREATE OR REPLACE TRIGGER trg_proteger_registros_activos
    BEFORE DELETE ON paciente
    FOR EACH ROW
    WHEN (OLD.estado = 'ACTIVO')
BEGIN
    RAISE_APPLICATION_ERROR(-20012,
        'No se puede eliminar pacientes activos. ' ||
        'Cambie el estado a INACTIVO primero. Paciente: ' || 
        :OLD.nombre || ' ' || :OLD.apellido || ' (DNI: ' || :OLD.dni || ')');
END trg_proteger_registros_activos;
/

-- TRIGGER PARA CONTROL DE VERSIÓN DE HISTORIAL CLÍNICO
-- Crea un registro histórico cada vez que se modifica el historial
CREATE OR REPLACE TRIGGER trg_versionar_historial_clinico
    BEFORE UPDATE ON historial_clinico
    FOR EACH ROW
DECLARE
    v_version_count NUMBER;
BEGIN
    -- Registrar versión anterior (simulación - en producción sería una tabla separada)
    INSERT INTO historial_accion (id_accion, tipo_accion, id_usuario, descripcion, modulo)
    VALUES (seq_accion.NEXTVAL, 'VERSION_HISTORIAL', 1, -- Usuario sistema
            'Versión anterior de historial - Paciente ID: ' || :OLD.id_paciente ||
            ' - Alergias antiguas: ' || SUBSTR(NVL(:OLD.alergias, 'Sin datos'), 1, 200) ||
            ' - Condiciones antiguas: ' || SUBSTR(NVL(:OLD.condiciones_cronicas, 'Sin datos'), 1, 200),
            'HISTORIAL_CLINICO');
END trg_versionar_historial_clinico;
/


-- ================= ASIGNACIÓN DE PERMISOS (RBAC) =================
GRANT EXECUTE ON PKG_AUTENTICACION TO rol_admin, rol_medico, rol_caja, rol_farmacia, rol_finanzas;

-- Permisos CAJA (Pacientes + Consultas)
GRANT EXECUTE ON PKG_PACIENTES TO rol_caja;
GRANT EXECUTE ON PKG_CONSULTAS TO rol_caja;
GRANT SELECT ON paciente TO rol_caja;
GRANT SELECT ON consulta_medica TO rol_caja;
GRANT SELECT ON usuario TO rol_caja;

-- Permisos MÉDICO (Todo clínico + Medicamentos)
GRANT EXECUTE ON PKG_PACIENTES TO rol_medico;
GRANT EXECUTE ON PKG_CONSULTAS TO rol_medico;
GRANT EXECUTE ON PKG_MEDICAMENTOS TO rol_medico;
GRANT SELECT ON paciente TO rol_medico;
GRANT SELECT ON consulta_medica TO rol_medico;
GRANT SELECT ON medicamento TO rol_medico;
GRANT SELECT ON usuario TO rol_medico;

-- Permisos FARMACIA (Medicamentos)
GRANT EXECUTE ON PKG_MEDICAMENTOS TO rol_farmacia;
GRANT SELECT ON medicamento TO rol_farmacia;
GRANT SELECT ON usuario TO rol_farmacia;

-- Permisos FINANZAS (Donaciones)
GRANT EXECUTE ON PKG_DONACIONES TO rol_finanzas;
GRANT SELECT ON donacion TO rol_finanzas;
GRANT SELECT ON area_clinica TO rol_finanzas;
GRANT SELECT ON usuario TO rol_finanzas;

-- ================= DATOS INICIALES (SEED DATA) =================
-- Roles
INSERT INTO rol VALUES (seq_rol.NEXTVAL, 'ADMINISTRADOR', 'Acceso total');
INSERT INTO rol VALUES (seq_rol.NEXTVAL, 'MEDICO', 'Personal médico');
INSERT INTO rol VALUES (seq_rol.NEXTVAL, 'CAJA', 'Admisión y pagos');
INSERT INTO rol VALUES (seq_rol.NEXTVAL, 'FARMACIA', 'Despacho de medicamentos');
INSERT INTO rol VALUES (seq_rol.NEXTVAL, 'FINANZAS', 'Contabilidad y donaciones');

-- Áreas
INSERT INTO area_clinica VALUES (seq_area.NEXTVAL ,'Administración', SYSDATE, 5, 'Oficinas');
INSERT INTO area_clinica VALUES (seq_area.NEXTVAL , 'Medicina General', SYSDATE, 10, 'General');

-- Usuarios (Password universal: 123456 -> Hash SHA-256)
-- Hash: 8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92

-- 1. ADMIN
INSERT INTO usuario (id_usuario, nombre, apellido, email, contrasena_hash, estado, id_rol, dni)
VALUES (seq_usuario.NEXTVAL, 'Super', 'Admin', 'admin@fisiclinica.pe', 
'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 'ACTIVO', 
(SELECT id_rol FROM rol WHERE nombre_rol='ADMINISTRADOR'), '99999999');

-- 2. MÉDICO
INSERT INTO usuario (id_usuario, nombre, apellido, email, contrasena_hash, estado, id_rol, dni)
VALUES (seq_usuario.NEXTVAL, 'Gregory', 'House', 'medico@fisiclinica.pe', 
'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 'ACTIVO', 
(SELECT id_rol FROM rol WHERE nombre_rol='MEDICO'), '88888888');

-- 3. CAJA
INSERT INTO usuario (id_usuario, nombre, apellido, email, contrasena_hash, estado, id_rol, dni)
VALUES (seq_usuario.NEXTVAL, 'Carmen', 'Caja', 'caja@fisiclinica.pe', 
'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 'ACTIVO', 
(SELECT id_rol FROM rol WHERE nombre_rol='CAJA'), '77777777');

-- 4. FARMACIA
INSERT INTO usuario (id_usuario, nombre, apellido, email, contrasena_hash, estado, id_rol, dni)
VALUES (seq_usuario.NEXTVAL, 'Felipe', 'Farma', 'farmacia@fisiclinica.pe', 
'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 'ACTIVO', 
(SELECT id_rol FROM rol WHERE nombre_rol='FARMACIA'), '66666666');

-- 5. FINANZAS
INSERT INTO usuario (id_usuario, nombre, apellido, email, contrasena_hash, estado, id_rol, dni)
VALUES (seq_usuario.NEXTVAL, 'Sr.', 'Dinero', 'finanzas@fisiclinica.pe', 
'8D969EEF6ECAD3C29A3A629280E686CF0C3F5D5A86AFF3CA12020C923ADC6C92', 'ACTIVO', 
(SELECT id_rol FROM rol WHERE nombre_rol='FINANZAS'), '55555555');

-- Datos de prueba
INSERT INTO paciente (id_paciente, dni, nombre, apellido, fecha_nacimiento, genero, telefono, direccion, email, id_usuario_registro)
VALUES (seq_paciente.NEXTVAL, '40000001', 'Juan', 'Perez', SYSDATE-10000, 'MASCULINO', '999888777', 'Av Lima', 'juan@mail.com', 1);

INSERT INTO medicamento VALUES (seq_medicamento.NEXTVAL , 'Paracetamol', 'Generico', 'Paracetamol', '500mg', 'Tableta', 100, 20, 0.50, 'ACTIVO', SYSDATE);
INSERT INTO medicamento VALUES (seq_medicamento.NEXTVAL, 'Ibuprofeno', 'Generico', 'Ibuprofeno', '400mg', 'Tableta', 50, 60, 0.80, 'ACTIVO', SYSDATE); -- Stock Bajo

COMMIT;
ALTER SESSION SET "_ORACLE_SCRIPT"=true;

-- 1. Tablespaces (Almacenamiento físico)
CREATE TABLESPACE fisiclinica_data DATAFILE 'fisiclinica_data01.dbf' SIZE 100M AUTOEXTEND ON NEXT 10M MAXSIZE UNLIMITED;
CREATE TABLESPACE fisiclinica_index DATAFILE 'fisiclinica_index01.dbf' SIZE 50M AUTOEXTEND ON NEXT 5M MAXSIZE UNLIMITED;
CREATE TEMPORARY TABLESPACE fisiclinica_temp TEMPFILE 'fisiclinica_temp01.dbf' SIZE 50M AUTOEXTEND ON NEXT 5M MAXSIZE UNLIMITED;

-- 2. Perfiles de Seguridad
CREATE PROFILE perfil_admin LIMIT FAILED_LOGIN_ATTEMPTS 5 PASSWORD_LIFE_TIME UNLIMITED;

-- 3. Roles de Base de Datos (RBAC)
CREATE ROLE rol_admin;
CREATE ROLE rol_medico;
CREATE ROLE rol_caja;
CREATE ROLE rol_farmacia;
CREATE ROLE rol_finanzas;

-- 4. Usuario Dueño del Esquema
CREATE USER fisiclinica_admin IDENTIFIED BY Admin2024$Fisiclinica
DEFAULT TABLESPACE fisiclinica_data TEMPORARY TABLESPACE fisiclinica_temp
QUOTA UNLIMITED ON fisiclinica_data QUOTA UNLIMITED ON fisiclinica_index
PROFILE perfil_admin;

-- 5. Permisos Iniciales
GRANT CONNECT, RESOURCE, CREATE VIEW, CREATE PROCEDURE, CREATE SEQUENCE, CREATE TRIGGER, CREATE TABLE TO fisiclinica_admin;
GRANT GRANT ANY ROLE TO fisiclinica_admin;

-- Asignar los roles creados al dueño para que pueda administrarlos
GRANT rol_admin, rol_medico, rol_caja, rol_farmacia, rol_finanzas TO fisiclinica_admin;
-- =====================================================================
--  BIA App - Datos de demostración (empresa FICTICIA)
--  Empresa: Zubialde Logística S.L. (nombre, personas y teléfonos inventados)
--
--  Uso:
--    1) Arranca la app al menos una vez (Hibernate crea las tablas).
--    2) Ejecuta este script sobre la base de datos `bia`:
--         mysql -u root -p bia < demo/datos_demo_zubialde.sql
--    3) Abre http://localhost:8080/empresas -> Zubialde Logística S.L.
--       -> BIA 2026 -> "Informe Ejecutivo PDF" -> Descargar PDF / Imprimir.
--
--  El script es re-ejecutable: borra antes la empresa demo si ya existe.
--  Resultado esperado en el informe:
--    8 procesos | 3 CRÍTICOS | 3 ALTOS | 2 MEDIO-BAJOS
--    SPOF personas: Iker Etxeberria, Ane Goikoetxea
--    SPOF activos : ERP, BBDD SQL PROD, Firewall perimetral, Enlace SD-WAN
-- =====================================================================

SET NAMES utf8mb4;
START TRANSACTION;

-- ---------- Limpieza de una carga anterior ----------
SET @old_emp := (SELECT id FROM empresa WHERE nombre = 'Zubialde Logística S.L.' LIMIT 1);

DELETE pa FROM proceso_activo pa
  JOIN proceso_critico pc ON pc.id = pa.proceso_id
  JOIN bia_proyecto b ON b.id = pc.bia_proyecto_id
 WHERE b.empresa_id = @old_emp;
DELETE pp FROM proceso_persona pp
  JOIN proceso_critico pc ON pc.id = pp.proceso_id
  JOIN bia_proyecto b ON b.id = pc.bia_proyecto_id
 WHERE b.empresa_id = @old_emp;
DELETE pc FROM proceso_critico pc
  JOIN bia_proyecto b ON b.id = pc.bia_proyecto_id
 WHERE b.empresa_id = @old_emp;
DELETE FROM bia_proyecto       WHERE empresa_id = @old_emp;
DELETE FROM persona            WHERE empresa_id = @old_emp;
DELETE FROM activo_tecnologico WHERE empresa_id = @old_emp;
DELETE FROM empresa            WHERE id = @old_emp;

-- ---------- Empresa ----------
INSERT INTO empresa (nombre, sector, tamano)
VALUES ('Zubialde Logística S.L.', 'Logística y Transporte', '180');
SET @emp := LAST_INSERT_ID();

-- ---------- Personas clave ----------
INSERT INTO persona (nombre, cargo, telefono, empresa_id) VALUES ('Ane Goikoetxea',   'Directora de Operaciones',          '+34 600 000 101', @emp); SET @p_ops   := LAST_INSERT_ID();
INSERT INTO persona (nombre, cargo, telefono, empresa_id) VALUES ('Iker Etxeberria',  'Administrador de Sistemas',         '+34 600 000 102', @emp); SET @p_sis   := LAST_INSERT_ID();
INSERT INTO persona (nombre, cargo, telefono, empresa_id) VALUES ('Mikel Arrieta',    'Jefe de Almacén',                   '+34 600 000 103', @emp); SET @p_alm   := LAST_INSERT_ID();
INSERT INTO persona (nombre, cargo, telefono, empresa_id) VALUES ('Leire Uriarte',    'Jefa de Tráfico y Flota',           '+34 600 000 104', @emp); SET @p_traf  := LAST_INSERT_ID();
INSERT INTO persona (nombre, cargo, telefono, empresa_id) VALUES ('Jon Bilbao',       'Responsable Financiero',            '+34 600 000 105', @emp); SET @p_fin   := LAST_INSERT_ID();
INSERT INTO persona (nombre, cargo, telefono, empresa_id) VALUES ('Nerea Zabala',     'Responsable de Atención al Cliente','+34 600 000 106', @emp); SET @p_cli   := LAST_INSERT_ID();
INSERT INTO persona (nombre, cargo, telefono, empresa_id) VALUES ('Amaia Larrañaga',  'Responsable de RRHH',               '+34 600 000 107', @emp); SET @p_rrhh  := LAST_INSERT_ID();
INSERT INTO persona (nombre, cargo, telefono, empresa_id) VALUES ('Unai Olabarria',   'Responsable de Compras',            '+34 600 000 108', @emp); SET @p_comp  := LAST_INSERT_ID();
INSERT INTO persona (nombre, cargo, telefono, empresa_id) VALUES ('Maialen Ibarra',   'Responsable de Marketing',          '+34 600 000 109', @emp); SET @p_mkt   := LAST_INSERT_ID();

-- ---------- Activos tecnológicos (tipos del desplegable de la app) ----------
INSERT INTO activo_tecnologico (nombre, tipo, empresa_id) VALUES ('ERP corporativo',                       'Aplicación Web',         @emp); SET @a_erp   := LAST_INSERT_ID();
INSERT INTO activo_tecnologico (nombre, tipo, empresa_id) VALUES ('SGA - Sistema de Gestión de Almacén',   'Aplicación Web',         @emp); SET @a_sga   := LAST_INSERT_ID();
INSERT INTO activo_tecnologico (nombre, tipo, empresa_id) VALUES ('BBDD SQL PROD',                         'Base de Datos',          @emp); SET @a_bbdd  := LAST_INSERT_ID();
INSERT INTO activo_tecnologico (nombre, tipo, empresa_id) VALUES ('Plataforma de telemetría GPS de flota', 'Aplicación Web',         @emp); SET @a_gps   := LAST_INSERT_ID();
INSERT INTO activo_tecnologico (nombre, tipo, empresa_id) VALUES ('Portal web de clientes',                'Aplicación Web',         @emp); SET @a_port  := LAST_INSERT_ID();
INSERT INTO activo_tecnologico (nombre, tipo, empresa_id) VALUES ('Clúster de virtualización CPD',         'Servidor',               @emp); SET @a_virt  := LAST_INSERT_ID();
INSERT INTO activo_tecnologico (nombre, tipo, empresa_id) VALUES ('Servidor de ficheros',                  'Servidor',               @emp); SET @a_fich  := LAST_INSERT_ID();
INSERT INTO activo_tecnologico (nombre, tipo, empresa_id) VALUES ('Firewall perimetral',                   'Infraestructura de Red', @emp); SET @a_fw    := LAST_INSERT_ID();
INSERT INTO activo_tecnologico (nombre, tipo, empresa_id) VALUES ('Enlace SD-WAN CPD - Almacén',           'Infraestructura de Red', @emp); SET @a_wan   := LAST_INSERT_ID();
INSERT INTO activo_tecnologico (nombre, tipo, empresa_id) VALUES ('Web corporativa',                       'Aplicación Web',         @emp); SET @a_web   := LAST_INSERT_ID();

-- ---------- Proyecto BIA ----------
INSERT INTO bia_proyecto (nombre, descripcion, anio, empresa_id)
VALUES ('BIA 2026 - Operaciones Logísticas',
        'Análisis de impacto en el negocio de los procesos operativos y de soporte del centro logístico, alineado con ISO 22301.',
        '2026', @emp);
SET @bia := LAST_INSERT_ID();

-- ---------- Procesos críticos ----------
-- criticidad = impacto + urgencia(RTO): <=1h:5 | <=4h:4 | <=8h:3 | <=24h:2 | >24h:1
-- riesgo     = impacto x probabilidad : >=15 CRÍTICO | 6-14 ALTO | 1-5 MEDIO-BAJO
INSERT INTO proceso_critico (nombre, descripcion, rto_horas, rpo, impacto, probabilidad, criticidad, bia_proyecto_id)
VALUES ('Gestión de pedidos y expediciones',
        'Recepción de pedidos de clientes, planificación de rutas y emisión de albaranes de expedición.',
        4, '15 min', 5, 4, 9, @bia);
SET @pr_ped := LAST_INSERT_ID();   -- riesgo 20 CRÍTICO

INSERT INTO proceso_critico (nombre, descripcion, rto_horas, rpo, impacto, probabilidad, criticidad, bia_proyecto_id)
VALUES ('Control de inventario y almacén',
        'Entradas, ubicaciones, picking y salidas de mercancía gestionadas a través del SGA.',
        2, '15 min', 5, 3, 9, @bia);
SET @pr_alm := LAST_INSERT_ID();   -- riesgo 15 CRÍTICO

INSERT INTO proceso_critico (nombre, descripcion, rto_horas, rpo, impacto, probabilidad, criticidad, bia_proyecto_id)
VALUES ('Seguimiento de flota en tiempo real',
        'Localización GPS de vehículos, control de tiempos de conducción y avisos de incidencias en ruta.',
        1, '5 min', 4, 4, 9, @bia);
SET @pr_gps := LAST_INSERT_ID();   -- riesgo 16 CRÍTICO

INSERT INTO proceso_critico (nombre, descripcion, rto_horas, rpo, impacto, probabilidad, criticidad, bia_proyecto_id)
VALUES ('Facturación y cobros',
        'Emisión de facturas a clientes, conciliación bancaria y gestión de cobros.',
        24, '1 h', 4, 3, 6, @bia);
SET @pr_fac := LAST_INSERT_ID();   -- riesgo 12 ALTO

INSERT INTO proceso_critico (nombre, descripcion, rto_horas, rpo, impacto, probabilidad, criticidad, bia_proyecto_id)
VALUES ('Atención al cliente',
        'Consulta de estado de envíos, reclamaciones y soporte a clientes a través del portal web.',
        8, '1 h', 3, 3, 6, @bia);
SET @pr_cli := LAST_INSERT_ID();   -- riesgo 9 ALTO

INSERT INTO proceso_critico (nombre, descripcion, rto_horas, rpo, impacto, probabilidad, criticidad, bia_proyecto_id)
VALUES ('Nóminas y gestión de RRHH',
        'Cálculo y pago de nóminas, control horario y documentación laboral del personal.',
        72, '24 h', 3, 2, 4, @bia);
SET @pr_rrhh := LAST_INSERT_ID();  -- riesgo 6 ALTO

INSERT INTO proceso_critico (nombre, descripcion, rto_horas, rpo, impacto, probabilidad, criticidad, bia_proyecto_id)
VALUES ('Compras y aprovisionamiento',
        'Pedidos a proveedores de embalaje, combustible y repuestos de flota.',
        48, '24 h', 2, 2, 3, @bia);
SET @pr_comp := LAST_INSERT_ID();  -- riesgo 4 MEDIO-BAJO

INSERT INTO proceso_critico (nombre, descripcion, rto_horas, rpo, impacto, probabilidad, criticidad, bia_proyecto_id)
VALUES ('Marketing y web corporativa',
        'Publicación de contenidos, campañas y mantenimiento de la web institucional.',
        120, '7 días', 1, 3, 2, @bia);
SET @pr_mkt := LAST_INSERT_ID();   -- riesgo 3 MEDIO-BAJO

-- ---------- Dependencias proceso -> activo ----------
INSERT INTO proceso_activo (proceso_id, activo_id) VALUES
  (@pr_ped,  @a_erp), (@pr_ped,  @a_bbdd), (@pr_ped, @a_fw), (@pr_ped, @a_wan),
  (@pr_alm,  @a_sga), (@pr_alm,  @a_bbdd), (@pr_alm, @a_wan), (@pr_alm, @a_virt),
  (@pr_gps,  @a_gps), (@pr_gps,  @a_fw),
  (@pr_fac,  @a_erp), (@pr_fac,  @a_bbdd),
  (@pr_cli,  @a_port), (@pr_cli, @a_erp),
  (@pr_rrhh, @a_fich),
  (@pr_comp, @a_erp),
  (@pr_mkt,  @a_web);

-- ---------- Dependencias proceso -> persona ----------
INSERT INTO proceso_persona (proceso_id, persona_id) VALUES
  (@pr_ped,  @p_ops), (@pr_ped, @p_sis),
  (@pr_alm,  @p_alm), (@pr_alm, @p_sis),
  (@pr_gps,  @p_traf), (@pr_gps, @p_ops),
  (@pr_fac,  @p_fin),
  (@pr_cli,  @p_cli),
  (@pr_rrhh, @p_rrhh),
  (@pr_comp, @p_comp),
  (@pr_mkt,  @p_mkt);

COMMIT;

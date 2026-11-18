-- Sistema de facturación: 5 tablas relacionadas (MySQL)
CREATE DATABASE IF NOT EXISTS tienda_facturacion CHARACTER SET utf8mb4;
USE tienda_facturacion;

CREATE TABLE cliente (
    id_cliente   INT AUTO_INCREMENT PRIMARY KEY,
    cedula_ruc   VARCHAR(13) NOT NULL UNIQUE,
    nombres      VARCHAR(60) NOT NULL,
    apellidos    VARCHAR(60) NOT NULL,
    direccion    VARCHAR(100),
    telefono     VARCHAR(15)
) ENGINE=InnoDB;

CREATE TABLE empleado (
    id_empleado  INT AUTO_INCREMENT PRIMARY KEY,
    nombres      VARCHAR(60) NOT NULL,
    apellidos    VARCHAR(60) NOT NULL,
    cargo        VARCHAR(40)
) ENGINE=InnoDB;

CREATE TABLE producto (
    id_producto  INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(80) NOT NULL,
    precio       DECIMAL(10,2) NOT NULL,
    stock        INT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

CREATE TABLE factura (
    id_factura   INT AUTO_INCREMENT PRIMARY KEY,
    fecha        DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_cliente   INT NOT NULL,
    id_empleado  INT NOT NULL,
    total        DECIMAL(10,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (id_cliente)  REFERENCES cliente(id_cliente),
    FOREIGN KEY (id_empleado) REFERENCES empleado(id_empleado)
) ENGINE=InnoDB;

CREATE TABLE detalle_factura (
    id_detalle      INT AUTO_INCREMENT PRIMARY KEY,
    id_factura      INT NOT NULL,
    id_producto     INT NOT NULL,
    cantidad        INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal        DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_factura)  REFERENCES factura(id_factura) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
) ENGINE=InnoDB;

-- Datos de ejemplo
INSERT INTO cliente (cedula_ruc, nombres, apellidos, direccion, telefono) VALUES
 ('1300000001', 'Ana',   'Mero',   'Portoviejo', '0991111111'),
 ('1300000002', 'Luis',  'Zambrano', 'Manta',    '0992222222');

INSERT INTO empleado (nombres, apellidos, cargo) VALUES
 ('Carlos', 'Vera',  'Cajero'),
 ('María',  'Loor',  'Vendedora');

INSERT INTO producto (nombre, precio, stock) VALUES
 ('Cuaderno universitario', 2.50, 50),
 ('Esfero azul',            0.40, 200),
 ('Calculadora científica', 18.00, 15),
 ('Carpeta plástica',       1.20, 80);

# Sistema de facturación en Java con MySQL

Aplicación de escritorio en Java (Swing + JDBC) que registra facturas de una tienda. Usa una base de datos MySQL con 5 tablas relacionadas.

## Tablas

| Tabla | Descripción |
| --- | --- |
| `cliente` | Datos del cliente (cédula/RUC, nombres, apellidos, dirección, teléfono) |
| `empleado` | Empleados que emiten las facturas |
| `producto` | Productos con precio y stock |
| `factura` | Cabecera de la factura (fecha, cliente, empleado, total) |
| `detalle_factura` | Líneas de cada factura (producto, cantidad, precio, subtotal) |

## Pantallas

- **Clientes:** guardar, consultar, actualizar y eliminar.
- **Nueva factura:** elegir cliente y empleado, agregar productos, calcular el total y descontar el stock (en una transacción).
- **Ver facturas:** lista de facturas con filtro por cliente y detalle de la factura seleccionada.

## Requisitos

- MySQL (por ejemplo con XAMPP y phpMyAdmin)
- NetBeans con Maven
- Java 17
- Conector `mysql-connector-j` (declarado en `pom.xml`)

## Cómo ejecutarlo

1. En phpMyAdmin, importar `schema.sql`. Crea la base `tienda_facturacion`, las tablas y datos de ejemplo.
2. Abrir el proyecto en NetBeans. Todas las clases deben estar en el paquete `com.mycompany.sistemafacturacion`.
3. Ejecutar la clase `Principal`.
4. Contaseña:`""`

## Archivos

- `schema.sql`: script de la base de datos
- `accesobd.java`: conexión JDBC
- `ClienteBeans.java`: datos y operaciones del cliente
- `FrmClientes.java`, `FrmFactura.java`, `FrmVerFacturas.java`: formularios
- `Principal.java`: menú principal
- `pom.xml`: configuración de Maven

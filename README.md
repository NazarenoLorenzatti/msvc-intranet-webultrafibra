Microservicio para la Gestión del Área de Intranet Corporativo

Este microservicio actúa como una interfaz de información procesada entre el sistema de gestión empresarial (ERP) y el sistema de reportes, proporcionando funcionalidades clave para la operación de la intranet corporativa.

Funciones principales
Integración con el ERP:
Consume los endpoints del sistema de gestión para obtener la información necesaria para los reportes. Posteriormente, procesa y almacena dichos datos en una base de datos propia, la cual es accesible mediante Power BI para la generación de informes y análisis.

Gestión de usuarios y roles:
Administra la autenticación, autorización y los niveles de acceso de los usuarios del frontend corporativo, estableciendo roles claramente definidos:

ROLE_ADMIN: Perfil orientado al desarrollo, con acceso completo al sistema.
ROLE_SUPPORT: Perfil enfocado en soporte técnico, con acceso exclusivo a reportes de desempeño diario del personal técnico.
ROLE_REP: Perfil de representante comercial, sin acceso a reportes. Permite realizar el ABM (Alta, Baja, Modificación) de usuarios en el portal de autogestión.
ROLE_GERENCIA: Perfil de dirección general (CEO), con acceso a todos los reportes, incluyendo aquellos exclusivos de gerencia.
ROLE_COMERCIAL: Perfil administrativo, con acceso limitado a reportes relacionados con ventas y clientes.
Asistencia en la creación de usuarios:
El microservicio se conecta con una segunda base de datos que contiene información sobre los usuarios creados por los clientes para el acceso al portal de autogestión. Esto permite que los representantes comerciales brinden soporte personalizado a clientes con dificultades para crear sus usuarios por sí mismos.

Propósito general
El microservicio tiene como objetivo central:

Facilitar la gestión de usuarios y permisos del intranet corporativo, asegurando una administración eficiente y segura.
Procesar información del ERP para su uso en reportes y análisis, optimizando la integración entre el sistema de gestión y las herramientas de visualización de datos como Power BI.
Este enfoque permite fortalecer la operación interna de la empresa al centralizar la gestión de usuarios, garantizar la seguridad de la información y proporcionar una capa de datos procesados para la toma de decisiones estratégicas.

En resumen este MSVC se encarga de gestionar los usuarios del intranet corporativo y sus permisos, y crear una interface para acceder a la informacion del ERP ya procesada.
asi mismo tiene la potestad de conectarse con una segunda base de datos donde se encuentra la informacion de los usuarios creados por los clientes para el ingreso al portal de autogestion, 
de esta manera nuestros representantes comerciales pueden dar ayudar a personas que tengan una mayor dificultad para crear su usuario por si mismos.

# Uso del proyecto de fichas de montaña.

## Servidor repositorio 
## ********************

El servidor escrito en go, funciona como repositorio de las fichas de la siguiente manera, se almacenan los metadatos de la ficha en dos ficheros, cards.txt donde se almacena su id, nombre, descripcion, dificultad y categoria, separando los
campos en formato csv.


### Formato fichero cards.txt
### -------------------------
Id,Nombre,Descripcion,Dificultad,Categoria

En el segundo fichero se almacenan los metadatos de las rutas de cada ficha, se relaciona una ruta con el id y nombre de la ficha.

### formato fichero routes.txt
### --------------------------

Id,Nombre,latitud,longitud,radio,pathImagen


*La localizacion y la imagen están relacionadas es decir la imagen que se 
muestre es la de la localizacion relacionada.*


### Repositorio de imagenes
### -----------------------

Las imagenes están guardas de la siguiente manera, cuando se crea una ficha
se crea un directorio para esa ficha en el directorio images de donde esté 
arrancado el servidor es decir si tengo dos fichas sus imágenes están 
almacenadas en el sistema de ficheros de donde esté el servidor de la siguiente
manera :

Directorio de images con id 1 y nombre ficha1 : images/1ficha1/
Directorio de images con id 2 y nombre ficha2 : images/2ficha2/

Se crea un directorio por ficha y en ese directorio se almacenaran todas sus 
imagenes. 

*Esto es por si hubiera una imagen que se llamara igual en dos fichas 
diferentes no se sobreescribieran las imágenes*

El servidor debe tener permisos de lectura y escritura en el directorio desde
donde se lance para poder crear todos los ficheros y directorios para realizar
su función de repositorio, en otro caso si no tuviera permisos suficientes 
el comportamiento del servidor será completamente inesperado.


## Cliente de Android
## ******************

El cliente de android se encarga de utilizar la aplicación de fichas de montaña para
realizar las rutas de montaña usando la geolocalización del dispositivo para encontrar
rutas de montaña cercanas a su ubicación actual, también permite crear fichas de montaña.

Cuando el dispositivo lanza la aplicación el usuario tiene dos opciones crear una ficha
o buscar fichas dentro de un radio de 10km de su posicion actual filtrando por categoria,
dificultad o ambas.

### Almacenamineto de las imágenes en el dispositivo.

Se guardan las imágenes dentro del dispositivo parecido a como se realiza en el servidor
se crea un directorio por cada ficha id+nombre cómo nombre del directorio y dentro del 
directorio se guardan las imágenes de la ficha.


### Almacenamiento de datos offline de fichas

Las fichas descargadas del repositorio que ya se han buscado se guardan en el dispositivo
en una base de datos interna, para acceder a las imágnes se guarda el path donde se encuentra
la imagen dentro del dispositivo móvil.


### Permisos necesarios del dispositivo móvil

Se debe dar permisos de localización y almacenamiento (location and storage) para que funcione
correctmente, la aplicación te pedirá los permisos para ejecutar, si no se conceden los permisos
de localización y almacenamiento a la aplicación el comportamiento de la aplicación es inesperado.

## Tests

Para que los tests funcionen el servidor y cliente deben ejecutar en modo test
el servidor en modo tests es un servidor de repositorio vacio.

El cliente ejecutara los test instrumentados para probar todo el sistema.

Tanto el protocolo, la base de datos y las activities del dispositivo movil
se prueban a través de los tests instrumentados con un servidor de respositorio
vacio.



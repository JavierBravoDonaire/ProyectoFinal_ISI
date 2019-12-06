#!/usr/bin/python3.6
import csv

peliculas = open("./peliculas.txt", "r")
movies = open("./movies.txt", "w")

r = csv.reader(peliculas, delimiter = ",")

for row in r:
    title = row[1]
    fecha = row[2]
    with open('title.basics.tsv','r') as tsv:
        re = csv.reader(tsv, delimiter = "\t")
        for fila in re:
            if title == fila[3]:
                print("Entramos")
                id = fila[0]
                movies.write(str(id) + ',' + title + ',' + fecha + '\n')
                break


peliculas.close()
movies.close()

#!/bin/bash

echo -n "" > routes.txt;
echo -n "" > cards.txt;
rm -rf images/

go run fichas.go cards.txt routes.txt

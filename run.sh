#!/bin/bash

make all run INPUT=$1 OUTPUT=result.ll
lli result.ll
read
vim -O $1 result.ll

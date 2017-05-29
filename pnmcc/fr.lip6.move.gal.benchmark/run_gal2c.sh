#!/bin/bash

CURRENT=$(pwd)
export BINDIR=/home/mcc/ITSTools/pnmcc/fr.lip6.move.gal.benchmark

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 MODEL EXAMINATION"
  exit 1
fi

# We only compete on LTLCardinality track
case $2 in
  "LTLCardinality")
    ;;
  *)
    echo "DO_NOT_COMPETE"
    exit
    ;;
esac

cd /home/mcc/BenchKit/INPUTS

tar xzf $1.tgz
if [ -d ../patch/$1 ] ;
then
    \cp -rf ../patch/$1 .
fi

cd $1

export MODEL=$(pwd)

$BINDIR/runeclipse.sh $MODEL $2 -onlyGal
$BINDIR/gal2c $MODEL/$2.gal

# I need perl here
$BINDIR/run_gal2c.pl $MODEL/$2.gal2C $MODEL/$2.ltl

cd ..

\rm -rf $1

cd $CURRENT




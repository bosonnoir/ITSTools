#!/bin/bash

export BINDIR=$(pwd)

# We only compete on LTLCardinality track
case $2 in
  "LTLCardinality")
    ;;
  *)
    echo "DO_NOT_COMPETE"
    exit
    ;;
esac

cd INPUTS

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
#for f in $(grep -v "^#" $MODEL/$2.ltl); do
# echo $f
# /work/max/git/spot/tests/ltsmin/modelcheck -Z $MODEL/$2.gal2C $f
#one

cd ..

#\rm -rf $1

cd ..




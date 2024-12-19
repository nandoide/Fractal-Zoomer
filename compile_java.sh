#!/bin/bash

if [ "$1" == "fz" ]; then
    MAIN_CLASS="fractalzoomer.main.MainWindow"
    JAR_NAME="FractalZoomer.jar"
elif [ "$1" == "render" ]; then
    MAIN_CLASS="fractalzoomer.main.MinimalRendererWindow"
    JAR_NAME="FZMinimalRenderer.jar"
else
    echo "Usage: $0 {fz|render}"
    exit 1
fi

# Detect the number of processors
if command -v nproc &> /dev/null
then
    NPROC=$(nproc)
else
    NPROC=$(sysctl -n hw.ncpu)
fi

# Create the output directory if it doesn't exist
if [ ! -d "../Fractal-Zoomer-Work" ]; then
    mkdir ../Fractal-Zoomer-Work
fi
BIN="../Fractal-Zoomer-Work/bin"

# Clean up previous builds
rm -rf $BIN
mkdir -p $BIN

# Compile the Java source files
find src -name "*.java" > sources.txt
javac --release 8 -Xlint:-options -d $BIN -cp "lib/*" @sources.txt

# Create the MANIFEST.MF file
echo "Manifest-Version: 1.0" > $BIN/MANIFEST.MF
echo "Main-Class: $MAIN_CLASS" >> $BIN/MANIFEST.MF

# Extract and deploy the JAR libraries
cwd=$(pwd)
echo "Extracting JAR libraries..."
cd $BIN
for jar in $cwd/lib/*.jar; do
    jar -xf $jar
done
echo "Coying resources..."
cd $cwd
cp /Users/nandoide/CODE/mpfr-4.2.1/src/.libs/libmpfr.6.dylib  /Users/nandoide/CODE/Fractal-Zoomer/src/fractalzoomer/native/general/darwin-aarch64
cp /Users/nandoide/CODE/gmp-6.3.0/.libs/libgmp.10.dylib  /Users/nandoide/CODE/Fractal-Zoomer/src/fractalzoomer/native/general/darwin-aarch64

# Copy the fractalzoomer/color_maps directory to the $BIN directory
cp -r src/fractalzoomer/color_maps $BIN/fractalzoomer/
cp -r src/fractalzoomer/native $BIN/fractalzoomer/

#  Copy other resources (e.g., images) to the $BIN directory
cp -r src/fractalzoomer/icons $BIN/fractalzoomer/
cp -r src/fractalzoomer/help $BIN/fractalzoomer/
mkdir -p $BIN/fractalzoomer/parser/code
cp src/fractalzoomer/parser/code/* $BIN/fractalzoomer/parser/code/
cp src/fractalzoomer/palettes/palettes.json $BIN/fractalzoomer/palettes/
mkdir -p $BIN/fractalzoomer/fonts
cp src/fractalzoomer/fonts/* $BIN/fractalzoomer/fonts/

# Package everything into a JAR file
cd $BIN
jar cfm ../$JAR_NAME MANIFEST.MF .
cd ..
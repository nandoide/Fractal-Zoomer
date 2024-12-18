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

# Clean up previous builds
rm -rf bin
mkdir -p bin

# Compile the Java source files
find src -name "*.java" > sources.txt
javac -d bin -cp "lib/*" @sources.txt

# exit 0
# Create the MANIFEST.MF file
echo "Manifest-Version: 1.0" > bin/MANIFEST.MF
# echo "Main-Class: fractalzoomer.main.MainWindow" >> bin/MANIFEST.MF
echo "Main-Class: $MAIN_CLASS" >> bin/MANIFEST.MF
echo -n "Class-Path: " >> bin/MANIFEST.MF
for jar in lib/*.jar; do
    echo -n "$jar " >> bin/MANIFEST.MF
done
echo "" >> bin/MANIFEST.MF

cp /Users/nandoide/CODE/mpfr-4.2.1/src/.libs/libmpfr.6.dylib  /Users/nandoide/CODE/Fractal-Zoomer/src/fractalzoomer/native/general/darwin-aarch64
cp /Users/nandoide/CODE/gmp-6.3.0/.libs/libgmp.10.dylib  /Users/nandoide/CODE/Fractal-Zoomer/src/fractalzoomer/native/general/darwin-aarch64

# Copy the fractalzoomer/color_maps directory to the bin directory
cp -r src/fractalzoomer/color_maps bin/fractalzoomer/
cp -r src/fractalzoomer/native bin/fractalzoomer/

# Copy the JAR libraries to the bin directory
mkdir -p bin/lib
cp lib/*.jar bin/lib/

#  Copy other resources (e.g., images) to the bin directory
cp -r src/fractalzoomer/icons bin/fractalzoomer/
cp -r src/fractalzoomer/help bin/fractalzoomer/
mkdir -p bin/fractalzoomer/parser/code
cp src/fractalzoomer/parser/code/* bin/fractalzoomer/parser/code/
cp src/fractalzoomer/palettes/palettes.json bin/fractalzoomer/palettes/
mkdir -p bin/fractalzoomer/fonts
cp src/fractalzoomer/fonts/* bin/fractalzoomer/fonts/

# Package everything into a JAR file
cd bin
jar cfm ../$JAR_NAME MANIFEST.MF .
cd ..

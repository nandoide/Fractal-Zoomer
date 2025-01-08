#!/bin/bash

if [ "$1" == "fz" ]; then
    MAIN_CLASS="fractalzoomer.main.MainWindow"
    JAR_NAME="FractalZoomer.jar"
elif [ "$1" == "render" ]; then
    MAIN_CLASS="fractalzoomer.main.MinimalRendererWindow"
    JAR_NAME="FZMinimalRenderer.jar"
else
    echo "Usage: $0 {fz|render} [--fast] [--ultrafast]"
    exit 1
fi

# Detect the number of processors
if command -v nproc &> /dev/null
then
    NPROC=$(nproc)
else
    NPROC=$(sysctl -n hw.ncpu)
fi

WORKPATH="../Fractal-Zoomer-Work"
BIN="$WORKPATH/bin"

# Parse command line arguments
FAST_MODE=false
ULTRAFAST_MODE=false
for arg in "$@"
do
    case $arg in
        --fast)
        FAST_MODE=true
        shift
        ;;
        --ultrafast)
        ULTRAFAST_MODE=true
        shift
        ;;
    esac
done

if [ "$ULTRAFAST_MODE" = false ]; then
    if [ "$FAST_MODE" = false ]; then
        # Create the output directory if it doesn't exist
        if [ ! -d "$WORKPATH" ]; then
            mkdir $WORKPATH
        fi
        # Clean up previous builds
        rm -rf $BIN
        mkdir -p $BIN
    fi

    # Compile the Java source files
    echo "Compiling Java source files..."
    find src -name "*.java" > $WORKPATH/sources.txt
    javac --release 8 -Xlint:-options -d $BIN -cp "lib/*" @$WORKPATH/sources.txt
fi

# Create the MANIFEST.MF file
echo "Creating MANIFEST.MF file for $MAIN_CLASS..."
echo "Manifest-Version: 1.0" > $BIN/MANIFEST.MF
echo "Main-Class: $MAIN_CLASS" >> $BIN/MANIFEST.MF

if [ "$FAST_MODE" = false ] && [ "$ULTRAFAST_MODE" = false ]; then
    # Extract and deploy the JAR libraries
    cwd=$(pwd)
    echo "Extracting JAR libraries..."
    cd $BIN
    for jar in $cwd/lib/*.jar; do
        jar -xf $jar
    done
    echo "Copying resources..."
    cd $cwd
    cp /Users/nandoide/CODE/mpfr-4.2.1/src/.libs/libmpfr.6.dylib  /Users/nandoide/CODE/Fractal-Zoomer/src/fractalzoomer/native/general/darwin-aarch64
    cp /Users/nandoide/CODE/gmp-6.3.0/.libs/libgmp.10.dylib  /Users/nandoide/CODE/Fractal-Zoomer/src/fractalzoomer/native/general/darwin-aarch64

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
fi

# Package everything into a JAR file
echo "Creating JAR file..."
cd $BIN
jar cfm ../$JAR_NAME MANIFEST.MF .
cd ..

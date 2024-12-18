#!/bin/bash
#Ubuntu compilation
# Bind the destination to a shell variable
if command -v nproc &> /dev/null
then
    NPROC=$(nproc)
else
    NPROC=$(sysctl -n hw.ncpu)
fi

# rm -rf ~/CODE/out
mkdir ~/CODE/out
# Navigate to working folder
cd ~/CODE
# rm -rf gmp-6.3.0
# rm -rf mpfr-4.2.1
# rm gmp-6.3.0.tar.xz
# rm mpfr-4.2.1.tar.bz2

export MPDIR=~/CODE/out
# Download and unpack gmp
# curl -O -k https://gmplib.org/download/gmp/gmp-6.3.0.tar.xz
# tar Jxf gmp-6.3.0.tar.xz
# Download and unpack mpfr
#curl -O https://www.mpfr.org/mpfr-current/mpfr-4.2.1.tar.bz2
# cp ~/CODE/mpfr-4.2.1.tar.bz2 ./
# tar jxf mpfr-4.2.1.tar.bz2

# Obtener la ruta de instalación de libomp
LIBOMP_PREFIX=$(brew --prefix libomp)

export CFLAGS="-O2 -pedantic -fomit-frame-pointer -m64 -fPIC -fopenmp"
export PATH="${LIBOMP_PREFIX}/bin:$PATH"
export LDFLAGS="-L${LIBOMP_PREFIX}/lib"
export CPPFLAGS="-I${LIBOMP_PREFIX}/include"

cd ~/CODE/gmp-6.3.0

# ./configure --enable-static=yes --enable-shared=yes --with-pic=yes
# make clean
# make -j$NPROC


# cd ~/CODE/mpfr-4.2.1
# make clean
# ./configure  --enable-static=yes --enable-shared=yes --with-pic=yes --with-gmp=~/CODE/gmp-6.3.0
# make -j$NPROC
make check

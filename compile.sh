#!/bin/bash

LIBOMP_PREFIX=$(brew --prefix libomp)

export CFLAGS="-O2 -pedantic -fomit-frame-pointer -m64 -fPIC -fopenmp"
export PATH="/opt/homebrew/opt/llvm/bin:$PATH"
export LDFLAGS="-L/opt/homebrew/opt/llvm/lib"
export CPPFLAGS="-I/opt/homebrew/opt/llvm/include"
export LIBRARY_PATH="/opt/homebrew/opt/llvm/lib:$LIBRARY_PATH"
export CC="clang"

NPROC=$(sysctl -n hw.ncpu)

compile_gmp() {
    cd ~/CODE/gmp-6.3.0
    ./configure --enable-static=yes --enable-shared=yes --with-pic=yes
    make clean
    make -j$NPROC
}

compile_mpfr() {
    cd ~/CODE/mpfr-4.2.1
    ./configure --enable-static=yes --enable-shared=yes --with-pic=yes --with-gmp=~/CODE/gmp-6.3.0
    make clean
    make -j$NPROC
}

check_gmp() {
    cd ~/CODE/gmp-6.3.0
    make check
}

check_mpfr() {
    cd ~/CODE/mpfr-4.2.1
    make check
}

case "$1" in
    gmp)
        compile_gmp
        ;;
    mpfr)
        compile_mpfr
        ;;
    gmp_check)
        check_gmp
        ;;
    mpfr_check)
        check_mpfr
        ;;
    *)
        echo "Usage: $0 {gmp|mpfr|gmp_check|mpfr_check}"
        exit 1
        ;;
esac
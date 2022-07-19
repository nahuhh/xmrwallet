FROM debian:stretch

ARG THREADS=1
ARG ANDROID_NDK_REVISION=21d
ARG ANDROID_NDK_HASH=bcf4023eb8cb6976a4c7cff0a8a8f145f162bf4d
ARG ANDROID_SDK_REVISION=4333796
ARG ANDROID_SDK_HASH=92ffee5a1d98d856634e8b71132e8a95d96c83a63fde1099be3d86df3106def9

WORKDIR /opt/android
ENV WORKDIR=/opt/android

ENV ANDROID_NATIVE_API_LEVEL=28
ENV ANDROID_API=android-${ANDROID_NATIVE_API_LEVEL}
ENV ANDROID_CLANG=aarch64-linux-android${ANDROID_NATIVE_API_LEVEL}-clang
ENV ANDROID_CLANGPP=aarch64-linux-android${ANDROID_NATIVE_API_LEVEL}-clang++
ENV ANDROID_NDK_ROOT=${WORKDIR}/android-ndk-r${ANDROID_NDK_REVISION}
ENV ANDROID_SDK_ROOT=${WORKDIR}/tools
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
ENV PATH=${JAVA_HOME}/bin:${PATH}
ENV PREFIX=${WORKDIR}/prefix
ENV TOOLCHAIN_DIR=${ANDROID_NDK_ROOT}/toolchains/llvm/prebuilt/linux-x86_64

RUN set -x && apt-get update \
    && apt-get install -y ant automake build-essential file gettext git libc6 libncurses5 \
    libssl-dev libstdc++6 libtinfo5 libtool libz1 pkg-config python3 \
    unzip curl wget

# RUN set -x && apt-get update && apt-get install -y unzip automake build-essential curl file pkg-config git python libtool libtinfo5

WORKDIR /opt/android
## INSTALL ANDROID SDK
RUN set -x \
    && curl -s -O https://dl.google.com/android/repository/sdk-tools-linux-${ANDROID_SDK_REVISION}.zip \
    && unzip -q sdk-tools-linux-${ANDROID_SDK_REVISION}.zip \
    && rm -f sdk-tools-linux-${ANDROID_SDK_REVISION}.zip

## INSTALL ANDROID NDK
RUN set -x \
    && curl -s -O https://dl.google.com/android/repository/android-ndk-r${ANDROID_NDK_REVISION}-linux-x86_64.zip \
    && unzip -q android-ndk-r${ANDROID_NDK_REVISION}-linux-x86_64.zip \
    && rm -f android-ndk-r${ANDROID_NDK_REVISION}-linux-x86_64.zip

RUN cd ${ANDROID_SDK_ROOT} && echo y | ./bin/sdkmanager "platform-tools" "platforms;${ANDROID_API}" "tools" > /dev/null
RUN cp -r ${WORKDIR}/platforms ${WORKDIR}/platform-tools ${ANDROID_SDK_ROOT}

ENV HOST_PATH=${PATH}
ENV PATH=${TOOLCHAIN_DIR}/aarch64-linux-android/bin:${TOOLCHAIN_DIR}/bin:${PATH}

# download, configure and make Zlib
ENV ZLIB_VERSION 1.2.12
ENV ZLIB_HASH 91844808532e5ce316b3c010929493c0244f3d37593afd6de04f71821d5136d9
RUN set -x \
    && curl -s -O https://zlib.net/zlib-${ZLIB_VERSION}.tar.gz \
    && tar -xzf zlib-${ZLIB_VERSION}.tar.gz \
    && rm zlib-${ZLIB_VERSION}.tar.gz \
    && cd zlib-${ZLIB_VERSION} \
    && CC=${ANDROID_CLANG} CXX=${ANDROID_CLANGPP} ./configure --prefix=${PREFIX} --static \
    && make -j${THREADS} \
    && make -j${THREADS} install \
    && rm -rf $(pwd)

# Build iconv for lib boost locale
ARG ICONV_VERSION=1.16
ARG ICONV_HASH=e6a1b1b589654277ee790cce3734f07876ac4ccfaecbee8afa0b649cf529cc04
RUN set -x \
    && curl -s -O http://ftp.gnu.org/pub/gnu/libiconv/libiconv-${ICONV_VERSION}.tar.gz \
    && echo "${ICONV_HASH}  libiconv-${ICONV_VERSION}.tar.gz" | sha256sum -c \
    && tar -xzf libiconv-${ICONV_VERSION}.tar.gz \
    && rm -f libiconv-${ICONV_VERSION}.tar.gz \
    && cd libiconv-${ICONV_VERSION} \
    && CC=${ANDROID_CLANG} CXX=${ANDROID_CLANGPP} ./configure --build=x86_64-linux-gnu --host=aarch64 --prefix=${PREFIX} --disable-rpath \
    && make -j${THREADS} \
    && make -j${THREADS} install

## Boost
ARG BOOST_VERSION=1_74_0
ARG BOOST_VERSION_DOT=1.74.0
ARG BOOST_HASH=83bfc1507731a0906e387fc28b7ef5417d591429e51e788417fe9ff025e116b1
RUN set -x \
    && curl -s -L -o  boost_${BOOST_VERSION}.tar.bz2 https://downloads.sourceforge.net/project/boost/boost/${BOOST_VERSION_DOT}/boost_${BOOST_VERSION}.tar.bz2 \
    && echo "${BOOST_HASH}  boost_${BOOST_VERSION}.tar.bz2" | sha256sum -c \
    && tar -xvf boost_${BOOST_VERSION}.tar.bz2 \
    && rm -f boost_${BOOST_VERSION}.tar.bz2 \
    && cd boost_${BOOST_VERSION} \
    && ./bootstrap.sh --prefix=${PREFIX}

## Build BOOST
RUN set -x \
    && PATH=${TOOLCHAIN_DIR}/bin:${HOST_PATH} ./b2 --build-type=minimal link=static runtime-link=static \
    --with-chrono --with-date_time --with-filesystem --with-program_options --with-regex --with-serialization \
    --with-system --with-thread --with-locale --build-dir=android --stagedir=android toolset=clang threading=multi \
    threadapi=pthread target-os=android -sICONV_PATH=${PREFIX} \
    cflags='--target=aarch64-linux-android' \
    cxxflags='--target=aarch64-linux-android' \
    linkflags='--target=aarch64-linux-android --sysroot=${ANDROID_NDK_ROOT}/platforms/${ANDROID_API}/arch-arm64 ${ANDROID_NDK_ROOT}/sources/cxx-stl/llvm-libc++/libs/arm64-v8a/libc++_shared.so -nostdlib++' \
    install -j${THREADS} \
    && rm -rf $(pwd)

ARG OPENSSL_VERSION=1.1.1g
ARG OPENSSL_HASH=ddb04774f1e32f0c49751e21b67216ac87852ceb056b75209af2443400636d46
RUN set -x \
    && curl -s -O https://www.openssl.org/source/openssl-${OPENSSL_VERSION}.tar.gz \
    && tar -xzf openssl-${OPENSSL_VERSION}.tar.gz \
    && rm openssl-${OPENSSL_VERSION}.tar.gz \
    && cd openssl-${OPENSSL_VERSION} \
    && ANDROID_NDK_HOME=${ANDROID_NDK_ROOT} ./Configure CC=${ANDROID_CLANG} CXX=${ANDROID_CLANGPP} \
    android-arm64 no-asm no-shared --static \
    --with-zlib-include=${PREFIX}/include --with-zlib-lib=${PREFIX}/lib \
    --prefix=${PREFIX} --openssldir=${PREFIX} \
    && sed -i 's/CNF_EX_LIBS=-ldl -pthread//g;s/BIN_CFLAGS=-pie $(CNF_CFLAGS) $(CFLAGS)//g' Makefile \
    && ANDROID_NDK_HOME=${ANDROID_NDK_ROOT} make -j${THREADS} \
    && make -j${THREADS} install \
    && rm -rf $(pwd)

# ZMQ
ARG ZMQ_VERSION=v4.3.3
ARG ZMQ_HASH=04f5bbedee58c538934374dc45182d8fc5926fa3
RUN set -x \
    && git clone https://github.com/zeromq/libzmq.git -b ${ZMQ_VERSION} --depth 1 \
    && cd libzmq \
    && git checkout ${ZMQ_HASH} \
    && ./autogen.sh \
    && CC=${ANDROID_CLANG} CXX=${ANDROID_CLANGPP} ./configure --prefix=${PREFIX} --host=aarch64-linux-android \
    --enable-static --disable-shared \
    && make -j${THREADS} \
    && make -j${THREADS} install \
    && rm -rf $(pwd)

ARG SODIUM_VERSION=1.0.18
ARG SODIUM_HASH=4f5e89fa84ce1d178a6765b8b46f2b6f91216677
RUN set -ex \
    && git clone https://github.com/jedisct1/libsodium.git -b ${SODIUM_VERSION} --depth 1 \
    && cd libsodium \
    && test `git rev-parse HEAD` = ${SODIUM_HASH} || exit 1 \
    && ./autogen.sh \
    && CC=${ANDROID_CLANG} CXX=${ANDROID_CLANGPP} ./configure --prefix=${PREFIX} --host=aarch64-linux-android --enable-static --disable-shared \
    && make -j${THREADS} install \
    && rm -rf $(pwd)

RUN set -x \
    && cd tools \
    && wget -q http://dl-ssl.google.com/android/repository/tools_r25.2.5-linux.zip \
    && unzip -q tools_r25.2.5-linux.zip \
    && rm -f tools_r25.2.5-linux.zip \
    && echo y | ${ANDROID_SDK_ROOT}/tools/android update sdk --no-ui --all --filter build-tools-28.0.3

RUN set -x \
    && git clone -b v3.19.7 --depth 1 https://github.com/Kitware/CMake \
    && cd CMake \
    && git reset --hard 22612dd53a46c7f9b4c3f4b7dbe5c78f9afd9581 \
    && PATH=${HOST_PATH} ./bootstrap \
    && PATH=${HOST_PATH} make -j${THREADS} \
    && PATH=${HOST_PATH} make -j${THREADS} install \
    && rm -rf $(pwd)


ENV HOST_PATH $PATH
ENV PATH $TOOLCHAIN_DIR/aarch64-linux-android/bin:$TOOLCHAIN_DIR/bin:$PATH

COPY . /src
ARG NPROC=4
RUN set -x \
    && cd /src \
    && CMAKE_INCLUDE_PATH="${PREFIX}/include" \
       CMAKE_LIBRARY_PATH="${PREFIX}/lib" \
       ANDROID_STANDALONE_TOOLCHAIN_PATH=${TOOLCHAIN_DIR} \
       USE_SINGLE_BUILDDIR=1 \
       PATH=${HOST_PATH} make release-static-android-armv8-wallet_api -j${NPROC}

RUN set -x \
    && cd /src/build/release \
    && find . -path ./lib -prune -o -name '*.a' -exec cp '{}' lib \;

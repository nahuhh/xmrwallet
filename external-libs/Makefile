FLAVOUR=wownero

all: arm64-v8a x86_64 include VERSION

arm64-v8a: $(FLAVOUR)
	-rm -rf arm64-v8a
	-docker container rm $(FLAVOUR)-android-arm64 -f
	docker build -f android64.Dockerfile -t $(FLAVOUR)-android-arm64 $(FLAVOUR)
	docker create -it --name $(FLAVOUR)-android-arm64 $(FLAVOUR)-android-arm64 bash
	docker cp $(FLAVOUR)-android-arm64:/opt/android/prefix/lib/. arm64-v8a/
	docker cp $(FLAVOUR)-android-arm64:/src/build/release/lib/. arm64-v8a//$(FLAVOUR)

armeabi-v7a: $(FLAVOUR)
	-rm -rf armeabi-v7a
	-docker container rm $(FLAVOUR)-android-arm32 -f
	docker build -f android32.Dockerfile -t $(FLAVOUR)-android-arm32 $(FLAVOUR)
	docker create -it --name $(FLAVOUR)-android-arm32 $(FLAVOUR)-android-arm32 bash
	docker cp $(FLAVOUR)-android-arm32:/opt/android/prefix/lib/. armeabi-v7a
	docker cp $(FLAVOUR)-android-arm32:/src/build/release/lib/. armeabi-v7a/$(FLAVOUR)

x86_64: $(FLAVOUR)
	-rm -rf x86_64
	-docker container rm $(FLAVOUR)-android-x86_64 -f
	docker build -f android64_x86.Dockerfile -t $(FLAVOUR)-android-x86_64 $(FLAVOUR)
	docker create -it --name $(FLAVOUR)-android-x86_64 $(FLAVOUR)-android-x86_64 bash
	docker cp $(FLAVOUR)-android-x86_64:/opt/android/prefix/lib/. x86_64
	docker cp $(FLAVOUR)-android-x86_64:/src/build/release/lib/. x86_64/$(FLAVOUR)

x86: $(FLAVOUR)
	-rm -rf x86
	-docker container rm $(FLAVOUR)-android-x86 -f
	docker build -f android32_x86.Dockerfile -t $(FLAVOUR)-android-x86 $(FLAVOUR)
	docker create -it --name $(FLAVOUR)-android-x86 $(FLAVOUR)-android-x86 bash
	docker cp $(FLAVOUR)-android-x86:/opt/android/prefix/lib/. x86
	docker cp $(FLAVOUR)-android-x86:/src/build/release/lib/. x86/$(FLAVOUR)

include: $(FLAVOUR)
	-mkdir include
	cp $(FLAVOUR)/src/wallet/api/wallet2_api.h include/

VERSION:
	echo MONERUJO_WOW `git -C . branch | grep "^\*" | sed 's/^..//'` with $(FLAVOUR) `git -C $(FLAVOUR) branch | grep "^\*" | sed 's/^..//'` > VERSION

clean:
	-rm -rf arm64-v8a
	-rm -rf armeabi-v7a
	-rm -rf x86_64
	-rm -rf x86
	-rm -rf include
	-rm -f VERSION

distclean: clean
	-docker container rm $(FLAVOUR)-android-arm64 -f
	-docker container rm $(FLAVOUR)-android-arm32 -f
	-docker container rm $(FLAVOUR)-android-x86_64 -f
	-docker container rm $(FLAVOUR)-android-x86 -f
	-docker image rm $(FLAVOUR)-android-arm64 -f
	-docker image rm $(FLAVOUR)-android-arm32 -f
	-docker image rm $(FLAVOUR)-android-x86_64 -f
	-docker image rm $(FLAVOUR)-android-x86 -f
	-rm $(FLAVOUR)

$(FLAVOUR):
	$(error Please ln -s $(FLAVOUR))

.PHONY: all clean distclean

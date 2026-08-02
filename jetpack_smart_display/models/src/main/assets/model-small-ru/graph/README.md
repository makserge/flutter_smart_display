# Reduced small model for Vosk (Kaldi based) howto

Based on info from https://habr.com/ru/articles/735480/

1. Download stable Ubuntu 24.04 (ubuntu-24.04.4-live-server-amd64.iso) from official repository
   https://ubuntu.com/download/server/thank-you?version=24.04.4&architecture=amd64&lts=true
2. Create VirtualBox machine with 16GB RAM, 7CPU, 70GB storage, set first network adapter to bridge mode on host machine
3. Start VM
4. Select "English"
5. Select "Continue without updating"
6. Select "Done"
7. Select "Ubuntu Server (minimized)" and press "Done"
8. Select "Done"
9. Select "Done"
10. Select "Done"
11. Select "Done"
12. Select "Done"
13. Select "Continue"
14. Enter name, server name, username, password and with "Tab" button select "Done"
15. Select "Continue"
16. Select "Install OpenSSH server" and with "Tab" button select "Done"
17. Select "Done"
18. Select "Reboot now"
19. Set VM Adapter 1 network to "Bridged Adapter" (VirtualBox: Settings → Network →
    Adapter 1 → Attached to: Bridged Adapter; VM must be powered off to change this)
20. Login to VM as user and find current IP via "ip a"
21. Connect via SSH as regular user using this IP

ssh user@host

22. Install dependencies

    sudo apt update
    sudo apt upgrade -y
    sudo apt install unzip git clang make automake sox gfortran libtool subversion g++ zlib1g-dev sudo gawk python3-pip -y

23. Get small model

    mkdir new-model && cd new-model
    wget "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip"
    unzip vosk-model-small-ru-0.22.zip

24. Get Kaldi

    git clone https://github.com/kaldi-asr/kaldi
    cd kaldi

25. Install extras

    cd tools
    sudo extras/install_mkl.sh

26. Check Kaldi dependencies

    extras/check_dependencies.sh

27. Build tools

    make -j 7

28. Install more extras

    rm -rf ngram-1.3.7 ngram-1.3.7.tar.gz
    sed -i 's/1.3.7/1.3.16/g' extras/install_opengrm.sh
    extras/install_opengrm.sh

    (if needed — alphacep OpenFST fork instead of stock OpenFST, before install_opengrm.sh above)

    git clone https://github.com/alphacep/openfst openfst-ac
    rm -rf openfst
    ln -sf openfst-ac openfst

    (if needed — fstproject flag fix for compile package, only if you hit an fstproject error in step 36)

    cd vosk-model-small-ru-0.22-compile
    grep -rl -- '--project_output=true' . | xargs -r sed -i 's/--project_output=true/--project_type=output/g'
    cd ..

29. Install SRILM

    extras/install_srilm.sh "Your Name" "Your Organization" "your@email.com" "Your Address"

30. Build Kaldi src

    cd ../src
    ./configure --shared
    make depend -j 7
    make -j 7

31. Update language model

    cd ../egs/wsj/s5
    wget "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22-compile.zip"
    rm -rf ./data
    rm -rf ./db
    rm -rf ./exp
    rm -rf ./model-out
    rm -rf ./steps
    rm -rf ./utils
    unzip -o vosk-model-small-ru-0.22-compile.zip
    mv -f vosk-model-small-ru-0.22-compile/* .

32. Edit kaldi/egs/wsj/s5/db/ru-250k.dic to remove words

    cd ~/new-model/kaldi/egs/wsj/s5
    cp db/ru-250k.dic db/ru-250k.dic.bak

33. Append kaldi/tools/env.sh to the end of kaldi/egs/wsj/s5/path.sh

    grep KALDI_ROOT path.sh
    sed -i "s|^export KALDI_ROOT=.*|export KALDI_ROOT=$HOME/new-model/kaldi|" path.sh
    grep KALDI_ROOT path.sh

    cat ../../../tools/env.sh >> path.sh

    Resulting kaldi/egs/wsj/s5/path.sh:

    export KALDI_ROOT=$HOME/new-model/kaldi
    export PATH=$PWD/utils:$KALDI_ROOT/src/bin:$KALDI_ROOT/tools/openfst/bin:$KALDI_ROOT/src/fstbin:$KALDI_ROOT/src/gmmbin:$KALDI_ROOT/src/featbin:$KALDI_ROOT/src/lm:$KALDI_ROOT/src/sgmmbin:$KALDI_ROOT/src/sgmm2bin:$KALDI_ROOT/src/fgmmbin:$KALDI_ROOT/src/latbin:$KALDI_ROOT/src/nnetbin:$KALDI_ROOT/src/nnet2bin:$KALDI_ROOT/src/online2bin:$KALDI_ROOT/src/ivectorbin:$KALDI_ROOT/src/lmbin:$KALDI_ROOT/src/chainbin:$KALDI_ROOT/src/nnet3bin:$PWD:$PATH:$KALDI_ROOT/tools/sph2pipe_v2.5
    export PATH=$PATH:$PWD/utils:$PWD/../tools/openfst/bin:$PWD/../tools/fst/bin
    export PATH=$KALDI_ROOT/tools/ngram-1.3.16/src/bin:$PATH
    export LD_LIBRARY_PATH=$KALDI_ROOT/tools/openfst/lib/fst

    export LC_ALL=C

    export PATH=$KALDI_ROOT/tools/python:${PATH}
    export IRSTLM=$KALDI_ROOT/tools/irstlm
    export PATH=${PATH}:${IRSTLM}/bin
    export LIBLBFGS=$KALDI_ROOT/tools/liblbfgs-1.10
    export LD_LIBRARY_PATH=${LD_LIBRARY_PATH:-}:${LIBLBFGS}/lib/.libs
    export SRILM=$KALDI_ROOT/tools/srilm
    export PATH=${PATH}:${SRILM}/bin:${SRILM}/bin/i686-m64

34. Load path.sh into your current shell

    source path.sh

35. Install phonetisaurus

    sudo pip install phonetisaurus --break-system-packages

36. Generate model

    ./compile-graph.sh

37. Copy generated files into the deployable model

    compile-graph.sh already copied Gr.fst, HCLr.fst, and disambig_tid.int into
    kaldi/egs/wsj/s5/model-out/vosk-model-small-ru-0.22/graph/. Copy them from
    there into the actual model directory unzipped in step 23:

    mkdir -p ~/new-model/vosk-model-small-ru-0.22/graph
    cp ~/new-model/kaldi/egs/wsj/s5/model-out/vosk-model-small-ru-0.22/graph/Gr.fst \
    ~/new-model/kaldi/egs/wsj/s5/model-out/vosk-model-small-ru-0.22/graph/HCLr.fst \
    ~/new-model/kaldi/egs/wsj/s5/model-out/vosk-model-small-ru-0.22/graph/disambig_tid.int \
    ~/new-model/vosk-model-small-ru-0.22/graph/
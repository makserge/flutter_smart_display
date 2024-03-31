# Reduced small model for Vosk (Kaldi based) howto

Based on info from https://habr.com/ru/articles/735480/

0. Download stable Debian 12.4 (debian-12.4.0-amd64-netinst.iso) from official repository
     https://www.debian.org/CD/http-ftp/#stable
1. Create VirtualBox machine with 16GB RAM, 4CPU, 50GB storage, set first network adapter to bridge mode with ethernet adapter on host machine
2. Run VM and select "Install" option
3. Select "English" and press "Enter"
4. Select "United States" and press "Enter"
5. Select "American English" and press "Enter"
6. Enter hostname and with "Tab" button select "Continue"
7. Leave domain name empty and with "Tab" button select "Continue"
8. Set root password and with "Tab" button select "Continue"
9. Reenter root password and with "Tab" button select "Continue"
10. Set full name for non-root user and with "Tab" button select "Continue"
11. Set username for non-root user and with "Tab" button select "Continue"
12. Set password for non-root user and with "Tab" button select "Continue"
13. Reenter password and with "Tab" button select "Continue"
14. Set timezone and press "Enter"
15. Set "Guided - use entire disk" and press "Enter"
16. Press "Enter"
17. Press "Enter"
18. Press "Enter"
19. Select "Yes" and press "Enter"
20. Press "Enter"
21. Press "Enter"
22. Press "Enter"
23. Press "Tab" button and select "Continue"
24. Press "Enter"
25. Select only "SSH server" and with "Tab" button select "Continue"
26. Press "Enter"
27. Select "/dev/sda" and press "Enter"
28. Press "Enter"
29. Connect via SSH as regular user

ssh user@host

Accept connection

30. Change user to root

    su -

31. Install dependencies

    apt update
    apt upgrade
    apt install unzip git clang make automake sox gfortran libtool subversion g++ zlib1g-dev sudo gawk pip -y

32. Add user to sudoers 

    usermod -aG sudo username

33. Install python2.7

    mkdir python2.7
    cd python2.7
    wget http://ftp.debian.org/debian/pool/main/p/python2.7/python2.7_2.7.18-8+deb11u1_amd64.deb
    wget http://ftp.debian.org/debian/pool/main/p/python2.7/python2.7-minimal_2.7.18-8+deb11u1_amd64.deb
    wget http://ftp.debian.org/debian/pool/main/p/python2.7/libpython2.7-stdlib_2.7.18-8+deb11u1_amd64.deb
    wget http://ftp.debian.org/debian/pool/main/o/openssl/libssl1.1_1.1.1w-0+deb11u1_amd64.deb
    wget http://ftp.debian.org/debian/pool/main/libf/libffi/libffi7_3.3-6_amd64.deb
    wget http://ftp.debian.org/debian/pool/main/p/python2.7/libpython2.7-minimal_2.7.18-8+deb11u1_amd64.deb

    dpkg -i libffi7_3.3-6_amd64.deb libssl1.1_1.1.1w-0+deb11u1_amd64.deb libpython2.7-minimal_2.7.18-8+deb11u1_amd64.deb python2.7-minimal_2.7.18-8+deb11u1_amd64.deb libpython2.7-stdlib_2.7.18-8+deb11u1_amd64.deb python2.7_2.7.18-8+deb11u1_amd64.deb  
    
34. Return back to regular user
    exit

35. Get small model

    mkdir new-model && cd new-model
    wget "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip"
    unzip vosk-model-small-ru-0.22.zip

36. Get Kaldi

    git clone https://github.com/kaldi-asr/kaldi
    cd kaldi
    
37. Install extras

    cd tools
    sudo extras/install_mkl.sh -sp debian intel-mkl-64bit-2020.0-088

38. Check Kaldi dependencies
    
    extras/check_dependencies.sh

    extras/check_dependencies.sh: python2.7 is installed, but the python2 binary does not exist. Creating a symlink and adding this to tools/env.sh
    extras/check_dependencies.sh: all OK.
    
40. Install tools
        
    make -j 4

41. Install more extras

    extras/install_irstlm.sh
    extras/install_opengrm.sh
    
42. Install SRILM

    wget https://github.com/weimeng23/SRILM/raw/master/srilm-1.7.3.tar.gz
    mv srilm-1.7.3.tar.gz srilm.tar.gz
    extras/install_srilm.sh
    
43. Install tools

    cd ../src
    ./configure --shared
    make depend -j 4
    make -j 4

44. Update language model

    cd ../egs/wsj/s5    

    wget "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22-compile.tar.gz"
    tar -xf vosk-model-small-ru-0.22-compile.tar.gz
    mv vosk-model-small-ru-0.22-compile/* .

45. Edit kaldi/egs/wsj/s5/db/ru-250k.dic by removing some words

46. Copy all content of kaldi/tools/env.sh and paste to the end of kaldi/egs/wsj/s5/path.sh

     Example of kaldi/egs/wsj/s5/path.sh:

export KALDI_ROOT=`pwd`/../../..
export PATH=$PWD/utils:$KALDI_ROOT/src/bin:$KALDI_ROOT/tools/openfst/bin:$KALDI_ROOT/src/fstbin:$KALDI_ROOT/src/gmmbin:$KALDI_ROOT/src/featbin:$KALDI_ROOT/src/lm:$KALDI_ROOT/src/sgmmbin:$KALDI_ROOT/src/sgmm2bin:$KALDI_ROO$
export PATH=$KALDI_ROOT/tools/ngram-1.3.7/src/bin:$PATH
export LD_LIBRARY_PATH=$KALDI_ROOT/tools/openfst/lib/fst/
export LC_ALL=C

export PATH=/home/sergey/new-model/kaldi/tools/python:${PATH}
export IRSTLM=/home/sergey/new-model/kaldi/tools/irstlm
export PATH=${PATH}:${IRSTLM}/bin
export LIBLBFGS=/home/sergey/new-model/kaldi/tools/liblbfgs-1.10
export LD_LIBRARY_PATH=${LD_LIBRARY_PATH:-}:${LIBLBFGS}/lib/.libs
export SRILM=/home/sergey/new-model/kaldi/tools/srilm
export PATH=${PATH}:${SRILM}/bin:${SRILM}/bin/i686-m64

47. Run this file

    ./path.sh

48. Install phonetisaurus

    sudo pip install phonetisaurus --break-system-packages

49. Generate model

    ./compile-graph.sh

50. Copy generated files

kaldi/egs/wsj/s5/exp/tdnn/lgraph/Gr.fst
kaldi/egs/wsj/s5/exp/tdnn/lgraph/HCLr.fst

to new-model/vosk-model-small-ru-0.22/graph
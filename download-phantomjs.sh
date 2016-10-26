wget https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2
bzip2 -d phantomjs-2.1.1-linux-x86_64.tar.bz2
tar xvf phantomjs-2.1.1-linux-x86_64.tar 
rm phantomjs-2.1.1-linux-x86_64.tar

mkdir phantomjs
cp phantomjs-2.1.1-linux-x86_64/bin/phantomjs phantomjs/
rm -r phantomjs-2.1.1-linux-x86_64/

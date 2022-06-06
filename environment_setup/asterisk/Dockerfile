FROM debian:buster-slim

RUN apt update && apt install -y --no-install-recommends git build-essential devscripts

RUN git clone --branch 19.2 --depth 1 https://github.com/asterisk/asterisk.git /usr/local/share/asterisk

RUN /usr/local/share/asterisk/contrib/scripts/install_prereq install

RUN cd /usr/local/share/asterisk && ./configure && make && make install && make sounds

CMD ["/usr/sbin/asterisk", "-T", "-qfvvvv"]

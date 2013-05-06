# Requirements: python; python-cheetah; bash
#
# Generate all of the configuration needed tu run a Zookeeper ensemble (wrapper of the zkconf and zktop scripts)
# The scripts and configuration are generated in the directory "./zookeeper/".
# The configuration of the ensemble is defined in the variable ZOOKEEPER_CONFIG ([options] to zkconf).
#
# Examples:
# 9 servers:
# 	ZOOKEEPER_CONFIG = --count 9 
# 9 servers and 3 groups
#	ZOOKEEPER_CONFIG = -c 9 --weights="1,1,1,1,1,0,0,0,0" --groups="1:2:3:4:5,6:7,8:9"
#
# scripts in "./zookeeper/":
#
# cli.sh “server:port,server:port,…” – open a client to the server list
# status.sh – status of each of the servers (prints leader | follower if active)
# start.sh – start the ensemble (logs are output to the respective server subdir, localhost only)
# stop.sh – stop the ensemble (localhost only)
# top.sh --servers "server:port,server:port,server:port" – unix “top” like utility for ZooKeeper(zktop)

ZOOKEEPER_CONFIG = --count 9

ZOOKEEPER_DIR = ./zookeeper-3.4.5/

ZKCONF_DIR = ./zkconf-master/
ZKCONF_BIN = ./zkconf-master/zkconf.py

ZKCONF_SRC = $(wildcard ./zkconf-master/*.tmpl)
ZKCONF_OBJ = $(patsubst %.tmpl,%.py,$(ZKCONF_SRC))

ZKTOP_BIN = ./../zktop-master/
ZKTOP_BIN = ./../zktop-master/zktop.py

.PHONY : zookeeper clean all

all: zookeeper

$(ZKCONF_OBJ): $(patsubst %.py,%.tmpl,$@)
	@cheetah compile $(patsubst %.py,%.tmpl,$@) > /dev/null

zkconf: $(ZKCONF_OBJ)
	@echo "Compiling zkconf..."

zookeeper: zkconf
	@echo "Generating zookeeper ensemble configuration and scripts..."
	@rm -R -f zookeeper/
	@$(ZKCONF_BIN) $(ZOOKEEPER_CONFIG) $(ZOOKEEPER_DIR) $@
	@echo "./../zktop-master/zktop.py \$$@" > $@/top.sh
	@chmod +x $@/top.sh

clean:
	@echo "Deleting zookeeper ensemble files..."
	@rm -R -f zookeeper/
	@rm -f $(ZKCONF_OBJ)


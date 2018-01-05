#!/bin/bash

cd ..
java -server -cp .:bin:libs/* nl.liacs.subdisc.gui.SubDisc
cd batch


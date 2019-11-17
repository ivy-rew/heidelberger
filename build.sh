#!/bin/bash

mvn clean verify
 
pandoc -o target/heidelberger.epub extract.html

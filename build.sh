#!/bin/bash

mvn clean verify

pandoc  --epub-chapter-level=2\
 -f html\
 -o target/heidelberger.epub target/extract.html

ebook-viewer target/heidelberger.epub
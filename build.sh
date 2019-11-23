#!/bin/bash

#mvn clean verify

pandoc --atx-headers\
 -f html\
 -o target/heidelberger.md target/extract.html

# poor mans approach to turn escaped (whitespace pre-fixed) footnotes into real ones
sed -i "s/\s\{4,\}\[/\[/g" target/heidelberger.md
sed -i "s/\\\//g" target/heidelberger.md

pandoc --toc --epub-chapter-level=2\
 -o target/heidelberger.epub target/heidelberger.md

ebook-viewer target/heidelberger.epub
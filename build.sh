#!/bin/bash

mvn clean test

pandoc --atx-headers\
 -f html\
 -o target/heidelberger.md target/extract.html

# poor mans approach to turn escaped (whitespace pre-fixed) footnotes into real ones
sed -i "s/\s\{4,\}\[/\[/g" target/heidelberger.md
sed -i "s/\\\//g" target/heidelberger.md

# https://reformedforum.org/wp-content/blogs.dir/1/files/2012/03/Heidelberger_Katechismus_1563.jpg
pandoc --toc --epub-chapter-level=2\
 --epub-cover-image=Heidelberger_Katechismus_1563.jpg\
 -o target/heidelberger.epub\
 bookMeta.md target/heidelberger.md

ebook-viewer target/heidelberger.epub

# kindle: use azw3 to show footnotes as popup:
# ebook-convert target/heidelberger.epub target/heidelberger.azw3
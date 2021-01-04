# PDF-Zensor
!![Tests](https://github.com/XxHalbfettxX/pdf-zensor/workflows/Build%20&%20Tests/badge.svg)

PDF-Zensor can be used to censor PDF-files. As such it strips annotations and metadata as  well  as  textual
and  graphical  content from the PDF-file. It can also partially censor PDF-files and highlight certain text
phrases.

The application comes with a set of predefined colors, however, individual colors  for  censoring  different
elements can be configured as well.

### Tech

PDF-Zensor uses a number of open source projects to work properly:

* [PDFBox] - The Apache PDFBox library is an open source Java tool for working with PDF documents.
* [Picocli] - Command line interface
* [Log4J] - Apache Log4j is a Java-based logging utility.
* [Jackson] - In computing, Jackson is a high-performance JSON processor for Java.
* [Apache Commons] - Apache Commons is an Apache project focused on all aspects of reusable Java components.

And of course PDF-Zensor itself is open source.

### Installation

PDFZensor requires Java >= 11

Install the PDF-Zensor(work in Progress)

```sh
$ wget https://git.se.uni-hannover.de/swp1920/pdfzensor-2/-/jobs/artifacts/63-deploy/raw/solution/target/pdfzensor_0.815_all.deb?job=deploy
$ sudo apt install ./pdfzensor_0.815_all.deb
```

alternative:

```sh
$ alias pdf-zensor='java -cp "pdfzensor-jar-with-dependencies.jar" de.uni_hannover.se.pdfzensor.App
```
to create a temporary alias "pdf-zensor" which is valid for the current shell session.

### Development

Want to contribute? Great!
Write a message!

### Todos

 - (Feature): Clipping of images and the like according to the current GraphicsContext
 - (Feature): Correctly censor inline drawings
 - (Feature): Remove watermark
 - (Feature): Correctly censor Chinese characters or similar
 - (Feature): Regex works across pages
 - (Feature): Detect line breaks
 - 🐞 ZCensoring of rotated text can be strange (since we merge text according to global coordinates and not according to local)
 - 🐞 Tokenizer cannot find tokens across the page boundary
 - 🐞 Annotations::getRect returns a wrong (?) Rectangle. Avoided by HighlightAnnotation::getQuads
 - 🐞 EOFException instead of a FileFormatException if no valid PDF was entered [error in PDFBox]

License
----

GNU GPLv3


**Free Software, Hell Yeah!**

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)


   [PDFBox]: <https://pdfbox.apache.org>
   [Picocli]: <https://picocli.info>
   [Log4J]: <https://logging.apache.org/log4j/2.x/>
   [Jackson]: <https://github.com/FasterXML/jackson>
   [Apache Commons]: <https://commons.apache.org>

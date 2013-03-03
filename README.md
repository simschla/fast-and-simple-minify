fast-and-simple-minify
======================

In a nutshell: fast-and-simple-minify is a combined java-port of the [JSMin][jsmin] and [CSSMin][cssmin] utility with some additional features:

- A [command-line interface](#cli)
- An [Ant-Task implementation](#ant) for easy inclusion into an ant-build.

The minification performed by this utility is intended to reduce _unneeded chars_ from javascript and css files such as whitespace and code comments. It is by no means as 'perfect' as [YUI Compressor][yui] but it is waaaaaay faster and does the job good enough! Without any scientific data I'd say that fast-and-simple-minify takes 20% of the time YUI Compressor does and produces output that is 80% as good.

Using the <a name="cli"></a>command-line interface
------------------------------------------------
The command line interface allows to use fast-and-simple-minify as any other command-line tool. You can minify any css and js files by issuing some simple statements from the command-line.

In contrast to the original command-line tools [JSMin][jsmin] and [CSSMin][cssmin], fast-and-simple-minify is not only able to minify from stdin to stdout but can also read from a file and write to a file directly.

### System Requirement
To use fast-and-simple-minify's command-line interface [java version][jre] >= 1.6 is required.

### Command line usage
```
java -jar fast-and-simple-minify-1.0.jar [-format FORMAT] [-encoding ENCODING] [-comment COMMENT] [-out OUT_FILE] [IN_FILE]
```

All options in square brackets are optional and defined as follows:

*   `FORMAT` is one of `auto`, `css` or `js` (default: `auto`).
    * `auto` automatically selects the correct minify-tool based on the filenames to be minifed.  
      `auto` will fail when reading from stdin and writing to stdout due to no available filenames.
    * `css` uses the cssmin implementation to minify the input
    * `js` uses the jsmin implementation to minify the input
*   `ENCODING` is a charset to be used to encode the comment in the output (default: `UTF-8`)
*   `COMMENT` is a string that will be added to the beginning of all minified files (default: empty)
*   `OUT_FILE` is the file to write the minified content to (if this option is not set the output goes to stdout.)
*   `IN_FILE` is the file to be processed and minified (if this option is not set, the input is expected to be passed from stdin.)

### Examples

#### Reading a file and writing the minified result to a file

```Shell
java -jar fast-and-simple-minify-1.0.jar -out script-min.js script.js
```

This reads the file `script.js` and writes the output to `script-min.js`. fast-and-simple-minify automatically selects the correct minify implementation (jsmin) based on the filename.

#### Reading a file and write the result to stdout

```Shell
java -jar fast-and-simple-minify-1.0.jar style.css
```

This will read the file `style.css` and outputs the result to stdout. fast-and-simple-minify automatically selects the correct minify implementation (cssmin) based on the filename.

#### Reading multiple files and writing the result *using stdout* into one single output file 

```Shell
java -jar fast-and-simple-minify-1.0.jar style1.css > styles.css  
java -jar fast-and-simple-minify-1.0.jar style2.css >> styles.css  
java -jar fast-and-simple-minify-1.0.jar style3.css >> styles.css  
```

This will

1. read the file `style1.css` and write the minified output to stdout (which is redirected to file `styles.css`)
2. read the file `style2.css` and write the minified output to stdout (which is appended to the file `styles.css`)
3. read the file `style3.css` and write the minified output to stdout (which is appended to the file `styles.css`)

The result is a single file `styles.css` that holds the minified content of the three files `style1.css`, `style2.css` and `style3.css`.

#### Reading multiple files from stdin and writing to an output file

```Shell
cat style*.css | java -jar fast-and-simple-minify-1.0.jar -out allstyles-min.css
```

This will read all files matching the pattern `style*.css` (e.g. style1.css, style2.css, ...) and pass them to fast-and-simple-minify. The result will be written to the file `allstyles-min.css` using the automatically selected cssmin utility.

#### Reading a file, minify it and add a copyright header to the minified version.

```Shell
java -jar fast-and-simple-minify-1.0.jar -encoding ISO-8859-15 -comment "(c) 2013 by simschla" -out style-min.css style.css
```

This will read the file `style.css` and minify it to the output file `style-min.css`. To the beginning of the `style-min.css` the text "(c) 2013 by simschla" is written using the encoding `ISO-8859-15`.


Using the <a name="ant"></a> Ant-Integration 
------------------------------------------
Using the fast-and-simple-minify's `minify` ant task, you can enhance your ant build to very quickly minify css and js files in the process. The `minify` ant task has a very similar interface as ant's very own [copy][copytask]-task.

### Integrating the `minify`-ant-task into your ant build

There are several ways you can integrate the minify ant task into your ant build.

#### Integration without namespace prefixes
Using `taskdef` and `classpath` tasks without explicitly specifying the minify jar file:

```xml
<taskdef resource="ch/simschla/minify/ant/antlib.xml">
    <classpath>
        <fileset dir="${basedir}/path/to/where/the/jar/file/is" includes="fast-and-simple-minify*.jar"/>
    </classpath>
</taskdef>
```

or if you don't mind selecting the minify jar file explicitly this is even simpler:

```xml
<taskdef resource="ch/simschla/minify/ant/antlib.xml" classpath="path/to/fast-and-simple-minify-1.0.jar"/>
```

In both cases you can then use the `minify` ant task without any namespacing directly.

#### Integration with namespace-prefix

If you'd like to have a namespace for the minify task (for whatever reasons) you can do so. Integrate it as follows:

```xml
<project xmlns:simschla="antlib:ch.simschla.minify">
....
    <taskdef uri="antlib:ch.simschla.minify" resource="ch/simschla/minify/ant/antlib.xml" classpath="path/to/fast-and-simple-minify-1.0.jar/>
... 
</project>
```

Including it this way, you have to use the minify task with the `simschla`-namespace as follows:

```xml   
<target name="example">
     <simschla:minify todir="outdir" ...>
         ...
     </simschla:minify>
</target>
```

The namespace to use is up to you, so feel free to use a different one if you don't like mine.

The examples below use the minify task without a namespaces. If you decide to include the `minify` task with a namespace, they will work the same way, simply use the `minify` task with the namespace.

### Definition/API of the `minify` ant task

#### Description

Minifies css and js files. The task itself is very similar to ant's [copy][copytask] and has a very similar paremeter set.

By default, files are only minified if the destination file does not exist. However, you can explicitly overwrite files with the overwrite attribute.

[Resource Collections][antrescoll] are used to select a group of files to minify. To use a resource collection, the todir attribute must be set. *Note* that some resources (for example the [file][antresfile] resource) return absolute paths as names and the result of using them without using a nested mapper (or the flatten attribute) may not be what you expect.

#### Parameters

<table border="1" cellpadding="2" cellspacing="0">
    <tbody>
        <tr>
            <td valign="top">
                <b>
                    Attribute
                </b>
            </td>
            <td valign="top">
                <b>
                    Description
                </b>
            </td>
            <td align="center" valign="top">
                <b>
                    Required
                </b>
            </td>
        </tr>
        <tr>
            <td valign="top">
                file
            </td>
            <td valign="top">
                The file to minify.
            </td>
            <td valign="top" align="center">
                Yes, unless a nested resource collection element is used.
            </td>
        </tr>
        <tr>
            <td valign="top">
                tofile
            </td>
            <td valign="top">
                The file to write the minifed result to.
            </td>
            <td valign="top" align="center" rowspan="2">
                With the <code>file</code> attribute, either <code>tofile</code> or <code>todir</code>
                can be used.
                <br/>
                With nested resource collection elements, if the number of included resources
                is greater than 1, or if only the <code>dir</code>attribute is specified in the
                <code>&lt;fileset&gt;</code>, or if the <code>file</code> attribute is also specified, then only
                <code>todir</code>is allowed.
                <br/>
                <em>Prior to Apache Ant 1.8.2</em> the <code>tofile</code>
                attribute only supports filesystem resources to copy from.
            </td>
        </tr>
        <tr>
            <td valign="top">
                todir
            </td>
            <td valign="top">
                The directory to copy to.
            </td>
        </tr>
        <tr>
            <td valign="top">
                overwrite
            </td>
            <td valign="top">
                Overwrite existing files.
            </td>
            <td valign="top" align="center">
                No; defaults to false.
            </td>
        </tr>
        <tr>
            <td valign="top">
                flatten
            </td>
            <td valign="top">
                Ignore the directory structure of the source files, and write all minified files
                into the directory specified by the <code>todir</code>
                attribute. Note that you can achieve the same effect by using a
                <a href="http://ant.apache.org/manual/Types/mapper.html#flatten-mapper">flatten mapper</a>.
            </td>
            <td valign="top" align="center">
                No; defaults to false.
            </td>
        </tr>
        <tr>
            <td valign="top">
                failonerror
            </td>
            <td valign="top">
                If false, log a warning message, but do not stop the build, when the file
                to minify does not exist or one of the nested filesets points to a directory
                that doesn't exist or an error occurs while minifying.
            </td>
            <td valign="top" align="center">
                No; defaults to true.
            </td>
        </tr>
        <tr>
            <td valign="top">
                encoding
            </td>
            <td valign="top">
                The encoding to use when adding a custom <code>header</code> when minifying the files.
                <br/>
                This property has no effect when <code>header</code> is empty.
            </td>
            <td align="center">
                No - defaults to UTF-8.
            </td>
        </tr>
        <tr>
            <td valign="top">
                enablemultiplemappings
            </td>
            <td valign="top">
                If true the task will process to all the mappings for a given source path.
                If false the task will only process the first file or directory. This attribute
                is only relevant if there is a mapper subelement.
                <em>
                    since Ant 1.6
                </em>
                .
            </td>
            <td align="center">
                No - defaults to false.
            </td>
        </tr>
        <tr>
            <td valign="top">
                header
            </td>
            <td valign="top">
                A string to be written to the beginning of every minified file. A typical usecase
                is to write a copyright header here, since comments in the source files are removed by the
                minifying process.
            </td>
            <td align="center">
                No - defaults to no header.
            </td>
        </tr>
</table>

#### Parameters specified as nested elements

##### fileset or any other resource collection

[Resource Collections][antrescoll] are used to select groups of files to copy. To use a resource collection, the todir attribute must be set.

Prior to Ant 1.7 only <fileset> has been supported as a nested element.

##### mapper

You can define filename transformations by using a nested mapper element. The default mapper used by `<minify>` is the [identity mapper][antidmapp].

Since Ant 1.6.3, one can use a filenamemapper type in place of the mapper element.

Note that the source name handed to the mapper depends on the resource collection you use. If you use `<fileset>` or any other collection that provides a base directory, the name passed to the mapper will be a relative filename, relative to the base directory. In any other case the absolute filename of the source will be used.

### Usage examples of the `minify` ant task

These are usage examples of the minify task. The complete specification can be seen below.

#### Example 1: Minify one file to another file

```xml
<minify file="sample/sample1.css"
        tofile="output/sample1-min.css" />
```

This will minify the file `sample1.css` to the output file `sample1-min.css` by automatically selecting the minifier `cssmin` based on the filenames.

#### Example 2: Minify one file to another and add a copyright header

```xml
<minify header="(c) 2013 by simschla"
        file="sample/sample1.css"
        tofile="output/sample1-min.css" />
```

This will minify the file `sample1.css` to the output file `sample1-min.css` by automatically selecting the minifier `cssmin` based on the filenames. The output file `sample1-min.css` will start with the copyright header "(c) 2013 by simschla".

#### Example 3: Minify a set of files to another folder

```xml
<minify type="auto" todir="output/minified">
    <fileset dir="path/to/resources" includes="**/*.css, **/*.js" />
</minify>
```

This will minify all css and js files located in the folder `path/to/resources` to the folder `output/minified`. The minifier is automatically selected based on the name of the files, that are minified.

#### Example 4: Minify a set of files and rename them in the process

```xml
<minify type="css"
        todir="output/resources/styles"
        overwrite="true">
        <fileset dir="input/resources/styles" includes="*.css" />
        <regexpmapper from="^(.*)\.(.*)$" to="\1-min.\2" />
</minify>
```

This will minify all css files located in the `input/resources/styles` folder using cssmin. The output files are written to `output/resources/styles`. All files are renamed based on a regex that renames every `filexyz.css` to `filexyz-min.css`.

[jsmin]: https://github.com/douglascrockford/JSMin
[cssmin]: https://github.com/soldair/cssmin
[yui]: http://yui.github.com/yuicompressor/
[jre]: http://www.java.com
[copytask]: http://ant.apache.org/manual/Tasks/copy.html
[antrescoll]: http://ant.apache.org/manual/Types/resources.html#collection
[antresfile]: http://ant.apache.org/manual/Types/resources.html#file
[antidmapp]: http://ant.apache.org/manual/Types/mapper.html#identity-mapper


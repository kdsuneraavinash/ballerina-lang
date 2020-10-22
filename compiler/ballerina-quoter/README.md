# The _quoter_ tool

> Tool to generate the list of Ballerina Syntax API calls to build the given source code.

Ballerina syntax API, which is part of the overall compiler API, exposes Ballerina source code as a tree data structure, which is typically called the syntax tree. A syntax tree is an essential data structure in building compilers. It represents the source code's syntactic structure, therefore allowing the compiler to reason about the code's syntactic properties. 

The syntax API, which we recently introduced, has various use cases. It allows developer tools to view, add, update, or rearrange the source code programmatically. One example would be the formatter: It creates the syntax tree using the syntax API for the given source code and checks whether the source is formatted according to the rules; if not, it rearranges the code using the syntax API.   

Another use case is to add new source statements, functions, etc. to existing source code. To create your new statement, function, or record, you need to make various syntax API calls. These calls are somewhat repetitive and may not be straightforward to everyone at first. 

Therefore, this project aims to list the required API calls to create the given source code's syntax tree. This project will be useful for anyone who uses the Ballerina syntax API to create source code elements.

## How to use 

### Step 1:

Update the [`parameter-names.json`](src/main/resources/parameter-names.json) with the necessary changes. 
This file can be generated via the python script provided as the `generate-parameter-names.py`.

```bash
python generate-parameter-names.py
```

### Step 2:

Change the default properties in the [`quoter-config.properties`](src/main/resources/quoter-config.properties) to customize the output.

| Property Key | Default Value | Description|
|-|-|--|
|`external.input.file`| `input.bal` | Input source file |
|`external.output.file`| `output.txt` | Output file |
|`external.output.sys.out`| `false` | Whether to print out to the stdout as well. |
|`external.formatter.name`| `template` | Formatter to use. Choices: `template`, `variable`, `default`, `none` |
|`external.formatter.template`*| `template.txt` | Template file to use if `external.formatter.name` is `template` |
|`external.formatter.template.tab.start`| 2 | Initial indent of code if `external.formatter.name` is `template`|
|`internal.node.children`| `parameter-names.json` | JSON file containing the generated parameter names. This path points to the `resources` directory. |

If used with the template formatter, the code will get replaced on `%s` in the template file given.

### Step 3:

Put the source code in the input file. This is `input.bal` by default, but you may use any file name.

### Step 4:

Run the following command. This will generate the output at the given location.

```bash
./gradle quoter
```

When running the command you can give additional arguments to override the properties stated in the `quoter-config.properties`.

```bash
usage: ./gradlew quoter -Props="[OPTIONS]"
 -f,--formatter <arg>   formatter name (none,default,template,variable)
 -i,--input <arg>       input file path
 -o,--output <arg>      output file path
 -p,--position <arg>    tab position to start (applicable only in template
                        formatter)
 -s,--stdout            output to stdout
 -t,--template <arg>    template to use (applicable only in template
                        formatter)
```

#### Examples:

`./gradlew quoter -Props="-f default"` - Override and use default formatter

`./gradlew quoter -Props="-i code.bal -s true"` - Read from `code.bal` and output generated code to stdout as well  

`./gradlew quoter -Props="-f template -t Template.java -p 2"` - Use the template formatter with `Template.java` as template and 2 as the starting tab space
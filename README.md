## Xcelite

* [Xcelite project site](http://www.xcelite.io/)
* [Introduction](#introduction)
* [Quick Start](#quick-start)
  * [Writing](#writing)
  * [Reading](#reading)
* [Advanced Stuff](#advanced-stuff)
  * [Using Converters](#using-converters)
  * [Dynamic Columns](#dynamic-columns)
  * [Row Post Processors](#row-post-processors)
* [Utils](#utils)
  * [XceliteDiff](#xcelitediff)
* [How To Use?](#how-to-use)
  * [Using Xcelite in Your Maven Project](#using-xcelite-in-your-maven-project)
  * [Using Xcelite in Your Gradle Project](#using-xcelite-in-your-gradle-project)
  * [Using Xcelite with other build systems](#using-xcelite-with-other-build-systems)

### Introduction
[Xcelite](http://www.xcelite.io/) is an ORM like Java library which allows you to easily serialize and deserialize Java beans to/from Excel spreadsheets

### Quick Start
#### Writing
I simply want to write a two-dimensional collection. How can I do that?
```java
Xcelite xcelite = new Xcelite();    
XceliteSheet nativeSheet = xcelite.createSheet("data_sheet");
SheetWriter<Collection<Object>> simpleWriter = nativeSheet.getSimpleWriter();
List<Collection<Object>> data = new ArrayList<>();
// ...fill up data
simpleWriter.write(data);   
xcelite.write(new File("data.xlsx"));
```
This will create an excel document with single nativeSheet named "data_sheet".

OK lets get serious, i have this POJO bean
```java
public class User { 

  private String firstName;
  private String lastName;
  private long id; 
  private Date birthdate; 
}
```
How do i serialize a collection of this bean to excel?

First, lets add annotations
```java
public class User { 

  @Column (name="Firstname")
  private String firstName;
  
  @Column (name="Lastname")
  private String lastName;
  
  @Column
  private long id; 
  
  @Column
  private Date birthdate; 
}
```
The `@Column` annotation on a property indicates that you want it to be serialized to excel.  
By default, if no `name` attribute is provided the excel column name will be taken from the property name.

Now we'll write the same data as before but this time using `BeanWriter` writer instead of `SimpleWriter`:
```java
Xcelite xcelite = new Xcelite();    
XceliteSheet nativeSheet = xcelite.createSheet("users");
SheetWriter<User> writer = nativeSheet.getBeanWriter(User.class);
List<User> users = new ArrayList<>();
// ...fill up users
writer.write(users); 
xcelite.write(new File("users_doc.xlsx"));
```
This will create an Excel workbook containing one sheet named "users" with 4 columns plus header row: 
* Firstname 
* Lastname 
* id 
* birthdate  

Naturally, the excel column types will be Text for `FirstName` and `LastName`, Number for `id` and Date for `birthdate`.  
If you prefer that column `id`  should be written as Text instead of Number, use  

```java 
@Column(ignoreType=true)
private long id;
```
It is possible to control the data format that will be used when writing. For instance, Xcelite will use a default data format for "birthdate" date. In order to change the format, use
```java 
@Column(dataFormat="ddd mmm dd hh:mm:ss yyy")
private Date birthdate;
```
The data format is exacly as the same as used in Excel. It is recommended to check the format in Excel first before using it in your code.

Note that the excel columns order in this case is arbitrary. If you want to control the order of the columns use the @Row annotation on your bean class
```java
@Row(colsOrder = {"Firstname", "Lastname", "id", birthdate})
public class User {
// ...
}
```
#### Reading
How do I simply read an existing Excel nativeSheet to a two-dimensional collection?
```java
Xcelite xcelite = new Xcelite(new File("data.xlsx"));
XceliteSheet nativeSheet = xcelite.getSheet("data_sheet");
SheetReader<Collection<Object>> simpleReader = nativeSheet.getSimpleReader();
Collection<Collection<Object>> data = simpleReader.read();
```
If the first row in the nativeSheet is an header, you can skip it by doing:
```java
simpleReader.skipHeaderRow(true);
```

Cool! How about reading to a collection of Java beans?
```java
Xcelite xcelite = new Xcelite(new File("users_doc.xlsx"));
XceliteSheet nativeSheet = xcelite.getSheet("users");
SheetReader<User> reader = nativeSheet.getBeanReader(User.class);
Collection<User> users = reader.read();
```
Note that Xcelite will try to map only the @Column annotated properties. If no column found in the nativeSheet for an annotated property it will be ignored.  
Sheet columns which are not mapped to a @Column annotated property will be ignored as well.

### Advanced Stuff
#### Using Converters

Lets say your bean contains a list of values or some object of your own. By default, Xcelite will serialize the toString() of the object or list, and sometimes this might not be what you want.  
The converter mechanism allows you to serialize/deserialize the object in any way you want.  
To demostrate lets add a list to our `User` bean and use the built-in `CSVColumnValueConverter` converter:

```java
@Column(name = "Emails", converter = CSVColumnValueConverter.class)
private List<String> mailAddresses;
```
The `CSVColumnValueConverter` takes a collection of objects and serializes it to a comma-separated String.  
Alternately when deserializing, the converter takes a comma-separated and deserializes it to a collection of Objects.  
So writing a collection of users will result with a column named "Emails" and the column data will look someting like that:  
john@mail.com,danny@mail.com,jerry@mail.com  

When reading the nativeSheet to a collection of `Users`, the column "Emails" will be deserialized to an `ArrayList`.
If you prefer a different collection implementation rather than the default `ArrayList`, you can always extend the `CSVColumnValueConverter` and override the `getCollection()` method to return your preferred implementation.

##### Custom Converters
It is possible to create your own converter. All you need to do is to implement `ColumnValueConverter` interface.  
For example, lets create a converter for our `firstName` property that will lowercase the `firstName` when writing and will uppercase it when reading.  
```java
public class UpperLowerCaseConverter implements ColumnValueConverter<String, String> {
  @Override
  public String serialize(String value) {
    return value.toUpperCase();
  }

  @Override
  public String deserialize(String value) {
    return value.toLowerCase();
  }
}

@Column (name="Firstname", converter = UpperLowerCaseConverter.class)
private String firstName;
```
#### Dynamic Columns
What if you don't know in advance which columns your Excel nativeSheet will hold? For example when your application reads dynamic content and save it to Excel.  
Obviously, a bean won't do any good because you don't know what properties and columns to define.  
For that purpose you can use the @AnyColumn annotation to annotate a ```Map<String, Object>``` property. The map will hold any column you want where the key represents the column name and the value represents the column value.
```java
@AnyColumn
private Map<String, Object> dynamicCols;
```

The map value can be of any type. If the type is not a `Number` or `Date` Xcelite will use the `toString()` of the object upon serializtion. If this is not what you want you can use a converter same way as before:
```java
@AnyColumn(converter = CSVColumnValueConverter.class)
private Map<String, List<String>> dynamicCols;
```

What about reading from Excel sheets using dynamic columns?  

Well, luckily it works both ways. If your bean contains an @AnyColumn property, any column in your Excel nativeSheet that is not mapped to a specific property in your bean will be injected to the @AnyColumn annotated Map property. If a converter is declared then the value will be deserialized using the converter before injected to the map.  
By default, Xcelite will use HashMap implementation for the Map when deserializing. If you'de prefer a different implementation use the 'as' attribute.  
For instance, if you want your map to be sorted by column names using a TreeMap, just do:
```java
@AnyColumn(converter = CSVColumnValueConverter.class, as = TreeMap.class)
private Map<String, List<String>> dynamicCols;
```

In addition, if you want some nativeSheet columns to be skipped from been injected to the map, use:

```java
@AnyColumn(ignoreCols = { "column1", "column2" })
private Map<String, List<String>> dynamicCols;
```

#### Row Post Processors
When reading an Excel nativeSheet you sometimes want to manipulate the data while reading. For example, you want to discard some row or object, or change some data in the deserialized object.  
In order to accomplish that you can add a `RowPostProcessor` to your reader.  
A `RowPostProcessor` is a simple interface which contain a single method `process()` which gets the deserialized Object as an argument and return boolean whether to keep the Object or not.  
```java
private class UserPostRowProcessor implements RowPostProcessor<User> {
  @Override
  public boolean process(User user) {
    return user.getFirstName().startsWith("A");
  }
}
```
In this example we filter out all users which their first name does not start with "A".  
All we have to do now is to register this row post processor in our reader:
```java
SheetReader<User> reader = nativeSheet.getBeanReader(User.class);
reader.addRowPostProcessor(new UserPostRowProcessor());
```
Note that you can register as many row post processors as you like. They will be executed in ordered manner.


#### Xcelite example Spring web application

Currently work in progress, see https://github.com/iSnow/xcelite-web

### Utils
#### XceliteDiff
Xcelite package provides a simple and easy-to-use utility which takes two `SheetReader` classes and compares them:
```java
Xcelite xceliteA = new Xcelite(new File("usersA.xlsx"));
SheetReader<User> readerA = xceliteA.getSheet("users").getBeanReader(User.class);
Xcelite xceliteB = new Xcelite(new File("usersB.xlsx"));
SheetReader<User> readerB = xceliteB.getSheet("users").getBeanReader(User.class);
DiffResult<User> diffResult = XceliteDiff.diff(readerA, readerB);
assertTrue("Sheets are not identical!\n" + diffResult.getReport(), diffResult.isIdentical());
```
Needless to say, your bean must implement `hashCode()` and `equals()` if you wish to have meaningful symmetric difference results.  
If you want to use a different report rather than the one provided by `XceliteDiff`, it is possible to do:
```java
DiffResult<User> diffResult = XceliteDiff.diff(readerA, readerB, new ReportGenerator() {
      @Override
      public <T> String generateReport(Info<T> info) {
        // return your own report based on the provided info
      }
    });
```
### How To Use
#### Using Xcelite in Your Maven Project
Add xcelite as a dependency:
```xml
<dependency>
	<groupId>io.xcelite.spreadsheet</groupId>
	<artifactId>xcelite</artifactId>
	<version>1.1.0</version>
</dependency>
```

#### Using Xcelite in Your Gradle Project
Add xcelite as a dependency:
```gradle
compile group: 'io.xcelite.spreadsheet', name: 'xcelite', version: '1.1.0'
```

#### Using Xcelite with other build systems
See https://mvnrepository.com/artifact/io.xcelite.spreadsheet/xcelite/1.1.0

# Tenshi's Java Language Extensions

Tenshi uses a number of extensions to make the java code more readable and to avoid repetitive code.

# Function Interfaces

Tenshi includes multiple classes providing generic function interfaces (eg. for callbacks):

**Return/Parameters**|**No Parameters**|**1 Parameter**|**2 Parameters**
:-----:|:-----:|:-----:|:-----:
Without Return|-|Consumer|BiConsumer
With Return|Action|Function|BiFunction

# LanguageUtils

The main extension class. <br>
This class is 'historically grown' and may require some cleanup...

## Usage
```java
import static io.github.shadow578.tenshi.lang.LanguageUtils.*;
```
<br>
<br>

## listOf
Creates a list of values.

Instead of
```java
ArrayList<String> list = new ArrayList<>();
list.add("1");
list.add("2");
...
```
Write
```java
ArrayList<String> list = listOf("1", "2", ...);
```


### __Also works to join collections together:__
Instead of
```java
ArrayList<String> list = new ArrayList<>();
list.addAll(listA);
list.addAll(listB);
...
```
Write
```java
ArrayList<String> list = listOf(listA, listB, ...);
```
## isNull/ notNull
Null check operation

Instead of
```java
if(obj == null)
    // obj is null
...
if(obj != null)
    // obj is not null
```
Write
```java
if(isNull(obj))
    // obj is null
...
if(notNull(obj))
    // obj is not null
```

## with
Run a function with a null- checked parameter

Instead of
```java
if(obj != null)
{
    function(obj);
}
```
Write
```java
with(obj, p -> function(obj));
```

### __usage with default parameter value__
Instead of
```java
if(obj != null)
{
    function(obj);
}
else
{
    function("fallback");
}
```
Write
```java
with(obj, "fallback", p -> function(p));
```

### __with return value__
Instead of
```java
Object result = null;
if(obj != null)
{
    result = function(obj);
}
```
Write
```java
Object result = withRet(obj, p -> function(p));
```

__with Default Value__

```java
Object result = new Object();
if(obj != null)
{
    result = function(obj);
}
```
Write
```java
Object result = withRet(obj, new Object(), p -> function(p));
```

### __conversion of parameter to string__
Instead of
```java
if(obj != null)
{
    function(obj.toString());
}
else
{
    function("fallback");
}
```
Write
```java
withStr(obj, "fallback", p -> function(p));
```

## elvis
Imitates the elvis (?:) parameter in other languages

Instead of
```java
String s = x != null ? x : "foo";
```
Write
```java
String s = elvis(x, "foo");
```

## elvisEmpty
Similar to the elvis operator, but for strings. Check if the string is null or empty.

Instead of
```java
String s = (x != null && !x.isEmpty()) ? x : "foo";
```
Write
```java
String s = elvisEmpty(x, "foo");
```

## nullSafe
Run a function swallowing all NullPointerExceptions.
Warning: this does not differentiate NullPointerExceptions because a variable was null and ones because of some other error => this may swallow unwanted / unexpected Exceptions, so handle with care.

Instead of
```java
String result = null;
if(response != null)
{
    if(response.data != null)
    {
        if(response.data.node != null)
        {
            ...
            result = function(response.data.node);
        }
    }
}
```
Write
```java
String result = function(nullSafe(response, p -> p.data.node));
```

By default, nullSafe returns null if a NullPointerException is thrown. But a default value can be assigned too.

## fmt
Shorthand for String.format() AND NumberFormat.getInstance().format() depending on parameters.<br>
The shorthand for String.format() also accepts android string resources

### __String.format__
Instead of
```java
String s1 = String.format("test %d", 2);
```
Write
```java
String s = fmt("test %d", 2);
```

__AND__

Instead of
```java
String s1 = String.format(this.getString(R.string.some_string), 2);
```
Write
```java
String s = fmt(this, R.string.some_string, 2);
```

### __NumberFormat.format__
Instead of
```java
String s1 = NumberFormat.getInstance().format(2000);
```
Write
```java
String s = fmt(2000);
```

## concat
Concatenate multiple strings, without delimiter

Instead of
```java
String s = "abc" + someStringVariable + "ghi";
```
Write
```java
String s = concat("abc", someStringVariable, "ghi")
```

## join
Concatenate multiple strings, with delimiter
Instead of
```java
String s = s1 + ", " + s2 + ", " + ...;
```
Write
```java
String s = join(", ", s1, s2, ...);
```

### __also works with collections__
Instead of
```java
List<SomeDataClass> list = ...;
StringBuilder builder = new StringBuilder();
for(SomeDataClass item : list)
{
    builder.append(item.someValue).append(", ");
}

String s = builder.toString();
```
Write
```java
List<SomeDataClass> list = ...;
String s = join(", " list, itm -> itm.someValue);
```

## nullOrEmpty
Check if a string or list is null __OR__ empty

Instead of
```java
if(someString != null && !someString.isEmpty())
{
    // do something
}
```
Write
```java
if(!nullOrEmpty(someString))
{
    // do something
}
```

## nullOrWhitespace
Check if a string is null, empty, or only whitespace

Instead of
```java
if(someString != null && !someString.isEmpty() && !someString.trim().isEmpty())
{
    // do something
}
```
Write
```java
if(!nullOrWhitespace(someString))
{
    // do something
}
```

## foreach
As the name implies, a foreach loop. 
However, this one does allow the use of null collections.

Instead of
```java
List<String> someList = ...
if(someList != null)
{
    for(String itm : someList)
    {
        // do something
    }
}
```
Write
```java
List<String> someList = ...;
foreach(someList, itm -> {
    // do something
})
```

### __Or, if you need the index as well__
Instead of
```java
List<String> someList = ...
if(someList != null)
{
    for(int i = 0; i < someList.size(); i++)
    {
        String itm = someList.get(i);
        // do something
    }
}
```
Write
```java
List<String> someList = ...;
foreach(someList, (itm, i) -> {
    // do something
})
```

## collect
Collects the results of a function call for all items in a collection

Instead of
```java
List<MyNiceObject> someList = new List<>();
List<String> results = new List<>();
for(MyNiceObject obj : someList)
{
    results.add(obj.function())
}
```
Write
```java
List<String> results = collect(someList, obj -> obj.function());
```

## repeat
Repeats a function, returning the return values to a collection or array

Instead of
```java
List<String> someList = new List<>();
for(int i = 0; i < 11; i++)
{
    someList.add(function(i))
}
```
Write
```java
List<String> someList = repeat(0, 10, i -> function(i));
```


## cast
Safely cast a object to a type.
If the cast fails (object to cast is null or incompatible types), null is returned.
Also supports defining a default value to return instead of null.

Instead of
```java
ClassB casted = null;
if(obj != null && obj instanceof ClassB)
{
    casted = (ClassB)obj;
}
```
Write
```java
ClassB casted = cast(obj);
```

### __with default value__
Instead of
```java
ClassB casted = new ClassB();
if(obj != null && obj instanceof ClassB)
{
    casted = (ClassB)obj;
}
```
Write
```java
ClassB casted = cast(obj, new ClassB());
```

## async
Run a action on in the background, with a callback that is called on the main (ui) thread.

Instead of using AsyncTask (now Deprecated), just do:
```java
async(this::someLongFunction, r -> Log.i("Test", "async return is " + r));
```

__Or, if parameters are required__
```java
async(() -> someLongFunction("this is a parameter"), r -> Log.i("Test", "async return is " + r));
```
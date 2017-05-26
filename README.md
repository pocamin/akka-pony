# akka-pony : Actors are magic

Akka pony permits to modify an object to change method 
calls into async messages send through akka actors.

> Akka-pony is provided here as a proof of context. 
> You are strongly invited to do pull request if you want to improve akka-pony 

## Quick start

### Maven dependency

```xml
<dependency>
    <groupId>org.pocamin</groupId>
    <artifactId>akka-pony</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Akka pony has been tested with akka 2.4.14. Javassist 3.20.0-GA is the only
transitively provided dependency.

> Currently akka-pony is not available on maven central repository 
> Akka pony will use semantic versioning.

### Create a java pony

A java pony is an instrumented class that accept asynchronous call backed by akka actor.

#### Decorated methods

A method will be called asynchronously if it returns a FutureReturn object.
```java
public FutureReturn<String> decorate(String toDecorate) {
   return shouldReturn("Applejack says > " + toDecorate);
}
```

If a method normally should return void. To be asynchronous it should return a FutureReturn<Void>
```java
public FutureReturn<Void> eatAnApple(Apple apple) {
   this.eat(apple);
   return FutureReturn.VOID;
}
```


#### Create a new instance :
```java
Fluttershy fluttershy = Pony.newPony(Fluttershy.class, actorSystem);
``` 

> Notice Pony create a new object and don't instrument an existing one. 

You can pass arguments during creation. Let's Rarity knows about Fluttershy : 
```java
Rarity rarity = Pony.newPony(Rarity.class, actorSystem, fluttershy);
```
> ​Instead of actor system you can directly pass actors (witch preserve actor hierarchy)
> It has been designed by taking cate of Spring integration possibility (Either with prototype or singleton).

#### Using FutureReturns

Future returns can be used in asynchronous or synchronous context :

##### Asynchronous
If you don't care about the result of a call you just ignore it
```java
fluttershy.eatApple(Apple.RED);
fluttershy.eatApple(Apple.GREEN);
rarity.eatApple(Apple.PURPLE);
```
 
> Since futtershy and rarity are backed bu different actor Red and Green apple will eaten in a deterministic order.
> But no guarantee is given for Purple apple. 
 
If you do care it is you probably want to do some operations given the result. 
You can always forward the result of an operation to another actor (Akka actor or pony). You can possibly do that in cascade

```java
public class Applejack {
    public FutureReturn<String> decorateStringWithFriends(String toDecorate) {
     return rarity.decorateString(toDecorate)
     .forward(fluttershy::decorateString)
     .forward(this::decorateString);
    }​
    
    FutureReturn<String> decorateString(string toDecorate){
        return FutureReturn.shouldReturn("Applejack says > " + toDecorate);
    }
}​
```

Here : 
- ​Applejack is asked to decorate a string with friends (He receive an akka message)
- Applejack tells Rarity to do so. (Again a message) 
- When Rarity successfully decorates he sends a message to Fluttershy to decorate. 
- Then Fluttershy asks applejack to complete

And AppleJack return the resulting futureReturn.

> Pony use lambda to forward message. 

It is important to notice that this lambda get proceed by the called actor
here the actual call to this::decorateString will be done during fluttershy execution

It's equivalent to 
```java
class FluttershyActor {
    handleDecorateMessage(String message){
        Applejack.tell(DecorateMessage(message));
    }
}
```
For this example it is what we want but this can be not correct if the lambda get some complex and/or slow  logic.
```java 
public FutureReturn<String> decorateStringWithFriends(String toDecorate) {
 return rarity.decorateString(toDecorate)
     .forward(message -> {
         this.doSomeComplexAndSlowStuff();
         return fluttershy.decorateString(message)
     });
     .forward(this::decorateString);
}​
```
doSomeComplexAndSlowStuff will be done in the rarity actor:
- If it changes state of this it will break akka state management => Thread safety is not anymore assured
- It will block rarity for something that doesnt belong to rarity => Hard to tune performance

:boom: :boom: :boom: **Only forward on forward method** :boom: :boom: :boom:
 
> Some simple routing logic are completely fine although  


##### Synchronous
It is sometime very useful to get the result of a method call. 
It should be considered only on actor with appropriate dispatcher or on a non actor context.

A futureResult is also a java Future. Hence you can block the thread waiting for the result :
```java
@Test
public void testSimpleCall() throws InterruptedException, ExecutionException, TimeoutException {
   FutureReturn.Result<String> result = applejack.decorateString("Actor Is Magic").get(2, SECONDS);
   Assert.assertEquals("Applejack says > Fluttershy says > Rarity says > Actor Is Magic", result.getValue());
}
```

#### Error management

Ponies eat exception at breakfast
```java
public FutureReturn<Void> useRoundup() {
   throw new IllegalAccessError("No thanks");
}
```
> Checked exceptions are also supported nonetheless there is possible confusion since error will 
> never be catchable in a try...catch block. **We advice not to use checked exception in pony**  


At this stage Akka pony doesnt rely on akka to do error management.
Error management should be managed by the overloaded forward method :

```java
@Test
public void testSimpleCallWithForwardError() throws InterruptedException, ExecutionException, TimeoutException {
   Assert.assertEquals("Fluttershy says > Applejack says > No thanks",
         applejack.useRoundup()
               .forward((value, error) -> applejack.selfDecorate(error.getMessage()))
               .forward(value -> fluttershy.decorateString(value))
         .toFuture().get(2, SECONDS).getValue());
}
```
When an error occurs and is not managed (We use forward without error) the chain is broken and the resulting FutureReturn will be in error 


#### Killing a pony
Killing a pony has never been as fun as killing with akka pony. If you want to kill your pony you just have to do

```java
Pony.kill(rarity, SAFE);
```

The underlying actor will be killed through a poison pills 

MODE|Behaviour
----|-----
SAFE|All future calls to a instrumented method will throw a PonyAlreadyKilledException  
UNSAFE|All future calls to a instrumented method will be proceed outside of actor. This is unsafe for thread safety and forward calls are proceed inside actor that should forward  

You cannot reanimate a pony.


#### Performance

Akka pony has been designed in order to limit performance impact on Akka actor. 
Here is some metrics for multiple actors calling a single actor on my machine (Not a fast one)
#Actor | #Messages |  Akka | pony | Synchronized call with work stealing pool of 5 threads 
---|---|---|--- | ---
3+1|10 000|54 ms|100 ms|18 ms
3+1|100 000|153 ms|263 ms|62 ms
3+1|1 000 000|698 ms|1172 ms|231 ms
100+1|1 000 000| 685ms| 870ms| 252ms
1000+1|1 000 000| 598ms| 1153ms| 228ms
10000+1|1 000 000| 636ms| 1315ms| 246ms
1000000+1|1 000 000| 739ms| 1094ms| 251ms 


> Obviously Pony is slower than Akka. Akka is also slower than old synchronization mechanism in certain cases
>
> We are choosing actor-based architecture for case where the abstraction is more important than the potential 
> performance impact
>
> The same applies to akka-pony, if your system would handle less than 1 million messages per seconds akka pony 
> should be able to handle them on a recent server. If not maybe you should even reconsider using akka.





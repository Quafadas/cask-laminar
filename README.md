# cask-laminar-stack

Example of laminar put on top of the so called "Singaporean stack". The aim is simplicitly... as far as possible inside the requirements of; 
- reload on change
- database access
- UI Interactions on (multiple!?!) single page applications
- Each with (potentially interactive!) data viz
- zero complicated effect frameworks! No scalaz, no cats, no zio. KISS :-).
- JSON API
- Shared client / server code

1. Client written with [Laminar](https://github.com/raquo/Laminar), interacting with server using laminars native Ajax integration

2. Server with [cask](https://com-lihaoyi.github.io/cask/) serving both the compiled Javascript for the client and the classic CRUD api for server-side interactions for simple deployment / development

3. Routing is via a semi safe custom "API concept" - it's worked well for me. YMMV. Ideally, there would be tapir or endpoints4s... but none support cask... and that added complexity.

4. ScalaJS bundler for JS dependancies

5. Database interactions via quill

6. Testing with uTest - front and back

7.  HTTP requests to other backend services through [requests scala](http://www.lihaoyi.com/post/HowtoworkwithHTTPJSONAPIsinScala.html).

8. Client server interactions are via [upickle](https://www.lihaoyi.com/post/HowtoworkwithJSONinScala.html). Although this is largely abstracted away by the API - pattern match it! 



## Development mode

Run in SBT (uses fast JS compilation, not optimized):

```
sbt> ~runDev
```

And open http://localhost:8080/

This will restart the server on any changes: shared code, client/server, assets. You should see "hello" if the server is working. 

Now you'll need a database. Have a look at the DB folder in modules and the "application.conf" file.

## Tests
It is a prerequisite to have jsdom installed, in order for the frontend tests to run. Proposal:
```
yarn add jsdom
```
Then move into an sbt console and run tests as normal

## What of scala 3? 
In theory, just change the scalaVersion to 3.0.0... 

Practically scala js bundler falls over, unclear why at this stage, help anyone?

## Production mode
NOT DONE FOR THIS TEMPLATE as it's primarily aimed at proving concepts. PR welcome should you care :-)!

Run in SBT (uses full JS optimization):

```
sbt> ~runProd 
```

## Docker packaging 
NOT DONE FOR THIS TEMPLATE as it's primarily aimed at proving concepts. PR welcome should you care :-)!

```
sbt> backend/docker:publishLocal
```

Will publish the docker image with fully optimised JS code, and you can run the container:

```bash
✗ docker run --rm -p 8080:8080 backend:0.1.0-SNAPSHOT

SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Running server on http://0.0.0.0:8080 (mode: prod)
```
## Development mode

Run in SBT (uses fast JS compilation, not optimized):

```
sbt> ~runDev
```

And open http://localhost:8080/

This will restart the server on any changes: shared code, and copy client assets to the right place. You should see "hello" if the server is working at the root url. It's pretty easy to see where that comes from :-). 

Then try http://localhost:8080/todo. You'll probably get errors if you didn't setup the database... 

Have a look at the DB folder in modules and the "application.conf" file to wire up postgres.

# cask-laminar-stack

Example of laminar put on top of the so called "Singaporean stack". The aim is simplicitly... as far as possible inside the requirements of; 
- reload on (any) change
- database access
- Modern UI interactions
- Data viz (interactive!)
- Zero complicated effect frameworks! No scalaz, no cats, no zio, in fact, no asynchrony at all!
- JSON API
- Shared client / server code

The choices made... and POC problems solved in this template.

1. Client written (ahem, stolen, thanks raquo) with [Laminar](https://github.com/raquo/Laminar), interacting with server using laminars native Ajax integration

2. Server with [cask](https://com-lihaoyi.github.io/cask/) serving both the compiled Javascript for the client and the classic CRUD api for server-side interactions for simple deployment / development

3. Backend routing is via a questionable semi safe custom API concept - it's worked well for me. YMMV. Ideally, there would be tapir or endpoints4s... but none support cask... and adding them added complexity against the principle above.

3. Part deux : Frontend routing via waypoint

4. ScalaJS bundler for JS dependancies (Vega in this case)

5. Database interactions via quill

6. Testing with uTest - front and back

7.  HTTP requests to other backend services through [requests scala](http://www.lihaoyi.com/post/HowtoworkwithHTTPJSONAPIsinScala.html).

8. Client server parsing are via [upickle](https://www.lihaoyi.com/post/HowtoworkwithJSONinScala.html). Although this is largely abstracted away by the API - pattern match it! 

9. Simple examples of writing custom JS facades for Vega View.

10. Vega Embed is sucked in through Scalably typed

11. Scala JS Bundler has a setup which allows "live reloading" in the "todo" project

## Real development mode
Whilst the above works, and it's easy to get started... the feedback loop is too slow for frontend development, and we have to refresh the browser with every change! That won't do... let's split the workflow into frontend and backend. 

1. Start two sbt shells
2. In the first shell, "project backend" to switch to the backend project. 
3. In the back project, ~reStart ... this will restart the backend on any change
4. In the second shell switch to the todo project "project todo"
5. start a webpack dev server... we have a config laid out in the root of that project... in sbt type "webpack::startWebpackDevServer"
6. once webpack is up and running ~fastOptJS::webpack... and you should see all change reload without intervention in the browser on the frontend. 
7. if you wish to stop it... webpack::stopWebpackDevServer

Note: The webpack config (which is being served on localhost:3000) proxies Ajax requests to port 8080/api, i.e. assumes you have kept that as the backend port, and have requests to the backend running through the "api" path. 

## Further ideas
1. Incorporate waypoint for client side routing
2. Frontend testing is non-existent (!)
3. Scala 3 (wait for the genius maintainers of quill)

## Credits
This is nothing other than glue around the genius of others. Thankyou to the maintainers of all the libraries included here. They are great.

## Tests
It is a prerequisite to have jsdom installed, in order for the frontend tests to run. Proposal:
```
yarn add jsdom
```
Then move into an sbt console and run tests as normal

## What of scala 3? 
Quill does not yet support it's full feature set on scala 3 (to my understanding). If you don't need a database though...

Just change the scalaVersion to 3.0.0... 

## Production mode
Run in SBT (uses full JS optimization):

```
sbt> ~runProd 
```

## Docker packaging 
NOT TESTED FOR THIS TEMPLATE as it's primarily aimed at proving concepts, getting started and experimentation. PR welcome should you care :-)!

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

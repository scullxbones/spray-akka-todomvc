### Demo Backbone.js TODO MVC application 
<sub><sup>... backed by RESTful Akka-Http/Slick stack</sup></sub>

Travis CI says: [![Build Status](https://travis-ci.org/scullxbones/spray-akka-todomvc.png?branch=master)](https://travis-ci.org/scullxbones/spray-akka-todomvc)

  * Stack based on backbone.js, akka-http, and slick
  * Supports PostgreSQL as a database
  * Uses bulkhead execution context for database I/O
  * Fully asynchronous restful services supporting simple todo mvc app
  * Packaged for docker container deployments using `sbt-native-packager`
  * Adds a docker-compose template to run locally including postgresql

If you already have postgres running, you can run and test it using SBT invoke: 'sbt run'

If not, or you'd like to try a container, you can stage a container and then use docker-compose to start it up:

```
$ sbt docker:publishLocal
$ docker-compose up
```

Future work:
  * Convert to multi-tenant?
  * Live websocket-based updates?
  * Find a better way to stream results without `runFold`ing

Licensed Apache Public License V2

   Copyright 2013-2016 Brian Scully

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

Demo Backbone.js TODO MVC application backed by RESTful Spray/Akka/Slick stack.

Travis CI says: [![Build Status](https://travis-ci.org/scullxbones/spray-akka-todomvc.png)](https://travis-ci.org/scullxbones/spray-akka-todomvc)

  * Almost a full Typesafe Stack (http://typesafe.com/stack).  Opted for Spray (http://spray.io) instead of Play (http://playframework.com).
  * Runs on spray-can for server
  * Built originally by giter8 template for akka 2.1.0 using Scala and SBT.
  * Supports in-memory H2 in dev mode or PGSQL in cloud mode
  * Uses bulkhead actor for database I/O
  * Fully asynchronous restful services
  * Packaged for deployment to heroku using sbt start-script plugin

To run and test it using SBT invoke: 'sbt run'

Future work:
  * Fix POST where id already exists - should be 303 See Other
  * Add travis CI
  * Convert to multi-tenant
  * Integrate with app direct APIs (http://www.appdirect.com/)


Licensed Apache Public License V2

   Copyright 2013 Brian Scully

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

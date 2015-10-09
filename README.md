# Scala Sandbox #

#### ...with Scala, Scalatra, and Atmosphere Websockets! Special appearance by comedian Long Running Process. Music by DJ Drag'n'Drop. Warm up by a Trello Impersonator! Set design by Bootstrap. Catering by KnockoutJS.

## Goal
This is a little R&D project to test Atmosphere with Scalatra, as well as to reaseach resilience, best practices, and client techniques. 

## Status
First pass at a simple websocket - adding one "Trello" card shows up in other clients in real-time.

## Build & Run ##

**All** you need is Java installed.

```sh
$ cd scalatra-sandbox
$ ./sbt
> container:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.

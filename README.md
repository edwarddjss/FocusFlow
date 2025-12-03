Study timer app built with Java Swing for our OOP final project.

## Features

- **Timer** - Pomodoro, 52/17, Ultradian, and Custom modes with visual countdown
- **History** - Track completed sessions with stats (total focus time, completion rate, streaks)
- **Planner** - Calendar view with AI chat assistant for scheduling study blocks
- **Settings** - Volume control, API key config, timer customization

## How to run

Make sure you have Java 22 and Maven installed.

```
mvn compile
mvn exec:java -Dexec.mainClass="com.focusflow.Main"
```

Or build the jar:
```
mvn package
java -jar target/focusflow-1.0.0.jar
```

## Running tests

```
mvn test
```

## Design patterns used

- Observer - timer events notify UI panels
- Singleton - TimerManager, SessionLogger, SettingsController
- Command - timer controls (start, pause, reset, skip)
- MVC - views, controllers, models separation
- Strategy - different timer modes
- Iterator - session history traversal

## AI Assistant

The Planner tab has an AI chat that can help schedule study blocks. To use it, get a free API key from console.groq.com and enter it in Settings.

## Team

- Frank Watkins - fwatkins2020@fau.edu
- Gianluca Binetti - gbinetti2020@fau.edu
- Edward De Jesus - edejesus2020@fau.edu
- Fareed Uddin - Mfareeduddin2023@fau.edu

FAU Fall 2025

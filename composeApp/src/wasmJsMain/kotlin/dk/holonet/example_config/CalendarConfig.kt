package dk.holonet.example_config

val calendarConfig = """
    {
      "name": "Calendar",
      "description": "A simple calendar plugin",
      "author": "Holonet",
      "version": "1.0.0",
      "config": {
        "url": {
          "type": "string",
          "description": "The URL to the calendar feed",
          "default": "",
          "required": true
        }
      }
    }
""".trimIndent()
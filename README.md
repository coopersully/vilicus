# Vilicus
Vilicus, named after the Latin word for "steward," is a server management tool designed for Minecraft Java Edition servers designed
to make server management as streamlined as possible. Once installed, run the program and follow the prompts to set up your server.

> Although it may work on other distributions, the tool is primarily developed to run on Ubuntu Server. Some features,
such as ones that include file management, may not work properly on other distros.

## Features
Vilicus comes equipped with several features to make server management more efficient, including:

* Automatic updates to the latest version of Purpur API
* Automatic plugin file organization for easier server management
* More features coming soon!

## Configuration
Here's a look at the default [`config.yml`](https://github.com/coopersully/vilicus/blob/main/src/main/resources/config.yml) generated by Vilicus upon its first startup:

```yml
#                 __        __
#  \  / | |    | /  ` |  | /__`
#   \/  | |___ | \__, \__/ .__/
#
# Vilicus is a server-management tool for Minecraft: Java Edition.
# Author: Cooper Sullivan (https://github.com/coopersully/vilicus)

# Should we automatically check for & install API updates?
update_api: true

# Should we automatically organize your plugins directory by
# renaming all plugins to match the scheme: "NAME-VERSION.jar"?
update_plugin_names: true
```
